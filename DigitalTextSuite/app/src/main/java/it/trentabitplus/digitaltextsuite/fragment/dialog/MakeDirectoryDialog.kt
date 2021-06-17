package it.trentabitplus.digitaltextsuite.fragment.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import it.trentabitplus.digitaltextsuite.R
import it.trentabitplus.digitaltextsuite.databinding.MakeDirectoryDialogBinding

/**
 * This class is a DialogFragment subclass and it allows the user to specify a
 * directory from a directory listing or create a new one and assign a title
 * @author Matteo Ciccaglione
 */
class MakeDirectoryDialog: DialogFragment() {
    private lateinit var binding: MakeDirectoryDialogBinding
    private lateinit var directorySelected : String
    private var directoryList : MutableList<String> = mutableListOf()
    private var directoryListener: (directory: String,title: String) -> Unit ={ s: String, s1: String ->

    }
    private var cancelListener: ()-> Boolean ={
       true
    }
    companion object{
        private var instance : MakeDirectoryDialog? = null
        fun getInstance(): MakeDirectoryDialog{
            if(instance==null){
                instance= MakeDirectoryDialog()
            }
            return instance!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = MakeDirectoryDialogBinding.inflate(inflater)
        isCancelable = false
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = ArrayAdapter(requireContext(),R.layout.my_spinner,directoryList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        if(!directoryList.contains(getString(R.string.default_dir)))
            directoryList.add(getString(R.string.default_dir))
        if(!directoryList.contains(getString(R.string.new_dir)))
            directoryList.add(getString(R.string.new_dir))
        directorySelected=requireContext().getString(R.string.default_dir)

        binding.spinner.prompt = directorySelected
        binding.spinner.adapter=adapter
        binding.spinner.onItemSelectedListener=DirectorySelectedSpinner()
        binding.buttonOk.setOnClickListener{
            val text = if(binding.etTitle.text.isEmpty()) "untitled" else binding.etTitle.text.toString()
            directoryListener(directorySelected, text)
            dismiss()
        }
        binding.buttonCanc.setOnClickListener{
            if(cancelListener()){
                dismiss()
            }
        }
    }

    /**
     * Set up a directory listener to handle on OK button pressed
     */
    fun setOnDirectorySelected(listener: (directory: String,title:String) -> Unit){
        directoryListener=listener
    }
    private inner class DirectorySelectedSpinner: AdapterView.OnItemSelectedListener{
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            if(directoryList[position]==requireContext().getString(R.string.new_dir)){
                binding.editTextTextPersonName.visibility=View.VISIBLE
            }else{
                binding.editTextTextPersonName.visibility = View.GONE
                binding.editTextTextPersonName.addTextChangedListener{
                    directorySelected=binding.editTextTextPersonName.text.toString()
                }
            }
            directorySelected = directoryList[position]
            binding.spinner.prompt = directorySelected
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {

        }
    }

    /**
     * Set up a cancel listener to handle on cancel button pressed
     * The listener must return true if the dialog must be closed
     */
    fun setOnCancelListener(listener: ()-> Boolean){
        cancelListener=listener
    }
    fun setDirectoryList(list: List<String>){
       directoryList.clear()
        directoryList.addAll(list)
    }
}