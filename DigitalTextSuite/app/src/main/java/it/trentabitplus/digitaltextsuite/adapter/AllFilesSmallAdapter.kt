package it.trentabitplus.digitaltextsuite.adapter

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import it.trentabitplus.digitaltextsuite.activities.DigitalInkActivity
import it.trentabitplus.digitaltextsuite.activities.RealMainActivity
import it.trentabitplus.digitaltextsuite.activities.TextResultActivity
import it.trentabitplus.digitaltextsuite.database.DbDigitalPhotoEditor
import it.trentabitplus.digitaltextsuite.database.DigitalizedWhiteboards
import it.trentabitplus.digitaltextsuite.database.Note
import it.trentabitplus.digitaltextsuite.databinding.ItemFileSmallBinding
import it.trentabitplus.digitaltextsuite.enumeration.TextResultType
import it.trentabitplus.digitaltextsuite.fragment.FragmentAllFiles
import it.trentabitplus.digitaltextsuite.fragment.FragmentNoteDetails
import it.trentabitplus.digitaltextsuite.interfaces.SelectedHandler
import it.trentabitplus.digitaltextsuite.utils.DateUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AllFilesSmallAdapter(val listNotes: List<Note>, val context: Context,val handler: SelectedHandler,val fragment : FragmentAllFiles): RecyclerView.Adapter<SmallFileViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SmallFileViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemFileSmallBinding.inflate(layoutInflater)
        return SmallFileViewHolder(binding,context,fragment)
    }

    override fun onBindViewHolder(holder: SmallFileViewHolder, position: Int) {
        holder.note = listNotes[position]
        CoroutineScope(Dispatchers.IO).launch{
            val dao = DbDigitalPhotoEditor.getInstance(context).digitalPhotoEditorDAO()
            val wb = dao.loadWhiteboard(holder.note.id)
            if(wb!=null)
            CoroutineScope(Dispatchers.Main).launch{
                holder.whiteboard = wb
            }
        }
        holder.binding.ibWbS.setOnClickListener{
            if(!holder.whiteboard.isEmpty()){
                val intent = Intent(context, DigitalInkActivity::class.java)
                intent.putExtra("whiteboard",holder.whiteboard)
                context.startActivity(intent)
            }
        }
        holder.binding.cv.setOnClickListener{
            if(handler.isAnItemSelected()){
                holder.binding.cv.isSelected = handler.selectedHandler(holder.note)
                if(fragment.viewModel.selectedNote.value!= null && fragment.viewModel.selectedNote.value!!.id == holder.note.id){
                    holder.binding.cv.isSelected = true
                }
            }
            else {
                val orientation = context.resources.configuration.orientation
                if(orientation == Configuration.ORIENTATION_LANDSCAPE){
                        fragment.viewModel.selectedNote.value = holder.note
                    (fragment.childFragmentManager.findFragmentByTag(
                        FragmentAllFiles.DETAILS_FRAGMENT_TAG) as FragmentNoteDetails).setNote(holder.note)
                }
                else {
                    val intent = Intent(context, TextResultActivity::class.java)
                    intent.putExtra("result", holder.note)
                    intent.putExtra("type", TextResultType.SAVED.ordinal)
                    context.startActivity(intent)
                }
            }
        }
        holder.binding.cv.setOnLongClickListener{
            val selected = !handler.isAnItemSelected()
            if(selected){
                holder.binding.cv.isSelected = true
                handler.selectedHandler(holder.note)
            }
            selected
        }
    }

    override fun getItemCount(): Int {
        return listNotes.size
    }
}
class SmallFileViewHolder(val binding: ItemFileSmallBinding,val context: Context,val fragment: FragmentAllFiles): RecyclerView.ViewHolder(binding.root){
    var note: Note = Note()
    set(value){
        field = value
        val observer = Observer<Note>{
            binding.cv.isSelected = it.id == note.id
            if(fragment.isSelected(note))
                binding.cv.isSelected = true
        }
        fragment.viewModel.selectedNote.observe(fragment,observer)
        binding.tvTitleSmall.text=field.title
        binding.tvFlag.text = note.getFlag()
        val dateUtil = DateUtil(context)
        binding.tvLastModify.text = dateUtil.printDate(note.lastDateModify)
    }
    var whiteboard = DigitalizedWhiteboards()
    set(value){
        field = value
        binding.ibWbS.visibility = View.VISIBLE
    }
}