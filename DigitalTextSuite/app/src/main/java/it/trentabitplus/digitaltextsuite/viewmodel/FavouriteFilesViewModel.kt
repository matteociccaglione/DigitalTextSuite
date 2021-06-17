package it.trentabitplus.digitaltextsuite.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import it.trentabitplus.digitaltextsuite.database.DbDigitalPhotoEditor
import it.trentabitplus.digitaltextsuite.database.Note
import it.trentabitplus.digitaltextsuite.enumeration.FilterMode
import it.trentabitplus.digitaltextsuite.enumeration.SortingType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class FavouriteFilesViewModel(application: Application): AndroidViewModel(application) {
    var listFiles : MutableLiveData<MutableList<Note>> = MutableLiveData()
    private var sortType: SortingType = SortingType.ALPHABETIC_ASC
    fun filter(query: String?, mode: FilterMode){
        if(query!=null){
            val dao = DbDigitalPhotoEditor.getInstance(getApplication()).digitalPhotoEditorDAO()
            CoroutineScope(Dispatchers.IO).launch{
                val results = when(mode){
                    FilterMode.BY_TEXT -> dao.filterTitlePref(String.format("%%%s%%",query))
                    FilterMode.BY_COUNTRY -> dao.filterCountryPref(String.format("%%%s%%",query))
                }
                CoroutineScope(Dispatchers.Main).launch{
                    listFiles.value = results.toMutableList()
                    sort(sortType)
                }
            }
        }
    }
    fun sort(sortingType: SortingType){
        this.sortType = sortingType
        if(listFiles.value!=null){
            listFiles.value!!.sortBy {
                when(sortType){
                    SortingType.ALPHABETIC_ASC -> it.title
                    SortingType.ALPHABETIC_DESC -> it.title
                    SortingType.BYDATE_ASC -> Date(it.lastDateModify).toString()
                    else -> Date(it.lastDateModify).toString()
                }
            }
            if(sortType== SortingType.ALPHABETIC_DESC || sortType == SortingType.BYDATE_DESC)
                listFiles.value!!.reverse()
        }
    }
}