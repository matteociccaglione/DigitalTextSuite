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
    private var colors : Int = Color.BLACK
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
        isCancelable = false
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
            when (seekBar) {
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
    fun setOnCancelSelected(listener: () -> Boolean){
        cancelListener = listener
    }
}