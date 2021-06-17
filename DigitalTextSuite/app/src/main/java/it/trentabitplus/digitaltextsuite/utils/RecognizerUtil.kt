package it.trentabitplus.digitaltextsuite.utils

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.net.toFile
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizerOptions
import it.trentabitplus.digitaltextsuite.R
import it.trentabitplus.digitaltextsuite.activities.TextResultActivity
import it.trentabitplus.digitaltextsuite.database.Note
import it.trentabitplus.digitaltextsuite.enumeration.TextResultType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.StringBuilder

/**
 * This class performs recognize operations on an image or a list of images
 */
class RecognizerUtil(val context: Context) {
    private var result : StringBuilder = StringBuilder()

    /**
     * Perform a single image recognition
     * @param fileUri the uri of the image to be recognized
     * @param temp a boolean flag. If temp == true the fileUri will be removed. Default value: false
     */
    fun recognize(fileUri: Uri,temp: Boolean = false){
        val stream = context.contentResolver.openInputStream(fileUri)
        val bitmap = BitmapFactory.decodeStream(stream)
        stream!!.close()
        val image = InputImage.fromBitmap(bitmap,0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        recognizer.process(image).addOnSuccessListener {
                text ->
            if(text.text.isNotEmpty()) {
                val intent = Intent(context, TextResultActivity::class.java)
                Log.d("lingua", text.textBlocks[0].recognizedLanguage)
                val language = text.textBlocks[0].recognizedLanguage
                val note = Note(text.text,"","",language,System.currentTimeMillis(),false)
                intent.putExtra("result",note)
                intent.putExtra("type", TextResultType.NOT_SAVED.ordinal)
//                intent.putExtra("language", Locale(language).displayName)
//                intent.putExtra("result", text.text)
                if(temp)
                    fileUri.toFile().delete()
                    context.startActivity(intent)
            }
            else{
                CoroutineScope(Dispatchers.Main).launch{
                    Toast.makeText(context, context.getString(R.string.empty_text), Toast.LENGTH_LONG).show()
                    if(temp)
                        fileUri.toFile().delete()
                }
            }
        }.addOnFailureListener{
            CoroutineScope(Dispatchers.Main).launch{
                Toast.makeText(context,context.getString(R.string.error_recognition), Toast.LENGTH_LONG).show()
                if(temp)
                    fileUri.toFile().delete()
            }
        }
    }

    /**
     * Perform a multiple image recognition
     * @param listUri the list of uri to be recognized
     *@param temp a boolean flag. If temp == true the uri will be removed. Default value: true
     */
    fun recognizeAll(listUri: List<Uri>, temp: Boolean = false){
        result = StringBuilder()
        recognizeRecursive(listUri,temp,0)
    }
    private fun recognizeRecursive(listUri: List<Uri>, temp: Boolean, count: Int){
        val stream = context.contentResolver.openInputStream(listUri[count])
        val bitmap = BitmapFactory.decodeStream(stream)
        stream!!.close()
        val image = InputImage.fromBitmap(bitmap,0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        recognizer.process(image).addOnSuccessListener {
            text ->
            if(text.text.isNotEmpty()){
                result.append(text.text)
                result.append("\n")
                if(count == listUri.size-1){
                    val note = Note(result.toString(),"","",text.textBlocks[0].recognizedLanguage,System.currentTimeMillis(),false)
                    val intent = Intent(context, TextResultActivity::class.java)
                    intent.putExtra("result",note)
                    intent.putExtra("type", TextResultType.NOT_SAVED.ordinal)
                    if(temp)
                        listUri[count].toFile().delete()
                    context.startActivity(intent)
                }
                else{
                    if(temp)
                        listUri[count].toFile().delete()
                    recognizeRecursive(listUri,temp,count+1)
                }
            }
        }.addOnFailureListener{
            //On error stop recursion and return no output
            CoroutineScope(Dispatchers.Main).launch{
                Toast.makeText(context,context.getString(R.string.error_recognition), Toast.LENGTH_LONG).show()
                if(temp)
                    listUri[count].toFile().delete()
            }
        }
    }
}