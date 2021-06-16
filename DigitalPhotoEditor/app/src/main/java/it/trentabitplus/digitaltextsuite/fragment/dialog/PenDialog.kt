package it.trentabitplus.digitaltextsuite.fragment.dialog

import androidx.fragment.app.DialogFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import it.trentabitplus.digitaltextsuite.databinding.ChoosePenDialogBinding

class PenDialog : DialogFragment() {
    private var value : Int = 0
    var stroke : Int = 3
    private var strokeListener: (stroke : Int) -> Unit = {

    }
    private var cancelListener: () -> Boolean ={
        true
    }
    private lateinit var binding: ChoosePenDialogBinding

    companion object{
        fun getInstance(): PenDialog{
            var instance : PenDialog? = null
            if(instance == null){
                instance = PenDialog()
            }
            return instance
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ChoosePenDialogBinding.inflate(inflater)
        setSeek()
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.seekBarN
        setSeek()
        binding.btnOk.setOnClickListener{
            strokeListener(stroke)
            dismiss()
        }
        binding.btnCanc.setOnClickListener{
            if(cancelListener()){
                dismiss()
            }
        }
    }
    private fun setSeek() {
        val seekBarN = binding.seekBarN
        seekBarN.setOnSeekBarChangeListener(mChangeListener)
    }
    private val mChangeListener: SeekBar.OnSeekBarChangeListener = object :
        SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(
            seekBar: SeekBar,
            progress: Int,
            fromUser: Boolean
        ) {
            when (seekBar) {
                binding.seekBarN -> value = progress
            }
            stroke = value
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {

        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {

        }
    }
    fun setOnStrokeSelected(listener: (stroke: Int)-> Unit){
        this.strokeListener=listener
    }
}