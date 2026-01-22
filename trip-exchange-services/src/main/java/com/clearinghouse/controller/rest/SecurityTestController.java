/// *
// * License to Clearing House Project
// * To be used for Clearing House  project only 
// */
//package com.clearinghouse.controller.rest;
//
//import com.clearinghouse.dto.SchoolDTO;
//import com.clearinghouse.service.interfaces.ISchoolService;
//import java.util.List;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.bind.annotation.RestController;
//
///**
// *
// * @author manisha
// */
//@RestController
//@RequestMapping(value = {"/secured/user"})
//public class SecurityTestController {
//    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityTestController.class);
//
//    @Autowired
//    ISchoolService schoolService;
//    
//    @RequestMapping(method = RequestMethod.GET )
//    public ResponseEntity<List<SchoolDTO>> listAllSecuredUsers() {
//        log.debug("Accessing secured user list");
//        //below is a dummy call to demonstrate that data is returned back
//        List<SchoolDTO> schoolDTOs = schoolService.findAllSchools();
//        if (schoolDTOs.isEmpty()) {
//            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
//        }
//        return new ResponseEntity<>(schoolDTOs, HttpStatus.OK);
//    }
//    
//}
