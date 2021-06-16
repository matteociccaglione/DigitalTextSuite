package it.trentabitplus.digitaltextsuite.database

import androidx.room.*

@Dao
interface DAODigitalPhotoEditor {

    @Insert
    fun insertAll(notes: List<Note>)

    @Insert
    fun insertNote(note: Note)
    @Insert
    fun insertWhiteBoards(board: DigitalizedWhiteboards)
    @Insert
    fun insertMatchingList(matchingList: List<Matching>)
    @Delete
    fun deleteWhiteBoards(board: DigitalizedWhiteboards)
    @Insert
    fun insertMatching(matching: Matching)
    @Delete
    fun deleteAll(notes: List<Note>)
    @Delete
    fun deleteNote(note: Note)
    @Update
    fun updateWhiteboard(whiteBoard: DigitalizedWhiteboards)
    @Update
    fun updateNote(note: Note)

    @Query("SELECT * FROM Note WHERE directory = :dir ")
    fun loadAllByDirectory(dir: String) : List<Note>

    @Query("SELECT * FROM Note WHERE title = :tit")
    fun loadAllByTitle(tit: String): List<Note>

    @Query("SELECT * FROM Note WHERE language = :lang")
    fun loadAllByLanguage(lang: String): List<Note>

    @Query("SELECT * FROM Matching WHERE `primary` = :id")
    fun loadAllTranslation(id: Int ) : List<Matching>
    @Query("SELECT count(*) from Note WHERE directory = :dir")
    fun loadDirectorySize(dir: String): Int
    @Query("SELECT distinct directory from Note")
    fun loadDirectories(): List<String>
    @Query("SELECT * from Note")
    fun loadAll(): List<Note>
    @Query("SELECT * from Note where id=:id")
    fun loadNoteById(id: Int): Note
    @Query("SELECT * from Note where preferito =:pref")
    fun loadPreferites(pref :Boolean =true): List<Note>
    @Query("SELECT max(lastDateModify) from Note where directory = :dir")
    fun getLastModifyDir(dir: String): Long
    @Query("SELECT * from Note where title like :title and directory =:dir")
    fun filterByTitle(title: String,dir: String): List<Note>
    @Query("SELECT * from Note where language like :lan and directory =:dir")
    fun filterByCountry(lan: String,dir: String): List<Note>
    @Query("SELECT distinct directory from Note where directory like:dir")
    fun filterDirectory(dir: String): List<String>
    @Query("SELECT * from Note where title like :title and preferito =:pref")
    fun filterTitlePref(title: String, pref: Boolean = true): List<Note>
    @Query("SELECT * from Note where language like :lan and preferito =:pref")
    fun filterCountryPref(lan: String, pref: Boolean = true): List<Note>
    @Query("SELECT * from DigitalizedWhiteboards")
    fun loadAllWhiteboards(): List<DigitalizedWhiteboards>
    @Query("SELECT * from DigitalizedWhiteboards where title like :title")
    fun filterWhiteboards(title: String): List<DigitalizedWhiteboards>
    @Query("SELECT max(id) from DigitalizedWhiteboards")
    fun loadLastId(): Int
    @Query("SELECT max(id) from Note")
    fun loadLastIdNote(): Int
    @Query("SELECT * from DigitalizedWhiteboards where idNote =:id")
    fun loadWhiteboard(id: Int): DigitalizedWhiteboards?
    @Query("delete from Note where directory =:dir")
    fun deleteDirectory(dir: String)
}