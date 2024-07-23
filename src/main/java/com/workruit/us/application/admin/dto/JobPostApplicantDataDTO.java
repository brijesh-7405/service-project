package com.workruit.us.application.admin.dto;

import com.workruit.us.application.dto.ApplicantDTO;
import com.workruit.us.application.dto.InterviewDTO;
import com.workruit.us.application.dto.InterviewFeedbackDTO;
import com.workruit.us.application.dto.OfferDetailsDTO;
import lombok.Data;

@Data
public class JobPostApplicantDataDTO {
    private ApplicantDTO applicantDTO;
    private InterviewDTO interviewDTO;
    private InterviewFeedbackDTO interviewFeedbackDTO;
    private OfferDetailsDTO offerDetailsDTO;
}
