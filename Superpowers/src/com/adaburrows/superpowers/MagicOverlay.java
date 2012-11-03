package com.adaburrows.superpowers;

import java.io.IOException;

import android.util.Log;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.View;

class MagicOverlay extends View {

  private static final String TAG = "MagicOverlay";

  // Member vars
  Bitmap mBitmap;
  Paint mPaintBlack;
  Paint mPaintYellow;
  Paint mPaintRed;
  Paint mPaintGreen;
  Paint mPaintBlue;
  int mImageWidth, mImageHeight;
  int mSamplingRate;
  int mCaptureSize;
  private byte[] mBytes;
  private float[] mPoints;
  private Rect mRect = new Rect();
  private Paint mForePaint = new Paint();

  // Constructor
  public MagicOverlay(Context context) {
    super(context);

    Log.i(TAG, "Entering constructor");

    mBytes = null;

    mForePaint.setStrokeWidth(1f);
    mForePaint.setAntiAlias(true);
    mForePaint.setColor(Color.rgb(0, 128, 255));

    mPaintBlack = new Paint();
    mPaintBlack.setStyle(Paint.Style.FILL);
    mPaintBlack.setColor(Color.BLACK);
    mPaintBlack.setAlpha(128);
    mPaintBlack.setTextSize(25);
        
    mPaintYellow = new Paint();
    mPaintYellow.setStyle(Paint.Style.FILL);
    mPaintYellow.setColor(Color.YELLOW);
    mPaintYellow.setAlpha(128);
    mPaintYellow.setTextSize(25);
        
    mPaintRed = new Paint();
    mPaintRed.setStyle(Paint.Style.FILL);
    mPaintRed.setColor(Color.RED);
    mPaintRed.setAlpha(128);
    mPaintRed.setTextSize(25);
        
    mPaintGreen = new Paint();
    mPaintGreen.setStyle(Paint.Style.FILL);
    mPaintGreen.setColor(Color.GREEN);
    mPaintGreen.setAlpha(128);
    mPaintGreen.setTextSize(25);
        
    mPaintBlue = new Paint();
    mPaintBlue.setStyle(Paint.Style.FILL);
    mPaintBlue.setColor(Color.BLUE);
    mPaintBlue.setAlpha(128);
    mPaintBlue.setTextSize(25);
        
    mBitmap = null;

    Log.i(TAG, "Leaving constructor");
  }

  public void updateData(byte[] data, int samplingRate, int captureSize) {
//    Log.i(TAG, "Entering updateData()");
    mBytes = data;
    mSamplingRate = samplingRate;
    mCaptureSize = captureSize;
    invalidate();
//    Log.i(TAG, "Leaving updateData()");
  }

  @Override
  protected void onDraw(Canvas canvas) {
    Log.i(TAG, "Entering onDraw()");

    int canvasWidth = canvas.getWidth();
    int canvasHeight = canvas.getHeight();
    int newImageWidth = canvasWidth;
    int newImageHeight = canvasHeight;
    int marginWidth = (canvasWidth - newImageWidth)/2;

    // Draw if we have a bitmap to draw on
    if (mBitmap != null) {
    }

    // Draw a string
    String imageMeanStr = "Superpower";
    canvas.drawText(imageMeanStr, marginWidth+10-1, 30-1, mPaintRed);
    canvas.drawText(imageMeanStr, marginWidth+10+1, 30-1, mPaintGreen);
    canvas.drawText(imageMeanStr, marginWidth+10+1, 30+1, mPaintBlue);
    canvas.drawText(imageMeanStr, marginWidth+10-1, 30+1, mPaintBlack);
    canvas.drawText(imageMeanStr, marginWidth+10, 30, mPaintYellow);


    if (mBytes == null) {
        return;
    }

    // Waveform or FFT is in mBytes (byte array), this function gets called every 
    // time there's new data.

    Log.i(TAG, "mBytes.length: " + mBytes.length);
    Log.i(TAG, "mCaptureSize, mSamplingRate: " + mCaptureSize + ", " + mSamplingRate );
    Log.i(TAG, "first twelve mBytes: " + mBytes[0] + ", " + mBytes[1] + ", " + mBytes[2] + ", " + mBytes[3] + ", " + mBytes[4] + ", " + mBytes[5] + ", " + mBytes[6] + ", " + mBytes[7] + ", " + mBytes[8] + ", " + mBytes[9] + ", " + mBytes[10] + ", " + mBytes[11] );

    if (mPoints == null || mPoints.length < mBytes.length * 4) {
        mPoints = new float[mBytes.length * 4];
    }

    mRect.set(0, 0, getWidth(), getHeight());

    Paint wallpaint = new Paint();
    Path wallpath = new Path();

    int alpha = 128; // Scale 0..255
    float hue = 180f; // Scale 0..260
    float saturation = 0.8f; // Scale 0..1
    float value = 0.4f; // Scale 0..1

    wallpaint.setColor(Color.HSVToColor(128,new float[]{hue,saturation,value}));
    wallpaint.setStyle(Style.FILL);
    wallpath.reset();

    wallpath.moveTo(0, 0);
    wallpath.lineTo(0, mRect.height());
    wallpath.lineTo(mRect.width(), mRect.height());
    wallpath.lineTo(mRect.width(), 0);
    wallpath.lineTo(0, 0); 

    canvas.drawPath(wallpath, wallpaint);

    // for (int i = 0; i < mBytes.length - 1; i++) {
    //     mPoints[i * 4] = mRect.width() * i / (mBytes.length - 1);
    //     mPoints[i * 4 + 1] = mRect.height() / 2
    //             + ((byte) (mBytes[i] + 128)) * (mRect.height() / 2) / 128;
        
    //     mPoints[i * 4 + 2] = mRect.width() * (i + 1) / (mBytes.length - 1);
    //     mPoints[i * 4 + 3] = mRect.height() / 2
    //             + ((byte) (mBytes[i + 1] + 128)) * (mRect.height() / 2) / 128;
    // }

    // canvas.drawLines(mPoints, mForePaint);

    Log.i(TAG, "Leaving onDraw()");

    // Draw our parent
    super.onDraw(canvas);
  }

}