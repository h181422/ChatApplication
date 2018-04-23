package com.example.larsv.chatapplication;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = {MessageEntity.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract MessageDao userDao();
}