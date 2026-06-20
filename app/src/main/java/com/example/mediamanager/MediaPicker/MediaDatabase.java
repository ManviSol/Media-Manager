package com.example.mediamanager.MediaPicker;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {MediaEntity.class},version = 3)
public abstract class MediaDatabase extends RoomDatabase {
    public abstract MediaDao mediaDao();
    public static  MediaDatabase instance;
    public static synchronized MediaDatabase getInstance(Context context){
        if(instance == null){
            instance = Room.databaseBuilder(context.getApplicationContext(),MediaDatabase.class,"media_database")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }

}
