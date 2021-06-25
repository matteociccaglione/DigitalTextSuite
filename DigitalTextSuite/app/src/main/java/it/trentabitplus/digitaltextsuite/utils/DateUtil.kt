package it.trentabitplus.digitaltextsuite.utils

import android.content.Context
import it.trentabitplus.digitaltextsuite.R
import java.util.*

/**
 * This class handles the conversion from Date to Calendar
 */
class DateUtil(val context: Context) {
    /**
     * Convert a calendar instance in a well formed string
     * @param calendar the Calendar to be converted
     * @return A well formed string in this format: day month year
     */
    private fun printDate(calendar: Calendar): String{
        val month = when(calendar.get(Calendar.MONTH)){
            Calendar.JANUARY -> context.getString(R.string.january)
            Calendar.FEBRUARY -> context.getString(R.string.february)
            Calendar.MARCH -> context.getString(R.string.march)
            Calendar.APRIL -> context.getString(R.string.april)
            Calendar.MAY -> context.getString(R.string.may)
            Calendar.JUNE -> context.getString(R.string.june)
            Calendar.JULY -> context.getString(R.string.july)
            Calendar.AUGUST -> context.getString(R.string.august)
            Calendar.SEPTEMBER -> context.getString(R.string.september)
            Calendar.OCTOBER -> context.getString(R.string.october)
            Calendar.NOVEMBER -> context.getString(R.string.november)
            else -> context.getString(R.string.december)
        }
        return String.format("%d %s %d",calendar.get(Calendar.DAY_OF_MONTH),month,calendar.get(
            Calendar.YEAR))
    }

    /**
     * Convert a date instance into a Calendar instance
     * @param date the Date to be converted
     * @return The converted Calendar instance
     */
    private fun fromDateToCalendar(date: Date): Calendar{
        val calendar = Calendar.getInstance()
        calendar.time=date
        return calendar
    }

    /**
     * Convert a date into a well formed string
     * @param date The date to be converted
     * @return A well formed string in this format: day month year
     */
    fun printDate(date: Date): String{
        return printDate(fromDateToCalendar(date))
    }
    /**
     * Convert a Long object into a well formed string
     * @param milliseconds A long instance which represents a time in milliseconds (for example System.currentTimeMillis())
     * @return A well formed string in this format: day month year
     */
    fun printDate(milliseconds: Long): String{
        return printDate(Date(milliseconds))
    }
}