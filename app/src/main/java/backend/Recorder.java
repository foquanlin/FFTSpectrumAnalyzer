package backend;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Process;
import android.util.Log;

/**
 * Created by lucien on 2018-03-05.
 */

public class Recorder {
    private int rBufferSize , pBufferSize;
    private AudioRecord audioRecord;
    private int SAMPLE_RATE , MAX_FRQ;
    private boolean mShouldContinue;
    private final String TAG = "RECORDER";

    public Recorder() {
        this.SAMPLE_RATE = 44100;
        this.rBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE , AudioFormat.CHANNEL_IN_MONO , AudioFormat.ENCODING_PCM_16BIT);
        this.audioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, rBufferSize);
        this.pBufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
    }

    void recordAudio() {
        new Thread((new Runnable() {
            @Override
            public void run() {
                android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);
                if(rBufferSize == AudioRecord.ERROR || rBufferSize == AudioRecord.ERROR_BAD_VALUE) {
                    rBufferSize = SAMPLE_RATE * 2;
                }

                short[] audioBuffer = new short[rBufferSize / 2];

                if(audioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
                    Log.e(TAG , "Audio Record can't initialize!");
                    return;
                }
                audioRecord.startRecording();
                Log.v(TAG , "Start Recording");

                long shortsRead = 0;
                while(mShouldContinue) {
                    int numberOfShort = audioRecord.read(audioBuffer , 0 , audioBuffer.length);
                    shortsRead += numberOfShort;
                }

                audioRecord.stop();
                audioRecord.release();

                Log.v(TAG , String.format("Recording stopped. Samples read: %d" , shortsRead));
            }
        })).start();
    }

    void playAudio() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (pBufferSize == AudioTrack.ERROR || pBufferSize == AudioTrack.ERROR_BAD_VALUE) {
                    // For some reason we couldn't obtain a buffer size
                    pBufferSize = SAMPLE_RATE * 2;
                }
            }
        });
    }
}
