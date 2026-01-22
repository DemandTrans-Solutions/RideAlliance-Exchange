# README for `Clearinghouse Trip Exchange — Global Trip Template (supporting dataset)`  

Bureau of Transportation Statistics (BTS), U.S. Department of Transportation (USDOT)  
2026-01-21  

## Links to Dataset  

Dataset Archive Link: <TBD DOI / persistent identifier>  
Series Archive Link: <TBD Series DOI / persistent identifier>  

## Summary of Dataset  

This data package documents the **Global Trip Template** used by the Clearinghouse Trip Exchange project to standardize tabular trip exchange/import/export data. The template provides a consistent set of fields (column headers) representing a trip record, including trip details, pickup/drop-off locations, rider attributes, and operational status fields.

A. [General Information](#a-general-information)  
B. [Sharing/Access & Policies Information](#b-sharingaccess-and-policies-information)  
C. [Data and Related Files Overview](#c-data-and-related-files-overview)  
D. [Methodological Information](#d-methodological-information)  
E. [Data-Specific Information](#e-data-specific-information)  
F. [Update Log](#f-update-log)  

**Title of Dataset:**  `Clearinghouse Trip Exchange — Global Trip Template (supporting dataset)`  

**Description of the Dataset:** This package provides the Global Trip Template workbook and supporting documentation (data dictionary, codebook, DCAT-US metadata pointer, and DMP pointer) to enable discovery, reuse, and long-term preservation.  

**Dataset Archive Link:** <TBD DOI / persistent identifier>  

## A. General Information  

**Authorship Information:**  

> *Principal Data Creator or Data Manager Contact Information*  
> Name: `Todd Voirol`  
> Institution: `DemandTrans Solutions`  
> Email: `todd.voirol@demandtrans.com`  

> *Data Distributor Contact Information*  
> Name: `DemandTrans Solutions (project team)`  
> Institution: `DemandTrans Solutions`  
> Email: `todd.voirol@demandtrans.com`  

> *Organizational Contact Information*  
> Name: `DemandTrans Solutions`  
> Institution: `DemandTrans Solutions`  
> Email: `todd.voirol@demandtrans.com`  

**Date of data collection and update interval:** `2025-06-29 initial template; updated irregularly as requirements evolve`  

**Geographic location of data collection:** `United States (template; not location-specific)` [(GeoNames URI: http://sws.geonames.org/6252001/)](http://sws.geonames.org/6252001/)  

**Information about funding sources that supported the collection of the data:** `USDOT SMART grant-supported project work (award/grant number TBD).`  

## B. Sharing/Access and Policies Information  

**Recommended citation for the data:**  

> `DemandTrans Solutions` (2026). *`Clearinghouse Trip Exchange — Global Trip Template (supporting dataset)`*. <TBD DOI / persistent identifier>  

**Licenses/restrictions placed on the data:** This document is disseminated under the sponsorship of the U.S. Department of Transportation in the interest of information exchange. The United States Government assumes no liability for the contents thereof.

**Data License:** [Creative Commons 4.0 International Attribution (CC BY 4.0)](https://creativecommons.org/licenses/by/4.0/)

**Rights Statement:** Open Use per USDOT funding with Attribution (Creative Commons 4.0/CC BY 4.0): This dataset is open access per USDOT funding. No restrictions on access or use. Users may share, copy, and redistribute in any medium with the condition they attribute to this source.

**Was data derived from another source?:** `No`  

This document was created to meet the requirements enumerated in the U.S. Department of Transportation's [Plan to Increase Public Access to the Results of Federally-Funded Scientific Research Version 1.1](https://doi.org/10.21949/1520559) and [Guidelines suggested by the DOT Public Access website](https://doi.org/10.21949/1503647), in effect and current as of December 03, 2020.  

## C. Data and Related Files Overview  

File List for the `Clearinghouse_Trip_Exchange_Data_Package`  

> 1. Filename: [global-trip-template.xlsx](trip-exchange-front-end/import-export/global-trip-template.xlsx)
> Short Description:  Global trip template workbook used to structure trip exchange/import/export tabular data.  

> 1. Filename: [data.json](data.json)
> Short Description:  DCAT-US v1.1 metadata file for federal search/discovery (data.gov / transportation.data.gov compatibility).  

> 1. Filename: [DATA_DICTIONARY.md](DATA_DICTIONARY.md)  
> Short Description:  Data dictionary describing the columns in the Global Trip Template workbook.  

> 1. Filename: [CODEBOOK.md](CODEBOOK.md)
> Short Description:  Codebook / controlled vocabulary notes (e.g., mobility aid code lists referenced by the template).  

> 1. Filename: [DMP.md](DMP.md)
> Short Description:  Data Management Plan content aligned to USDOT SMART / DOT recipient reporting guidance.  

> 1. Filename: [CODE_SCRIPTS.md](CODE_SCRIPTS.md)
> Short Description:  Inventory of scripts and supporting materials for working with the template and related artifacts.  


## D. Methodological Information  

**Description of methods used for collection/generation of data:** The Global Trip Template workbook was created by the project team as a standardized schema/template for trip exchange data. The dataset itself is generated by populating one row per trip record following the defined column headers; required fields are indicated in the template header naming convention (e.g., `*`).  

**Instrument or software-specific information needed to interpret the data:** The template is provided as an `.xlsx` workbook and can be opened with Microsoft Excel or compatible spreadsheet software (e.g., LibreOffice). For long-term preservation and interoperability, tabular data should be exported to CSV when feasible.

**Best practice for long-term preservation:** CSV (.csv) is the recommended file format for long-term preservation of tabular data because it is open, non-proprietary, backward and forward compatible, and long-term preservable. For machine readability and interoperability, data files should have only two types of rows: a single header row, and all other rows as data.  

## E. Data-Specific Information  

1. `trip-exchange-front-end/import-export/global-trip-template.xlsx`  

- Number of variables (columns): `65`  
- Number of cases/rows: `Template workbook; Global Trip Template sheet contains 1 header row and 3 empty example rows`  
- Each row represents: `One trip record`  
- Data Dictionary/Variable List: `DATA_DICTIONARY.md`  
- Missing data codes: `Not applicable for template; for populated datasets, document missing/skip codes if used`  

## F. Update Log  

This README.md file was originally created on `2026-01-21` by `Todd Voirol`, `Project contributor`, `DemandTrans Solutions` <`todd.voirol@demandtrans.com`>  
 
`2026-01-21`: Original file created  
