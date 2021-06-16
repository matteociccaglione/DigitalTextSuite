package it.trentabitplus.digitaltextsuite.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import it.trentabitplus.digitaltextsuite.databinding.ActivityMainBinding
import it.trentabitplus.digitaltextsuite.notifications.NotificationBuilder
import java.io.File


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private final val SPLASH_SCREEN_DURATION = 1500L
    companion object{
        const val CHANNEL_ID = "it.trentabitplus.digitaltextsuite"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val sharedPreferences = getPreferences(Context.MODE_PRIVATE)
        val edit = sharedPreferences.edit()
        if(!sharedPreferences.getBoolean("alarm_manager_set",false)){
            NotificationBuilder(this).setNotificationOn()
            edit.putBoolean("alarm_manager_set",true)
            edit.apply()
        }
        val mainDir =
            File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "digitaltextsuite")
        if (!mainDir.exists()) {
            mainDir.mkdir()
        }
        val wbDir = File(
            getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            "digitaltextsuite" + File.separator + "whiteboards"
        )
        if (!wbDir.exists()) {
            wbDir.mkdir()
        }
        val imageUri = intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri
        if (imageUri != null) {
            Log.d("TESTINTENTENTRY", "HERE")
            val intent = Intent(this, RealMainActivity::class.java)
            intent.putExtra("image", imageUri)
            startActivity(intent)
        } else {
            val listMultiple = intent.getParcelableArrayListExtra<Parcelable>(Intent.EXTRA_STREAM)
            if(listMultiple!=null) {
                val listUri = listMultiple.filterIsInstance<Uri>()
                val arrayList = ArrayList<Uri>()
                for(uri in listUri){
                    arrayList.add(uri)
                }
                val intent = Intent(this,RealMainActivity::class.java)
                intent.putParcelableArrayListExtra("images",arrayList)
                startActivity(intent)
            }
            // we used the postDelayed(Runnable, time) method
            // to send a message with a delayed time.
            else {
                Handler(Looper.getMainLooper()).postDelayed({
                    val intent = Intent(this, RealMainActivity::class.java)
                    startActivity(intent)
                    finish()
                }, SPLASH_SCREEN_DURATION)
            }
        }
    }

}