package com.testerbook.controller;

import com.testerbook.model.TrainingPost;
import com.testerbook.model.PostStatus;
import com.testerbook.repository.TrainingPostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/posts/public")
public class PublicPostController {

    @Autowired
    private TrainingPostRepository trainingPostRepository;

    @GetMapping("/all")
    public ResponseEntity<List<TrainingPost>> getAllPublicPosts() {
        // Return only published posts for the public view
        List<TrainingPost> posts = trainingPostRepository.findPublishedPostsOrderByDateDesc(PostStatus.PUBLISHED);
        return ResponseEntity.ok(posts);
    }
}
