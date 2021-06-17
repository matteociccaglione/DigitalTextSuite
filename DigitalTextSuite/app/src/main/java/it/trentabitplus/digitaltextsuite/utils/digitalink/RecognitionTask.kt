package it.trentabitplus.digitaltextsuite.utils.digitalink

import com.google.android.gms.tasks.SuccessContinuation
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.digitalink.DigitalInkRecognizer
import com.google.mlkit.vision.digitalink.Ink
import com.google.mlkit.vision.digitalink.RecognitionContext
import com.google.mlkit.vision.digitalink.RecognitionResult
import java.util.concurrent.atomic.AtomicBoolean

/** Task to run asynchronously to obtain recognition results.
 * @author Matteo Ciccaglione */
class RecognitionTask(private val recognizer: DigitalInkRecognizer?, private val ink: Ink,private val context: RecognitionContext,private val page: Int) {
    private var currentResult: RecognizedInk? = null
    private val cancelled: AtomicBoolean
    private val done: AtomicBoolean
    fun cancel() {
        cancelled.set(true)
    }

    fun done(): Boolean {
        return done.get()
    }

    fun result(): RecognizedInk? {
        return currentResult
    }

    /** Helper class that stores an ink along with the corresponding recognized text.  */
    class RecognizedInk internal constructor(val ink: Ink, val text: String?,val page: Int)

    /**
     * Perform the digital ink recognition and create a RecognizedInk instance
     * The method return only the first candidate.
     * @return Task<String> instance. The task result is null if there are no results and text if there is a result
     *
     */
    fun run(): Task<String?> {
        return recognizer!!
            .recognize(ink,context)
            .onSuccessTask(
                SuccessContinuation { result: RecognitionResult? ->
                    if (cancelled.get() || result == null || result.candidates.isEmpty()
                    ) {
                        return@SuccessContinuation Tasks.forResult<String?>(null)
                    }
                    currentResult =
                        RecognizedInk(
                            ink,
                            result.candidates[0].text,
                            page
                        )
                    done.set(
                        true
                    )
                    return@SuccessContinuation Tasks.forResult<String?>(currentResult!!.text)
                }
            )
    }

    init {
        cancelled = AtomicBoolean(false)
        done = AtomicBoolean(false)
    }
}