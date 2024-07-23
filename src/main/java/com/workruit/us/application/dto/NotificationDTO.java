package com.workruit.us.application.dto;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class NotificationDTO {
	private String subject;
	private String content;
	private Map<String, String> data=new HashMap<String, String>();
	private String image;
}