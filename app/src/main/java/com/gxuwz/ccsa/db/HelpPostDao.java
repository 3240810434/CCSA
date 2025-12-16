package com.gxuwz.ccsa.db;

import androidx.room.Dao;
import androidx.room.Delete;
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

    // 【新增】关联用户表，查询指定小区的所有互助帖
    @Query("SELECT help_post.* FROM help_post INNER JOIN user ON help_post.userId = user.id WHERE user.community = :community ORDER BY help_post.createTime DESC")
    List<HelpPost> getHelpPostsByCommunity(String community);

    // 新增：查询当前用户的互助帖
    @Query("SELECT * FROM help_post WHERE userId = :userId ORDER BY createTime DESC")
    List<HelpPost> getMyHelpPosts(int userId);

    // 新增：删除互助帖
    @Delete
    void deletePost(HelpPost post);

    @Query("SELECT * FROM help_post_media WHERE helpPostId = :postId")
    List<HelpPostMedia> getMediaForPost(int postId);
}