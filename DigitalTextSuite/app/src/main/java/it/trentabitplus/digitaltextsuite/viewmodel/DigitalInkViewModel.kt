package it.trentabitplus.digitaltextsuite.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import it.trentabitplus.digitaltextsuite.database.DbDigitalPhotoEditor
import it.trentabitplus.digitaltextsuite.database.DigitalizedWhiteboards
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DigitalInkViewModel(application: Application): AndroidViewModel(application) {
    val listWhiteboards: MutableLiveData<MutableList<DigitalizedWhiteboards>> = MutableLiveData()
    fun filter(title: String?){
        if(title!=null){
            val dao = DbDigitalPhotoEditor.getInstance(getApplication()).digitalPhotoEditorDAO()
            CoroutineScope(Dispatchers.IO).launch{
                val results = dao.filterWhiteboards(String.format("%%%s%%",title))
                CoroutineScope(Dispatchers.Main).launch{
                    val resultsM = results.toMutableList()
                    resultsM.add(0, DigitalizedWhiteboards())
                    listWhiteboards.value = resultsM
                }
            }
        }
    }
    fun loadAll(){
        val dao = DbDigitalPhotoEditor.getInstance(getApplication()).digitalPhotoEditorDAO()
        CoroutineScope(Dispatchers.IO).launch{
            val results = dao.loadAllWhiteboards()
            CoroutineScope(Dispatchers.Main).launch{
                val resultsM = results.toMutableList()
                resultsM.add(0,DigitalizedWhiteboards())
                listWhiteboards.value = resultsM
            }
        }
    }
}