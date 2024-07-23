package com.workruit.us.application.controllers.utils;

import com.workruit.us.application.dto.FailedMessage;
import com.workruit.us.application.dto.Message;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ControllerUtils {

    @SuppressWarnings("rawtypes")
    public static ResponseEntity genericErrorMessage() {
        FailedMessage failedMessage = FailedMessage.builder()
                .msg(Message.builder().description("Internal Server Error").title("Failed").build()).status("Failed")
                .build();
        return new ResponseEntity<>(failedMessage, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @SuppressWarnings("rawtypes")
    public static ResponseEntity customErrorMessage(String description) {
        FailedMessage failedMessage = FailedMessage.builder()
                .msg(Message.builder().description(description).title("Failed").build()).status("Failed").build();
        return new ResponseEntity<>(failedMessage, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}