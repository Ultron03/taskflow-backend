package com.taskflow.list;

import com.taskflow.exception.ForbiddenException;
import com.taskflow.task.TaskRepository;
import com.taskflow.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskListService {

    private final TaskListRepository taskListRepository;
    private final TaskRepository taskRepository;

    public List<Map<String, Object>> getAllLists(User user) {
        return taskListRepository.findByOwnerOrderBySortOrderAscCreatedAtAsc(user)
            .stream()
            .map(list -> toListResponse(list,
                taskRepository.countByTaskList(list),
                taskRepository.countByTaskListAndCompletedTrue(list)))
            .toList();
    }

    public TaskList createList(User user, String name, String description,
                               String color, String icon, ListCategory category) {
        TaskList list = TaskList.builder()
            .owner(user)
            .name(name)
            .description(description)
            .color(color)
            .icon(icon)
            .category(category)
            .build();
        log.info("Creating list '{}' for user {}", name, user.getEmail());
        return taskListRepository.save(list);
    }

    public Map<String, Object> getList(Long id, User user) {
        TaskList list = findOwned(id, user);
        long total = taskRepository.countByTaskList(list);
        long completed = taskRepository.countByTaskListAndCompletedTrue(list);
        return toListResponse(list, total, completed);
    }

    @Transactional
    public TaskList updateList(Long id, User user, Map<String, Object> updates) {
        TaskList list = findOwned(id, user);
        if (updates.containsKey("name")) list.setName((String) updates.get("name"));
        if (updates.containsKey("description")) list.setDescription((String) updates.get("description"));
        if (updates.containsKey("color")) list.setColor((String) updates.get("color"));
        if (updates.containsKey("icon")) list.setIcon((String) updates.get("icon"));
        if (updates.containsKey("sortOrder")) {
            Object val = updates.get("sortOrder");
            list.setSortOrder(val instanceof Integer i ? i : Integer.parseInt(val.toString()));
        }
        return taskListRepository.save(list);
    }

    @Transactional
    public void deleteList(Long id, User user) {
        TaskList list = findOwned(id, user);
        taskRepository.deleteByTaskList(list);
        taskListRepository.delete(list);
        log.info("Deleted list {} for user {}", id, user.getEmail());
    }

    public TaskList findOwned(Long id, User user) {
        return taskListRepository.findByIdAndOwner(id, user)
            .orElseThrow(() -> new ForbiddenException("List not found or access denied"));
    }

    private Map<String, Object> toListResponse(TaskList list, long taskCount, long completedCount) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", list.getId());
        m.put("name", list.getName());
        m.put("description", list.getDescription() != null ? list.getDescription() : "");
        m.put("color", list.getColor() != null ? list.getColor() : "#6366F1");
        m.put("icon", list.getIcon() != null ? list.getIcon() : "");
        m.put("category", list.getCategory() != null ? list.getCategory().name() : "CUSTOM");
        m.put("sortOrder", list.getSortOrder() != null ? list.getSortOrder() : 0);
        m.put("taskCount", taskCount);
        m.put("completedCount", completedCount);
        m.put("createdAt", list.getCreatedAt());
        m.put("updatedAt", list.getUpdatedAt());
        return m;
    }
}
