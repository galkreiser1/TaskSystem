package com.example.tasksystem.comment;

import jakarta.validation.constraints.NotBlank;

public class CommentRequest {

    @NotBlank
    private String text;

    public String getText() {
        return text;
    }
}
