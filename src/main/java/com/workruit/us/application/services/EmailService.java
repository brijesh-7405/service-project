/**
 *
 */
package com.workruit.us.application.services;

import com.microtripit.mandrillapp.lutung.MandrillApi;
import com.microtripit.mandrillapp.lutung.model.MandrillApiError;
import com.microtripit.mandrillapp.lutung.view.MandrillMessage;
import com.microtripit.mandrillapp.lutung.view.MandrillMessage.MergeVar;
import com.microtripit.mandrillapp.lutung.view.MandrillMessage.Recipient;
import com.microtripit.mandrillapp.lutung.view.MandrillMessageStatus;
import com.microtripit.mandrillapp.lutung.view.MandrillTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Santosh Bhima
 */
@Slf4j
@Service
public class EmailService {
    @Value("${mail.sender.host}")
    private String host;
    @Value("${mail.sender.port}")
    private String port;
    @Value("${mail.sender.sender}")
    private String sender;
    @Value("${mail.sender.apikey}")
    private String apiKey;

    @Value("${mail.send.flag}")
    private boolean mailSendFlag;

    public void sendMail(String toName, String toEmail, String templateName, List<MergeVar> globalMergeVars, String content)
            throws MandrillApiError, IOException {
        log.info("Mail Chimp Configuration key is  ::" + apiKey);
        MandrillApi mandrillApi = new MandrillApi(apiKey);

        Map mapObject = new HashMap();

        MandrillTemplate mandrillTemplate = null;
        try {
            mandrillTemplate = mandrillApi.templates().info(templateName);
            mandrillTemplate.getPublishSubject();

        } catch (MandrillApiError | IOException e) {
            log.error("Exception inside sendMail : ", e);
        }
        System.out.println(mandrillTemplate.getName());
        // create your message
        MandrillMessage message = new MandrillMessage();
        message.setSubject("Welcome workruit !");
        message.setAutoText(true);
        message.setFromEmail(sender);
        message.setFromName(sender);
        message.setText(content);
        message.setGlobalMergeVars(globalMergeVars);

        // add recipients
        ArrayList<Recipient> recipients = new ArrayList<Recipient>();
        Recipient recipient = new Recipient();
        recipient.setEmail(toEmail);
        recipient.setName(toName);
        recipients.add(recipient);
        message.setTo(recipients);
        message.setPreserveRecipients(true);


        log.debug("Sending email");
        // TODO:
        //MandrillMessageStatus[] messageStatusReports = mandrillApi.messages().send(message, false);

        MandrillMessageStatus[] messageStatusReports = mandrillApi.messages().sendTemplate(mandrillTemplate.getName(), mapObject, message, false);
        log.info("Email Status " + messageStatusReports.toString());

    }
}
