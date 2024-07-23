/**
 * 
 */
package com.workruit.us.application.dto;

import java.util.List;
import java.util.Objects;

import lombok.Data;

/**
 * @author Santosh Bhima
 *
 */
@Data
public class TeamProfileDTO {
	private Long recruiterId;
	private String name;
	private int totalProfilesUploaded;
	private List<JobAppliedFor> jobAppliedFors;

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TeamProfileDTO other = (TeamProfileDTO) obj;
		return Objects.equals(recruiterId, other.recruiterId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(recruiterId);
	}

}
