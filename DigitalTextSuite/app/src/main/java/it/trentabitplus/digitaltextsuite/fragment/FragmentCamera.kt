package it.trentabitplus.digitaltextsuite.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.util.Size
import android.view.*
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import it.trentabitplus.digitaltextsuite.interfaces.CaptureHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.lang.IllegalStateException
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


/**
 * This is an abstract class, performing camera features
 * using CameraX APIs. This class has to be extended.
 */
abstract class CameraFragment : Fragment(), CaptureHandler {
    protected var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    protected var imageAnalysis: ImageAnalysis? = null
    protected var lensFacing = CameraSelector.DEFAULT_BACK_CAMERA
    protected lateinit var captureHandler: CaptureHandler
    protected var imageCapture : ImageCapture? = null
    protected lateinit var preview : Preview
    protected lateinit var cameraProvider: ProcessCameraProvider
    protected var camera : Camera? = null
    private var effect = 0
    companion object {
        const val CAMERA_TAG = "CAM FRAGMENT"
        const val CAMERA_MODE_TAG = "camera_mode"
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sharedPref = requireActivity().getPreferences(Context.MODE_PRIVATE)
        val cameraPref = sharedPref.getInt(CAMERA_MODE_TAG,0)
        if (cameraPref == 0)
            lensFacing = CameraSelector.DEFAULT_BACK_CAMERA
        else
            lensFacing = CameraSelector.DEFAULT_FRONT_CAMERA
    }

    /**
     * Children classes must call this method to start the camera
     */
    fun show(vfCamera: PreviewView){
        startCamera(vfCamera)
    }

    override fun onAttach(context: Context) {
        if(context is CaptureHandler){
            captureHandler = context
        }
        else
            captureHandler = this
        super.onAttach(context)
    }

    /**
     * This method start the camera, setting the right use cases
     * @param vfCamera : PreviewView used to stream frames
     */
    @SuppressLint("UnsafeOptInUsageError")
    open fun startCamera(vfCamera: PreviewView){
            // get an instance of ProcessCameraProvider
            val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
            // add a runnable listener
            cameraProviderFuture.addListener({
                try {
                    cameraProvider = cameraProviderFuture.get()
                    val previewBuilder = Preview.Builder()
                    val imageCaptureBuilt = ImageCapture.Builder()
                    // build the Preview and bind it with the SurfaceProvider of the PreviewView
                    preview = previewBuilder
                        .build()
                        .also { it.setSurfaceProvider(vfCamera.surfaceProvider) }
                    imageCapture = imageCaptureBuilt
                        .build()

                    // create a camera selector and set the back camera as default
//            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    imageAnalysis = ImageAnalysis.Builder()
                        .setTargetResolution(Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                    setAnalyzer()
                    // bind CameraSelector and Preview to the fragment lifecycle
                    try {
                        cameraProvider.unbindAll()
                        camera = cameraProvider.bindToLifecycle(
                            this,
                            lensFacing,
                            preview,
                            imageCapture,
                            imageAnalysis
                        )
                    } catch (ex: Exception) {
                        Log.e(CAMERA_TAG, "Unable to bind to lifecycle")
                    }

                    //handle pinch to zoom
                    val scaleGestureDetector = ScaleGestureDetector(requireContext(),
                        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                            override fun onScale(detector: ScaleGestureDetector): Boolean {
                                val scale =
                                    camera!!.cameraInfo.zoomState.value!!.zoomRatio * detector.scaleFactor
                                camera!!.cameraControl.setZoomRatio(scale)
                                return true
                            }
                        })

                    vfCamera.setOnTouchListener { view, event ->
                        view.performClick()
                        scaleGestureDetector.onTouchEvent(event)
                    }
                }catch(exception: IllegalStateException){

                }
            }, ContextCompat.getMainExecutor(requireContext()))
    }
    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
    fun setFlash(mode: Int){
        imageCapture!!.flashMode=mode
    }
    fun takePhoto(save: Boolean){
        Log.d("SAVE?",save.toString())
        val outputDirectory = if(save)
        //store the image
            requireActivity().getExternalFilesDir(Environment.DIRECTORY_DCIM+File.separator+"effectscam")
        else
        //cache the image
            requireContext().cacheDir
        val filename = "Image_" + System.currentTimeMillis()
        val extension = ".jpg"
        val photoFile = if(save)
            File(outputDirectory, filename+extension)
        else
            File.createTempFile(filename,extension,outputDirectory)
        //Check camera mode and add metadata for selfie
        val outputFileOptions: ImageCapture.OutputFileOptions
        if(lensFacing== CameraSelector.DEFAULT_FRONT_CAMERA){
            val metadata = ImageCapture.Metadata()
            //Reverse image
            metadata.isReversedHorizontal=true
            outputFileOptions = ImageCapture.OutputFileOptions.Builder(photoFile).setMetadata(metadata).build()
        }
        else
            outputFileOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture!!.takePicture(outputFileOptions, cameraExecutor, object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                captureHandler.saveImage(Uri.fromFile(photoFile),!save)
            }

            override fun onError(exception: ImageCaptureException) {
                captureHandler.errorHandler(exception)
            }
        })
    }

    override fun saveImage(fileUri: Uri,temp: Boolean) {
        //Do nothing
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(requireContext(), "Unsupported operation", Toast.LENGTH_LONG).show()
        }
    }

    override fun errorHandler(exception: ImageCaptureException) {
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(requireContext(), "Unsupported operation", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Children class have to override this method to
     * set an Analyzer to ImageAnalysis use case.
     * If they don't override this method,
     * no analyzer will be set
     */
    open fun setAnalyzer() {

    }
}