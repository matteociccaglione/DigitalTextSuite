package it.trentabitplus.digitaltextsuite.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import it.trentabitplus.digitaltextsuite.database.DbDigitalPhotoEditor
import it.trentabitplus.digitaltextsuite.database.Note
import it.trentabitplus.digitaltextsuite.enumeration.FilterMode
import it.trentabitplus.digitaltextsuite.enumeration.SortingType
import it.trentabitplus.digitaltextsuite.utils.Directory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class AllFilesViewModel(application: Application): AndroidViewModel(application) {

    val listNotes: MutableLiveData<MutableList<Note>> = MutableLiveData()
    val listDirectory: MutableLiveData<MutableList<Directory>> = MutableLiveData()
    val selectedNote : MutableLiveData<Note> = MutableLiveData()
    private var sortType: SortingType = SortingType.ALPHABETIC_ASC

    fun filter(value: String?,mode: FilterMode,directory: String){
        if(value!=null){
            val dao = DbDigitalPhotoEditor.getInstance(getApplication()).digitalPhotoEditorDAO()
            CoroutineScope(Dispatchers.IO).launch{
                val results = when(mode) {
                    FilterMode.BY_TEXT -> dao.filterByTitle(String.format("%%%s%%", value),directory)
                    FilterMode.BY_COUNTRY -> dao.filterByCountry(String.format("%%%s%%", value),directory)
                }
                            CoroutineScope(Dispatchers.Main).launch{
                        listNotes.value = results.toMutableList()
                        sort(sortType)
                    }
                }
            }

        }

    fun sort(sortType: SortingType){
        this.sortType = sortType
        if(listNotes.value!=null){
            listNotes.value!!.sortBy {
                when(sortType){
                    SortingType.ALPHABETIC_ASC -> it.title
                    SortingType.ALPHABETIC_DESC -> it.title
                    SortingType.BYDATE_ASC -> Date(it.lastDateModify).toString()
                    else -> Date(it.lastDateModify).toString()
                }
            }
            if(sortType== SortingType.ALPHABETIC_DESC || sortType == SortingType.BYDATE_DESC)
                listNotes.value!!.reverse()
        }
    }

    fun sortDirectories(sortType: SortingType){
        this.sortType = sortType
        if(listDirectory.value!=null){
            listDirectory.value!!.sortBy {
                when(sortType){
                    SortingType.ALPHABETIC_ASC -> it.name
                    SortingType.ALPHABETIC_DESC -> it.name
                    SortingType.BYDATE_ASC -> it.lastModify.toString()
                    else -> it.lastModify.toString()
                }
            }
            if(sortType== SortingType.ALPHABETIC_DESC || sortType == SortingType.BYDATE_DESC)
                listDirectory.value!!.reverse()
        }
    }

    fun filterDirectory(value: String?){
        if(value!=null){
            val dao = DbDigitalPhotoEditor.getInstance(getApplication()).digitalPhotoEditorDAO()
            CoroutineScope(Dispatchers.IO).launch{
                val results = dao.filterDirectory(String.format("%%%s%%",value))
                val directories = mutableListOf<Directory>()
                var size: Int
                var lastModify: Long
                for(res in results){
                    size = dao.loadDirectorySize(res)
                    lastModify = dao.getLastModifyDir(res)
                    directories.add(Directory(res,size, Date(lastModify)))
                }
                CoroutineScope(Dispatchers.Main).launch{
                    listDirectory.value = directories.toMutableList()
                    sortDirectories(sortType)
                }
            }
        }
    }
}