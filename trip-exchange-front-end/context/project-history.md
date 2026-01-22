# Project History

## Background

The [README.md](../README.md) file references the technical setup details and a high-level overview of this project.

This project was initially an Angular 2.x project and has since been migrated to Angular 18. The steps taken during that migration are detailed in [update-guide.md](./update-guide.md)

The migration also consisted of a modernization effort to transition to a clean modernized look and feel. Styles are stored in multiple SCSS and CSS files in the project. Each component has its own SCSS file as well and general SCSS files stored in /src/app/styles, a high-level [styles.css](../src/app/styles.css) file, [custom.css](../src/assets/css/custom.css), [modern-theme.css](../src/assets/css/modern-theme.css) and [theme-override.css](../src/assets/css/theme-override.css)

[CSS_USAGE.md](./CSS_USAGE.md) details details how the styles are used in the project.

Icon usage also migrated from font-awesome to the more modern [Phosphur](https://phosphoricons.com/) implementation.

All datatables the p-tables component in allowing users to interact with data.

The backend is a REST-based Spring Boot 3.4 implementation. Authorization uses JWT tokens.

There are multiple other *.md files in the current context directory detailing other updates made in the project since the initial Angular 18 migration was implemented.
