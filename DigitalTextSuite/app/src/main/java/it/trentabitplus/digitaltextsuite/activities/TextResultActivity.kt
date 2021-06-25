
package it.trentabitplus.digitaltextsuite.activities

import android.content.*
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.nl.translate.TranslateLanguage
import it.trentabitplus.digitaltextsuite.R
import it.trentabitplus.digitaltextsuite.database.DAODigitalPhotoEditor
import it.trentabitplus.digitaltextsuite.database.DbDigitalPhotoEditor
import it.trentabitplus.digitaltextsuite.database.DigitalizedWhiteboards
import it.trentabitplus.digitaltextsuite.database.Note
import it.trentabitplus.digitaltextsuite.databinding.ActivityTextResultBinding
import it.trentabitplus.digitaltextsuite.databinding.CheckboxBinding
import it.trentabitplus.digitaltextsuite.enumeration.TextResultType
import it.trentabitplus.digitaltextsuite.fragment.dialog.LanguageDialogFragment
import it.trentabitplus.digitaltextsuite.fragment.dialog.MakeDirectoryDialog
import it.trentabitplus.digitaltextsuite.utils.Language
import it.trentabitplus.digitaltextsuite.utils.Translator
import it.trentabitplus.digitaltextsuite.utils.pdf.PdfManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class TextResultActivity : AppCompatActivity() {
    private var fabMenuShowed = false
    private lateinit var menu: Menu
    private lateinit var textResult: String
    private lateinit var binding: ActivityTextResultBinding
    private lateinit var type: TextResultType
    private lateinit var originalText: String
    private lateinit var dao: DAODigitalPhotoEditor
    private lateinit var database : DbDigitalPhotoEditor
    private lateinit var language: String
    private lateinit var note: Note
    private lateinit var whiteboard: DigitalizedWhiteboards
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTextResultBinding.inflate(layoutInflater)
        setContentView(binding.root)
        note = intent.getParcelableExtra("result") ?: Note("","","","",System.currentTimeMillis(),false)
        textResult = note.text
        language = note.language
        originalText=textResult
        whiteboard = intent.getParcelableExtra("whiteboard") ?: DigitalizedWhiteboards()
        val ordinal = intent.getIntExtra("type",TextResultType.NOT_SAVED.ordinal)
        type = when(ordinal){
            TextResultType.SAVED.ordinal -> TextResultType.SAVED
            TextResultType.EDITABLE.ordinal -> TextResultType.EDITABLE
            else -> TextResultType.NOT_SAVED
        }
        initializeDB()
        setUI()
    }



    private fun initializeDB(){
        database = DbDigitalPhotoEditor.getInstance(this)
        dao = database.digitalPhotoEditorDAO()
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.text_result_menu,menu)
        this.menu=menu!!
        setType(menu)
        return super.onCreateOptionsMenu(menu)
    }
    private fun setType(menu: Menu?){
        var itSave: MenuItem? = null
        var itDelete: MenuItem? = null
        var itUndo: MenuItem? = null
        for (item in menu!!.children){
            when(item.itemId){
                R.id.it_delete -> itDelete=item
                R.id.it_save -> itSave=item
                R.id.it_undo -> itUndo=item
            }
        }
        when(type){
            TextResultType.SAVED -> {
                itDelete!!.isVisible=true
                itSave!!.isVisible=false
                itUndo!!.isVisible=false
            }
            TextResultType.NOT_SAVED -> {
                itDelete!!.isVisible=false
                itSave!!.isVisible=true
                itUndo!!.isVisible=false
            }
            TextResultType.EDITABLE->{
                itDelete!!.isVisible=false
                itSave!!.isVisible=true
                itUndo!!.isVisible=true
            }
        }
    }
    private fun editableSave(saved: Boolean,whiteboard: DigitalizedWhiteboards?){
        CoroutineScope(Dispatchers.IO).launch {
            note.lastDateModify = System.currentTimeMillis()
            note.text = textResult
            val result = MutableLiveData<String>()
            CoroutineScope(Dispatchers.Main).launch {
                val observer = Observer<String> {
                    note.language = it
                    CoroutineScope(Dispatchers.IO).launch {
                        dao.updateNote(note)
                        if (whiteboard != null) {
                            whiteboard.idNote = null
                            dao.updateWhiteboard(whiteboard)
                        }
                        CoroutineScope(Dispatchers.Main).launch {
                            if (saved) {
                                type = TextResultType.SAVED
                                setType(menu)
                            }
                        }
                    }
                }
                result.observe(this@TextResultActivity, observer)
                Language.identifyLanguage(note.text, result)
            }
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.it_save -> {
                var saved = true
                CoroutineScope(Dispatchers.IO).launch {
                    val directoryList = dao.loadDirectories()
                    CoroutineScope(Dispatchers.Main).launch {
                        val dialog = MakeDirectoryDialog.getInstance()
                        dialog.setDirectoryList(directoryList)
                        if (type == TextResultType.NOT_SAVED) {
                            dialog.setOnDirectorySelected { directory: String, title: String ->
                                note.text = textResult
                                note.language = language
                                note.directory = directory
                                note.title = title
                                CoroutineScope(Dispatchers.IO).launch {
                                    dao.insertNote(note)
                                    val lastNote = dao.loadLastIdNote()
                                    note.id = lastNote
                                    if(!whiteboard.isEmpty()){
                                        whiteboard.idNote = lastNote
                                        dao.updateWhiteboard(whiteboard)
                                    }
                                    CoroutineScope(Dispatchers.Main).launch {
                                        if (saved) {
                                            type = TextResultType.SAVED
                                            setType(menu)
                                        }
                                    }
                                }
                            }
                            dialog.setOnCancelListener {
                                saved = false
                                true
                            }
                            dialog.show(supportFragmentManager, "DIALOGDIR")
                        } else if (type == TextResultType.EDITABLE) {
                            CoroutineScope(Dispatchers.IO).launch {
                                val wb = dao.loadWhiteboard(note.id)
                                CoroutineScope(Dispatchers.Main).launch {
                                    val alertDialog = AlertDialog.Builder(this@TextResultActivity)
                                    val binding = CheckboxBinding.inflate(layoutInflater)
                                    val checkbox = binding.checkBox
                                    val sharedPreferences = this@TextResultActivity.getPreferences(
                                        Context.MODE_PRIVATE
                                    )
                                    val edit = sharedPreferences.edit()
                                    checkbox.setOnCheckedChangeListener { _: CompoundButton, b: Boolean ->
                                        edit.putBoolean("no_ask_me", b)
                                        edit.apply()
                                    }
                                    binding.root.removeView(checkbox)
                                    alertDialog.setView(checkbox)
                                    alertDialog.setTitle(R.string.digitalized_alert_title)
                                    alertDialog.setMessage(R.string.note_whiteboard_alert_message)
                                    alertDialog.setPositiveButton(R.string.yes) { dialogInterface: DialogInterface, _: Int ->
                                        editableSave(saved,wb)
                                        dialogInterface.dismiss()
                                    }
                                    alertDialog.setNegativeButton(R.string.canc) { dialogInterface: DialogInterface, _: Int ->
                                        dialogInterface.dismiss()
                                    }
                                    if (wb != null && !sharedPreferences.getBoolean(
                                            "no_ask_me",
                                            false
                                        )
                                    ) {
                                        alertDialog.show()
                                    } else {
                                        editableSave(saved,wb)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            R.id.it_editable->{
                if(type==TextResultType.NOT_SAVED){
                    Toast.makeText(this,getString(R.string.not_saved_edit),Toast.LENGTH_LONG).show()
                }
                else {
                    var editable = false
                    for (item1 in menu.children) {
                        if (item1.itemId == R.id.it_editable) {
                            item1.isChecked = !item1.isChecked
                            editable = item1.isChecked
                        }
                    }
                    if (editable) {
                        type = TextResultType.EDITABLE
                        binding.editTextTextMultiLine.isEnabled = true
                        setType(menu)
                    } else {
                        if (originalText == textResult) {
                            type = TextResultType.SAVED
                            setType(menu)
                        }
                        binding.editTextTextMultiLine.isEnabled = false
                    }
                }
            }
            R.id.it_undo -> {
                binding.editTextTextMultiLine.text.clear()
                binding.editTextTextMultiLine.text.append(originalText)
                textResult=originalText
            }
            R.id.it_delete -> {
                val alert = AlertDialog.Builder(this)
                alert.setTitle(R.string.alert_delete_title)
                alert.setMessage(R.string.alert_delete_message)
                alert.setPositiveButton(R.string.yes) { dialogInterface: DialogInterface, _: Int ->
                    CoroutineScope(Dispatchers.IO).launch {
                    dao.deleteNote(note)
                        CoroutineScope(Dispatchers.Main).launch {
                            dialogInterface.cancel()
                            finish()
                        }
                }
                }
                alert.setNegativeButton(R.string.no) { dialogInterface: DialogInterface, _: Int ->
                    dialogInterface.cancel()
                }
                alert.show()
            }
            R.id.it_copy -> {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("note",textResult)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this,getString(R.string.copied),Toast.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }
    private fun setUI(){
        binding.fabFavourite.setImageDrawable(if(note.preferito)
            ContextCompat.getDrawable(this@TextResultActivity,R.drawable.ic_baseline_star_24)
        else
            ContextCompat.getDrawable(this@TextResultActivity,R.drawable.favourite_icon_24))
        binding.fabMore.setOnClickListener{
            if(fabMenuShowed){
                closeFabMenu()
            }
            else{
                val orientation = resources.configuration.orientation
                if(orientation == Configuration.ORIENTATION_LANDSCAPE){
                    binding.fabTranslate.animate()
                        .translationX(-resources.getDimension(R.dimen.fab_move_80))
                    binding.fabPrintPdf.animate()
                        .translationX(-resources.getDimension(R.dimen.fab_move_150))
                    binding.fabFavourite.animate()
                        .translationX(-resources.getDimension(R.dimen.fab_move_225))
                }
                else {
                    binding.fabTranslate.animate()
                        .translationY(-resources.getDimension(R.dimen.fab_move_80))
                    binding.fabPrintPdf.animate()
                        .translationY(-resources.getDimension(R.dimen.fab_move_150))
                    binding.fabFavourite.animate()
                        .translationY(-resources.getDimension(R.dimen.fab_move_225))
                }
                fabMenuShowed=true
                binding.fabMore.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.close_24))
            }
        }
        binding.fabFavourite.setOnClickListener{
            CoroutineScope(Dispatchers.IO).launch{
                note.preferito = !note.preferito
                dao.updateNote(note)
                CoroutineScope(Dispatchers.Main).launch{
                    binding.fabFavourite.setImageDrawable(if(note.preferito)
                        ContextCompat.getDrawable(this@TextResultActivity,R.drawable.ic_baseline_star_24)
                    else
                        ContextCompat.getDrawable(this@TextResultActivity,R.drawable.favourite_icon_24))
                }
            }
        }
        binding.editTextTextMultiLine.text.clear()
        binding.editTextTextMultiLine.text.append(textResult)
        binding.editTextTextMultiLine.isEnabled=false
        binding.editTextTextMultiLine.addTextChangedListener {
            textResult = binding.editTextTextMultiLine.text.toString()
        }

        binding.fabPrintPdf.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val stringDirectoryList: MutableList<String> = mutableListOf()
                RealMainActivity.pdfDir.listFiles()?.forEach { stringDirectoryList.add(it.name) }
                stringDirectoryList.add(getString(R.string.new_dir))
                CoroutineScope(Dispatchers.Main).launch {
                    val pdfDialog = MakeDirectoryDialog.getInstance()
                    pdfDialog.setDirectoryList(stringDirectoryList)

                    pdfDialog.setOnDirectorySelected { directory: String, title: String ->
                        val text = textResult
                        val dir = File(RealMainActivity.pdfDir, directory)
                        CoroutineScope(Dispatchers.IO).launch {
                            dir.apply { mkdirs() }
                            PdfManager.transformToPdf(title, text, dir)
                            CoroutineScope(Dispatchers.Main).launch {
                                Toast.makeText(
                                    this@TextResultActivity,
                                    getString(R.string.pdf_saved),
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }
                        }
                    }

                    pdfDialog.setOnCancelListener {
                        true
                    }

                    pdfDialog.show(supportFragmentManager, "DIALOGPDF")
                }
            }
        }

        binding.fabTranslate.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val availableLanguages: List<Language> = TranslateLanguage.getAllLanguages()
                    .map { Language(it) }
                val listLang: MutableList<String> = ArrayList()
                for (lan in availableLanguages) {
                    listLang.add(lan.displayName)
                }
                CoroutineScope(Dispatchers.Main).launch {
                    val dialog = LanguageDialogFragment.getInstance(listLang)
                    dialog.setOnLanguageSelected {
                        var targetLang = "und"
                        for (lan in availableLanguages) {
                            if (lan.displayName == it) {
                                targetLang = lan.code
                                break
                            }
                        }
                        if(targetLang != language){
                            setNormalLayoutEnable(false)
                            binding.grpTranslation.visibility = View.VISIBLE
                            CoroutineScope(Dispatchers.Default).launch {
                                val translator = Translator()

                                // waiting the translation, blocking UI
                                kotlin.runCatching {
                                    val newRes = Tasks.await(translator.translate(textResult, language, targetLang))
                                    CoroutineScope(Dispatchers.Main).launch {
                                        binding.grpTranslation.visibility = View.GONE
                                        val intent = Intent(this@TextResultActivity,TextResultActivity::class.java)
                                        val newNote = Note(newRes,"","",targetLang,System.currentTimeMillis(),false)
                                        intent.putExtra("result",newNote)
                                        intent.putExtra("type",TextResultType.NOT_SAVED)
                                        startActivity(intent)
                                        setNormalLayoutEnable(true)
                                    }
                                }
                            }
                        }
                    }
                    if(note.language != "und"){
                    dialog.show(supportFragmentManager, "LANGDIALOG")
                    }else{
                        Toast.makeText(this@TextResultActivity, getString(R.string.und_translation_error), Toast.LENGTH_SHORT).show()
                    }
                }


            }

        }
    }

    private fun setNormalLayoutEnable (enabled: Boolean){
        binding.fabTranslate.isEnabled = enabled
        binding.fabPrintPdf.isEnabled = enabled
        binding.fabMore.isEnabled = enabled
        binding.fabFavourite.isEnabled = enabled
        binding.editTextTextMultiLine.isEnabled = enabled
        binding.scrollView2.isEnabled = enabled
    }
    private fun closeFabMenu(){
        val orientation = resources.configuration.orientation
        if(orientation == Configuration.ORIENTATION_LANDSCAPE){
            binding.fabPrintPdf.animate().translationX(0f)
            binding.fabTranslate.animate().translationX(0f)
            binding.fabFavourite.animate().translationX(0f)
        }
        else {
            binding.fabPrintPdf.animate().translationY(0f)
            binding.fabTranslate.animate().translationY(0f)
            binding.fabFavourite.animate().translationY(0f)
        }
        binding.fabMore.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_baseline_add_24))
        fabMenuShowed=false
    }
}