package ru.labore.eventeger.data.db.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.labore.eventeger.data.db.entities.RoleEntity

@Dao
interface RoleEntityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(role: RoleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertArray(roles: Array<RoleEntity>)

    @Query("""
        select * from role where id = :roleId
    """)
    suspend fun getRole(roleId: Int): RoleEntity?
}