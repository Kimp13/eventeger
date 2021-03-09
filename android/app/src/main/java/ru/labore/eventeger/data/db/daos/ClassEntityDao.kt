package ru.labore.eventeger.data.db.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.labore.eventeger.data.db.entities.ClassEntity

@Dao
interface ClassEntityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(classEntity: ClassEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertArray(classes: Array<ClassEntity>)

    @Query("""
        select * from class where id = :classId
    """)
    suspend fun getClass(classId: Int): ClassEntity?
}