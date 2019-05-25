package com.pockettextrecognizer.Activities;

import android.Manifest;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import android.text.method.ScrollingMovementMethod;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;
import java.util.Locale;

import com.pockettextrecognizer.R;

public class MainActivity extends AppCompatActivity {

    SurfaceView phoneCameraView;
    TextView scanTextView;
    CameraSource phoneCameraSource;
    TextToSpeech textToSpeech;

    Button scanButton;
    Button captureButton;
    Button listenButton;

    private static final String TAG = "MainActivity";
    private static final int requestPermissionID = 101;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**
         * Making the text view scrollable
         */
        TextView textView = findViewById(R.id.edit_text);
        textView.setMovementMethod(new ScrollingMovementMethod());

        /**
         * Casting Camera View to surface view
         */
        phoneCameraView = findViewById(R.id.surfaceView);

        /**
         * Calling Camera Source function for preview
         */
        startCameraSource();

        /**
         * Casting buttons to layout
         */
        scanButton = findViewById(R.id.scan_button);
        captureButton = findViewById(R.id.capture_button);
        listenButton = findViewById(R.id.listen_button);

        ScanInit();
        CaptureInit();
        ListenInit();

    }

    public void ScanInit(){
        scanButton.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick (View v){
                        if(scanButton.getText()!="reset scanner") {
                            scanTextView = findViewById(R.id.edit_text);
                            scanButton.setText("reset scanner");
                        }
                        else
                        {
                            Thread restartThread = new Thread(){
                                @Override
                                public void run() {
                                    Intent intent = new Intent(getApplicationContext(),LoadingActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            };
                            // Launching LoadingActivity.
                            restartThread.start();
                        }
                    }
                }
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode != requestPermissionID) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                phoneCameraSource.start(phoneCameraView.getHolder());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void startCameraSource() {

        // Creating the Text Recognizer
        TextRecognizer textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();

        /**
         * Checking if textRecognizer is operational.
         * Checking the device has enough storage to download the native library.
         * If everything is in order, the Camera Source is initialized.
         */
        if (!textRecognizer.isOperational()) {

            Log.w(TAG, "Detector dependencies not loaded yet");
            IntentFilter lowStorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowStorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(this, R.string.low_storage_error, Toast.LENGTH_LONG).show();
                Log.w(TAG, getString(R.string.low_storage_error));
            }
        } else {

            // Initializing the Camera Source to use high resolution and set auto-focus on
            phoneCameraSource = new CameraSource.Builder(getApplicationContext(), textRecognizer)
                    .setFacing(CameraSource.CAMERA_FACING_BACK)
                    .setRequestedPreviewSize(1280, 1024)
                    .setAutoFocusEnabled(true)
                    .setRequestedFps(15.0f)
                    .build();

            /**
            * Adding callback to SurfaceView.
            * Checking if camera permission is given by the user.
            * If permission is granted the phoneCameraSource can start and be passed to phoneCameraView.
            */
            phoneCameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    try {

                        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.CAMERA},
                                    requestPermissionID);
                            return;
                        }
                        /**
                         * Passing CameraSource to phoneCameraView
                         */
                        phoneCameraSource.start(phoneCameraView.getHolder());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    phoneCameraSource.stop();
                }
            });

            /**
             * Setting the Text Recognizer Processor.
             */
            textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
                @Override
                public void release() {
                }

                /**
                 * Setting for the processor to receive detections, build captured text strings
                 * and pass it to the scanTextView
                 */
                @Override
                public void receiveDetections(Detector.Detections<TextBlock> detections) {
                    final SparseArray<TextBlock> items = detections.getDetectedItems();
                    if (items.size() != 0) {
                        /**
                         * Posting to scanTextView
                         */
                        scanTextView.post(new Runnable() {
                            @Override
                            public void run() {
                                /**
                                 * Building the strings.
                                 */
                                StringBuilder stringBuilder = new StringBuilder();
                                for (int i = 0; i < items.size(); i++) {
                                    TextBlock item = items.valueAt(i);
                                    stringBuilder.append(item.getValue());
                                    stringBuilder.append("\n");
                                }
                                scanTextView.setText(stringBuilder.toString());
                            }
                        });
                    }
                }
            });
        }
    }

    /**
     * Checking if the button is clicked.
     * If clicked, reassigning the button's tittle and functionality to pass the text in
     * SaveActivity and launch SaveActivity.
     */
    public void CaptureInit(){
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (captureButton.getText() != "save/view") {
                    phoneCameraSource.stop();
                    scanButton.setText("reset scanner");
                    captureButton.setText("save/view");
                }
                else{
                    TextView capturedText = findViewById(R.id.edit_text);
                    String text = capturedText.getText().toString();
                    Intent intent = new Intent(v.getContext(), SaveActivity.class);
                    intent.putExtra("capturedText",text);
                    startActivity(intent);
                }
            }
        });
    }

    public void ListenInit(){
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {

                if (status == TextToSpeech.SUCCESS) {
                    int textToSpeechLang = textToSpeech.setLanguage(Locale.US);
                    if (textToSpeechLang == TextToSpeech.LANG_MISSING_DATA
                            || textToSpeechLang == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "The Language is not supported!");
                    } else {
                        Log.i("TTS", "Language Supported.");
                    }
                    Log.i("TTS", "Initialization success.");
                } else {
                    Toast.makeText(getApplicationContext(), "Text To Speech Initialization failed!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        listenButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        listen();
                    }
                });
    }

    public void listen() {

        if(scanTextView==null){
            Toast.makeText(this, R.string.listen_error, Toast.LENGTH_LONG).show();
        }else {
            String data = scanTextView.getText().toString();
            Log.i("TTS", "button clicked: " + data);
            int speechStatus = textToSpeech.speak(data, TextToSpeech.QUEUE_FLUSH, null);

            if (speechStatus == TextToSpeech.ERROR) {
                Log.e("TTS", "Error in converting Text to Speech!");
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

}