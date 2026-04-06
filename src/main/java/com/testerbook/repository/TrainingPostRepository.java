package com.testerbook.repository;

import com.testerbook.model.TrainingPost;
import com.testerbook.model.PostStatus;
import com.testerbook.model.LearningPhase;
import com.testerbook.model.PostCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TrainingPostRepository extends JpaRepository<TrainingPost, Long> {
    List<TrainingPost> findByAuthorId(Long authorId);
    List<TrainingPost> findByStatus(PostStatus status);
    List<TrainingPost> findByPhase(LearningPhase phase);
    List<TrainingPost> findByCategory(PostCategory category);
    List<TrainingPost> findByPostDate(LocalDate postDate);
    
    @Query("SELECT p FROM TrainingPost p WHERE p.status = :status ORDER BY p.postDate DESC")
    List<TrainingPost> findPublishedPostsOrderByDateDesc(@Param("status") PostStatus status);
    
    @Query("SELECT p FROM TrainingPost p WHERE p.author.id = :authorId AND p.status = :status ORDER BY p.postDate DESC")
    List<TrainingPost> findByAuthorAndStatusOrderByDateDesc(@Param("authorId") Long authorId, @Param("status") PostStatus status);
    
    @Query("SELECT p FROM TrainingPost p WHERE p.title LIKE %:keyword% OR p.content LIKE %:keyword% AND p.status = :status")
    List<TrainingPost> searchPublishedPosts(@Param("keyword") String keyword, @Param("status") PostStatus status);
}
