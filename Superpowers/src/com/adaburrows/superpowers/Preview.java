package com.adaburrows.superpowers;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;

class Preview extends SurfaceView implements SurfaceHolder.Callback {

  // Member vars
  SurfaceHolder mHolder;
  Camera mCamera;
  MagicOverlay mOverlay;
  boolean mFinished;

  // Constructor
  Preview(Context context, MagicOverlay overlay) {
    super(context);
        
    mOverlay = overlay;
    mFinished = false;

    mHolder = getHolder();
    mHolder.addCallback(this);
    mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
  }

  // When our surface is created (the application starts or becomes active)
  public void surfaceCreated(SurfaceHolder holder) {
    mCamera = Camera.open();
    try {
      mCamera.setPreviewDisplay(holder);
           
      // Annonymous preview callback (called whenever there's a preview frame)
      mCamera.setPreviewCallback(new PreviewCallback() {
        public void onPreviewFrame(byte[] data, Camera camera) {
          // Only run if we have an overlay or we're not finished.
          if ( (mOverlay == null) || mFinished )
            return;

          if (mOverlay.mBitmap == null) {
            // Set up our overlay
            Camera.Parameters params = camera.getParameters();
            mOverlay.mImageWidth = params.getPreviewSize().width;
            mOverlay.mImageHeight = params.getPreviewSize().height;
            mOverlay.mBitmap = Bitmap.createBitmap(mOverlay.mImageWidth,
              mOverlay.mImageHeight, Bitmap.Config.RGB_565);
            mOverlay.mRGBData = new int[mOverlay.mImageWidth * mOverlay.mImageHeight];
            mOverlay.mYUVData = new byte[data.length];
          }

          // Pass YUV data to our overlay
          System.arraycopy(data, 0, mOverlay.mYUVData, 0, data.length);
          mOverlay.invalidate();
        }
      });
    }
    catch (IOException exception) {
      mCamera.release();
      mCamera = null;
    }
  }

  // When we go out of sight
  public void surfaceDestroyed(SurfaceHolder holder) {
    // We share the camera; let's play nice and let others use it.
    mFinished = true;
    mCamera.setPreviewCallback(null);
    mCamera.stopPreview();
    mCamera.release();
    mCamera = null;
  }

  // On a resize
  public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
    Camera.Parameters parameters = mCamera.getParameters();
    /*
     * This needs to be changed. I'm using two obsolete API calls because it was easier.
     */
    parameters.setPreviewSize(640, 480); //obs
    parameters.setPreviewFrameRate(30); //obs
    parameters.setSceneMode(Camera.Parameters.SCENE_MODE_NIGHT);
    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
    // Use above parameters
    mCamera.setParameters(parameters);
    // Kick it off
    mCamera.startPreview();
  }

}