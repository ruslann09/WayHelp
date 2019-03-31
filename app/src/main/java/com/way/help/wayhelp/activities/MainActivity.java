package com.way.help.wayhelp.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.way.help.wayhelp.R;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    VideoView videoView;
    EditText textView;

    //For looking logs
    ArrayAdapter adapter;
    ArrayList<String> list = new ArrayList<>();

    CameraSource cameraSource;

    private MediaPlayer mediaPlayer;

    private boolean isSignalStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startActivity(new Intent(getApplicationContext(), VoiceAssistantActivity.class));

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
            Toast.makeText(this, "Grant Permission and restart app", Toast.LENGTH_SHORT).show();
        }
        else {
            videoView = findViewById(R.id.videoView);
            textView = findViewById(R.id.textView);
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
            videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.videoplayback));
            videoView.start();
            createCameraSource();
        }
    }


    //This class will use google vision api to detect eyes
    private class EyesTracker extends Tracker<Face> {

        private final float THRESHOLD = 0.5f;

        private EyesTracker() {

        }

        @Override
        public void onUpdate(Detector.Detections<Face> detections, final Face face) {
            if (face.getIsLeftEyeOpenProbability() > THRESHOLD || face.getIsRightEyeOpenProbability() > THRESHOLD) {
                isSignalStarted = false;

                Log.i(TAG, "onUpdate: Eyes Detected");
                showStatus("Eyes Detected and open, so video continues");

                if (!videoView.isPlaying())
                    videoView.start();

                releaseMP(mediaPlayer);
            } else {
                if (videoView.isPlaying())
                    videoView.pause();

                    if (!isSignalStarted) {
                        isSignalStarted = true;

                        releaseMP(mediaPlayer);
                        mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.alarm);
                        mediaPlayer.setLooping(true);
                        mediaPlayer.start();
                    }

                showStatus("Eyes Detected and closed, so video paused");
            }
        }

        @Override
        public void onMissing(Detector.Detections<Face> detections) {
            super.onMissing(detections);

            releaseMP(mediaPlayer);
            mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.alarm_infinity);
            mediaPlayer.setLooping(true);
            mediaPlayer.start();

            showStatus("Face Not Detected yet!");
        }

        @Override
        public void onDone() {
            super.onDone();
        }
    }

    private void releaseMP(MediaPlayer mediaPlayer) {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.release();
                mediaPlayer = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class FaceTrackerFactory implements MultiProcessor.Factory<Face> {

        private FaceTrackerFactory() {

        }

        @Override
        public Tracker<Face> create(Face face) {
            return new EyesTracker();
        }
    }

    public void createCameraSource() {
        FaceDetector detector = new FaceDetector.Builder(this)
                .setTrackingEnabled(true)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .setMode(FaceDetector.ACCURATE_MODE)
                .build();
        detector.setProcessor(new MultiProcessor.Builder(new FaceTrackerFactory()).build());

        cameraSource = new CameraSource.Builder(this, detector)
                .setRequestedPreviewSize(1024, 768)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedFps(50.0f)
                .build();

        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            cameraSource.start();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (cameraSource != null) {
            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                cameraSource.start();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cameraSource!=null) {
            cameraSource.stop();
        }
        if (videoView.isPlaying()) {
            videoView.pause();
        }
    }

    public void showStatus(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText(message);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraSource!=null) {
            cameraSource.release();
        }
    }
}