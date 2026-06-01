package com.example.tasksystem.tasks;

import com.example.tasksystem.account.AccountRepository;
import com.example.tasksystem.account.AccountService;
import com.example.tasksystem.account.RegisterRequest;
import com.example.tasksystem.comment.CommentRepository;
import com.example.tasksystem.task.TaskResponse;
import com.example.tasksystem.task.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TasksIntegrationTests {

    private static final String AUTH_EMAIL = "valid@email.com";
    private static final String AUTH_PASSWORD = "validpassword";
    private static final String OTHER_EMAIL = "other@email.com";
    private static final String OTHER_PASSWORD = "otherpassword";
    private static final String TASK_REQUEST_BODY = """
            {
              "title": "new task",
              "description": "a task for anyone"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountService accountService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private CommentRepository commentRepository;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        taskRepository.deleteAll();
        accountRepository.deleteAll();

        RegisterRequest request = new RegisterRequest();
        request.setEmail(AUTH_EMAIL);
        request.setPassword(AUTH_PASSWORD);
        accountService.register(request);

        RegisterRequest otherRequest = new RegisterRequest();
        otherRequest.setEmail(OTHER_EMAIL);
        otherRequest.setPassword(OTHER_PASSWORD);
        accountService.register(otherRequest);
    }

    @Test
    void getTasks_withoutAuthor_returnsEmptyThenCreatedTask() throws Exception {
        mockMvc.perform(get("/api/tasks")
                        .with(httpBasic(AUTH_EMAIL, AUTH_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        mockMvc.perform(post("/api/tasks")
                        .with(httpBasic(AUTH_EMAIL, AUTH_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TASK_REQUEST_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("new task"))
                .andExpect(jsonPath("$.description").value("a task for anyone"))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.author").value(AUTH_EMAIL));

        mockMvc.perform(get("/api/tasks")
                        .with(httpBasic(AUTH_EMAIL, AUTH_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("new task"))
                .andExpect(jsonPath("$[0].description").value("a task for anyone"))
                .andExpect(jsonPath("$[0].status").value("CREATED"))
                .andExpect(jsonPath("$[0].author").value(AUTH_EMAIL));
    }

    @Test
    void getTasks_byAuthor_returnsEmptyThenCreatedTask() throws Exception {
        mockMvc.perform(get("/api/tasks")
                        .param("author", AUTH_EMAIL.toUpperCase())
                        .with(httpBasic(AUTH_EMAIL, AUTH_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        mockMvc.perform(post("/api/tasks")
                        .with(httpBasic(AUTH_EMAIL, AUTH_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TASK_REQUEST_BODY))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/tasks")
                        .param("author", AUTH_EMAIL.toUpperCase())
                        .with(httpBasic(AUTH_EMAIL, AUTH_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("new task"))
                .andExpect(jsonPath("$[0].description").value("a task for anyone"))
                .andExpect(jsonPath("$[0].status").value("CREATED"))
                .andExpect(jsonPath("$[0].author").value(AUTH_EMAIL));
    }

    @Test
    void createTask_withValidRequest_returnsCreatedTaskResponse() throws Exception {
        mockMvc.perform(post("/api/tasks")
                        .with(httpBasic(AUTH_EMAIL, AUTH_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TASK_REQUEST_BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isString())
                .andExpect(jsonPath("$.title").value("new task"))
                .andExpect(jsonPath("$.description").value("a task for anyone"))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.author").value(AUTH_EMAIL));
    }

    @Test
    void createTask_withInvalidRequest_returnsBadRequest() throws Exception {
        String invalidRequestBody = """
                {
                  "title": "",
                  "description": "a task for anyone"
                }
                """;

        mockMvc.perform(post("/api/tasks")
                        .with(httpBasic(AUTH_EMAIL, AUTH_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTasks_returnsNewestTasksFirst() throws Exception {
        String firstTaskRequestBody = """
                {
                  "title": "older task",
                  "description": "first created task"
                }
                """;

        String secondTaskRequestBody = """
                {
                  "title": "newer task",
                  "description": "second created task"
                }
                """;

        mockMvc.perform(post("/api/tasks")
                        .with(httpBasic(AUTH_EMAIL, AUTH_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(firstTaskRequestBody))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/tasks")
                        .with(httpBasic(AUTH_EMAIL, AUTH_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(secondTaskRequestBody))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/tasks")
                        .with(httpBasic(AUTH_EMAIL, AUTH_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("newer task"))
                .andExpect(jsonPath("$[0].description").value("second created task"))
                .andExpect(jsonPath("$[1].title").value("older task"))
                .andExpect(jsonPath("$[1].description").value("first created task"));
    }

    @Test
    void assignTask_byAuthor_returnsUpdatedTask() throws Exception {
        TaskResponse createdTask = createTaskAs(AUTH_EMAIL, AUTH_PASSWORD);

        String assignRequestBody = """
                {
                  "assignee": "other@email.com"
                }
                """;

        mockMvc.perform(put("/api/tasks/{taskId}/assign", createdTask.getId())
                        .with(httpBasic(AUTH_EMAIL, AUTH_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(assignRequestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdTask.getId()))
                .andExpect(jsonPath("$.author").value(AUTH_EMAIL))
                .andExpect(jsonPath("$.assignee").value(OTHER_EMAIL));
    }

    @Test
    void assignTask_withNone_removesAssignee() throws Exception {
        TaskResponse createdTask = createTaskAs(AUTH_EMAIL, AUTH_PASSWORD);

        mockMvc.perform(put("/api/tasks/{taskId}/assign", createdTask.getId())
                        .with(httpBasic(AUTH_EMAIL, AUTH_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "assignee": "other@email.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignee").value(OTHER_EMAIL));

        mockMvc.perform(put("/api/tasks/{taskId}/assign", createdTask.getId())
                        .with(httpBasic(AUTH_EMAIL, AUTH_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "assignee": "none"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignee").value("none"));
    }

    @Test
    void assignTask_byNonAuthor_returnsForbidden() throws Exception {
        TaskResponse createdTask = createTaskAs(AUTH_EMAIL, AUTH_PASSWORD);

        mockMvc.perform(put("/api/tasks/{taskId}/assign", createdTask.getId())
                        .with(httpBasic(OTHER_EMAIL, OTHER_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "assignee": "other@email.com"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void getTasks_byAssignee_returnsAssignedTasks() throws Exception {
        TaskResponse createdTask = createTaskAs(AUTH_EMAIL, AUTH_PASSWORD);

        mockMvc.perform(put("/api/tasks/{taskId}/assign", createdTask.getId())
                        .with(httpBasic(AUTH_EMAIL, AUTH_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "assignee": "other@email.com"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/tasks")
                        .param("assignee", OTHER_EMAIL.toUpperCase())
                        .with(httpBasic(AUTH_EMAIL, AUTH_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(createdTask.getId()))
                .andExpect(jsonPath("$[0].assignee").value(OTHER_EMAIL));
    }

    @Test
    void getTasks_byAuthorAndAssignee_returnsFilteredTasks() throws Exception {
        TaskResponse createdTask = createTaskAs(AUTH_EMAIL, AUTH_PASSWORD);

        mockMvc.perform(put("/api/tasks/{taskId}/assign", createdTask.getId())
                        .with(httpBasic(AUTH_EMAIL, AUTH_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "assignee": "other@email.com"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/tasks")
                        .param("author", AUTH_EMAIL.toUpperCase())
                        .param("assignee", OTHER_EMAIL.toUpperCase())
                        .with(httpBasic(AUTH_EMAIL, AUTH_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(createdTask.getId()))
                .andExpect(jsonPath("$[0].author").value(AUTH_EMAIL))
                .andExpect(jsonPath("$[0].assignee").value(OTHER_EMAIL));
    }

    @Test
    void updateTaskStatus_byAuthor_returnsUpdatedTask() throws Exception {
        TaskResponse createdTask = createTaskAs(AUTH_EMAIL, AUTH_PASSWORD);

        mockMvc.perform(put("/api/tasks/{taskId}/status", createdTask.getId())
                        .with(httpBasic(AUTH_EMAIL, AUTH_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "COMPLETED"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdTask.getId()))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void updateTaskStatus_byUnauthorizedUser_returnsForbidden() throws Exception {
        TaskResponse createdTask = createTaskAs(AUTH_EMAIL, AUTH_PASSWORD);

        mockMvc.perform(put("/api/tasks/{taskId}/status", createdTask.getId())
                        .with(httpBasic(OTHER_EMAIL, OTHER_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "COMPLETED"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void createComment_withValidRequest_returnsOk() throws Exception {
        TaskResponse createdTask = createTaskAs(AUTH_EMAIL, AUTH_PASSWORD);

        mockMvc.perform(post("/api/tasks/{taskId}/comments", createdTask.getId())
                        .with(httpBasic(OTHER_EMAIL, OTHER_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "text": "I'll be happy to take it!"
                                }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void createComment_withInvalidRequest_returnsBadRequest() throws Exception {
        TaskResponse createdTask = createTaskAs(AUTH_EMAIL, AUTH_PASSWORD);

        mockMvc.perform(post("/api/tasks/{taskId}/comments", createdTask.getId())
                        .with(httpBasic(OTHER_EMAIL, OTHER_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "text": ""
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getComments_returnsNewestFirst() throws Exception {
        TaskResponse createdTask = createTaskAs(AUTH_EMAIL, AUTH_PASSWORD);

        mockMvc.perform(post("/api/tasks/{taskId}/comments", createdTask.getId())
                        .with(httpBasic(AUTH_EMAIL, AUTH_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "text": "older comment"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/tasks/{taskId}/comments", createdTask.getId())
                        .with(httpBasic(OTHER_EMAIL, OTHER_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "text": "newer comment"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/tasks/{taskId}/comments", createdTask.getId())
                        .with(httpBasic(AUTH_EMAIL, AUTH_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].text").value("newer comment"))
                .andExpect(jsonPath("$[0].author").value(OTHER_EMAIL))
                .andExpect(jsonPath("$[1].text").value("older comment"))
                .andExpect(jsonPath("$[1].author").value(AUTH_EMAIL));
    }

    @Test
    void getTasks_includesTotalComments() throws Exception {
        TaskResponse createdTask = createTaskAs(AUTH_EMAIL, AUTH_PASSWORD);

        mockMvc.perform(post("/api/tasks/{taskId}/comments", createdTask.getId())
                        .with(httpBasic(OTHER_EMAIL, OTHER_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "text": "first comment"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/tasks/{taskId}/comments", createdTask.getId())
                        .with(httpBasic(AUTH_EMAIL, AUTH_PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "text": "second comment"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/tasks")
                        .with(httpBasic(AUTH_EMAIL, AUTH_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(createdTask.getId()))
                .andExpect(jsonPath("$[0].total_comments").value(2));
    }

    private TaskResponse createTaskAs(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/tasks")
                        .with(httpBasic(email, password))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TASK_REQUEST_BODY))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readValue(result.getResponse().getContentAsString(), TaskResponse.class);
    }
}
