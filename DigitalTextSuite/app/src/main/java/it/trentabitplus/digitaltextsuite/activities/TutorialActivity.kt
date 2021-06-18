package it.trentabitplus.digitaltextsuite.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayoutMediator
import it.trentabitplus.digitaltextsuite.R
import it.trentabitplus.digitaltextsuite.databinding.ActivityTutorialBinding

class TutorialActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTutorialBinding
    private var listFragment = listOf(R.layout.all_files_frag_tutorial,R.layout.whiteboard_frag_tutorial
        ,R.layout.whiteboard_activity_tutorial,R.layout.translate_frag_tutorial,R.layout.digital_recognize_frag_tutorial
        ,R.layout.text_result_activity_tutorial)

    private var pageChangeCallback:
            ViewPager2.OnPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            when (position) {
                listFragment.size - 1 -> {
                    binding.btnSkip.text = getString(R.string.got_it)
                }
                0 -> {
                    binding.ibPrevTutorial.isVisible = false
                }
                else -> {
                    binding.btnSkip.text = getString(R.string.btn_skip)
                    binding.ibPrevTutorial.isVisible = true
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTutorialBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }
    private fun launchApp(){
        val intent = Intent(this,RealMainActivity::class.java)
        startActivity(intent)
        finish()
    }
    private fun init(){
        binding.ibPrevTutorial.isVisible = false
        binding.viewPager.adapter = TutorialAdapter()
        binding.viewPager.registerOnPageChangeCallback(pageChangeCallback)

        binding.ibNextTutorial.setOnClickListener{
            val current = binding.viewPager.currentItem+1
            if(current<listFragment.size){
                binding.viewPager.currentItem = current
            }
            else{
                launchApp()
            }
        }

        binding.ibPrevTutorial.setOnClickListener{
            val current = binding.viewPager.currentItem-1
            if(current >= 0){
                binding.viewPager.currentItem = current

            }
        }

        binding.btnSkip.setOnClickListener{
            launchApp()
        }

        // setting the tab layout with viewPager to show dots
        val tabLayoutMediator = TabLayoutMediator(binding.tabDots, binding.viewPager
        ) { _, _ ->

        }
        tabLayoutMediator.attach()
    }

    inner class TutorialAdapter: RecyclerView.Adapter<TutorialAdapter.ViewHolder>(){

        inner class ViewHolder(val view: View): RecyclerView.ViewHolder(view){
            init{
                val cl = view as ConstraintLayout
                for (item in cl.children){
                    val img = item as ImageView
                    img.layoutParams.width = binding.viewPager.width
                    img.layoutParams.height = binding.viewPager.height
                    Glide.with(this@TutorialActivity).load(img.drawable).into(img)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view : View = LayoutInflater.from(parent.context).inflate(viewType,parent,false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        }

        override fun getItemViewType(position: Int): Int {
            return listFragment[position]
        }
        override fun getItemCount(): Int {
            return listFragment.size
        }
    }

}