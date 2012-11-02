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
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.MediaRecorder.AudioSource;
import android.media.audiofx.Visualizer;

/*
 * This is probably mostly going away, in favor of drawing data from audio.
 * Let's keep the histgoram stuff around for the future (even though it will
 * change to HSV space).
 */

class MagicOverlay extends View {

  // Member vars
  Bitmap mBitmap;
  Paint mPaintBlack;
  Paint mPaintYellow;
  Paint mPaintRed;
  Paint mPaintGreen;
  Paint mPaintBlue;
  int mImageWidth, mImageHeight;

  // Constructor
  public MagicOverlay(Context context, Visualizer visualizer) {
    super(context);
        
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

  @Override
  protected void onDraw(Canvas canvas) {
    // Draw if we have a bitmap to draw on
    //if (mBitmap != null) {
      int canvasWidth = canvas.getWidth();
      int canvasHeight = canvas.getHeight();
      int newImageWidth = canvasWidth;
      int newImageHeight = canvasHeight;
      int marginWidth = (canvasWidth - newImageWidth)/2;

      // Draw a string
      String imageMeanStr = "Hello World!";
      canvas.drawText(imageMeanStr, marginWidth+10-1, 30-1, mPaintRed);
      canvas.drawText(imageMeanStr, marginWidth+10+1, 30-1, mPaintGreen);
      canvas.drawText(imageMeanStr, marginWidth+10+1, 30+1, mPaintBlue);
      canvas.drawText(imageMeanStr, marginWidth+10-1, 30+1, mPaintBlack);
      canvas.drawText(imageMeanStr, marginWidth+10, 30, mPaintYellow);
      //}

    // Draw our parent
    super.onDraw(canvas);
  }

}