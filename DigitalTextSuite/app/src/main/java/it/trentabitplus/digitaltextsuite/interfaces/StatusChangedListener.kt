package it.trentabitplus.digitaltextsuite.interfaces

import it.trentabitplus.digitaltextsuite.enumeration.DigitalInkState

/**
 * This interface must be implemented by a class that want to handle digital ink manager status changing
 * @author Matteo Ciccaglione
 */
interface StatusChangedListener {
    /** This method is called when the recognized content changes.  */
    fun onStatusChanged(status: DigitalInkState)
}