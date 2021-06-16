package it.trentabitplus.digitaltextsuite.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import it.trentabitplus.digitaltextsuite.databinding.ItemDirectorySmallBinding
import it.trentabitplus.digitaltextsuite.fragment.FragmentAllFiles
import it.trentabitplus.digitaltextsuite.utils.Directory

class DirectorySmallAdapter(val directory: List<Directory>, val context: Context, val fragment: FragmentAllFiles) : RecyclerView.Adapter<DirectorySmallViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DirectorySmallViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemDirectorySmallBinding.inflate(layoutInflater)
        return DirectorySmallViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DirectorySmallViewHolder, position: Int) {
        holder.directory=directory[position]
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
        return directory.size
    }
}


class DirectorySmallViewHolder(val binding: ItemDirectorySmallBinding): RecyclerView.ViewHolder(binding.root){
    var directory: Directory = Directory()
    set(value){
        field = value
        binding.tvCountSmall.text = directory.count.toString()
        binding.tvTitleSmall.text = directory.name
    }
}