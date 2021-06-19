package it.trentabitplus.digitaltextsuite.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import it.trentabitplus.digitaltextsuite.R
import it.trentabitplus.digitaltextsuite.activities.TextResultActivity
import it.trentabitplus.digitaltextsuite.database.DbDigitalPhotoEditor
import it.trentabitplus.digitaltextsuite.database.DigitalizedWhiteboards
import it.trentabitplus.digitaltextsuite.database.Note
import it.trentabitplus.digitaltextsuite.databinding.FragmentNoteDetailsBinding
import it.trentabitplus.digitaltextsuite.enumeration.TextResultType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class FragmentNoteDetails : Fragment() {
    private lateinit var  binding : FragmentNoteDetailsBinding
    private var note : Note? = null
    private var whiteboard = DigitalizedWhiteboards()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentNoteDetailsBinding.inflate(inflater)
        return binding.root
    }
    fun reset(){
        note = null
        setUI()
    }
    fun setNote(note: Note){
        this.note = note
        CoroutineScope(Dispatchers.IO).launch {
            val wb = DbDigitalPhotoEditor.getInstance(requireContext()).digitalPhotoEditorDAO().loadWhiteboard(note.id)
            CoroutineScope(Dispatchers.Main).launch{
                if(wb != null)
                    whiteboard = wb
                setUI()
            }
        }
    }
    private fun setUI(){
        if(note != null)
            binding.tvNoteDetails.text = note!!.text
        else
            binding.tvNoteDetails.text = ""
        binding.fbFullScreen.isVisible = binding.tvNoteDetails.text.isNotEmpty()
        binding.fbFullScreen.setOnClickListener{
            val intent = Intent(requireContext(),TextResultActivity::class.java)
            intent.putExtra("result",note)
            intent.putExtra("type",TextResultType.SAVED.ordinal)
            intent.putExtra("whiteboard",whiteboard)
            requireContext().startActivity(intent)
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setUI()
    }
    companion object {
        @JvmStatic
        fun newInstance() =
            FragmentNoteDetails().apply {
            }
    }
}