/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.controller.rest.exception;

import com.clearinghouse.exceptions.*;
import com.clearinghouse.service.TripTicketService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 *
 * @author manisha
 */
@ControllerAdvice
@Slf4j
@AllArgsConstructor
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {


    private final TripTicketService tripTicketService;

    /*used status code*/
    /*status code=500 internal server error*/


    /*status code=410 USER EXPIRED*/
    /*status code=411 DUPLICATE_ENTRY_FOR_NEW_USERNAME*/
    /*status code=412 NO_USERNAME_EXISTS*/
    /*status code=416 PROVIDER_EMAIL_EXISTS*/
    /*status code=417 PROVIDER_PARTNESHIP_ALREADY_EXISTS*/
    /*ststus code =424 NO ACTIVE SERVICE AREA*/

    /*ststus code =206 partial conetent while adding ticket from adapter*/
    /*status code=500 generic exception*/

    //handle application exception
    @ExceptionHandler(value = {SpringAppRuntimeException.class})
    protected ResponseEntity<ErrorResponse> handleSpringAppRuntimeException(SpringAppRuntimeException ex) {
        log.error("APP_RUNTIME_EXCEPTION", ex);
        ErrorResponse error = new ErrorResponse();
        error.setErrorCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        error.setMessage("Application runtime exception occurred. [" + ex.getMessage() + "]");
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    //duplicate entry check for user...410
    @ExceptionHandler(value = {UserExpiredRuntimeException.class})
    protected ResponseEntity<ErrorResponse> userExpiredRuntimeException(UserExpiredRuntimeException ex) {
        log.error("USER EXPIRED", ex);
        ErrorResponse error = new ErrorResponse();
        error.setErrorCode(HttpStatus.GONE.value());

        error.setMessage(ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.GONE);
    }

    //duplicate entry check for user...411
    @ExceptionHandler(value = {UsernameExistException.class})
    protected ResponseEntity<ErrorResponse> handleSQLExceptionForDuplicateUserEmailEntry(UsernameExistException ex) {
        log.error("DUPLICATE_ENTRY_FOR_NEW_USERNAME", ex);
        ErrorResponse error = new ErrorResponse();
        error.setErrorCode(HttpStatus.LENGTH_REQUIRED.value());

        error.setMessage("You entered email " + ex.getUsername() + " which is already exist..!! ");
        return new ResponseEntity<>(error, (HttpStatus.LENGTH_REQUIRED));
    }

    //is username exist check..412
    @ExceptionHandler(value = {UsernameNotExistException.class})
    protected ResponseEntity<ErrorResponse> checkingIsUsernameExists(UsernameNotExistException ex) {
        log.error("NO_USERNAME_EXISTS", ex);
        ErrorResponse error = new ErrorResponse();
        error.setErrorCode(HttpStatus.PRECONDITION_FAILED.value());
        error.setMessage("You entered email/username " + ex.getUsername() + " which is not exists..!! ");
        return new ResponseEntity<>(error, HttpStatus.PRECONDITION_FAILED);
    }

    //is provider exists check...416
    @ExceptionHandler(value = {ProviderExistsException.class})
    protected ResponseEntity<ErrorResponse> checkingIsProviderEmailExists(ProviderExistsException ex) {
        log.error("PROVIDER_EMAIL_EXISTS", ex);
        ErrorResponse error = new ErrorResponse();
        error.setErrorCode(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE.value());
        error.setMessage("You entered email " + ex.getEmail() + " which is exists..!! ");
        return new ResponseEntity<>(error, HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE);
    }

    //is providerPartnership exists check...417
    @ExceptionHandler(value = {ProviderPartnershipAlreadyExistsException.class})
    protected ResponseEntity<ErrorResponse> checkingIsProviderPartnershipExists(ProviderPartnershipAlreadyExistsException ex) {
        log.error("PROVIDER_PARTNESHIP_ALREADY_EXISTS", ex);
        ErrorResponse error = new ErrorResponse();
        error.setErrorCode(HttpStatus.EXPECTATION_FAILED.value());
        error.setMessage("Request for " + ex.getProviderName() + " is already present or you are already partner..!! ");
        return new ResponseEntity<>(error, HttpStatus.EXPECTATION_FAILED);
    }

    //is activerServiceareaexists check...424 retring value
    @ExceptionHandler(value = {ServiceAreaActiveCheckException.class})
    protected ResponseEntity<ErrorResponse> checkingIsActiveServiceAreaExists(ServiceAreaActiveCheckException ex) {
        log.error("NO ACTIVE SERVICEAREA", ex);
        ErrorResponse error = new ErrorResponse();
        error.setErrorCode(HttpStatus.FAILED_DEPENDENCY.value());

        error.setMessage(ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.FAILED_DEPENDENCY);
    }

    //is INVALID INPUT check...206 returning value
    @ExceptionHandler(value = {InvalidInputCheckException.class})
    protected ResponseEntity<ErrorResponse> invalidInputCheckException(InvalidInputCheckException ex) {
        log.error("INVALID INPUT", ex);
        ErrorResponse error = new ErrorResponse();
        error.setErrorCode(HttpStatus.PARTIAL_CONTENT.value());

        error.setMessage(ex.getMessageList().toString());

        return new ResponseEntity<>(error, HttpStatus.PARTIAL_CONTENT);
    }

    @ExceptionHandler(value = {InvalidKMLFileException.class})
    protected ResponseEntity<ErrorResponse> invalidKMLFileException(InvalidKMLFileException ex) {
        log.error("INVALID File-", ex);
        ErrorResponse error = new ErrorResponse();
        error.setErrorCode(HttpStatus.BANDWIDTH_LIMIT_EXCEEDED.value());
        error.setMessage(ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BANDWIDTH_LIMIT_EXCEEDED);
    }

    //newly added 400 bad request
    @ExceptionHandler(value = {InvalidInputException.class})
    protected ResponseEntity<ErrorResponse> invalidInputException(InvalidInputException ex) {
        log.error("GENERIC EXCEPTION ", ex);
        ErrorResponse error = new ErrorResponse();
        error.setErrorCode(HttpStatus.BAD_REQUEST.value());
        error.setMessage(ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    //newly added  404 not found
    @ExceptionHandler(value = {NoInternetConnectionException.class})
    protected ResponseEntity<ErrorResponse> noInternetConnectionException(NoInternetConnectionException ex) {
        log.error("GENERIC EXCEPTION ", ex);
        ErrorResponse error = new ErrorResponse();
        error.setErrorCode(HttpStatus.NOT_FOUND.value());
        error.setMessage(ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    // 200 OK
    @ExceptionHandler(value = {HandlingExceptionForOKStatus.class})
    protected ResponseEntity<ErrorResponse> handlingExceptionForOKStatus(HandlingExceptionForOKStatus ex) {
        log.error("GENERIC EXCEPTION ", ex);
        ErrorResponse error = new ErrorResponse();
        error.setErrorCode(HttpStatus.OK.value());
        error.setMessage(ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.OK);
    }

    /*status code=500*/
    //handle all other exceptions
    @ExceptionHandler(value = {Exception.class})
    protected ResponseEntity<ErrorResponse> handleException(Exception ex) {
        log.error("GENERIC_EXCEPTION", ex);
        ErrorResponse error = new ErrorResponse();
        error.setErrorCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        error.setMessage("General exception occurred. [" + ex.toString() + "].");
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
