package com.workruit.us.application;

import java.io.IOException;
import java.util.Calendar;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Timer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.workruit.us.application.configuration.JobMatchDailyProcessor;
import com.workruit.us.application.configuration.JobMatchProcessor;
import com.workruit.us.application.dto.JobForQueue;
import com.workruit.us.application.services.JobPostService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WorkruitUSApplication implements CommandLineRunner {

    @Autowired
    JobPostService jobService;

    public static void main(String[] args) {
        SpringApplication.run(WorkruitUSApplication.class, args);
    }

    @Bean
    FirebaseMessaging firebaseMessaging() throws IOException {
        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(new ClassPathResource("firebase-service-account.json").getInputStream());
        FirebaseOptions firebaseOptions = FirebaseOptions.builder().setCredentials(googleCredentials).build();
        FirebaseApp app = FirebaseApp.initializeApp(firebaseOptions, "WRK-US-DEV");
        return FirebaseMessaging.getInstance(app);
    }

    @Bean
    public BlockingQueue<JobForQueue> jobMatchQueue() {
        return new LinkedBlockingQueue<JobForQueue>();
    }

    @Override
    public void run(String... args) throws Exception {
        BlockingQueue<JobForQueue> blockingQueue = jobMatchQueue();
        new Thread(new JobMatchProcessor(blockingQueue, jobService)).start();
        // Scheduler to run daily at 1 AM
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 1);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        // every night at 1am you run your task
        Timer timer = new Timer();
        timer.schedule(new JobMatchDailyProcessor(jobService), today.getTime(),
                TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS)); // period: 1 day }
    }
}
