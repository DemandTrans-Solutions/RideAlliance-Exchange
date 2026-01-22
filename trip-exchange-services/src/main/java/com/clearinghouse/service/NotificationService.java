package com.clearinghouse.service;

import com.clearinghouse.dao.ApplicationSettingDAO;
import com.clearinghouse.exceptions.SpringAppRuntimeException;
import freemarker.template.Configuration;
import jakarta.activation.DataSource;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.*;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@Slf4j
@AllArgsConstructor
public class NotificationService {


    private final JavaMailSenderImpl mailSender;


    private final SimpleMailMessage simpleMailMessage;


    private final Configuration freemarkerConfiguration;


    private final ApplicationSettingDAO applicationSettingDAO;

    private final MimeMessage mimeMessage;

    private final String emailFrom;

    public void sendMail(String to, String subject, String body) {
        try {

            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setFrom(simpleMailMessage.getFrom());
            // simpleMailMessage.setTo("chaitanya.patil@zconsolutions.com");
            simpleMailMessage.setTo("todd.voirol@demandtrans.com");
            //helper.setTo(to);
            helper.setTo("todd.voirol@demandtrans.com");
            helper.setSubject(subject);
            helper.setText(body, true);

            mailSender.send(mimeMessage);
            //log.debug("mail is disabled sending to the address..%s", to);
        } catch (MessagingException e) {
            log.error("Error in sending email message", e);
            throw new SpringAppRuntimeException("Error in sending email message" + e.getMessage());
        }
    }


    public boolean sendMailWithTemplate(String to, String cc, String bcc, String subject, String template, Map<String, Object> parameters, boolean isAttachment, List<String> fileNameList, List<String> filePathList) {
        MimeMessagePreparator preparator = new MimeMessagePreparator() {
            public void prepare(MimeMessage mimeMessage) throws Exception {
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
                helper.setFrom(simpleMailMessage.getFrom());
                helper.setTo(to);
                //helper.setTo("todd.voirol@demandtrans.com");
                if (cc != null) {
                    helper.setCc(cc);
                    helper.setBcc(bcc);
                }
                helper.setSubject(subject);

                StringBuffer content = new StringBuffer();
                String text = "";
                try {
                    content.append(FreeMarkerTemplateUtils.processTemplateIntoString(
                            freemarkerConfiguration.getTemplate(template), parameters));
                    text = content.toString();

                    List<String> multiFileNames = fileNameList;
                    List<String> multiFilePaths = filePathList;
                    if (multiFileNames != null) {
                        for (int i = 0; i < multiFileNames.size(); i++) {
                            String fileName = multiFileNames.get(i);
                            String filePath = multiFilePaths.get(i);

                            if (filePath != null && fileName != null && isAttachment) {
                                byte[] fileData = transformToBytes(filePath);
                                DataSource src = new ByteArrayDataSource(fileData, "application/csv");
                                String filename2 = fileName + ".csv";
                                helper.addAttachment(filename2, src);

                            }
                        }
                    }
                } catch (Exception ex) {
                    // text will remain blank for error in email..
                    log.error("Some error occurred during sendingMail template content", ex);
                }
                helper.setText(text, true);
            }
        };

        try {

            /*setting dynamic email properties from database*/
            //String emailFrom = applicationSettingDAO.findApplicationSettingById(1).getFromEmail();
            //String emailFrom = applicationSettingDAO.findApplicationSettingById(2).getFromEmail();
            //String emailFrom = mailSender.get

            if (!emailFrom.equalsIgnoreCase("")) {
                simpleMailMessage.setFrom(emailFrom);
            }

            /* Decode password , by processing encoded data*/
            // byte[] valueDecoded = Base64.getDecoder().decode(applicationSettingDAO.findApplicationSettingById(1).getPasswordOfMail().getBytes());
            //byte[] valueDecoded = Base64.getDecoder().decode(applicationSettingDAO.findApplicationSettingById(2).getPasswordOfMail().getBytes());
            //String decodedPassword = new String(valueDecoded);

            //String password = decodedPassword;
            ///if (!password.equalsIgnoreCase("")) {
            //    mailSender.setPassword(password.trim());
            //}

            log.debug("SEnding mail to " + to + " for " + subject);
            mailSender.send(preparator);

            //log.warn("mail is disabled sending to the address..", to);
//           throw new SpringAppRuntimeException("Error in sending email message" );

        } catch (MailAuthenticationException authenticationException) {
            log.error("Invalid Credentials used to send mail", authenticationException);
            throw new SpringAppRuntimeException(" Invalid Credentials used to send mail..!!!!!!!!Please check!! " + authenticationException.getMessage());
        } catch (Exception e) {
            log.error("Error in sending email message", e);
            throw new SpringAppRuntimeException("Error in sending email message" + e.getMessage());
        }

        return true;
    }

    /**
     * Helper function to read a local file into a byte[].
     */
    public static byte[] transformToBytes(String aFilename) throws Exception {

        if (null == aFilename) {

            throw new NullPointerException("aFilename is null");
        }
        File theFile = new File(aFilename);
        if (!theFile.isFile()) {
            throw new IllegalArgumentException("Path doesn't represent a file: " + aFilename);
        }
        if (!theFile.exists()) {
            throw new IllegalArgumentException("File not found: " + aFilename);
        }

        InputStream theIs = new BufferedInputStream(new FileInputStream(theFile));
        ByteArrayOutputStream theRawData = new ByteArrayOutputStream();

        byte[] theBuffer = new byte[1024];
        int theBytesRead;

        try {
            while ((theBytesRead = theIs.read(theBuffer)) != -1) {
                if (theBytesRead < 1024) {
                    byte[] theSlice = new byte[theBytesRead];
                    System.arraycopy(theBuffer, 0, theSlice, 0, theBytesRead);
                    theRawData.write(theSlice);
                } else {
                    theRawData.write(theBuffer);
                }
            }
        } finally {
            theIs.close();
            theRawData.close();
        }

        return theRawData.toByteArray();
    }
}