package ru.labore.eventeger.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.labore.eventeger.data.db.daos.*
import ru.labore.eventeger.data.db.entities.*

@Database(
    entities = [
        CommentEntity::class,
        AnnouncementEntity::class,
        RoleEntity::class,
        UserEntity::class,
        ClassEntity::class
    ],
    version = 1
)
@TypeConverters(DateTimeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun commentEntityDao(): CommentEntityDao
    abstract fun announcementEntityDao(): AnnouncementEntityDao
    abstract fun userEntityDao(): UserEntityDao
    abstract fun roleEntityDao(): RoleEntityDao
    abstract fun classEntityDao(): ClassEntityDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(context).also { instance = it }
        }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "moderngymnasium.db"
            )
                .build()
    }
}