package com.example.tasksystem.task;

import com.example.tasksystem.account.Account;
import com.example.tasksystem.account.AccountRepository;
import com.example.tasksystem.comment.Comment;
import com.example.tasksystem.comment.CommentRepository;
import com.example.tasksystem.comment.CommentRequest;
import com.example.tasksystem.comment.CommentResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final AccountRepository accountRepository;
    private final CommentRepository commentRepository;

    public TaskService(TaskRepository taskRepository, AccountRepository accountRepository, CommentRepository commentRepository) {
        this.taskRepository = taskRepository;
        this.accountRepository = accountRepository;
        this.commentRepository = commentRepository;
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

        response.setTotalComments(commentRepository.countByTaskId(task.getId()));
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

    public void createComment(Long taskId, CommentRequest request, String authorEmail) {
        Comment comment = new Comment();
        comment.setText(request.getText());
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        comment.setTask(task);
        Account author = accountRepository.findByEmail(authorEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Author not found"));
        comment.setAuthor(author);

        commentRepository.save(comment);
    }

    private CommentResponse toCommentResponse(Comment comment) {
        CommentResponse response = new CommentResponse();
        response.setId(String.valueOf(comment.getId()));
        response.setText(comment.getText());
        response.setAuthor(comment.getAuthor().getEmail());
        response.setTaskId(String.valueOf(comment.getTask().getId()));
        return response;
    }

    public List<CommentResponse> getComments(Long taskId) {
        taskRepository.findById(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
        List<Comment> comments = commentRepository.findByTaskIdOrderByIdDesc(taskId);
        return comments.stream()
                .map(this::toCommentResponse)
                .collect(Collectors.toList());
    }
}
