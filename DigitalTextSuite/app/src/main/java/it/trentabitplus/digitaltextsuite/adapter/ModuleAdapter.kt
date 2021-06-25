package it.trentabitplus.digitaltextsuite.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import it.trentabitplus.digitaltextsuite.databinding.ItemModuleBinding
import it.trentabitplus.digitaltextsuite.utils.Language

class ModuleAdapter(private val listModules : MutableList<Language>, val context: Context)
    :RecyclerView.Adapter<ModuleAdapter.ModuleViewHolder>(){

    var selectedLanguages = mutableListOf<Language>()



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModuleViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemModuleBinding.inflate(layoutInflater)
        return ModuleViewHolder(binding, context)
    }

    override fun onBindViewHolder(holder: ModuleViewHolder, position: Int) {
        holder.code = listModules[position].code
        holder.displayName = listModules[position].displayName
        holder.flag = listModules[position].getFlag()

        holder.binding.cvModule.setOnClickListener {
            if(!holder.binding.cvModule.isSelected){
              holder.binding.cvModule.isSelected = true
              selectedLanguages.add(listModules[position])
            } else{
                holder.binding.cvModule.isSelected = false
                selectedLanguages.remove(listModules[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return listModules.size
    }

    fun deleteItems(){
        selectedLanguages.forEach {
            listModules.remove(it)
        }
        selectedLanguages.clear()

        notifyDataSetChanged()
    }

    inner class ModuleViewHolder(val binding: ItemModuleBinding, val context: Context) : RecyclerView.ViewHolder(binding.root){
        var code = "und"

        var displayName = "Undefined"
        set(value) {
            field = value
            binding.tvItemLanguage.text = value
        }

        var flag = "und"
        set(value) {
            field = value
            binding.tvFlag.text = value
        }
    }
}