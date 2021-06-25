package it.trentabitplus.digitaltextsuite.viewmodel

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.util.LruCache
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.languageid.LanguageIdentificationOptions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import it.trentabitplus.digitaltextsuite.utils.Language
import it.trentabitplus.digitaltextsuite.utils.ResultOrError
import it.trentabitplus.digitaltextsuite.utils.SmoothedMutableLiveData

/**
 * ViewModel class used to perform translation
 */
class TranslationViewModel(application: Application) : AndroidViewModel(application) {

    companion object{
        // Amount of time (in milliseconds) required for detected text to be stable to settle
        private const val SMOOTHING_DURATION = 1000L

        private const val NUM_TRANSLATORS = 1
    }

    // Gets a list of all available translation languages and map them in custom Language class
    val availableLanguages: List<Language> = TranslateLanguage.getAllLanguages()
        .map { Language(it) }

    // instance of language identifier
    // set the threshold of language identifier to 80%
    private val languageIdentifier = LanguageIdentification.getClient(LanguageIdentificationOptions
        .Builder()
        .setConfidenceThreshold(0.80f)
        .build())

    val targetLang = MutableLiveData<Language>()

    // this is the detected text
    val sourceText = SmoothedMutableLiveData<String>(SMOOTHING_DURATION)

    // MediatorLiveData to change value if some other liveData change
    val translatedText = MediatorLiveData<ResultOrError>()
    private val translating = MutableLiveData<Boolean>()

    // the following variables handle the downloading of language models if needed
    val modelDownloading = SmoothedMutableLiveData<Boolean>(SMOOTHING_DURATION)
    private var modelDownloadTask: Task<Void> = Tasks.forCanceled()


    // MLKit object that really performs translation
    private val translators = object : LruCache<TranslatorOptions, Translator>(NUM_TRANSLATORS) {
        /**
         * Create translators when requested
         */
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

    // Identify the source text language
    private val sourceLang = Transformations.switchMap(sourceText){text ->
        val result = MutableLiveData<Language>()
        /**
         * Call the language identification method and assign it
         * if it is not undefined ("und")
         */
        languageIdentifier.identifyLanguage(text).addOnSuccessListener { langCode ->
            if(langCode != "und")
                result.value = Language(langCode)
        }
        result
    }

    init {
        modelDownloading.setValue(false)
        translating.value = false

        // create a translation result or error object
        val translationProcess = OnCompleteListener<String> { task ->
            if(task.isSuccessful){
                translatedText.value = ResultOrError(task.result, null)
            }else{
                if(task.isCanceled){
                    // task canceled for some reason; ignore it
                    return@OnCompleteListener
                }
                translatedText.value = ResultOrError(null, task.exception)
            }
        }

        /**
         * Init MediatorLiveData sources.
         * Start translation if any of the following change:
         * detected text, source language, target language
         */
        translatedText.addSource(sourceText) {translate().addOnCompleteListener(translationProcess)}
        translatedText.addSource(sourceLang) {translate().addOnCompleteListener(translationProcess)}
        translatedText.addSource(targetLang) {translate().addOnCompleteListener(translationProcess)}
    }

    /**
     * Take the source language value, target language value, and the source text and
     * perform the translation.
     * If the chosen target language model has not been downloaded to the device yet,
     * the method downloadModelIfNeeded() downloads it and then proceed with the translation.
     *
     * @return Task<String>
     */
    private fun translate(): Task<String> {

        val text = sourceText.value
        val source = sourceLang.value
        val target = targetLang.value

        if (modelDownloading.value != false || translating.value != false){
            return Tasks.forCanceled()
        }

        if(source == null || target == null || text == null || text.isEmpty()){
            return Tasks.forResult("")
        }

        // get the language identifiers
        val sourceLangCode = TranslateLanguage.fromLanguageTag(source.code)
        val targetLangCode = TranslateLanguage.fromLanguageTag(target.code)
        if (sourceLangCode == null || targetLangCode == null){
            return Tasks.forCanceled()
        }

        // get the right options for translator
        val translatorOptions = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLangCode)
            .setTargetLanguage(targetLangCode)
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

        // signal the translating mode as active
        translating.value = true

        return modelDownloadTask.onSuccessTask {
            // translate the source text
            translator.translate(text)
        }.addOnCompleteListener {
            translating.value = false
        }
    }


    override fun onCleared() {
        // shutdown the ML Kit clients
        languageIdentifier.close()
        translators.evictAll()
    }
}