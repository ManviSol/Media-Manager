package com.example.mediamanager.MediaPicker;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Media_table")
public class MediaEntity {
    @PrimaryKey(autoGenerate = true)
    int id;

    @ColumnInfo(name = "uri_path")
    String Uri;

    @ColumnInfo(name = "image_type")
    String type;

    @ColumnInfo(name = "image_source")
    String source;

    @ColumnInfo(name = "image_name")
    String image_name;

    @ColumnInfo(name = "size")
    long size;

    @ColumnInfo(name = "time")
    long time;

}
