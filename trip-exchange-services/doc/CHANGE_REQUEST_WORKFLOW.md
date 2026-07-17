# Trip Ticket Change-Request Approval Workflow

This document summarizes the functional changes that introduce a **change-request
approval workflow for editing claimed trip tickets**, spanning both repositories:

- **`clearinghouse_server`** — REST API, business rules, persistence, email notifications.
- **`clearinghouse_client`** — Angular UI, API client, edit-gating.

> **Status:** at the time of writing, these changes existed as uncommitted working-tree
> changes on the `master` branch in both repositories.

---

## 1. Problem this solves

Once a trip ticket is **claimed**, changing it is a coordination problem between the two
providers (the originator and the claimant). Previously there was no in-app mechanism to
request, approve, and gate such edits. This feature adds a formal request → approve/deny →
one-time-edit-unlock workflow, enforced at the API and mirrored in the UI.

---

## 2. The rules

These rules are **enforced server-side** (the real authority) and **mirrored in the client UI**
so the buttons and fields reflect what the server will allow.

1. **Super admin (`ROLE_ADMIN`)** may edit any trip, anytime — no change request needed.
2. **Unclaimed trips** can be edited freely by the **originating provider** while the trip is
   `Available` (unchanged from prior behavior).
3. **Claimed trips, more than 24 hours before pickup:** either party's **provider admin**
   (`ROLE_PROVIDERADMIN`) may send a change request to the other party. On approval, the proposed
   changes are **applied to the trip automatically**; the requester does not make a manual edit. A
   claimed trip can never be edited directly by a provider — all changes go through this workflow.
4. **Claimed trips, within 24 hours of pickup (or already past pickup):** locked to everyone
   **except super admin**. No change requests may be created in this window.

A trip is considered **claimed** when it has an approved claim and is **not** in one of the
editable-without-approval states: `Available`, `Cancelled`, `Completed`, `Expired`, `Rescinded`.

The **24-hour window** is computed from the trip's requested pickup date/time (falling back to
the requested dropoff date if pickup is absent). If no schedule information exists at all, the
trip is **not** locked on the basis of time. Server-side, the comparison is done in the
`UTC-6` time zone.

---

## 3. Request lifecycle and statuses

| Status        | Value | Meaning                                                                                  |
|---------------|-------|------------------------------------------------------------------------------------------|
| `pending`     | 1     | Request created; awaiting the other provider's response.                                 |
| `approved`    | 2     | Reserved. Approval applies the changes and lands the request in `applied` in one step; a request does not normally rest in this state. |
| `denied`      | 3     | The other provider denied the request. The trip remains unchanged.                       |
| `cancelled`   | 5     | The requesting provider withdrew the request before it was responded to.                 |
| `applied`     | 4     | The other provider approved and the proposed changes were applied to the trip automatically. |

### How approval applies the changes

Approval and application are a **single atomic step**. When the target provider approves, the
server, in one `@Transactional` operation:

1. parses the request's `proposed_changes` and writes each changed field onto the trip ticket,
2. persists the trip,
3. sets the request's status straight to `applied`,
4. records a "Change request changes applied" activity (with old → new values) and notifies the
   requester.

Key points:

- The requester makes **no** manual edit; there is no edit unlock to consume.
- Only fields whose proposed value differs from the trip's current value are written; if a proposed
  value equals the current one it is skipped and not logged.
- If applying the changes fails (e.g. an unparseable proposed value), the whole transaction rolls
  back: the request stays `pending` and the trip is unchanged. The caller receives a `400`.
- A further round of changes requires a new change request.

### Proposed changes are applied verbatim

A request carries an optional structured `proposed_changes` map (field name → proposed new value),
stored as JSON. On approval the server parses these and writes them to the trip. The currently
applied fields are: `requested_pickup_date`, `requested_pickup_time`, `requested_dropoff_date`,
`requested_dropoff_time`, `customer_seats_required`, and `trip_notes`. Dates are parsed as
`MM/dd/yyyy` and times as `hh:mm a` (the formats the client sends), in `Locale.US`.

