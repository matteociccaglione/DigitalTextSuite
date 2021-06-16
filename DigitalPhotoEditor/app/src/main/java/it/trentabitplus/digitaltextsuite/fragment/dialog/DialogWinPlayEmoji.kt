package it.trentabitplus.digitaltextsuite.fragment.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import it.trentabitplus.digitaltextsuite.R
import it.trentabitplus.digitaltextsuite.databinding.DialogWinPlayEmojiBinding

class DialogWinPlayEmoji: DialogFragment() {
    private lateinit var binding: DialogWinPlayEmojiBinding
    companion object{
        fun getInstance(): DialogWinPlayEmoji{
            return DialogWinPlayEmoji()
        }
    }
    var win = true
    var emoji = ""
    private var newGameSelectedListener : () -> Unit = {

    }
    private var closeSelectedListener : () -> Unit ={

    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogWinPlayEmojiBinding.inflate(inflater)
        isCancelable = false
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.tvHaveWin.text = if(win)
            String.format(requireContext().getString(R.string.win_emoji),emoji)
        else
            requireContext().getString(R.string.lose_emoji)
        binding.btnNewGame.setOnClickListener{
            newGameSelectedListener()
            dismiss()
        }
        binding.btnCloseDial.setOnClickListener{
            closeSelectedListener()
            dismiss()
        }
    }
    fun setOnNewGameClicked(listener: ()-> Unit){
        newGameSelectedListener = listener
    }
    fun setOnCloseClicked(listener: () -> Unit){
        closeSelectedListener = listener
    }
}