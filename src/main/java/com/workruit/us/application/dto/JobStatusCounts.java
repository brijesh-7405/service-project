package com.workruit.us.application.dto;

import java.util.List;

import lombok.Data;

@Data
public class JobStatusCounts {

	private long shortlistedCount = 0;
	private long mactchedCount = 0;
	private long intervieedCount = 0;
	private long hiredCount = 0;

}
