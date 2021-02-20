package ru.labore.moderngymnasium.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.threeten.bp.ZonedDateTime

@Entity(tableName = "user")
class UserEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Int,

    val firstName: String?,
    val lastName: String?,
    var roleId: Int?,
    var classId: Int?,

    var updatedAt: ZonedDateTime? = null,
)
//): Parcelable {
//    constructor(parcel: Parcel) : this(
//        parcel.readInt(),
//        parcel.readString(),
//        parcel.readString(),
//        parcel.readInt(),
//        parcel.readInt(),
//        null
//    ) {
//        updatedAt = try {
//            ZonedDateTime.parse(parcel.readString())
//        } catch(e: Exception) {
//            ZonedDateTime.now()
//        }
//
//        if (classId == -1) {
//            classId = null
//        }
//
//        if (roleId == -1) {
//            roleId = null
//        }
//    }
//
//    override fun writeToParcel(parcel: Parcel, flags: Int) {
//        parcel.writeInt(id)
//        parcel.writeString(firstName)
//        parcel.writeString(lastName)
//        parcel.writeInt(roleId ?: -1)
//        parcel.writeInt(classId ?: -1)
//        parcel.writeString(updatedAt.toString())
//    }
//
//    override fun describeContents(): Int {
//        return 0
//    }
//
//    companion object CREATOR : Parcelable.Creator<UserEntity> {
//        override fun createFromParcel(parcel: Parcel): UserEntity {
//            return UserEntity(parcel)
//        }
//
//        override fun newArray(size: Int): Array<UserEntity?> {
//            return arrayOfNulls(size)
//        }
//    }
//}