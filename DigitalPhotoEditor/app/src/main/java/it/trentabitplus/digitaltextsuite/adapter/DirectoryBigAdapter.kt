package it.trentabitplus.digitaltextsuite.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import it.trentabitplus.digitaltextsuite.R
import it.trentabitplus.digitaltextsuite.databinding.ItemDirectoryBigBinding
import it.trentabitplus.digitaltextsuite.fragment.FragmentAllFiles
import it.trentabitplus.digitaltextsuite.utils.DateUtil
import it.trentabitplus.digitaltextsuite.utils.Directory

class DirectoryBigAdapter(val listDirectory: List<Directory>, val context: Context, val fragment: FragmentAllFiles, val spanCount: Int) : RecyclerView.Adapter<DirectoryBigViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DirectoryBigViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemDirectoryBigBinding.inflate(layoutInflater)
        return DirectoryBigViewHolder(binding,context,spanCount)
    }

    override fun onBindViewHolder(holder: DirectoryBigViewHolder, position: Int) {
        holder.directory = listDirectory[position]
        holder.binding.root.setOnClickListener{
            if(fragment.isAnItemSelected()){
                holder.binding.root.isSelected = fragment.selectedHandler(holder.directory)
            }
            else
                fragment.changeToFiles(holder.directory.name)
        }
        holder.binding.root.setOnLongClickListener{
            val selected = !fragment.isAnItemSelected()
            if(selected){
                holder.binding.root.isSelected = true
                fragment.selectedHandler(holder.directory)
            }
            selected
        }
    }

    override fun getItemCount(): Int {
       return listDirectory.size
    }

}
class DirectoryBigViewHolder(val binding: ItemDirectoryBigBinding,val context: Context,val spanCount: Int): RecyclerView.ViewHolder(binding.root){
    var directory: Directory = Directory()
    set(value){
        field = value
        val dataUtil = DateUtil(context)
        binding.tvTitle.text=directory.name
        binding.tvLastModify.text = dataUtil.printDate(directory.lastModify)
        binding.tvCount.text=directory.count.toString()
        val conf = context.resources?.configuration
        val dm = context.resources?.displayMetrics
        val defaultSize = (conf!!.screenWidthDp* dm!!.density / spanCount).toInt()
        val drawable = ContextCompat.getDrawable(context, R.drawable.directory_image) as BitmapDrawable
        binding.ivDirectory.setImageBitmap(Bitmap.createScaledBitmap(drawable.bitmap,defaultSize,defaultSize,false))
    }
}