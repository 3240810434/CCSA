package com.gxuwz.ccsa.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.gxuwz.ccsa.model.HelpPost;
import com.gxuwz.ccsa.model.HelpPostMedia;
import java.util.List;

@Dao
public interface HelpPostDao {
    @Insert
    long insertPost(HelpPost post);

    @Insert
    void insertMediaList(List<HelpPostMedia> list);

    @Query("SELECT * FROM help_post ORDER BY createTime DESC")
    List<HelpPost> getAllHelpPosts();

    @Query("SELECT * FROM help_post_media WHERE helpPostId = :postId")
    List<HelpPostMedia> getMediaForPost(int postId);
}