package it.trentabitplus.digitaltextsuite.interfaces

import it.trentabitplus.digitaltextsuite.utils.digitalink.RecognitionTask

/**
 * This interface must be implemented by a class that want to handle digital ink recognition result
 */

interface DigitalRecognizerHandler {
    /**
     * This method is called by the DigitalInkManager when a new result is ready
     */
    fun onChangedResult(content: MutableList<RecognitionTask.RecognizedInk>)
}