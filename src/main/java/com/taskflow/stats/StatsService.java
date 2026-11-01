package com.taskflow.stats;

import com.taskflow.task.Task;
import com.taskflow.task.TaskRepository;
import com.taskflow.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatsService {

    private final TaskRepository taskRepository;

    public Map<String, Object> getSummary(User user) {
        Long userId = user.getId();
        long totalTasks = taskRepository.countByUserId(userId);
        long completedTasks = taskRepository.countCompletedByUserId(userId);
        long totalXp = taskRepository.sumXpByUserId(userId);

        LocalDate today = LocalDate.now();
        LocalDateTime startOfToday = today.atStartOfDay();
        long todayCompleted = taskRepository.findCompletedSince(userId, startOfToday).size();

        int streak = computeStreak(userId);
        double completionPct = totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0;

        return Map.of(
            "totalTasks", totalTasks,
            "completedTasks", completedTasks,
            "totalXp", totalXp,
            "currentStreak", streak,
            "todayCompleted", todayCompleted,
            "completionPct", Math.round(completionPct * 10.0) / 10.0
        );
    }

    public Map<String, Object> getStreakData(User user) {
        int streak = computeStreak(user.getId());
        List<LocalDate> streakDates = getStreakDates(user.getId());
        int longest = computeLongestStreak(user.getId());

        return Map.of(
            "currentStreak", streak,
            "longestStreak", longest,
            "streakDates", streakDates
        );
    }

    private int computeStreak(Long userId) {
        List<Task> completed = taskRepository.findAllCompletedByUserId(userId);
        Set<LocalDate> completedDates = completed.stream()
            .filter(t -> t.getCompletedAt() != null)
            .map(t -> t.getCompletedAt().toLocalDate())
            .collect(Collectors.toSet());

        if (completedDates.isEmpty()) return 0;

        LocalDate day = LocalDate.now();
        int streak = 0;

        while (completedDates.contains(day)) {
            streak++;
            day = day.minusDays(1);
        }

        // also count yesterday if today has no completions yet
        if (streak == 0) {
            day = LocalDate.now().minusDays(1);
            while (completedDates.contains(day)) {
                streak++;
                day = day.minusDays(1);
            }
        }

        return streak;
    }

    private int computeLongestStreak(Long userId) {
        List<Task> completed = taskRepository.findAllCompletedByUserId(userId);
        TreeSet<LocalDate> dates = completed.stream()
            .filter(t -> t.getCompletedAt() != null)
            .map(t -> t.getCompletedAt().toLocalDate())
            .collect(Collectors.toCollection(TreeSet::new));

        if (dates.isEmpty()) return 0;

        int longest = 1, current = 1;
        LocalDate prev = null;

        for (LocalDate date : dates) {
            if (prev != null && date.equals(prev.plusDays(1))) {
                current++;
                longest = Math.max(longest, current);
            } else if (prev != null && !date.equals(prev)) {
                current = 1;
            }
            prev = date;
        }
        return longest;
    }

    private List<LocalDate> getStreakDates(Long userId) {
        List<Task> completed = taskRepository.findAllCompletedByUserId(userId);
        return completed.stream()
            .filter(t -> t.getCompletedAt() != null)
            .map(t -> t.getCompletedAt().toLocalDate())
            .distinct()
            .sorted()
            .toList();
    }
}
