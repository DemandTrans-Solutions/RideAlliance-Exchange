/*
 * License to Clearing House Project
 * To be used for Clearing House  project only
 */
package com.clearinghouse.exceptions;

/**
 * Thrown when the current user is not permitted to edit a trip ticket directly
 * (e.g. the trip is claimed and requires a change-request approval, or it is
 * within 24 hours of pickup and only a super admin may change it). Maps to 403.
 */
@SuppressWarnings("serial")
public class EditNotAllowedException extends RuntimeException {

    public EditNotAllowedException(String message) {
        super(message);
    }
}
