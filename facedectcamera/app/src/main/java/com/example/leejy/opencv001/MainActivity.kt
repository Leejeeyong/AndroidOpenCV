package com.example.leejy.opencv001

import android.hardware.Camera
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.content.Context

import org.opencv.android.*
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.CascadeClassifier

import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

import org.opencv.android.OpenCVLoader

class MainActivity : AppCompatActivity(), CvCameraViewListener {
    ////////opencv load check

    private var openCvCameraView: CameraBridgeViewBase? = null
    private var cascadeClassifier: CascadeClassifier? = null
    private var grayscaleImage: Mat? = null
    private var absoluteFaceSize: Int = 0

    private val mLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                LoaderCallbackInterface.SUCCESS -> initializeOpenCVDependencies()
                else -> super.onManagerConnected(status)
            }
        }
    }

    private fun initializeOpenCVDependencies() {
        try {
            // Copy the resource into a temp file so OpenCV can load it
            val `is` = resources.openRawResource(R.raw.lbpcascade_frontalface)
            val cascadeDir = getDir("cascade", Context.MODE_APPEND)
            val mCascadeFile = File(cascadeDir, "lbpcascade_frontalface.xml")
            val os = FileOutputStream(mCascadeFile)

            val buffer = ByteArray(4096)
            var bytesRead: Int
            while ((bytesRead = `is`.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead)
            }
            `is`.close()
            os.close()

            // Load the cascade classifier
            cascadeClassifier = CascadeClassifier(mCascadeFile.absolutePath)
        } catch (e: Exception) {
            Log.e("OpenCVActivity", "Error loading cascade", e)
        }

        // And we are ready to go
        openCvCameraView!!.enableView()
    }

    private fun findFrontSideCamera(): Int {
        var cameraId = -1

        val numberOfCameras = Camera.getNumberOfCameras()

        for (i in 0..numberOfCameras - 1) {

            val cmInfo = Camera.CameraInfo()

            Camera.getCameraInfo(i, cmInfo)

            if (cmInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {

                cameraId = i

                break

            }

        }

        return cameraId

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val cameraId = findFrontSideCamera()

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        openCvCameraView = JavaCameraView(this, cameraId)
        setContentView(openCvCameraView)
        openCvCameraView!!.setCvCameraViewListener(this)
        //setContentView(R.layout.activity_main);
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        if (id == R.id.action_settings) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCameraViewStarted(width: Int, height: Int) {
        grayscaleImage = Mat(height, width, CvType.CV_8UC4)
        // The faces will be a 20% of the height of the screen
        absoluteFaceSize = (height * 0.2).toInt()
    }

    override fun onCameraViewStopped() {

    }

    override fun onCameraFrame(aInputFrame: Mat): Mat {
        // Create a grayscale image
        Imgproc.cvtColor(aInputFrame, grayscaleImage, Imgproc.COLOR_RGBA2RGB)

        val faces = MatOfRect()

        // Use the classifier to detect faces
        if (cascadeClassifier != null) {
            cascadeClassifier!!.detectMultiScale(grayscaleImage, faces, 1.1, 2, 2,
                    Size(absoluteFaceSize.toDouble(), absoluteFaceSize.toDouble()), Size())
        }

        // If there are any faces found, draw a rectangle around it
        val facesArray = faces.toArray()
        for (i in facesArray.indices)
            Imgproc.rectangle(aInputFrame, facesArray[i].tl(), facesArray[i].br(), Scalar(0.0, 255.0, 0.0, 255.0), 3)

        return aInputFrame
    }

    public override fun onResume() {
        super.onResume()
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback)
    }

    companion object {

        private val TAG = "MainActivity"

        init {
            if (!OpenCVLoader.initDebug()) {
                Log.d(TAG, "Opencv not loaded")
            } else {
                Log.d(TAG, "Opencv loaded")
            }
        }
    }
}