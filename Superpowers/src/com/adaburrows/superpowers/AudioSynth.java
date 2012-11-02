package com.adaburrows.superpowers;

import com.adaburrows.superpowers.PlaySound;
import java.io.IOException;

import android.util.Log;
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
import android.media.AudioTrack;
import android.media.AudioManager;
import android.media.AudioFormat;


class AudioSynth implements Camera.PreviewCallback {

  // Member vars
  private static final String TAG = "AudioSynth";

  boolean mFinished;
  byte[] mYUVData;
  int[] mRGBData;
  int mImageWidth, mImageHeight;
  int[] mRedHistogram;
  int[] mGreenHistogram;
  int[] mBlueHistogram;
  double[] mBinSquared;
  AudioTrack mRedSynthesizer;
  AudioTrack mGreenSynthesizer;
  AudioTrack mBlueSynthesizer;
  private final int sampleRate = 44100;
  private double redSample[];
  private double greenSample[];
  private double blueSample[];
  private byte redGeneratedSnd[];
  private byte greenGeneratedSnd[];
  private byte blueGeneratedSnd[];


  // Constructor
  public AudioSynth(AudioTrack redSynthesizer, AudioTrack greenSynthesizer, AudioTrack blueSynthesizer) {
    mFinished = false;
    mYUVData = null;
    mRGBData = null;
    mRedSynthesizer = redSynthesizer;
    mGreenSynthesizer = greenSynthesizer;
    mBlueSynthesizer = blueSynthesizer;
    mRedHistogram = new int[256];
    mGreenHistogram = new int[256];
    mBlueHistogram = new int[256];
    mBinSquared = new double[256];
    for (int bin = 0; bin < 256; bin++)
    {
      mBinSquared[bin] = ((double)bin) * bin;
    }
  }

  @Override
  public void onPreviewFrame(byte[] data, Camera camera) {
    // Only run if we're not finished.
    if ( mFinished )
      return;

    Camera.Parameters params = camera.getParameters();
    mImageWidth = params.getPreviewSize().width;
    mImageHeight = params.getPreviewSize().height;
    mRGBData = new int[mImageWidth * mImageHeight];
    mYUVData = new byte[data.length];

    // Convert from YUV to RGB
    decodeYUV420SP(mRGBData, data, mImageWidth, mImageHeight);

    // Calculate histogram
    calculateIntensityHistogram(mRGBData, mRedHistogram, 
      mImageWidth, mImageHeight, 0);
    calculateIntensityHistogram(mRGBData, mGreenHistogram, 
      mImageWidth, mImageHeight, 1);
    calculateIntensityHistogram(mRGBData, mBlueHistogram, 
      mImageWidth, mImageHeight, 2);

    // Calculate mean
    double imageRedMean = 0, imageGreenMean = 0, imageBlueMean = 0;
    double redHistogramSum = 0, greenHistogramSum = 0, blueHistogramSum = 0;
    for (int bin = 0; bin < 256; bin++) {
      imageRedMean += mRedHistogram[bin] * bin;
      redHistogramSum += mRedHistogram[bin];
      imageGreenMean += mGreenHistogram[bin] * bin;
      greenHistogramSum += mGreenHistogram[bin];
      imageBlueMean += mBlueHistogram[bin] * bin;
      blueHistogramSum += mBlueHistogram[bin];
    }
    imageRedMean /= redHistogramSum;
    imageGreenMean /= greenHistogramSum;
    imageBlueMean /= blueHistogramSum;

    // Test of all the above, creates lots of log messages!!!
    Log.i(TAG, "Mean (R,G,B): " + String.format("%.4g", imageRedMean) + ", " + String.format("%.4g", imageGreenMean) + ", " + String.format("%.4g", imageBlueMean));

    // This is where you'll use imageRedMean, imageGreenMean, and imageBlueMean to 
    // construct a waveform based on the aggregate channel luminosities.

    double maxFreq = 5200;
    double minFreq = 3900;
    double redVolume = imageRedMean / 255;
    double redFrequency = minFreq + ((maxFreq - minFreq) * redVolume);
    double greenVolume = imageGreenMean / 255;
    double greenFrequency = minFreq + ((maxFreq - minFreq) * greenVolume);
    double blueVolume = imageBlueMean / 255;
    double blueFrequency = minFreq + ((maxFreq - minFreq) * blueVolume);
    
    genTones(redFrequency, redVolume, greenFrequency, greenVolume, blueFrequency, blueVolume);
    playSound();

  }

