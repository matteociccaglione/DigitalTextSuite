package it.trentabitplus.digitaltextsuite.analyzer

import android.annotation.SuppressLint
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizerOptions
import it.trentabitplus.digitaltextsuite.utils.ImageUtils
import kotlin.math.max
import kotlin.math.min

/**
 * This class is an analyzer used in the translation use case
 * to recognize the text in the real time translation.
 * It is set as the analyzer of the CameraX ImageAnalysis use case.
 *
 * @author Andrea Pepe
 */
class TextAnalyzer(lifecycle: Lifecycle,
                   private val result: MutableLiveData<String>,
                   private val cropPercentage : Float)
    : ImageAnalysis.Analyzer {

    companion object {
        private const val TAG = "Text Analyzer"
        private const val DIFFERENCE_PERCENTAGE = 0.35f
    }

    // instance of TextRecognition detector
    private val textDetector = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    init {
        lifecycle.addObserver(textDetector)
    }

    /**
     * This method analyze only a percentage of the caught image and
     * also rotate it.
     * @param imageProxy : the ImageProxy object captured by the camera
     */
    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: return
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees

        val bitmap = ImageUtils.convertYuv420888ImageToBitmap(mediaImage)
        val rotatedBitmap = ImageUtils.rotateBitmap(bitmap, rotationDegrees)
        val croppedBitmap = ImageUtils.cropOnlyHeight(rotatedBitmap, cropPercentage)

        recognizeText(InputImage.fromBitmap(croppedBitmap, 0))
            .addOnCompleteListener {
                // close the imageProxy to allow following images to be analyzed
                imageProxy.close()
            }
    }

    /*
     * This is the method called from the analyze method to recognize the text
     * in the cropped and rotate bitmap, updating the recognized text if needed
     */
    private fun recognizeText(inputImage: InputImage): Task<Text> {
        return textDetector.process(inputImage).addOnSuccessListener { text ->
            //update mutableLiveData result
            val words = text.text.split(" ")

            if (compareResult(words)) {
                result.value = text.text
            }
        }.addOnFailureListener { exception ->
            Log.d(TAG, "Text Recognition Error")
            Log.d(TAG, exception.stackTraceToString())
        }
    }


    /* This method is used to compare previous recognized data with the new analyzed data.
    Recognized data will be updated only if there is a difference of at least
    DIFFERENCE PERCENTAGE. This operation is used to avoid continuous updates on
    recognized data, so the feature has more stability. */
    private fun compareResult(words: List<String>): Boolean {
        if (result.value == null) {
            return true
        }
        val actualWords = result.value!!.split(" ")
        val size = min(words.size, actualWords.size)
        val maxSize = max(words.size, actualWords.size)

        val ref = (size * DIFFERENCE_PERCENTAGE)
        val refInt: Int = if (ref - ref.toInt() >= 0.5f)
            ref.toInt() + 1
        else
            ref.toInt()
        var count = 0
        for (i in 0 until size) {
            if (words[i] != actualWords[i])
                count++
            if (count >= refInt)
                return true
        }
        if (maxSize - size + count >= refInt)
            return true
        return false
    }
}