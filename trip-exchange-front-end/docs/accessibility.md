# Accessibility Conformance

Last updated: June 13, 2026

## Target

Ride Alliance targets WCAG 2.2 Level A and Level AA for user-facing web flows.

## Current Status

The WCAG remediation implementation pass is in progress. Static template sweeps and Angular builds have been used to verify completed remediation items, but a full manual accessibility conformance pass has not yet been completed.

## Testing Completed

- Angular production build via `npm run build`.
- Static template sweeps for:
  - labels without explicit `for` or Angular `[for]` bindings
  - required controls missing `aria-required`
  - native selects, PrimeNG dropdowns, and PrimeNG multiselects without accessible names
  - placeholder-only controls without accessible names
  - icon-only buttons without accessible names
  - dynamic error blocks missing ids
  - unreferenced field-level error ids
- Manual code review against `ui-ux-updates/WCAG_2.2_REMEDIATION.md`.

## Known Exceptions And Remaining Verification

- Full axe-core or axe DevTools coverage has not yet been completed across all target pages.
- Full keyboard-only testing has not yet been completed across all target pages.
- Screen reader testing with VoiceOver or NVDA has not yet been completed.
- Rendered contrast checks in both light and dark themes have not yet been completed.
- PrimeNG default component contrast and focus styles still need rendered verification.
- Footer placeholder links remain pending project-owner confirmation before they are wired or removed.

## Last Full Pass

No full manual conformance pass has been completed yet. The latest implementation verification build was completed on June 13, 2026.
