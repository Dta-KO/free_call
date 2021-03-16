package com.dtako.freecall.model

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.Keep
import java.util.*

/**
 * Created by Nguyen Kim Khanh on 2/20/2021.
 */
@Keep
class User(val id: String, var token: String, var status: Boolean) : Parcelable {
    constructor() : this("", "", true)

    val timeJoin: Date = Date(System.currentTimeMillis())

    constructor(parcel: Parcel) : this(
            parcel.readString()!!,
            parcel.readString()!!,
            parcel.readByte() != 0.toByte()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(token)
        parcel.writeByte(if (status) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }


}