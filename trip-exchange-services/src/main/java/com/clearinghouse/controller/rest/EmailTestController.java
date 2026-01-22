/// *
// * License to Clearing House Project
// * To be used for Clearing House  project only 
// */
//package com.clearinghouse.controller.rest;
//
//import com.clearinghouse.dao.interfaces.INotificationDAO;
//import com.clearinghouse.entity.Notification;
//import com.clearinghouse.enumEntity.NotificationStatus;
//import com.clearinghouse.service.interfaces.INotificationService;
//import com.fasterxml.jackson.core.type.TypeReference;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import java.io.ByteArrayInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.AbstractList;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.RestController;
//
///**
// *
// * @author manisha
// */
//@RestController
//
////add new
//public class EmailTestController {
//
//    @Autowired
//    INotificationService notificationService;
//
//    @Autowired
//    INotificationDAO notificationDAO;
//
//    @RequestMapping(value = "/email", method = RequestMethod.GET)
//    public ResponseEntity<String> sendEmail() {
//        notificationService.sendMail("manisha.msathe@gmail.com", "Test Subject Email Testing", "This is email test body");
//        String statusString = "'emailstatus':'success'";
//        return new ResponseEntity<>(statusString, HttpStatus.OK);
//    }
//
//}
