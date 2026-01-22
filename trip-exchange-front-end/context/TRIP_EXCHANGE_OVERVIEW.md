# Trip Exchange Overview

## Functional Overview

The Clearinghouse project is a modern Angular 18 web application designed to facilitate the exchange of available transportation trips between different providers. Its primary function is to allow multiple transportation providers to view, claim, and manage trip tickets in a secure, auditable, and efficient manner. The system supports workflows for both provider administrators and users, enabling them to:

- View available trips (trip tickets) in a centralized grid
- Filter, search, and paginate through large sets of trip data
- Claim available trips, submit claims, and manage claim statuses (Pending, Approved, Declined, Rescinded)
- Upload new trip tickets in bulk via CSV or Excel files
- Add comments and track activity on each trip ticket
- Edit, update, or cancel trip tickets (with appropriate permissions)
- Generate summary and detailed reports on trip and claim activity

The application is used by transportation providers, provider administrators, and system administrators. It is designed to support collaboration and competition among providers, ensuring that available trips are efficiently matched to eligible providers.

## Technical Overview

### Frontend
- **Framework:** Angular 18 (migrated from Angular 2.x)
- **UI Library:** PrimeNG (for tables, dialogs, forms, etc.)
- **Styling:** Modular SCSS/CSS, with a modernized look and feel; icons migrated to Phosphor Icons
- **Key Components:**
  - `trip-ticket.component`: Main UI for trip management, including grid, filters, dialogs for claims/comments/upload/edit
  - `trip-ticket.service`: Handles all API interactions for tickets, claims, filters, uploads, and reports
  - Modular structure with lazy-loaded features and shared services
- **Features:**
  - Server-side pagination, filtering, and sorting for large datasets
  - File upload and transformation to backend DTOs
  - Real-time time zone support and dynamic UI updates
  - Robust error handling and user notifications

### Backend
- **API:** RESTful, implemented in Spring Boot 3.4
- **Authentication:** JWT-based, with role-based access control (Provider Admin, Provider User, Admin)
- **Endpoints:**
  - Trip ticket CRUD and search
  - Claims management (create, edit, approve, decline, rescind)
  - Bulk upload of trip tickets
  - Comments and activity tracking
  - Reporting endpoints for summaries and analytics
- **DTOs:** Data is exchanged using well-defined DTOs (e.g., `TripTicketRequestDTO`, `AddressDTO`)

### Data Flow
- **Trip Exchange:**
  - Providers upload or create trip tickets
  - Tickets are visible to all eligible providers
  - Providers can claim available tickets; claims are tracked and managed through their lifecycle
  - All actions (claim, approve, decline, rescind, comment) are logged for auditability
- **Bulk Upload:**
  - Users can upload CSV/XLSX files containing multiple trip tickets
  - The frontend parses and transforms these files into DTOs, then submits them to the backend for processing
- **Filtering & Pagination:**
  - The UI supports advanced filtering (by provider, status, date, eligibility, etc.)
  - Server-side pagination ensures performance with large datasets

### Infrastructure & Tooling
- **MCP Servers:** Used for enhanced code intelligence, large file support, and workspace management in development
- **Nx Workspace:** Modular monorepo structure for scalable development
- **Testing:** Unit tests (Karma), E2E tests (Protractor)
- **Deployment:** Scripts and documentation for AWS S3/CloudFront deployment

## Key Workflows
- **Trip Ticket Lifecycle:**
  1. Trip ticket is created (manually or via upload)
  2. Ticket is visible in the grid for eligible providers
  3. Providers claim tickets; claims are reviewed and approved/declined
  4. Comments and activity are tracked for each ticket
  5. Tickets can be updated, cancelled, or completed
- **Claims Management:**
  - Providers submit claims for tickets
  - Claims can be approved, declined, or rescinded by authorized users
  - All claim actions are auditable
- **Reporting:**
  - Summary and detailed reports are available for trip and claim activity
  - Reports can be filtered by date, provider, status, etc.

## Security & Permissions
- **Role-based access:** Only authorized users can view, claim, or manage tickets
- **Auditability:** All actions are logged and traceable
- **Data validation:** Both frontend and backend validate data for integrity and security

## Extensibility
- The system is designed for future enhancements, including chatbot integration, additional reporting, and new provider workflows.

---

This document provides a foundational overview of the Clearinghouse Trip Exchange project, covering both functional and technical aspects. Use this as a base context for future LLM interactions and onboarding.
