package it.trentabitplus.digitaltextsuite.interfaces

interface DownloadedModelsChangedListener {
    /** This method is called when the downloaded models changes.  */
    fun onDownloadedModelsChanged(downloadedLanguageTags: Set<String>)
}