# Dark Mode - Global Summary & Remediation Guide

Date: October 7, 2025

This document consolidates the Windows/macOS dark-mode investigation and fixes applied across the codebase. It's written so a coding agent or developer can immediately understand the root causes, detection steps, concrete fixes already applied, and a repeatable remediation pattern for similar future issues.

## Goal / Contract

- Input: A UI that supports a user-selected theme via `body.dark-theme` / `body.light-theme` while some components use `@media (prefers-color-scheme: dark)` or global `:root` variables.
- Output: Consistent, predictable dark-mode appearance across Windows and macOS when `body.dark-theme` is present.
- Error modes: Browser-specific CSS variable resolution, media-query vs class mismatch, component-scoped media queries winning/losing due to specificity.

## Executive summary (short)

- Root cause: Mix of class-based theming (app uses `body.dark-theme`) and component-scoped `@media (prefers-color-scheme: dark)` blocks plus `:root`-level CSS variables. On Windows (system light) prefers-color-scheme blocks don’t match, so components fall back to their light defaults. macOS often matched because the system was set to dark, masking the problem.

- Fix approach used: Add two central override files that load late and force class-based `body.dark-theme` rules (and variable overrides) with sufficient specificity and selective use of `!important`. Also add media-query conflict handlers for edge cases.

## Files created/modified (high level)

- `/src/assets/css/theme-override.css` — Primary class-based overrides for body.dark-theme. Contains variable overrides, PrimeNG overrides, native-select rules, paginator rules, and component container overrides.

- `/src/assets/css/media-query-override.css` — Secondary file that mirrors important overrides and includes `@media (prefers-color-scheme: ...)` conflict handlers (e.g., when system and app preferences differ).

- Documentation files: multiple markdowns created/updated (see repo root) documenting specific fixes (select fixes, paginator fixes, native select fixes, testing guides).

## Root causes (detailed)

1. Media query vs class mismatch

- Many components use `@media (prefers-color-scheme: dark)` inside component-scoped SCSS to implement 'dark' styles. These media queries reflect the OS/system preference, not the app's class-based selection. If the user chooses dark theme while the OS is set to light, those media queries do not apply on Windows and the components use the light fallback styles.

2. CSS variable specificity and propagation

- Some variables (e.g. `--text-color`) are defined on `:root` in legacy CSS. `:root` variable definitions can beat `body.dark-theme` overrides due to cascade/specificity. Different browsers may resolve variable cascades differently.

3. Component-scoped selectors winning due to encapsulation/specificity

- Component styles (scoped or compiled) sometimes emit selectors that are more specific than global overrides; global override files must be loaded last and use enough specificity (and occasionally `!important`) or override variables rather than properties.

## Concrete fixes applied (what was changed)

Below is a concise list of the fixes applied and examples (these are already in the repository under the files above):

1. Force CSS variable overrides at :root and body levels

Example (already in `theme-override.css`):

```css
:root:has(body.dark-theme),
body.dark-theme {
  --text-color: #e9ecef !important;
  --text-color-secondary: #dee2e6 !important;
  /* other variables used by theme */
}

/* Fallback for browsers that don't support :has() */
html body.dark-theme { --text-color: #e9ecef !important; }

/* Nuclear fallback */
body.dark-theme * { --text-color: #e9ecef !important; }
```

Why: Overriding variables at the component scope is more reliable than trying to override hundreds of properties. Variables cascade down and can be used by child rules that reference var(). Using `:root:has()` gives the cleanest override for modern browsers; fallback selectors handle older browsers.

2. Centralized component overrides (paginator, PrimeNG components)

Example: paginator override (already in `theme-override.css`)

```css
body.dark-theme .p-paginator {
  background-color: #343a40 !important;
  color: #e9ecef !important;
}
body.dark-theme .p-paginator .p-paginator-pages .p-paginator-page {
  color: #e9ecef !important;
  background-color: transparent !important;
}
/* hover, highlight, dropdown inside paginator, and arrow colors included similarly */
```

Why: Many report components defined a light `.p-paginator` by default and a dark one inside an `@media (prefers-color-scheme: dark)` block. The global override forces the dark look when `body.dark-theme` is present, regardless of the system preference.

3. PrimeNG multiselect / dropdown fixes (variable overrides)

Example: set background, color, and variables on the component itself

```css
body.dark-theme .p-multiselect {
  background-color: #2a2d35 !important; /* dark */
  color: #e9ecef !important;            /* light text */
  border-color: #495057 !important;
  --input-text: #e9ecef !important;    /* override used internally by child selectors */
}
body.dark-theme .p-multiselect .p-multiselect-label { color: #e9ecef !important; }
body.dark-theme .p-multiselect-panel { background-color: #343a40 !important; }
```

Why: Some PrimeNG child elements used CSS variables like `--input-text`. Overriding the variable at the component root ensures child selectors using var(--input-text) inherit the correct value.

4. Native select & inputs

Example:

```css
body.dark-theme select,
body.dark-theme input[type="time"],
body.dark-theme input[type="date"],
body.dark-theme input[type="datetime-local"] {
  background-color: #2a2d35 !important;
  color: #e9ecef !important;
  border-color: #495057 !important;
}
```

