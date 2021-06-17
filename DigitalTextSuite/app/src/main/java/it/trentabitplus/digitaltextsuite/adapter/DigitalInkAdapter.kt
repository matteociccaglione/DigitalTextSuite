package it.trentabitplus.digitaltextsuite.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.signature.ObjectKey
import it.trentabitplus.digitaltextsuite.activities.DigitalInkActivity
import it.trentabitplus.digitaltextsuite.activities.TextResultActivity
import it.trentabitplus.digitaltextsuite.database.DbDigitalPhotoEditor
import it.trentabitplus.digitaltextsuite.database.DigitalizedWhiteboards
import it.trentabitplus.digitaltextsuite.databinding.ItemWhiteboardCvBinding
import it.trentabitplus.digitaltextsuite.enumeration.TextResultType
import it.trentabitplus.digitaltextsuite.interfaces.SelectedHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class DigitalInkAdapter(val context: Context, val whiteboards: List<DigitalizedWhiteboards>,val defaultSize: Int,val handler: SelectedHandler): RecyclerView.Adapter<DigitalInkViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DigitalInkViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemWhiteboardCvBinding.inflate(inflater)
        return DigitalInkViewHolder(binding,defaultSize,context)
    }

    override fun onBindViewHolder(holder: DigitalInkViewHolder, position: Int) {
        holder.whiteboard = whiteboards[position]
        holder.binding.cardViewWhiteboard.setOnClickListener{
            if(handler.isAnItemSelected() && !holder.whiteboard.isEmpty()){
                holder.binding.cardViewWhiteboard.isSelected = handler.selectedHandler(holder.whiteboard)
            }
            else {
                val intent = Intent(context, DigitalInkActivity::class.java)
                intent.putExtra("whiteboard", whiteboards[position])
                context.startActivity(intent)
            }
        }
        holder.binding.cardViewWhiteboard.setOnLongClickListener{
            val selected = !handler.isAnItemSelected()
            if(!holder.whiteboard.isEmpty()) {
                if (selected) {
                    holder.binding.cardViewWhiteboard.isSelected = true
                    handler.selectedHandler(holder.whiteboard)
                }
            }
            selected && !holder.whiteboard.isEmpty()
        }
        holder.binding.ibDb.setOnClickListener {
            if (holder.whiteboard.idNote != null) {
                val intent = Intent(context, TextResultActivity::class.java)
                CoroutineScope(Dispatchers.IO).launch {
                    val dao = DbDigitalPhotoEditor.getInstance(context).digitalPhotoEditorDAO()
                    val note = dao.loadNoteById(holder.whiteboard.idNote!!)
                    CoroutineScope(Dispatchers.Main).launch{
                        intent.putExtra("result",note)
                        intent.putExtra("type",TextResultType.SAVED.ordinal)
                        intent.putExtra("whiteboard",holder.whiteboard)
                        context.startActivity(intent)
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return whiteboards.size
    }
}

class DigitalInkViewHolder(val binding: ItemWhiteboardCvBinding,val defaultSize: Int,val context: Context) :RecyclerView.ViewHolder(binding.root){
    var whiteboard: DigitalizedWhiteboards = DigitalizedWhiteboards()
    set(value){
        field=value
        if(whiteboard.isEmpty()){
            binding.clNewWB.visibility=View.VISIBLE
            binding.clOtherWB.visibility=View.GONE
        }
        else {
            binding.clNewWB.visibility = View.GONE
            binding.clOtherWB.visibility = View.VISIBLE
            binding.tvTitleWhiteboard.text = whiteboard.title
            binding.imageView4.layoutParams.height=defaultSize
            binding.imageView4.layoutParams.width=defaultSize
            Log.d("CIAO A TUTTI",whiteboard.imgPath.isEmpty().toString())
            if(whiteboard.idNote!=null){
                binding.ibDb.visibility = View.VISIBLE
            }
            else
                binding.ibDb.visibility = View.GONE
            Glide.with(context).load(whiteboard.imgPath).signature(ObjectKey(File(whiteboard.imgPath).lastModified())).into(binding.imageView4)
        }
    }
}