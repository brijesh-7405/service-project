package com.workruit.us.application.dto;

import javax.validation.constraints.NotNull;

import lombok.Data;
import java.util.List;

@Data
public class ConBulkApplyInfoDTO {

	@NotNull
	private String action;

	private List<ConsBulkApplyDTO> consBulkApplyDTO;
}
