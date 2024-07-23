/**
 *
 */
package com.workruit.us.application.configuration;

import com.workruit.us.application.dto.JobForQueue;
import com.workruit.us.application.services.JobPostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;

/**
 * @author Mahesh
 */
@Slf4j
@Component
public class JobMatchProcessor implements Runnable {

    JobPostService jobService;

    BlockingQueue<JobForQueue> jobMatchRunnerQueue;

    public JobMatchProcessor(BlockingQueue<JobForQueue> jobMatchRunnerQueue, JobPostService jobService) {
        this.jobMatchRunnerQueue = jobMatchRunnerQueue;
        this.jobService = jobService;
    }

    @Override
    public void run() {
        while (true) {
            try {
                JobForQueue jobQueue = jobMatchRunnerQueue.take();
                if (jobQueue.getJobPostId() != null) {
                    //Call job matcher for user
                    //jobService.runJobMatcherForJobPost(jobQueue.getJobPostId(), 10);
                } else if (jobQueue.getApplicantId() != null) {
                    //Call profile matcher for job
                    //jobService.runJobMatcherForApplicant(jobQueue.getApplicantId(), 10);
                } else {
                    log.info("no message matching criteria in the queue");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

}
