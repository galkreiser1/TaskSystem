package com.example.tasksystem.task;

import com.example.tasksystem.account.Account;
import com.example.tasksystem.account.AccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final AccountRepository accountRepository;

    public TaskService(TaskRepository taskRepository, AccountRepository accountRepository) {
        this.taskRepository = taskRepository;
        this.accountRepository = accountRepository;
    }

    public TaskResponse createTask(CreateTaskRequest request, String authorEmail) {
        Account account = accountRepository.findByEmail(authorEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Author not found"));

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(TaskStatus.CREATED);
        task.setAuthor(account);

        return toTaskResponse(taskRepository.save(task));
    }

    private TaskResponse toTaskResponse(Task task) {
        TaskResponse response = new TaskResponse();
        response.setId(String.valueOf(task.getId()));
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        response.setStatus(task.getStatus().name());
        response.setAuthor(task.getAuthor().getEmail());
        return response;
    }

    public List<TaskResponse> getAllTasks() {
        List<Task> tasks = taskRepository.findAllByOrderByIdDesc();
        return tasks.stream()
                .map(this::toTaskResponse)
                .collect(Collectors.toList());
    }

    public List<TaskResponse> getTasksByAuthor(String authorEmail) {
        List<Task> tasks = taskRepository.findByAuthorEmailIgnoreCaseOrderByIdDesc(authorEmail);
        return tasks.stream()
                .map(this::toTaskResponse)
                .collect(Collectors.toList());
    }

}
