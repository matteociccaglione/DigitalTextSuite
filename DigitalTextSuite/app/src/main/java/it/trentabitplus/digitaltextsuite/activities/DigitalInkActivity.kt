package it.trentabitplus.digitaltextsuite.activities

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import it.trentabitplus.digitaltextsuite.database.DigitalizedWhiteboards
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.mlkit.nl.translate.TranslateLanguage
import it.trentabitplus.digitaltextsuite.R
import it.trentabitplus.digitaltextsuite.database.DbDigitalPhotoEditor
import it.trentabitplus.digitaltextsuite.database.Note
import it.trentabitplus.digitaltextsuite.databinding.ActivityDigitalInkBinding
import it.trentabitplus.digitaltextsuite.databinding.CheckboxBinding
import it.trentabitplus.digitaltextsuite.enumeration.DigitalInkState
import it.trentabitplus.digitaltextsuite.enumeration.DrawingMode
import it.trentabitplus.digitaltextsuite.enumeration.TextResultType
import it.trentabitplus.digitaltextsuite.fragment.dialog.ColorDialog
import it.trentabitplus.digitaltextsuite.fragment.dialog.LanguageDialogFragment
import it.trentabitplus.digitaltextsuite.fragment.dialog.MakeDirectoryDialog
import it.trentabitplus.digitaltextsuite.fragment.dialog.PenDialog
import it.trentabitplus.digitaltextsuite.interfaces.DigitalRecognizerHandler
import it.trentabitplus.digitaltextsuite.interfaces.StatusChangedListener
import it.trentabitplus.digitaltextsuite.utils.Language
import it.trentabitplus.digitaltextsuite.utils.digitalink.DigitalInkManager
import it.trentabitplus.digitaltextsuite.utils.digitalink.RecognitionTask
import it.trentabitplus.digitaltextsuite.utils.digitalink.SaveManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class DigitalInkActivity : AppCompatActivity(),StatusChangedListener,DigitalRecognizerHandler{
    private lateinit var binding: ActivityDigitalInkBinding
    private lateinit var whiteboard: DigitalizedWhiteboards
    private lateinit var manager: DigitalInkManager
    private lateinit var language: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDigitalInkBinding.inflate(layoutInflater)
        setContentView(binding.root)
        whiteboard = intent.getParcelableExtra("whiteboard") ?: DigitalizedWhiteboards()
    }
    override fun onResume(){
        super.onResume()
        setUI()
    }
    private fun setUI(){
        binding.textView5.visibility=View.GONE
        binding.progressBar2.visibility=View.GONE
        binding.whiteboard.refresh()
        manager = DigitalInkManager()
        manager.setStatusChangedListener(this)
        manager.setDigitalInkHandler(this)
        binding.whiteboard.setDigitalInkManager(manager)
        if(!whiteboard.isEmpty()){
            val saveManager = SaveManager()
            saveManager.path = whiteboard.path
            val whiteboardMetadata = saveManager.fromJsonToMetadata()
            binding.whiteboard.setContent(whiteboardMetadata.toMutableList())
        }
        binding.btnErase.setOnClickListener{
            binding.whiteboard.drawingMode= DrawingMode.ERASE
            binding.selected.setBackgroundColor(ContextCompat.getColor(this,R.color.unselected))
            binding.selected2.setBackgroundColor(ContextCompat.getColor(this,R.color.selected_blue))
            binding.selected3.setBackgroundColor(ContextCompat.getColor(this,R.color.unselected))
        }
        binding.btnPen.setOnClickListener{
            binding.whiteboard.drawingMode= DrawingMode.DRAW
            binding.selected2.setBackgroundColor(ContextCompat.getColor(this,R.color.unselected))
            binding.selected.setBackgroundColor(ContextCompat.getColor(this,R.color.selected_blue))
            binding.selected3.setBackgroundColor(ContextCompat.getColor(this,R.color.unselected))
            binding.btnPen.setBackgroundColor(Color.GRAY)
            binding.btnErase.setBackgroundColor(Color.TRANSPARENT)
            binding.btnPickColor.setBackgroundColor(Color.TRANSPARENT)
            val penPick = PenDialog.getInstance()
            var value : Int
            penPick.setOnStrokeSelected {
                value = penPick.stroke
                Log.d("spessore","prima"+binding.whiteboard.stroke)
                setStroke(value)
                Log.d("spessore", penPick.stroke.toString())
                Log.d("spessore", value.toString())
                Log.d("spessore","dopo"+binding.whiteboard.stroke)
            }
            penPick.show(supportFragmentManager,"PenDialog")
        }
        binding.btnPickColor.setOnClickListener{
            val colorPick = ColorDialog.getInstance()
            var colore : Int
            colorPick.setOnColorSelected {
                colore = colorPick.colors
                Log.d("Colore", "prima"+binding.whiteboard.color.toString())
                setColor(colore)
                Log.d("Colore", colorPick.colors.toString())
                Log.d("Colore", colore.toString())
                Log.d("Colore", "dopo"+binding.whiteboard.color.toString())
            }
            colorPick.show(supportFragmentManager,"ColorDialog")

            binding.selected.setBackgroundColor(ContextCompat.getColor(this,R.color.unselected))
            binding.selected3.setBackgroundColor(ContextCompat.getColor(this,R.color.selected_blue))
            binding.selected2.setBackgroundColor(ContextCompat.getColor(this,R.color.unselected))
        }
        binding.ibDelPage.setOnClickListener{
            binding.whiteboard.removePage()
        }
        binding.btnClearWhiteboard.setOnClickListener{
            binding.whiteboard.clear(true)
        }
        binding.ibNewPage.setOnClickListener{
            binding.whiteboard.nextPage()
            binding.ibPrevPage.isEnabled = true
        }
        binding.ibPrevPage.setOnClickListener{
            if(!binding.whiteboard.prevPage()){
                binding.ibPrevPage.isEnabled = false
            }
        }
        binding.btnDigitalizeWhiteboard.setOnClickListener {
            digitalizeNote()
        }
        binding.buttonSaveWhiteboard.setOnClickListener{
            saveWhiteboard()
        }
    }
    private fun setColor(RGB : Int){
        binding.whiteboard.setDrawColor(RGB)
    }
    private fun setStroke(dpWidth : Int){
        binding.whiteboard.setDrawWidth(dpWidth)
    }
    private fun digitalizeNote(){
        val availableLanguages: List<Language> = TranslateLanguage.getAllLanguages()
            .map { Language(it) }
        val listLang: MutableList<String> = ArrayList()
        for (lan in availableLanguages) {
            listLang.add(lan.displayName)
        }
        listLang.add("emoji")
        listLang.sort()
        if (whiteboard.isEmpty()) {
            saveWhiteboard(true)
        }
        else {
            if (whiteboard.idNote == null) {
                val dialog = LanguageDialogFragment.getInstance(listLang)
                dialog.setOnLanguageSelected {
                    for (lan in availableLanguages) {
                        if (lan.displayName == it) {
                            language = lan.code
                            break
                        }
                    }
                    if(it=="emoji")
                        language = "emoji"
                    saveWhiteboard()
                    recognize(language)
                }
                dialog.show(supportFragmentManager, "LANGDIALOG")
            } else {
                val alertDialog = AlertDialog.Builder(this)
                val binding = CheckboxBinding.inflate(layoutInflater)
                val checkbox = binding.checkBox
                binding.root.removeView(checkbox)
                alertDialog.setView(checkbox)
                alertDialog.setTitle(R.string.digitalized_alert_title)
                alertDialog.setMessage(R.string.digitalized_alert_message)
                val sharedPref = this.getPreferences(Context.MODE_PRIVATE)
                val edit = sharedPref.edit()
                checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
                    edit.putBoolean(getString(R.string.shared_key_no_remember), isChecked)
                    edit.apply()
                }
                alertDialog.setPositiveButton(R.string.ok) { dialogInterface: DialogInterface, _: Int ->
                    saveWhiteboard()
                    overrideDigital()
                    dialogInterface.cancel()
                }
                alertDialog.setNegativeButton(R.string.canc) { dialogInterface: DialogInterface, _: Int ->
                    dialogInterface.cancel()
                }
                if (!sharedPref.getBoolean(getString(R.string.shared_key_no_remember), false)) {
                    alertDialog.show()
                } else {
                    overrideDigital()
                }
            }
        }
    }
    private fun overrideDigital(){
        val dao = DbDigitalPhotoEditor.getInstance(this).digitalPhotoEditorDAO()
        CoroutineScope(Dispatchers.IO).launch{
            val note = dao.loadNoteById(whiteboard.idNote!!)
            this@DigitalInkActivity.language = if(note.language=="und") "emoji" else note.language
            CoroutineScope(Dispatchers.Main).launch {
                recognize(language)
            }
        }
    }
    private fun recognize(language: String){
        manager.setActiveModel(language)
        binding.progressBar2.visibility = View.VISIBLE
        binding.textView5.visibility = View.VISIBLE
        binding.buttonBar.visibility = View.GONE
        binding.verticalBar.visibility = View.GONE
        binding.whiteboard.isEnabled = false
        binding.textView5.text = getString(R.string.start)
        manager.recognizeAll()
    }
    private fun saveWhiteboard(digitalize: Boolean = false){
        val pickerDirectory = MakeDirectoryDialog()
        val directories = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),"digitaltextsuite"+ File.separator+"whiteboards")
        val listName = mutableListOf<String>()
        val content = directories.listFiles()
        if(!whiteboard.isEmpty()){
            binding.whiteboard.saveBoard(
                File(whiteboard.path),
                File(whiteboard.imgPath)
            )
        }
        else {
            if (content != null) {
                for (dir in content) {
                    listName.add(dir.name)
                }
                pickerDirectory.setDirectoryList(listName)
                pickerDirectory.setOnDirectorySelected { dir: String, title: String ->
                    saveListener(title, dir, listName,digitalize)
                }
                pickerDirectory.show(supportFragmentManager, "MKDIRDIALOG")
            }
        }
    }
    private fun saveListener(title: String,dir: String,listName: List<String>,digitalize: Boolean = false){
        if(!listName.contains(dir)){
            val newDir = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),"digitaltextsuite"+File.separator+"whiteboards"+File.separator+dir)
            newDir.mkdir()
        }
        CoroutineScope(Dispatchers.IO).launch {
            val dao = DbDigitalPhotoEditor.getInstance(this@DigitalInkActivity).digitalPhotoEditorDAO()
            if(whiteboard.isEmpty()){
                dao.insertWhiteBoards(whiteboard)
                whiteboard = DigitalizedWhiteboards(dao.loadLastId(),"","",null,"")
            }
            val photoDir = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),"digitaltextsuite"+File.separator+"whiteboardsImages")
            if(!photoDir.exists())
                photoDir.mkdir()
            val jsonUri = File(
                getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                "digitaltextsuite" + File.separator + "whiteboards" + File.separator + dir + File.separator + title+"_"+whiteboard.id + ".json"
            )
            val imgUri = File(photoDir.absolutePath,title+"_"+whiteboard.id+".png")
            binding.whiteboard.saveBoard(
                jsonUri,
                imgUri
            )
            whiteboard.path=jsonUri.absolutePath
            whiteboard.imgPath = imgUri.absolutePath
            whiteboard.title=title
            dao.updateWhiteboard(whiteboard)
            CoroutineScope(Dispatchers.Main).launch{
                Toast.makeText(this@DigitalInkActivity,"SAVED",Toast.LENGTH_LONG).show()
                if(digitalize){
                    digitalizeNote()
                }
            }
        }
    }
    override fun onStatusChanged(status: DigitalInkState) {
        when(status){
            DigitalInkState.NO_MODEL_DOWNLOADED -> manager.download()
            DigitalInkState.DOWNLOAD_START -> {
                binding.buttonBar.visibility= View.GONE
//                binding.whiteboard.visibility= View.GONE
                binding.progressBar2.visibility=View.VISIBLE
                binding.textView5.visibility=View.VISIBLE
                binding.textView5.text = getString(R.string.downloading_lan)
            }
            DigitalInkState.MODEL_DOWNLOADED->{
                binding.buttonBar.visibility= View.GONE
                binding.verticalBar.visibility = View.GONE
//                binding.whiteboard.visibility= View.GONE
                binding.progressBar2.visibility=View.VISIBLE
                binding.textView5.visibility=View.VISIBLE
                binding.textView5.text = getString(R.string.recognize_text)
                manager.recognizeAll()
            }
            DigitalInkState.EMPTY_INK,DigitalInkState.NO_RECOGNIZER,DigitalInkState.NO_MODEL_SET,DigitalInkState.UNKOWN_ERROR ->{
                binding.buttonBar.visibility = View.VISIBLE
                binding.whiteboard.visibility=View.VISIBLE
                binding.progressBar2.visibility=View.GONE
                binding.textView5.visibility=View.GONE
                binding.whiteboard.isEnabled = true
                binding.verticalBar.visibility=View.GONE
                Toast.makeText(this@DigitalInkActivity,getString(R.string.digitink_error)+status.name,Toast.LENGTH_LONG).show()
            }
            DigitalInkState.NO_MODEL_AVAILABLE ->{
                CoroutineScope(Dispatchers.Main).launch{
                    Toast.makeText(this@DigitalInkActivity,getString(R.string.digitink_no_lang),Toast.LENGTH_LONG).show()
                }
            }
            else -> {
            }
        }
    }

    override fun onChangedResult(content: MutableList<RecognitionTask.RecognizedInk>) {
        binding.buttonBar.visibility = View.VISIBLE
        binding.verticalBar.visibility = View.VISIBLE
        binding.whiteboard.visibility=View.VISIBLE
        binding.progressBar2.visibility=View.GONE
        binding.textView5.visibility=View.GONE
        binding.whiteboard.isEnabled = true
        content.sortBy{
            it.page
        }
        val stringResult : StringBuilder = java.lang.StringBuilder()
        for(recognized in content){
            stringResult.append(recognized.text)
            stringResult.append("\n")
        }
        var note = Note(stringResult.toString(),"","",
            if(language=="emoji")
                "und"
            else
                language,
            System.currentTimeMillis(),false)
        CoroutineScope(Dispatchers.IO).launch {
            var textResultType = TextResultType.NOT_SAVED
            if (whiteboard.idNote != null) {
                Log.d("IDNOTE",whiteboard.idNote.toString())
                val dao = DbDigitalPhotoEditor.getInstance(this@DigitalInkActivity).digitalPhotoEditorDAO()
                val oldNote = dao.loadNoteById(whiteboard.idNote!!)
                oldNote.text = stringResult.toString()
                dao.updateNote(oldNote)
                note = oldNote
                textResultType = TextResultType.SAVED
            }
            CoroutineScope(Dispatchers.Main).launch {
                val intent = Intent(this@DigitalInkActivity, TextResultActivity::class.java)
                intent.putExtra("type", textResultType.ordinal)
                intent.putExtra("result", note)
                intent.putExtra("whiteboard", whiteboard)
                startActivity(intent)
            }
        }
    }
}