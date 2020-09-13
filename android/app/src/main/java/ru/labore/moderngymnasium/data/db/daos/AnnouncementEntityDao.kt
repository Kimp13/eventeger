package ru.labore.moderngymnasium.data.db.daos

import androidx.lifecycle.LiveData
import androidx.room.*
import ru.labore.moderngymnasium.data.db.entities.AnnouncementEntity
import ru.labore.moderngymnasium.data.db.entities.AnnouncementWithAuthor

@Dao
interface AnnouncementEntityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(announcements: Array<AnnouncementEntity>)

    @Transaction
    @Query("""
        select * from announcement 
        join user on user.id = announcement.authorId
        limit :limit offset :offset
    """)
    fun getAnnouncements(offset: Int, limit: Int): LiveData<Array<AnnouncementWithAuthor>>
}
