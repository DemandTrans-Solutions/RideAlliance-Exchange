# Ride Alliance Trip Exchange — Training Outline (Provider & Admin)

**Date:** October 16, 2025

**Audience:** Provider Users, Provider Admins, Super Admins

**Format:** Instructor-led training with live demo and Q&A


## 1) Welcome and Goals

- What you’ll learn:

  - Navigate the Trip Exchange UI

  - View and filter Trip Tickets

  - Create/approve/rescind Claims

  - Configure Provider settings (service area, partners, pricing, reports)

  - Understand roles/permissions and automation options

- Expected outcomes:

  - Confident daily operations for Providers

  - Clear process for cross-agency trip sharing

  - Accurate pricing and reporting


## 2) Program Context (Very Brief)

- Purpose of the Trip Exchange:

  - Enable cost-effective resource sharing across Denver-region transportation providers

  - Facilitate digital trip transactions between agencies

- Two interaction modes:

  - Web UI (manual operations) — focus of this training

  - Machine-to-machine automation (optional; high-level awareness)


## 2a) Key Definitions (Terms You'll See)

- Originating (Submitting) Provider:

  - Agency that submits a trip ticket to be fulfilled by another Provider; does not own the trip for reporting.

- Claimant Provider:

  - Agency that claims and delivers the trip; owns the trip for reporting and reimbursement.

- Maximum charge / Charge / Fare:

  - Maximum charge = highest amount the Submitter is willing to pay.

  - Charge = amount the Submitter will pay the Claimant.

  - Fare = amount paid by the consumer (may be zero).

- Agent:

  - Non-provider entity that may upload trip data (DRCOG may act as an Agent for some agencies).

- Consumer:

  - Rider and/or agency client.

- Trip Exchange (the platform):

  - Data exchange that routes trip tickets and enables cross-agency claiming and fulfillment.

- Eligibility note:

  - Some Providers can only be reimbursed for clients enrolled in specific programs or meeting criteria (e.g., age, income). Filters can help target eligible trips.


## 3) Access and Security

- Accounts:

  - Provider Admin grants user accounts and roles

- Sign-in:

  - Username/password

  - Two-factor verification (code via phone/email)

- Profile & password:

  - View profile

  - Change password

- Forgot password flow:

  - Use “Forgot your password?” on the sign-in screen, enter your email.

  - You’ll receive a temporary password; use it to reset your password.

  - After login, complete the verification code challenge (2FA) sent to your phone/email.


## 4) Roles and Permissions

- Super Admin:

  - System-wide control; create/manage Providers and Users

- Provider Admin:

  - Manage Provider settings (Users, Partners, Service Area, Medical Centers, Funding Sources, Trip Cost Estimator)

  - Approve/Decline Claims as Originating Provider; Create/Rescind Claims as Claimant Provider

- Provider User:

  - Create/Rescind Claims, Approve/Decline Claims on behalf of Provider


## 5) UI Tour — Landing and Menus

- Landing page: Trip Tickets

- Header icons: Trip Tickets | Admin | Reports

- User info block (top-right): User and Provider names

- “Show My Tickets” vs. “Show All Tickets”


## 6) Working with Trip Tickets

- List behavior:

  - Sorted by pickup date/time (toggle sort on other fields)

  - Pagination/scroll for multiple pages

- Quick Summary:

  - At-a-glance counts of ticket states

- Ticket Details:

  - Open by clicking Customer Name

  - Trip Activity Details: claims, customer info, trip details, claim details, comments

  - Map: pickup and drop-off locations

- Uber option:

  - “Send to Uber” available for qualifying trips (if enabled by your organization)

- “Show My Tickets”:

  - Restricts the grid to your Provider’s submitted tickets for quick review.

  - Note: You cannot claim your own Provider’s tickets; switch back to “Show All Tickets” to view network-wide opportunities.


## 7) Claiming Trips

- Claim eligibility:

  - Green check in “Claim Actions” indicates claimable

  - Not claimable: no partner relationship, already claimed, or out-of-area without proper filter

- Create Claim screen:

  - View Submitter price and trip details

  - Set or adjust your requested price

  - Price match rules:

    - A claim is accepted only when Submitter’s bid >= Claimant’s requested price

    - If mismatch, claim remains provisional until prices align

  - Acknowledge out-of-hours pickup/drop-off if applicable

  - Propose a new pickup time and/or add Notes (Submitter can review/update)

- Uber handoff during claim (if used):

  - Selecting “Send to Uber” routes trip to Uber fulfillment

- Approval behavior:

  - Trusted Partner claims auto-approve

  - Non-trusted partners: Originating Provider approves/declines

  - Multiple non-trusted claims: Originating Provider selects which to approve

- Rescinding:

  - Provider can rescind before or after approval (subject to workflow status)

- Notifications and tracking:

  - Email notifications keep both parties informed of claim creation, approval/decline, and key status changes.

  - Upon trip completion, the Claimant’s system sends execution results to the Exchange; the Exchange delivers results to the Originating Provider.

  - Weekly summary reports of trip transactions are distributed to participating Providers.


## 8) Finding the Right Trips — Filters

- Filter panel (left):

  - Status, Provider, Medical Centers, Funding Source, operating hours, eligibility criteria

  - Date/time ranges

  - Save filters for reuse

- Geographic filter (special case):

  - Show trips outside Service Area

  - Restrict by pickup in area, drop-off in area, or any

  - Note: trips contained within another Provider’s service area require geo filter to view/claim

