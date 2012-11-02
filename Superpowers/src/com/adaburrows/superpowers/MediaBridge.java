package com.adaburrows.superpowers;

import android.content.Context;
import android.media.AudioRecord;
import android.media.AudioTrack;

class MediaBridge implements Runnable{

	private boolean keepRunning = true;
	private AudioRecord mAudioRecord;
	private AudioTrack mAudioTrack;
	private int mAudioBufferSize;

	MediaBridge(AudioRecord audioRecord, AudioTrack audioTrack, int audioBufferSize){
		mAudioRecord = audioRecord;
		mAudioTrack = audioTrack;
		mAudioBufferSize = audioBufferSize;
	}

	@Override
	public void run(){
		mAudioRecord.startRecording();
		while (keepRunning) {
			int chunk_size = mAudioBufferSize / 2;
			short[] samples = new short[chunk_size];
			for (int i = 0; i < 2; i++) {
				int offset = i * chunk_size;
				mAudioRecord.read(samples, offset, chunk_size);
				mAudioTrack.write(samples, offset, chunk_size);
			}
		}
	}

	public void stopRunning(){
		keepRunning = false;
	}
}
