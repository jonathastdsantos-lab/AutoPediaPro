package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class RegiaoBrasil {
    SUL, SUDESTE, CENTRO_OESTE, NORDESTE, NORTE
}

@Entity(tableName = "precos_reparo_regiao")
data class PrecoReparoRegiao(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val veiculoId: Int,
    val pecaId: Int,
    val valorPecasCentavos: Long,
    val valorMaoObraCentavos: Long,
    val regiao: RegiaoBrasil,
    val dataRegistro: Long,
    val verificado: Boolean,
    val autor: String = ""
)

@Entity(tableName = "precos_medio_regiao")
data class PrecoMedioRegiao(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val veiculoId: Int,
    val pecaId: Int,
    val regiao: RegiaoBrasil,
    val precoMinimoCentavos: Long,
    val precoMaximoCentavos: Long,
    val precoMedioPecasCentavos: Long,
    val precoMedioMaoObraCentavos: Long,
    val totalRegistros: Int
)
