package ru.labore.moderngymnasium.data.db.entities

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import org.threeten.bp.ZonedDateTime

@Entity(tableName = "announcement")
class AnnouncementEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Int,

    @SerializedName("author_id")
    val authorId: Int,

    val text: String,

    @SerializedName("created_at")
    var createdAt: ZonedDateTime? = null,

    var updatedAt: ZonedDateTime? = null
): Parcelable {
    @Ignore
    var author: UserEntity? = null

    @Ignore
    var authorRole: RoleEntity? = null

    @Ignore
    var authorClass: ClassEntity? = null

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString() ?: "Empty text"
    ) {
        try {
            createdAt = ZonedDateTime.parse(parcel.readString())
        } catch(e: Exception) {}

        updatedAt = try {
            ZonedDateTime.parse(parcel.readString())
        } catch(e: Exception) {
            ZonedDateTime.now()
        }

        author = parcel.readParcelable(UserEntity::class.java.classLoader)
        authorRole = parcel.readParcelable(RoleEntity::class.java.classLoader)
        authorClass = parcel.readParcelable(ClassEntity::class.java.classLoader)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeInt(authorId)
        parcel.writeString(text)
        parcel.writeString(createdAt.toString())
        parcel.writeString(updatedAt.toString())
        parcel.writeParcelable(author, 0)
        parcel.writeParcelable(authorRole, 0)
        parcel.writeParcelable(authorClass, 0)

    }

    companion object CREATOR : Parcelable.Creator<AnnouncementEntity> {
        override fun createFromParcel(parcel: Parcel): AnnouncementEntity {
            return AnnouncementEntity(parcel)
        }

        override fun newArray(size: Int): Array<AnnouncementEntity?> {
            return arrayOfNulls(size)
        }
    }
}