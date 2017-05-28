package com.example.uberv.opencvapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class MainActivity extends AppCompatActivity
        implements JavaCameraView.CvCameraViewListener2 {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private JavaCameraView mCameraView;
    // represents a camera frame
    private Mat mRgba, mGray, mCanny;
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case BaseLoaderCallback.SUCCESS: {
                    mCameraView.enableView();
                    break;
                }
                default: {
                    super.onManagerConnected(status);
                }
            }
        }
    };

    static {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCameraView = (JavaCameraView) findViewById(R.id.java_camera_view);
        mCameraView.setCvCameraViewListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // First try loading OpenCV from application package
        if (OpenCVLoader.initDebug()) {
            Log.d(LOG_TAG, "OpenCV successfully loaded!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        } else {
            Log.d(LOG_TAG, "OpenCV loading failed, trying to load from OpenCV Manager!");
            // Could not load from app package, try to load from OpenCV Manager app
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mLoaderCallback);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mCameraView != null) {
            mCameraView.disableView();
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        Log.d(LOG_TAG, String.format("Camera view started, width=%d, height=%d", width, height));

        // prepare our frame matrix of type
        // Unsigned int 8-bit (8U)
        // Four channels (C4), for R, G, B and A
        mRgba = new Mat(width, height, CvType.CV_8UC4);

        // grayscale and canny frames need only 1 chanell (gray saturation)
        mGray = new Mat(width, height, CvType.CV_8UC1);
        mCanny = new Mat(width, height, CvType.CV_8UC1);
    }

    @Override
    public void onCameraViewStopped() {
        Log.d(LOG_TAG, "onCameraViewStopped()");

        // release resources
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        // frame capture
        mRgba = inputFrame.rgba();

        // convert to grayscale
        Imgproc.cvtColor(mRgba, mGray, Imgproc.COLOR_RGBA2GRAY);

        // apply canny detector
        Imgproc.Canny(mGray, mCanny, 50, 150);

        return mCanny;
    }
}