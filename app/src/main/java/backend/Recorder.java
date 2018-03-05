package backend;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

/**
 * Created by lucien on 2018-03-05.
 */

public class Recorder {
    private int bufferSize;
    private AudioRecord audioRecord;
    private int SAMPLE_RATE , MAX_FRQ;

    public Recorder() {
        this.SAMPLE_RATE = 44100;
        int minBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE , AudioFormat.CHANNEL_IN_MONO , AudioFormat.ENCODING_PCM_16BIT);
        this.audioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
        if(minBufferSize == AudioRecord.ERROR_BAD_VALUE) {
            SAMPLE_RATE = 8000;
            MAX_FRQ = SAMPLE_RATE / 2;
        }
        else {
            this.bufferSize = minBufferSize;
        }
    }
}
