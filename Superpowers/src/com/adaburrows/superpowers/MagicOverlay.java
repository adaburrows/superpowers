package com.adaburrows.superpowers;

import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.View;

class MagicOverlay extends View {

  // Member vars
  Bitmap mBitmap;
  Paint mPaintBlack;
  Paint mPaintYellow;
  Paint mPaintRed;
  Paint mPaintGreen;
  Paint mPaintBlue;
  int mImageWidth, mImageHeight;
  int mSamplingRate;
  private byte[] mBytes;
  private float[] mPoints;
  private Rect mRect = new Rect();
  private Paint mForePaint = new Paint();

  // Constructor
  public MagicOverlay(Context context) {
    super(context);

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
  }

  public void updateData(byte[] data, int samplingRate) {
    mBytes = data;
    mSamplingRate = samplingRate;
    invalidate();
  }

  @Override
  protected void onDraw(Canvas canvas) {
    int canvasWidth = canvas.getWidth();
    int canvasHeight = canvas.getHeight();
    int newImageWidth = canvasWidth;
    int newImageHeight = canvasHeight;
    int marginWidth = (canvasWidth - newImageWidth)/2;

    // Draw if we have a bitmap to draw on
    if (mBitmap != null) {
    }

    // Draw a string
    String imageMeanStr = "" + mSamplingRate;
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

    if (mPoints == null || mPoints.length < mBytes.length * 4) {
        mPoints = new float[mBytes.length * 4];
    }

    mRect.set(0, 0, getWidth(), getHeight());

    for (int i = 0; i < mBytes.length - 1; i++) {
        mPoints[i * 4] = mRect.width() * i / (mBytes.length - 1);
        mPoints[i * 4 + 1] = mRect.height() / 2
                + ((byte) (mBytes[i] + 128)) * (mRect.height() / 2) / 128;
        mPoints[i * 4 + 2] = mRect.width() * (i + 1) / (mBytes.length - 1);
        mPoints[i * 4 + 3] = mRect.height() / 2
                + ((byte) (mBytes[i + 1] + 128)) * (mRect.height() / 2) / 128;
    }

    canvas.drawLines(mPoints, mForePaint);

    // Draw our parent
    super.onDraw(canvas);
  }

}