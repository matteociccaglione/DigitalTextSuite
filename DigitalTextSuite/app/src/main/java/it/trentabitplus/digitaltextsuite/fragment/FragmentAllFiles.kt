package it.trentabitplus.digitaltextsuite.fragment

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import it.trentabitplus.digitaltextsuite.R
import it.trentabitplus.digitaltextsuite.activities.RealMainActivity
import it.trentabitplus.digitaltextsuite.activities.TextResultActivity
import it.trentabitplus.digitaltextsuite.adapter.AllFilesBigAdapter
import it.trentabitplus.digitaltextsuite.adapter.AllFilesSmallAdapter
import it.trentabitplus.digitaltextsuite.adapter.DirectoryBigAdapter
import it.trentabitplus.digitaltextsuite.adapter.DirectorySmallAdapter
import it.trentabitplus.digitaltextsuite.database.DbDigitalPhotoEditor
import it.trentabitplus.digitaltextsuite.database.Note
import it.trentabitplus.digitaltextsuite.databinding.FragmentAllFilesBinding
import it.trentabitplus.digitaltextsuite.decorator.GridSpacingDecorator
import it.trentabitplus.digitaltextsuite.decorator.LinearSpacingDecorator
import it.trentabitplus.digitaltextsuite.enumeration.FilesShowMode
import it.trentabitplus.digitaltextsuite.enumeration.FilterMode
import it.trentabitplus.digitaltextsuite.enumeration.SortingType
import it.trentabitplus.digitaltextsuite.enumeration.TextResultType
import it.trentabitplus.digitaltextsuite.fragment.dialog.FilterDialog
import it.trentabitplus.digitaltextsuite.fragment.dialog.SelectPdfDialog
import it.trentabitplus.digitaltextsuite.interfaces.SelectedHandler
import it.trentabitplus.digitaltextsuite.utils.Directory
import it.trentabitplus.digitaltextsuite.viewmodel.AllFilesViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.*


