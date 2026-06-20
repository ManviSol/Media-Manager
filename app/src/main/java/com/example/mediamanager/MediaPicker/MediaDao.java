package com.example.mediamanager.MediaPicker;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface MediaDao {
    @Insert
    public void insert(MediaEntity entity);

    @Query("SELECT * FROM Media_table")
    List<MediaEntity> getAll();

    @Delete
    public void delete(MediaEntity entity);

}
