# Data Dictionary — Global Trip Template

This data dictionary describes the columns in the global trip template workbook.

- Source: [global-trip-template.xlsx](trip-exchange-front-end/import-export/global-trip-template.xlsx)
- Sheet: `Global Trip Template`
- Convention: fields with `*` in the column header are required.
- Controlled vocabularies: see [CODEBOOK.md](CODEBOOK.md) for allowed values.

## Columns

| Column | Data Type | Size/Format | Required | Nullability | Controlled Vocabulary | Notes |
| ------ | --------- | ----------- | :------: | :---------: | :-------------------: | ----- |
| Trip Id | String | — | No | Yes | — | Unique trip identifier assigned by provider |
| Provider Name | String | — | No | Yes | — | Name of the transportation provider |
| Trip date | Date | YYYY-MM-DD | Yes | No | — | Header is `Trip date*` |
| Pick-Up or Drop-Off | String | — | Yes | No | — | Header is `Pick-Up or Drop-Off*`; indicates leg type |
| Trip Time | Time | HH:MM | Yes | No | — | Header is `Trip Time*` |
| Pick-Up Window | String | — | No | Yes | — | Time window for pick-up |
| Vehicle Requirement | String | — | Yes | No | TBD | Header is `Vehicle Requirement*` |
| Trip Purpose | String | — | No | Yes | See CODEBOOK | Trailing space in original header |
| Mobility Aids | String | — | No | Yes | See CODEBOOK | Mobility aid codes (comma-delimited if multiple) |
| Service Needs | String | — | No | Yes | See CODEBOOK | Service need codes (comma-delimited if multiple) |
| Pick-up Location Name | String | — | No | Yes | — | Proper name of pick-up location |
| Pick-up Location Street | String | — | Yes | No | — | Header is `Pick-up Location Street*` |
| Pick-up location - City | String | — | Yes | No | — | Header is `Pick-up location - City*` |
| Pick-up location - State | String | 2-char | No | Yes | — | US state abbreviation |
| Pick-up location - County | String | — | Yes | No | — | Header is `Pick-up location - County*` |
| Pick-up location - ZIP code | String | 5 or 9 digits | Yes | No | — | Header is `Pick-up location - ZIP code*` |
| Pick-up Lat | Decimal | ±DD.DDDDDD | Yes | No | — | Header is `Pick-up Lat*`; latitude |
| Pick-up Lon | Decimal | ±DDD.DDDDDD | Yes | No | — | Header is `Pick-up Lon*`; longitude |
| Drop-off location - Name | String | — | No | Yes | — | Proper name of drop-off location |
| Drop-off location - Street | String | — | Yes | No | — | Header is `Drop-off location - Street*` |
| Drop-off location - City | String | — | Yes | No | — | Header is `Drop-off location - City*` |
| Drop-off location - State | String | 2-char | No | Yes | — | US state abbreviation |
| Drop-off location - County | String | — | Yes | No | — | Header is `Drop-off location - County*` |
| Drop-off location - ZIP code | String | 5 or 9 digits | Yes | No | — | Header is `Drop-off location - ZIP code*` |
| Drop-off Lat | Decimal | ±DD.DDDDDD | Yes | No | — | Header is `Drop-off Lat*` |
| Drop-off Lon | Decimal | ±DDD.DDDDDD | Yes | No | — | Header is `Drop-off Lon*` |
| Customer key | String | — | Yes | No | — | Header is `Customer key *`; unique customer ID |
| Customer first name/legal name | String | — | Yes | No | — | Header has non-breaking space; legal name |
| Customer nickname | String | — | No | Yes | — | Preferred name |
| Customer middle name | String | — | No | Yes | — | |
| Customer last name | String | — | Yes | No | — | Header is `Customer last name *` |
| Date of birth | Date | YYYY-MM-DD | No | Yes | — | |
| Gender | String | — | No | Yes | TBD | |
| Poverty Level | String | — | No | Yes | TBD | |
| Disability | String | — | No | Yes | TBD | Person with disability indicator |
| Language | String | — | No | Yes | TBD | Preferred language |
| Race | String | — | No | Yes | TBD | |
| Ethnicity | String | — | No | Yes | See CODEBOOK | Hispanic/Latino or not |
| Email address | String | — | No | Yes | — | |
| Veteran Status | String | — | No | Yes | TBD | |
| Customer home address | String | — | No | Yes | — | |
| Customer home phone | String | — | Yes | No | — | Header is `Customer home phone *` |
| Customer cell phone | String | — | No | Yes | — | |
| Mailing/Billing address (customer) | String | — | No | Yes | — | |
| Caregiver's contact name | String | — | No | Yes | — | |
| Caregiver's contact phone number | String | — | No | Yes | — | |
| Customer's emergency contact name | String | — | No | Yes | — | |
| Customer's emergency contact relationship with customer | String | — | No | Yes | — | |
| Customer's emergency phone number | String | — | No | Yes | — | |
| Comment about care required (hands-off) | String | — | No | Yes | — | |
| Fare type | String | — | No | Yes | TBD | |
| Billing information (funding entity) | String | — | No | Yes | — | |
| Funding type | String | — | No | Yes | See CODEBOOK | e.g., Medicaid |
| Other (free field) | String | — | No | Yes | — | Free-form notes |
| Status | String | — | Yes | No | TBD | Header is `Status*`; trip status |
| No-Show Reason | String | — | No | Yes | TBD | |
| Number of Guests | Integer | — | No | Yes | — | |
| Number of Passengers | Integer | — | No | Yes | — | |
| Fare Collected | Decimal | Currency | No | Yes | — | |
| Vehicle ID | String | — | No | Yes | — | |
| Driver ID | String | — | No | Yes | — | |
| Actual pick-up arrive time | DateTime | — | No | Yes | — | |
| Actual pick-up depart time | DateTime | — | No | Yes | — | |
| Actual drop-off arrive time | DateTime | — | No | Yes | — | |
| Actual drop-off depart time | DateTime | — | No | Yes | — | |

## Missing Data Codes

For populated datasets, use the following conventions (customize as needed):

| Code | Meaning |
| ---- | ------- |
| (blank) | Not collected / not applicable |
| N/A | Not applicable |
| UNK | Unknown |

## Notes / Next Steps

- If this template is converted to CSV for preservation, preserve the column names exactly (including punctuation), but consider normalizing whitespace (e.g., remove trailing spaces) and documenting any normalization here.