---

## 4. Notifications — who gets told what

Notifications are sent by email to the **provider admins** of the relevant provider (every user
of that provider holding `ROLE_PROVIDERADMIN` or `ROLE_ADMIN`). Three new templates / codes were
added.

| Event                       | Template (code)                     | Recipients                                  |
|-----------------------------|-------------------------------------|---------------------------------------------|
| Change request **created**  | `tripChangeRequestReceived` (34)    | Admins of the **target** provider (the other party being asked to approve/deny). |
| Change request **approved** | `tripChangeRequestApproved` (35)    | Admins of the **requesting** provider (informing them the changes were applied automatically). |
| Change request **denied**   | `tripChangeRequestDenied` (36)      | Admins of the **requesting** provider (informing them the request was rejected).    |

In other words:

- When a provider **sends** a request, the **other party's** admins are notified that a request
  was received and needs their review.
- When a provider **approves or denies** a request, the **original requester's** admins are
  notified of the outcome. The approval email tells the requester the changes were applied to the
  trip in the Trip Exchange automatically and reminds them to make the matching change in their own
  system so the two stay in sync. The denial email states the trip is unchanged and they may contact
  the other provider or submit a new request.

Notification email parameters (built by `NotificationParamBuilder.changeRequestParams`) include:
recipient name, the common trip ticket id, requester and target provider names, the requester's
free-text message, a human-readable summary of the proposed changes, the approver/denier's
optional response message, and formatted pickup/dropoff date and time.

Every lifecycle event (created / approved / denied / applied) is also recorded in the trip's
**activity log**.

---

## 5. Server changes (`clearinghouse_server`)

### New files

- **`entity/TripChangeRequest.java`** — JPA entity mapped to the new `tripchangerequest` table.
- **`dao/TripChangeRequestDAO.java`** — data access, including:
  - `findAllForTripTicket(tripTicketId)` — all requests for a trip, newest first.
  - `hasPendingRequest(tripTicketId)` — guards against more than one pending request per trip.
- **`service/TripChangeRequestService.java`** — all business logic: create / approve / deny, the
  `isClaimed` and `isWithinLockWindow` checks, `applyProposedTripChanges(...)` (which writes the
  approved changes onto the trip on approval), `assertCanEditDirectly(...)` edit-lock enforcement
  (which blocks any direct edit of a claimed trip), notifications, and activity logging.
- **`controller/rest/TripChangeRequestController.java`** — REST endpoints (see below).
- **`dto/CreateChangeRequestRequest.java`** — request body for creating a request
  (`message`, optional `proposed_changes`).
- **`dto/ChangeRequestResponseRequest.java`** — request body for approve/deny
  (optional `response_message`).
- **`dto/TripChangeRequestDTO.java`** — response shape returned to the client.
- **`enumentity/TripChangeRequestStatusConstants.java`** — the `pending/approved/denied/applied`
  status enum.
- **`exceptions/EditNotAllowedException.java`** — thrown when an edit is not permitted; maps to
  HTTP **403 Forbidden**.
- **`resources/notification-templates/tripChangeRequestReceived.txt`**,
  **`tripChangeRequestApproved.txt`**, **`tripChangeRequestDenied.txt`** — HTML email templates.
- **`dbscript/ClearingHouse_changerequest_2026-05-29.sql`** — idempotent migration creating the
  `tripchangerequest` table (with indexes and foreign keys to `tripticket` and `provider`) and
  inserting the three notification templates (codes 34, 35, 36).

### Modified files

- **`controller/rest/TripTicketController.java`** — both trip-ticket update paths now call
  `tripChangeRequestService.assertCanEditDirectly(...)` **before** applying the update. This is
  where the rules get their teeth: enforcement is at the API, not merely hidden in the UI.
- **`controller/rest/exception/RestResponseEntityExceptionHandler.java`** — registers
  `EditNotAllowedException` → **403 Forbidden**.
