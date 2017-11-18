package com.hackaton.sadm;

import android.Manifest;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hackaton.sadm.pref.PreferenceHelper;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.listener.multi.BaseMultiplePermissionsListener;

public class LauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);
        if (getSupportActionBar() != null )
            getSupportActionBar().hide();
        checkPermissions();

    }

    private void checkPermissions(){
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO
                ).withListener(new BaseMultiplePermissionsListener() {
                                   @Override
                                   public void onPermissionsChecked(MultiplePermissionsReport report) {
                                       Log.d("++++", "onPermissionsChecked");
                                       if (report.areAllPermissionsGranted()){
                                           navigate(getNextActivity());
                                       } else {
                                           deniedPermissionDialog();
                                       }
                                   }
                               }
        ).check();
    }

    private void deniedPermissionDialog() {
        new MaterialDialog.Builder(this)
                .title(R.string.dialog_denied_title)
                .content(R.string.dialog_denied_content)
                .positiveText(R.string.dialog_denied_ok)
                .negativeText(R.string.dialog_denied_cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        checkPermissions();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        LauncherActivity.this.finish();
                    }
                })
                .show();
    }

    private Class getNextActivity(){
        PreferenceHelper helper = new PreferenceHelper(this);
        if (helper.isUserLogged()) return MainActivity.class;
        return LoginActivity.class;
    }

    private void navigate(final Class nextActivity){
        new Handler().postDelayed(new Runnable() {
            public void run() {
                Intent intent = new Intent(LauncherActivity.this, nextActivity);
                LauncherActivity.this.startActivity(intent);
                LauncherActivity.this.finish();
            }
        }, 2000);
    }

}
