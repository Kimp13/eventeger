package ru.labore.eventeger.data.db

import android.os.Parcel
import kotlinx.android.parcel.Parceler
import org.threeten.bp.ZonedDateTime

object ZonedDateTimeParceler : Parceler<ZonedDateTime> {
    override fun create(parcel: Parcel): ZonedDateTime {
        return ZonedDateTime.parse(parcel.readString())
    }

    override fun ZonedDateTime.write(parcel: Parcel, flags: Int) {
        parcel.writeString(toString())
    }
}