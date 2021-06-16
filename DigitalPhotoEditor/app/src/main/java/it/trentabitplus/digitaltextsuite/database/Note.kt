package it.trentabitplus.digitaltextsuite.database

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import it.trentabitplus.digitaltextsuite.utils.Language
import kotlinx.parcelize.Parcelize

//Date must be the milliseconds
@Parcelize
@Entity
data class Note(@PrimaryKey(autoGenerate = true) var id: Int,
                    var text: String,
                    var directory: String,
                    var title: String,
                    var language: String,
                    var lastDateModify: Long,
                    var preferito: Boolean
                    ): Parcelable {
    constructor(text: String,directory: String,title: String,language: String,date: Long, preferito: Boolean): this(0,text,directory,title,language,date,preferito)
    constructor(): this(0,"","","","",0,false)
    fun getFlag(): String{
        return Language.getFlag(language)
    }
}
