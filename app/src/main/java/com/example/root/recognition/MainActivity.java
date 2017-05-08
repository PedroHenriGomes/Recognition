package com.example.root.recognition;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private TextView vizualizadorDeTexto;
    private SurfaceView imagemDaCamera;
    private CameraSource cameraSource;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imagemDaCamera = (SurfaceView) findViewById(R.id.cam_view);
        vizualizadorDeTexto = (TextView) findViewById(R.id.text_value);

        TextRecognizer processadorDeImagens = new TextRecognizer.Builder(getApplicationContext()).build();

        if (isDisponivel(processadorDeImagens)) {
            Log.w("MainActivity", "As dependências do projeto ainda não estão disponívels, conecte-se para baixá-las ");
        }


        cameraSource = new CameraSource.Builder(getApplicationContext(), processadorDeImagens)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1280, 1024)
                .setRequestedFps(2.0f)
                .setAutoFocusEnabled(true)
                .build();

        imagemDaCamera.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {

                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    cameraSource.start(imagemDaCamera.getHolder());
                    System.out.print("passou aqui");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });

        processadorDeImagens.setProcessor(new Detector.Processor<TextBlock>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<TextBlock> detections) {
                Log.d("Main", "receiveDetections");
                final SparseArray<TextBlock> items = detections.getDetectedItems();
                if (items.size() != 0) {
                    vizualizadorDeTexto.post(new Runnable() {
                        @Override
                        public void run() {
                            StringBuilder value = new StringBuilder();
                            for (int i = 0; i < items.size(); ++i) {
                                TextBlock item = items.valueAt(i);
                                value.append(item.getValue());
                                value.append("\n");
                            }
                            vizualizadorDeTexto.setText(value.toString());
                        }
                    });
                }

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public native String stringFromJNI();

    private boolean isDisponivel(TextRecognizer textRecognizer) {
        return !textRecognizer.isOperational();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraSource.release();
    }
}
