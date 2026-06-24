package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vehicles")
data class Vehicle(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String,          // Carro, Moto, Caminhão
    val brand: String,         // Fiat, Chevrolet, Honda, Mercedes-Benz, etc.
    val model: String,         // Gol, Palio, CG 160, Accelo, etc.
    val year: Int,             // 2018, 2020, etc.
    val manufacturer: String   // VW, GM, Fiat, etc.
)

@Entity(tableName = "parts_and_defects")
data class PartAndDefect(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val vehicleId: Int,
    val name: String,          // Amortecedor Dianteiro, Vela de Ignição, etc.
    val code: String,          // CO-12345
    val serialNumber: String,  // SN-987654321
    val category: String,      // Mecânica, Elétrica, Pintura
    val chronicProblems: String, // "Vazamento prematuro de óleo após 30.000km..."
    val diagramUrl: String = "", // Descrição ou link para diagrama
    val imageUrl: String = ""    // Descrição ou link para foto de defeito
)

@Entity(tableName = "user_contributions")
data class UserContribution(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val vehicleId: Int?,
    val authorName: String,
    val title: String,
    val body: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "forum_topics")
data class ForumTopic(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val author: String,
    val body: String,
    val category: String,      // Mecânica, Elétrica, Funilaria/Pintura
    val timestamp: Long = System.currentTimeMillis(),
    val isFollowed: Boolean = false
)

@Entity(tableName = "forum_replies")
data class ForumReply(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val topicId: Int,
    val author: String,
    val body: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "saved_items")
data class SavedItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String,          // MANUAL, FORUM
    val referenceId: Int,      // ID do veículo ou tópico do fórum
    val title: String,
    val description: String,
    val savedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_badges")
data class UserBadge(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val iconName: String,      // m3 icon string reference
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null
)

@Entity(tableName = "notifications")
data class InAppNotification(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val body: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
