package com.example.audioprocess;

import android.Manifest;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.content.pm.PackageManager;

import android.media.AudioAttributes;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;


import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.AudioTrack;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.SeekBar;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    private TextView tvAmplitude, tvVolume;
    private Button btnStart, btnStop;
    private SeekBar volumeSeekBar;

    private boolean isRecording = false;
    private AudioRecord audioRecord;
    private AudioTrack audioTrack;
    private short[] audioBuffer;
    private final Object lock = new Object();

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvAmplitude = findViewById(R.id.tvAmplitude);
        btnStart = findViewById(R.id.btnStart);
        btnStop = findViewById(R.id.btnStop);
        volumeSeekBar = findViewById(R.id.volumeSeekBar);
        tvVolume = findViewById(R.id.tvVolume);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAudioProcessing();
                btnStart.setEnabled(false);
                btnStop.setEnabled(true);
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopAudioProcessing();
                btnStart.setEnabled(true);
                btnStop.setEnabled(false);
            }
        });

        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Adjust the volume based on the seek bar progress
                float volume = (float) progress / seekBar.getMax();
//                float volume = progress;

                audioTrack.setVolume(volume);
                int newProgress = seekBar.getProgress();
                tvVolume.setText("Volume: " + newProgress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Request the necessary permissions at runtime
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO_PERMISSION);
        }
    }

    private void startAudioProcessing() {
        synchronized (lock) {
            isRecording = true;
        }

        Thread recordingThread = new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void run() {
                int bufferSize = AudioRecord.getMinBufferSize(44100,
                        AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);

                audioBuffer = new short[bufferSize / 2];

                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    // Request the missing permissions
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.RECORD_AUDIO},
                            REQUEST_RECORD_AUDIO_PERMISSION);
                    return;
                }
                audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 44100,
                        AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
                audioTrack = new AudioTrack.Builder()
                        .setAudioAttributes(new AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build())
                        .setAudioFormat(new AudioFormat.Builder()
                                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                .setSampleRate(44100)
                                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                                .build())
                        .setBufferSizeInBytes(bufferSize)
                        .build();
                audioRecord.startRecording();
                audioTrack.play();

                while (isRecording) {
                    int bytesRead = audioRecord.read(audioBuffer, 0, audioBuffer.length);
                    if (bytesRead > 0) {
                        double amplitude = calculateAmplitude(audioBuffer);
                        updateAmplitudeText(amplitude);
                        audioTrack.write(audioBuffer, 0, bytesRead);
                    }
                }

                audioRecord.stop();
                audioRecord.release();
                audioRecord = null;
                audioBuffer = null;
            }
        });

        recordingThread.start();
    }

    private void stopAudioProcessing() {
        synchronized (lock) {
            isRecording = false;
        }
    }


    private double calculateAmplitude(short[] samples) {
        double sum = 0.0;
        for (short sample : samples) {
//            sum += Math.abs(sample);
            sum += (sample*sample);
        }
        double averageAmplitude = (double)Math.sqrt( sum / samples.length);

        double reference = 32767.0; // Maximum amplitude for 16-bit signed samples
        double amplitudeRatio = averageAmplitude / reference;
        double amplitude_dB = 250.0 * amplitudeRatio;
        double amplitude = Math.abs(amplitude_dB);

        return amplitude;
    }

    private void adjustVolume(int progress) {
        double volumeScale = progress / 100.0;
        // Apply volume scaling or any other audio processing as desired
    }

    private void updateAmplitudeText(final double amplitude) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> tvAmplitude.setText("Amplitude: " + String.format("%.2f", amplitude) + " dB"));
    }
}
