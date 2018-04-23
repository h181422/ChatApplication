package com.example.larsv.chatapplication;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.example.larsv.chatapplication.Messages.Message;

import java.util.List;

@Dao
public interface MessageDao {
    @Query("SELECT * FROM messageentity")
    List<MessageEntity> getAll();

    @Query("SELECT * FROM messageentity WHERE uid IN (:userIds)")
    List<MessageEntity> loadAllByIds(int[] userIds);

    @Query("SELECT * FROM messageentity WHERE sender LIKE :first AND "
            + "sender LIKE :second AND "+ "receiver LIKE :last LIMIT 1")
    MessageEntity findByName(String first, String second, String last);

    @Insert
    void insertAll(MessageEntity... users);

    @Delete
    void delete(MessageEntity messageEntity);
}