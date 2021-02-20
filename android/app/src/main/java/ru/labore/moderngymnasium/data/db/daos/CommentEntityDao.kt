package ru.labore.moderngymnasium.data.db.daos

import androidx.room.*
import ru.labore.moderngymnasium.data.db.entities.AnnouncementEntity
import ru.labore.moderngymnasium.data.db.entities.CommentEntity

@Dao
interface CommentEntityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(comment: CommentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertArray(comments: Array<CommentEntity>)

    @Query("""
        select count(id) from comment
    """)
    suspend fun countComments(): Int

    @Query("""
        select * from comment where id = :id
    """)
    suspend fun getComment(id: Int): CommentEntity?

    @Query("""
        select * from comment
        where announcementId = :announcementId
        order by createdAt asc
        limit 1
        offset :offset
    """)
    suspend fun getCommentAtOffset(announcementId: Int, offset: Int): CommentEntity?

    @Transaction
    @Query("""
        select * from comment
        where announcementId = :announcementId
        order by createdAt asc
        limit :limit
    """)
    suspend fun getFirstComments(announcementId: Int, limit: Int): Array<CommentEntity>

    @Transaction
    @Query("""
        select * from comment
        where announcementId = :announcementId
        order by createdAt asc
        limit :limit
        offset :offset
    """)
    suspend fun getComments(
        announcementId: Int,
        offset: Int,
        limit: Int
    ): Array<CommentEntity>
}