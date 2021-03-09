package ru.labore.eventeger.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.threeten.bp.ZonedDateTime

@Entity(tableName = "class")
class ClassEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Int,

    val grade: Int,
    val letter: String,
    var updatedAt: ZonedDateTime? = null
)// : Parcelable {
//    constructor(parcel: Parcel) : this(
//        parcel.readInt(),
//        parcel.readInt(),
//        parcel.readString() ?: "",
//        ZonedDateTime.parse(parcel.readString())
//    )
//
//    override fun writeToParcel(parcel: Parcel, flags: Int) {
//        parcel.writeInt(id)
//        parcel.writeInt(grade)
//        parcel.writeString(letter)
//        parcel.writeString(updatedAt.toString())
//    }
//
//    override fun describeContents(): Int {
//        return 0
//    }
//
//    companion object CREATOR : Parcelable.Creator<ClassEntity> {
//        override fun createFromParcel(parcel: Parcel): ClassEntity {
//            return ClassEntity(parcel)
//        }
//
//        override fun newArray(size: Int): Array<ClassEntity?> {
//            return arrayOfNulls(size)
//        }
//    }
//}