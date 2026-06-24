package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        Vehicle::class,
        PartAndDefect::class,
        UserContribution::class,
        ForumTopic::class,
        ForumReply::class,
        SavedItem::class,
        UserBadge::class,
        InAppNotification::class,
        Oficina::class,
        MembroOficina::class,
        VeiculoAtendido::class,
        HistoricoAtendimento::class,
        HistoricoAtendimentoPeca::class,
        PrecoReparoRegiao::class,
        PrecoMedioRegiao::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vehicleDao(): VehicleDao
    abstract fun partDao(): PartDao
    abstract fun contributionDao(): ContributionDao
    abstract fun forumDao(): ForumDao
    abstract fun savedItemDao(): SavedItemDao
    abstract fun badgeDao(): BadgeDao
    abstract fun notificationDao(): NotificationDao
    abstract fun oficinaDao(): OficinaDao
    abstract fun membroOficinaDao(): MembroOficinaDao
    abstract fun veiculoAtendidoDao(): VeiculoAtendidoDao
    abstract fun historicoAtendimentoDao(): HistoricoAtendimentoDao
    abstract fun precoReparoDao(): PrecoReparoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `oficinas` (
                        `id` TEXT NOT NULL, 
                        `nome` TEXT NOT NULL, 
                        `cnpj` TEXT, 
                        `uf` TEXT NOT NULL, 
                        `cidade` TEXT NOT NULL, 
                        `tipo` TEXT NOT NULL, 
                        `plano` TEXT NOT NULL, 
                        `criadoEm` INTEGER NOT NULL, 
                        PRIMARY KEY(`id`)
                    )
                """)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `membros_oficina` (
                        `id` TEXT NOT NULL, 
                        `oficinaId` TEXT NOT NULL, 
                        `usuarioId` TEXT NOT NULL, 
                        `papel` TEXT NOT NULL, 
                        `criadoEm` INTEGER NOT NULL, 
                        PRIMARY KEY(`id`), 
                        FOREIGN KEY(`oficinaId`) REFERENCES `oficinas`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE 
                    )
                """)
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_membros_oficina_oficinaId_usuarioId` ON `membros_oficina` (`oficinaId`, `usuarioId`)")
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `veiculos_atendidos` (
                        `id` TEXT NOT NULL, 
                        `oficinaId` TEXT NOT NULL, 
                        `versaoId` INTEGER NOT NULL, 
                        `placa` TEXT, 
                        `anoFabricacao` INTEGER, 
                        `nomeCliente` TEXT, 
                        `contatoCliente` TEXT, 
                        `observacoes` TEXT, 
                        `criadoEm` INTEGER NOT NULL, 
                        PRIMARY KEY(`id`), 
                        FOREIGN KEY(`oficinaId`) REFERENCES `oficinas`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, 
                        FOREIGN KEY(`versaoId`) REFERENCES `vehicles`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE 
                    )
                """)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `historico_atendimento` (
                        `id` TEXT NOT NULL, 
                        `veiculoAtendidoId` TEXT NOT NULL, 
                        `realizadoPor` TEXT, 
                        `problemaId` INTEGER, 
                        `descricao` TEXT NOT NULL, 
                        `kmVeiculo` INTEGER, 
                        `valorTotalCentavos` INTEGER, 
                        `dataAtendimento` INTEGER NOT NULL, 
                        `criadoEm` INTEGER NOT NULL, 
                        PRIMARY KEY(`id`), 
                        FOREIGN KEY(`veiculoAtendidoId`) REFERENCES `veiculos_atendidos`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, 
                        FOREIGN KEY(`problemaId`) REFERENCES `parts_and_defects`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL 
                    )
                """)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `historico_atendimento_pecas` (
                        `id` TEXT NOT NULL, 
                        `historicoId` TEXT NOT NULL, 
                        `pecaId` INTEGER NOT NULL, 
                        `quantidade` INTEGER NOT NULL, 
                        `valorUnitarioCentavos` INTEGER, 
                        PRIMARY KEY(`id`), 
                        FOREIGN KEY(`historicoId`) REFERENCES `historico_atendimento`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, 
                        FOREIGN KEY(`pecaId`) REFERENCES `parts_and_defects`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE 
                    )
                """)
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `precos_reparo_regiao` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `veiculoId` INTEGER NOT NULL, 
                        `pecaId` INTEGER NOT NULL, 
                        `valorPecasCentavos` INTEGER NOT NULL, 
                        `valorMaoObraCentavos` INTEGER NOT NULL, 
                        `regiao` TEXT NOT NULL, 
                        `dataRegistro` INTEGER NOT NULL, 
                        `verificado` INTEGER NOT NULL,
                        `autor` TEXT NOT NULL DEFAULT ''
                    )
                """)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `precos_medio_regiao` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `veiculoId` INTEGER NOT NULL, 
                        `pecaId` INTEGER NOT NULL, 
                        `regiao` TEXT NOT NULL, 
                        `precoMinimoCentavos` INTEGER NOT NULL, 
                        `precoMaximoCentavos` INTEGER NOT NULL, 
                        `precoMedioPecasCentavos` INTEGER NOT NULL, 
                        `precoMedioMaoObraCentavos` INTEGER NOT NULL, 
                        `totalRegistros` INTEGER NOT NULL
                    )
                """)
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mecanicopro_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
