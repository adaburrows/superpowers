package com.adaburrows.superpowers;

import java.io.IOException;

import android.util.Log;
import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.media.AudioManager;
import android.media.audiofx.Equalizer;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.MediaRecorder.AudioSource;
import android.media.audiofx.Visualizer;

public class Superpowers extends Activity {
  private static final String TAG = "Superpowers";

  private CameraView mCameraView;
  Camera mCamera;
  private MagicOverlay mOverlay;
  AudioRecord mAudioRecord;
  AudioTrack mAudioTrack, mRedSynthesizer, mGreenSynthesizer, mBlueSynthesizer;
  AudioSynth mAudioSynth;
  Visualizer mAudioVisualizer;
  MediaBridge mPlayer;
  int mAudioSessionId;
  int mAudioSampleRate;
  int mAudioChannelInConfig;
  int mAudioChannelOutConfig;
  int mAudioEncodingFormat;
  int mAudioBufferSize;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Hide the window title.
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
  }

  @Override
  protected void onPause() {
    super.onPause();
    if (mCamera != null) {
      mCamera.stopPreview();
      mCamera.release();
      mCamera = null;
    }
    if (mAudioRecord != null) {
      mAudioRecord.stop();
      mAudioRecord.release();
      mAudioRecord = null;
    }
    if (mAudioTrack != null) {
      mAudioTrack.stop();
      mAudioTrack.release();
      mAudioTrack = null;
    }
    if (mRedSynthesizer != null) {
      mRedSynthesizer.stop();
      mRedSynthesizer.release();
      mRedSynthesizer = null;
    }
    if (mGreenSynthesizer != null) {
      mGreenSynthesizer.stop();
      mGreenSynthesizer.release();
      mGreenSynthesizer = null;
    }
    if (mBlueSynthesizer != null) {
      mBlueSynthesizer.stop();
      mBlueSynthesizer.release();
      mBlueSynthesizer = null;
    }
    if (mAudioVisualizer != null) {
      mAudioVisualizer.release();
    }
    if (mPlayer != null) {
      mPlayer.stopRunning();
    }
  }

  @Override
  protected void onResume() {
    super.onResume();

    setVolumeControlStream(AudioManager.STREAM_MUSIC);
    mCamera = getCamera();
    setupAudio();
    setupCamera();
    mAudioSynth = new AudioSynth(mCamera, mRedSynthesizer, mGreenSynthesizer, mBlueSynthesizer);
    mCameraView = new CameraView(this, mCamera, mAudioSynth);
    mOverlay = new MagicOverlay(this);
    setContentView(mCameraView);
    addContentView(mOverlay, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    //setContentView(R.layout.main);
  }

  public static Camera getCamera() {
    Camera c = null;
    try {
      // attempt to get a Camera instance
      c = Camera.open();
    }
    catch (Exception exception){
      // Camera is not available (in use or does not exist)
      Log.d(TAG, "Error fetching camera: " + exception.getMessage());
    }
    return c;
  }

  public void setupCamera() {
    if (mCamera != null) {
      Camera.Parameters parameters = mCamera.getParameters();
      /*
       * This needs to be changed. I'm using two obsolete API calls because it was easier.
       */
      Display display = getWindowManager().getDefaultDisplay();
      int w = display.getWidth();
      int h = display.getHeight();
      double aspect = (double)w/(double)h;
      Camera.Size previewSize = null;
      int previewPixels = 0;
      for(Camera.Size s : parameters.getSupportedPreviewSizes()) {
        // OK, here's the deal: We want to pick a preview resolution which:
        //   * Matches the aspect of our display
        //   * Is as large as possible for clarity
        //   * Is not larger than we can display, to avoid extra computation
        // We are foiled in this by the fact that cameras on android can be
        // kind of annoying. For example, my phone has a 960x540 screen,
        // but the camera captures at 960x544! Because of this some amount
        // of fuzzy matching is needed.

        if(s.width - 8 > w)
          continue;
        if(s.height - 8 > h)
          continue;

        double cam_aspect = (double)s.width/(double)s.height;
        // 1% fuzz factor on aspect should be fine? (I hope...)
        if(Math.abs(1.0 - cam_aspect/aspect) < 0.01) {
          int cameraPixels = s.width * s.height;
          if(cameraPixels > previewPixels) {
            // This is bigger than the last one we found. Use it!
            previewSize = s;
            previewPixels = cameraPixels;
          }
        }
      }
      if(previewSize != null) {
        parameters.setPreviewSize(previewSize.width, previewSize.height); //obs
        Log.d(TAG, String.format("Using %d x % d", previewSize.width, previewSize.height));
      } else {
        // We didn't find a good match. Just fall back to 640x480
        parameters.setPreviewSize(640, 480);
      }
      parameters.setPreviewFrameRate(30); //obs
//      parameters.setSceneMode(Camera.Parameters.SCENE_MODE_NIGHT);
      parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
      // Use above parameters
      mCamera.setParameters(parameters);
    }
  }

  public void setupAudio() {
    //HOWTO:  Set up audio buffer for reading.
    mAudioSampleRate = 44100;
    mAudioChannelInConfig = AudioFormat.CHANNEL_IN_MONO;
    mAudioChannelOutConfig = AudioFormat.CHANNEL_OUT_MONO;
    mAudioEncodingFormat = AudioFormat.ENCODING_PCM_16BIT;
    mAudioBufferSize = AudioRecord.getMinBufferSize(
      mAudioSampleRate,
      mAudioChannelInConfig,
      mAudioEncodingFormat
    ) * 3;
    
    mAudioRecord = new AudioRecord(
      MediaRecorder.AudioSource.MIC,
      mAudioSampleRate,
      mAudioChannelInConfig,
      mAudioEncodingFormat,
      mAudioBufferSize
    );
    mAudioSessionId = mAudioRecord.getAudioSessionId();
    Log.d(TAG, "Audio session ID: " + mAudioSessionId);

    mAudioTrack = new AudioTrack(
      AudioManager.STREAM_MUSIC,
      mAudioSampleRate,
      mAudioChannelOutConfig,
      mAudioEncodingFormat,
      mAudioBufferSize,
      AudioTrack.MODE_STREAM,
      mAudioSessionId
    );

    mRedSynthesizer = new AudioTrack(
      AudioManager.STREAM_MUSIC,
      mAudioSampleRate,
      mAudioChannelOutConfig,
      mAudioEncodingFormat,
      mAudioBufferSize,
      AudioTrack.MODE_STREAM
    );

    mGreenSynthesizer = new AudioTrack(
      AudioManager.STREAM_MUSIC,
      mAudioSampleRate,
      mAudioChannelOutConfig,
      mAudioEncodingFormat,
      mAudioBufferSize,
      AudioTrack.MODE_STREAM
    );

    mBlueSynthesizer = new AudioTrack(
      AudioManager.STREAM_MUSIC,
      mAudioSampleRate,
      mAudioChannelOutConfig,
      mAudioEncodingFormat,
      mAudioBufferSize,
      AudioTrack.MODE_STREAM
    );

    mAudioVisualizer = null;
    try {
      mAudioVisualizer = new Visualizer(mAudioSessionId);
      mAudioVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
      mAudioVisualizer.setDataCaptureListener(
        new Visualizer.OnDataCaptureListener() {

          public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {}

          public void onFftDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {
            mOverlay.updateData(bytes, samplingRate, mAudioVisualizer.getCaptureSize());
          }

        },
        Visualizer.getMaxCaptureRate() / 2, false, true
      );
      mAudioVisualizer.setEnabled(true);
    } catch (Exception exception) {
      Log.d(TAG, "Error creating Visualizer: " + exception.getMessage());
    }

    mPlayer = new MediaBridge(mAudioRecord, mAudioTrack, mAudioBufferSize);
    new Thread(mPlayer).start();

    mAudioTrack.play();
    mRedSynthesizer.play();
    mGreenSynthesizer.play();
    mBlueSynthesizer.play();
  }

}