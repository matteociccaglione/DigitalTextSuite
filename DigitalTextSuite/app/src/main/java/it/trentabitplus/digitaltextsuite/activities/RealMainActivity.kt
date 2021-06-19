package it.trentabitplus.digitaltextsuite.activities

import android.Manifest
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.PersistableBundle
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import it.trentabitplus.digitaltextsuite.R
import it.trentabitplus.digitaltextsuite.databinding.ActivityRealMainBinding
import it.trentabitplus.digitaltextsuite.fragment.*
import it.trentabitplus.digitaltextsuite.utils.RecognizerUtil
import java.io.File
import kotlin.properties.Delegates

class RealMainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRealMainBinding
    private val fragmentNumber = 5
    private lateinit var menu: Menu
    private var widget_metadata by Delegates.notNull<Int>()

    companion object{
        private const val REQUEST_CODE_PERMISSIONS = 30
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.INTERNET)
        lateinit var rootDir : File
        lateinit var pdfDir : File
        lateinit var pdfDefaultDir : File
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRealMainBinding.inflate(layoutInflater)
        widget_metadata = intent.getIntExtra("fragment",-1)
        setContentView(binding.root)
        if(allPermissionsGranted()){
            init()
        }else{
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }



    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_all_files,menu)
        return super.onCreateOptionsMenu(menu)
    }
    private fun loadImageIntent(){
        binding.pbRec.visibility = View.VISIBLE
        binding.tvRec.visibility = View.VISIBLE
        val fileUri = intent.getParcelableExtra<Uri>("image")
        if(fileUri!=null){
            RecognizerUtil(this).recognize(fileUri,false)
        }
        else{
            val listUri = intent.getParcelableArrayListExtra<Uri>("images")
            if(listUri!=null){
                RecognizerUtil(this).recognizeAll(listUri,false)
            }
        }
        binding.pbRec.visibility = View.GONE
        binding.tvRec.visibility = View.GONE
    }
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS){
            if (allPermissionsGranted()){
                init()
            }else{
                Toast.makeText(this, resources.getString(R.string.permission_not_granted), Toast.LENGTH_SHORT)
                    .show()
                this.finish()
            }
        }
    }
    private var pageChangeCallback:
            ViewPager2.OnPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            requestedOrientation = if(position==2 || position==3){
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            } else{
                ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
            }
        }
    }
    override fun onResume() {
        super.onResume()

        if (widget_metadata!=-1)
            binding.viewPagerMain.currentItem = widget_metadata
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    private fun init(){
        binding.pbRec.visibility = View.GONE
        binding.tvRec.visibility = View.GONE
        binding.viewPagerMain.isUserInputEnabled=false
        binding.viewPagerMain.adapter = ViewPagerAdapter(this)
        binding.viewPagerMain.registerOnPageChangeCallback(pageChangeCallback)
        val icons = arrayOf(R.drawable.database,R.drawable.square_edit_outline,R.drawable.ic_baseline_translate_24,R.drawable.text_recognition,R.drawable.favourite_icon_24)
        TabLayoutMediator(
            binding.tabLayout, binding.viewPagerMain
        ) { tab: TabLayout.Tab, position: Int ->
            tab.icon =
                ContextCompat.getDrawable(this@RealMainActivity,
                    icons[position])
        }.attach()

        if(widget_metadata != -1)
            binding.viewPagerMain.currentItem = widget_metadata

        loadImageIntent()
        rootDir = getOutputDirectory()
        pdfDir = File(rootDir, getString(R.string.pdf_dir)).apply { mkdir() }
        pdfDefaultDir = File(pdfDir, getString(R.string.default_dir)).apply { mkdir() }
    }
    private inner class ViewPagerAdapter(fragmentActivity: FragmentActivity): FragmentStateAdapter(fragmentActivity){
        override fun getItemCount(): Int {
            return fragmentNumber
        }
        override fun createFragment(position: Int): Fragment {
           val frag =when(position){
               0 -> FragmentAllFiles()
               1 -> DigitalInkFragment()
               2 -> FragmentCameraTranslate()
               3 ->  FragmentCameraDigitalize()
               else -> FavouriteFragment()
           }
         //   binding.viewPagerMain.isUserInputEnabled = frag !is DigitalInkFragment
            return frag
        }

    }
}