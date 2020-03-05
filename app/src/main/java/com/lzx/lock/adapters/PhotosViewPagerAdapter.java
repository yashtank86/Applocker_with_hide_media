package com.lzx.lock.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bogdwellers.pinchtozoom.ImageMatrixTouchHandler;
import com.lzx.lock.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class PhotosViewPagerAdapter extends PagerAdapter {


    Uri imageUri;
    Bitmap bitmap;
    Context context;
    ArrayList<String> dataimage;

    public PhotosViewPagerAdapter(Context context, ArrayList<String> dataimage) {
        this.context = context;
        this.dataimage = dataimage;

        Log.d("PhotosAdapterinside", "PhotosViewPagerAdapter: PhotosAdapterinside");
    }

    @Override
    public int getCount() {
        Log.d("dataimagesize", "getCount: dataimagesize:--"+dataimage.size());
        return dataimage.size();

    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater inflater= (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE);
        View view=inflater.inflate(R.layout.custom_photo_view_pager_layout,null);

        ImageView item_viewpager_image=view.findViewById( R.id.item_viewpager_image);

        //image zoom
        item_viewpager_image.setOnTouchListener(new ImageMatrixTouchHandler(context));

        String uria=dataimage.get(position);
        imageUri= Uri.parse(uria);

        File targetl = new File(imageUri.getPath());


        item_viewpager_image.setImageBitmap(decryptPdfFile(targetl));

        ViewPager viewPager = (ViewPager) container;
        viewPager.addView(view);

        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        ViewPager viewPager = (ViewPager) container;
        View view = (View) object;
        viewPager.removeView(view);
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
            //byte[] buffer = new byte[4096];
            byte[] buffer = new byte[8192];
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
}
