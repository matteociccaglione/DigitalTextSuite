package it.trentabitplus.digitaltextsuite.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import com.google.mlkit.vision.digitalink.Ink
import it.trentabitplus.digitaltextsuite.enumeration.DrawingMode
import it.trentabitplus.digitaltextsuite.utils.digitalink.DigitalInkManager
import it.trentabitplus.digitaltextsuite.utils.digitalink.SaveManager
import it.trentabitplus.digitaltextsuite.utils.digitalink.WhiteboardMetadata
import java.io.File
import java.io.FileOutputStream

/**
 * This class is a view representing a whiteboard where the user can write using their fingers or a suitable pen
 * (depending on the type of device used).
 * This whiteboard is directly linked with a DigitalInkManager for digitizing the text written on the whiteboard.
 * The class is self-sufficient and autonomously manages communication with the manager and the operations of saving and restoring
 * the whiteboards. The manager must be assigned from the outside and it is the responsibility of the activity (or fragment) to
 * instantiate an appropriate manager and pass him to the view, this allows the view to be released from the responsibility of
 * deciding who must respond to the manager's results, thus making the class highly reusable
 */
class Whiteboard(context: Context,attributeSet: AttributeSet? = null): View(context,attributeSet){
    companion object {
        private const val STROKE_WIDTH_DP = 3
    }
    private var lastStroke: Path = Path()
    private var paths: MutableList<MutableList<Path>> = ArrayList()
    var stroke = 3
    var color  = Color.BLACK
    var drawingMode = DrawingMode.DRAW
    private var currentStrokePaint : Paint
    private  var canvasPaint : Paint
    private var pathToStroke: MutableList<MutableList<PathStroke>> = mutableListOf()
    private var currentPage = 0
    private  var currentStroke: Path
    private lateinit var drawCanvas : Canvas
    private  var canvasBitmap : Bitmap? = null
    private lateinit var manager: DigitalInkManager
    fun setDigitalInkManager(digInkManager: DigitalInkManager){
        manager = digInkManager
    }

