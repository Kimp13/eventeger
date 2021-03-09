package ru.labore.eventeger.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.threeten.bp.ZonedDateTime

@Entity(tableName = "role")
class RoleEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Int,

    val type: String,
    val name: String,
    var updatedAt: ZonedDateTime? = null
)// : Parcelable {
//    constructor(parcel: Parcel) : this(
//        parcel.readInt(),
//        parcel.readString() ?: "No type",
//        parcel.readString() ?: "Безымянная роль",
//        ZonedDateTime.parse(parcel.readString())
//    )
//
//    override fun describeContents(): Int {
//        return 0
//    }
//
//    override fun writeToParcel(parcel: Parcel, flags: Int) {
//        parcel.writeInt(id)
//        parcel.writeString(type)
//        parcel.writeString(name)
//        parcel.writeString(updatedAt.toString())
//    }
//
//    companion object CREATOR : Parcelable.Creator<RoleEntity> {
//        override fun createFromParcel(parcel: Parcel): RoleEntity {
//            return RoleEntity(parcel)
//        }
//
//        override fun newArray(size: Int): Array<RoleEntity?> {
//            return arrayOfNulls(size)
//        }
//    }
//}