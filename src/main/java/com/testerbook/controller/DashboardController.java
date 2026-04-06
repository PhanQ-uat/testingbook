package com.testerbook.controller;

import com.testerbook.model.*;
import com.testerbook.repository.*;
import com.testerbook.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DashboardController {

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private TrainingPostRepository trainingPostRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Get comprehensive dashboard overview data
     */
    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getDashboardOverview(HttpServletRequest request) {
        User user = getCurrentUser(request);
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }

        Map<String, Object> overview = new HashMap<>();
        
        // User stats
        overview.put("user", getUserSummary(user));
        
        // Activity stats
        overview.put("activityStats", getActivityStats(user));
        
        // Training post stats
        overview.put("postStats", getPostStats(user));
        
        // Phase progress
        overview.put("phaseProgress", getPhaseProgress(user));
        
        // Recent activity heatmap data
        overview.put("activityHeatmap", getActivityHeatmap(user));
        
        // Recent posts
        overview.put("recentPosts", getRecentPosts(user));
        
        // Gamification data
        overview.put("gamification", getGamificationData(user));
        
        return ResponseEntity.ok(overview);
    }

    /**
     * Get activity data for the chart (last 30 days)
     */
    @GetMapping("/activity-chart")
    public ResponseEntity<List<Map<String, Object>>> getActivityChart(HttpServletRequest request) {
        User user = getCurrentUser(request);
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }

        List<Activity> activities = activityRepository.findByUserId(user.getId());
        List<Map<String, Object>> chartData = new ArrayList<>();
        
        LocalDate today = LocalDate.now();
        
        for (int i = 29; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            
            long activityCount = activities.stream()
                    .filter(a -> a.getActivityDate().equals(date))
                    .count();
            
            long postCount = trainingPostRepository.findAll().stream()
                    .filter(p -> p.getAuthor().getId().equals(user.getId()))
                    .filter(p -> p.getPostDate().equals(date))
                    .count();
            
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", date.toString());
            dayData.put("dayLabel", date.getDayOfMonth() + "/" + (date.getMonthValue()));
            dayData.put("activities", activityCount);
            dayData.put("posts", postCount);
            
            chartData.add(dayData);
        }
        
        return ResponseEntity.ok(chartData);
    }

    /**
     * Get learning phase statistics
     */
    @GetMapping("/phase-stats")
    public ResponseEntity<Map<String, Object>> getPhaseStats(HttpServletRequest request) {
        User user = getCurrentUser(request);
        if (user == null) {
            return ResponseEntity.badRequest().build();
        }

        List<TrainingPost> posts = trainingPostRepository.findAll().stream()
                .filter(p -> p.getAuthor().getId().equals(user.getId()))
                .toList();
        
        Map<String, Object> phaseStats = new HashMap<>();
        
        for (LearningPhase phase : LearningPhase.values()) {
            long count = posts.stream()
                    .filter(p -> p.getPhase() == phase)
                    .count();
            
            Map<String, Object> phaseData = new HashMap<>();
            phaseData.put("count", count);
            phaseData.put("percentage", posts.isEmpty() ? 0 : (count * 100 / posts.size()));
            phaseData.put("completed", count >= 5); // Assume 5 posts = phase completed
            
            phaseStats.put(phase.name(), phaseData);
        }
        
        return ResponseEntity.ok(phaseStats);
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

    private Map<String, Object> getUserSummary(User user) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("id", user.getId());
        summary.put("username", user.getUsername());
        summary.put("fullName", (user.getFirstName() != null ? user.getFirstName() : "") + 
                (user.getLastName() != null ? " " + user.getLastName() : ""));
        summary.put("role", user.getRole());
        summary.put("memberSince", user.getCreatedAt());
        summary.put("daysActive", ChronoUnit.DAYS.between(user.getCreatedAt().toLocalDate(), LocalDate.now()));
        return summary;
    }

    private Map<String, Object> getActivityStats(User user) {
        List<Activity> activities = activityRepository.findByUserId(user.getId());
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("total", activities.size());
        stats.put("thisWeek", countActivitiesThisWeek(activities));
        stats.put("thisMonth", countActivitiesThisMonth(activities));
        stats.put("currentStreak", calculateStreak(activities));
        stats.put("longestStreak", calculateLongestStreak(activities));
        
        return stats;
    }

    private Map<String, Object> getPostStats(User user) {
        List<TrainingPost> posts = trainingPostRepository.findAll().stream()
                .filter(p -> p.getAuthor().getId().equals(user.getId()))
                .toList();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", posts.size());
        stats.put("published", posts.stream().filter(p -> p.getStatus() == PostStatus.PUBLISHED).count());
        stats.put("drafts", posts.stream().filter(p -> p.getStatus() == PostStatus.DRAFT).count());
        
        // Posts by category
        Map<String, Long> byCategory = new HashMap<>();
        for (PostCategory cat : PostCategory.values()) {
            long count = posts.stream().filter(p -> p.getCategory() == cat).count();
            byCategory.put(cat.name(), count);
        }
        stats.put("byCategory", byCategory);
        
        return stats;
    }

    private Map<String, Object> getPhaseProgress(User user) {
        List<TrainingPost> posts = trainingPostRepository.findAll().stream()
                .filter(p -> p.getAuthor().getId().equals(user.getId()))
                .toList();
        
        Map<String, Object> progress = new HashMap<>();
        LearningPhase[] phases = LearningPhase.values();
        
        for (int i = 0; i < phases.length; i++) {
            LearningPhase phase = phases[i];
            long count = posts.stream().filter(p -> p.getPhase() == phase).count();
            
            Map<String, Object> phaseData = new HashMap<>();
            phaseData.put("name", phase.name());
            phaseData.put("postsCount", count);
            phaseData.put("progress", Math.min(100, count * 20)); // 5 posts = 100%
            phaseData.put("completed", count >= 5);
            phaseData.put("current", i == 1 && count < 5); // Phase 2 is current if not completed
            
            progress.put(phase.name(), phaseData);
        }
        
        return progress;
    }

    private List<Map<String, Object>> getActivityHeatmap(User user) {
        List<Activity> activities = activityRepository.findByUserId(user.getId());
        List<Map<String, Object>> heatmap = new ArrayList<>();
        
        LocalDate today = LocalDate.now();
        
        // Generate last 16 weeks (112 days) for heatmap
        for (int week = 0; week < 16; week++) {
            for (int day = 0; day < 7; day++) {
                LocalDate date = today.minusDays((15 - week) * 7 + (6 - day));
                
                long count = activities.stream()
                        .filter(a -> a.getActivityDate().equals(date))
                        .count();
                
                Map<String, Object> cell = new HashMap<>();
                cell.put("date", date.toString());
                cell.put("week", week);
                cell.put("day", day);
                cell.put("count", count);
                cell.put("level", Math.min(4, (int) count)); // 0-4 level
                
                heatmap.add(cell);
            }
        }
        
        return heatmap;
    }

    private List<Map<String, Object>> getRecentPosts(User user) {
        return trainingPostRepository.findAll().stream()
                .filter(p -> p.getAuthor().getId().equals(user.getId()))
                .sorted((a, b) -> b.getPostDate().compareTo(a.getPostDate()))
                .limit(5)
                .map(p -> {
                    Map<String, Object> post = new HashMap<>();
                    post.put("id", p.getId());
                    post.put("title", p.getTitle());
                    post.put("phase", p.getPhase());
                    post.put("category", p.getCategory());
                    post.put("status", p.getStatus());
                    post.put("date", p.getPostDate().toString());
                    return post;
                })
                .toList();
    }

    private Map<String, Object> getGamificationData(User user) {
        Map<String, Object> gamification = new HashMap<>();
        
        // Calculate points based on activities and posts
        List<Activity> activities = activityRepository.findByUserId(user.getId());
        List<TrainingPost> posts = trainingPostRepository.findAll().stream()
                .filter(p -> p.getAuthor().getId().equals(user.getId()))
                .toList();
        
        int points = activities.size() * 10 + posts.size() * 50;
        int streak = calculateStreak(activities);
        
        // Badges
        List<Map<String, Object>> badges = new ArrayList<>();
        
        badges.add(createBadge("First Step", "🏃", posts.size() > 0, "Create your first post"));
        badges.add(createBadge("Writer", "📝", posts.size() >= 5, "Create 5 posts"));
        badges.add(createBadge("Streak 7", "🔥", streak >= 7, "7-day activity streak"));
        badges.add(createBadge("Streak 30", "🌟", streak >= 30, "30-day activity streak"));
        badges.add(createBadge("Bookworm", "📚", posts.size() >= 20, "Create 20 posts"));
        badges.add(createBadge("Rising Star", "⭐", points >= 500, "Earn 500 points"));
        badges.add(createBadge("Goal Setter", "🎯", activities.size() >= 30, "Log 30 activities"));
        badges.add(createBadge("Champion", "🏆", points >= 1000, "Earn 1000 points"));
        
        gamification.put("points", points);
        gamification.put("streak", streak);
        gamification.put("badges", badges);
        gamification.put("unlockedBadges", badges.stream().filter(b -> (Boolean) b.get("unlocked")).count());
        gamification.put("totalBadges", badges.size());
        
        return gamification;
    }

    private Map<String, Object> createBadge(String name, String icon, boolean unlocked, String description) {
        Map<String, Object> badge = new HashMap<>();
        badge.put("name", name);
        badge.put("icon", icon);
        badge.put("unlocked", unlocked);
        badge.put("description", description);
        return badge;
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
