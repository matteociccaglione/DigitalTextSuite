package it.trentabitplus.digitaltextsuite.fragment

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.CompoundButton
import android.widget.SearchView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import it.trentabitplus.digitaltextsuite.R
import it.trentabitplus.digitaltextsuite.adapter.DigitalInkAdapter
import it.trentabitplus.digitaltextsuite.database.DbDigitalPhotoEditor
import it.trentabitplus.digitaltextsuite.database.DigitalizedWhiteboards
import it.trentabitplus.digitaltextsuite.databinding.CheckboxBinding
import it.trentabitplus.digitaltextsuite.databinding.FragmentDigitalInkBinding
import it.trentabitplus.digitaltextsuite.decorator.GridSpacingDecorator
import it.trentabitplus.digitaltextsuite.interfaces.SelectedHandler
import it.trentabitplus.digitaltextsuite.utils.digitalink.ModelManager
import it.trentabitplus.digitaltextsuite.viewmodel.DigitalInkViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.*


class DigitalInkFragment : Fragment(),SelectedHandler {
    private var spanCount = 3
    private var mActionMode : ActionMode? = null
    private lateinit var adapter: DigitalInkAdapter
    private lateinit var binding : FragmentDigitalInkBinding
    private val viewModel : DigitalInkViewModel by viewModels()
    private var selectedWhiteboard = ArrayList<DigitalizedWhiteboards>()
    private var defaultSize = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentDigitalInkBinding.inflate(inflater)
        return binding.root
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        for(it in menu.children){
            it.isVisible = false
        }
        super.onCreateOptionsMenu(menu, inflater)
    }
    override fun onResume(){
        super.onResume()
        Log.d("FRAGDEBUG","ONRESUMEDIGITALINK")
        setUI()
        setLiveData()
    }

    private fun setLiveData(){
        val observer = Observer<List<DigitalizedWhiteboards>>{
            try {
                adapter = DigitalInkAdapter(requireContext(), it,defaultSize,this)
                binding.rvWhiteboards.adapter = adapter
            }catch(e: Exception){

            }
        }
        try {
            viewModel.listWhiteboards.observe(requireActivity(), observer)
            viewModel.loadAll()
        }catch(e: Exception){

        }
    }
    //Return true if an item is selected, false otherwise
    override fun isAnItemSelected(): Boolean{
        return selectedWhiteboard.isNotEmpty()
    }
    //Return true if the object is selected
    override fun selectedHandler(element: Any): Boolean{
        if(!(element is DigitalizedWhiteboards))
            return false
        val whiteboard = element as DigitalizedWhiteboards
        if(selectedWhiteboard.isEmpty()){
            mActionMode = requireActivity().startActionMode(MyActionModeCallback())
        }
        if(selectedWhiteboard.contains(whiteboard)) {
            selectedWhiteboard.remove(whiteboard)
            if(selectedWhiteboard.size==0)
                mActionMode!!.finish()
            return false
        }
        selectedWhiteboard.add(whiteboard)
        return true
    }
    private fun setUI(){
        val conf = requireContext().resources?.configuration
        val dm = requireContext().resources?.displayMetrics
        val orientation = requireContext().resources.configuration.orientation
        binding.rvWhiteboards.layoutManager = null
        binding.rvWhiteboards.adapter = null
        spanCount = 3
        if(conf!!.screenHeightDp<conf.screenWidthDp)
            //Landscape mode
            spanCount=5
        defaultSize = (conf.screenWidthDp * dm!!.density / spanCount).toInt()
        try {
            binding.rvWhiteboards.layoutManager = GridLayoutManager(requireContext(), spanCount)
            if(binding.rvWhiteboards.itemDecorationCount==0) {
                binding.rvWhiteboards.addItemDecoration(
                    GridSpacingDecorator(
                        resources.getDimensionPixelSize(
                            R.dimen.cardView_margin
                        ), spanCount
                    )
                )
            }
        }catch(e: Exception){

        }
        binding.svWhiteboards.setOnQueryTextListener(MySearchListener())
        binding.svWhiteboards.setQuery("",true)
    }
    companion object {
        @JvmStatic
        fun newInstance() =
            DigitalInkFragment()
    }
    private inner class MySearchListener(): SearchView.OnQueryTextListener{
        override fun onQueryTextSubmit(query: String?): Boolean {
            return false
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            viewModel.filter(newText)
            return true
        }

    }
    inner class MyActionModeCallback(): ActionMode.Callback{
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            mode!!.menuInflater.inflate(R.menu.menu_onlong_digital,menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
           return false
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            val alertDialog = AlertDialog.Builder(requireContext())
            val binding = CheckboxBinding.inflate(layoutInflater)
            val checkbox = binding.checkBox
            checkbox.setText(R.string.delete_also_model)
            var alsoModel = false
            checkbox.setOnCheckedChangeListener{ compoundButton: CompoundButton, b: Boolean ->
                alsoModel = b
            }
            binding.root.removeView(checkbox)
            alertDialog.setView(checkbox)
            alertDialog.setTitle(R.string.delete_whiteboards_alert_title)
            alertDialog.setMessage(R.string.delete_whiteboards_alert_message)
            alertDialog.setPositiveButton(R.string.yes) { dialogInterface: DialogInterface, _: Int ->
                CoroutineScope(Dispatchers.IO).launch {
                    val dao =
                        DbDigitalPhotoEditor.getInstance(requireContext()).digitalPhotoEditorDAO()
                    for (whiteboard in selectedWhiteboard) {
                        dao.deleteWhiteBoards(whiteboard)
                        File(whiteboard.imgPath).delete()
                        File(whiteboard.path).delete()
                        if(alsoModel && whiteboard.idNote!=null){
                            val note = dao.loadNoteById(whiteboard.idNote!!)
                            val manager = ModelManager()
                            manager.setModel(note.language)
                            manager.deleteActiveModel()
                        }
                    }
                    CoroutineScope(Dispatchers.Main).launch {
                        viewModel.listWhiteboards.value!!.removeAll(selectedWhiteboard)
                        adapter = DigitalInkAdapter(requireContext(),viewModel.listWhiteboards.value!!,defaultSize,this@DigitalInkFragment)
                        this@DigitalInkFragment.binding.rvWhiteboards.adapter = adapter
                        mode!!.finish()
                    }
                }
                dialogInterface.dismiss()
            }
            alertDialog.setNegativeButton(R.string.canc){ dialogInterface: DialogInterface, _: Int ->
                dialogInterface.dismiss()
            }
            alertDialog.show()
            return true
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            selectedWhiteboard = ArrayList()
            mActionMode = null
        }

    }
}