//package com.example.audioprocess;
//
//package com.example.audioprocess;
//
//import android.Manifest;
//import androidx.annotation.NonNull;
//import androidx.annotation.RequiresApi;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;
//
//import android.os.Build;
//import android.os.Bundle;
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
//import android.util.Log;
//
//public class MainActivity extends AppCompatActivity {
//    private static final String TAG = "AudioProcessing";
//
//    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
//    private static final int SAMPLE_RATE = 44100;
//    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
//    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
//
//    private AudioRecord audioRecord;
//    private AudioTrack audioTrack;
//    private Thread recordingThread;
//
//    private boolean isRecording = false;
//
//    private final Object lock = new Object();
//
//    private short[] buffer;
//
//    @RequiresApi(api = Build.VERSION_CODES.M)
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        // Check if the required permissions are granted
//        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
//            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
//        } else {
//            startAudioProcessing();
//        }
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.M)
//    private void startAudioProcessing() {
//        // Set up the audio buffer
//        int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
//        buffer = new short[bufferSize / 2];
//
//        // Create the AudioRecord instance
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, bufferSize);
//
//        // Create the AudioTrack instance
//        audioTrack = new AudioTrack.Builder()
//                .setAudioFormat(new AudioFormat.Builder()
//                        .setEncoding(AUDIO_FORMAT)
//                        .setSampleRate(SAMPLE_RATE)
//                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
//                        .build())
//                .setTransferMode(AudioTrack.MODE_STREAM)
//                .build();
//
//        // Start recording and playback in separate threads
//        isRecording = true;
//        recordingThread = new Thread(this::recordAndPlayAudio);
//        recordingThread.start();
//    }
//
//    private void recordAndPlayAudio() {
//        try {
//            audioRecord.startRecording();
//            audioTrack.play();
//
//            while (isRecording) {
//                int bytesRead = audioRecord.read(buffer, 0, buffer.length);
//                audioTrack.write(buffer, 0, bytesRead);
//
//                // Process the audio data here (e.g., calculate amplitude, apply filters, etc.)
//                // You can access the audio samples in the 'buffer' array
//                // Remember to perform any UI updates on the main UI thread if needed
//                // Example:
//                double amplitude = calculateAmplitude(buffer);
//                Log.d(TAG, "Amplitude: " + amplitude);
//            }
//
//            audioRecord.stop();
//            audioRecord.release();
//            audioTrack.stop();
//            audioTrack.release();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    // Calculate the amplitude of the audio signal
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
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        stopAudioProcessing();
//    }
//
//    private void stopAudioProcessing() {
//        synchronized (lock) {
//            isRecording = false;
//        }
//        try {
//            recordingThread.join();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.M)
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                startAudioProcessing();
//            } else {
//                // Permission denied. Handle accordingly (e.g., show an error message, disable functionality)
//                Log.e(TAG, "Record audio permission denied");
//            }
//        }
//    }
//}
//
//
