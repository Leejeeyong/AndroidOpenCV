package com.example.leejy.examples001

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import org.opencv.android.BaseLoaderCallback

import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame

import org.opencv.android.LoaderCallbackInterface

import org.opencv.android.OpenCVLoader

import org.opencv.core.Mat

import org.opencv.android.CameraBridgeViewBase

import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2

import android.view.MenuItem
import android.view.SurfaceView

import android.view.WindowManager

class MainActivity : AppCompatActivity(), CvCameraViewListener2 {

    private var mOpenCvCameraView: CameraBridgeViewBase? = null
    private val mIsJavaCamera = true
    private val mItemSwitchCamera: MenuItem? = null

    private val mLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                LoaderCallbackInterface.SUCCESS -> {
                    Log.i(TAG1, "OpenCV loaded successfully")
                    mOpenCvCameraView!!.enableView()
                }
                else -> {
                    super.onManagerConnected(status)
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG1, "called onCreate")
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContentView(R.layout.activity_main)

        mOpenCvCameraView = findViewById(R.id.tutorial1_activity_java_surface_view) as CameraBridgeViewBase

        mOpenCvCameraView!!.visibility = SurfaceView.VISIBLE

        mOpenCvCameraView!!.setCvCameraViewListener(this)
    }

    public override fun onPause() {
        super.onPause()
        if (mOpenCvCameraView != null)
            mOpenCvCameraView!!.disableView()
    }

    public override fun onResume() {
        super.onResume()
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG1, "Internal OpenCV library not found. Using OpenCV Manager for initialization")
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback)
        } else {
            Log.d(TAG1, "OpenCV library found inside package. Using it!")
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
        if (mOpenCvCameraView != null)
            mOpenCvCameraView!!.disableView()
    }


    override fun onCameraFrame(inputFrame: CvCameraViewFrame): Mat {
        return inputFrame.rgba()
    }

    override fun onCameraViewStarted(width: Int, height: Int) {

    }

    override fun onCameraViewStopped() {

    }

    companion object {

        private val TAG = "MainActivity"


        init {
            if (!OpenCVLoader.initDebug()) {
                Log.d(TAG, "OpenCV not loaded")
            } else {
                Log.d(TAG, "OpenCV loaded")
            }
        }


        private val TAG1 = "OCVSample::Activity"
    }
}