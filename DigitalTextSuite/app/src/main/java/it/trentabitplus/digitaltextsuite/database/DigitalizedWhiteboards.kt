package it.trentabitplus.digitaltextsuite.database

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity
data class DigitalizedWhiteboards(@PrimaryKey(autoGenerate = true)
                                    val id: Int,
                                  var path: String,
                                  var title: String,
                                  var idNote: Int? = null,
                                  var imgPath: String): Parcelable{
    constructor () : this(0,"","",null,"")
    fun isEmpty(): Boolean{
        return (id==0 && idNote == null && path=="" && title=="" && imgPath == "")
    }
}
