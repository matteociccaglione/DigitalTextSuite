package it.trentabitplus.digitaltextsuite.fragment

import android.app.Activity
import android.os.Bundle
import android.view.*
import android.widget.SearchView
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import it.trentabitplus.digitaltextsuite.R
import it.trentabitplus.digitaltextsuite.adapter.AllFilesBigAdapter
import it.trentabitplus.digitaltextsuite.database.DbDigitalPhotoEditor
import it.trentabitplus.digitaltextsuite.database.Note
import it.trentabitplus.digitaltextsuite.databinding.FragmentFavouriteBinding
import it.trentabitplus.digitaltextsuite.decorator.GridSpacingDecorator
import it.trentabitplus.digitaltextsuite.enumeration.FilterMode
import it.trentabitplus.digitaltextsuite.enumeration.SortingType
import it.trentabitplus.digitaltextsuite.fragment.dialog.FilterDialog
import it.trentabitplus.digitaltextsuite.viewmodel.FavouriteFilesViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class FavouriteFragment : Fragment(){
    private lateinit var binding: FragmentFavouriteBinding
    private var spanCount = 2
    private lateinit var menu: Menu
    private val viewModel : FavouriteFilesViewModel by viewModels()
    private var filterMode: FilterMode = FilterMode.BY_TEXT
    private var sortingType : SortingType = SortingType.ALPHABETIC_ASC
    private lateinit var adapter: AllFilesBigAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentFavouriteBinding.inflate(inflater)
        return binding.root
    }
    override fun onResume(){
        super.onResume()
        setLiveData()
        loadData()
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setUI()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        this.menu = menu
        for(item in menu.children){
            item.isVisible = item.itemId == R.id.it_filter
        }
        super.onCreateOptionsMenu(menu, inflater)
    }
    private fun loadData(){
        val dao = DbDigitalPhotoEditor.getInstance(requireContext()).digitalPhotoEditorDAO()
        CoroutineScope(Dispatchers.IO).launch{
            val results = dao.loadPreferites()
            CoroutineScope(Dispatchers.Main).launch {
                viewModel.listFiles.value=results.toMutableList()
                binding.textView8.isVisible = results.isEmpty()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.it_filter){
            val sharedPreferences = requireActivity().getSharedPreferences("filter_all_files",
                Activity.MODE_PRIVATE)
            var filterMode = sharedPreferences.getInt("filterMode", FilterMode.BY_TEXT.ordinal)
            var sortingType = sharedPreferences.getInt("sortingType", SortingType.ALPHABETIC_ASC.ordinal)
            val dialog = FilterDialog.getInstance(filterMode,sortingType)
            dialog.setOnFilterSelected { filter, sorting ->
                filterMode = filter.ordinal
                sortingType = sorting.ordinal
                this.filterMode = filter
                this.sortingType = sorting
                viewModel.filter(binding.svFavourite.query.toString(), filter)
                viewModel.sort(sorting)
                val editor = sharedPreferences.edit()
                editor.putInt("filterMode",filterMode)
                editor.putInt("sortingType",sortingType)
                editor.apply()
            }
            dialog.show(parentFragmentManager,"FILTERDIALOG")
        }
        return super.onOptionsItemSelected(item)
    }
    private fun setLiveData(){
        val observer = Observer<List<Note>>{
            adapter = AllFilesBigAdapter(it,requireContext(),null)
            setUI()
            binding.rvFilesFavourite.adapter = adapter
        }
        viewModel.listFiles.observe(requireActivity(),observer)
    }

    private fun setUI(){
        binding.rvFilesFavourite.adapter = AllFilesBigAdapter(emptyList(),requireContext(),null)
        binding.rvFilesFavourite.layoutManager = null
        val layoutManager =
            StaggeredGridLayoutManager(spanCount, StaggeredGridLayoutManager.VERTICAL)
        if(binding.rvFilesFavourite.itemDecorationCount == 0)
            binding.rvFilesFavourite.addItemDecoration( GridSpacingDecorator(
                resources.getDimensionPixelSize(R.dimen.cardView_margin),
                spanCount
            ))
        binding.rvFilesFavourite.layoutManager=layoutManager
        binding.svFavourite.setOnQueryTextListener(MyQueryListener())
    }
    companion object {

        @JvmStatic
        fun newInstance() =
            FavouriteFragment()
    }
    private inner class MyQueryListener: SearchView.OnQueryTextListener{
        override fun onQueryTextSubmit(query: String?): Boolean {
            return false
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            viewModel.filter(newText,filterMode)
            return false
        }

    }
}