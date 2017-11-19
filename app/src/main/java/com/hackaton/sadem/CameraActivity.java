package com.hackaton.sadem;

import android.graphics.Bitmap;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.hackaton.sadem.api.ApiHelper;
import com.hackaton.sadem.api.ApiService;
import com.hackaton.sadem.api.model.Code;
import com.hackaton.sadem.api.model.DetectionResponse;
import com.hackaton.sadem.api.model.Marbete;
import com.hackaton.sadem.api.model.DgiiResponse;
import com.hackaton.sadem.pref.PreferenceHelper;
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
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CameraActivity extends AppCompatActivity {

    private static String STORAGE_DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/sadem_plates/";
    private String DATA_DIR;

    private static String PLATE_REGION = "il";
    private static String PLATE_COUNTRY = "us";
    private static String OPEN_ALPR_CONF_FILE = File.separatorChar + "runtime_data" + File.separatorChar + "openalpr.conf";
    private static int RESULT_NUMBER = 10;

    private CameraView mCamera;
    private FloatingActionButton mShootCamera;
    private FloatingActionButton mAutoShootCamera;
    private OpenALPR mOpenALPR;
    private ApiService apiService;
    private PreferenceHelper preferenceHelper;
    private Ringtone alarm;

    private MaterialDialog resultDialog;
    private View resultView;
    private View resultPlateLayout;
    private View resultDgiiLayout;
    private View resultProgressLayout;
    private ImageView resultImage;
    private TextView resultPlate;
    private TextView resultConfidence;
    private TextView resultProgressText;
    private TextView resultDgiiText;
    private TextView ownerDgiiText;
    private TextView modelDgiiText;
    private Button closeDialog;

    private boolean autoShoot = false;

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

        preferenceHelper = new PreferenceHelper(this);
        apiService = ApiHelper.newApiService(preferenceHelper.getAccessToken());

        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        alarm = RingtoneManager.getRingtone(getApplicationContext(), notification);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initResultView();
        mCamera.start();
    }

    @Override
    protected void onPause() {
        mCamera.stop();
        dismissDialog();
        resultDialog = null;
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
        resultDgiiLayout = (View) resultView.findViewById(R.id.dgii_result_layout);
        resultImage = (ImageView) resultView.findViewById(R.id.result_image);
        resultPlate = (TextView) resultView.findViewById(R.id.result_plate);
        resultConfidence = (TextView) resultView.findViewById(R.id.result_confidence);
        resultProgressText = (TextView) resultView.findViewById(R.id.progress_text);
        resultDgiiText = (TextView) resultView.findViewById(R.id.result_dgii);
        ownerDgiiText = (TextView) resultView.findViewById(R.id.result_dgii_owner);
        modelDgiiText = (TextView) resultView.findViewById(R.id.result_dgii_model);
        closeDialog = (Button) resultView.findViewById(R.id.close_dialog);
        closeDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismissDialog();
            }
        });
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
                showResulDialog();
                File image = saveImage(cameraKitImage.getBitmap());
                setImageInDialog(image);
            }

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {

            }
        });

        mShootCamera = (FloatingActionButton) findViewById(R.id.shoot_camera_image_view);
        mShootCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                autoShoot = false;
                if (mCamera != null) mCamera.captureImage();
            }
        });

        mAutoShootCamera = (FloatingActionButton) findViewById(R.id.auto_shoot_image_view);
        mAutoShootCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                autoShoot = true;
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
                image.compress(Bitmap.CompressFormat.JPEG, 80, fOut);
                fOut.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return imageFile;
    }

    private void showResulDialog() {
        if (resultDialog != null){
            resultProgressText.setText(R.string.processing_image);
        resultProgressLayout.setVisibility(View.VISIBLE);
        resultPlateLayout.setVisibility(View.GONE);
        resultDgiiLayout.setVisibility(View.GONE);
        closeDialog.setVisibility(View.GONE);
        resultDialog.show();
        mCamera.stop();
        }
    }

    private void setImageInDialog(File imageFile){
        Glide.with(this).load(Uri.fromFile(imageFile)).into(resultImage);
        recognizePlate(imageFile.getAbsolutePath());
    }

    private void recognizePlate(final String imagePath){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                if (resultDialog != null) {
                    String result = mOpenALPR.recognizeWithCountryRegionNConfig(PLATE_COUNTRY, PLATE_REGION,
                            imagePath, DATA_DIR + OPEN_ALPR_CONF_FILE, RESULT_NUMBER);

                    Log.d("OPEN ALPR", result);
                    validatePlateResult(result, imagePath);
                }
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

    private void renderPlateResult(final Results results, final String imagePath){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (resultDialog != null) {
                    String plate = results.getResults().get(0).getPlate();
                    resultProgressText.setText(R.string.processing_plate);
                    resultPlateLayout.setVisibility(View.VISIBLE);
                    resultPlate.setText(plate);
                    resultConfidence.setText(
                            String.valueOf(Math.round(results.getResults().get(0).getConfidence()))
                                    + "% de confianza"
                    );
                    doDgiiQuery(plate, imagePath);
                }
            }
        });
    }

    private void doDgiiQuery(final String plate, final String imagePath){
        Call<DgiiResponse> dgiiCall = apiService.doDegiiQuery(plate, getCoordenates());
        dgiiCall.enqueue(new Callback<DgiiResponse>() {
            @Override
            public void onResponse(Call<DgiiResponse> call, Response<DgiiResponse> response) {
                if (response.code() == 200 && response.body() != null && response.body().getMarbete() != null) {
                    renderDgiiResult(response.body().getMarbete(), imagePath);
                    //upload photo
                } else {
                    Code code = new Code("404", "Matricula no encontrada");
                    Marbete marbete = new Marbete(code);
                    renderDgiiResult(marbete, imagePath);
                }
            }

            @Override
            public void onFailure(Call<DgiiResponse> call, Throwable t) {
                Code code = new Code("999", "No se pudo conectar con el servidor");
                Marbete marbete = new Marbete(code);
                renderDgiiResult(marbete, imagePath);
            }
        });
    }

    private void renderDgiiResult(final Marbete marbete, final String imagePath){
        boolean temp = false;
        switch (marbete.getCode().getCode()){
            case "902":
            case "915":
            case "911":
            case "404":
                temp = true;
                break;
        }
        final boolean stopCard = temp;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                resultProgressLayout.setVisibility(View.GONE);
                resultDgiiLayout.setVisibility(View.VISIBLE);
                resultDgiiText.setText(marbete.getCode().getDescription());
                resultDgiiText.setTextColor(getResources().getColor(
                        stopCard?android.R.color.holo_red_dark:android.R.color.holo_green_dark));
                ownerDgiiText.setText(marbete.getOwner()!=null? marbete.getOwner():"");
                modelDgiiText.setText(marbete.getModel()!=null? marbete.getBrand()+" "+ marbete.getModel()
                        +" "+marbete.getColor()+" "+marbete.getYear_production():"");
                if (stopCard){
                    playAlarm();
                }
            }
        });
        if (stopCard){
            //playAlarm();
            uploadPhoto(marbete, imagePath);
            closeDialog.setVisibility(View.VISIBLE);
        }else{
            if (autoShoot)
                new Handler().postDelayed(new Runnable() {
                public void run() {
                    dismissDialog();
                }
            }, 1000);
            else
                closeDialog.setVisibility(View.VISIBLE);
        }
    }

    private void playAlarm(){
        alarm.play();
    }

    private void uploadPhoto(Marbete marbete, String imagePath){
        File image = new File(imagePath);
        RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), image);
        MultipartBody.Part body = MultipartBody.Part.createFormData("upload", image.getName(), reqFile);
        RequestBody fined = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(false));
        Call<DetectionResponse> call = apiService.doDetectionPut(marbete.getUuid(), body, fined);
        call.enqueue(new Callback<DetectionResponse>() {
            @Override
            public void onResponse(Call<DetectionResponse> call, Response<DetectionResponse> response) {
                if (response.code() == 200){

                }
            }

            @Override
            public void onFailure(Call<DetectionResponse> call, Throwable t) {

            }
        });
    }

    private void dismissDialog(){
        mCamera.start();
        alarm.stop();
        resultDialog.dismiss();
        if (autoShoot){
            CameraActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    new Handler().postDelayed(new Runnable() {
                        public void run() {
                            mCamera.captureImage();
                        }
                    }, 1400);
                }
            });
        }
    }

    private Map<String, String> getCoordenates(){
        Map<String, String> coordinates= new HashMap<>();
        coordinates.put("latitude", "18.481653");
        coordinates.put("longitude", "-69.955808");
        return coordinates;
    }
}
