package com.example.tasksystem.tasks;

import com.example.tasksystem.account.AccountRepository;
import com.example.tasksystem.account.AccountService;
import com.example.tasksystem.account.RegisterRequest;
import com.example.tasksystem.task.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TasksIntegrationTests {

    private static final String AUTH_EMAIL = "valid@email.com";
    private static final String AUTH_PASSWORD = "validpassword";
    private static final String TASK_REQUEST_BODY = """
            {
              "title": "new task",
              "description": "a task for anyone"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private AccountService accountService;

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        accountRepository.deleteAll();

        RegisterRequest request = new RegisterRequest();
        request.setEmail(AUTH_EMAIL);
        request.setPassword(AUTH_PASSWORD);
        accountService.register(request);
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
}
