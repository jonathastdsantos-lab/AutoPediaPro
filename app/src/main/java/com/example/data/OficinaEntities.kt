package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.TypeConverter

enum class TipoOficina { OFICINA, FROTA_EMPRESA }
enum class PlanoOficina { TRIAL, BASICO, PROFISSIONAL, ENTERPRISE }
enum class PapelMembro { ADMIN, MECANICO, ATENDENTE }

@Entity(tableName = "oficinas")
data class Oficina(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val nome: String,
    val cnpj: String?,
    val uf: String,
    val cidade: String,
    val tipo: TipoOficina,
    val plano: PlanoOficina = PlanoOficina.TRIAL,
    val criadoEm: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "membros_oficina",
    foreignKeys = [
        ForeignKey(
            entity = Oficina::class,
            parentColumns = ["id"],
            childColumns = ["oficinaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["oficinaId", "usuarioId"], unique = true)
    ]
)
data class MembroOficina(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val oficinaId: String,
    val usuarioId: String,
    val papel: PapelMembro,
    val criadoEm: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "veiculos_atendidos",
    foreignKeys = [
        ForeignKey(
            entity = Oficina::class,
            parentColumns = ["id"],
            childColumns = ["oficinaId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Vehicle::class,
            parentColumns = ["id"],
            childColumns = ["versaoId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class VeiculoAtendido(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val oficinaId: String,
    val versaoId: Int, // FK to Vehicle.id
    val placa: String?,
    val anoFabricacao: Int?,
    val nomeCliente: String?,
    val contatoCliente: String?,
    val observacoes: String?,
    val criadoEm: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "historico_atendimento",
    foreignKeys = [
        ForeignKey(
            entity = VeiculoAtendido::class,
            parentColumns = ["id"],
            childColumns = ["veiculoAtendidoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PartAndDefect::class,
            parentColumns = ["id"],
            childColumns = ["problemaId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class HistoricoAtendimento(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val veiculoAtendidoId: String,
    val realizadoPor: String?,
    val problemaId: Int?, // FK to PartAndDefect.id (nullable)
    val descricao: String,
    val kmVeiculo: Int?,
    val valorTotalCentavos: Long?,
    val dataAtendimento: Long = System.currentTimeMillis(),
    val criadoEm: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "historico_atendimento_pecas",
    foreignKeys = [
        ForeignKey(
            entity = HistoricoAtendimento::class,
            parentColumns = ["id"],
            childColumns = ["historicoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = PartAndDefect::class,
            parentColumns = ["id"],
            childColumns = ["pecaId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class HistoricoAtendimentoPeca(
    @PrimaryKey val id: String = java.util.UUID.randomUUID().toString(),
    val historicoId: String,
    val pecaId: Int, // FK to PartAndDefect.id
    val quantidade: Int = 1,
    val valorUnitarioCentavos: Long?
)

class Converters {
    @TypeConverter
    fun fromTipoOficina(value: TipoOficina): String = value.name

    @TypeConverter
    fun toTipoOficina(value: String): TipoOficina = TipoOficina.valueOf(value)

    @TypeConverter
    fun fromPlanoOficina(value: PlanoOficina): String = value.name

    @TypeConverter
    fun toPlanoOficina(value: String): PlanoOficina = PlanoOficina.valueOf(value)

    @TypeConverter
    fun fromPapelMembro(value: PapelMembro): String = value.name

    @TypeConverter
    fun toPapelMembro(value: String): PapelMembro = PapelMembro.valueOf(value)

    @TypeConverter
    fun fromRegiaoBrasil(value: RegiaoBrasil): String = value.name

    @TypeConverter
    fun toRegiaoBrasil(value: String): RegiaoBrasil = RegiaoBrasil.valueOf(value)
}
