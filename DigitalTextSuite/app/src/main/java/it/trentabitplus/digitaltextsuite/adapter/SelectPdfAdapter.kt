package it.trentabitplus.digitaltextsuite.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import it.trentabitplus.digitaltextsuite.activities.RealMainActivity
import it.trentabitplus.digitaltextsuite.databinding.ItemPdfBinding
import java.io.File

class SelectPdfAdapter(private val listPdf : List<File>, val context: Context) : RecyclerView.Adapter<SelectPdfAdapter.SelectPdfViewHolder>(){

    var selectedFile: MutableLiveData<File?> = MutableLiveData(null)



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectPdfViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemPdfBinding.inflate(layoutInflater)
        return SelectPdfViewHolder(binding, context)
    }

    override fun onBindViewHolder(holder: SelectPdfViewHolder, position: Int) {
        holder.file = listPdf[position]

        holder.binding.cvItem.setOnClickListener {
            selectedFile.value = holder.file
        }
    }

    override fun getItemCount(): Int {
        return listPdf.size
    }


    inner class SelectPdfViewHolder(val binding: ItemPdfBinding, val context:Context) : RecyclerView.ViewHolder(binding.root){
        var file : File = RealMainActivity.rootDir
            set(value) {
                field = value
                binding.tvPdfFilename.text = file.name
            }

        init {
            val selectedObserver = Observer<File?>{
                if (it != null){
                    binding.cvItem.isSelected = it.canonicalPath.equals(file.canonicalPath)
                }else{
                    binding.cvItem.isSelected = false
                }
            }
            selectedFile.observe(context as FragmentActivity, selectedObserver)
        }

    }
}

