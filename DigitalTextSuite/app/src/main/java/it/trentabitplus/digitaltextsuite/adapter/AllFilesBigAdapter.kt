package it.trentabitplus.digitaltextsuite.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import it.trentabitplus.digitaltextsuite.activities.DigitalInkActivity
import it.trentabitplus.digitaltextsuite.activities.TextResultActivity
import it.trentabitplus.digitaltextsuite.database.DbDigitalPhotoEditor
import it.trentabitplus.digitaltextsuite.database.DigitalizedWhiteboards
import it.trentabitplus.digitaltextsuite.database.Note
import it.trentabitplus.digitaltextsuite.databinding.ItemFileBigBinding
import it.trentabitplus.digitaltextsuite.enumeration.TextResultType
import it.trentabitplus.digitaltextsuite.interfaces.SelectedHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AllFilesBigAdapter(val listNotes: List<Note>,val context: Context,private val handler: SelectedHandler?): RecyclerView.Adapter<BigViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BigViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemFileBigBinding.inflate(layoutInflater)
        return BigViewHolder(binding,context)
    }

    override fun onBindViewHolder(holder: BigViewHolder, position: Int) {
        holder.note = listNotes[position]
        CoroutineScope(Dispatchers.IO).launch{
            val dao = DbDigitalPhotoEditor.getInstance(context).digitalPhotoEditorDAO()
            val wb = dao.loadWhiteboard(holder.note.id)
            if(wb!=null)
            CoroutineScope(Dispatchers.Main).launch {
                holder.whiteboard = wb
            }
        }
        holder.binding.ibWb.setOnClickListener{
            if(!holder.whiteboard.isEmpty()){
                val intent = Intent(context,DigitalInkActivity::class.java)
                intent.putExtra("whiteboard",holder.whiteboard)
                context.startActivity(intent)
            }
        }
        holder.binding.cv.setOnClickListener{
            if(handler!= null && handler.isAnItemSelected()){
                holder.binding.cv.isSelected = handler.selectedHandler(holder.note)
            }
            else {
                val intent = Intent(context, TextResultActivity::class.java)
                intent.putExtra("result", holder.note)
                intent.putExtra("type", TextResultType.SAVED.ordinal)
                context.startActivity(intent)
            }
        }
        holder.binding.cv.setOnLongClickListener{
            if(handler == null)
                false
            else {
                val selected = !handler.isAnItemSelected()
                if (selected) {
                    holder.binding.cv.isSelected = true
                    handler.selectedHandler(holder.note)
                }
                selected
            }
        }
    }

    override fun getItemCount(): Int {
        return listNotes.size
    }
}

class BigViewHolder(var binding: ItemFileBigBinding,val context: Context): RecyclerView.ViewHolder(binding.root){
    var note: Note = Note()
    set(value){
        field=value
        binding.tvTitle.text = note.title
        binding.tvPreview.text = note.text
        binding.tvFlag.text = note.getFlag()
        val conf = context.resources?.configuration
        val dm = context.resources?.displayMetrics
        val defaultSize = (conf!!.screenHeightDp * dm!!.density / 3).toInt()
        var nLines = 0
        for(char in note.text){
            if(char=='\n'){
                nLines++
            }
        }
        val maxCharacter = 200*defaultSize/700
        val limit = if(field.text.length<maxCharacter)
            field.text.length
        else
            maxCharacter
        val maxLines = 10*defaultSize/700
        if(note.text.length > maxCharacter || nLines > maxLines){
            if(nLines > maxLines){
                var count = 0
                var index = 0
                for(ch in field.text){
                    if(ch=='\n')
                        count++
                    if(count == maxLines){
                        binding.tvPreview.text = String.format(field.text.substring(0,limit)+"[...]")
                    }
                    index++
                }
            }
            else{
                binding.tvPreview.text = String.format(field.text.substring(0,limit)+"[...]")
            }
        }
    }
    var whiteboard: DigitalizedWhiteboards = DigitalizedWhiteboards()
    set(value){
        field = value
        binding.ibWb.visibility = View.VISIBLE
    }
}