# Data Management Plan (DMP) — Stage 1 (SMART / USDOT)

This file captures the Stage 1 DMP content required by the USDOT SMART recipient reporting guidance (see the reference PDF in [context/Recipient Reporting Guidance_Data Management Plan_022726.pdf](../context/Recipient%20Reporting%20Guidance_Data%20Management%20Plan_022726.pdf)).

## 1. Dataset and Contact Information

- **Project name:** Clearinghouse Trip Exchange (Angular front-end)
- **Grant number / FAIN:** (TBD)
- **DMP submitter:** [todd.voirol@demandtrans.com](mailto:todd.voirol@demandtrans.com)
- **Submitter email / phone:** [todd.voirol@demandtrans.com](mailto:todd.voirol@demandtrans.com) / (TBD)
- **Submitter organization:** DemandTrans Solutions
- **Organization email / phone:** (TBD)
- **Project/repository website:** [https://github.com/DemandTrans-Solutions/trip-exchange-front-end](https://github.com/DemandTrans-Solutions/trip-exchange-front-end)
- **Date DMP written:** 2026-01-21

## 2. Data Description

- This project produces software source code and data exchange templates used to structure trip exchange/import/export data.
- Expected nature/scope/scale: primary deliverables include XLSX templates and related documentation; operational datasets (if any) are expected to be tabular trip records.
- Disclosure risk: operational trip datasets may contain PII (names, phone numbers, addresses, DOB, etc.). Public releases will exclude PII or apply de-identification/restrictions as necessary.
- Long-term value: standardized templates and documentation support interoperability, reuse, and reproducibility.

## 3. Data Format and Metadata Standards Employed

### File formats

To the maximum extent practicable, data outputs will be published in platform-independent, non-proprietary formats.

- Preferred tabular preservation format: **CSV**
- Other open formats used when appropriate: TXT, XML, JSON, PDF, JPEG, PNG
- XLSX is used for data entry convenience; preservation deposits should include CSV equivalents when feasible.

### Metadata standards

- **DCAT-US v1.1 (required):** a `.JSON` metadata file for federal data search and discovery.
- **Other potential metadata schemas (if relevant):** CSV on the Web (CSVW) may be used to document tabular structures.

Required statement (per DOT guidance):

The final data will have a DCAT-US v1.1 ([https://resources.data.gov/resources/dcat-us/](https://resources.data.gov/resources/dcat-us/)) .JSON metadata file, which is the federal standard for data search and discovery to be compliant with the USDOT Public Access Plan.

## 4. Access Policies

- Default to open access when appropriate.
- Sensitive data handling: protect PII/CBI; if data cannot be de-identified while maintaining utility, document restrictions and access procedures.
- Stewardship responsibilities: (TBD)

## 5. Re-use, Redistribution, and Derivative Products Policies

- Intellectual property: Data outputs will be made available for public use.
- Licensing:
  - **Recommended data license:** Creative Commons 4.0 International Attribution (CC BY 4.0)
  - **Rights statement:** "Open Use per USDOT funding with Attribution (Creative Commons 4.0/CC BY 4.0): This dataset is open access per USDOT funding. No restrictions on access or use. Users may share, copy, and redistribute in any medium with the condition they attribute to this source."
  - Source code license is as stated in the project's `package.json`.
- USDOT rights: recipients grant USDOT a comprehensive non-exclusive, paid-up, royalty-free copyright license for project outputs (publications, datasets, software, code, etc.).

## 6. Archiving and Preservation Plan

- Preservation repository: **National Transportation Library (NTL)** ([https://ntl.bts.gov/](https://ntl.bts.gov/))
- Repository link: (TBD for the specific deposited dataset record)
- Minimum metadata for discoverability: DCAT-US v1.1 metadata will be provided.
- Persistent identifiers: a DOI/handle (or equivalent persistent identifier) will be assigned upon deposit and maintained through the preservation lifecycle.
