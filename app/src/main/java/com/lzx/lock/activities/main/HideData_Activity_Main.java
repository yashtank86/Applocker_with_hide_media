package com.lzx.lock.activities.main;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.lzx.lock.R;
import com.lzx.lock.widget.PhotoActivity;

public class HideData_Activity_Main extends AppCompatActivity {

    CardView photo;
    public static final int CODE_REQUEST_PERMISSION = 100;

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.hide_data_activity_main );

        photo = findViewById( R.id.hide_photo_card );

        checkWriteStoragePermition();
        checkReadStoragePermition();

        if (!checkIfGetPermission()) {
            showPermissionRequestDialog();
        }


    }

    public void card_hide_photos(View view) {

        //startActivity( new Intent( HideData_Activity_Main.this, PhotoActivity.class ) );
        if (ContextCompat.checkSelfPermission(getApplicationContext(), "android.permission.WRITE_EXTERNAL_STORAGE") != 0 || ContextCompat.checkSelfPermission(getApplicationContext(), "android.permission.READ_EXTERNAL_STORAGE") != 0) {
            showDailogpermission();
        } else {

            final Animation myAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bounce);
            photo.startAnimation(myAnim);

            Intent intent = new Intent(getApplicationContext(), PhotoActivity.class);
            startActivity(intent);

        }

    }

    private boolean checkIfGetPermission() {
        AppOpsManager appOps = (AppOpsManager) this
                .getSystemService( Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow("android:get_usage_stats",
                android.os.Process.myUid(), this.getPackageName());
        return (mode == AppOpsManager.MODE_ALLOWED);
    }

    private void showPermissionRequestDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.ic_warning_white_48dp)
                .setTitle(R.string.dialog_title_warning)
                .setCancelable(false)
                .setMessage(R.string.dialog_content_request_permission)
                .setPositiveButton(R.string.dialog_action_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent( Settings.ACTION_USAGE_ACCESS_SETTINGS);
                        HideData_Activity_Main.this.startActivityForResult(intent, CODE_REQUEST_PERMISSION);
                    }
                })
                .setNegativeButton(R.string.dialog_action_no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Toast.makeText(StartActivity.this,R.string.toast_info_request_permission_failed, Toast.LENGTH_SHORT).show();
                    }
                });
        builder.create().show();
    }


    public void checkWriteStoragePermition() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), "android.permission.WRITE_EXTERNAL_STORAGE") != 0) {
            requestStoragePermission();
            return;
        }

    }

    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, "android.permission.WRITE_EXTERNAL_STORAGE")) {
            ActivityCompat.requestPermissions(this, new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 0);
            //Toast.makeText(this, "Permission needed to Get Contact", Toast.LENGTH_SHORT).show();
            return;
        }
        ActivityCompat.requestPermissions(this, new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 0);
    }

    public void checkReadStoragePermition() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), "android.permission.READ_EXTERNAL_STORAGE") != 0) {
            requestReadStoragePermission();
            return;
        }

    }

    private void requestReadStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, "android.permission.READ_EXTERNAL_STORAGE")) {
            ActivityCompat.requestPermissions(this, new String[]{"android.permission.READ_EXTERNAL_STORAGE"}, 0);
            //Toast.makeText(this, "Permission needed to Get Contact", Toast.LENGTH_SHORT).show();
            return;
        }
        ActivityCompat.requestPermissions(this, new String[]{"android.permission.READ_EXTERNAL_STORAGE"}, 0);
    }

    public void showDailogpermission() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Application Required Read Write External Storage Permission");
        alertDialogBuilder.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        checkWriteStoragePermition();
                        checkReadStoragePermition();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

}
