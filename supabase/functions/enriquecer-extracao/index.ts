// =========================================================
// Edge Function: enriquecer-extracao
// O que faz:
//   1. Recebe extracao_staging_id, campos_faltantes e o contexto
//   2. Usa a GEMINI_API_KEY para buscar ou deduzir os dados faltantes
//   3. Atualiza a linha em extracoes_staging com os dados mesclados
//   4. Registra no log de auditoria (enriquecimento_web_log)
// =========================================================

import { createClient } from 'https://esm.sh/@supabase/supabase-js@2'

const supabase = createClient(
  Deno.env.get('SUPABASE_URL')!,
  Deno.env.get('SUPABASE_SERVICE_ROLE_KEY')!
)

const GEMINI_API_KEY = Deno.env.get('GEMINI_API_KEY')!

Deno.serve(async (req) => {
  const startTime = Date.now()
  let extracaoStagingId = ''
  let camposFaltantes: string[] = []
  let contexto: Record<string, unknown> = {}

  try {
    const body = await req.json()
    extracaoStagingId = body.extracao_staging_id
    camposFaltantes = body.campos_faltantes || []
    contexto = body.contexto || {}

    if (!extracaoStagingId) {
      return new Response(JSON.stringify({ error: 'extracao_staging_id é obrigatório' }), { status: 400 })
    }

    // 1. Busca os dados atuais no staging
    const { data: staged, error: errStaged } = await supabase
      .from('extracoes_staging')
      .select('*')
      .eq('id', extracaoStagingId)
      .single()

    if (errStaged || !staged) {
      return new Response(JSON.stringify({ error: 'Extração staging não encontrada' }), { status: 404 })
    }

    const tipoEntidade = staged.tipo_entidade
    const dadosAtuais = typeof staged.dados_extraidos === 'string' 
      ? JSON.parse(staged.dados_extraidos) 
      : staged.dados_extraidos

    // 2. Chama a API do Gemini para realizar o enriquecimento de forma inteligente
    const prompt = `
Você é um especialista em mecânica automotiva brasileira de alta performance e assistente de IA da plataforma AutoPedia.
Recebemos dados parciais extraídos de um manual técnico para uma entidade do tipo "${tipoEntidade}".

Dados já extraídos do PDF:
${JSON.stringify(dadosAtuais, null, 2)}

Precisamos que você enriqueça essa entidade preenchendo as seguintes informações que estão faltando na página original do PDF:
${JSON.stringify(camposFaltantes, null, 2)}

Instruções:
- Deduzi ou utilize sua base de conhecimento automotiva para encontrar valores comuns de mercado no Brasil para esses campos (ex: preço médio de referência, equivalência de marca aftermarket como Bosch, Magneti Marelli, Cobreq, Fram, etc).
- Para códigos de peças alternativos ou torque de aperto padrão, traga dados de mercado confiáveis.
- Retorne APENAS um objeto JSON combinando os dados originais mais os novos campos descobertos.
- Nunca acrescente texto explicativo ou markdown fora do formato JSON.

JSON Esperado:
`

    const geminiUrl = `https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=${GEMINI_API_KEY}`
    
    const response = await fetch(geminiUrl, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        contents: [
          {
            parts: [
              { text: prompt.trim() }
            ]
          }
        ],
        generationConfig: {
          responseMimeType: 'application/json'
        }
      })
    })

    if (!response.ok) {
      throw new Error(`Falha na API do Gemini: ${response.status} - ${await response.text()}`)
    }

    const geminiData = await response.json()
    const responseText = geminiData.candidates?.[0]?.content?.parts?.[0]?.text || '{}'
    const dadosEnriquecidos = JSON.parse(responseText.trim())

    // Combina os dados originais com os enriquecidos para garantir que nada seja perdido
    const dadosFinais = { ...dadosAtuais, ...dadosEnriquecidos }

    // 3. Atualiza os dados na tabela de staging
    const { error: errUpdate } = await supabase
      .from('extracoes_staging')
      .update({
        dados_extraidos: JSON.stringify(dadosFinais),
        fonte_dado: 'pdf_e_web', // indica que foi enriquecido
        confianca: Math.min((staged.confianca || 0.5) + 0.15, 1.0) // eleva a confiança pelo enriquecimento bem sucedido
      })
      .eq('id', extracaoStagingId)

    if (errUpdate) {
      throw errUpdate
    }

    // 4. Registra no log de enriquecimento (enriquecimento_web_log)
    const durationMs = Date.now() - startTime
    await supabase
      .from('enriquecimento_web_log')
      .insert({
        extracao_id: extracaoStagingId,
        provedor_busca: 'gemini_api_enrichment',
        query_utilizada: `Enriquecimento de ${tipoEntidade}: ${camposFaltantes.join(', ')}`,
        resultado_encontrado: JSON.stringify(dadosEnriquecidos),
        tempo_resposta_ms: durationMs,
        sucesso: true
      })

    return new Response(
      JSON.stringify({
        sucesso: true,
        extracao_staging_id: extracaoStagingId,
        campos_enriquecidos: camposFaltantes,
        dados_finais: dadosFinais
      }),
      { headers: { 'Content-Type': 'application/json' } }
    )

  } catch (err) {
    console.error('Erro no enriquecimento da extração:', err)

    // Se houve erro, registra falha no log de auditoria
    if (extracaoStagingId) {
      const durationMs = Date.now() - startTime
      try {
        await supabase
          .from('enriquecimento_web_log')
          .insert({
            extracao_id: extracaoStagingId,
            provedor_busca: 'gemini_api_enrichment',
            query_utilizada: `Falha ao enriquecer: ${camposFaltantes.join(', ')}`,
            resultado_encontrado: JSON.stringify({ error: String(err) }),
            tempo_resposta_ms: durationMs,
            sucesso: false
          })
      } catch (logErr) {
        console.error('Falha ao gravar log de erro:', logErr)
      }
    }

    return new Response(JSON.stringify({ error: String(err) }), { status: 500 })
  }
})
