package com.example.mediamanager.MediaPicker;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mediamanager.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MediaPickerActivity extends AppCompatActivity {
    FloatingActionButton add_media;
    RecyclerView recycle;
    private ActivityResultLauncher<Uri> cameraLauncher;
    private ActivityResultLauncher<String> galleryLauncher;
    private ActivityResultLauncher<String[]> documentLauncher;
    private ActivityResultLauncher<String> permissionLauncher;
    private Uri imageUri;
    MediaPickerAdapter adapter;
    TextView nadata;
    LinearLayout llnodata;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mediapicker_main);
        add_media = findViewById(R.id.add_media);
        recycle = findViewById(R.id.recycle);
        nadata = findViewById(R.id.nadata);
        llnodata = findViewById(R.id.llnodata);

        add_media.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SelectImage();
            }
        });

        cameraLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), result ->{
            if(result){
                String image_name = getFileNameFromUri(imageUri);
                long size = getFileSize(imageUri);
                long time = System.currentTimeMillis();
                saveToDatabase(imageUri.toString(),"image","camera",image_name,size,time);
            }
        });
        galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri ->{
            if(uri != null){
                String image_name = getFileNameFromUri(uri);
                long size = getFileSize(uri);
                long time = System.currentTimeMillis();
                saveToDatabase(uri.toString(),"image","gallery",image_name,size,time);
            }
        });

        documentLauncher = registerForActivityResult(new ActivityResultContracts.OpenDocument(),uri ->{
            if(uri != null){
                getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                String doc_name = getFileNameFromUri(uri);
                long size = getFileSize(uri);
                long time = System.currentTimeMillis();
                saveToDatabase(uri.toString(),"document","pdf_document",doc_name,size,time);
            }


        });

        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(),isGranted ->{
            if(isGranted){
                OpenCamera();
            }else {
                Toast.makeText(this, "Pemission denied", Toast.LENGTH_SHORT).show();
            }
        });

        List<MediaEntity> list = MediaDatabase.getInstance(this).mediaDao().getAll();
        if(list != null){
            adapter = new MediaPickerAdapter(MediaPickerActivity.this, list, new MediaPickerAdapter.OnItemDeleteListener() {
                @Override
                public void onDelete(MediaEntity entity) {
                    deleteItem(entity);
                }
            });
            GridLayoutManager manager = new GridLayoutManager(MediaPickerActivity.this,2,RecyclerView.VERTICAL,false);
            recycle.setLayoutManager(manager);
            recycle.setAdapter(adapter);
            if(list.size() == 0){
                llnodata.setVisibility(View.VISIBLE);
            }else {
                llnodata.setVisibility(View.GONE);
            }

        }else{
            llnodata.setVisibility(View.VISIBLE);
        }


        System.out.println("what is the size of the list?"+list.size());

    }
    public void OpenCamera(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
            imageUri = createImageUri();
            if(imageUri != null){
                cameraLauncher.launch(imageUri);
            }else {
                Toast.makeText(this, "failed to launch camera", Toast.LENGTH_SHORT).show();
            }

        }else {
            permissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }
    public void OpenGallery(){
        galleryLauncher.launch("image/*");
    }
    public void OpenDocument(){
        documentLauncher.launch(new String[]{
                "application/pdf","image/*"
        });
    }

    private Uri createImageUri(){
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"Camera Image");
        return getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
    }


    public void SelectImage(){
        final BottomSheetDialog dialog = new BottomSheetDialog(MediaPickerActivity.this,R.style.AppTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.choose_image);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
            if(dialog != null){
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable((Color.TRANSPARENT)));
             }
        LinearLayout llcamera = dialog.findViewById(R.id.llcamera);
        LinearLayout llgallery = dialog.findViewById(R.id.llgallery);
        LinearLayout lldocument = dialog.findViewById(R.id.lldocument);

        lldocument.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OpenDocument();
                dialog.dismiss();
            }
        });
        llgallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OpenGallery();
                dialog.dismiss();
            }
        });
        llcamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OpenCamera();
                dialog.dismiss();
            }
        });


            dialog.show();
    }
    public String getFileNameFromUri(Uri uri){
        String name = null;

        if(uri.getScheme().equals("content")){
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try{
                if(cursor != null && cursor.moveToFirst()){
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if(index != -1){
                        name = cursor.getString(index);
                    }
                }
            } finally {
                if(cursor != null) cursor.close();
            }
        }

        if(name == null){
            name = uri.getLastPathSegment();
        }

        return name;
    }
    public long getFileSize(Uri uri){
        long size = 0;
        if(uri.getScheme().equals("content")){
            Cursor cursor = getContentResolver().query(uri,null,null,null,null);
            try{
                if(cursor != null && cursor.moveToFirst()){
                    int index = cursor.getColumnIndex(OpenableColumns.SIZE);
                    if(index != -1){
                        size = cursor.getLong(index);
                    }
                }
            }catch (Exception e){

            }
        }
        return size;
    }


    public  void saveToDatabase(String uri, String type,String source,String name,long size,long time){
        MediaEntity entity = new MediaEntity();
        entity.Uri = uri;
        entity.type = type;
        entity.source = source;
        entity.image_name = name;
        entity.size = size;
        entity.time = time;


        MediaDatabase.getInstance(this).mediaDao().insert(entity);

        List<MediaEntity> list = MediaDatabase.getInstance(this).mediaDao().getAll();
        adapter = new MediaPickerAdapter(MediaPickerActivity.this, list, new MediaPickerAdapter.OnItemDeleteListener() {
            @Override
            public void onDelete(MediaEntity entity) {
                deleteItem(entity);
            }
        });
        GridLayoutManager manager = new GridLayoutManager(MediaPickerActivity.this,2,RecyclerView.VERTICAL,false);
        recycle.setLayoutManager(manager);
        recycle.setAdapter(adapter);
        llnodata.setVisibility(View.GONE);
        adapter.notifyDataSetChanged();


    }
    public  void deleteItem(MediaEntity entity){
        MediaDatabase.getInstance(MediaPickerActivity.this).mediaDao().delete(entity);

        List<MediaEntity> updated_list = MediaDatabase.getInstance(MediaPickerActivity.this).mediaDao().getAll();
        adapter.updatedList(updated_list);

    }






}
