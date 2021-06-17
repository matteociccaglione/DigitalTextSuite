package it.trentabitplus.digitaltextsuite.utils

import java.util.*

/**
 * This class represents a Directory in the database
 */
data class Directory(val name: String, val count: Int, val lastModify: Date){
    constructor(): this("",0,Date(0))
}
