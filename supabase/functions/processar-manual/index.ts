// =========================================================
// Edge Function: processar-manual
// Trigger: chamada após upload do PDF para o Supabase Storage
// O que faz:
//   1. Recebe o documento_id (já criado em documentos_fonte)
//   2. Extrai o texto do PDF, página por página (via unpdf)
//   3. Para cada página (ou lote de páginas), chama a IA pedindo
//      extração estruturada em JSON
//   4. Grava cada achado em extracoes_staging (NUNCA na tabela final)
//   5. Se algum campo crítico estiver vazio (ex: código de peça),
//      marca para enriquecimento web na próxima etapa
// =========================================================

import { createClient } from 'https://esm.sh/@supabase/supabase-js@2'
import { extractText, getDocumentProxy } from 'https://esm.sh/unpdf@0.12.1'

const supabase = createClient(
  Deno.env.get('SUPABASE_URL')!,
  Deno.env.get('SUPABASE_SERVICE_ROLE_KEY')! // service role: só Edge Function acessa staging com escrita direta
)

const ANTHROPIC_API_KEY = Deno.env.get('ANTHROPIC_API_KEY')!

// Prompt fixo que define exatamente o formato de saída.
// Forçar JSON estrito evita texto solto que quebraria o parser.
const SYSTEM_PROMPT = `
Você é um extrator técnico especializado em manuais automotivos brasileiros
(carros, motos e caminhões). Você recebe o texto de UMA página de um manual
e deve extrair APENAS o que está explicitamente escrito nessa página.

Nunca invente código de peça, valor numérico ou nome que não esteja no texto.
Se a página não tiver informação relevante, retorne uma lista vazia.

Para cada item encontrado, retorne um objeto com:
- tipo_entidade: "peca" | "codigo_peca" | "problema_cronico" | "sintoma" | "especificacao_motor"
- dados: objeto com os campos relevantes (nome, codigo, fabricante, valor, unidade, etc)
- confianca: número de 0 a 1, sua certeza de que a extração está correta e completa
- campos_faltantes: lista de campos que seriam úteis mas não apareceram nesta página
  (ex: ["preco_referencia", "codigo_equivalente_aftermarket"])

Responda SOMENTE com um array JSON. Sem markdown, sem texto antes ou depois.
`.trim()

interface ExtractedItem {
  tipo_entidade: string
  dados: Record<string, unknown>
  confianca: number
  campos_faltantes: string[]
}

// =========================================================
// Extração real de texto do PDF, página por página.
// =========================================================
async function extrairTextoPorPagina(arquivoUrl: string): Promise<string[]> {
  const resposta = await fetch(arquivoUrl)
  if (!resposta.ok) {
    throw new Error(
      `Falha ao baixar o PDF (status ${resposta.status}) em ${arquivoUrl}`
    )
  }

  const buffer = await resposta.arrayBuffer()
  const pdf = await getDocumentProxy(new Uint8Array(buffer))

  const paginas: string[] = []
  for (let numeroPagina = 1; numeroPagina <= pdf.numPages; numeroPagina++) {
    const pagina = await pdf.getPage(numeroPagina)
    const conteudo = await pagina.getTextContent()
    const textoDaPagina = conteudo.items
      .map((item: { str?: string }) => item.str ?? '')
      .join(' ')
      .trim()
    paginas.push(textoDaPagina)
  }

  return paginas
}

async function extrairPagina(textoPagina: string, numeroPagina: number): Promise<ExtractedItem[]> {
  // Página sem texto extraível (provavelmente escaneada/imagem, sem OCR)
  if (textoPagina.trim().length === 0) {
    return []
  }

  const response = await fetch('https://api.anthropic.com/v1/messages', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'x-api-key': ANTHROPIC_API_KEY,
      'anthropic-version': '2023-06-01',
    },
    body: JSON.stringify({
      model: 'claude-3-5-sonnet-20241022',
      max_tokens: 2000,
      system: SYSTEM_PROMPT,
      messages: [
        { role: 'user', content: `Página ${numeroPagina}:\n\n${textoPagina}` }
      ],
    }),
  })

  const data = await response.json()
  const textoResposta = data.content?.[0]?.text ?? '[]'

  try {
    const limpo = textoResposta.replace(/```json|```/g, '').trim()
    return JSON.parse(limpo)
  } catch (err) {
    console.error(`Falha ao parsear resposta da IA na página ${numeroPagina}:`, err)
    return [] // página com erro não derruba o lote inteiro
  }
}

