/**
 *
 */
package com.workruit.us.application.services;

import com.workruit.us.application.dto.AlertDTO;
import com.workruit.us.application.dto.AlertsResponse;
import com.workruit.us.application.models.Alert;
import com.workruit.us.application.models.User;
import com.workruit.us.application.repositories.AlertRepository;
import com.workruit.us.application.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Santosh Bhima
 */
@Component
public class AlertService {
    private @Autowired AlertRepository alertRepository;
    private @Autowired UserRepository userRepository;

    public AlertsResponse alerts(Long consultancyId, int page, int size, Long userId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("alertId").descending());
        Page<Alert> alerts = null;
        if (userId == null) {
            alerts = alertRepository.findByConsultancyId(consultancyId, pageable);
        } else {
            User user = userRepository.getById(userId);
            if (user.isEnabled())
                alerts = alertRepository.findByConsultancyIdAndUserId(consultancyId, userId, pageable);
            else
                alerts = alertRepository.findByConsultancyId(consultancyId, pageable);
        }
        List<AlertDTO> alertDTOs = new ArrayList<>();
        AlertsResponse alertsResponse = new AlertsResponse();
        for (Alert alert : alerts.getContent()) {
            AlertDTO alertDTO = new AlertDTO();
            alertDTO.setAlertId(alert.getAlertId());
            alertDTO.setMessage(alert.getMessage());
            alertDTO.setUserId(alert.getUserId());
            if (alert.getUserId() != null) {
                User user = userRepository.findById(alert.getUserId()).get();
                alertDTO.setUsername(user.getFirstName() + " " + user.getLastName());
            } else {
                alertDTO.setUsername("");
            }
            Date date = alert.getCreatedDate();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh.mm aa | dd MMM yyyy");
            String outputDate = simpleDateFormat.format(date);
            alertDTO.setDate(outputDate);
            alertDTOs.add(alertDTO);
        }
//        List<AlertDTO> alertNoDuplicatesDTOs = new ArrayList<>();
//        for (AlertDTO alert : alertDTOs) {
//            boolean isFound = false;
//            // check if the event name exists in noRepeat
//            for (AlertDTO e : alertNoDuplicatesDTOs) {
//                if (e.getMessage().equals(alert.getMessage()) || (e.equals(alert))) {
//                    isFound = true;
//                    break;
//                }
//            }
//            if (!isFound) alertNoDuplicatesDTOs.add(alert);
//        }
        alertsResponse.setAlertDTOs(alertDTOs);
        alertsResponse.setTotalCount(alerts.getTotalElements());
        alertsResponse.setTotalPages(alerts.getTotalPages());
        return alertsResponse;
    }

    public void saveAlertInfo(Long userId, String message, Long consultantId) {

        Alert alert = new Alert();
        alert.setCreatedDate(new Date());
        alert.setMessage(message);
        alert.setUserId(userId);
        alert.setConsultancyId(consultantId);

        alert.setUpdatedDate(new Date());
        alertRepository.save(alert);
    }
}
