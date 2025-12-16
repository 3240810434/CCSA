package com.gxuwz.ccsa.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import com.gxuwz.ccsa.model.Comment;
import com.gxuwz.ccsa.model.Post;
import com.gxuwz.ccsa.model.PostMedia;
import java.util.List;

@Dao
public interface PostDao {
    @Insert
    long insertPost(Post post);

    @Insert
    void insertMedia(List<PostMedia> mediaList);

    @Insert
    void insertComment(Comment comment);

    @Query("SELECT * FROM post ORDER BY createTime DESC")
    List<Post> getAllPosts();

    // 【新增】关联用户表，查询指定小区的所有生活动态
    @Query("SELECT post.* FROM post INNER JOIN user ON post.userId = user.id WHERE user.community = :community ORDER BY post.createTime DESC")
    List<Post> getPostsByCommunity(String community);

    @Query("SELECT * FROM post WHERE userId = :userId ORDER BY createTime DESC")
    List<Post> getMyPosts(int userId);

    @Query("SELECT * FROM post_media WHERE postId = :postId")
    List<PostMedia> getMediaForPost(int postId);

    @Query("SELECT * FROM comment WHERE postId = :postId ORDER BY createTime DESC")
    List<Comment> getCommentsForPost(int postId);

    @Query("SELECT COUNT(*) FROM comment WHERE postId = :postId")
    int getCommentCount(int postId);

    @Delete
    void deletePost(Post post);

    @Query("DELETE FROM post_media WHERE postId = :postId")
    void deletePostMedia(int postId);

    @Query("DELETE FROM comment WHERE postId = :postId")
    void deletePostComments(int postId);

    @Delete
    void deleteComment(Comment comment);
}