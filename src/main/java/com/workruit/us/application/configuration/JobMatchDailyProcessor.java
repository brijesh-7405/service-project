/**
 *
 */
package com.workruit.us.application.configuration;

import com.workruit.us.application.services.JobPostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.TimerTask;

@Slf4j
@Component
public class JobMatchDailyProcessor extends TimerTask {

    private static final int BACTH_SIZE = 100;
    JobPostService jobService;

    public JobMatchDailyProcessor(JobPostService jobService) {
        this.jobService = jobService;
    }

    @Override
    public void run() {
        try {
            //Sleep for 6hrs after starting the server
            Thread.sleep(43200000);
            jobService.runJobMatcher(BACTH_SIZE);
        } catch (Exception e) {
        }
    }
}