Deno.serve(async (req) => {
  try {
    const { documento_id } = await req.json()

    if (!documento_id) {
      return new Response(JSON.stringify({ error: 'documento_id é obrigatório' }), { status: 400 })
    }

    // 1. Busca o documento e marca como "processando"
    const { data: documento, error: errDoc } = await supabase
      .from('documentos_fonte')
      .select('*')
      .eq('id', documento_id)
      .single()

    if (errDoc || !documento) {
      return new Response(JSON.stringify({ error: 'Documento não encontrado' }), { status: 404 })
    }

    await supabase
      .from('documentos_fonte')
      .update({ status_processamento: 'processando' })
      .eq('id', documento_id)

    // 2. Extrai texto do PDF por página (implementação real via unpdf)
    let paginasTexto: string[]
    try {
      paginasTexto = await extrairTextoPorPagina(documento.arquivo_url)
    } catch (errExtracao) {
      await supabase
        .from('documentos_fonte')
        .update({ status_processamento: 'erro' })
        .eq('id', documento_id)

      return new Response(
        JSON.stringify({
          error: 'Falha ao extrair texto do PDF',
          detalhe: String(errExtracao),
          sugestao: 'Se o PDF for escaneado/imagem sem texto selecionável, é necessário aplicar OCR antes deste pipeline.',
        }),
        { status: 422 }
      )
    }

    if (paginasTexto.every((texto) => texto.trim().length === 0)) {
      await supabase
        .from('documentos_fonte')
        .update({ status_processamento: 'erro', total_paginas: paginasTexto.length })
        .eq('id', documento_id)

      return new Response(
        JSON.stringify({
          error: 'PDF não contém texto extraível (provavelmente escaneado sem OCR)',
          paginas_encontradas: paginasTexto.length,
        }),
        { status: 422 }
      )
    }

    let totalItensExtraidos = 0

    // 3. Processa página por página (permite retomar se cair no meio)
    for (let i = 0; i < paginasTexto.length; i++) {
      const numeroPagina = i + 1
      const itens = await extrairPagina(paginasTexto[i], numeroPagina)

      for (const item of itens) {
        // Decide a origem do dado e se precisa de enriquecimento depois
        const precisaEnriquecimento = item.campos_faltantes.length > 0

        const { data: staged } = await supabase
          .from('extracoes_staging')
          .insert({
            documento_id,
            tipo_entidade: item.tipo_entidade,
            dados_extraidos: JSON.stringify(item.dados),
            pagina_origem: numeroPagina,
            confianca: item.confianca,
            fonte_dado: 'pdf_extracao',
            status_revisao: 'pendente',
          })
          .select()
          .single()

        totalItensExtraidos++

        // 4. Se faltam campos, dispara enriquecimento web pra essa extração
        if (precisaEnriquecimento && staged) {
          try {
            await supabase.functions.invoke('enriquecer-extracao', {
              body: {
                extracao_staging_id: staged.id,
                campos_faltantes: item.campos_faltantes,
                contexto: item.dados,
              },
            })
          } catch (e) {
            console.error('Erro ao invocar enriquecimento web:', e)
          }
        }
      }
    }

    // 5. Marca documento como extraído
    await supabase
      .from('documentos_fonte')
      .update({
        status_processamento: 'extraido',
        total_paginas: paginasTexto.length,
      })
      .eq('id', documento_id)

    return new Response(
      JSON.stringify({
        sucesso: true,
        paginas_processadas: paginasTexto.length,
        itens_extraidos: totalItensExtraidos,
      }),
      { headers: { 'Content-Type': 'application/json' } }
    )
  } catch (err) {
    console.error('Erro no processamento do manual:', err)
    return new Response(JSON.stringify({ error: String(err) }), { status: 500 })
  }
})
