package com.taskflow.task;

import com.taskflow.exception.ForbiddenException;
import com.taskflow.list.TaskList;
import com.taskflow.list.TaskListService;
import com.taskflow.task.dto.TaskRequest;
import com.taskflow.task.dto.TaskResponse;
import com.taskflow.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskListService taskListService;

    public List<TaskResponse> getTasksForList(Long listId, User user, Boolean completed, Priority priority) {
        TaskList list = taskListService.findOwned(listId, user);

        List<Task> tasks;
        if (completed != null) {
            tasks = taskRepository.findByTaskListAndCompletedOrderBySortOrderAscCreatedAtAsc(list, completed);
        } else if (priority != null) {
            tasks = taskRepository.findByTaskListAndPriorityOrderBySortOrderAscCreatedAtAsc(list, priority);
        } else {
            tasks = taskRepository.findByTaskListOrderBySortOrderAscCreatedAtAsc(list);
        }
        return tasks.stream().map(TaskResponse::from).toList();
    }

    @Transactional
    public TaskResponse createTask(Long listId, User user, TaskRequest request) {
        TaskList list = taskListService.findOwned(listId, user);
        Task task = Task.builder()
            .taskList(list)
            .title(request.title())
            .notes(request.notes())
            .priority(request.priority())
            .difficulty(request.difficulty())
            .category(request.category())
            .tags(request.tags())
            .externalUrl(request.externalUrl())
            .xpValue(computeXp(request.difficulty(), request.xpValue()))
            .sortOrder(request.sortOrder())
            .build();
        log.info("Creating task '{}' in list {}", task.getTitle(), listId);
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse updateTask(Long taskId, User user, TaskRequest request) {
        Task task = findOwned(taskId, user);
        if (request.title() != null) task.setTitle(request.title());
        if (request.notes() != null) task.setNotes(request.notes());
        if (request.priority() != null) task.setPriority(request.priority());
        if (request.difficulty() != null) {
            task.setDifficulty(request.difficulty());
            task.setXpValue(computeXp(request.difficulty(), null));
        }
        if (request.category() != null) task.setCategory(request.category());
        if (request.tags() != null) task.setTags(request.tags());
        if (request.externalUrl() != null) task.setExternalUrl(request.externalUrl());
        if (request.sortOrder() != null) task.setSortOrder(request.sortOrder());
        if (request.completed() != null) {
            task.setCompleted(request.completed());
            task.setCompletedAt(request.completed() ? LocalDateTime.now() : null);
        }
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse toggleComplete(Long taskId, User user) {
        Task task = findOwned(taskId, user);
        boolean nowComplete = !Boolean.TRUE.equals(task.getCompleted());
        task.setCompleted(nowComplete);
        task.setCompletedAt(nowComplete ? LocalDateTime.now() : null);
        log.info("Task {} toggled to completed={}", taskId, nowComplete);
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public void deleteTask(Long taskId, User user) {
        Task task = findOwned(taskId, user);
        taskRepository.delete(task);
        log.info("Deleted task {}", taskId);
    }

    @Transactional
    public int bulkCreate(Long listId, User user, List<TaskRequest> requests) {
        TaskList list = taskListService.findOwned(listId, user);
        List<Task> tasks = requests.stream().map(req -> Task.builder()
            .taskList(list)
            .title(req.title())
            .notes(req.notes())
            .priority(req.priority())
            .difficulty(req.difficulty())
            .category(req.category())
            .tags(req.tags())
            .externalUrl(req.externalUrl())
            .xpValue(computeXp(req.difficulty(), req.xpValue()))
            .sortOrder(req.sortOrder())
            .build()
        ).toList();
        taskRepository.saveAll(tasks);
        log.info("Bulk created {} tasks in list {}", tasks.size(), listId);
        return tasks.size();
    }

    private Task findOwned(Long taskId, User user) {
        Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new ForbiddenException("Task not found"));
        if (!task.getTaskList().getOwner().getId().equals(user.getId())) {
            throw new ForbiddenException("Access denied to task " + taskId);
        }
        return task;
    }

    private int computeXp(Difficulty difficulty, Integer override) {
        if (override != null) return override;
        if (difficulty == null) return 10;
        return switch (difficulty) {
            case EASY -> 10;
            case MEDIUM -> 25;
            case HARD -> 50;
            case PROJECT -> 100;
            case CONCEPT -> 15;
        };
    }
}
