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

    @GetMapping
    public List<Task> all(@RequestParam(required = false) Task.Status status) {
        return status == null ? repo.findAll() : repo.findByStatus(status);
    }

    @GetMapping("/{id}")
    public Task one(@PathVariable Long id) {
        return repo.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Task create(@RequestBody Task t) {
        t.setId(null);
        Task saved = repo.save(t);
        log.info("Created task {} with priority {}", saved.getId(), saved.getPriority());
        return saved;
    }

    @PutMapping("/{id}")
    public Task update(@PathVariable Long id, @RequestBody Task in) {
        Task t = one(id);
        t.setTitle(in.getTitle());
        t.setDescription(in.getDescription());
        t.setPriority(in.getPriority());
        t.setStatus(in.getStatus());
        t.setDueDate(in.getDueDate());
        log.info("Updated task {} (status={})", id, t.getStatus());
        return repo.save(t);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        repo.deleteById(id);
        log.info("Deleted task {}", id);
    }
}