- **`enumentity/NotificationTemplateCodeValue.java`** — adds template codes 34, 35, 36.
- **`service/notification/NotificationParamBuilder.java`** — adds `changeRequestParams(...)` to
  build the email parameter map for the three templates.

### REST endpoints

All under `api/trip_tickets/{trip_ticket_id}/change_requests`:

| Method & path                 | Purpose                                                        | Success status |
|-------------------------------|---------------------------------------------------------------|----------------|
| `GET`                         | List change requests for the trip (newest first).             | `200 OK`, or `204 No Content` if none |
| `POST`                        | Create a change request (`message`, optional `proposed_changes`). | `201 Created` |
| `PUT /{id}/approve`           | Target provider approves (optional `response_message`).       | `200 OK`       |
| `PUT /{id}/deny`              | Target provider denies (optional `response_message`).         | `200 OK`       |

Validation / authorization performed by the service includes: trip must exist and be claimed;
must be outside the 24h window; caller must be a party to the trip (originator or claimant) and a
provider admin; no existing pending request; a non-empty message is required. Approve/deny require
the caller to be the **target** provider (or a super admin) and a provider admin, and the request
must still be `pending`.

---

## 6. Client changes (`clearinghouse_client`)

### Modified files

- **`models/detailed-trip-ticket.dto.ts`** — adds the `TripChangeRequestDTO` interface.
- **`trip-ticket/trip-ticket.service.ts`** — four new API client methods matching the endpoints:
  `getChangeRequests`, `createChangeRequest`, `approveChangeRequest`, `denyChangeRequest`.
- **`trip-ticket/trip-ticket.component.ts`**:
  - `canEditTicket()` rewritten to mirror the server rules (super admin → always; unclaimed →
    originator while `Available`; claimed → blocked within 24h, otherwise requires the caller to
    be a party **and** hold an approved unlock).
  - New helper checks: `isTicketClaimed`, `isPartyToTicket`, `isWithin24Hours`,
    `canRequestChanges`, `hasApprovedChangeRequest`, `hasPendingChangeRequest`, and
    `pendingRequestForMe` (returns the request object addressed to the current provider, or null).
  - `loadChangeRequests()` is called when a ticket is opened, caching requests per ticket id so the
    inline UI and edit-gating reflect server state.
  - Submit/respond handlers for the two dialogs (`submitChangeRequest`, `submitRespond`) plus
    open/close handlers.
  - The edit dialog unlocks its fields when the trip is `Available` **or** the current user holds
    an approved change request.
- **`trip-ticket/trip-ticket.component.html`**:
  - Toolbar and overflow-menu buttons: **Request Changes**, **Approve Change Request**,
    **Deny Change Request**, shown conditionally per role and request state.
  - Color-coded **inline banners** on the single-ticket view reflecting request state from the
    viewer's perspective (pending-for-me with approve/deny buttons; pending-sent-by-me waiting;
    approved-and-mine "editing unlocked"; denied-and-mine).
  - A **Request Changes dialog** — required free-text message plus optional structured field
    proposals (pickup date/time, dropoff date/time, seats required, trip notes, passenger info).
  - An **approve/deny dialog** showing the request details and an optional response message.
  - A note in the edit dialog steering demographic changes (DOB, gender, eligibility, etc.) to the
    always-editable **Passenger Information** section.

---

## 7. Notes / things to be aware of

- **Approval applies the proposed changes automatically** in one atomic step; there is no manual
  edit and no edit unlock. See §3.
- **A claimed trip can never be edited directly by a provider** — `assertCanEditDirectly` blocks it
  unconditionally for any party. Only a super admin may edit directly; everything else goes through
  the request → approve flow.
- **Only the fields the server knows how to apply are applied.** A proposed change to a field not in
  the applied set (see §3) is stored and shown in the emails but has no effect on the trip on
  approval. Keep the client's proposable fields and the server's applied set in sync.
