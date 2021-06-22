package it.trentabitplus.digitaltextsuite.fragment

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.content.ContextCompat
import androidx.core.view.children
import com.bumptech.glide.load.ImageHeaderParser.UNKNOWN_ORIENTATION
import it.trentabitplus.digitaltextsuite.R
import it.trentabitplus.digitaltextsuite.animation.ZoomAnimationListener
import it.trentabitplus.digitaltextsuite.databinding.FragmentCameraDigitalRecBinding
import it.trentabitplus.digitaltextsuite.utils.RecognizerUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class FragmentCameraDigitalize: CameraFragment(){
    private lateinit var binding: FragmentCameraDigitalRecBinding
    private lateinit var mediaPlayer: MediaPlayer
    private var flashMode = ImageCapture.FLASH_MODE_ON
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCameraDigitalRecBinding.inflate(inflater)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onStop() {
        super.onStop()
        orientationEventListener.disable()
    }
    private val orientationEventListener by lazy {
        object : OrientationEventListener(requireContext()) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == UNKNOWN_ORIENTATION) {
                    return
                }

                val rotation = when (orientation) {
                    in 45 until 135 -> Surface.ROTATION_270
                    in 135 until 225 -> Surface.ROTATION_180
                    in 225 until 315 -> Surface.ROTATION_90
                    else -> Surface.ROTATION_0
                }
                if(imageCapture!=null)
                    imageCapture!!.targetRotation = rotation
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        for(it in menu.children){
            it.isVisible = false
        }
        super.onCreateOptionsMenu(menu, inflater)
    }
    override fun onResume(){
        super.onResume()
        Log.d("FRAGDEBUG","ONRESUMEDIGITALIZE")
        show(binding.vfCamera)
        setUI()
        orientationEventListener.enable()
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d("FRAGDEBUG","ONVIEWCREATEDDIGITALIZE")
    }
    private fun setUI(){
        mediaPlayer = MediaPlayer.create(requireContext(), R.raw.camera_shutter_click)
        //show(binding.vfCamera)
        binding.imgBtnCamera.setOnClickListener{
            takePhoto(false)
            mediaPlayer = MediaPlayer.create(requireContext(), R.raw.camera_shutter_click)
            mediaPlayer.start()
            val zoomOut = AnimationUtils.loadAnimation(requireContext(), R.anim.zoom_out)
            zoomOut.setAnimationListener(ZoomAnimationListener(binding.imgBtnCamera,requireContext()))
            binding.imgBtnCamera.startAnimation(zoomOut)
        }
        flashMode = requireActivity().getPreferences(Context.MODE_PRIVATE).getInt("flash",ImageCapture.FLASH_MODE_ON)

        setIconFlash()
        binding.imgBtnFlash.setOnClickListener{
            when(flashMode){
                ImageCapture.FLASH_MODE_ON -> {
                    flashMode = ImageCapture.FLASH_MODE_AUTO
                }
                ImageCapture.FLASH_MODE_AUTO -> {
                    flashMode = ImageCapture.FLASH_MODE_OFF
                }
                ImageCapture.FLASH_MODE_OFF -> {
                    flashMode = ImageCapture.FLASH_MODE_ON
                }
            }
            setIconFlash()
            setFlash(flashMode)
            val sharedPreferences = requireActivity().getPreferences(Context.MODE_PRIVATE)
            val edit = sharedPreferences.edit()
            edit.putInt("flash",flashMode)
            edit.apply()
        }
    }
    private fun setIconFlash(){
        binding.imgBtnFlash.setImageDrawable(when(flashMode){
            ImageCapture.FLASH_MODE_ON ->
                ContextCompat.getDrawable(requireContext(), R.drawable.flash_on_48)
            ImageCapture.FLASH_MODE_OFF -> ContextCompat.getDrawable(requireContext(), R.drawable.flash_off_48)
            else -> ContextCompat.getDrawable(requireContext(), R.drawable.flash_auto_48)
        })
    }
    override fun saveImage(fileUri: Uri, temp: Boolean) {
        RecognizerUtil(requireContext()).recognize(fileUri,true)
    }
    companion object {

        @JvmStatic
        fun newInstance() =
            FragmentCameraDigitalize()
    }
    override fun errorHandler(exception: ImageCaptureException) {
        CoroutineScope(Dispatchers.Main).launch{
            Toast.makeText(requireContext(),getString(R.string.error_camera), Toast.LENGTH_LONG).show()
        }
    }
}