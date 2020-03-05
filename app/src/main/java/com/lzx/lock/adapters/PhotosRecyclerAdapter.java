package com.lzx.lock.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


import com.lzx.lock.R;
import com.lzx.lock.widget.PhotosViewpagerActivity;

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

public class PhotosRecyclerAdapter extends RecyclerView.Adapter<PhotosRecyclerAdapter.MyViewHolder> {


    Uri imageUri;
    Bitmap bitmap;
    Context context;
    ArrayList<String> dataimage;

    public PhotosRecyclerAdapter(Context context, ArrayList<String> dataimage) {
        this.context = context;
        this.dataimage = dataimage;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo_view, parent, false);

        return new MyViewHolder(itemView);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {

        String uria=dataimage.get(position);
        imageUri= Uri.parse(uria);

        File targetl = new File(imageUri.getPath());

        holder.item_photo_image.setImageBitmap(decryptPdfFile(targetl));


        holder.item_photo_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(context, PhotosViewpagerActivity.class);
                intent.addFlags( Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("photos",dataimage);
                intent.putExtra("position",position);
                context.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return dataimage.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView item_photo_image;

        public MyViewHolder(View itemView) {
            super(itemView);

            item_photo_image=itemView.findViewById( R.id.item_photo_image);

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

}
