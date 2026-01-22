# Copilot Instructions for Clearinghouse Trip Exchange

## Project Overview

This is an **Angular 18** transportation clearinghouse application migrated from Angular 2.4. It manages trip tickets, provider coordination, and trip exchange workflows. The backend is a **Spring Boot 3.4 REST API** using JWT authentication. Authentication uses **Firebase** (first factor + MFA) with backend validation for session establishment.

## Architecture & Key Patterns

### Module Structure
- **Lazy-loaded feature modules**: `TripTicketModule`, `AdminModule`, `ReportsModule`, `AuthModule`
- **CoreModule**: Singleton services (provided in root)
- **SharedModule**: Reusable UI components, directives, pipes
- All routes use `HashLocationStrategy` (not HTML5 mode)

### Authentication Flow
1. User authenticates via Firebase (email/password or Google sign-in)
2. MFA is **mandatory** - enforced in `LoginComponent` via `FirebaseAuthService`
3. After Firebase success, `LoginService.validateUser(firebaseUser)` sends encrypted email to backend
4. Backend validates and returns JWT + session data (stored via `TokenService` and `LocalStorageService`)
5. `AuthGuard` checks both Firebase auth **and** backend user data in localStorage

**Critical files**:
- `src/app/shared/service/firebase-auth.service.ts` - Firebase SDK wrapper
- `src/app/login/login.service.ts` - Backend validation (`validateUser` method)
- `src/app/shared/guard/auth-guard.service.ts` - Route protection

### HTTP & API Patterns
- **All API calls** use `SharedHttpClientService` (wraps HttpClient)
- Automatically adds `X-AUTH-TOKEN` header from `localStorage.xsrfToken`
- URLs normalized to **remove trailing slashes** in `SharedHttpClientService`
- **Environment `apiUrl` must END with `/`** (see `environment.ts`) - normalized by service
- XSRF token stored in both cookie (`XSRF-TOKEN`) and localStorage (`xsrfToken`)
- Auth errors (401/403) auto-redirect to `/login` after clearing tokens

**Example service pattern**:
```typescript
constructor(private sharedHttp: SharedHttpClientService) {}

getTickets(): Observable<Ticket[]> {
  return this.sharedHttp.get<Ticket[]>(environment.apiUrl + 'tripTickets');
}
```

### Service Injection
- Use `providedIn: 'root'` for all services (modern pattern)
- Services are **singletons** across the app
- `ConstantService` provides `WEBSERVICE_URL` (from `environment.apiUrl`)

### State Management
- **No NgRx/signals** - uses services with BehaviorSubject for shared state
- `NotificationEmitterService` - toast notifications (PrimeNG)
- `HeaderEmitterService` - header updates
- `AdminEmitterService` - admin panel events
- `ConfirmPopupEmitterService` - confirmation dialogs

### UI Components
- **PrimeNG 16.4.0** for data tables, dialogs, calendars, etc.
- Use `p-table` (not deprecated `p-dataTable`)
- Templates use `<ng-template pTemplate="header|body">` syntax
- **Phosphor Icons** (modern) replacing legacy Font Awesome
- Dark mode support via CSS classes (see `context/DARK_MODE_GLOBAL_SUMMARY.md`)

## Development Workflows

### Local Development
```bash
npm install --legacy-peer-deps  # Required for Angular 2→18 migration compatibility
npm run start:dev               # Development (localhost:4200)
npm run start:staging           # Staging environment
npm run start:prod              # Production build locally
```

### Environment Files
- `environment.ts` - Development (localhost:8080 API)
- `environment.staging.ts` - Staging API
- `environment.prod.ts` - Production API

**Key environment properties**:
- `apiUrl` - **MUST end with `/`** (normalized by services)
- `loginValidateSharedKey` - Base64 AES key for email encryption (see README)
- `firebase` - Firebase config for authentication
- `passwordResetRedirectUrl` - Post-reset redirect URL

### Building
```bash
npm run build:dev      # Development build → dist/
npm run build:staging  # Staging build
npm run build:prod     # Production build (optimized)
```

### Testing
```bash
npm test  # Karma unit tests
npm run e2e  # Protractor e2e tests
```

### Debugging in VS Code
1. Run `npm run debug` (or `start:dev`)
2. Press `F5` → Select "Debug Angular in Chrome"
3. Breakpoints work in `.ts` files (source maps enabled)
4. JWT debugging: Set breakpoint in `TokenService`, call `this.debug()` in debug console

## Critical Conventions

### Trip Ticket Module
- **Largest component** - `trip-ticket.component.ts` (~3000+ lines, migrated incrementally)
- Service: `TripTicketService` (~772 lines) - handles all trip CRUD, claims, results
- Uses server-side pagination (see `context/PAGINATION_FIX_SUMMARY.md`)
- Complex interfaces: `TripResultDTO`, `TripResultRequestDTO`, `TripTicketDTO`

