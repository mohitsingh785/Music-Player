package com.example.myapplication.Model

import android.os.Parcel
import android.os.Parcelable

data class Track(
    val id: String,
    val status: String,
    val user_created: String,
    val date_created: String,
    val user_updated: String,
    val date_updated: String,
    val name: String,
    val artist: String,
    val accent: String,
    val cover: String,
    val top_track: Boolean,
    val url: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readByte() != 0.toByte(),
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(status)
        parcel.writeString(user_created)
        parcel.writeString(date_created)
        parcel.writeString(user_updated)
        parcel.writeString(date_updated)
        parcel.writeString(name)
        parcel.writeString(artist)
        parcel.writeString(accent)
        parcel.writeString(cover)
        parcel.writeByte(if (top_track) 1 else 0)
        parcel.writeString(url)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Track> {
        override fun createFromParcel(parcel: Parcel): Track {
            return Track(parcel)
        }

        override fun newArray(size: Int): Array<Track?> {
            return arrayOfNulls(size)
        }
    }
}


data class ApiResponse(
    val data: List<Track>
)
