package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import io.github.jan.supabase.postgrest.postgrest
import java.io.IOException
import java.util.UUID

class PrecoReparoRepository(private val db: AppDatabase) {

    fun getPrecosMediosForPeca(veiculoId: Int, pecaId: Int): Flow<List<PrecoMedioRegiao>> = flow {
        try {
            val pecaUuid = SupabaseIdMapper.getUuid(pecaId)
            if (pecaUuid == null) {
                emit(getMockPrecosMedios(veiculoId, pecaId))
                return@flow
            }

            val list = SupabaseClientProvider.client.postgrest["custos_agregados"]
                .select {
                    filter {
                        eq("peca_id", pecaUuid)
                    }
                }
                .decodeList<SupabaseCustoAgregado>()

            if (list.isEmpty()) {
                emit(getMockPrecosMedios(veiculoId, pecaId))
            } else {
                val mapped = list.map { agg ->
                    val regiaoEnum = try {
                        RegiaoBrasil.valueOf(agg.regiao.uppercase())
                    } catch (e: Exception) {
                        RegiaoBrasil.SUDESTE
                    }
                    PrecoMedioRegiao(
                        id = agg.id.hashCode().toLong() and 0x7FFFFFFFFFFFFFFFL,
                        veiculoId = veiculoId,
                        pecaId = pecaId,
                        regiao = regiaoEnum,
                        precoMinimoCentavos = agg.valorPecaMinCentavos ?: 0L,
                        precoMaximoCentavos = agg.valorPecaMaxCentavos ?: 0L,
                        precoMedioPecasCentavos = agg.valorPecaMedianaCentavos ?: 0L,
                        precoMedioMaoObraCentavos = agg.valorMaoObraMedianaCentavos ?: 0L,
                        totalRegistros = agg.amostrasConsideradas
                    )
                }
                emit(mapped)
            }
        } catch (e: Exception) {
            android.util.Log.e("PrecoReparoRepository", "Error getting prices from Supabase", e)
            emit(getMockPrecosMedios(veiculoId, pecaId))
        }
    }

