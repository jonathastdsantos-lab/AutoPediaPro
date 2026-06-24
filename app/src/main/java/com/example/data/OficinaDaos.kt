package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface OficinaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOficina(oficina: Oficina)

    @Query("SELECT * FROM oficinas WHERE id = :id")
    fun getOficinaById(id: String): Flow<Oficina?>

    @Query("""
        SELECT o.* FROM oficinas o 
        INNER JOIN membros_oficina m ON o.id = m.oficinaId 
        WHERE m.usuarioId = :usuarioId
    """)
    fun getOficinasForUser(usuarioId: String): Flow<List<Oficina>>

    @Query("SELECT * FROM oficinas")
    fun getAllOficinas(): Flow<List<Oficina>>

    @Delete
    suspend fun deleteOficina(oficina: Oficina)
}

@Dao
interface MembroOficinaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMembro(membro: MembroOficina)

    @Query("SELECT * FROM membros_oficina WHERE oficinaId = :oficinaId")
    fun getMembrosForOficina(oficinaId: String): Flow<List<MembroOficina>>

    @Query("SELECT COUNT(*) FROM membros_oficina WHERE oficinaId = :oficinaId")
    suspend fun countMembrosOficina(oficinaId: String): Int

    @Query("SELECT * FROM membros_oficina WHERE oficinaId = :oficinaId AND usuarioId = :usuarioId LIMIT 1")
    suspend fun getMembroOficina(oficinaId: String, usuarioId: String): MembroOficina?

    @Query("SELECT EXISTS(SELECT 1 FROM membros_oficina WHERE usuarioId = :usuarioId)")
    fun hasAnyOficinaMembership(usuarioId: String): Flow<Boolean>
}

@Dao
interface VeiculoAtendidoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVeiculoAtendido(veiculo: VeiculoAtendido)

    @Query("SELECT * FROM veiculos_atendidos WHERE oficinaId = :oficinaId")
    fun getVeiculosForOficina(oficinaId: String): Flow<List<VeiculoAtendido>>

    @Query("""
        SELECT * FROM veiculos_atendidos 
        WHERE oficinaId = :oficinaId 
          AND (:query = '' OR :query IS NULL OR placa LIKE '%' || :query || '%' OR nomeCliente LIKE '%' || :query || '%')
    """)
    fun searchVeiculosAtendidos(oficinaId: String, query: String?): Flow<List<VeiculoAtendido>>

    @Query("SELECT * FROM veiculos_atendidos WHERE id = :id")
    fun getVeiculoAtendidoById(id: String): Flow<VeiculoAtendido?>
}

@Dao
interface HistoricoAtendimentoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistorico(historico: HistoricoAtendimento)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoricoPeca(peca: HistoricoAtendimentoPeca)

    @Query("SELECT * FROM historico_atendimento WHERE veiculoAtendidoId = :veiculoAtendidoId ORDER BY dataAtendimento DESC")
    fun getHistoricoForVeiculo(veiculoAtendidoId: String): Flow<List<HistoricoAtendimento>>

    @Query("SELECT * FROM historico_atendimento_pecas WHERE historicoId = :historicoId")
    fun getPecasForHistorico(historicoId: String): Flow<List<HistoricoAtendimentoPeca>>
}
