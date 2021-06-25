package it.trentabitplus.digitaltextsuite.utils.digitalink

import android.graphics.Paint
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.google.mlkit.vision.digitalink.Ink
import java.io.FileReader
import java.io.FileWriter
import java.lang.reflect.Type

/**
 * This class manages the operations of saving and restoring a whiteboard to the device memory
 * @author Matteo Ciccaglione
 */
class SaveManager {
    var path: String = ""
    private val metadataType = object : TypeToken<MutableList<WhiteboardMetadata>>() {}.type
    val strokeMetadataType: Type = object : TypeToken<MutableList<WhiteboardMetadata.StrokeMetadata>>() {}.type
    val pointMetadataType = object : TypeToken<MutableList<WhiteboardMetadata.StrokeMetadata>>() {}.type
    private var metadata: MutableList<WhiteboardMetadata> = mutableListOf()
    fun fromJsonToMetadata(): List<WhiteboardMetadata> {
        val gson = GsonBuilder()
        val adapter = MetadataAdapter()
        gson.registerTypeAdapter(WhiteboardMetadata::class.java,adapter)
        return gson.create().fromJson<MutableList<WhiteboardMetadata>>(FileReader(path), metadataType)
    }

    /**
     * Save a list of WhiteboardMetadata instance on the device storage as a json
     */
    fun fromMetadataToJson(){
        val gson = GsonBuilder()
        val adapter = MetadataAdapter()
        gson.registerTypeAdapter(WhiteboardMetadata::class.java,adapter)
        val writer = FileWriter(path)
        gson.create().toJson(metadata,metadataType,writer )
        writer.flush()
        writer.close()
    }

    /**
     * Set the metadata that must be stored
     * @param listOfStrokes a list of list of Ink.Stroke instance which must be stored. The master list must contain a list of strokes for each whiteboard page.
     * @param listPaints A list of Paint instance lists that correspond to the Paint associated with strokes in the listOfStrokes
     *
     */
    fun setMetadata(listOfStrokes: List<List<Ink.Stroke>>,listPaints: List<List<Paint>>){
        metadata = mutableListOf()
        var whiteboardMetadata: WhiteboardMetadata
        for (i in listOfStrokes.indices){
            whiteboardMetadata = WhiteboardMetadata(i)
            whiteboardMetadata.encodeMetadata(listOfStrokes[i],listPaints[i])
            metadata.add(whiteboardMetadata)
        }
    }

    /**
     * This class is used by Gson to perform the IO operations
     */
    class MetadataAdapter(): TypeAdapter<WhiteboardMetadata>(){
        override fun write(out: JsonWriter?, value: WhiteboardMetadata?) {
            out!!.beginObject()
            out.name("id")
            out.value(value!!.id)
            out.name("strokes")
            out.beginArray()
            for(stroke in value.listStrokeMetadata){
                out.beginObject()
                out.name("id")
                out.value(stroke.id)
                out.name("color")
                out.value(stroke.paint.color)
                out.name("width")
                out.value(stroke.paint.strokeWidth)
                out.name("points")
                out.beginArray()
                for(point in stroke.listPointMetadata){
                    out.beginObject()
                    out.name("id")
                    out.value(point.id)
                    out.name("x")
                    out.value(point.x)
                    out.name("y")
                    out.value(point.y)
                    out.endObject()
                }
                out.endArray()
                out.endObject()
            }
            out.endArray()
            out.endObject()
        }

        override fun read(reader: JsonReader?): WhiteboardMetadata {
            val whiteboardMetadata = WhiteboardMetadata(0)
            var all  = 0
            var allStroke = 0
            var token : com.google.gson.stream.JsonToken
            var stroke : WhiteboardMetadata.StrokeMetadata = WhiteboardMetadata.StrokeMetadata(0)
            var listOfPoints : MutableList<WhiteboardMetadata.PointMetadata> = mutableListOf()
            var point : WhiteboardMetadata.PointMetadata = WhiteboardMetadata.PointMetadata(0,0f,0f)
            val listOfStrokes : MutableList<WhiteboardMetadata.StrokeMetadata> = mutableListOf()
            var fieldName = ""
            if(reader!= null) {
                    reader.beginObject()
                    token = reader.peek()
                    if (token == com.google.gson.stream.JsonToken.NAME) {
                        fieldName = reader.nextName()
                    }
                    if (fieldName == "id") {
                        token = reader.peek()
                        whiteboardMetadata.id = reader.nextInt()
                        fieldName = reader.nextName()
                    }
                    if (fieldName == "strokes") {
                        token = reader.peek()
                        reader.beginArray()
                        if (token == com.google.gson.stream.JsonToken.BEGIN_ARRAY) {
                            while (token != com.google.gson.stream.JsonToken.END_ARRAY) {
                                if(allStroke == 0)
                                    reader.beginObject()
                                token = reader.peek()
                                if (token == com.google.gson.stream.JsonToken.NAME) {
                                    fieldName = reader.nextName()
                                }
                                if (fieldName == "id") {
                                    token = reader.peek()
                                    allStroke++
                                    stroke = WhiteboardMetadata.StrokeMetadata(reader.nextInt())
                                }
                                if(fieldName == "color"){
                                    token = reader.peek()
                                    allStroke++
                                    stroke.paint = Paint()
                                    stroke.paint.color = reader.nextInt()
                                }
                                if(fieldName == "width"){
                                    token = reader.peek()
                                    allStroke++
                                    stroke.paint.strokeWidth = reader.nextDouble().toFloat()
                                }
                                if (fieldName == "points") {
                                    allStroke++
                                    token = reader.peek()
                                reader.beginArray()
                                    if (token == com.google.gson.stream.JsonToken.BEGIN_ARRAY) {
                                        while (token != com.google.gson.stream.JsonToken.END_ARRAY) {
                                            if(all==0){
                                                reader.beginObject()
                                            }
                                            token = reader.peek()
                                            if (token == com.google.gson.stream.JsonToken.NAME) {
                                                fieldName = reader.nextName()
                                            }
                                            if (fieldName == "id") {
                                                if (all != 0)
                                                    all = 0
                                                all++
                                                point = WhiteboardMetadata.PointMetadata(
                                                    reader.nextInt(),
                                                    0f,
                                                    0f
                                                )
                                                token = reader.peek()
                                            }
                                            if (fieldName == "x") {
                                                point.x = reader.nextDouble().toFloat()
                                                token = reader.peek()
                                                all++
                                            }
                                            if (fieldName == "y") {
                                                point.y = reader.nextDouble().toFloat()
                                                all++
                                                reader.endObject()
                                                token = reader.peek()
                                            }
                                            if (all == 3) {
                                                token = reader.peek()
                                                all = 0
                                                listOfPoints.add(point)
                                            }
                                        }
                                        reader.endArray()
                                        reader.endObject()
                                        token = reader.peek()
                                        allStroke = 0
                                        stroke.listPointMetadata=listOfPoints
                                        listOfPoints = mutableListOf()
                                        listOfStrokes.add(stroke)
                                    }
                                }
                            }
                            reader.endArray()
                        }
                    }
                    whiteboardMetadata.listStrokeMetadata = listOfStrokes
                    reader.endObject()
                }
            return whiteboardMetadata
        }

    }
}