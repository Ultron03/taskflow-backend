package com.taskflow.task;

import com.taskflow.list.TaskList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByTaskListOrderBySortOrderAscCreatedAtAsc(TaskList taskList);

    List<Task> findByTaskListAndCompletedOrderBySortOrderAscCreatedAtAsc(TaskList taskList, Boolean completed);

    List<Task> findByTaskListAndPriorityOrderBySortOrderAscCreatedAtAsc(TaskList taskList, Priority priority);

    long countByTaskList(TaskList taskList);

    long countByTaskListAndCompletedTrue(TaskList taskList);

    void deleteByTaskList(TaskList taskList);

    @Query("SELECT t FROM Task t WHERE t.taskList.owner.id = :userId AND t.completed = true")
    List<Task> findAllCompletedByUserId(Long userId);

    @Query("SELECT t FROM Task t WHERE t.taskList.owner.id = :userId AND t.completed = true AND t.completedAt >= :since")
    List<Task> findCompletedSince(Long userId, LocalDateTime since);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.taskList.owner.id = :userId")
    long countByUserId(Long userId);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.taskList.owner.id = :userId AND t.completed = true")
    long countCompletedByUserId(Long userId);

    @Query("SELECT COALESCE(SUM(t.xpValue), 0) FROM Task t WHERE t.taskList.owner.id = :userId AND t.completed = true")
    long sumXpByUserId(Long userId);
}
