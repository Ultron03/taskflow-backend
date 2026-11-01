package com.taskflow.list;

import com.taskflow.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskListRepository extends JpaRepository<TaskList, Long> {

    List<TaskList> findByOwnerOrderBySortOrderAscCreatedAtAsc(User owner);

    Optional<TaskList> findByIdAndOwner(Long id, User owner);
}