  void genTones(double redFrequency, double redVolume, double greenFrequency, double greenVolume, double blueFrequency, double blueVolume){

    double redPeriod = 1.0 / redFrequency;
    double redAdjustedDuration = (int)((1.0/3)/redPeriod) * redPeriod;
    int redNumSamples = (int)(redAdjustedDuration * sampleRate);
    redSample = new double[redNumSamples];
    redGeneratedSnd = new byte[2 * redNumSamples];

    // fill out the red array
    for (int i = 0; i < redNumSamples; ++i) {
        redSample[i] = redVolume * Math.sin(2 * Math.PI * i / (sampleRate/redFrequency));
    }

    // convert to 16 bit pcm sound array
    // assumes the redSample buffer is normalised.
    int idx = 0;
    for (final double dVal : redSample) {
        // scale to maximum amplitude
        final short val = (short) ((dVal * 16384));
        // in 16 bit wav PCM, first byte is the low order byte
        redGeneratedSnd[idx++] = (byte) (val & 0x00ff);
        redGeneratedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
    }


    // Compute the green channel
    double greenPeriod = 1.0 / greenFrequency;
    double greenAdjustedDuration = (int)((1.0/3)/greenPeriod) * greenPeriod;
    int greenNumSamples = (int)(greenAdjustedDuration * sampleRate);
    greenSample = new double[greenNumSamples];
    greenGeneratedSnd = new byte[2 * greenNumSamples];

    // fill out the green array
    for (int i = 0; i < greenNumSamples; ++i) {
        greenSample[i] = greenVolume * Math.sin(2 * Math.PI * i / (sampleRate/greenFrequency));
    }

    // convert to 16 bit pcm sound array
    // assumes the greenSample buffer is normalised.
    idx = 0;
    for (final double dVal : greenSample) {
        // scale to maximum amplitude
        final short val = (short) ((dVal * 16384));
        // in 16 bit wav PCM, first byte is the low order byte
        greenGeneratedSnd[idx++] = (byte) (val & 0x00ff);
        greenGeneratedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
    }


    // Compute the blue channel
    double bluePeriod = 1.0 / blueFrequency;
    double blueAdjustedDuration = (int)((1.0/3)/bluePeriod) * bluePeriod;
    int blueNumSamples = (int)(blueAdjustedDuration * sampleRate);
    blueSample = new double[blueNumSamples];
    blueGeneratedSnd = new byte[2 * blueNumSamples];

    // fill out the blue array
    for (int i = 0; i < blueNumSamples; ++i) {
        blueSample[i] = blueVolume * Math.sin(2 * Math.PI * i / (sampleRate/blueFrequency));
    }

    // convert to 16 bit pcm sound array
    // assumes the blueSample buffer is normalised.
    idx = 0;
    for (final double dVal : blueSample) {
        // scale to maximum amplitude
        final short val = (short) ((dVal * 16384));
        // in 16 bit wav PCM, first byte is the low order byte
        blueGeneratedSnd[idx++] = (byte) (val & 0x00ff);
        blueGeneratedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
    }
  }

  void playSound(){
      mRedSynthesizer.write(redGeneratedSnd, 0, redGeneratedSnd.length);
      mGreenSynthesizer.write(greenGeneratedSnd, 0, greenGeneratedSnd.length);
      mBlueSynthesizer.write(blueGeneratedSnd, 0, blueGeneratedSnd.length);
  }


  // Decode YUV420SP colorspace to RGB
  static public void decodeYUV420SP(int[] rgb, byte[] yuv420sp, int width, int height) {
    // Calculate frame size
    final int frameSize = width * height;

    // Lets do this with a lot of bit twiddling for some speed
    for (int j = 0, yp = 0; j < height; j++) {
      int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
      for (int i = 0; i < width; i++, yp++) {
        // Get luminosity and set uv values
        int y = (0xff & ((int) yuv420sp[yp])) - 16;
        if (y < 0) y = 0;
        if ((i & 1) == 0) {
          v = (0xff & yuv420sp[uvp++]) - 128;
          u = (0xff & yuv420sp[uvp++]) - 128;
        }

        // Actual converstion equations
        int y1192 = 1192 * y;
        int r = (y1192 + 1634 * v);
        int g = (y1192 - 833 * v - 400 * u);
        int b = (y1192 + 2066 * u);

        // Must be within a certain range and we don't care about errors.
        if (r < 0) r = 0; else if (r > 262143) r = 262143;
        if (g < 0) g = 0; else if (g > 262143) g = 262143;
        if (b < 0) b = 0; else if (b > 262143) b = 262143;

        // Pack it into bytes
        rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
      }
    }
  }

  static public void calculateIntensityHistogram(int[] rgb, int[] histogram, int width, int height, int component) {
    // Zero histogram bins
    for (int bin = 0; bin < 256; bin++) {
      histogram[bin] = 0;
    }

    // RED
    if (component == 0) {
      for (int pix = 0; pix < width*height; pix += 3) {
        int pixVal = (rgb[pix] >> 16) & 0xff; // bitshift and mask to get red value
        histogram[ pixVal ]++;
      }
    }

    // GREEN
    else if (component == 1) {
      for (int pix = 0; pix < width*height; pix += 3) {
        int pixVal = (rgb[pix] >> 8) & 0xff; // bitshift and mask to get green value
        histogram[ pixVal ]++;
      }
    }

    // Must be BLUE
    else {
      for (int pix = 0; pix < width*height; pix += 3) {
        int pixVal = rgb[pix] & 0xff; // mask for blue value
        histogram[ pixVal ]++;
      }
    }
  }
}