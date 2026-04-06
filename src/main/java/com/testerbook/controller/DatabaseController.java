package com.testerbook.controller;

import com.testerbook.model.TrainingPost;
import com.testerbook.model.User;
import com.testerbook.model.PostStatus;
import com.testerbook.repository.TrainingPostRepository;
import com.testerbook.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
public class DatabaseController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TrainingPostRepository trainingPostRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/user-count")
    public ResponseEntity<String> getUserCount() {
        long count = userRepository.count();
        return ResponseEntity.ok("Total users in database: " + count);
    }

    @PostMapping("/reset-admin")
    public ResponseEntity<String> resetAdminPassword() {
        User admin = userRepository.findByUsername("admin")
            .orElse(new User());
        
        admin.setUsername("admin");
        admin.setEmail("admin@testerbook.com");
        admin.setPassword(passwordEncoder.encode("admin"));
        admin.setFirstName("System");
        admin.setLastName("Administrator");
        admin.setBio("System Administrator");
        admin.setRole(com.testerbook.model.UserRole.ADMIN);
        
        userRepository.save(admin);
        return ResponseEntity.ok("Admin password reset to 'admin'");
    }

    // ==================== TRAINING POST CRUD ENDPOINTS ====================

    @GetMapping("/training-posts")
    public ResponseEntity<List<TrainingPost>> getAllTrainingPosts() {
        List<TrainingPost> posts = trainingPostRepository.findAll();
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/training-posts/{id}")
    public ResponseEntity<TrainingPost> getTrainingPostById(@PathVariable Long id) {
        Optional<TrainingPost> post = trainingPostRepository.findById(id);
        return post.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/training-posts/search")
    public ResponseEntity<List<TrainingPost>> searchTrainingPosts(@RequestParam String keyword) {
        List<TrainingPost> posts = trainingPostRepository.searchPublishedPosts(keyword, PostStatus.PUBLISHED);
        return ResponseEntity.ok(posts);
    }

    @PostMapping("/training-posts")
    public ResponseEntity<TrainingPost> createTrainingPost(@RequestBody TrainingPost post, @RequestParam(required = false) Long authorId) {
        post.setPostDate(LocalDate.now());
        if (post.getStatus() == null) {
            post.setStatus(PostStatus.DRAFT);
        }
        
        if (authorId != null) {
            userRepository.findById(authorId).ifPresent(post::setAuthor);
        }
        
        TrainingPost savedPost = trainingPostRepository.save(post);
        return ResponseEntity.ok(savedPost);
    }

    @PutMapping("/training-posts/{id}")
    public ResponseEntity<TrainingPost> updateTrainingPost(@PathVariable Long id, @RequestBody TrainingPost post) {
        Optional<TrainingPost> existingPost = trainingPostRepository.findById(id);
        if (existingPost.isPresent()) {
            TrainingPost updatedPost = existingPost.get();
            updatedPost.setTitle(post.getTitle());
            updatedPost.setContent(post.getContent());
            updatedPost.setPhase(post.getPhase());
            updatedPost.setCategory(post.getCategory());
            updatedPost.setStatus(post.getStatus());
            trainingPostRepository.save(updatedPost);
            return ResponseEntity.ok(updatedPost);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/training-posts/{id}")
    public ResponseEntity<Void> deleteTrainingPost(@PathVariable Long id) {
        if (trainingPostRepository.existsById(id)) {
            trainingPostRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/training-posts/count")
    public ResponseEntity<String> getTrainingPostCount() {
        long count = trainingPostRepository.count();
        return ResponseEntity.ok("Total training posts: " + count);
    }
}
