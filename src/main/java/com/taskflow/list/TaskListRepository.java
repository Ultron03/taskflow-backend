package com.taskflow.list;

import com.taskflow.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TaskListRepository extends JpaRepository<TaskList, Long> {

    List<TaskList> findByOwnerOrderBySortOrderAscCreatedAtAsc(User owner);

    Optional<TaskList> findByIdAndOwner(Long id, User owner);

    @Query("SELECT tl FROM TaskList tl WHERE tl.id = :id AND tl.owner = :owner")
    Optional<TaskList> findByIdAndOwnerStrict(Long id, User owner);
}
