package it.trentabitplus.digitaltextsuite.analyzer

import android.annotation.SuppressLint
import android.content.Context
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

class TextAnalyzer(private val context: Context,
                   private val lifecycle: Lifecycle,
                   private val result: MutableLiveData<String>,
                   private val cropPercentage : Float)
    : ImageAnalysis.Analyzer{

    companion object{
        private const val TAG = "Text Analyzer"
        private const val DIFFERENCE_PERCENTAGE = 0.35f
    }

    // instance of TextRecognition detector
    private val textDetector = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    init {
        lifecycle.addObserver(textDetector)
    }

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

    private fun recognizeText(inputImage : InputImage) : Task<Text> {
        return textDetector.process(inputImage).addOnSuccessListener { text ->
            //update mutableLiveData result
            val words = text.text.split(" ")

            if (compareResult(words)){
                Log.d("TEXT1234", "\n\nPrev value: ${result.value}")
                Log.d("TEXT1234", text.text)
                Log.d("TEXT1234", "\n New: ${words.size} Old: ${result.value?.split(" ")?.size}\n")
                result.value = text.text
            }
        }.addOnFailureListener { exception ->
            Log.d(TAG, "Text Recognition Error")
            Log.d(TAG, exception.stackTraceToString())
        }
    }

    private fun compareResult(words: List<String>) : Boolean{
        if (result.value == null){
            return true
        }
        val actualWords = result.value!!.split(" ")
        val size = Math.min(words.size, actualWords.size)
        val maxSize = Math.max(words.size, actualWords.size)

        val ref = (size * DIFFERENCE_PERCENTAGE)
        val refInt : Int
        if(ref - ref.toInt() >= 0.5f)
            refInt = ref.toInt() + 1
        else
            refInt = ref.toInt()
        var count = 0
        for (i in 0 until size){
            if (!words[i].equals(actualWords[i]))
                count++
            if (count >= refInt)
                return true
        }
        if (maxSize - size + count >= refInt)
            return true
        return false
    }
}