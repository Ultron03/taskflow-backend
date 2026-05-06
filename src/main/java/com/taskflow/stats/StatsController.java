package com.taskflow.stats;

import com.taskflow.user.User;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/stats")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(statsService.getSummary(user));
    }

    @GetMapping("/streak")
    public ResponseEntity<Map<String, Object>> getStreak(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(statsService.getStreakData(user));
    }
}
