package com.ccd.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    private static final Logger log = LoggerFactory.getLogger(TaskController.class);
    private final TaskRepository repo;

    public TaskController(TaskRepository repo) {
        this.repo = repo;
    }

    /** Finds a task only if it belongs to this user; otherwise behaves as if it does not exist. */
    private Task owned(Long id, String userId) {
        return repo.findById(id)
                .filter(t -> userId.equals(t.getUserId()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
    }

    @GetMapping
    public List<Task> all(@RequestAttribute("userId") String userId,
                          @RequestParam(required = false) Task.Status status) {
        return status == null ? repo.findByUserId(userId) : repo.findByUserIdAndStatus(userId, status);
    }

    @GetMapping("/{id}")
    public Task one(@RequestAttribute("userId") String userId, @PathVariable Long id) {
        return owned(id, userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Task create(@RequestAttribute("userId") String userId, @RequestBody Task t) {
        t.setId(null);
        t.setUserId(userId);
        Task saved = repo.save(t);
        log.info("User {} created task {} with priority {}", userId, saved.getId(), saved.getPriority());
        return saved;
    }

    @PutMapping("/{id}")
    public Task update(@RequestAttribute("userId") String userId, @PathVariable Long id,
                       @RequestBody Task in) {
        Task t = owned(id, userId);
        t.setTitle(in.getTitle());
        t.setDescription(in.getDescription());
        t.setPriority(in.getPriority());
        t.setStatus(in.getStatus());
        t.setDueDate(in.getDueDate());
        log.info("User {} updated task {} (status={})", userId, id, t.getStatus());
        return repo.save(t);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@RequestAttribute("userId") String userId, @PathVariable Long id) {
        owned(id, userId);
        repo.deleteById(id);
        log.info("User {} deleted task {}", userId, id);
    }
}
