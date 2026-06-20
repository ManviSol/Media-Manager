package com.example.mediamanager.MediaPicker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.example.mediamanager.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MediaPickerAdapter extends RecyclerView.Adapter<MediaPickerAdapter.ViewHolder>{
    Context context;
    List<MediaEntity> arraylist;
    OnItemDeleteListener listener;

   public MediaPickerAdapter(Context context, List<MediaEntity> arraylist,OnItemDeleteListener listener){
       this.arraylist = arraylist;
       this.context = context;
       this.listener = listener;

   }
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.media_adapter,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MediaEntity entity_list = arraylist.get(position);
        holder.name.setText(entity_list.type);

        Uri uri = Uri.parse(entity_list.Uri);

        if(entity_list.source.equals("camera")){
            holder.new_image.setVisibility(View.VISIBLE);
            holder.pdf_placeholder.setVisibility(View.GONE);
            holder.empty_placeholder.setVisibility(View.GONE);

            Glide.with(context).load(uri).into(holder.new_image);
            holder.media_type_badge.setText("Camera");
            holder.media_type_badge.setVisibility(View.VISIBLE);

        }else if(entity_list.source.equals("gallery")){
            holder.new_image.setVisibility(View.VISIBLE);
            holder.pdf_placeholder.setVisibility(View.GONE);
            holder.empty_placeholder.setVisibility(View.GONE);

            Glide.with(context).load(uri).into(holder.new_image);
            holder.media_type_badge.setText("Gallery");
            holder.media_type_badge.setVisibility(View.VISIBLE);

        }else if(entity_list.source.equals("pdf_document")){
            holder.new_image.setVisibility(View.GONE);
            holder.pdf_placeholder.setVisibility(View.VISIBLE);
            holder.empty_placeholder.setVisibility(View.GONE);
            holder.media_type_badge.setText("PDF");
        }else {
            holder.new_image.setVisibility(View.GONE);
            holder.pdf_placeholder.setVisibility(View.GONE);
            holder.empty_placeholder.setVisibility(View.VISIBLE);
        }

        holder.name.setText(entity_list.image_name);
        holder.file_size.setText(formatSize(entity_list.size));
        holder.file_date.setText(GetDate(entity_list.time));

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                dialog.setTitle("Delete Media");
                dialog.setMessage("Are you sure you want to delete this media file?");
                dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        int pos = holder.getAdapterPosition();
                        if(pos != RecyclerView.NO_POSITION && listener != null){
                            listener.onDelete(arraylist.get(pos));
                        }
                    }
                });
                dialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                dialog.show();

            }
        });




    }

    @Override
    public int getItemCount() {
        return arraylist.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
       ImageView  new_image;
       TextView name,media_type_badge,file_size,file_date,delete;
       LinearLayout empty_placeholder,pdf_placeholder;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            new_image = itemView.findViewById(R.id.new_image);
            name = itemView.findViewById(R.id.name);
            delete = itemView.findViewById(R.id.delete);
            file_date = itemView.findViewById(R.id.file_date);
            file_size = itemView.findViewById(R.id.file_size);
            empty_placeholder = itemView.findViewById(R.id.empty_placeholder);
            pdf_placeholder = itemView.findViewById(R.id.pdf_placeholder);
            media_type_badge = itemView.findViewById(R.id.media_type_badge);
        }
    }
    public String GetDate(long timestamp){
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
    public String formatSize(long size){
        if(size <= 0) return "0 B";

        final String[] units = new String[]{"B", "KB", "MB", "GB"};
        int digitGroup = (int) (Math.log10(size) / Math.log10(1024));

        return String.format(Locale.getDefault(),
                "%.1f %s",
                size / Math.pow(1024, digitGroup),
                units[digitGroup]);
    }
    public  interface OnItemDeleteListener{
        void onDelete(MediaEntity entity);
    }
    public void updatedList(List<MediaEntity> list){
       this.arraylist = list;
       notifyDataSetChanged();

    }
}
