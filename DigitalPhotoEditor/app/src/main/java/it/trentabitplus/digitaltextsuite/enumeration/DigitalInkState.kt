package it.trentabitplus.digitaltextsuite.enumeration


enum class DigitalInkState {
    /**
     * Indicates that the recognition operation has finished and a result is available
     */
    REC_DONE,

    /**
     *The analyzed link is empty or a language cannot be found
     */
    EMPTY_INK,

    /**
     *Default status indicates that there are no changes since the last request
     */
    NO_CHANGES,

    /**
     * The associated model has not yet been downloaded
     */
    NO_MODEL_DOWNLOADED,

    /**
     *General error related to possible exceptions
     */
    UNKOWN_ERROR,

    /**
     * There are no models currently available for digitization
     */
    NO_MODEL_AVAILABLE,

    /**
     * The model has been correctly set
     */
    MODEL_SET,

    /**
     * The model has not been set
     */
    NO_MODEL_SET,

    /**
     * The model has been successfully removed from the device
     */
    MODEL_DELETED,

    /**
     * The model has been downloaded
     */
    MODEL_DOWNLOADED,

    /**
     * The download has started, it ends when the status will be MODEL_DOWNLOADED or any error status
     */
    DOWNLOAD_START,

    /**
     * There is no recognizer set
     */
    NO_RECOGNIZER
}