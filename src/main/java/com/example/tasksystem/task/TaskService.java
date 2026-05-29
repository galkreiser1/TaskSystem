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
        response.setAssignee("none");
        if (task.getAssignee() != null) {
            response.setAssignee(task.getAssignee().getEmail());
        }
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

    public List<TaskResponse> getTasksByAssignee(String assigneeEmail) {
        List<Task> tasks = taskRepository.findByAssigneeEmailIgnoreCaseOrderByIdDesc(assigneeEmail);
        return tasks.stream()
                .map(this::toTaskResponse)
                .collect(Collectors.toList());
    }

    public List<TaskResponse> getTasksByAuthorAndAssignee(String author, String asignee) {
        List<Task> tasks = taskRepository.findByAuthorEmailIgnoreCaseAndAssigneeEmailIgnoreCaseOrderByIdDesc(author, asignee);
        return tasks.stream()
                .map(this::toTaskResponse)
                .collect(Collectors.toList());
    }

    public TaskResponse assignTask(Long taskId, AssignTaskRequest request, String currentUserEmail){

        String assigneeEmail = request.getAssignee().toLowerCase();

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        if (!task.getAuthor().getEmail().equalsIgnoreCase(currentUserEmail)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the author can assign the task");
        }

        if ("none".equalsIgnoreCase(assigneeEmail)) {
            task.setAssignee(null);
            return toTaskResponse(taskRepository.save(task));
        }

        if (!isValidEmail(assigneeEmail)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid assignee email");
        }

        Account assignee = accountRepository.findByEmail(assigneeEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignee not found"));

        task.setAssignee(assignee);
        return toTaskResponse(taskRepository.save(task));
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    }

    public TaskResponse updateTaskStatus(Long taskId, UpdateTaskStatusRequest request, String currentUserEmail) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));

        String newStatus = request.getStatus().toUpperCase();
        if (!isValidStatus(newStatus)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status value");
        }

        if (!task.getAuthor().getEmail().equalsIgnoreCase(currentUserEmail) &&
                (task.getAssignee() == null || !task.getAssignee().getEmail().equalsIgnoreCase(currentUserEmail))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the author or assignee can update the task status");
        }

        task.setStatus(TaskStatus.valueOf(newStatus));
        return toTaskResponse(taskRepository.save(task));
    }

    private boolean isValidStatus(String status) {
        try {
            TaskStatus.valueOf(status);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

}
