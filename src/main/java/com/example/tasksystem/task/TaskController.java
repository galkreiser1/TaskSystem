package com.example.tasksystem.task;

import com.example.tasksystem.comment.CommentRequest;
import com.example.tasksystem.comment.CommentResponse;
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
    public ResponseEntity<List<TaskResponse>> getTasks(@RequestParam (required = false) String author, @RequestParam (required = false) String assignee) {
        List<TaskResponse> tasks;

        if (author != null && assignee != null) {
            tasks = taskService.getTasksByAuthorAndAssignee(author, assignee);
        } else if (author != null) {
            tasks = taskService.getTasksByAuthor(author);
        } else if (assignee != null) {
            tasks = taskService.getTasksByAssignee(assignee);
        } else {
            tasks = taskService.getAllTasks();
        }
        return ResponseEntity.ok(tasks);
    }

    @PostMapping("/tasks")
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody CreateTaskRequest request, Authentication authentication) {
        String authorEmail = authentication.getName();
        TaskResponse response = taskService.createTask(request, authorEmail);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/tasks/{taskId}/assign")
    public ResponseEntity<TaskResponse> assignTask(@PathVariable Long taskId, @RequestBody AssignTaskRequest request, Authentication authentication) {
        String assigneeEmail = authentication.getName();
        TaskResponse response = taskService.assignTask(taskId, request, assigneeEmail);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/tasks/{taskId}/status")
    public ResponseEntity<TaskResponse> updateTaskStatus(@PathVariable Long taskId, @RequestBody UpdateTaskStatusRequest request, Authentication authentication) {
        String userEmail = authentication.getName();
        TaskResponse response = taskService.updateTaskStatus(taskId, request, userEmail);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/tasks/{taskId}/comments")
    public ResponseEntity<?> createComment(@PathVariable Long taskId, @Valid @RequestBody CommentRequest request, Authentication authentication) {
        String authorEmail = authentication.getName();
        taskService.createComment(taskId, request, authorEmail);
        return ResponseEntity.ok().build();
    }

    @GetMapping ("/tasks/{taskId}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long taskId) {
        List<CommentResponse> comments = taskService.getComments(taskId);
        return ResponseEntity.ok(comments);
    }


}
