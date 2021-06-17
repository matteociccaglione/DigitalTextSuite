package it.trentabitplus.digitaltextsuite.fragment.dialog

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import androidx.fragment.app.DialogFragment
import it.trentabitplus.digitaltextsuite.databinding.ChooseColorDialogBinding


class ColorDialog : DialogFragment() {
    private var colorImageView: ImageView? = null
    private var redValue : Int = 0
    private var greenValue : Int = 0
    private var blueValue : Int = 0
    var colors : Int = Color.BLACK
    private var colorListener: (colors : Int) -> Unit = {

    }
    private var cancelListener: () -> Boolean ={
        true
    }
    private lateinit var binding: ChooseColorDialogBinding

    companion object{
        fun getInstance(): ColorDialog{
            var instance : ColorDialog? = null
            if(instance == null){
                instance = ColorDialog()
            }
            return instance
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ChooseColorDialogBinding.inflate(inflater)
        colorImageView = binding.ColorPicker
        colorImageView!!.setBackgroundColor(Color.BLACK)
        setSeek()
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.ColorPicker
        binding.seekBarB
        binding.seekBarG
        binding.seekBarR
        setSeek()
        binding.btnOk.setOnClickListener{
            colorListener(colors)
            dismiss()
        }
        binding.btnCanc.setOnClickListener{
            if(cancelListener()){
                dismiss()
            }
        }
    }
    private fun setSeek(){
        val seekBarR = binding.seekBarR
        val seekBarG = binding.seekBarG
        val seekBarB = binding.seekBarB
        seekBarR.setOnSeekBarChangeListener(mChangeListener)
        seekBarG.setOnSeekBarChangeListener(mChangeListener)
        seekBarB.setOnSeekBarChangeListener(mChangeListener)
    }
    private val mChangeListener: SeekBar.OnSeekBarChangeListener = object :
        SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(
            seekBar: SeekBar,
            progress: Int,
            fromUser: Boolean
        ) {
            val viewId = seekBar
            when (viewId) {
                binding.seekBarR -> redValue = progress
                binding.seekBarG -> greenValue = progress
                binding.seekBarB -> blueValue = progress
            }
            colors = Color.rgb(redValue, greenValue, blueValue)
            colorImageView!!.setBackgroundColor(colors)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {

        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
        }
    }
    fun setOnColorSelected(listener: (color: Int)-> Unit){
        this.colorListener=listener
    }
}
//fun setDrawWidth(dpWidth: Int){
//        stroke = dpWidth
//        currentStrokePaint.strokeWidth = TypedValue.applyDimension(
//            TypedValue.COMPLEX_UNIT_DIP,
//            stroke.toFloat(),
//            resources.displayMetrics
//        )
//    }
//    fun setDrawColor(rgb : Int) {
//        color = rgb
//        currentStrokePaint.color = color
//    }

// binding.btnPickColor.setOnClickListener{
//            val colorPick = ColorDialog.getInstance()
//            colorPick.setOnColorSelected {
//                colore = colorPick.colors
//                Log.d("Colore", "prima"+binding.whiteboard.color.toString())
//                setColor()
//                Log.d("Colore", colorPick.colors.toString())
//                Log.d("Colore", colore.toString())
//                Log.d("Colore", "dopo"+binding.whiteboard.color.toString())
//            }
//            colorPick.show(supportFragmentManager,"ColorDialog")
//        }

//binding.btnPen.setOnClickListener{
//            binding.btnPen.setBackgroundColor(Color.GRAY)
//            binding.btnErase.setBackgroundColor(Color.TRANSPARENT)
//            binding.btnPickColor.setBackgroundColor(Color.TRANSPARENT)
//            val penPick = PenDialog.getInstance()
//            penPick.setOnStrokeSelected {
//                value = penPick.stroke
//                Log.d("spessore","prima"+binding.whiteboard.stroke)
//                setStroke()
//                Log.d("spessore", penPick.stroke.toString())
//                Log.d("spessore", value.toString())
//                Log.d("spessore","dopo"+binding.whiteboard.stroke)
//            }
//            penPick.show(supportFragmentManager,"PenDialog")
//        }