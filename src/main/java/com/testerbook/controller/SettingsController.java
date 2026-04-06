package com.testerbook.controller;

import com.testerbook.model.SiteSettings;
import com.testerbook.repository.SiteSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    @Autowired
    private SiteSettingsRepository siteSettingsRepository;

    @GetMapping("/site-name")
    public ResponseEntity<String> getSiteName() {
        Optional<SiteSettings> setting = siteSettingsRepository.findByKey("siteName");
        return ResponseEntity.ok(setting.map(SiteSettings::getValue).orElse("testerBook"));
    }

    @GetMapping("/site-description")
    public ResponseEntity<String> getSiteDescription() {
        Optional<SiteSettings> setting = siteSettingsRepository.findByKey("siteDescription");
        return ResponseEntity.ok(setting.map(SiteSettings::getValue).orElse("Learning Management System for Software Testing Students"));
    }

    @GetMapping("/all")
    public ResponseEntity<Map<String, String>> getAllSettings() {
        Map<String, String> settings = new HashMap<>();
        
        siteSettingsRepository.findByKey("siteName")
            .ifPresent(s -> settings.put("siteName", s.getValue()));
        siteSettingsRepository.findByKey("siteDescription")
            .ifPresent(s -> settings.put("siteDescription", s.getValue()));
        siteSettingsRepository.findByKey("allowRegistration")
            .ifPresent(s -> settings.put("allowRegistration", s.getValue()));
        siteSettingsRepository.findByKey("requireApproval")
            .ifPresent(s -> settings.put("requireApproval", s.getValue()));
        siteSettingsRepository.findByKey("allowComments")
            .ifPresent(s -> settings.put("allowComments", s.getValue()));
        
        // Set defaults if not present
        if (!settings.containsKey("siteName")) settings.put("siteName", "testerBook");
        if (!settings.containsKey("siteDescription")) settings.put("siteDescription", "Learning Management System for Software Testing Students");
        if (!settings.containsKey("allowRegistration")) settings.put("allowRegistration", "true");
        if (!settings.containsKey("requireApproval")) settings.put("requireApproval", "false");
        if (!settings.containsKey("allowComments")) settings.put("allowComments", "true");
        
        return ResponseEntity.ok(settings);
    }

    @PostMapping("/save")
    public ResponseEntity<Void> saveSettings(@RequestBody Map<String, String> settings) {
        settings.forEach((key, value) -> {
            Optional<SiteSettings> existing = siteSettingsRepository.findByKey(key);
            if (existing.isPresent()) {
                SiteSettings s = existing.get();
                s.setValue(value);
                siteSettingsRepository.save(s);
            } else {
                siteSettingsRepository.save(new SiteSettings(key, value));
            }
        });
        return ResponseEntity.ok().build();
    }
}
