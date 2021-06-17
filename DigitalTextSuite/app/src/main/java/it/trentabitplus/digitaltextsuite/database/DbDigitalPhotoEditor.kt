package it.trentabitplus.digitaltextsuite.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [Matching::class, Note::class, DigitalizedWhiteboards::class], version = 1)
abstract class DbDigitalPhotoEditor : RoomDatabase() {
    companion object{
        private var db: DbDigitalPhotoEditor? = null //singleton
        fun getInstance(context: Context) : DbDigitalPhotoEditor
        {
            if(db == null)
                db = Room.databaseBuilder(context.applicationContext,
                DbDigitalPhotoEditor::class.java,
                "DigitalPhotoEditor.db").build()

            return db as DbDigitalPhotoEditor
        }
    }

    abstract fun digitalPhotoEditorDAO() : DAODigitalPhotoEditor
}