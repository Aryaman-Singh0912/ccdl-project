package com.ccd.task;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByUserId(String userId);
    List<Task> findByUserIdAndStatus(String userId, Task.Status status);
}
