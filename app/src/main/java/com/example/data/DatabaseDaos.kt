package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {
    @Query("SELECT * FROM vehicles ORDER BY brand, model")
    fun getAllVehicles(): Flow<List<Vehicle>>

    @Query("""
        SELECT * FROM vehicles 
        WHERE (:query IS NULL OR :query = '' OR brand LIKE '%' || :query || '%' OR model LIKE '%' || :query || '%')
          AND (:brand IS NULL OR :brand = '' OR brand = :brand)
          AND (:year IS NULL OR :year = 0 OR year = :year)
          AND (:type IS NULL OR :type = '' OR type = :type)
        ORDER BY brand, model
    """)
    fun searchVehicles(
        query: String?,
        brand: String?,
        year: Int?,
        type: String?
    ): Flow<List<Vehicle>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicle(vehicle: Vehicle): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicles(vehicles: List<Vehicle>)
}

@Dao
interface PartDao {
    @Query("SELECT * FROM parts_and_defects WHERE vehicleId = :vehicleId")
    fun getPartsForVehicle(vehicleId: Int): Flow<List<PartAndDefect>>

    @Query("""
        SELECT * FROM parts_and_defects 
        WHERE (:query IS NULL OR :query = '' OR name LIKE '%' || :query || '%' OR code LIKE '%' || :query || '%' OR chronicProblems LIKE '%' || :query || '%')
          AND (:category IS NULL OR :category = '' OR category = :category)
    """)
    fun searchParts(query: String?, category: String?): Flow<List<PartAndDefect>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPart(part: PartAndDefect)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParts(parts: List<PartAndDefect>)
}

@Dao
interface ContributionDao {
    @Query("SELECT * FROM user_contributions ORDER BY timestamp DESC")
    fun getAllContributions(): Flow<List<UserContribution>>

    @Query("SELECT * FROM user_contributions WHERE vehicleId = :vehicleId ORDER BY timestamp DESC")
    fun getContributionsForVehicle(vehicleId: Int): Flow<List<UserContribution>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContribution(contribution: UserContribution): Long
}

@Dao
interface ForumDao {
    @Query("SELECT * FROM forum_topics ORDER BY timestamp DESC")
    fun getAllTopics(): Flow<List<ForumTopic>>

    @Query("SELECT * FROM forum_topics WHERE category = :category ORDER BY timestamp DESC")
    fun getTopicsByCategory(category: String): Flow<List<ForumTopic>>

    @Query("SELECT * FROM forum_topics WHERE id = :id")
    fun getTopicById(id: Int): Flow<ForumTopic?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopic(topic: ForumTopic): Long

    @Update
    suspend fun updateTopic(topic: ForumTopic)

    @Query("SELECT * FROM forum_replies WHERE topicId = :topicId ORDER BY timestamp ASC")
    fun getRepliesForTopic(topicId: Int): Flow<List<ForumReply>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReply(reply: ForumReply): Long

    @Query("SELECT COUNT(*) FROM forum_replies WHERE author = :author")
    fun getRepliesCountByAuthor(author: String): Flow<Int>
}

@Dao
interface SavedItemDao {
    @Query("SELECT * FROM saved_items ORDER BY savedAt DESC")
    fun getAllSavedItems(): Flow<List<SavedItem>>

    @Query("SELECT EXISTS(SELECT 1 FROM saved_items WHERE type = :type AND referenceId = :refId)")
    fun isItemSaved(type: String, refId: Int): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveItem(item: SavedItem): Long

    @Query("DELETE FROM saved_items WHERE type = :type AND referenceId = :refId")
    suspend fun deleteSavedItem(type: String, refId: Int)
}

@Dao
interface BadgeDao {
    @Query("SELECT * FROM user_badges")
    fun getAllBadges(): Flow<List<UserBadge>>

    @Query("UPDATE user_badges SET isUnlocked = 1, unlockedAt = :timestamp WHERE name = :badgeName AND isUnlocked = 0")
    suspend fun unlockBadge(badgeName: String, timestamp: Long = System.currentTimeMillis())

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBadges(badges: List<UserBadge>)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<InAppNotification>>

    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: InAppNotification): Long

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllAsRead()
}
