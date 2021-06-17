package it.trentabitplus.digitaltextsuite.utils.digitalink

import android.view.MotionEvent
import com.google.android.gms.tasks.SuccessContinuation
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.digitalink.*
import com.google.mlkit.vision.digitalink.Ink.Stroke
import it.trentabitplus.digitaltextsuite.enumeration.DigitalInkState
import it.trentabitplus.digitaltextsuite.interfaces.DigitalRecognizerHandler
import it.trentabitplus.digitaltextsuite.interfaces.DownloadedModelsChangedListener
import it.trentabitplus.digitaltextsuite.interfaces.StatusChangedListener

/**
 * This class represents a manager for digital ink recognition operations. It must be assigned to a Whiteboard.
 * @author Matteo Ciccaglione
 */
class DigitalInkManager {

    private var strokeBuilder = Stroke.builder()
    private var listBuilder = ArrayList<Ink.Builder>()
    private var language = ""
    private var recognitionTask: RecognitionTask? = null
    private var textRecognized: MutableList<RecognitionTask.RecognizedInk> = ArrayList()
    private var contentChangedListener: DigitalRecognizerHandler? = null
    private var statusChangedListener: StatusChangedListener? = null
     lateinit var lastStroke: Ink.Stroke
    private var downloadedModelsChangedListener: DownloadedModelsChangedListener? = null
    private var stateChangedSinceLastRequest : Boolean = true
    var writingArea: WritingArea = WritingArea(1000f,1000f)
    set(value){
        field = value
        recognitionContext = RecognitionContext.builder().setPreContext("").setWritingArea(writingArea).build()
    }
    private var recognitionContext: RecognitionContext = RecognitionContext.builder().setPreContext("").setWritingArea(WritingArea(1000f,1000f)).build()
    private var modelManager =
        ModelManager()
    private var status: DigitalInkState = DigitalInkState.NO_CHANGES
    set(value){
        field = value
        statusChangedListener?.onStatusChanged(status)
    }

    /**
     * This method must be called by the Witheboard instance which uses this manager to remove a stroke
     * @param removedStroke the stroke to remove
     * @param page the number of the page which contains the removedStroke
     */
    fun removeStroke(removedStroke: Stroke,page: Int){
        val oldStrokes = listBuilder[page].build().strokes
        resetCurrentInk(page)
        for(stroke in oldStrokes) {
            if(stroke!=removedStroke)
                listBuilder[page].addStroke(stroke)
        }
    }

    /**
     * This method must be called by the Whiteboard instance which uses this manager to properly manage the creation of a new stroke
     */
    fun addTouchEvent(event: MotionEvent,page: Int): Boolean{
        val action = event.actionMasked
        val x = event.x
        val y = event.y
        when(action){
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> strokeBuilder.addPoint(Ink.Point.create(x,y))
            MotionEvent.ACTION_UP -> {
                //Set stroke and clear the builder
                strokeBuilder.addPoint(Ink.Point.create(x,y))
                lastStroke=strokeBuilder.build()
                listBuilder[page].addStroke(lastStroke)
                strokeBuilder = Stroke.builder()
                return true
            }
            else ->
                return false
        }
        return false
    }
    private fun saveResult(page: Int) {
        recognitionTask!!.result()?.let {
            textRecognized.add(it)
            status = DigitalInkState.REC_DONE
            var count = 0
            for(builder in listBuilder){
                if(!builder.isEmpty)
                    count++
            }
            if(textRecognized.size==count) {
                contentChangedListener?.onChangedResult(textRecognized)
                textRecognized = ArrayList()
            }
            else
                recognize(page+1)
        }
    }

    /**
     * This method must be called by the whiteboard which uses this manager to reset a page
     * @param page the number of the page to reset
     */
    fun reset(page: Int) {
        resetCurrentInk(page)
        textRecognized.clear()
        recognitionTask?.cancel()
        status = DigitalInkState.NO_CHANGES
    }

    private fun resetCurrentInk(page: Int) {
        listBuilder[page] = Ink.builder()
        strokeBuilder = Stroke.builder()
       // stateChangedSinceLastRequest = false
    }

    /**
     * This method allows you to add a new StatusChangedListener for this manager
     * @param statusChangedListener the StatusChangedListener that must be used
     */
    fun setStatusChangedListener(statusChangedListener: StatusChangedListener?) {
        this.statusChangedListener = statusChangedListener
    }