- Geographic filter — step-by-step:

  1. Check “Show trips outside Service Area” to enable the geographic options.

  2. Choose one: “Pickup in area”, “Drop-off in area”, or “All conditions”.

  3. Apply any other filters (date range, Provider, status) as needed.

  4. Click Search to apply; only matching tickets remain in the grid. Save the filter for reuse if helpful.


## 9) Reports

- Provider Summary Report:

  - Totals by Trip Ticket status, new claim offers, new claim requests

  - Date range: From Date auto-calculated from oldest non-completed ticket; To Date required

- Current Tickets Report:

  - Shows all tickets relevant to the Provider (submitted, claimed, unclaimed)

  - Includes completed tickets if the start date precedes the first non-completed ticket

  - To Date required

- Completed Trips Report:

  - Trips completed per Provider in a given date range

- When to use which report:

  - Provider Summary: daily pulse of current workload and claim activity.

  - Current Tickets: operational review of what’s on deck (submitted/claimed/unclaimed).

  - Completed Trips: monthly/quarterly reconciliations and funding reviews.


## 10) Provider Admin Setup and Maintenance

- Provider (profile):

  - Update own Provider’s details (cannot add new Provider)

  - Set trip ticket expiration policy (days before/time of day)

  - Zip-based auto latitude/longitude population

  - Trip ticket expiration policy:

    - “Days before” and “Time of day” determine expiration relative to pickup date/time.

    - Tickets expire if no claim is created by the expiration point.

- Users:

  - Add/update/activate/deactivate

  - Configure email notifications per action

- Provider Partners:

  - View current partners and statuses

  - Request new partnerships; approve/deny incoming requests

  - Terminate or update partnerships (Trusted flag)

  - Trusted partners = auto-approval of claims

- Service Area:

  - Add/update/activate/deactivate areas

  - Ticket visibility/claiming filtered by service area unless geographic filter used

- Medical Centers:

  - Select medical centers to accept trips even outside service area

  - Choose and import geo files (approx. 5-mile catchment areas)

  - Adds to effective service area logic

  - Effect on claiming:

    - Selected Medical Centers allow acceptance of trips to those locations even if outside your base Service Area, expanding your eligible claim zone.

- Funding Sources:

  - Activate/deactivate accepted sources; update descriptions for your Provider

  - Creation of new funding sources = Super Admin only

- Trip Cost Estimator:

  - Pricing parameters (up to four): pickup fee, per-mile, per-hour, and modality-specific fees

  - Typical formula: Pickup Fee + (Fee/Mile × Distance) + (Fee/Hour × Estimated Time)

  - Defaults:

    - Distance ~ straight-line × 1.27

    - Estimated Time ~ Distance × 20 mph

    - Dwell time not included; use higher pickup fee for wheelchair trips to account for dwell

  - Formula used for both offer price (claiming) and bid price (submitting)

  - Prices can be adjusted any time; price match required for successful claim


## 10a) Automation Operations (Optional, Advanced)

- Overview:

  - Providers may integrate their own software for machine-to-machine operations.

  - Automated posting of trip tickets and automated claiming can be configured based on business rules and partner relationships.

- Data flow:

  - After a claimed trip is delivered, the Claimant’s system posts completion results back to the Exchange, which forwards them to the Originating Provider for records and reporting.

- Governance:

  - Align automated behaviors with partnership status (Trusted vs. non-Trusted), operating hours, and eligibility/funding constraints.


## 11) Super Admin Capabilities (Awareness)

- Providers:

  - Add/update/activate/deactivate Providers

  - Deactivation cascades to all Users; reactivation restores access

- Users:

  - Add/update/activate/deactivate Users per Provider

- Global Tickets & Filters:

  - Access all tickets across Providers

  - Create/save/update global filters


## 12) Daily Operations Workflow (Quick Reference)

- Log in (2FA)

- Trip Tickets:

  - “Show My Tickets” for your Provider’s submissions

  - Apply filters (including geo, if needed)

  - Inspect ticket details and notes

- Claims:

  - Check green claim icon

  - Open Create Claim, review price and details

  - Adjust requested price or notes; ensure price match

  - If non-trusted partner, monitor approval

  - Rescind if necessary

- Reporting:

  - Run Provider Summary, Current Tickets, Completed Trips as needed

- Admin (Provider Admin):

  - Maintain Users, Partners, Service Areas, Medical Centers, Funding Sources

  - Review/update Trip Cost Estimator


## 13) Common Pitfalls and Tips

- Can’t claim:

  - Likely no partnership, already claimed, or out-of-service-area without geo filter

- Price not matching:

  - Submitter’s bid must be >= your requested price; coordinate or adjust

- Out-of-hours claims:

  - Must acknowledge explicitly on Create Claim screen

- Completed trips in reports:

  - Adjust date range start to include dates prior to the first non-completed ticket


## 14) Q&A and Next Steps

- Review your Provider’s current:

  - Partners and Trusted flags

  - Service Areas and Medical Centers

  - Funding Sources

  - Trip Cost Estimator values for ambulatory vs. wheelchair

- Optional: discuss automation (machine-to-machine) readiness and governance

 
---

**Notes:**

- This outline is intentionally concise and image-free for distribution to class participants.

- For a hands-on session, use the Trip Exchange UI to demo each section live.
