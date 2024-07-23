package com.workruit.us.application.dto;
import java.util.List;

import com.workruit.us.application.dto.InvitedUsedDTO;
import com.workruit.us.application.dto.UserDTO;

import lombok.Data;

@Data
public class InvitedUsersCountDTO {
	private long pendingUsesCount;
	private long activatedUsersCount;

}
