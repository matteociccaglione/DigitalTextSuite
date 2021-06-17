package it.trentabitplus.digitaltextsuite.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import it.trentabitplus.digitaltextsuite.R
import it.trentabitplus.digitaltextsuite.databinding.ActivityPlayWithEmojiBinding
import it.trentabitplus.digitaltextsuite.enumeration.DigitalInkState
import it.trentabitplus.digitaltextsuite.enumeration.DrawingMode
import it.trentabitplus.digitaltextsuite.fragment.dialog.DialogWinPlayEmoji
import it.trentabitplus.digitaltextsuite.interfaces.DigitalRecognizerHandler
import it.trentabitplus.digitaltextsuite.interfaces.StatusChangedListener
import it.trentabitplus.digitaltextsuite.notifications.NotificationBuilder
import it.trentabitplus.digitaltextsuite.utils.digitalink.DigitalInkManager
import it.trentabitplus.digitaltextsuite.utils.digitalink.RecognitionTask
import it.trentabitplus.digitaltextsuite.viewmodel.PlayWithEmojiViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.random.Random

class PlayWithEmojiActivity : AppCompatActivity(), DigitalRecognizerHandler,StatusChangedListener{
    private lateinit var binding: ActivityPlayWithEmojiBinding
    private lateinit var manager: DigitalInkManager

    private var highScore = 0
    private val viewModel : PlayWithEmojiViewModel by viewModels()
    private  val FILENAME = "emojiBuone.csv"
    private var listEmoji = ArrayList<CharArray>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayWithEmojiBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
    override fun onResume(){
        super.onResume()
        NotificationBuilder(this).setNotificationOn()
        setUI()
    }
    private fun setEmoji(){
        if(viewModel.emoji=="") {
            val reader = assets.open(FILENAME).bufferedReader()
            CoroutineScope(Dispatchers.IO).launch {
                if (listEmoji.isEmpty()) {
                    reader.useLines { lines ->
                        lines.forEach {
                            val emojiRead = it.split(",")[0].split(" ")[0]
                            Log.d("EMOJI", emojiRead)
                            listEmoji.add(
                                Character.toChars(
                                    Integer.decode(
                                        "0x" + emojiRead.substring(
                                            2,
                                            emojiRead.length
                                        )
                                    )
                                )
                            )
                        }
                    }
                }
                CoroutineScope(Dispatchers.Main).launch {
                    val random = Random.nextInt(0, listEmoji.size)
                    viewModel.emoji = String(listEmoji[random])
                    binding.tvEmoji.text =
                        String.format(getString(R.string.draw_emoji), viewModel.emoji)
                }
            }
        }
    }
    private fun updateScore(){
        if(viewModel.actualScore>highScore) {
            highScore = viewModel.actualScore
            val edit = getPreferences(Context.MODE_PRIVATE).edit()
            edit.putInt("highscore",highScore)
            edit.apply()
        }
        binding.tvHighScore.text = String.format(getString(R.string.highscore),highScore)
        binding.tvActualScore.text = String.format(getString(R.string.actual_score),viewModel.actualScore)
    }
    private fun setUI(){
        setEmoji()
        val sharedPref = getPreferences(Context.MODE_PRIVATE)
        highScore = sharedPref.getInt("highscore",0)
        viewModel.actualScore = sharedPref.getInt("actual_score",0)
        updateScore()
        binding.tvEmoji.text = String.format(getString(R.string.draw_emoji),viewModel.emoji)
        binding.tvAttempts.text = String.format(getString(R.string.emoji_attempts),viewModel.attempts)
        binding.textView13.visibility = View.GONE
        binding.progressBar4.visibility = View.GONE
        binding.whiteboardEmoji.refresh()
        manager = DigitalInkManager()
        manager.setStatusChangedListener(this)
        manager.setDigitalInkHandler(this)
        binding.whiteboardEmoji.setDigitalInkManager(manager)
        binding.ibEraser.setOnClickListener{
            binding.whiteboardEmoji.drawingMode = DrawingMode.ERASE
        }
        binding.ibEdit.setOnClickListener{
            binding.whiteboardEmoji.drawingMode = DrawingMode.DRAW
        }
        binding.btnGo.setOnClickListener{
            recognize()
        }
    }
    private fun recognize(){
        manager.setActiveModel("emoji")
        binding.buttonBarEmoji.visibility = View.GONE
        binding.progressBar4.visibility = View.VISIBLE
        binding.textView13.visibility = View.VISIBLE
        manager.recognizeAll()
    }
    private fun resetActualScore(edit: SharedPreferences.Editor){
        viewModel.actualScore = 0
        edit.putInt("actual_score",viewModel.actualScore)
        edit.apply()
    }
    override fun onChangedResult(content: MutableList<RecognitionTask.RecognizedInk>) {
        binding.progressBar4.visibility = View.GONE
        binding.textView13.visibility = View.GONE
        binding.buttonBarEmoji.visibility = View.VISIBLE
        val result = content[0].text
        val sharedPref = getPreferences(Context.MODE_PRIVATE)
        val edit = sharedPref.edit()
        if(result.equals(viewModel.emoji)){
            //You win
            val dialog = DialogWinPlayEmoji.getInstance()
            dialog.emoji = result!!
            dialog.setOnNewGameClicked{
                viewModel.attempts = 3
                viewModel.emoji = ""
                onResume()
            }
            dialog.setOnCloseClicked {
                resetActualScore(edit)
                val intent = Intent(this,RealMainActivity::class.java)
                startActivity(intent)
            }
            viewModel.actualScore++
            updateScore()
            edit.putInt("actual_score",viewModel.actualScore)
            edit.apply()
            dialog.show(supportFragmentManager,"WINDIALOG")
        }
        else{
            viewModel.attempts--
            binding.tvAttempts.text = String.format(getString(R.string.emoji_attempts),viewModel.attempts)
            Toast.makeText(this,String.format(getString(R.string.emoji_insert),result),Toast.LENGTH_LONG).show()
            if(viewModel.attempts == 0){
                resetActualScore(edit)
                val dialog = DialogWinPlayEmoji.getInstance()
                dialog.setOnNewGameClicked {
                    viewModel.emoji = ""
                    viewModel.attempts = 3
                    onResume()
                }
                dialog.win = false
                dialog.setOnCloseClicked {
                    resetActualScore(edit)
                    val intent = Intent(this,RealMainActivity::class.java)
                    startActivity(intent)
                }
                dialog.show(supportFragmentManager,"WINDIALOG")
            }
        }
    }

