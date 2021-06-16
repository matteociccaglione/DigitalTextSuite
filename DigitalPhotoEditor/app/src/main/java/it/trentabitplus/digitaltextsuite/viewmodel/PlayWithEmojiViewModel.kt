package it.trentabitplus.digitaltextsuite.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel

class PlayWithEmojiViewModel(application: Application): AndroidViewModel(application) {
    var attempts = 3
    var actualScore = 0
    var emoji = ""
}