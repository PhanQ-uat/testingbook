package com.testerbook.repository;

import com.testerbook.model.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {
    List<Activity> findByUserId(Long userId);
    List<Activity> findByActivityDate(LocalDate activityDate);
    
    @Query("SELECT a FROM Activity a WHERE a.user.id = :userId ORDER BY a.activityDate DESC")
    List<Activity> findByUserIdOrderByDateDesc(@Param("userId") Long userId);
    
    @Query("SELECT a FROM Activity a ORDER BY a.activityDate DESC")
    List<Activity> findAllOrderByDateDesc();
}