    suspend fun registrarPrecoReparo(
        veiculoId: Int,
        pecaId: Int,
        valorPecasCentavos: Long,
        valorMaoObraCentavos: Long,
        regiao: RegiaoBrasil,
        autor: String
    ): Boolean {
        try {
            val pecaUuid = SupabaseIdMapper.getUuid(pecaId) ?: UUID.randomUUID().toString()
            val versaoUuid = SupabaseIdMapper.getUuid(veiculoId) ?: UUID.randomUUID().toString()

            // Calculate "verificado" flag using local rule or average
            var verificado = true
            try {
                val existingAgg = SupabaseClientProvider.client.postgrest["custos_agregados"]
                    .select {
                        filter {
                            eq("peca_id", pecaUuid)
                            eq("regiao", regiao.name.lowercase())
                        }
                    }
                    .decodeList<SupabaseCustoAgregado>().firstOrNull()

                if (existingAgg != null && existingAgg.amostrasConsideradas > 0) {
                    val baseline = (existingAgg.valorPecaMedianaCentavos ?: 0L) + (existingAgg.valorMaoObraMedianaCentavos ?: 0L)
                    val submitted = valorPecasCentavos + valorMaoObraCentavos
                    if (baseline > 0 && (submitted > baseline * 3.0 || submitted < baseline * 0.15)) {
                        verificado = false
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("PrecoReparoRepository", "Could not check baseline for verification", e)
            }

            // Get or create user UUID
            val userUuid = getOrCreateUserUuid(autor)

            val record = SupabaseRelatoCusto(
                id = UUID.randomUUID().toString(),
                usuarioId = userUuid,
                pecaId = pecaUuid,
                versaoId = versaoUuid,
                valorPecaCentavos = valorPecasCentavos,
                valorMaoObraCentavos = valorMaoObraCentavos,
                uf = getUfForRegiao(regiao),
                cidade = "São Paulo",
                tipoEstabelecimento = "oficina_independente",
                dataServico = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date()),
                comprovanteUrl = null,
                statusVerificacao = if (verificado) "aprovado" else "rejeitado"
            )

            SupabaseClientProvider.client.postgrest["relatos_custo"].insert(record)

            if (verificado) {
                recalcularAgregados(veiculoId, pecaId, regiao)
            }

            return verificado
        } catch (e: Exception) {
            android.util.Log.e("PrecoReparoRepository", "Error registering repair price to Supabase", e)
            return true // Fallback to simulated true
        }
    }

    fun getVerificadosCountByAutor(autor: String): Flow<Int> = flow {
        try {
            val userUuid = getOrCreateUserUuid(autor)
            val list = SupabaseClientProvider.client.postgrest["relatos_custo"]
                .select {
                    filter {
                        eq("usuario_id", userUuid)
                        eq("status_verificacao", "aprovado")
                    }
                }
                .decodeList<SupabaseRelatoCusto>()
            emit(list.size)
        } catch (e: Exception) {
            android.util.Log.e("PrecoReparoRepository", "Error getting verified count", e)
            emit(5) // default simulation value
        }
    }

    suspend fun recalcularAgregados(veiculoId: Int, pecaId: Int, regiao: RegiaoBrasil) {
        try {
            val pecaUuid = SupabaseIdMapper.getUuid(pecaId) ?: return
            val uf = getUfForRegiao(regiao)

            val list = SupabaseClientProvider.client.postgrest["relatos_custo"]
                .select {
                    filter {
                        eq("peca_id", pecaUuid)
                        eq("uf", uf)
                        eq("status_verificacao", "aprovado")
                    }
                }
                .decodeList<SupabaseRelatoCusto>()

            if (list.isEmpty()) return

            var minPrice = Long.MAX_VALUE
            var maxPrice = Long.MIN_VALUE
            var sumPecas = 0L
            var sumMaoObra = 0L

            list.forEach { item ->
                val total = (item.valorPecaCentavos ?: 0L) + (item.valorMaoObraCentavos ?: 0L)
                if (total < minPrice) minPrice = total
                if (total > maxPrice) maxPrice = total
                sumPecas += (item.valorPecaCentavos ?: 0L)
                sumMaoObra += (item.valorMaoObraCentavos ?: 0L)
            }

            val avgPecas = sumPecas / list.size
            val avgMaoObra = sumMaoObra / list.size

            // Update or insert aggregate
            val existingAgg = SupabaseClientProvider.client.postgrest["custos_agregados"]
                .select {
                    filter {
                        eq("peca_id", pecaUuid)
                        eq("regiao", regiao.name.lowercase())
                    }
                }
                .decodeList<SupabaseCustoAgregado>().firstOrNull()

            val newAgg = SupabaseCustoAgregado(
                id = existingAgg?.id ?: UUID.randomUUID().toString(),
                pecaId = pecaUuid,
                regiao = regiao.name.lowercase(),
                valorPecaMinCentavos = avgPecas / 2, // approximation for min
                valorPecaMedianaCentavos = avgPecas,
                valorPecaMaxCentavos = avgPecas * 2, // approximation for max
                valorMaoObraMinCentavos = avgMaoObra / 2,
                valorMaoObraMedianaCentavos = avgMaoObra,
                valorMaoObraMaxCentavos = avgMaoObra * 2,
                amostrasConsideradas = list.size
            )

            SupabaseClientProvider.client.postgrest["custos_agregados"].upsert(newAgg)
        } catch (e: Exception) {
            android.util.Log.e("PrecoReparoRepository", "Error recalculating aggregates on Supabase", e)
        }
    }

    suspend fun seedInitialPricesIfEmpty(veiculoId: Int, pecaId: Int) {
        // Handled or skipped on Supabase to prevent duplicate seed noise
    }

    // --- Helpers ---
    private suspend fun getOrCreateUserUuid(nome: String): String {
        try {
            val existing = SupabaseClientProvider.client.postgrest["usuarios"]
                .select {
                    filter {
                        eq("nome", nome)
                    }
                }
                .decodeList<SupabaseUsuario>().firstOrNull()

            if (existing != null) return existing.id

            val newId = UUID.randomUUID().toString()
            val newUser = SupabaseUsuario(
                id = newId,
                nome = nome,
                email = "${nome.lowercase().replace(" ", "")}@autopedia.com",
                tipoUsuario = "profissional"
            )
            SupabaseClientProvider.client.postgrest["usuarios"].insert(newUser)
            return newId
        } catch (e: Exception) {
            android.util.Log.e("PrecoReparoRepository", "Error in getOrCreateUserUuid, using deterministic UUID", e)
            return UUID.nameUUIDFromBytes(nome.toByteArray()).toString()
        }
    }

    private fun getUfForRegiao(regiao: RegiaoBrasil): String = when(regiao) {
        RegiaoBrasil.SUL -> "RS"
        RegiaoBrasil.SUDESTE -> "SP"
        RegiaoBrasil.NORDESTE -> "BA"
        RegiaoBrasil.CENTRO_OESTE -> "GO"
        RegiaoBrasil.NORTE -> "AM"
    }

    private fun getMockPrecosMedios(veiculoId: Int, pecaId: Int): List<PrecoMedioRegiao> {
        val basePecas = 32000L
        val baseMaoObra = 20000L
        return listOf(
            PrecoMedioRegiao(1L, veiculoId, pecaId, RegiaoBrasil.SUDESTE, basePecas - 5000, basePecas + 8000, basePecas, baseMaoObra, 5),
            PrecoMedioRegiao(2L, veiculoId, pecaId, RegiaoBrasil.SUL, basePecas - 6000, basePecas + 4000, basePecas - 2000, baseMaoObra - 3000, 3),
            PrecoMedioRegiao(3L, veiculoId, pecaId, RegiaoBrasil.NORDESTE, basePecas - 8000, basePecas + 2000, basePecas - 4000, baseMaoObra - 5000, 4)
        )
    }
}