Why: Browser defaults or styles inside `@media (prefers-color-scheme: dark)` were applying only when the OS was dark. This forces native controls to match the app-level theme.

5. Media-query conflict handlers (in `media-query-override.css`)

Pattern applied:

```css
@media (prefers-color-scheme: dark) {
  /* If system is dark but app is light, ensure app still shows light theme */
  body.light-theme .some-component { /* explicit light rules */ }
}

@media (prefers-color-scheme: light) {
  /* If system is light but app is dark, force dark overrides */
  body.dark-theme .some-component { /* explicit dark rules (duplicate) */ }
}
```

Why: These handlers are defensive: they cover the cross-state cases where system preference and app preference differ.

## Detection & triage (how an agent should find similar problems)

1. Search the repo for `@media (prefers-color-scheme: dark)` occurrences. Files using it for component-level dark styling are high risk.

2. Search for `var(--` usage to find child selectors that depend on CSS variables (ex: `--input-text`, `--input-bg`, `--neutral-50` etc.). If variables are set at `:root`, they may override body-level overrides.

3. Reproduce locally with two scenarios:
   - System preference: light; app theme: dark (simulate on Windows)
   - System preference: dark; app theme: dark (simulate macOS)

4. Use DevTools -> Computed to inspect element background-color and color and record which CSS rule is winning (file + selector).

5. If the winning rule is a component stylesheet default (light) or a `@media` rule that depends on system preference, add a body.dark-theme override in the central files.

## Remediation pattern (repeatable recipe)

1. Prefer overriding CSS variables at a scope where the component reads them.
   - If child elements use var(--input-text), set `--input-text` on the component selector (e.g., `.p-multiselect`) under `body.dark-theme`.

2. Add a small, targeted override rather than wholesale `!important` for everything.
   - Example: prefer `body.dark-theme .component .part { color: ... }` or `body.dark-theme .component { --var: ... }`.

3. Add the override in `/src/assets/css/theme-override.css` (primary) and mirror critical ones in `/src/assets/css/media-query-override.css` when the component uses `@media` internally.

4. Load order matters: these override files must be linked after other styles so their rules win. Keep the files small and focused.

5. When feasible, refactor component styles to use class-based theme selectors (e.g., `:host-context(body.dark-theme) ....` or `:host-context(body.dark-theme)` inside component SCSS) instead of media queries.

## Sample checklist for a single failing component
1. Inspect element and note computed background-color and the rule that sets it.
2. If rule is inside `@media (prefers-color-scheme: dark)`, add a `body.dark-theme .selector` rule into `theme-override.css` to match the media-query rule.
3. If the element uses CSS variables, override the variable at the component root (`body.dark-theme .selector { --input-text: #... }`).
4. Hard-refresh and test in both scenarios.
5. If necessary, add a mirrored rule into `media-query-override.css` to defend cross-preference conflicts.

## Testing checklist (what QA should run)
- Reproduce on Windows (system light) with app theme set to dark: visit all affected pages (reports, trip tickets, admin, filters) and verify no white/light areas remain.
- Reproduce on macOS (system dark) with app theme set to dark: verify no regression.
- Use DevTools on a failing element and confirm the computed style source is one of the `theme-override.css` or `media-query-override.css` entries and not a component-level `@media` block.
- Check critical components:
  - `.p-paginator` (report pages) — background, page numbers, highlighted page
  - `.p-multiselect`, `.p-dropdown` — closed state background, panel background, item hover
  - Native `select`, `input[type=time|date]`

## Long-term recommendations
1. Establish a theming pattern: components should not rely solely on `@media (prefers-color-scheme: dark)` for app-level theme. Use `:host-context(body.dark-theme)` or similar class-aware selectors.
2. Consolidate theme variables into a single theme tokens file and import where needed. Prefer setting variables on `html`/`body` (with the `:has()` trick) and avoid `:root` conflicting values.
3. When adding new components, enforce a lint or review step that flags `@media (prefers-color-scheme: dark)` use. Prefer class-based theming for app-level toggles.

## Quick debugging commands (local developer)
Run the dev server and open the app:

```bash
yarn install
yarn start
# or
ng serve --open
```

Open DevTools (F12) → inspect → Computed styles → check `background-color` and `color` and the source rule.

## Appendix: Common selectors to override (use as starter list)
- `body.dark-theme .p-paginator`
- `body.dark-theme .p-paginator .p-paginator-pages .p-paginator-page`
- `body.dark-theme .p-multiselect`, `.p-multiselect-panel`, `.p-multiselect-label`
- `body.dark-theme .p-dropdown`, `.p-dropdown-panel`
- `body.dark-theme select`, `body.dark-theme input[type=time|date|datetime-local]`
- `body.dark-theme .report-container`, `.report-header`, `.report-data`, `.filter-panel`

## If overrides don't win: debugging tips
1. Check load order in `index.html` — override files must load after other CSS.
2. Inspect the compiled selector specificity. If component styles are scoped with long selectors, prefer overriding variables instead of raw properties, or increase selector specificity (e.g., `html body.dark-theme .component .sub { ... }`).
3. As last resort, use `!important` on the variable or property, but keep it targeted.

---
End of summary. Place this file at the repo root (`DARK_MODE_GLOBAL_SUMMARY.md`) as the single source-of-truth for dark-mode cross-browser fixes.
