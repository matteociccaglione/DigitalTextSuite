package it.trentabitplus.digitaltextsuite.fragment.dialog

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import it.trentabitplus.digitaltextsuite.BuildConfig
import it.trentabitplus.digitaltextsuite.R
import it.trentabitplus.digitaltextsuite.adapter.SelectPdfAdapter
import it.trentabitplus.digitaltextsuite.databinding.FragmentSelectPdfDialogBinding
import it.trentabitplus.digitaltextsuite.decorator.LinearSpacingDecorator
import java.io.File

class SelectPdfDialog(private var fileList : List<File>) : DialogFragment() {

    private lateinit var binding: FragmentSelectPdfDialogBinding

    private var cancelListener: () -> Boolean = {
        true
    }

    companion object{
        // singleton
        private var instance : SelectPdfDialog? = null
        fun getInstance(fileList: List<File>): SelectPdfDialog{
            if (instance == null)
                instance = SelectPdfDialog(fileList)
            else
                instance!!.fileList = fileList

            return instance as SelectPdfDialog
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSelectPdfDialogBinding.inflate(inflater)
        isCancelable = false
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d("PDF", "Dialog: ${fileList.size}")
        val rvAdapter = SelectPdfAdapter(fileList, requireContext())
        binding.rvPdfList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPdfList.adapter = rvAdapter
        binding.rvPdfList.addItemDecoration(
            LinearSpacingDecorator(resources.getDimensionPixelSize(R.dimen.cardView_margin),1)
        )

        binding.btnCancPdfDialog.setOnClickListener {
            if (cancelListener())
                dismiss()
        }

        binding.btnOpenPdfDialog.setOnClickListener {
            if (rvAdapter.selectedFile.value == null){
                Toast.makeText(requireContext(), getString(R.string.no_pdf_chosen), Toast.LENGTH_SHORT).show()
            }else{
                val intent = Intent(Intent.ACTION_VIEW)
                val data = FileProvider.getUriForFile(requireContext(),
                    BuildConfig.APPLICATION_ID +".provider",
                    rvAdapter.selectedFile.value!!)
                intent.setDataAndTypeAndNormalize(data, "application/pdf")
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)


                startActivity(Intent.createChooser(intent, "Open pdf with ..."))
                dismiss()
            }
        }
    }


}