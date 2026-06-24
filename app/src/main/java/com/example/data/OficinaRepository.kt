package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import java.util.UUID

class OficinaRepository(private val db: AppDatabase) {

    private fun toUuidString(input: String): String {
        return try {
            UUID.fromString(input)
            input
        } catch (e: Exception) {
            UUID.nameUUIDFromBytes(input.toByteArray()).toString()
        }
    }

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
            android.util.Log.e("OficinaRepository", "Error getting/creating user UUID", e)
            return UUID.nameUUIDFromBytes(nome.toByteArray()).toString()
        }
    }

    // --- Create Oficina ---
    suspend fun createOficina(
        nome: String,
        cnpj: String?,
        uf: String,
        cidade: String,
        tipo: TipoOficina,
        plano: PlanoOficina = PlanoOficina.TRIAL,
        creatorUsuarioId: String
    ): String {
        val oficinaId = UUID.randomUUID().toString()
        try {
            val userUuid = toUuidString(creatorUsuarioId)
            
            // Check if user exists in database, or create them
            getOrCreateUserUuid(creatorUsuarioId)

            val newOficina = SupabaseOficina(
                id = oficinaId,
                nome = nome,
                cnpj = cnpj,
                uf = uf,
                cidade = cidade,
                tipo = tipo.name.lowercase(),
                plano = plano.name.lowercase()
            )
            SupabaseClientProvider.client.postgrest["oficinas"].insert(newOficina)

            val newMembro = SupabaseMembroOficina(
                id = UUID.randomUUID().toString(),
                oficinaId = oficinaId,
                usuarioId = userUuid,
                papel = PapelMembro.ADMIN.name.lowercase()
            )
            SupabaseClientProvider.client.postgrest["membros_oficina"].insert(newMembro)
        } catch (e: Exception) {
            android.util.Log.e("OficinaRepository", "Error creating oficina on Supabase", e)
        }
        return oficinaId
    }

    // --- Get/List Oficinas ---
    fun getOficinasForUser(usuarioId: String): Flow<List<Oficina>> = flow {
        try {
            val userUuid = toUuidString(usuarioId)
            val memberships = SupabaseClientProvider.client.postgrest["membros_oficina"]
                .select {
                    filter {
                        eq("usuario_id", userUuid)
                    }
                }
                .decodeList<SupabaseMembroOficina>()

            val resultList = mutableListOf<Oficina>()
            for (mem in memberships) {
                val of = SupabaseClientProvider.client.postgrest["oficinas"]
                    .select {
                        filter {
                            eq("id", mem.oficinaId)
                        }
                    }
                    .decodeSingleOrNull<SupabaseOficina>()
                if (of != null) {
                    resultList.add(mapOficina(of))
                }
            }
            emit(resultList)
        } catch (e: Exception) {
            android.util.Log.e("OficinaRepository", "Error getting user oficinas", e)
            emit(emptyList())
        }
    }

    fun getOficinaById(id: String): Flow<Oficina?> = flow {
        try {
            val of = SupabaseClientProvider.client.postgrest["oficinas"]
                .select {
                    filter {
                        eq("id", id)
                    }
                }
                .decodeSingleOrNull<SupabaseOficina>()
            emit(of?.let { mapOficina(it) })
        } catch (e: Exception) {
            android.util.Log.e("OficinaRepository", "Error getting oficina by ID", e)
            emit(null)
        }
    }

    fun getAllOficinas(): Flow<List<Oficina>> = flow {
        try {
            val list = SupabaseClientProvider.client.postgrest["oficinas"]
                .select()
                .decodeList<SupabaseOficina>()
            emit(list.map { mapOficina(it) })
        } catch (e: Exception) {
            android.util.Log.e("OficinaRepository", "Error getting all oficinas", e)
            emit(emptyList())
        }
    }

    suspend fun joinOficina(oficinaId: String, usuarioId: String, papel: PapelMembro) {
        try {
            val userUuid = toUuidString(usuarioId)
            getOrCreateUserUuid(usuarioId)

            val newMembro = SupabaseMembroOficina(
                id = UUID.randomUUID().toString(),
                oficinaId = oficinaId,
                usuarioId = userUuid,
                papel = papel.name.lowercase()
            )
            SupabaseClientProvider.client.postgrest["membros_oficina"].insert(newMembro)
        } catch (e: Exception) {
            android.util.Log.e("OficinaRepository", "Error joining oficina on Supabase", e)
        }
    }

    fun hasAnyOficinaMembership(usuarioId: String): Flow<Boolean> = flow {
        try {
            val userUuid = toUuidString(usuarioId)
            val list = SupabaseClientProvider.client.postgrest["membros_oficina"]
                .select {
                    filter {
                        eq("usuario_id", userUuid)
                    }
                }
                .decodeList<SupabaseMembroOficina>()
            emit(list.isNotEmpty())
        } catch (e: Exception) {
            android.util.Log.e("OficinaRepository", "Error checking membership", e)
            emit(false)
        }
    }

    fun getMembrosForOficina(oficinaId: String): Flow<List<MembroOficina>> = flow {
        try {
            val list = SupabaseClientProvider.client.postgrest["membros_oficina"]
                .select {
                    filter {
                        eq("oficina_id", oficinaId)
                    }
                }
                .decodeList<SupabaseMembroOficina>()
            emit(list.map { mapMembro(it) })
        } catch (e: Exception) {
            android.util.Log.e("OficinaRepository", "Error getting members", e)
            emit(emptyList())
        }
    }

    suspend fun countMembrosOficina(oficinaId: String): Int {
        try {
            val list = SupabaseClientProvider.client.postgrest["membros_oficina"]
                .select {
                    filter {
                        eq("oficina_id", oficinaId)
                    }
                }
                .decodeList<SupabaseMembroOficina>()
            return list.size
        } catch (e: Exception) {
            android.util.Log.e("OficinaRepository", "Error counting members", e)
            return 0
        }
    }

    // --- Serviced Vehicles (Veiculos Atendidos) ---
    suspend fun addVeiculoAtendido(
        oficinaId: String,
        versaoId: Int,
        placa: String?,
        anoFabricacao: Int?,
        nomeCliente: String?,
        contatoCliente: String?,
        observacoes: String?
    ): String {
        val id = UUID.randomUUID().toString()
        try {
            val versaoUuid = SupabaseIdMapper.getUuid(versaoId)

            val veiculo = SupabaseVeiculoAtendido(
                id = id,
                oficinaId = oficinaId,
                versaoId = versaoUuid,
                placa = placa,
                anoFabricacao = anoFabricacao,
                nomeCliente = nomeCliente,
                contatoCliente = contatoCliente,
                observacoes = observacoes
            )
            SupabaseClientProvider.client.postgrest["veiculos_atendidos"].insert(veiculo)
        } catch (e: Exception) {
            android.util.Log.e("OficinaRepository", "Error adding serviced vehicle on Supabase", e)
        }
        return id
    }

    fun getVeiculosForOficina(oficinaId: String): Flow<List<VeiculoAtendido>> = flow {
        try {
            val list = SupabaseClientProvider.client.postgrest["veiculos_atendidos"]
                .select {
                    filter {
                        eq("oficina_id", oficinaId)
                    }
                }
                .decodeList<SupabaseVeiculoAtendido>()
            emit(list.map { mapVeiculoAtendido(it) })
        } catch (e: Exception) {
            android.util.Log.e("OficinaRepository", "Error getting serviced vehicles", e)
            emit(emptyList())
        }
    }

    fun searchVeiculosAtendidos(oficinaId: String, query: String?): Flow<List<VeiculoAtendido>> = flow {
        try {
            val list = SupabaseClientProvider.client.postgrest["veiculos_atendidos"]
                .select {
                    filter {
                        eq("oficina_id", oficinaId)
                    }
                }
                .decodeList<SupabaseVeiculoAtendido>()

            val mapped = list.map { mapVeiculoAtendido(it) }
            if (query.isNullOrBlank()) {
                emit(mapped)
            } else {
                val lowercaseQuery = query.lowercase()
                emit(mapped.filter {
                    it.placa?.lowercase()?.contains(lowercaseQuery) == true ||
                    it.nomeCliente?.lowercase()?.contains(lowercaseQuery) == true
                })
            }
        } catch (e: Exception) {
            android.util.Log.e("OficinaRepository", "Error searching serviced vehicles", e)
            emit(emptyList())
        }
    }

    fun getVeiculoAtendidoById(id: String): Flow<VeiculoAtendido?> = flow {
        try {
            val veiculo = SupabaseClientProvider.client.postgrest["veiculos_atendidos"]
                .select {
                    filter {
                        eq("id", id)
                    }
                }
                .decodeSingleOrNull<SupabaseVeiculoAtendido>()
            emit(veiculo?.let { mapVeiculoAtendido(it) })
        } catch (e: Exception) {
            android.util.Log.e("OficinaRepository", "Error getting serviced vehicle by ID", e)
            emit(null)
        }
    }

    // --- Attendance History (Historico de Atendimento) ---
    suspend fun registrarAtendimento(
        veiculoAtendidoId: String,
        realizadoPor: String?,
        problemaId: Int?,
        descricao: String,
        kmVeiculo: Int?,
        valorTotalCentavos: Long?,
        dataAtendimento: Long = System.currentTimeMillis(),
        pecas: List<Pair<Int, Long?>>
    ): String {
        val historicoId = UUID.randomUUID().toString()
        try {
            val realizadoPorUuid = realizadoPor?.let { toUuidString(it) }
            val problemaUuid = problemaId?.let { SupabaseIdMapper.getUuid(it) }

            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            val dateStr = sdf.format(java.util.Date(dataAtendimento))

            val historico = SupabaseHistoricoAtendimento(
                id = historicoId,
                veiculoAtendidoId = veiculoAtendidoId,
                realizadoPor = realizadoPorUuid,
                problemaId = problemaUuid,
                descricao = descricao,
                kmVeiculo = kmVeiculo,
                valorTotalCentavos = valorTotalCentavos,
                dataAtendimento = dateStr
            )
            SupabaseClientProvider.client.postgrest["historico_atendimento"].insert(historico)

            // Insert parts
            for (pecaInfo in pecas) {
                val (pecaIntId, valorUnitario) = pecaInfo
                val pecaUuid = SupabaseIdMapper.getUuid(pecaIntId) ?: UUID.randomUUID().toString()

                val historicoPeca = SupabaseHistoricoAtendimentoPeca(
                    id = UUID.randomUUID().toString(),
                    historicoId = historicoId,
                    pecaId = pecaUuid,
                    quantidade = 1,
                    valorUnitarioCentavos = valorUnitario
                )
                SupabaseClientProvider.client.postgrest["historico_atendimento_peca"].insert(historicoPeca)
            }
        } catch (e: Exception) {
            android.util.Log.e("OficinaRepository", "Error registering attendance on Supabase", e)
        }
        return historicoId
    }

    fun getHistoricoForVeiculo(veiculoAtendidoId: String): Flow<List<HistoricoAtendimento>> = flow {
        try {
            val list = SupabaseClientProvider.client.postgrest["historico_atendimento"]
                .select {
                    filter {
                        eq("veiculo_atendido_id", veiculoAtendidoId)
                    }
                }
                .decodeList<SupabaseHistoricoAtendimento>()
            emit(list.map { mapHistorico(it) })
        } catch (e: Exception) {
            android.util.Log.e("OficinaRepository", "Error getting history", e)
            emit(emptyList())
        }
    }

    fun getPecasForHistorico(historicoId: String): Flow<List<HistoricoAtendimentoPeca>> = flow {
        try {
            val list = SupabaseClientProvider.client.postgrest["historico_atendimento_peca"]
                .select {
                    filter {
                        eq("historico_id", historicoId)
                    }
                }
                .decodeList<SupabaseHistoricoAtendimentoPeca>()
            emit(list.map { mapHistoricoPeca(it) })
        } catch (e: Exception) {
            android.util.Log.e("OficinaRepository", "Error getting history parts", e)
            emit(emptyList())
        }
    }

    // --- Mapper Helpers ---
    private fun mapOficina(sop: SupabaseOficina): Oficina {
        val tipoEnum = try {
            TipoOficina.valueOf(sop.tipo.uppercase())
        } catch (e: Exception) {
            TipoOficina.OFICINA
        }
        val planoEnum = try {
            PlanoOficina.valueOf(sop.plano.uppercase())
        } catch (e: Exception) {
            PlanoOficina.TRIAL
        }
        return Oficina(
            id = sop.id,
            nome = sop.nome,
            cnpj = sop.cnpj,
            uf = sop.uf,
            cidade = sop.cidade,
            tipo = tipoEnum,
            plano = planoEnum,
            criadoEm = System.currentTimeMillis()
        )
    }

    private fun mapMembro(smp: SupabaseMembroOficina): MembroOficina {
        val papelEnum = try {
            PapelMembro.valueOf(smp.papel.uppercase())
        } catch (e: Exception) {
            PapelMembro.MECANICO
        }
        return MembroOficina(
            id = smp.id,
            oficinaId = smp.oficinaId,
            usuarioId = smp.usuarioId,
            papel = papelEnum,
            criadoEm = System.currentTimeMillis()
        )
    }

    private fun mapVeiculoAtendido(sva: SupabaseVeiculoAtendido): VeiculoAtendido {
        val versaoIntId = sva.versaoId?.let { SupabaseIdMapper.getIntId(it) } ?: 1
        return VeiculoAtendido(
            id = sva.id,
            oficinaId = sva.oficinaId,
            versaoId = versaoIntId,
            placa = sva.placa,
            anoFabricacao = sva.anoFabricacao,
            nomeCliente = sva.nomeCliente,
            contatoCliente = sva.contatoCliente,
            observacoes = sva.observacoes,
            criadoEm = System.currentTimeMillis()
        )
    }

    private fun mapHistorico(sha: SupabaseHistoricoAtendimento): HistoricoAtendimento {
        val problemaIntId = sha.problemaId?.let { SupabaseIdMapper.getIntId(it) }
        return HistoricoAtendimento(
            id = sha.id,
            veiculoAtendidoId = sha.veiculoAtendidoId,
            realizadoPor = sha.realizadoPor,
            problemaId = problemaIntId,
            descricao = sha.descricao,
            kmVeiculo = sha.kmVeiculo,
            valorTotalCentavos = sha.valorTotalCentavos,
            dataAtendimento = System.currentTimeMillis()
        )
    }

    private fun mapHistoricoPeca(shp: SupabaseHistoricoAtendimentoPeca): HistoricoAtendimentoPeca {
        val pecaIntId = SupabaseIdMapper.getIntId(shp.pecaId)
        return HistoricoAtendimentoPeca(
            id = shp.id,
            historicoId = shp.historicoId,
            pecaId = pecaIntId,
            quantidade = shp.quantidade,
            valorUnitarioCentavos = shp.valorUnitarioCentavos
        )
    }
}
