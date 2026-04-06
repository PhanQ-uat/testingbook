package com.testerbook.controller;

import com.testerbook.model.Activity;
import com.testerbook.model.User;
import com.testerbook.repository.ActivityRepository;
import com.testerbook.repository.UserRepository;
import com.testerbook.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/activities")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ActivityController {

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Get all activities for the current logged-in user
     */
    @GetMapping("/my-activities")
    public ResponseEntity<List<Activity>> getMyActivities(HttpServletRequest request) {
        User user = getCurrentUser(request);
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }
        
        List<Activity> activities = activityRepository.findByUserIdOrderByDateDesc(user.getId());
        return ResponseEntity.ok(activities);
    }

    /**
     * Get activities for a specific date
     */
    @GetMapping("/by-date")
    public ResponseEntity<List<Activity>> getActivitiesByDate(
            HttpServletRequest request,
            @RequestParam String date) {
        User user = getCurrentUser(request);
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }
        
        LocalDate activityDate = LocalDate.parse(date);
        List<Activity> activities = activityRepository.findByActivityDate(activityDate);
        return ResponseEntity.ok(activities);
    }

    /**
     * Create a new activity
     */
    @PostMapping
    public ResponseEntity<Activity> createActivity(
            HttpServletRequest request,
            @Valid @RequestBody Activity activity) {
        User user = getCurrentUser(request);
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }
        
        activity.setUser(user);
        activity.setCreatedAt(LocalDateTime.now());
        if (activity.getActivityDate() == null) {
            activity.setActivityDate(LocalDate.now());
        }
        
        Activity savedActivity = activityRepository.save(activity);
        return ResponseEntity.ok(savedActivity);
    }

    /**
     * Update an existing activity
     */
    @PutMapping("/{id}")
    public ResponseEntity<Activity> updateActivity(
            HttpServletRequest request,
            @PathVariable Long id,
            @Valid @RequestBody Activity activityDetails) {
        User user = getCurrentUser(request);
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }
        
        Optional<Activity> activityOpt = activityRepository.findById(id);
        if (activityOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Activity activity = activityOpt.get();
        // Ensure user owns this activity
        if (!activity.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }
        
        activity.setActivityType(activityDetails.getActivityType());
        activity.setDescription(activityDetails.getDescription());
        activity.setActivityDate(activityDetails.getActivityDate());
        
        Activity updatedActivity = activityRepository.save(activity);
        return ResponseEntity.ok(updatedActivity);
    }

    /**
     * Delete an activity
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteActivity(
            HttpServletRequest request,
            @PathVariable Long id) {
        User user = getCurrentUser(request);
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }
        
        Optional<Activity> activityOpt = activityRepository.findById(id);
        if (activityOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Activity activity = activityOpt.get();
        // Ensure user owns this activity
        if (!activity.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }
        
        activityRepository.delete(activity);
        return ResponseEntity.ok().build();
    }

    /**
     * Get activity statistics for the current user
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getActivityStats(HttpServletRequest request) {
        User user = getCurrentUser(request);
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }
        
        List<Activity> activities = activityRepository.findByUserId(user.getId());
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalActivities", activities.size());
        stats.put("activitiesThisWeek", countActivitiesThisWeek(activities));
        stats.put("activitiesThisMonth", countActivitiesThisMonth(activities));
        stats.put("currentStreak", calculateStreak(activities));
        stats.put("longestStreak", calculateLongestStreak(activities));
        
        return ResponseEntity.ok(stats);
    }

    // Helper methods
    private User getCurrentUser(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            String username = jwtUtil.extractUsername(token);
            Optional<User> userOpt = userRepository.findByUsername(username);
            return userOpt.orElse(null);
        }
        return null;
    }

    private long countActivitiesThisWeek(List<Activity> activities) {
        LocalDate now = LocalDate.now();
        LocalDate weekStart = now.minusDays(now.getDayOfWeek().getValue() - 1);
        return activities.stream()
                .filter(a -> !a.getActivityDate().isBefore(weekStart))
                .count();
    }

    private long countActivitiesThisMonth(List<Activity> activities) {
        LocalDate now = LocalDate.now();
        return activities.stream()
                .filter(a -> a.getActivityDate().getMonth() == now.getMonth()
                        && a.getActivityDate().getYear() == now.getYear())
                .count();
    }

    private int calculateStreak(List<Activity> activities) {
        if (activities.isEmpty()) return 0;
        
        LocalDate today = LocalDate.now();
        int streak = 0;
        
        for (int i = 0; i < 365; i++) {
            LocalDate checkDate = today.minusDays(i);
            boolean hasActivity = activities.stream()
                    .anyMatch(a -> a.getActivityDate().equals(checkDate));
            if (hasActivity) {
                streak++;
            } else if (i > 0) {
                break;
            }
        }
        return streak;
    }

    private int calculateLongestStreak(List<Activity> activities) {
        if (activities.isEmpty()) return 0;
        
        List<LocalDate> dates = activities.stream()
                .map(Activity::getActivityDate)
                .sorted()
                .distinct()
                .toList();
        
        int maxStreak = 1;
        int currentStreak = 1;
        
        for (int i = 1; i < dates.size(); i++) {
            if (dates.get(i).minusDays(1).equals(dates.get(i - 1))) {
                currentStreak++;
                maxStreak = Math.max(maxStreak, currentStreak);
            } else {
                currentStreak = 1;
            }
        }
        return maxStreak;
    }
}
