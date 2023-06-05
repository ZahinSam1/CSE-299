//package com.example.audioprocess;
//
//import android.Manifest;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.RequiresApi;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;
//
//import android.os.Build;
//import android.os.Bundle;
//import android.os.Handler;
//
//import android.content.pm.PackageManager;
//import android.media.AudioFormat;
//import android.media.AudioRecord;
//import android.media.MediaRecorder;
//import android.media.AudioTrack;
//
//import android.os.Looper;
//import android.util.Log;
//import android.view.View;
//import android.widget.Button;
//import android.widget.TextView;
//
//
//public class MainActivity extends AppCompatActivity {
//    private static final String TAG = "MainActivity";
//
//    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
//
//    private TextView tvAmplitude;
//    private Button btnStart, btnStop;
//
//    private boolean isRecording = false;
//    private Thread recordingThread;
//    private AudioRecord audioRecord;
//    private short[] audioBuffer;
//    private final Object lock = new Object();
//
//    @RequiresApi(api = Build.VERSION_CODES.M)
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        tvAmplitude = findViewById(R.id.tvAmplitude);
//        btnStart = findViewById(R.id.btnStart);
//        btnStop = findViewById(R.id.btnStop);
//
//        btnStart.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startAudioProcessing();
//                btnStart.setEnabled(false);
//                btnStop.setEnabled(true);
//            }
//        });
//
//        btnStop.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                stopAudioProcessing();
//                btnStart.setEnabled(true);
//                btnStop.setEnabled(false);
//            }
//        });
//
//        // Request the necessary permissions at runtime
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
//                != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.RECORD_AUDIO},
//                    REQUEST_RECORD_AUDIO_PERMISSION);
//        }
//    }
//
//    private void startAudioProcessing() {
//        synchronized (lock) {
//            isRecording = true;
//        }
//
//        // Create a separate thread for audio recording and processing
//        recordingThread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                int bufferSize = AudioRecord.getMinBufferSize(44100,
//                        AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
//
//                audioBuffer = new short[bufferSize / 2];
//
//                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
//                    // TODO: Consider calling
//                    //    ActivityCompat#requestPermissions
//                    // here to request the missing permissions, and then overriding
//                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                    //                                          int[] grantResults)
//                    // to handle the case where the user grants the permission. See the documentation
//                    // for ActivityCompat#requestPermissions for more details.
//                    return;
//                }
//                audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 44100,
//                        AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
//
//                audioRecord.startRecording();
//
//                while (isRecording) {
//                    int bytesRead = audioRecord.read(audioBuffer, 0, audioBuffer.length);
//                    if (bytesRead > 0) {
//                        double amplitude = calculateAmplitude(audioBuffer);
//                        updateAmplitudeText(amplitude);
//                    }
//                }
//
//                audioRecord.stop();
//                audioRecord.release();
//                audioRecord = null;
//                audioBuffer = null;
//            }
//        });
//
//        recordingThread.start();
//    }
//
//    private void stopAudioProcessing() {
//        synchronized (lock) {
//            isRecording = false;
//        }
//    }
//
//    private void updateAmplitudeText(final double amplitude) {
//        Handler handler = new Handler(Looper.getMainLooper());
//        handler.post(() -> tvAmplitude.setText("Amplitude: " + String.format("%.2f", amplitude) + " dB"));
//    }
//
//    private double calculateAmplitude(short[] samples) {
//        double sum = 0.0;
//        for (short sample : samples) {
//            sum += Math.abs(sample);
//        }
//        double averageAmplitude = sum / samples.length;
//
//        double reference = 32767.0; // Maximum amplitude for 16-bit signed samples
//        double amplitudeRatio = averageAmplitude / reference;
//        double amplitude_dB = 20.0 * Math.log10(amplitudeRatio);
//        double amplitude = Math.abs(amplitude_dB);
//
//        return amplitude;
//    }
//}
