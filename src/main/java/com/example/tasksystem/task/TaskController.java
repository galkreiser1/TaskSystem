package com.example.tasksystem.task;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TaskController {

    @GetMapping("/tasks")
    public ResponseEntity<?> getTasks() {
        return ResponseEntity.ok().build();
    }
}
