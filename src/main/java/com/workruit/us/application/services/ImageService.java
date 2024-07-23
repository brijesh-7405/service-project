/**
 *
 */
package com.workruit.us.application.services;

import com.amazonaws.HttpMethod;
import com.amazonaws.util.StringUtils;
import com.workruit.us.application.repositories.CompanyRepository;
import com.workruit.us.application.repositories.ConsultancyRepository;
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
public class ImageService {

    private @Autowired AWSService awsService;
    private @Autowired CompanyRepository companyRepository;

    private @Autowired ConsultancyRepository consultancyRepository;

    @Value("${images.bucket}")
    private String imagesBucket;

    public String saveImage(String key, String role, Long companyId, Long consultancyId) throws IOException {
        String bucketName = imagesBucket;
//        ObjectMetadata metadata = new ObjectMetadata();
//        metadata.setContentType(multipartFile.getContentType());
//        metadata.setContentLength(multipartFile.getSize());
//        String key = RandomStringUtils.randomAlphanumeric(32);
//
//        PutObjectRequest request = new PutObjectRequest(bucketName, key, multipartFile.getInputStream(), metadata);
//        PutObjectResult putObjectResult = awsService.getS3Client().putObject(request);
//        System.out.println(putObjectResult.getETag());
        //if (role != null && role.equals("COMPANY_ADMIN")) {
        companyRepository.updateProfileImageKey(companyId, key);
//        } else if (role != null && role.equals("CONSULTANCY_ADMIN")) {
        consultancyRepository.updateProfileImageKey(consultancyId, key);
//        }
        return key;
    }

    public String getImage(String key) throws IOException {
        if (key != null && !StringUtils.isNullOrEmpty(key)) {
            String bucketName = imagesBucket;
            URL url = awsService.getS3Client().generatePresignedUrl(bucketName, key, DateUtils.next10Min());
            return Base64.getEncoder().encodeToString(IOUtils.toByteArray(url));
        }
        return null;
    }

    public String generateUrl(String fileName, HttpMethod httpMethod) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, 1); // Generated URL will be valid for 24 hours
        return awsService.getS3Client().generatePresignedUrl(imagesBucket, fileName, calendar.getTime(), httpMethod).toString();
    }


}
