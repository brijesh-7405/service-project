/**
 *
 */
package com.workruit.us.application.services;

import com.amazonaws.HttpMethod;
import com.workruit.us.application.utils.DateUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URL;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Santosh Bhima
 */
@Service
public class FileService {

    private @Autowired AWSService awsService;

    private @Autowired ApplicantService applicantService;

    @Value("${files.bucket}")
    private String filesBucket;

    @Value("${images.bucket}")
    private String imagesBucket;

    @Value("${csv.bucket}")
    private String csvBucket;


//    public String save(String multipartFile, Long userId) throws IOException {
//        String bucketName = filesBucket;
//        ObjectMetadata metadata = new ObjectMetadata();
//        metadata.setContentType(multipartFile.getContentType());
//        String key = RandomStringUtils.randomAlphanumeric(32);
//        PutObjectRequest request = new PutObjectRequest(bucketName, key, multipartFile.getInputStream(), metadata);
//        PutObjectResult putObjectResult = awsService.getS3Client().putObject(request);
//        System.out.println(putObjectResult.getETag());
//        return key;
//    }

    public String saveUserFiles(String key, Long userId, Long certificateID, String fileType) throws IOException {
        String bucketName = filesBucket;
        if (fileType != null && fileType.equalsIgnoreCase("profile-image")) {
            bucketName = imagesBucket;
        }

//        ObjectMetadata metadata = new ObjectMetadata();
//        metadata.setContentType(multipartFile.getContentType());
//        String key = RandomStringUtils.randomAlphanumeric(32);
//        PutObjectRequest request = new PutObjectRequest(bucketName, key, multipartFile.getInputStream(), metadata);
//        PutObjectResult putObjectResult = awsService.getS3Client().putObject(request);
        applicantService.saveUserFiles(userId, certificateID, fileType, key);
        return key;
    }


    public String get(String key) throws IOException {
        String bucketName = filesBucket;
        URL url = awsService.getS3Client().generatePresignedUrl(bucketName, key, DateUtils.next10Min());
        return Base64.getEncoder().encodeToString(IOUtils.toByteArray(url));
    }

    public String getCSV(String key) throws IOException {
        String bucketName = csvBucket;
        URL url = awsService.getS3Client().generatePresignedUrl(bucketName, key, DateUtils.next10Min());
        return Base64.getEncoder().encodeToString(IOUtils.toByteArray(url));
    }

    public String generateUrl(String fileName, HttpMethod httpMethod) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, 1); // Generated URL will be valid for 24 hours
        return awsService.getS3Client().generatePresignedUrl(filesBucket, fileName, calendar.getTime(), httpMethod).toString();
    }

    public String generateCsvUrl(String fileName, HttpMethod httpMethod) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, 1); // Generated URL will be valid for 24 hours
        return awsService.getS3Client().generatePresignedUrl(csvBucket, fileName, calendar.getTime(), httpMethod).toString();
    }
}
