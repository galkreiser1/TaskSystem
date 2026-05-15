package com.example.tasksystem.task;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/tasks")
    public ResponseEntity<List<TaskResponse>> getTasks(@RequestParam (required = false) String author) {
        List<TaskResponse> tasks = (author == null) ? taskService.getAllTasks() : taskService.getTasksByAuthor(author);
        return ResponseEntity.ok(tasks);
    }

    @PostMapping("/tasks")
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody CreateTaskRequest request, Authentication authentication) {
        String authorEmail = authentication.getName();
        TaskResponse response = taskService.createTask(request, authorEmail);
        return ResponseEntity.ok(response);
    }
}
