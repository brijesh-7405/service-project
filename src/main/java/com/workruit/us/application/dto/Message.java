package com.workruit.us.application.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Message {
	private String description;
	private String title;
}
