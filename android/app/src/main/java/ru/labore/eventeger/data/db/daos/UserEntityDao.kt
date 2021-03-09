package ru.labore.eventeger.data.db.daos

import androidx.room.*
import ru.labore.eventeger.data.db.entities.UserEntity

@Dao
interface UserEntityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertArray(users: Array<UserEntity>)

    @Query("select * from user where id = :userId")
    suspend fun getUserLastUpdatedTime(userId: Int): UserEntity?

    @Query("select * from user where id = :userId")
    suspend fun getUser(userId: Int): UserEntity?
}
