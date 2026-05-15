package com.example.tasksystem.task;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findAllByOrderByIdDesc();
    List<Task> findByAuthorEmailIgnoreCaseOrderByIdDesc(String email);

}
