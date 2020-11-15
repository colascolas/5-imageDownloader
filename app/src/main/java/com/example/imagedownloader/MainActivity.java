package com.example.imagedownloader;

import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {


    final static int WRITE_EXTERNAL = 0;
    private File mPath;
    private String mName;
    private Button mButton;
    private EditText mEdit;
    private ProgressBar mProgress;
    private ImageView mImage;

    private Thread downloadThread;

    File mFile = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mButton = findViewById(R.id.button);
        mEdit = findViewById(R.id.url);
        mProgress = findViewById(R.id.pbar);
        mImage = findViewById(R.id.picture);
        mEdit.setText("http://www.cashadvance6online.com/data/archive/img/675795715.jpeg");

        // permission request for writing in download directory
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL);

            mButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final String url = mEdit.getText().toString();
                    mName = url.substring(url.lastIndexOf('/') + 1);
                    mPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

                    // launch the thread
                    downloadThread = new Thread(new Runnable() {
                        public void run() {
                            downloadFile(url);
                        }
                    });
                    mProgress.setVisibility(View.VISIBLE);
                    downloadThread.start();
                }
            });

        }
    }

    /**
     * Vérification de l'obtention des droits
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case WRITE_EXTERNAL:
                if (grantResults.length == 0 || grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(this, "Need Write access", Toast.LENGTH_SHORT).show();
                    finish();
                }
            default:
        }

    }


    private void downloadFile(String url) {

        int total = 0;
        String name;
        int count = 0;
        byte data[] = new byte[1024];
        HttpURLConnection connection = null;
        InputStream iStream = null;
        OutputStream oStream = null;
        try {
            URL urlRef = new URL(url);
            connection = (HttpURLConnection) urlRef.openConnection();
            connection.connect();
            final int fileLength = connection.getContentLength();
            iStream = connection.getInputStream();
            File file = new File(mPath, mName);
            oStream = new FileOutputStream(file);

            while ((count = iStream.read(data)) != -1) {
                total += count;
                oStream.write(data, 0, count);
                updateProgress(total, fileLength);
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mProgress.setVisibility(View.INVISIBLE);
                    // draw the picture
                    mImage.setImageBitmap(BitmapFactory.decodeFile(mPath + "/" + mName));
                    mImage.setVisibility(View.VISIBLE);
                }
            });


        } catch (Exception e) {
            Log.w("ExternalStorage", "Error writing " + mPath + "/" + mName, e);
        } finally {
            try {
                if (iStream != null)
                    iStream.close();
                if (oStream != null)
                    oStream.close();
            } catch (IOException ignored) {

            }
            if (connection != null)
                connection.disconnect();
        }
    }

    /**
     * Mise à jour de la barre de progression
     * @param count : nombre d'octets lus
     * @param length : nombre d'octets à lire
     */
    private void updateProgress(final int count, final int length) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgress.setProgress((100 * count) / length);
            }
        });

    }
}

