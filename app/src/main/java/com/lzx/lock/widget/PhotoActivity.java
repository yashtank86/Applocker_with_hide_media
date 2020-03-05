
package com.lzx.lock.widget;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.asksira.bsimagepicker.BSImagePicker;
import com.bumptech.glide.Glide;
import com.lzx.lock.R;
import com.lzx.lock.adapters.PhotosRecyclerAdapter;
import com.lzx.lock.widget.BaseActivity;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import id.zelory.compressor.Compressor;

public class PhotoActivity extends BaseActivity implements BSImagePicker.OnSingleImageSelectedListener,
        BSImagePicker.OnMultiImageSelectedListener, BSImagePicker.ImageLoaderDelegate {

    FloatingActionButton add_photo;
    RecyclerView photo_recyclerview;
    PhotosRecyclerAdapter photosRecyclerAdapter;


    ArrayList<String> imagesEncodedList;
    //ArrayList<Contact> arrayList;
    List<Uri> selectedImageUriList;
    String imageEncoded;
    private static final int SELECT_PICTURE = 100;

    ArrayList<String> dataimage;

    TextView txt_no_image;

    //DatabaseHelper db;

    Bitmap bitmap;

    ImageView image_demo;

    ArrayList<String> data;

    private static final int BUFFER_SIZE = 4096;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.activity_photo);

        image_demo = (ImageView) findViewById(R.id.image_demo);

        txt_no_image = (TextView) findViewById(R.id.txt_no_image);
        add_photo = (FloatingActionButton) findViewById(R.id.add_photo);
        photo_recyclerview = (RecyclerView) findViewById(R.id.photo_recyclerview);

        new Handler().postDelayed( new Runnable() {
            @Override
            public void run() {


                check();

                //setdata();


                Log.d("dataimageaaa", "onCreate: " + dataimage);


                //db = new DatabaseHelper(getApplicationContext());


                add_photo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BSImagePicker pickerDialog = new BSImagePicker.Builder("com.vault.vault")
                                .setMaximumDisplayingImages( Integer.MAX_VALUE)
                                .isMultiSelect()
                                .setMinimumMultiSelectCount(1)
                                .setMaximumMultiSelectCount(5)
                                .build();
                        pickerDialog.show(getSupportFragmentManager(), "picker");
                    }
                });

            }
        }, 100);

    }


    public void setdata() {

        File notefile = new File( Environment.getExternalStorageDirectory() + "/.FileExplorer/datai/Notes");

        FileInputStream is;
        BufferedReader reader;
        final File file = notefile;

        if (file.exists()) {

            txt_no_image.setVisibility( View.GONE);
            photo_recyclerview.setVisibility( View.VISIBLE);

            Log.d("notefileexist", "onCreate: exist");

            try {
                is = new FileInputStream(file);

                dataimage = new ArrayList<>();
                reader = new BufferedReader(new InputStreamReader(is));
                String line = reader.readLine();
                while (line != null) {
                    Log.d("StackOverflow", line);

                    dataimage.add(line);

                    line = reader.readLine();

                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            photosRecyclerAdapter = new PhotosRecyclerAdapter(getApplicationContext(), dataimage);
            RecyclerView.LayoutManager homelayoutmanager = new GridLayoutManager(getActivity(), 3);
            photo_recyclerview.setLayoutManager(homelayoutmanager);
            photo_recyclerview.setHasFixedSize(true);
            photo_recyclerview.setNestedScrollingEnabled(false);
            photo_recyclerview.setItemAnimator(new DefaultItemAnimator());
            photo_recyclerview.setAdapter(photosRecyclerAdapter);


        } else {
            Log.d("notefileexist", "onCreate: not exist");
            txt_no_image.setVisibility( View.VISIBLE);
            photo_recyclerview.setVisibility( View.GONE);
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        //Toast.makeText(this, "onResume", Toast.LENGTH_LONG).show();
        new Handler().postDelayed( new Runnable() {
            @Override
            public void run() {

                setdata();

            }
        }, 100);
    }


    /*@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            // When an Image is picked
            if (requestCode == SELECT_PICTURE && resultCode == RESULT_OK
                    && null != data) {
                // Get the Image from data

                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                //arrayList = new ArrayList<>();
                if (data.getData() != null) {

                    Uri mImageUri = data.getData();

                    // Get the cursor
                    Cursor cursor = getContentResolver().query(mImageUri,
                            filePathColumn, null, null, null);
                    // Move to first row
                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    imageEncoded = cursor.getString(columnIndex);
                    cursor.close();

                } else {
                    if (data.getClipData() != null) {
                        ClipData mClipData = data.getClipData();
                        ArrayList<Uri> mArrayUri = new ArrayList<Uri>();
                        for (int i = 0; i < mClipData.getItemCount(); i++) {

                            ClipData.Item item = mClipData.getItemAt(i);
                            Uri uri = item.getUri();
                            mArrayUri.add(uri);
                            // Get the cursor
                            Cursor cursor = getContentResolver().query
                                    (uri, filePathColumn, null, null, null);
                            // Move to first row
                            cursor.moveToFirst();

                            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                            imageEncoded = cursor.getString(columnIndex);
                            imagesEncodedList.add(imageEncoded);
                            cursor.close();

                        }
                        Log.v("LOG_TAG", "Selected Images" + mArrayUri.size());
                    }
                }
            } else {
                Toast.makeText(this, "You haven't picked Image",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                    .show();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void loadImage(File imageFile, ImageView ivImage) {
        Glide.with(PhotoActivity.this).load(imageFile).into(ivImage);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onMultiImageSelected(List<Uri> uriList, String tag) {
        selectedImageUriList = uriList;

        Log.d("datatatatata", "onMultiImageSelected: " + selectedImageUriList);

        for (int i = 0; i < selectedImageUriList.size(); i++) {
            createDirectoryMoveFile(selectedImageUriList.get(i));
        }

    }

    @Override
    public void onSingleImageSelected(Uri uri, String tag) {

    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createDirectoryMoveFile(Uri uri) {

        //File direct = new File(Environment.getExternalStorageDirectory() + "/.FileExplorer/image");
        File direct = new File( Environment.getExternalStorageDirectory() + "/.FileExplorer/datai");

        if (!direct.exists()) {
            File wallpaperDirectory = new File( String.valueOf(direct));
            wallpaperDirectory.mkdirs();
        }


        Log.d("ffdirectfffffffffff", "createDirectoryMoveFile: sasasasas" + direct);


        File sourceLocation = new File(uri.getPath());

        //File getFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/testing.pdf");

        String someFilepath = sourceLocation.getName().toString();
        String extension = someFilepath.substring(0, someFilepath.lastIndexOf("."));

        File targetLocation = new File(direct, someFilepath);
        encryptPdfFile(sourceLocation, targetLocation);


        Log.d("fiiileenameee", "createDirectoryMoveFile: " + sourceLocation.getName());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void encryptPdfFile(File sourceLocation, File targetLocation) {
        try {

            File compressedImageFile = new Compressor(this).compressToFile(sourceLocation);

            File extStore = compressedImageFile;
            FileInputStream fis = new FileInputStream(extStore);
            // This stream write the encrypted text. This stream will be wrapped by
            // another stream.
            FileOutputStream fos = new FileOutputStream(targetLocation);

            // Length is 16 byte
            SecretKeySpec sks = new SecretKeySpec("MyDifficultPassw".getBytes(),
                    "AES");
            // Create cipher
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init( Cipher.ENCRYPT_MODE, sks);
            // Wrap the output stream
            CipherOutputStream cos = new CipherOutputStream(fos, cipher);
            // Write bytes
            int b;
            //byte[] d = new byte[4096];
            byte[] d = new byte[8192];
            while ((b = fis.read(d)) != -1) {
                cos.write(d, 0, b);
            }
            // Flush and close streams.
            cos.flush();
            cos.close();
            fis.close();

            //create notes
            generateNote(targetLocation.toString());

            //delete file
            File fdelete = new File( Paths.get(sourceLocation.getPath().toString()).toString());
            if (fdelete.exists()) {
                if (fdelete.delete()) {
                    Log.d("Deletedfile", "createDirectoryMoveFile: file Deleted:");
                } else {
                    Log.d("Deletedfile", "createDirectoryMoveFile: file not Deleted :");
                }
            }

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
    public static Bitmap decryptPdfFile(File sourceLocation) {

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
            byte[] buffer = new byte[4096];
            while ((len = cis.read(buffer, 0, buffer.length)) != -1) {
                baos.write(buffer, 0, len);
            }
            baos.flush();

            byte[] cipherByteArray = baos.toByteArray(); // get the byte array


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

            setdata();

            deleteCache(getApplicationContext());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public void check() {
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

    public static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            deleteDir(dir);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

}

