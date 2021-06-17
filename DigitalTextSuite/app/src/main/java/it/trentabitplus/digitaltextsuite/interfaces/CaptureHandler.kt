package it.trentabitplus.digitaltextsuite.interfaces

import android.net.Uri
import androidx.camera.core.ImageCaptureException

/**
 * This interface must be implemented by a class which want to handle takePhoto result of a FragmentCamera subclass
 */
interface CaptureHandler {
    /**
     * This method is called when a new photo is available
     * @param fileUri is the Uri of the new image
     * @param temp is a boolean flag. temp = true if and only if the image must be removed at the end of the method
     */
    fun saveImage(fileUri: Uri, temp: Boolean)

    /**
     * This method is called when an exception has occurred during ImageCapture operation.
     * @param exception the ImageCaptureException thrown
     */
    fun errorHandler(exception: ImageCaptureException)
}