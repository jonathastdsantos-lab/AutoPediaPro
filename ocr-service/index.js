// =========================================================
// AutoPedia OCR Service
// Serviço isolado, porta própria (3002), para não conflitar com:
//   - corta-vc-worker (porta 3001)
//   - Evolution API (Docker, Sublime Gestão)
//
// O que faz: recebe um PDF (URL ou upload), converte cada página em
// imagem via Ghostscript, roda Tesseract OCR em cada imagem, e devolve
// o texto extraído por página em JSON — no mesmo formato que a Edge
// Function processar-manual já espera de extrairTextoPorPagina.
//
// Por que isso fica no VPS e não em Edge Function:
//   - OCR é pesado de CPU e pode levar minutos num manual de 50+ páginas
//   - Edge Functions do Supabase têm timeout curto, não são feitas pra
//     isso
//   - O VPS já tem CPU disponível e está pago, sem custo incremental
// =========================================================

import express from 'express';
import cors from 'cors';
import { exec } from 'child_process';
import { promisify } from 'util';
import fs from 'fs/promises';
import path from 'path';
import os from 'os';
import crypto from 'crypto';

const execAsync = promisify(exec);

const app = express();
app.use(cors());
app.use(express.json({ limit: '50mb' }));

const PORT = process.env.OCR_SERVICE_PORT || 3002;
const OCR_SECRET = process.env.OCR_SERVICE_SECRET; // mesmo padrão do WORKER_SECRET do Corta.vc

// Middleware de autenticação simples — mesmo padrão x-worker-secret
// já usado no corta-vc-worker, pra manter consistência entre os serviços
function checarSecret(req, res, next) {
  const secretRecebido = req.header('x-ocr-secret');
  if (!OCR_SECRET) {
    return res.status(500).json({ error: 'OCR_SERVICE_SECRET não configurado no servidor' });
  }
  if (secretRecebido !== OCR_SECRET) {
    return res.status(401).json({ error: 'Secret inválido ou ausente' });
  }
  next();
}

// Health check — sem autenticação, só confirma que o serviço está de pé
// e que os binários necessários (gs, tesseract) existem no sistema
app.get('/health', async (req, res) => {
  try {
    const [gsVersion, tesseractVersion] = await Promise.all([
      execAsync('gs --version').then((r) => r.stdout.trim()).catch(() => null),
      execAsync('tesseract --version').then((r) => r.stdout.split('\n')[0]).catch(() => null),
    ]);

    res.json({
      ok: true,
      ghostscript: gsVersion !== null,
      ghostscript_version: gsVersion,
      tesseract: tesseractVersion !== null,
      tesseract_version: tesseractVersion,
      uptime: process.uptime(),
    });
  } catch (err) {
    res.status(500).json({ ok: false, error: String(err) });
  }
});

// =========================================================
// POST /ocr-pdf
// Body: { pdf_url: string, idioma?: string }
// idioma segue o código do Tesseract: "por" (português), "eng" (inglês)
// Default "por" porque os manuais são majoritariamente em português.
// =========================================================
app.post('/ocr-pdf', checarSecret, async (req, res) => {
  const { pdf_url, idioma = 'por' } = req.body;

  if (!pdf_url) {
    return res.status(400).json({ error: 'pdf_url é obrigatório' });
  }

  // Diretório de trabalho isolado por requisição, em /tmp — evita
  // colisão entre processamentos simultâneos e facilita limpeza
  const idExecucao = crypto.randomUUID();
  const dirTrabalho = path.join(os.tmpdir(), `autopedia-ocr-${idExecucao}`);

  try {
    await fs.mkdir(dirTrabalho, { recursive: true });

    const caminhoPdf = path.join(dirTrabalho, 'documento.pdf');

    // 1. Baixa o PDF
    const respostaFetch = await fetch(pdf_url);
    if (!respostaFetch.ok) {
      throw new Error(`Falha ao baixar PDF: status ${respostaFetch.status}`);
    }
    const bufferPdf = Buffer.from(await respostaFetch.arrayBuffer());
    await fs.writeFile(caminhoPdf, bufferPdf);

    // 2. Ghostscript converte cada página em PNG separado
    // -r200: resolução 200dpi, equilíbrio entre qualidade de OCR e
    // tamanho/tempo de processamento — manuais técnicos geralmente não
    // precisam de mais que isso para texto legível
    const padraoSaidaPng = path.join(dirTrabalho, 'pagina-%03d.png');
    await execAsync(
      `gs -dNOPAUSE -dBATCH -sDEVICE=png16m -r200 -sOutputFile="${padraoSaidaPng}" "${caminhoPdf}"`
    );

    // 3. Lista as imagens geradas, em ordem
    const arquivos = await fs.readdir(dirTrabalho);
    const paginasPng = arquivos
      .filter((nome) => nome.startsWith('pagina-') && nome.endsWith('.png'))
      .sort(); // ordenação lexicográfica funciona por causa do zero-padding %03d

    if (paginasPng.length === 0) {
      throw new Error('Ghostscript não gerou nenhuma página de imagem — PDF pode estar corrompido');
    }

    // 4. Tesseract roda em cada página, sequencialmente
    // (sequencial, não paralelo, para não disputar todos os cores da
    // CPU de uma vez só e afetar o worker do Corta.vc rodando ao lado)
    const textoPorPagina = [];
    for (const arquivoPng of paginasPng) {
      const caminhoImagem = path.join(dirTrabalho, arquivoPng);
      const caminhoSaidaBase = path.join(dirTrabalho, arquivoPng.replace('.png', ''));

      await execAsync(
        `tesseract "${caminhoImagem}" "${caminhoSaidaBase}" -l ${idioma}`
      );

      const textoExtraido = await fs.readFile(`${caminhoSaidaBase}.txt`, 'utf-8');
      textoPorPagina.push(textoExtraido.trim());
    }

    res.json({
      sucesso: true,
      total_paginas: textoPorPagina.length,
      texto_por_pagina: textoPorPagina,
    });
  } catch (err) {
    console.error('Erro no OCR:', err);
    res.status(500).json({ error: String(err) });
  } finally {
    // Limpeza: sempre remove o diretório temporário, mesmo se algo
    // falhou no meio — não queremos acumular PDFs/PNGs em /tmp
    await fs.rm(dirTrabalho, { recursive: true, force: true }).catch(() => {});
  }
});

app.listen(PORT, '0.0.0.0', () => {
  console.log(`AutoPedia OCR Service rodando na porta ${PORT}`);
});
