# Codebook

This codebook provides controlled vocabularies and code tables for the dataset/template described in this data package.

## Scope

- Dataset/template: [global-trip-template.xlsx](trip-exchange-front-end/import-export/global-trip-template.xlsx)
- The primary data dictionary for columns is in [DATA_DICTIONARY.md](DATA_DICTIONARY.md).
- Source: values extracted from the `Instructions` sheet of the template workbook.

---

## Mobility Aids

Codes used in the `Mobility Aids` column.

| Code | Meaning |
| ---- | ------- |
| A | Ambulatory |
| AL | Ambulatory lift |
| C | Cane |
| CU | Crutches |
| E | Electric wheelchair |
| S | Scooter |
| W | Wheelchair |
| WA | Walker |
| WAK | Knee walker |
| WE | Extended leg wheelchair |
| WT | Transport chair (must transfer) |
| WW | Wide wheelchair |

---

## Trip Purpose

Allowed values for the `Trip Purpose` column.

| Value | Explanation |
| ----- | ----------- |
| Adult Day Program | |
| Dialysis | |
| Employment | |
| Educational | |
| Grocery | |
| Health related | Includes dentist, pharmacy, vision care, etc. |
| Medical | |
| Meal Program | |
| Miscellaneous | |
| Personal | Includes personal care, social, etc. |
| Recreation | |
| Public transit | |

---

## Service Needs

Codes used in the `Service Needs` column.

| Code | Meaning |
| ---- | ------- |
| D2D | Door to door |
| DA | Driver Alert |
| DTD | Door through door (implies arm assist) |
| HI | Hearing impaired |
| IDD | Intellectually or developmentally disability |
| MI | Dementia (memory impaired) |
| NLA | Never leave alone / no leave alone |
| O | Oxygen |
| SA | Service Animal |
| SD | Seizure disorder |
| SI | Speech impaired |
| TD | Temporary Disability |
| U | Unstable, needs assistance |
| VI | Vision impaired |

---

## Address Abbreviations

USPS Standard Suffix Abbreviations (case-insensitive). Use these when normalizing address fields.

| Abbreviation | Meaning |
| ------------ | ------- |
| APT | Apartment |
| AVE | Avenue |
| BLDG | Building |
| BLVD | Boulevard |
| CTR | Center |
| CIR | Circle |
| CT | Court |
| DR | Drive |
| E | East |
| HWY | Highway |
| MHP | Mobile Home Park |
| N | North |
| PKWY | Parkway |
| S | South |
| SPC | Space |
| STE | Suite |
| ST | Street |
| SVC | Service |
| TH | Town Home |
| TRL | Trail |
| TRLR | Trailer |
| WAY | Way |
| W | West |

**Notes on address fields:**

- Second address line is for information not officially part of the address (e.g., "2nd floor").
- The "notes" field is for descriptive information (e.g., "green door around back", "south entrance").
- The address name is the proper name of the building (e.g., McDonalds, Target, Walgreens).

---

## Customer Field Explanations

| Data Element | Explanation |
| ------------ | ----------- |
| Customer key | Customer ID |
| Customer first name/legal name | Legal name as written on documents such as passport |
| Customer nickname | The name they prefer to be called |
| Funding type | Indicators for different billing groups (e.g., Medicaid) |
| Other (free field) | Customer service needs |
| Low income | At or below poverty level |
| Disability | Person with disability |
| Ethnicity | Hispanic/Latino OR not Hispanic/Latino |

---

## Status / Next Steps

- If multiple values can appear in a single cell (e.g., semicolon-delimited lists), document the delimiter and parsing rules here.
- Add any new code tables as they are defined.
