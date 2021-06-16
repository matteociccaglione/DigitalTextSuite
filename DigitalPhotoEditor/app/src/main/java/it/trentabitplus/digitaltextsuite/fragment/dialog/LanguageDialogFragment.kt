package it.trentabitplus.digitaltextsuite.fragment.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import it.trentabitplus.digitaltextsuite.databinding.ChooseLanguageDialogBinding

class LanguageDialogFragment(private val languages : List<String>): DialogFragment() {
    /*
    This attribute must be set using setOnLanguageSelected
    If not set the Dialog do nothing with the selected string
     */
    private  var languageSelectedListener: (language: String) -> Unit = {

    }
    /*
    To handle the cancel request in a different way you can set this listener using the set method
    setOnCancelRequest
    For example if you want to customize this dialog by adding a confirmation message you can set it up
     */
    private var cancelListener: () -> Boolean={
        true
    }
    private lateinit var binding : ChooseLanguageDialogBinding
    companion object{
        private var instance : LanguageDialogFragment? = null
        fun getInstance(languages: List<String>): LanguageDialogFragment{
            if(instance == null){
                instance = LanguageDialogFragment(languages)
            }
            return instance as LanguageDialogFragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ChooseLanguageDialogBinding.inflate(inflater)
        isCancelable = false
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.stringPicker.minValue=0
        binding.stringPicker.maxValue=languages.size-1
        binding.stringPicker.displayedValues=languages.toTypedArray()
        binding.btnOk.setOnClickListener{
            languageSelectedListener(languages[binding.stringPicker.value])
            dismiss()
        }
        binding.btnCanc.setOnClickListener{
            if(cancelListener()){
                dismiss()
            }
        }
    }
    fun setOnLanguageSelected(listener: (language: String)-> Unit){
        this.languageSelectedListener=listener
    }
    fun setOnCancelRequest(listener: ()-> Boolean){
        this.cancelListener = listener
    }
}