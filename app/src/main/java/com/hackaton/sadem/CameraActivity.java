package com.hackaton.sadem;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import org.openalpr.OpenALPR;
import org.openalpr.model.Results;
import org.openalpr.model.ResultsError;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class CameraActivity extends AppCompatActivity {

    private static String STORAGE_DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/sadem_plates/";
    private String DATA_DIR;

    private static String PLATE_REGION = "il";
    private static String PLATE_COUNTRY = "us";
    private static String OPEN_ALPR_CONF_FILE = File.separatorChar + "runtime_data" + File.separatorChar + "openalpr.conf";
    private static int RESULT_NUMBER = 10;

    private CameraView mCamera;
    private ImageView mShootCamera;
    private OpenALPR mOpenALPR;

    private MaterialDialog resultDialog;
    private View resultView;
    private View resultPlateLayout;
    private View resultProgressLayout;
    private ImageView resultImage;
    private TextView resultPlate;
    private TextView resultConfidence;
    private TextView resultProgressText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        if (getSupportActionBar() != null )
            getSupportActionBar().hide();

        DATA_DIR = this.getApplicationInfo().dataDir;

        initResultView();
        initCamera();
        initOpenALPR();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCamera.start();
    }

    @Override
    protected void onPause() {
        mCamera.stop();
        super.onPause();
    }

    private void initResultView(){
        resultDialog = new MaterialDialog.Builder(this)
                .title(R.string.dialog_result_title)
                .customView(R.layout.dialog_result_recognize, true)
                .cancelable(false)
                .canceledOnTouchOutside(false)
                .build();

        resultView = resultDialog.getView();
        resultPlateLayout = (View) resultView.findViewById(R.id.plate_result_layout);
        resultProgressLayout = (View) resultView.findViewById(R.id.progress_bar_layout);
        resultImage = (ImageView) resultView.findViewById(R.id.result_image);
        resultPlate = (TextView) resultView.findViewById(R.id.result_plate);
        resultConfidence = (TextView) resultView.findViewById(R.id.result_confidence);
        resultProgressText = (TextView) resultView.findViewById(R.id.progress_text);
    }

    private void initCamera() {
        mCamera = (CameraView) findViewById(R.id.camera);
        mCamera.addCameraKitListener(new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {

            }

            @Override
            public void onError(CameraKitError cameraKitError) {

            }

            @Override
            public void onImage(CameraKitImage cameraKitImage) {
                File image = saveImage(cameraKitImage.getBitmap());
                showResulDialog(image);
            }

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {

            }
        });

        mShootCamera = (ImageView) findViewById(R.id.shoot_camera_image_view);
        mShootCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCamera != null) mCamera.captureImage();
            }
        });
    }

    private void initOpenALPR(){
        mOpenALPR = OpenALPR.Factory.create(CameraActivity.this, DATA_DIR);
    }

    private File saveImage(Bitmap image) {
        File imageFile = null;

        String imageFileName = String.valueOf(System.currentTimeMillis()) + ".jpg";
        File storageDir = new File(STORAGE_DIR);
        boolean success = true;
        if (!storageDir.exists()) {
            success = storageDir.mkdirs();
        }
        if (success) {
            imageFile = new File(storageDir, imageFileName);
            try {
                OutputStream fOut = new FileOutputStream(imageFile);
                image.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                fOut.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return imageFile;
    }

    private void showResulDialog(File imageFile){
        Glide.with(this).load(Uri.fromFile(imageFile)).into(resultImage);
        resultProgressText.setText(R.string.processing_image);
        resultProgressLayout.setVisibility(View.VISIBLE);
        resultPlateLayout.setVisibility(View.GONE);
        resultDialog.show();
        mCamera.stop();
        recognizePlate(imageFile.getAbsolutePath());
    }

    private void recognizePlate(final String imagePath){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                String result = mOpenALPR.recognizeWithCountryRegionNConfig(PLATE_COUNTRY, PLATE_REGION,
                        imagePath, DATA_DIR + OPEN_ALPR_CONF_FILE, RESULT_NUMBER);

                Log.d("OPEN ALPR", result);
                validatePlateResult(result, imagePath);
            }
        });
    }

    void validatePlateResult(String result, String imagePath){
        try {
            final Results results = new Gson().fromJson(result, Results.class);

            if (results == null || results.getResults() == null || results.getResults().size() == 0) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CameraActivity.this,
                                CameraActivity.this.getString(R.string.cant_recognize),
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });
                dismissDialog();
            } else {
                renderPlateResult(results, imagePath);
            }
        } catch (JsonSyntaxException exception) {
            final ResultsError resultsError = new Gson().fromJson(result, ResultsError.class);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(CameraActivity.this,resultsError.getMsg(), Toast.LENGTH_LONG)
                        .show();
                }
            });
            dismissDialog();
        }
    }



    private void renderPlateResult(final Results results, String imagePath){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                resultProgressText.setText(R.string.processing_plate);
                resultPlateLayout.setVisibility(View.VISIBLE);
                resultPlate.setText(results.getResults().get(0).getPlate());
                resultConfidence.setText(
                        String.valueOf(Math.round(results.getResults().get(0).getConfidence()))
                        + "% de confianza"
                );
            }
        });
    }

    private void dismissDialog(){
        mCamera.start();
        resultDialog.dismiss();
    }
}
