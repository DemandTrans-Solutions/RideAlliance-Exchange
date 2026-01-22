# Trip Ticket Edit Feature

## Background

Refer to [project-history.md](./project-history.md) for a high-level overview of the current project.

## Plan

Currently [trip-ticket.component.html](../src/app/trip-ticket/trip-ticket.component.html) does not provide a way for a user to edit trip tickets. Trip Ticket editing should only be allowed

* If the logged in user belongs to the provider organization of the ticket and if the user has the role ROLE_PROVIDERADMIN
* The logged in user has the ROLE_ADMIN role

If the user has the appropriate rights, a new Update Feature should be added to the trip-ticket.component.html screen. This update feature should use current best practices for editing rows of a p-table grid.

[trip-ticket.component.ts](../src/app/trip-ticket.component.ts) and [trip-ticket.service.ts](../src/app/trip-ticket.service.ts) should be updated with all the required methods and functions to complete this. The update endpoint will be used for editing the ticket.

Do not update existing code except where it is required by the new feature.
