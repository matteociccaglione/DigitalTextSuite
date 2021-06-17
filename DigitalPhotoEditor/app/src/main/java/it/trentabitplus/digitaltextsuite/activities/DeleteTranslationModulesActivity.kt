package it.trentabitplus.digitaltextsuite.activities

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateRemoteModel
import it.trentabitplus.digitaltextsuite.R
import it.trentabitplus.digitaltextsuite.adapter.ModuleAdapter
import it.trentabitplus.digitaltextsuite.databinding.ActivityDeleteTranslationModulesBinding
import it.trentabitplus.digitaltextsuite.decorator.LinearSpacingDecorator
import it.trentabitplus.digitaltextsuite.utils.Language
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * This activity has been created to allow user to directly manage
 * the downloaded models needed to perform translation.
 * Particularly this activity allow user to delete these models,
 * each of those is about 30 MB.
 *
 * @author Andrea Pepe
 */
class DeleteTranslationModulesActivity : AppCompatActivity() {
    private lateinit var binding : ActivityDeleteTranslationModulesBinding
    private var modules : MutableList<Language> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeleteTranslationModulesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUI()
    }

    private fun setUI(){
        val modelManager = RemoteModelManager.getInstance()

        // get translation models stored on the device
        CoroutineScope(Dispatchers.IO).launch {
            Tasks.await(modelManager.getDownloadedModels(TranslateRemoteModel::class.java)
                .addOnSuccessListener{ models ->
                    models.forEach {
                        modules.add(Language(it.language))
                    }
                }
                .addOnFailureListener{
                    Log.d("Modules", "Unable to detect downloaded modules")
                    finish()
                })
            CoroutineScope(Dispatchers.Main).launch {
                if (modules.isEmpty())
                    binding.tvEmptyList.visibility = View.VISIBLE
                else
                    binding.tvEmptyList.visibility = View.GONE

                val recView = binding.rvModules
                recView.layoutManager = LinearLayoutManager(baseContext)
                recView.addItemDecoration(
                    LinearSpacingDecorator(resources.getDimensionPixelSize(R.dimen.cardView_margin),1)
                )
                val adapter = ModuleAdapter(modules, baseContext)
                recView.adapter = adapter

                binding.btnDeleteModules.setOnClickListener {
                    if (adapter.selectedLanguages.size == 0)
                        Toast.makeText(applicationContext, "No items selected", Toast.LENGTH_SHORT).show()
                    else{
                        adapter.selectedLanguages.forEach {

                            modules.remove(it)
                            CoroutineScope(Dispatchers.Default).launch {
                                deleteModule(modelManager, it.code)
                            }
                        }
                        recView.invalidate()
                        val newAdapter = ModuleAdapter(modules, baseContext)
                        recView.adapter = newAdapter
                    }
                }
            }

        }


    }

    /**
     * Delete translating model
     * @param modelManager an instance of RemoteModelManager to perform deletion
     * @param code the two characters code of the language to be deleted
     *
     * @author Andrea Pepe
     */
    private fun deleteModule(modelManager: RemoteModelManager, code: String) {
        val model = TranslateRemoteModel.Builder(code).build()
        modelManager.deleteDownloadedModel(model).addOnFailureListener {
            Toast.makeText(this, "Deletion of model $code failed", Toast.LENGTH_SHORT).show()
        }
    }
}