    /**
     * This class is used by the view to maintain a correspondence between
     * path drawn on the whiteboard, stroke stored by the manager and paint assigned to the path
     */
     class PathStroke(val path: Path, val stroke: Ink.Stroke,val paint: Paint)
    //Create a new canvas from a larger bitmap and redraw content
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        canvasBitmap = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888)
        drawCanvas = Canvas(canvasBitmap!!)
        //manager.writingArea = WritingArea(w.toFloat(),h.toFloat())
        invalidate()
    }

    /**
     * This method is called automatically following a call to the invalidate () method.
     * It redraws the various stored paths and adds the new path
     */
    override fun onDraw(canvas: Canvas){
        canvas.drawBitmap(canvasBitmap!!,0f,0f,canvasPaint)
        for(path in pathToStroke[currentPage]){
            canvas.drawPath(path.path,path.paint)
        }
        canvas.drawPath(currentStroke,currentStrokePaint)
    }

    /**
     * This method is responsible for managing user interaction with the whiteboard.
     * The ACTION_DOWN event corresponds to the action of the user touching the screen with the finger,
     * the ACTION_MOVE event corresponds to the action of the user to move the finger on the screen,
     * the ACTION_UP event corresponds to the action of the user to lift your finger from the screen
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        performClick()
        Log.d("HERE","HERE")
        val action = event.actionMasked
        val x = event.x
        val y = event.y
//        if(canvasBitmap!=null)
//            manager.writingArea = WritingArea(canvasBitmap!!.width.toFloat(), canvasBitmap!!.height.toFloat())
//        else
//            Log.d("NOCANVAS","NOCANVAS")
        if(drawingMode == DrawingMode.DRAW) {
            when (action) {
                MotionEvent.ACTION_DOWN -> currentStroke.moveTo(x, y)
                MotionEvent.ACTION_MOVE -> currentStroke.lineTo(x, y)
                MotionEvent.ACTION_UP -> {
                    currentStroke.lineTo(x, y)
                    drawCanvas.drawPath(currentStroke, currentStrokePaint)
                    paths[currentPage].add(currentStroke)
                    lastStroke = currentStroke
                    currentStroke = Path()
                }
                else -> {

                }
            }
            /**
             * Inform the manager of the event. The method returns true if and only if a new stroke has been added.
             * In this case the stroke is stored with the associated path
             */
            if(manager.addTouchEvent(event,currentPage)){
                pathToStroke[currentPage].add(PathStroke(lastStroke,manager.lastStroke,currentStrokePaint))
            }
            invalidate()
            return true
        }
        else{
            /**
             * Erase mode: Search among all the paths the one that contains the point corresponding to the touch event by
             * building a rectangle around it for each path and verifying that the rectangle contains the point
             */
            currentStroke = Path()
            val rect = RectF()
            for (i in 0 until pathToStroke[currentPage].size){
                pathToStroke[currentPage][i].path.computeBounds(rect,false)
                if(rect.contains(x,y)){
                    /**
                     * Notify the manager of the removal of the detected stroke
                     */
                    manager.removeStroke(pathToStroke[currentPage][i].stroke,currentPage)
                    erase(i)
                    break
                }
            }
            return false
        }
    }
    /**
     * Remove a pathToStroke element. This method must be called by the onTouchEvent in Erase mode
     * @param index the pathToStroke index
     */
    private fun erase(index: Int){
        pathToStroke[currentPage].removeAt(index)
        clear(false)
        invalidate()
    }

    /**
     * This method is used to completely invalidate the whiteboard.
     * If all = true then the manager will also be brought to the initial conditions
     */
    fun clear(all: Boolean){
        currentStroke = Path()
        onSizeChanged(
            canvasBitmap!!.width,
            canvasBitmap!!.height,
            canvasBitmap!!.width,
            canvasBitmap!!.height
        )
        if(all){
            manager.reset(currentPage)
            paths = mutableListOf()
            paths.add(ArrayList())
            pathToStroke = mutableListOf()
            pathToStroke.add(ArrayList())
            currentPage = 0
        }
    }

    /**
     * Set the stroke width in dp. Can be used by the activity (or fragment) to modify stroke width (default is 3dp)
     * @param dpWidth the new width in dp
     */
    fun setDrawWidth(dpWidth: Int){
        stroke = dpWidth
        resetPaint()
    }

    /**
     * Create a new current paint using the color and stroke value.
     * It must be called every time you want to change the paint structure
     */
    private fun resetPaint(){
        currentStrokePaint = Paint()
        currentStrokePaint.color = color
        currentStrokePaint.isAntiAlias = true
        currentStrokePaint.strokeWidth = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            stroke.toFloat(),
            resources.displayMetrics
        )
        currentStrokePaint.style = Paint.Style.STROKE
        currentStrokePaint.strokeJoin = Paint.Join.ROUND
        currentStrokePaint.strokeCap = Paint.Cap.ROUND
    }

    /**
     * Set the draw color of the paint
     * @param rgb The Int representation of the color
     */
    fun setDrawColor(rgb : Int) {
        color = rgb
        resetPaint()
    }

    /**
     * Completely reset the view.
     * It must be called by the activity (or fragment) before adding a new manager to avoid dirty results related to a previous use.
     */
    fun refresh(){
        manager = DigitalInkManager()
        currentPage = 0
        if(canvasBitmap!=null)
            clear(true)
    }

    /**
     * This method allows you to save the current whiteboard (and all its pages)
     * @param path is the file that must contain the metadata used to restore the whiteboard
     * @param imagePath is the file that must contain the whiteboard preview (Usually a front page image)
     */
    fun saveBoard(path: File,imagePath: File){
        val listOfStrokes = mutableListOf<MutableList<Ink.Stroke>>()
        val listPaints = mutableListOf<MutableList<Paint>>()
        for(page in pathToStroke.indices) {
            listOfStrokes.add(mutableListOf())
            listPaints.add(mutableListOf())
            Log.d("PAGESTROKESIZE",pathToStroke[page].size.toString())
            for (i in pathToStroke[page].indices) {
                listOfStrokes[page].add(pathToStroke[page][i].stroke)
                listPaints[page].add(pathToStroke[page][i].paint)
            }
        }
        val saveManager = SaveManager()
        saveManager.path = path.absolutePath
        saveManager.setMetadata(listOfStrokes,listPaints)
        saveManager.fromMetadataToJson()
        val fileOutputStream = FileOutputStream(imagePath)
        val bitmap = Bitmap.createBitmap(canvasBitmap!!.width,canvasBitmap!!.height,Bitmap.Config.ARGB_8888)
        val canvas = Canvas()
        canvas.setBitmap(bitmap)
        for(p in pathToStroke[0]){
            canvas.drawPath(p.path,p.paint)
        }
        bitmap.compress(Bitmap.CompressFormat.PNG,100,fileOutputStream)
    }

    /**
     * This method allows you to go to the next page. If not present, a new one is created
     */
    fun nextPage(){
        if(currentPage+1>=paths.size){
            paths.add(ArrayList())
            pathToStroke.add(ArrayList())
            manager.newPage()
        }
        currentStroke = Path()
        currentPage++
        onSizeChanged(
            canvasBitmap!!.width,
            canvasBitmap!!.height,
            canvasBitmap!!.width,
            canvasBitmap!!.height
        )
    }

    /**
     * This method allows you to go to the previous page. If not present, it remains on the current page.
     * @return true if and only if another page is present on the left
     */
    fun prevPage(): Boolean{
        if(currentPage-1>=0)
            currentPage--
        else
            return false
        currentStroke = Path()
        onSizeChanged(
            canvasBitmap!!.width,
            canvasBitmap!!.height,
            canvasBitmap!!.width,
            canvasBitmap!!.height
        )
        return currentPage>0
    }

    /**
     * This method allows you to remove the current page. If it is the only one present then a new one is created
     */
    fun removePage(){
        paths.removeAt(currentPage)
        pathToStroke.removeAt(currentPage)
        if(currentPage == 0 && paths.isEmpty() && pathToStroke.isEmpty()){
            paths.add(ArrayList())
            pathToStroke.add(ArrayList())
        }
        else{
            if(currentPage>0)
                currentPage--
        }
        manager.deletePage(currentPage)
        currentStroke = Path()
        onSizeChanged(
            canvasBitmap!!.width,
            canvasBitmap!!.height,
            canvasBitmap!!.width,
            canvasBitmap!!.height
        )
        invalidate()
    }

    /**
     * This method allows you to restore a whiteboard instance
     * @param metadata The stored WhiteboardMetadata
     */
    fun setContent(metadata: MutableList<WhiteboardMetadata>){
        metadata.sortBy{
            it.id
        }
        manager.setStartNumberPage(metadata.size)
        currentStroke = Path()
        paths = mutableListOf()
        pathToStroke = mutableListOf()
        currentPage = -1
        for(whiteboard in metadata) {
            currentPage ++
            paths.add(ArrayList())
            pathToStroke.add(ArrayList())
            val decoded = whiteboard.decodeMetadata()
            val listStroke = mutableListOf<Ink.Stroke>()
            for(elem in decoded){
                listStroke.add(elem.stroke)
            }
            manager.setInkStrokes(listStroke,currentPage)
            for (stroke in whiteboard.decodeMetadata()) {
                for (i in stroke.stroke.points.indices) {
                    if (i == 0) {
                        currentStroke.moveTo(stroke.stroke.points[i].x, stroke.stroke.points[i].y)
                    } else {
                        currentStroke.lineTo(stroke.stroke.points[i].x, stroke.stroke.points[i].y)
                    }
                }
                paths[currentPage].add(currentStroke)
                lastStroke = currentStroke
                currentStroke = Path()
                color = stroke.paint.color
                this.stroke = (stroke.paint.strokeWidth/(resources.displayMetrics.densityDpi/ DisplayMetrics.DENSITY_DEFAULT)).toInt()
                resetPaint()
                pathToStroke[currentPage].add(PathStroke(lastStroke, stroke.stroke,currentStrokePaint))
            }
        }
        currentPage = 0
        currentStroke = Path()
    }
    init{
        paths.add(ArrayList())
        pathToStroke.add(ArrayList())
        currentStrokePaint = Paint()
        currentStrokePaint.color = color
        currentStrokePaint.isAntiAlias = true
        currentStrokePaint.strokeWidth = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            STROKE_WIDTH_DP.toFloat(),
            resources.displayMetrics
        )
        currentPage = 0
        currentStrokePaint.style = Paint.Style.STROKE
        currentStrokePaint.strokeJoin = Paint.Join.ROUND
        currentStrokePaint.strokeCap = Paint.Cap.ROUND
        currentStroke = Path()
        canvasPaint = Paint(Paint.DITHER_FLAG)
    }
}