    /**
     * This method allows you to add a new DigitalRecognizerHandler for this manager
     * @param handler the DigitalRecognizerHandler that must be used
     */
    fun setDigitalInkHandler(handler: DigitalRecognizerHandler){
        this.contentChangedListener = handler
    }
    fun setDownloadedModelsChangedListener(
        downloadedModelsChangedListener: DownloadedModelsChangedListener?
    ) {
        this.downloadedModelsChangedListener = downloadedModelsChangedListener
    }

    fun getContent(): List<RecognitionTask.RecognizedInk> {
        return textRecognized
    }
    fun deleteActiveModel(): Task<Nothing?> {
        return modelManager
            .deleteActiveModel()
            .addOnSuccessListener { refreshDownloadedModelsStatus() }
            .onSuccessTask(
                SuccessContinuation { status: DigitalInkState ->
                    this.status = status
                    return@SuccessContinuation Tasks.forResult(null)
                }
            )
    }

    /**
     * Set the initial number of page for this manager. Must be called by the Whiteboard which uses this manager.
     * @param number the number of pages that must be created
     */
    fun setStartNumberPage(number: Int){
        listBuilder = ArrayList()
        for(i in 0 until number){
            listBuilder.add(Ink.builder())
        }
    }

    /**
     * This method must be called by the whiteboard which uses this manager to set the strokes for a page.
     *  @param page the number of the page which contains the strokes
     */
    fun setInkStrokes(strokes: List<Ink.Stroke>,page: Int){
        resetCurrentInk(page)
        for(stroke in strokes){
            for(point in stroke.points){
                strokeBuilder.addPoint(Ink.Point.create(point.x,point.y))
            }
            listBuilder[page].addStroke(strokeBuilder.build())
            strokeBuilder = Stroke.builder()
        }
    }

    /**
     * This method allows you to download a new model for digital ink recognition. The model downloaded corresponds to the current language
     *
     */
    fun download(): Task<Nothing?> {
        status = DigitalInkState.DOWNLOAD_START
        return modelManager
            .download()
            .addOnSuccessListener { refreshDownloadedModelsStatus() }
            .onSuccessTask(
                SuccessContinuation { status: DigitalInkState ->
                    this.status = status
                    return@SuccessContinuation Tasks.forResult(null)
                }
            )
    }

    fun newPage(){
        listBuilder.add(Ink.builder())
    }
    fun deletePage(page: Int){
        listBuilder.removeAt(page)
        if(listBuilder.size==0)
            listBuilder.add(Ink.builder())
    }

    /**
     * This method performs digital ink recognition for a given page. The manager status evolves as follows:
     * If nothing has been drawn, it enters the EMPTY_INK status
    *If a language has not been chosen, it enters the NO_RECOGNIZER state
    *If the chosen model is not downloaded, it enters the NO_MODEL_DOWNLOADED state
    *Otherwise it performs the recognition
     */
    private fun recognize(page: Int): Task<String?> {
        if (!stateChangedSinceLastRequest || listBuilder[page].isEmpty) {
            status = DigitalInkState.EMPTY_INK
            return Tasks.forResult(null)
        }

        if (modelManager.recognizer == null) {
            status = DigitalInkState.NO_RECOGNIZER
            return Tasks.forResult(null)
        }
        return modelManager
            .checkIsModelDownloaded()
            .onSuccessTask { result: Boolean? ->
                if (!result!!) {
                    status = DigitalInkState.NO_MODEL_DOWNLOADED
                    return@onSuccessTask Tasks.forResult<String?>(
                        null
                    )
                }
                recognitionTask =
                    RecognitionTask(
                        modelManager.recognizer,
                        listBuilder[page].build(),recognitionContext,page
                    )
                recognitionTask!!.run().addOnSuccessListener {
                    saveResult(page)
                }
            }
    }

    /**
     * This method is the access point for recognition request
     */
    fun recognizeAll(){
            recognize(0)
    }
    private fun refreshDownloadedModelsStatus() {
        modelManager
            .downloadedModelLanguages
            .addOnSuccessListener { downloadedLanguageTags: Set<String> ->
                downloadedModelsChangedListener?.onDownloadedModelsChanged(downloadedLanguageTags)
            }
    }

    /**
     * Set the current active model
     * @param languageTag The language code for the model that must be set. If languageTag == emoji the model will only recognize emojis
     */
    fun setActiveModel(languageTag: String) {
        if(languageTag=="emoji")
            status = modelManager.setModel(DigitalInkRecognitionModelIdentifier.EMOJI)
        else {
            status = modelManager.setModel(languageTag)
        }
        language = languageTag
    }
    init{
        listBuilder.add(Ink.builder())
    }
}