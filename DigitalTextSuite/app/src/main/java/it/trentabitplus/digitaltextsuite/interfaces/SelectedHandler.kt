package it.trentabitplus.digitaltextsuite.interfaces

/**
 * This interface must be implemented by a class that wants to manage on long click events on elements
 * of an adapter of a recycler view
 * @author Matteo Ciccaglione
 */
interface SelectedHandler {
    /**
     * This method is called when an element on adapter is clicked
     * This method must return true if the element is selected and false otherwise
     * @return True if and only if the element is selected
     */
    fun selectedHandler(element: Any): Boolean

    /**
     * This method must return true if an item is selected
     */
    fun isAnItemSelected(): Boolean
}