package it.trentabitplus.digitaltextsuite.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.*
import android.widget.Toast
import androidx.camera.core.ImageAnalysis
import androidx.core.view.children
import androidx.fragment.app.viewModels
import it.trentabitplus.digitaltextsuite.activities.DeleteTranslationModulesActivity
import it.trentabitplus.digitaltextsuite.analyzer.TextAnalyzer
import it.trentabitplus.digitaltextsuite.databinding.FragmentCameraTranslateBinding
import it.trentabitplus.digitaltextsuite.fragment.dialog.LanguageDialogFragment
import it.trentabitplus.digitaltextsuite.utils.Language
import it.trentabitplus.digitaltextsuite.viewmodel.TranslationViewModel
import java.util.*
import kotlin.collections.ArrayList

/**
 * This fragment implements the real-time text recognition and
 * translation of the recognized text in a settable target language
 *
 * @author Andrea Pepe
 */
class FragmentCameraTranslate : CameraFragment() {

    private lateinit var binding : FragmentCameraTranslateBinding

    private val viewModel : TranslationViewModel by viewModels()

    companion object {
        const val LANGUAGE_DIALOG= "LANGUAGE_DIALOG"
        private const val CROP_PERCENTAGE = 0.25f
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentCameraTranslateBinding.inflate(inflater)
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        for(it in menu.children){
            it.isVisible = false
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    /**
     * This method is called to link a CameraX ImageAnalyzer
     * to the camera ImageAnalysis use case.
     * The analyzer is a TextAnalyzer.
     */
    override fun setAnalyzer() {
        try{
            val analyzer = TextAnalyzer(requireContext(), lifecycle, viewModel.sourceText, CROP_PERCENTAGE)
            if (imageAnalysis != null) {
                imageAnalysis.also { it!!.setAnalyzer(cameraExecutor, analyzer) }
            }else{
                imageAnalysis = ImageAnalysis.Builder()
                    .setTargetResolution(Size(1280, 720))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { it.setAnalyzer(cameraExecutor, analyzer) }
            }
        }catch(exception: IllegalStateException){

        }
    }

    override fun onResume() {
        super.onResume()
        show(binding.vfCamera)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        show(binding.vfCamera)


        binding.fabChooseLanguage.setOnClickListener{
            val languages = viewModel.availableLanguages
            val stringLanguages = ArrayList<String>()
            languages.forEach {
                stringLanguages.add(it.displayName)
            }
            val picker = LanguageDialogFragment.getInstance(stringLanguages)
            picker.setOnLanguageSelected {  stringLang ->
                val targetLang : Language
                for (lang in languages){
                    if (stringLang == lang.displayName){
                        targetLang = lang
                        // modify the target language in the view model
                        viewModel.targetLang.value = targetLang
                        break
                    }
                }
                Toast.makeText(requireContext(),Language.getFlag(viewModel.targetLang.value!!.code), Toast.LENGTH_LONG).show()
            }
            picker.show(parentFragmentManager, LANGUAGE_DIALOG)
        }

        binding.fabDeleteModules.setOnClickListener {
            val intent = Intent(requireContext(), DeleteTranslationModulesActivity::class.java)
            startActivity(intent)
        }

        viewModel.targetLang.value = Language(Locale.getDefault().language)

        viewModel.translatedText.observe(viewLifecycleOwner, { resultOrError ->
            resultOrError?.let {
                if (it.error != null){
                    Log.d("Translation", resultOrError.error?.localizedMessage.toString())
                }else{
                    binding.textView.text = resultOrError.result.toString()
                }
            }
        })

        viewModel.modelDownloading.observe(viewLifecycleOwner, { downloading ->
            if (downloading){
                binding.grpDownloading.visibility = View.VISIBLE
            }else{
                binding.grpDownloading.visibility = View.GONE
            }
        })


    }

}