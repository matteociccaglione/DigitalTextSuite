package it.trentabitplus.digitaltextsuite.utils

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData

/**
 * A MutableLiveData class that only emits change events when
 * the underlying data has been stable for the configured amount of time.
 *
 * @param duration time delay to wait in milliseconds
 *
 * @author Andrea Pepe
 */
class SmoothedMutableLiveData<T>(private val duration: Long) : MutableLiveData<T>(){

    private var pendingValue : T? = null
    private var runnable = Runnable{
        super.setValue(pendingValue)
    }

    /**
     * Call this method to perform the update of the MutableLiveData value
     * @param value : new value to set
     *
     * @author Andrea Pepe
     */
    override fun setValue(value: T) {
        if (value != pendingValue){
            pendingValue = value
            Handler(Looper.getMainLooper()).removeCallbacks(runnable)
            // launch an event only if pendingValue has been the same for duration time
            Handler(Looper.getMainLooper()).postDelayed(runnable, duration)
        }
    }
}