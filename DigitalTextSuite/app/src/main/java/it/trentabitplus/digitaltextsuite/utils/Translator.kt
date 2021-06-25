package it.trentabitplus.digitaltextsuite.utils

import android.os.Handler
import android.os.Looper
import android.util.LruCache
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions

/**
 * This class uses MLKit libraries to perform static translation
 * on given text
 *
 * @author Andrea Pepe
 */
class Translator {

    companion object{
        private const val SMOOTHING_DURATION = 50L
        private const val NUM_TRANSLATORS = 1
    }


    // the following variables handle the downloading of language models, if needed
    val modelDownloading = SmoothedMutableLiveData<Boolean>(SMOOTHING_DURATION)
    private var modelDownloadTask: Task<Void> = Tasks.forCanceled()

    // translators object of the MLKit libraries that really perform translation
    private val translators = object : LruCache<TranslatorOptions, Translator>(NUM_TRANSLATORS) {
        override fun create(options: TranslatorOptions): Translator {
            return Translation.getClient(options)
        }

        override fun entryRemoved(
            evicted: Boolean,
            key: TranslatorOptions,
            oldValue: Translator,
            newValue: Translator?
        ) {
            oldValue.close()
        }
    }

    /**
     * This method translate the given text in a target language
     * @param text : text to be translated
     * @param sourceCode : code of the actual language of the text
     * @param targetCode : code of the target language to translate into
     *
     * @return Task<String>; wait the end of the Task to get final result
     * @author Andrea Pepe
     */
    fun translate(text: String, sourceCode: String, targetCode: String) : Task<String> {

        // params can't be null; check only if text is empty
        if(text.isEmpty() || sourceCode.length != 2){
            return Tasks.forResult("")
        }

        // get the right options for translator
        val translatorOptions = TranslatorOptions.Builder()
            .setSourceLanguage(sourceCode)
            .setTargetLanguage(targetCode)
            .build()

        // get the translator
        val translator = translators[translatorOptions]

        // signal the downloading mode as active
        modelDownloading.setValue(true)

        // register a watchdog to unblock long running downloads (>15 sec)
        Handler(Looper.getMainLooper())
            .postDelayed({modelDownloading.setValue(false)}, 15000)

        // download language models if needed
        modelDownloadTask = translator.downloadModelIfNeeded().addOnCompleteListener {
            modelDownloading.setValue(false)
        }


        return modelDownloadTask.onSuccessTask {
            // translate the source text
            translator.translate(text)
        }

    }
}