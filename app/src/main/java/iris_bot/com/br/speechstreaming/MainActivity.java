package iris_bot.com.br.speechstreaming;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity {
    AudioRecord recorder;
    private int port = 9000;
    private Socket sk = null;
    private final OnClickListener stopListener = new OnClickListener() {

        @Override
        public void onClick(View arg0) {
            try {
                recorder.release();
                sk.close();
                Log.d("VS", "Recorder released, socket closed");
            } catch (IOException e) {
                Log.e("VS", "IOException");
                e.printStackTrace();
            }
        }

    };
    private int sampleRate = 8000; // 44100 for music
    private int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    int minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat) * 10;
    private boolean status = true;
    private final OnClickListener startListener = new OnClickListener() {

        @Override
        public void onClick(View arg0) {
            status = true;
            startStreaming();
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            View startButton = findViewById(R.id.start_button);
            View stopButton = findViewById(R.id.stop_button);
            //
            startButton.setOnClickListener(startListener);
            stopButton.setOnClickListener(stopListener);

        } catch (Exception e) {
            Log.e("VS", e.toString());
        }

    }

    public void startStreaming() {


        Thread streamThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    sk = new Socket("10.0.2.2", port);
                    Log.d("VS", "Socket Created");

                    byte[] buffer = new byte[minBufSize];

                    Log.d("VS", "Buffer created of size " + minBufSize);

                    recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelConfig, audioFormat, minBufSize);
                    Log.d("VS", "Recorder initialized");

                    recorder.startRecording();
                    while (status) {
                        //reading data from MIC into buffer
                        recorder.read(buffer, 0, buffer.length);
                        sk.getOutputStream().write(buffer);
                    }

                } catch (UnknownHostException e) {
                    e.printStackTrace();
                    Log.e("VS", "UnknownHostException");
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("VS", "IOException");
                } finally {
                    if (sk != null) {
                        try {
                            sk.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e("VS", "IOException_finally");
                        }
                    }
                }
            }

        });
        streamThread.start();
    }

}
