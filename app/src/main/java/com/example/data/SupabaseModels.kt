package com.example.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class SupabaseUsuario(
    val id: String,
    @SerialName("auth_id") val authId: String? = null,
    val nome: String,
    val email: String,
    @SerialName("tipo_usuario") val tipoUsuario: String = "profissional",
    val especialidade: String? = "mecanico",
    val verificado: Boolean = false,
    @SerialName("criado_em") val criadoEm: String? = null
)

@Serializable
data class SupabaseMontadora(
    val id: String,
    val nome: String,
    @SerialName("pais_origem") val paisOrigem: String? = null
)

@Serializable
data class SupabaseMarca(
    val id: String,
    @SerialName("montadora_id") val montadoraId: String,
    val nome: String
)

@Serializable
data class SupabaseModelo(
    val id: String,
    @SerialName("marca_id") val marcaId: String,
    val nome: String,
    @SerialName("tipo_veiculo") val tipoVeiculo: String,
    val segmento: String? = null
)

@Serializable
data class SupabaseGeracao(
    val id: String,
    @SerialName("modelo_id") val modeloId: String,
    val nome: String,
    @SerialName("ano_inicio") val anoInicio: Int,
    @SerialName("ano_fim") val anoFim: Int? = null
)

@Serializable
data class SupabaseVersao(
    val id: String,
    @SerialName("geracao_id") val geracaoId: String,
    val nome: String,
    val carroceria: String? = null,
    val cambio: String? = null
)

@Serializable
data class SupabaseSistema(
    val id: String,
    val nome: String,
    val categoria: String,
    val descricao: String? = null
)

@Serializable
data class SupabasePeca(
    val id: String,
    val nome: String,
    @SerialName("sistema_id") val sistemaId: String? = null,
    val descricao: String? = null
)

@Serializable
data class SupabaseProblemaCronico(
    val id: String,
    val titulo: String,
    @SerialName("descricao_tecnica") val descricaoTecnica: String? = null,
    @SerialName("descricao_simples") val descricaoSimples: String? = null,
    @SerialName("peca_relacionada_id") val pecaRelacionadaId: String? = null,
    @SerialName("dificuldade_reparo") val dificuldadeReparo: String? = null
)

@Serializable
data class SupabaseOficina(
    val id: String,
    val nome: String,
    val cnpj: String?,
    val uf: String,
    val cidade: String,
    val tipo: String,
    val plano: String,
    @SerialName("criado_em") val criadoEm: String? = null
)

@Serializable
data class SupabaseMembroOficina(
    val id: String,
    @SerialName("oficina_id") val oficinaId: String,
    @SerialName("usuario_id") val usuarioId: String,
    val papel: String,
    @SerialName("criado_em") val criadoEm: String? = null
)

@Serializable
data class SupabaseVeiculoAtendido(
    val id: String,
    @SerialName("oficina_id") val oficinaId: String,
    @SerialName("versao_id") val versaoId: String?,
    val placa: String?,
    @SerialName("ano_fabricacao") val anoFabricacao: Int?,
    @SerialName("nome_cliente") val nomeCliente: String?,
    @SerialName("contato_cliente") val contatoCliente: String?,
    val observacoes: String?,
    @SerialName("criado_em") val criadoEm: String? = null
)

@Serializable
data class SupabaseHistoricoAtendimento(
    val id: String,
    @SerialName("veiculo_atendido_id") val veiculoAtendidoId: String,
    @SerialName("realizado_por") val realizadoPor: String?,
    @SerialName("problema_id") val problemaId: String?,
    val descricao: String,
    @SerialName("km_veiculo") val kmVeiculo: Int?,
    @SerialName("valor_total_centavos") val valorTotalCentavos: Long?,
    @SerialName("data_atendimento") val dataAtendimento: String? = null,
    @SerialName("criado_em") val criadoEm: String? = null
)