### Date/Time Handling
- Uses **moment.js** via `ngx-moment` for formatting
- Backend expects ISO 8601 strings
- Display format: `moment(value).format('MM/DD/YYYY hh:mm A')`

### Forms
- Reactive Forms pattern preferred
- Validation via Angular validators
- PrimeNG form controls (Calendar, Dropdown, InputNumber)

### Error Handling
- Use `SharedHttpClientService.handleError` for consistent error responses
- Show errors via `NotificationEmitterService.error(title, message)`
- Auth errors auto-redirect (handled in SharedHttpClientService)

### Styling
- Component-specific `.css` files (not SCSS in most places)
- Global styles: `src/styles.css`, `src/assets/css/custom.css`, `modern-theme.css`, `theme-override.css`
- Dark mode classes added per component (see `context/CSS_USAGE.md`)
- Phosphor Icons: `<i class="ph ph-icon-name"></i>` (not Font Awesome)

## AWS Deployment

### CloudFront/S3 Deployment
```bash
./deploy-to-aws-cloudfront.sh initial-setup  # First time
./deploy-to-aws-cloudfront.sh update        # Updates
```
- Script uses **Node v20** via nvm (auto-switched)
- Deploys to S3 bucket configured in script
- Creates/updates CloudFront distribution
- Invalidates cache on updates
- See `context/aws-cloudfront-deployment.md` for details

### Translation
```bash
npm run translate:aws  # AWS Translate for i18n JSON files
```
- Translates `src/assets/i18n/en.json` to other locales
- Requires `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY`

## Important Context Documents

The `/context` directory contains detailed migration/fix documentation:
- `project-history.md` - Project evolution overview
- `update-guide.md` - Angular 2.4→18 migration steps (1400+ lines)
- `TRIP_EXCHANGE_OVERVIEW.md` - Business logic overview
- `trip-ticket-migration-*.md` - Trip ticket migration tracking
- `PAGINATION_FIX_SUMMARY.md` - Server-side pagination implementation
- `CSS_USAGE.md` - Style architecture analysis
- `ICON_USE.md` - Icon migration (Font Awesome → Phosphor)
- `DARK_MODE_GLOBAL_SUMMARY.md` - Dark mode implementation
- `DEBUG_GUIDE.md` - Debugging workflows

## Code Generation Guidelines

### Adding New Components
```bash
ng generate component feature/new-component --module=feature
```
- Components go in feature modules (not AppModule)
- Use lazy loading for new features
- Add to feature routing module

### Adding New Services
```typescript
@Injectable({ providedIn: 'root' })
export class MyService {
  constructor(private sharedHttp: SharedHttpClientService) {}
}
```
- No need to add to `providers` array (providedIn handles it)
- Inject `SharedHttpClientService` for HTTP calls (not raw HttpClient)

### Adding Routes
- Add to feature routing module (e.g., `trip-ticket-routing.module.ts`)
- Protected routes need `canActivate: [AuthGuard]`
- Admin routes checked in AuthGuard for `ROLE_ADMIN` or `ROLE_PROVIDERADMIN`

### Working with PrimeNG Tables
```html
<p-table [value]="data" [paginator]="true" [rows]="10">
  <ng-template pTemplate="header">
    <tr><th>Column</th></tr>
  </ng-template>
  <ng-template pTemplate="body" let-item>
    <tr><td>{{item.field}}</td></tr>
  </ng-template>
</p-table>
```

## Common Pitfalls

1. **API URL trailing slash**: Environment `apiUrl` MUST end with `/` but service normalizes URLs
2. **`--legacy-peer-deps`**: Required for `npm install` due to migration peer dep conflicts
3. **Firebase + Backend**: Auth requires BOTH Firebase auth AND backend `validateUser` success
4. **localStorage keys**: `userId`, `Role`, `xsrfToken` required for auth checks
5. **Large files**: `trip-ticket.component.ts` and `trip-ticket.service.ts` are massive - modify carefully
6. **Hash routing**: URLs use `#/` prefix (HashLocationStrategy)
7. **MFA mandatory**: All users must enroll in MFA - enforced in LoginComponent
8. **Shared encryption key**: `loginValidateSharedKey` in environment must match backend for email encryption

## Testing Checklist

- [ ] Run `npm test` for unit tests
- [ ] Test in Chrome (primary browser)
- [ ] Verify auth flow (login → MFA → backend validation)
- [ ] Check XSRF token in localStorage and cookies
- [ ] Test lazy-loaded routes load correctly
- [ ] Verify API calls use correct environment URL
- [ ] Check PrimeNG table pagination works
- [ ] Validate dark mode CSS classes applied

## References

- Angular 18 docs: https://angular.dev/
- PrimeNG docs: https://primeng.org/documentation
- Phosphor Icons: https://phosphoricons.com/
- Firebase docs: https://firebase.google.com/docs
- Project README: `/README.md`
- Context docs: `/context/*.md`
