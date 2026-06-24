package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PrecoReparoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrecoReparo(preco: PrecoReparoRegiao): Long

    @Query("SELECT * FROM precos_reparo_regiao WHERE veiculoId = :veiculoId AND pecaId = :pecaId AND regiao = :regiao AND verificado = 1")
    suspend fun getVerificados(veiculoId: Int, pecaId: Int, regiao: RegiaoBrasil): List<PrecoReparoRegiao>

    @Query("SELECT * FROM precos_medio_regiao WHERE veiculoId = :veiculoId AND pecaId = :pecaId AND regiao = :regiao LIMIT 1")
    suspend fun getPrecoMedio(veiculoId: Int, pecaId: Int, regiao: RegiaoBrasil): PrecoMedioRegiao?

    @Query("SELECT * FROM precos_medio_regiao WHERE veiculoId = :veiculoId AND pecaId = :pecaId")
    fun getPrecosMediosForPeca(veiculoId: Int, pecaId: Int): Flow<List<PrecoMedioRegiao>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrecoMedio(medio: PrecoMedioRegiao)

    @Query("SELECT COUNT(*) FROM precos_reparo_regiao WHERE autor = :autor AND verificado = 1")
    fun getVerificadosCountByAutor(autor: String): Flow<Int>
}
