package it.trentabitplus.digitaltextsuite.utils.digitalink

import android.graphics.Paint
import android.graphics.Path
import com.google.mlkit.vision.digitalink.Ink
import it.trentabitplus.digitaltextsuite.view.Whiteboard

/**
 * This class represents the metadata associated with a whiteboard used in IO operations
 */
class WhiteboardMetadata(var id: Int, var listStrokeMetadata: MutableList<StrokeMetadata> = mutableListOf()) {
    class StrokeMetadata(var id: Int,var listPointMetadata: MutableList<PointMetadata> = mutableListOf(),var paint: Paint = Paint()){
        fun encodeMetadata(points: List<Ink.Point>,newPaint: Paint){
            listPointMetadata = mutableListOf()
            var pointMetadata: PointMetadata
            paint=newPaint
            for(i in points.indices){
                pointMetadata = PointMetadata(i,0f,0f)
                pointMetadata.encodeMetadata(points[i])
                listPointMetadata.add(pointMetadata)
            }
        }
    }
    class PointMetadata(var id: Int,var x: Float,var y: Float){
        fun encodeMetadata(point: Ink.Point){
            x = point.x
            y = point.y
        }
    }

    /**
     * Decode the listStrokeMetadata into a list of Whiteboard.PathStroke that can be used by a Whiteboard instance to restore the content
     * @return the decoded Whiteboard.PathStroke
     */
    fun decodeMetadata(): List<Whiteboard.PathStroke>{
        var strokeBuilder: Ink.Stroke.Builder = Ink.Stroke.builder()
        val result: MutableList<Whiteboard.PathStroke> = mutableListOf()
        var stroke: Ink.Stroke
        listStrokeMetadata.sortBy{
            it.id
        }
        for(strokeMetadata in listStrokeMetadata){
            for(pointMetadata in strokeMetadata.listPointMetadata){
                strokeBuilder.addPoint(Ink.Point.create(pointMetadata.x,pointMetadata.y))
            }
            stroke = strokeBuilder.build()
            result.add(Whiteboard.PathStroke(Path(), stroke, strokeMetadata.paint))
            strokeBuilder = Ink.Stroke.builder()
        }
        return result
    }

    /**
     * Convert a list of Ink.Stroke and paints into a list of StrokeMetadata
     * @param strokes a list of Ink.Stroke
     * @param paints the list of paints associated with strokes
     */
    fun encodeMetadata(strokes: List<Ink.Stroke>,paints: List<Paint>){
        listStrokeMetadata = mutableListOf()
        var strokeMetadata: StrokeMetadata
        for(i in strokes.indices){
            strokeMetadata = StrokeMetadata(i)
            strokeMetadata.encodeMetadata(strokes[i].points,paints[i])
            listStrokeMetadata.add(strokeMetadata)
        }
    }
}