    override fun onDestroy() {
        val edit = getPreferences(Context.MODE_PRIVATE).edit()
        edit.putInt("actual_score",0)
        edit.apply()
        super.onDestroy()
    }
    override fun onStatusChanged(status: DigitalInkState) {
        when(status){
            DigitalInkState.NO_MODEL_DOWNLOADED -> manager.download()
            DigitalInkState.DOWNLOAD_START -> {
                binding.buttonBarEmoji.visibility= View.GONE
//                binding.whiteboard.visibility= View.GONE
                binding.progressBar4.visibility=View.VISIBLE
                binding.textView13.visibility=View.VISIBLE
                binding.textView13.text = getString(R.string.downloading_lan)
            }
            DigitalInkState.MODEL_DOWNLOADED->{
                binding.buttonBarEmoji.visibility= View.GONE
                binding.progressBar4.visibility=View.VISIBLE
                binding.textView13.visibility=View.VISIBLE
                binding.textView13.text = getString(R.string.recognize_text)
                manager.recognizeAll()
            }
            DigitalInkState.EMPTY_INK,DigitalInkState.NO_RECOGNIZER,DigitalInkState.NO_MODEL_SET,DigitalInkState.UNKOWN_ERROR ->{
                binding.buttonBarEmoji.visibility = View.VISIBLE
                binding.progressBar4.visibility=View.GONE
                binding.textView13.visibility=View.GONE
                binding.whiteboardEmoji.isEnabled = true
                Toast.makeText(this@PlayWithEmojiActivity,getString(R.string.digitink_error)+status.name,
                    Toast.LENGTH_LONG).show()
            }
            DigitalInkState.NO_MODEL_AVAILABLE ->{
                CoroutineScope(Dispatchers.Main).launch{
                    Toast.makeText(this@PlayWithEmojiActivity,getString(R.string.digitink_no_lang),
                        Toast.LENGTH_LONG).show()
                }
            }
            else -> {
            }
        }
    }
}