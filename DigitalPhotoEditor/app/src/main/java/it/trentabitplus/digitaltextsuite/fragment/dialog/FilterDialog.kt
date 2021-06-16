package it.trentabitplus.digitaltextsuite.fragment.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import it.trentabitplus.digitaltextsuite.R
import it.trentabitplus.digitaltextsuite.databinding.FilterDialogBinding
import it.trentabitplus.digitaltextsuite.enumeration.FilterMode
import it.trentabitplus.digitaltextsuite.enumeration.SortingType

class FilterDialog: DialogFragment() {
    private lateinit var binding: FilterDialogBinding
    private lateinit var sortType: SortingType
    private lateinit var filterType: FilterMode
    private lateinit var sortArray: Array<String>
    private lateinit var filterArray: Array<String>
    private var confirmationListener: (filterType: FilterMode, sortType: SortingType) -> Unit = { _: FilterMode, _: SortingType ->

    }
    private var cancelListener: () -> Boolean ={
        true
    }
    companion object{
        fun getInstance(filterMode: Int,sortingType: Int): FilterDialog{
            val instance = FilterDialog()
            val args = Bundle()
            args.putInt("filterMode",filterMode)
            args.putInt("sortingType",sortingType)
            instance.arguments=args
            return instance
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FilterDialogBinding.inflate(inflater)
        isCancelable=false
        return binding.root
    }
    private fun fromSortingTypeToString(sortingType: SortingType): String{
        return when(sortingType){
            SortingType.ALPHABETIC_ASC -> requireContext().getString(R.string.sort_by_Alphabetic)
            SortingType.ALPHABETIC_DESC -> requireContext().getString(R.string.sort_by_Alphabetic_desc)
            SortingType.BYDATE_ASC -> requireContext().getString(R.string.sort_by_date)
            else -> requireContext().getString(R.string.sort_by_date_desc)
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sortArray = requireContext().resources.getStringArray(R.array.sortingTypes)
        filterArray = requireContext().resources.getStringArray(R.array.filterTypes)
        sortType = when(arguments?.getInt("sortingType")){
            SortingType.ALPHABETIC_ASC.ordinal -> SortingType.ALPHABETIC_ASC
            SortingType.ALPHABETIC_DESC.ordinal -> SortingType.ALPHABETIC_DESC
            SortingType.BYDATE_ASC.ordinal -> SortingType.BYDATE_ASC
            else -> SortingType.BYDATE_DESC
        }
        filterType = when(arguments?.getInt("filterMode")){
            FilterMode.BY_COUNTRY.ordinal -> FilterMode.BY_COUNTRY
            else -> FilterMode.BY_TEXT
        }

        val sortingAdapter = ArrayAdapter(requireContext(), R.layout.my_spinner, sortArray)
        val filterAdapter = ArrayAdapter(requireContext(), R.layout.my_spinner, filterArray)
        sortingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.sorterSpinner.adapter = sortingAdapter
        binding.filterSpinner.adapter = filterAdapter

        binding.sorterSpinner.onItemSelectedListener=SortingTypeAdapter()
        binding.filterSpinner.onItemSelectedListener=FilteringTypeAdapter()
        binding.sorterSpinner.setSelection(sortArray.indexOf(fromSortingTypeToString(sortType)))
        binding.filterSpinner.setSelection(sortArray.indexOf(when(filterType){
            FilterMode.BY_TEXT -> requireContext().getString(R.string.filter_by_title)
            else -> requireContext().getString(R.string.filter_by_country)
        }))
        binding.buttonOkFilter.setOnClickListener{
            confirmationListener(filterType,sortType)
            dismiss()
        }
        binding.buttonCancFilter.setOnClickListener{
            if(cancelListener()){
                dismiss()
            }
        }
    }
    fun setOnFilterSelected(listener: (filterType: FilterMode,sortingType: SortingType) -> Unit){
        confirmationListener = listener
    }
    fun setOnCancel(listener: ()-> Boolean){
        cancelListener = listener
    }
    private inner class SortingTypeAdapter: AdapterView.OnItemSelectedListener{
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            sortType = when(sortArray[position]){
                requireContext().getString(R.string.sort_by_Alphabetic) -> SortingType.ALPHABETIC_ASC
                requireContext().getString(R.string.sort_by_Alphabetic_desc) -> SortingType.ALPHABETIC_DESC
                requireContext().getString(R.string.sort_by_date) -> SortingType.BYDATE_ASC
                else -> SortingType.BYDATE_DESC
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {

        }

    }
    private inner class FilteringTypeAdapter: AdapterView.OnItemSelectedListener{
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            filterType = when(filterArray[position]){
                requireContext().getString(R.string.filter_by_title) -> FilterMode.BY_TEXT
                else -> FilterMode.BY_COUNTRY
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {

        }

    }
}