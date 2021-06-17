package it.trentabitplus.digitaltextsuite.utils.digitalink
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.common.MlKitException
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.vision.digitalink.DigitalInkRecognition
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModel
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModelIdentifier
import com.google.mlkit.vision.digitalink.DigitalInkRecognizer
import com.google.mlkit.vision.digitalink.DigitalInkRecognizerOptions
import it.trentabitplus.digitaltextsuite.enumeration.DigitalInkState
import java.util.HashSet

/**
 * This class is responsible for managing models for digital ink recognition.
 * It manages the download and removal of models and creates the recognition task
 * @author Matteo Ciccaglione
 */
class ModelManager {
    private var model: DigitalInkRecognitionModel? = null
    var recognizer: DigitalInkRecognizer? = null
    val remoteModelManager = RemoteModelManager.getInstance()

    /**
     * Set a new model for digital ink recognition
     * @param languageTag the language code
     * @return a DigitalInkState value: UNKOWN_ERROR if there is an exception, NO_MODEL_AVAILABLE if there are no models for the language code and MODEL_SET if the model has been set
     *
     */
    fun setModel(languageTag: String): DigitalInkState {
        // Clear the old model and recognizer.
        model = null
        recognizer?.close()
        recognizer = null

        // Try to parse the languageTag and get a model from it.
        val modelIdentifier: DigitalInkRecognitionModelIdentifier?
        modelIdentifier = try {
            DigitalInkRecognitionModelIdentifier.fromLanguageTag(languageTag)
        } catch (e: MlKitException) {
            return DigitalInkState.UNKOWN_ERROR
        } ?: return DigitalInkState.NO_MODEL_AVAILABLE

        // Initialize the model and recognizer.
        model = DigitalInkRecognitionModel.builder(modelIdentifier).build()

        recognizer = DigitalInkRecognition.getClient(
            DigitalInkRecognizerOptions.builder(model!!).build()
        )
        return DigitalInkState.MODEL_SET
    }

    /**
     * Set the model for a DigitalInkRecognitionModelIdentifier
     * @param identifier The DigitalInkRecognitionModelIdentifier that must be set
     * @return DigitalInkState value: MODEL_SET if the model has been set
     */
    fun setModel(identifier: DigitalInkRecognitionModelIdentifier): DigitalInkState{
        model = DigitalInkRecognitionModel.builder(identifier).build()
        recognizer = DigitalInkRecognition.getClient(DigitalInkRecognizerOptions.builder(model!!).build())
        return DigitalInkState.MODEL_SET
    }

    /**
     * This method check if a model has already been downloaded
     * @return A Task<Boolean?> instance which is true if the model has already been downloaded
     */
    fun checkIsModelDownloaded(): Task<Boolean?> {
        return remoteModelManager.isModelDownloaded(model!!)
    }

    /**
     * This method remove the active model from the device storage
     * @return a Tast<DigitalInkState> instance. The result of the task is NO_MODEL_DOWNLOADED if the model is not in the device storage and MODEL_DELETED if the model has been deleted and UNKOWN_ERROR if there is an exception
     */
    fun deleteActiveModel(): Task<DigitalInkState> {
        if (model == null) {
            return Tasks.forResult(DigitalInkState.NO_MODEL_SET)
        }
        return checkIsModelDownloaded()
            .onSuccessTask { result: Boolean? ->
                if (!result!!) {
                    return@onSuccessTask Tasks.forResult(DigitalInkState.NO_MODEL_DOWNLOADED)
                }
                remoteModelManager
                    .deleteDownloadedModel(model!!)
                    .onSuccessTask { _: Void? ->
                        Tasks.forResult(
                            DigitalInkState.MODEL_DELETED
                        )
                    }
            }
            .addOnFailureListener { e: Exception ->
                Tasks.forResult(DigitalInkState.UNKOWN_ERROR)
            }
    }

    val downloadedModelLanguages: Task<Set<String>>
        get() = remoteModelManager
            .getDownloadedModels(DigitalInkRecognitionModel::class.java)
            .onSuccessTask { remoteModels: Set<DigitalInkRecognitionModel>? ->
                val result: MutableSet<String> = HashSet()
                for (model in remoteModels!!) {
                    result.add(model.modelIdentifier.languageTag)
                }
                Tasks.forResult<Set<String>>(result.toSet())
            }

    /**
     * Download a model
     * @return A Task<DigitalInkState> instance. The result of the task is: NO_MODEL_SET if the model is null, MODEL_DOWNLOADED if the model has been downloaded
     */
    fun download(): Task<DigitalInkState> {
        return if (model == null) {
            Tasks.forResult(DigitalInkState.NO_MODEL_SET)
        } else remoteModelManager
            .download(model!!, DownloadConditions.Builder().build())
            .onSuccessTask { _: Void? ->
                Tasks.forResult(DigitalInkState.MODEL_DOWNLOADED)
            }
            .addOnFailureListener { e: Exception ->
            }
    }


}