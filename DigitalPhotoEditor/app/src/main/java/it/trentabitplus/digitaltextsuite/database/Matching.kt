package it.trentabitplus.digitaltextsuite.database

import androidx.room.Entity

@Entity(primaryKeys = ["primary", "secondary"])
data class Matching(var primary: Int,
                    var secondary: Int)
