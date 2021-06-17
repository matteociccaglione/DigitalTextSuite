package it.trentabitplus.digitaltextsuite.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import it.trentabitplus.digitaltextsuite.databinding.ActivityTutorialBinding

class TutorialActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTutorialBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTutorialBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}