class FragmentAllFiles : Fragment(), SelectedHandler{
    private lateinit var binding: FragmentAllFilesBinding
    private var showMode = FilesShowMode.DIR_BIG
    val viewModel: AllFilesViewModel by viewModels()
    private lateinit var adapter: AllFilesBigAdapter
    private var mActionMode: ActionMode? = null
    private lateinit var adapterSmall: AllFilesSmallAdapter
    private lateinit var adapterDirectoryBig: DirectoryBigAdapter
    private lateinit var adapterDirectorySmall: DirectorySmallAdapter
    private var selectedItem  = ArrayList<Any>()
    private var spanCount: Int = 3
    private var filterMode = FilterMode.BY_TEXT
    private var sortingType = SortingType.ALPHABETIC_ASC
    private var menu: Menu? = null
    private var actualDirectory = "Default"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true);
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentAllFilesBinding.inflate(inflater)
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        this.menu = menu
        for(it in menu.children){
            it.isVisible=true
            if(it.itemId == R.id.it_preview){
                val drawableId = if(showMode == FilesShowMode.BIG || showMode == FilesShowMode.DIR_BIG){
                    R.drawable.ic_baseline_format_list_bulleted_24
                }
                else
                    R.drawable.ic_baseline_grid_view_24
                it.icon = ContextCompat.getDrawable(requireContext(),drawableId)
            }
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.it_preview -> {
                selectedItem = ArrayList()
                mActionMode = null
                if (showMode == FilesShowMode.DIR_BIG || showMode == FilesShowMode.BIG) {
                    Log.d("HERE", "HERE")
                    item.icon = ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.ic_baseline_grid_view_24
                    )
                    if (showMode == FilesShowMode.DIR_BIG) {
                        showMode = FilesShowMode.DIR_SMALL
                        setUI()
                        setAdapterDirectory(viewModel.listDirectory.value!!)
                    } else {
                        showMode = FilesShowMode.SMALL
                        setUI()
                        setAdapterFiles(viewModel.listNotes.value!!)
                    }
                } else {
                    item.icon =
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_baseline_format_list_bulleted_24
                        )
                    if (showMode == FilesShowMode.DIR_SMALL) {
                        showMode = FilesShowMode.DIR_BIG
                        setUI()
                        setAdapterDirectory(viewModel.listDirectory.value!!)
                    } else {
                        showMode = FilesShowMode.BIG
                        setUI()
                        setAdapterFiles(viewModel.listNotes.value!!)
                    }
                }
            }
            R.id.it_filter -> {
                val sharedPreferences = requireActivity().getSharedPreferences("filter_all_files",
                    Activity.MODE_PRIVATE)
                var filterMode = sharedPreferences.getInt("filterMode",FilterMode.BY_TEXT.ordinal)
                var sortingType = sharedPreferences.getInt("sortingType",SortingType.ALPHABETIC_ASC.ordinal)
                val dialog = FilterDialog.getInstance(filterMode,sortingType)
                dialog.setOnFilterSelected{ filter: FilterMode, sorting: SortingType ->
                    selectedItem = ArrayList()
                    mActionMode = null
                    filterMode = filter.ordinal
                    sortingType = sorting.ordinal
                    this.filterMode = filter
                    this.sortingType = sorting
                    if(showMode == FilesShowMode.DIR_BIG || showMode == FilesShowMode.DIR_SMALL) {
                        if(filter == FilterMode.BY_COUNTRY){
                            Toast.makeText(requireContext(),requireContext().getString(R.string.no_country_directory),Toast.LENGTH_LONG).show()
                        }
                        viewModel.filterDirectory(binding.sv.query.toString())
                        viewModel.sortDirectories(sorting)
                    }
                    else {
                        viewModel.filter(binding.sv.query.toString(), filter,actualDirectory)
                        viewModel.sort(sorting)
                    }
                    val editor = sharedPreferences.edit()
                    editor.putInt("filterMode",filterMode)
                    editor.putInt("sortingType",sortingType)
                    editor.apply()
                }
                dialog.show(parentFragmentManager,"FILTERDIALOG")
            }
            R.id.it_pdf -> {
                CoroutineScope(Dispatchers.IO).launch {
                    val rootDir = RealMainActivity.rootDir
                    val listFiles = getPdfFilesFromRootDir(rootDir)
                    CoroutineScope(Dispatchers.Main).launch {
                        val dialog = SelectPdfDialog.getInstance(listFiles)
                        dialog.show(parentFragmentManager, "SELECTPDFDIALOG")
                    }
                }

            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getPdfFilesFromRootDir(rootDir: File): List<File> {
        return rootDir.walk().filter{
           it.extension == "pdf"
        }.toList()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if(savedInstanceState!=null){
            val mode = savedInstanceState.getInt("showMode")
            showMode = when(mode){
                FilesShowMode.BIG.ordinal -> FilesShowMode.BIG
                FilesShowMode.SMALL.ordinal -> FilesShowMode.SMALL
                FilesShowMode.DIR_BIG.ordinal -> FilesShowMode.DIR_BIG
                FilesShowMode.DIR_SMALL.ordinal -> FilesShowMode.DIR_SMALL
                else -> FilesShowMode.DIR_BIG
            }
            actualDirectory = savedInstanceState.getString("directory") ?: "Default"
            val note = Note()
            note.id = -1
            viewModel.selectedNote.value = note
        }
        setLiveData()
        setUI()
        if(showMode == FilesShowMode.SMALL || showMode == FilesShowMode.BIG)
            changeToFiles(actualDirectory)
        //loadData()
    }


    private fun loadData(){
        Log.d("RESUME","RESUME")
        val dao = DbDigitalPhotoEditor.getInstance(requireContext()).digitalPhotoEditorDAO()
        CoroutineScope(Dispatchers.IO).launch{
            val results = dao.loadDirectories()
            val listDirectory = mutableListOf<Directory>()
            var size = 0
            var lastModify: Long
            for(res in results){
                size = dao.loadDirectorySize(res)
                lastModify = dao.getLastModifyDir(res)
                listDirectory.add(Directory(res,size, Date(lastModify)))
            }
            CoroutineScope(Dispatchers.Main).launch {
                viewModel.listDirectory.value = listDirectory
                binding.textView7.isVisible = listDirectory.isEmpty()
            }
        }
    }
    override fun onResume(){
        super.onResume()
        setUI()
        loadData()
        viewModel.refresh()
    }
    private fun setAdapterDirectory(listDirectory: List<Directory>){
        binding.rvFiles.invalidate()
        Log.d("DIR_SIZE",listDirectory.size.toString())
        if(showMode == FilesShowMode.DIR_BIG){
            adapterDirectoryBig = DirectoryBigAdapter(listDirectory, requireContext(),this,spanCount)
            binding.rvFiles.adapter = adapterDirectoryBig
        }
        else if (showMode == FilesShowMode.DIR_SMALL){
            adapterDirectorySmall = DirectorySmallAdapter(listDirectory,requireContext(),this)
            binding.rvFiles.adapter = adapterDirectorySmall
        }
    }
    private fun setAdapterFiles(listFiles: List<Note>){
        if(showMode == FilesShowMode.BIG){
            adapter = AllFilesBigAdapter(listFiles,requireContext(),this)
            binding.rvFiles.adapter=adapter
        }
        else{
            adapterSmall = AllFilesSmallAdapter(listFiles,requireContext(),this,this)
            binding.rvFiles.adapter=adapterSmall
        }
    }
    private fun setLiveData(){
        val directoryObserver = Observer<List<Directory>>(){
            setUI()
            setAdapterDirectory(it)
        }
        val listObserver = Observer<List<Note>>(){
            setUI()
            setAdapterFiles(it)
        }
        viewModel.listDirectory.observe(requireActivity(),directoryObserver)
        viewModel.listNotes.observe(requireActivity(),listObserver)
    }
    fun changeToFiles(directory: String){
        CoroutineScope(Dispatchers.IO).launch{
            val files = DbDigitalPhotoEditor.getInstance(requireContext()).digitalPhotoEditorDAO().loadAllByDirectory(directory)
            CoroutineScope(Dispatchers.Main).launch{
                if(showMode == FilesShowMode.DIR_BIG)
                    showMode = FilesShowMode.BIG
                else if(showMode == FilesShowMode.DIR_SMALL)
                    showMode = FilesShowMode.SMALL
                setUI()
                actualDirectory = directory
                viewModel.listNotes.value=files.toMutableList()
                viewModel.sort(sortingType)
                selectedItem = ArrayList()
                mActionMode = null
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("showMode",showMode.ordinal)
        outState.putString("directory",actualDirectory)
        super.onSaveInstanceState(outState)
    }
    private fun setUI(){
        val note = Note()
        note.id = -1
        viewModel.selectedNote.value = note
        binding.rvFiles.invalidate()
        binding.rvFiles.invalidateItemDecorations()
        binding.rvFiles.adapter = null
        binding.rvFiles.layoutManager = null
        val layoutManager: RecyclerView.LayoutManager
        for(i in 0 until binding.rvFiles.itemDecorationCount){
            binding.rvFiles.removeItemDecorationAt(i)
        }
        val orientation = requireContext().resources.configuration.orientation
        if(orientation == Configuration.ORIENTATION_LANDSCAPE && showMode == FilesShowMode.SMALL){
            binding.previewLayout!!.isVisible = true
            binding.guidelineAllFiles!!.setGuidelinePercent(0.4f)
           childFragmentManager.beginTransaction()
                .replace(binding.previewLayout!!.id, FragmentNoteDetails(), DETAILS_FRAGMENT_TAG )
                .commit()
        }
        else{
            if(orientation == Configuration.ORIENTATION_LANDSCAPE){
                binding.previewLayout!!.isVisible = false
                binding.guidelineAllFiles!!.setGuidelinePercent(1f)
            }
        }
        when(showMode) {
            FilesShowMode.BIG -> {
                spanCount = if(orientation == Configuration.ORIENTATION_LANDSCAPE)
                    4
                else
                    2
                layoutManager =
                    StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL)
                (layoutManager as StaggeredGridLayoutManager).gapStrategy=StaggeredGridLayoutManager.GAP_HANDLING_NONE
            }
            FilesShowMode.DIR_BIG -> {
                spanCount =  if(orientation == Configuration.ORIENTATION_LANDSCAPE)
                    5
                else
                    3
                layoutManager =
                    StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL)
                (layoutManager as StaggeredGridLayoutManager).gapStrategy=StaggeredGridLayoutManager.GAP_HANDLING_NONE
            }
            FilesShowMode.DIR_SMALL -> {
                spanCount = 1
                layoutManager = LinearLayoutManager(requireContext())
            }
            FilesShowMode.SMALL -> {
                spanCount = 1
                layoutManager = LinearLayoutManager(requireContext())
            }
        }
                    binding.rvFiles.layoutManager = layoutManager
                    if(showMode==FilesShowMode.DIR_BIG || showMode == FilesShowMode.BIG) {
                        if (binding.rvFiles.itemDecorationCount == 0) {
                            binding.rvFiles.addItemDecoration(
                                GridSpacingDecorator(
                                    resources.getDimensionPixelSize(R.dimen.cardView_margin),
                                    spanCount
                                )
                            )
                        }
                    }
        else{
            if(binding.rvFiles.itemDecorationCount == 0){
                binding.rvFiles.addItemDecoration(
                    LinearSpacingDecorator(resources.getDimensionPixelSize(R.dimen.cardView_margin),
                    spanCount)
                )
            }
                    }

        if(showMode == FilesShowMode.DIR_BIG || showMode == FilesShowMode.DIR_SMALL){
            binding.imageButton.visibility=View.GONE
        }
        else{
            if(viewModel.listNotes.value != null)
            setAdapterFiles(viewModel.listNotes.value!!)
            binding.imageButton.visibility=View.VISIBLE
        }
        binding.imageButton.setOnClickListener{
            showMode = if(showMode == FilesShowMode.BIG)
                FilesShowMode.DIR_BIG
            else
                FilesShowMode.DIR_SMALL
            mActionMode = null
            selectedItem = ArrayList()
            setUI()
            loadData()
        }
        binding.sv.setOnQueryTextListener(MyQueryListener())

    }
    private inner class MyQueryListener(): SearchView.OnQueryTextListener{
        override fun onQueryTextSubmit(query: String?): Boolean {
            return false
        }
        override fun onQueryTextChange(newText: String?): Boolean {
            if(showMode == FilesShowMode.DIR_BIG || showMode == FilesShowMode.DIR_SMALL)
                viewModel.filterDirectory(newText)
            else
                viewModel.filter(newText,filterMode,actualDirectory)
            return false
        }
    }
    companion object {
        const val DETAILS_FRAGMENT_TAG = "details"
        @JvmStatic
        fun newInstance() =
            FragmentAllFiles()
    }

    override fun selectedHandler(element: Any): Boolean {
        var isFirst = false
        if(selectedItem.isEmpty()){
            mActionMode = requireActivity().startActionMode(AllFilesActionModeCallback())
            isFirst = true
        }
        if(selectedItem.contains(element)) {
            selectedItem.remove(element)
            if(selectedItem.size==0)
                mActionMode!!.finish()
            return false
        }
        selectedItem.add(element)
        if(isFirst) {
            val note = Note()
            note.id = -1
            viewModel.selectedNote.value = note
            //Invoke reset only if the fragment exists
            (if (childFragmentManager.findFragmentByTag(DETAILS_FRAGMENT_TAG) == null) null
            else
                (childFragmentManager.findFragmentByTag(DETAILS_FRAGMENT_TAG) as FragmentNoteDetails))?.reset()
        }
        return true
    }
    fun isSelected(note: Note): Boolean{
        for(any in selectedItem){
            if(any is Note){
                if(any.id == note.id)
                    return true
            }
        }
        return false
    }
    override fun isAnItemSelected(): Boolean {
        return selectedItem.isNotEmpty()
    }
    private inner class AllFilesActionModeCallback() : ActionMode.Callback{
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            if(showMode==FilesShowMode.DIR_BIG || showMode == FilesShowMode.DIR_SMALL)
                mode!!.menuInflater.inflate(R.menu.menu_onlong_digital,menu)
            else
                mode!!.menuInflater.inflate(R.menu.menu_onlong_files,menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            if(showMode==FilesShowMode.BIG || showMode == FilesShowMode.SMALL){
                //Files case
                when(item!!.itemId){
                    R.id.it_files_delete -> deleteFiles(mode)
                    R.id.it_files_merge -> mergeFiles(mode)
                    else -> deleteFiles(mode)
                }
            }
            else{
                //Directory case
                deleteDirectories(mode)
            }
            return true
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            selectedItem = ArrayList()
            mActionMode=null
        }
        private fun deleteDirectories(mode: ActionMode?){
            val alertDialog = AlertDialog.Builder(requireContext())
            alertDialog.setTitle(R.string.delete_directories_alert_title)
            alertDialog.setMessage(R.string.delete_directories_alert_message)
            alertDialog.setPositiveButton(R.string.yes){ dialogInterface: DialogInterface, i: Int ->
                CoroutineScope(Dispatchers.IO).launch{
                    val dao = DbDigitalPhotoEditor.getInstance(requireContext()).digitalPhotoEditorDAO()
                    val listDir = ArrayList<Directory>()
                    for(elem in selectedItem){
                        if(elem is Directory){
                            dao.deleteDirectory(elem.name)
                            listDir.add(elem)
                        }
                    }
                    CoroutineScope(Dispatchers.Main).launch{
                        val newList = viewModel.listDirectory.value!!
                        newList.removeAll(listDir)
                        viewModel.listDirectory.value = newList
                        dialogInterface.dismiss()
                        mode!!.finish()
                    }
                }
            }
            alertDialog.setNegativeButton(R.string.no){ dialogInterface: DialogInterface, _: Int ->
                dialogInterface.dismiss()
                mode!!.finish()
            }
            alertDialog.show()
        }
        private fun deleteFiles(mode: ActionMode?){
            val alertDialog = AlertDialog.Builder(requireContext())
            alertDialog.setTitle(R.string.delete_files_alert_title)
            alertDialog.setTitle(R.string.delete_files_alert_message)
            alertDialog.setPositiveButton(R.string.yes){ dialogInterface: DialogInterface, _: Int ->
                val dao = DbDigitalPhotoEditor.getInstance(requireContext()).digitalPhotoEditorDAO()
                CoroutineScope(Dispatchers.IO).launch{
                    val listNote = ArrayList<Note>()
                    for(elem in selectedItem){
                        if(elem is Note)
                            listNote.add(elem)
                    }
                    dao.deleteAll(listNote)
                    CoroutineScope(Dispatchers.Main).launch{
                        val list = viewModel.listNotes.value!!
                        list.removeAll(listNote)
                        viewModel.listNotes.value = list
                        dialogInterface.dismiss()
                        mode!!.finish()
                    }
                }
            }
            alertDialog.setNegativeButton(R.string.no){ dialogInterface: DialogInterface, _: Int ->
                dialogInterface.dismiss()
                mode!!.finish()
            }
            alertDialog.show()
        }
        private fun mergeFiles(mode: ActionMode?){
            val stringBuilder = StringBuilder()
            for(elem in selectedItem){
                if(elem is Note){
                    stringBuilder.append(elem.text)
                    stringBuilder.append("\n")
                }
            }
            val note = Note(stringBuilder.toString(),"","","und",System.currentTimeMillis(),false)
            val intent = Intent(requireContext(),TextResultActivity::class.java)
            intent.putExtra("result",note)
            intent.putExtra("type", TextResultType.NOT_SAVED.ordinal)
            requireContext().startActivity(intent)
            mode!!.finish()
        }
    }
}