@Serializable
data class SupabaseHistoricoAtendimentoPeca(
    val id: String,
    @SerialName("historico_id") val historicoId: String,
    @SerialName("peca_id") val pecaId: String,
    val quantidade: Int = 1,
    @SerialName("valor_unitario_centavos") val valorUnitarioCentavos: Long?
)

@Serializable
data class SupabaseRelatoCusto(
    val id: String,
    @SerialName("usuario_id") val usuarioId: String,
    @SerialName("problema_id") val problemaId: String? = null,
    @SerialName("peca_id") val pecaId: String? = null,
    @SerialName("versao_id") val versaoId: String? = null,
    @SerialName("valor_peca_centavos") val valorPecaCentavos: Long? = null,
    @SerialName("valor_mao_obra_centavos") val valorMaoObraCentavos: Long? = null,
    val uf: String,
    val cidade: String?,
    @SerialName("tipo_estabelecimento") val tipoEstabelecimento: String? = null,
    @SerialName("data_servico") val dataServico: String? = null,
    @SerialName("comprovante_url") val comprovanteUrl: String? = null,
    @SerialName("status_verificacao") val statusVerificacao: String = "aprovado"
)

@Serializable
data class SupabasePostComunidade(
    val id: String,
    @SerialName("usuario_id") val usuarioId: String,
    val titulo: String,
    val descricao: String?,
    @SerialName("versao_id") val versaoId: String? = null,
    @SerialName("peca_id") val pecaId: String? = null,
    @SerialName("problema_id") val problemaId: String? = null,
    val resolvido: Boolean = false,
    @SerialName("criado_em") val criadoEm: String? = null
)

@Serializable
data class SupabaseComentario(
    val id: String,
    @SerialName("post_id") val postId: String,
    @SerialName("usuario_id") val usuarioId: String,
    val conteudo: String,
    @SerialName("marcado_como_solucao") val marcadoComoSolucao: Boolean = false,
    @SerialName("criado_em") val criadoEm: String? = null
)

@Serializable
data class SupabaseCredibilidadeUsuario(
    val id: String,
    @SerialName("usuario_id") val usuarioId: String,
    val categoria: String,
    val pontos: Int,
    val nivel: String,
    @SerialName("atualizado_em") val atualizadoEm: String? = null
)

@Serializable
data class SupabaseCustoAgregado(
    val id: String,
    @SerialName("problema_id") val problemaId: String? = null,
    @SerialName("peca_id") val pecaId: String? = null,
    val regiao: String,
    @SerialName("valor_peca_min_centavos") val valorPecaMinCentavos: Long? = null,
    @SerialName("valor_peca_mediana_centavos") val valorPecaMedianaCentavos: Long? = null,
    @SerialName("valor_peca_max_centavos") val valorPecaMaxCentavos: Long? = null,
    @SerialName("valor_mao_obra_min_centavos") val valorMaoObraMinCentavos: Long? = null,
    @SerialName("valor_mao_obra_mediana_centavos") val valorMaoObraMedianaCentavos: Long? = null,
    @SerialName("valor_mao_obra_max_centavos") val valorMaoObraMaxCentavos: Long? = null,
    @SerialName("amostras_consideradas") val amostrasConsideradas: Int,
    @SerialName("atualizado_em") val atualizadoEm: String? = null
)

@Serializable
data class SupabaseExtracaoStaging(
    val id: String,
    @SerialName("tipo_entidade") val tipoEntidade: String, // 'peca' or 'problema_cronico'
    @SerialName("dados_extraidos") val dadosExtraidos: String, // JSON
    val confianca: Double? = null,
    @SerialName("status_revisao") val statusRevisao: String = "pendente", // 'pendente', 'aprovado', 'rejeitado'
    @SerialName("revisado_por") val revisadoPor: String? = null,
    @SerialName("criado_em") val criadoEm: String? = null
)

@Serializable
data class SupabaseEnriquecimentoWebLog(
    val id: String,
    @SerialName("extracao_id") val extracaoId: String,
    @SerialName("fonte_url") val fonteUrl: String,
    @SerialName("criado_em") val criadoEm: String? = null
)

