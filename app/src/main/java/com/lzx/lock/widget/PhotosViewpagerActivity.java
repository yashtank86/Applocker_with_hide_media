package com.lzx.lock.widget;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lzx.lock.R;
import com.lzx.lock.adapters.PhotosViewPagerAdapter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class PhotosViewpagerActivity extends BaseActivity {

    ViewPager photo_view_pager;
    ArrayList<String> dataimage;
    int currentPosition;
    TextView mtextView;

    LinearLayout photo_delete,photo_export,photo_share;

    Uri imageUri;

    private static final int BUFFER_SIZE = 4096;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_photos_viewpager);

        dataimage=getIntent().getExtras().getStringArrayList("photos");
        currentPosition=getIntent().getExtras().getInt("position");

        //String encodedImage = Base64.encodeToString(byteArrayImage, Base64.DEFAULT);

        mtextView.setText(""+(currentPosition+1)+"/"+dataimage.size());

        photo_view_pager= (ViewPager) findViewById(R.id.photo_view_pager);

        photo_delete= (LinearLayout) findViewById(R.id.photo_delete);
        photo_export= (LinearLayout) findViewById(R.id.photo_export);
        photo_share= (LinearLayout) findViewById(R.id.photo_share);

        mtextView = findViewById( R.id.title_of_image );


        Log.d("viewdataimagepager", "onCreate: dataimage:--"+dataimage);

        photo_view_pager.setAdapter(new PhotosViewPagerAdapter(getApplicationContext(),dataimage));
        photo_view_pager.setCurrentItem(currentPosition);

        photo_view_pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                mtextView.setText(""+(position+1)+"/"+dataimage.size());

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        photo_share.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {

                photo_share.setClickable(false);
                photo_delete.setClickable(false);
                photo_share.setClickable(false);

                String uria=dataimage.get(photo_view_pager.getCurrentItem());
                imageUri= Uri.parse(uria);

                File targetl = new File(imageUri.getPath());

                Bitmap decodedByte = decryptFileAndShare(targetl);


                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                decodedByte.compress( Bitmap.CompressFormat.JPEG, 100, bytes);
                String path1 = MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(), decodedByte, "Title", null);
                Uri path2 = Uri.parse(path1);

                Intent intent = new Intent( Intent.ACTION_SEND);
                intent.setType("image/png");
                intent.putExtra( Intent.EXTRA_STREAM, path2);
                startActivity( Intent.createChooser(intent, "Share"));

                new Handler().postDelayed( new Runnable() {
                    @Override
                    public void run() {
                        photo_share.setClickable(true);
                        photo_delete.setClickable(true);
                        photo_share.setClickable(true);
                    }
                }, 2000);

            }
        });

        photo_export.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showExportDailog();

            }
        });

        photo_delete.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {

                showDeleteDailog();

            }
        });

    }

    public void generateNote(String sBody) {
        try {
            File root = new File( Environment.getExternalStorageDirectory() + "/.FileExplorer/datai");
            if (!root.exists()) {
                root.mkdirs();
            }

            File gpxfile;
            File notefile = new File( Environment.getExternalStorageDirectory() + "/.FileExplorer/datai/Notes");
            gpxfile = notefile;
            if (!notefile.exists()) {
                gpxfile = new File(root, "Notes");
                Log.d("Noteexiteeeeee", "generateNote: Noteexiteeeeee");
            }

            FileWriter writer = new FileWriter(gpxfile, true);
            writer.append(sBody);
            writer.append('\n');
            writer.flush();
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void showDeleteDailog() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Are you sure you want to permanently delete this image");
        alertDialogBuilder.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                        delete_item();

                    }
                });

        alertDialogBuilder.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }


    public void showExportDailog() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Are you sure you want to export this image");
        alertDialogBuilder.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                        File direct = new File( Environment.getExternalStorageDirectory() + "/DCIM");

                        if (!direct.exists()) {
                            File wallpaperDirectory = new File( String.valueOf(direct));
                            wallpaperDirectory.mkdirs();
                        }

                        String uria=dataimage.get(photo_view_pager.getCurrentItem());
                        imageUri= Uri.parse(uria);

                        File sourceLocation = new File(imageUri.getPath());

                        String someFilepath = sourceLocation.getName().toString();
                        //String extension = someFilepath.substring(0, someFilepath.lastIndexOf("."));

                        File targetLocation = new File(direct, someFilepath);

                        decryptFileAndSave(sourceLocation,targetLocation);

                    }
                });

        alertDialogBuilder.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void decryptFileAndSave(File sourceLocation, File targetLocation) {

        try {

            File extStore = sourceLocation;
            FileInputStream fis = new FileInputStream(extStore);

            FileOutputStream fos = new FileOutputStream(targetLocation);
            SecretKeySpec sks = new SecretKeySpec("MyDifficultPassw".getBytes(),
                    "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init( Cipher.DECRYPT_MODE, sks);
            CipherInputStream cis = new CipherInputStream(fis, cipher);

            int b;
            byte[] d = new byte[8192];
            while ((b = cis.read(d)) != -1) {
                fos.write(d, 0, b);

            }
            fos.flush();
            fos.close();
            cis.close();

            delete_item();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static Bitmap decryptFileAndShare(File sourceLocation) {

        Bitmap decodedByte = null;

        try {

            File extStore = sourceLocation;
            FileInputStream fis = new FileInputStream(extStore);

            //FileOutputStream fos = new FileOutputStream(targetLocation+".jpg");
            SecretKeySpec sks = new SecretKeySpec("MyDifficultPassw".getBytes(),
                    "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init( Cipher.DECRYPT_MODE, sks);
            CipherInputStream cis = new CipherInputStream(fis, cipher);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int len;
            //byte[] buffer = new byte[4096];
            byte[] buffer = new byte[8192];
            while ((len = cis.read(buffer, 0, buffer.length)) != -1) {
                baos.write(buffer, 0, len);
            }
            baos.flush();

            byte[] cipherByteArray = baos.toByteArray(); // get the byte array


            /*int b;
            byte[] d = new byte[8];
            while ((b = cis.read(d)) != -1) {
                //fos.write(d, 0, b);

            }
            fos.flush();
            fos.close();
            cis.close();*/

            /*byte[] ba = new byte[0];
            Path path = Paths.get(sourceLocation.getPath());
            try {
                ba= Files.readAllBytes(path);
            } catch (IOException e) {
                e.printStackTrace();
            }*/

            decodedByte = BitmapFactory.decodeByteArray(cipherByteArray, 0, cipherByteArray.length);




        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return decodedByte;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void delete_item(){

        String uria=dataimage.get(photo_view_pager.getCurrentItem());
        imageUri= Uri.parse(uria);

        dataimage.remove(photo_view_pager.getCurrentItem());

        File notefile = new File( Environment.getExternalStorageDirectory() + "/.FileExplorer/datai/Notes");

        PrintWriter writer = null;
        try {
            writer = new PrintWriter(notefile);

            writer.print("");
            writer.close();

            for (int i=0;i<dataimage.size();i++){

                generateNote(dataimage.get(i));

            }

            photo_view_pager.setAdapter(new PhotosViewPagerAdapter(getApplicationContext(),dataimage));

            if (currentPosition==0){
                currentPosition=currentPosition+1;
            }else {
                currentPosition=currentPosition-1;
            }

            photo_view_pager.setCurrentItem(currentPosition);

            mtextView.setText(""+((currentPosition)+1)+"/"+dataimage.size());


            File fdelete = new File( Paths.get(imageUri.getPath().toString()).toString());
            if (fdelete.exists()) {
                if (fdelete.delete()) {
                    Log.d("Deletedfile", "createDirectoryMoveFile: file Deleted:");
                } else {
                    Log.d("Deletedfile", "createDirectoryMoveFile: file not Deleted :");
                }
            }

            if (dataimage.size()==0){
                File fdelete1 = new File( Environment.getExternalStorageDirectory() + "/.FileExplorer/datai/Notes");
                if (fdelete1.exists()) {
                    if (fdelete1.delete()) {
                        Log.d("DeleteNotesdfile", "createDirectoryMoveFile:Notes file Deleted:");
                    } else {
                        Log.d("DeleteNotesdfile", "createDirectoryMoveFile:Notes file not Deleted :");
                    }
                }
                onBackPressed();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }


    public void btn_back_image(View view) {
        onBackPressed();
    }
}
