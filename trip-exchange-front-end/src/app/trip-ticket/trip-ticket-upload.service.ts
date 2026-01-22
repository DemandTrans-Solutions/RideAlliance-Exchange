import { Injectable } from '@angular/core';
import * as XLSX from 'xlsx';
import moment from 'moment';
import { Subject } from 'rxjs';

// Import types and models needed
import { AddressDTO } from '../models/address.dto';
import { TripTicketRequestDTO } from '../models/trip-ticket-request.dto';
import { TripTicketHeaderValidationResult } from './trip-ticket-upload.utils';

export type TripTicketUploadRecord = TripTicketRequestDTO & Record<string, any>;

// Excel template headers from trip-exchange-import-export-template.xlsx
export const EXCEL_TRIP_TEMPLATE_HEADERS: string[] = [
  'Case Number',
  'Actual Provider',
  'Trip Name',
  'Date & Time Of Trip',
  'Reason(s) for Trip',
  'Mobility Device',
  'Level of Service',
  'Origin Street',
  'Origin City',
  'Origin State',
  'Origin ZIP',
  'Final Stop Street',
  'Final Stop City',
  'Final Stop State',
  'Final Stop ZIP',
  'First Name*',
  'Nickname',
  'Middle Name',
  'Last Name*',
  'Date Of Birth*',
  'Gender*',
  'Poverty Level*',
  'Disability Type',
  'Primary Language',
  'Race*',
  'Ethnicity*',
  'Email',
  'Veteran Status*',
  'Home Address Line 1',
  'Home Phone',
  'Cell Phone*',
  'Billing Address',
  'Caregiver Details',
  'Primary Emergency Contact Name',
  'Primary Emergency Contact Phone Number',
  'Primary Emergency Contact Relationship',
  'Vehicle Type',
  'Pick-Up/Drop-Off Time',
  'If Cancelled, why?',
  'Scheduled Start',
  'Scheduled Ends',
  'Funding Source',
  'Cost of Trip',
  'Estimated Miles',
  'Pick-Up Lat',
  'Pick-up Long',
  'Drop-Off Lat',
  'Drop-Off Long',
  'Actual pick-up arrive time',
  'Actual pick-up depart time',
  'Actual drop-off arrive time',
  'Actual drop-off depart time',
  'Status'  ,
  'No-Show Reason',
  'Number of Guests',
  'Number of Passengers',
  'Fare Collected',
  'Vehicle ID',
  'Driver ID'
];

// CSV template headers from ecolane-global-mapping.csv (line 2)
export const CSV_TRIP_TEMPLATE_HEADERS: string[] = [
  'Trip_id',
  'Provider Name',
  'appt_date',
  'REQ_PU',
  'REQ_DO',
  'VEHICLE_REQ',
  'Purpose',
  'DEVICE',
  'PU_NOTE',
  'pu_location_name',
  'pu_street',
  'pu_city',
  'pu_state',
  'pu_county',
  'pu_zipcode',
  'lat_pu',
  'lon_pu',
  'do_location_name',
  'DO_STREET',
  'do_city',
  'do_state',
  'do_county',
  'do_zipcode',
  'lat_do',
  'lon_do',
  'CLIENT_ID',
  'CLIENT_F_NAME',
  'CLIENT_L_NAME',
  'customer_dob',
  'Gender',
  'Language',
  'Ethnicity',
  'Email address',
  'customer_phone',
  'pu_phone',
  'Fare_type',
  'FUNDING_SOURCE',
  'Status',
  'No-Show Reason',
  'num_attendant',
  'Fare Collected',
  'Vehicle ID',
  'Driver ID',
  'Actual pick-up arrive time',
  'Actual pick-up depart time',
  'Actual drop-off arrive time',
  'Actual drop-off depart time'
];

// Helper function to identify required Excel headers (ending with *)
export const getRequiredExcelHeaders = (): string[] => {
  return EXCEL_TRIP_TEMPLATE_HEADERS.filter(header => header.endsWith('*'));
};

// File format detection
export enum FileFormat {
  CSV = 'CSV',
  EXCEL = 'EXCEL'
}

export interface UploadValidationResult {
  isValid: boolean;
  errors: { row: number; errors: string[] }[];
}

export interface UploadState {
  selectedFile: File | null;
  uploadProgress: number;
  uploadError: string | null;
  isUploading: boolean;
  uploadSuccess: boolean;
  uploadResult: any;
  ticketCollection: TripTicketUploadRecord[];
  isDragging: boolean;
  errorMessage: string;
}

export interface UploadConfig {
  allowedFileTypes: string;
  maxFileSize: number;
  expectedHeaders: string[];
  uploadTemplatePath: string;
}

@Injectable({
  providedIn: 'root'
})
export class TripTicketUploadService {

  // Default configuration
  private readonly defaultConfig: UploadConfig = {
    allowedFileTypes: '.csv,.xlsx,.xls',
    maxFileSize: 10 * 1024 * 1024, // 10MB
    expectedHeaders: CSV_TRIP_TEMPLATE_HEADERS, // Use CSV headers as default
    // Correct path to downloadable Excel template (previously had duplicated 'src/assets/templates')
    uploadTemplatePath: 'assets/templates/trip-exchange-import-export-template.xlsx'
  };

  private uploadState: UploadState = {
    selectedFile: null,
    uploadProgress: 0,
    uploadError: null,
    isUploading: false,
    uploadSuccess: false,
    uploadResult: null,
    ticketCollection: [],
    isDragging: false,
    errorMessage: ''
  };

  // Observable for state changes
  private stateSubject = new Subject<UploadState>();
  public state$ = this.stateSubject.asObservable();

  constructor() { }

  /**
   * Get current upload state
   */
  getState(): UploadState {
    return { ...this.uploadState };
  }

  /**
   * Get upload configuration
   */
  getConfig(): UploadConfig {
    return { ...this.defaultConfig };
  }

  /**
   * Reset the upload state
   */
  resetUploadState(): void {
    this.uploadState = {
      selectedFile: null,
      uploadProgress: 0,
      uploadError: null,
      isUploading: false,
      uploadSuccess: false,
      uploadResult: null,
      ticketCollection: [],
      isDragging: false,
      errorMessage: ''
    };
    this.emitState();
  }

  /**
   * Process file selection and validation
   */
  async processFileSelection(
    file: File,
    selectedOriginatingProvider: string | null,
    loggedProviderId: any
  ): Promise<{success: boolean, error?: string}> {

    const fileExtension = file.name.split('.').pop()?.toLowerCase();

    // File size validation
    if (file.size > this.defaultConfig.maxFileSize) {
      const error = `File size exceeds the maximum allowed size (${this.defaultConfig.maxFileSize / (1024 * 1024)}MB)`;
      this.uploadState.uploadError = error;
      this.emitState();
      return { success: false, error };
    }

    // File type validation
    if (!fileExtension || !['csv', 'xlsx', 'xls'].includes(fileExtension)) {
      const error = 'Invalid file type. Please upload a CSV or Excel file.';
      this.uploadState.uploadError = error;
      this.emitState();
      return { success: false, error };
    }

    // Provider validation
    const pid = parseInt(String(selectedOriginatingProvider), 10);
    if (isNaN(pid) || pid <= 0) {
      const error = 'Provider must be selected for upload.';
      this.uploadState.uploadError = error;
      this.emitState();
      return { success: false, error };
    }

    try {
      // Determine file format from extension (fileExtension is the suffix without dot)
      const fileFormat = (fileExtension === 'xlsx' || fileExtension === 'xls') ? FileFormat.EXCEL : FileFormat.CSV;

      const { headers, records } = await this.parseUploadFile(file, fileExtension);

      const headerValidation = this.validateHeaders(headers, fileFormat);

      if (!headerValidation.isValid) {
        const error = this.buildTemplateErrorMessage(headerValidation);
        this.uploadState.uploadError = error;
        this.emitState();
        return { success: false, error };
      }

      // Validate required data in each row for Excel files
      if (fileFormat === FileFormat.EXCEL) {
        const rowValidation = this.validateExcelRowData(records, headers);
        if (!rowValidation.isValid) {
          const error = rowValidation.error || 'Excel validation failed';
          this.uploadState.uploadError = error;
          this.emitState();
          return { success: false, error };
        }
      }

      this.uploadState.selectedFile = file;
      this.uploadState.uploadError = null;

      // Transform records and attach provider information
      const transformedCollection = records.map((record: Record<string, any>) => {
        const dto = this.transformToTripTicketDTO(record, loggedProviderId, fileFormat);
        if (selectedOriginatingProvider) {
          (dto as any).origin_provider_id = pid;
          (dto as any).originProviderId = pid;
        }
        return dto;
      });

      const validationResult = this.validateTicketCollection(transformedCollection);

      if (!validationResult.isValid) {
        const errorMessages = validationResult.errors.map(error => `Row ${error.row}: ${error.errors.join(', ')}`);
        const error = `Validation errors found:\n${errorMessages.join('\n')}`;
        this.uploadState.uploadError = error;
        this.uploadState.ticketCollection = [];
        this.emitState();
        return { success: false, error };
      }

      if (transformedCollection.length === 0) {
        const error = 'The uploaded file is empty. Please use the provided template.';
        this.uploadState.uploadError = error;
        this.uploadState.ticketCollection = [];
        this.uploadState.selectedFile = null;
        this.emitState();
        return { success: false, error };
      }

      this.uploadState.ticketCollection = transformedCollection;
      this.emitState();
      return { success: true };

    } catch (error) {
      const errorMsg = 'Failed to parse file. Please verify that it matches the latest template format.';
      this.uploadState.uploadError = errorMsg;
      this.uploadState.ticketCollection = [];
      this.uploadState.selectedFile = null;
      this.emitState();
      console.error('File parsing error:', error);
      return { success: false, error: errorMsg };
    }
  }

  /**
   * Clear selected file and reset related state
   */
  clearSelectedFile(): void {
    this.uploadState.selectedFile = null;
    this.uploadState.ticketCollection = [];
    this.uploadState.uploadSuccess = false;
    this.emitState();
  }

  /**
   * Set dragging state
   */
  setDragging(isDragging: boolean): void {
    this.uploadState.isDragging = isDragging;
    this.emitState();
  }

  /**
   * Set upload progress
   */
  setUploadProgress(progress: number): void {
    this.uploadState.uploadProgress = progress;
    this.emitState();
  }

  /**
   * Set uploading state
   */
  setUploading(isUploading: boolean): void {
    this.uploadState.isUploading = isUploading;
    this.emitState();
  }

  /**
   * Set upload success state
   */
  setUploadSuccess(success: boolean, result?: any): void {
    this.uploadState.uploadSuccess = success;
    if (result) {
      this.uploadState.uploadResult = result;
    }
    this.emitState();
  }

  /**
   * Set upload error
   */
  setUploadError(error: string | null): void {
    this.uploadState.uploadError = error;
    this.emitState();
  }

  /**
   * Parse uploaded file (CSV or Excel)
   */
  private parseUploadFile(
    file: File,
    extension: string
  ): Promise<{ headers: string[]; records: Record<string, any>[] }> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();

      reader.onerror = () => reject(reader.error ?? new Error('Unable to read file.'));
      reader.onload = event => {
        try {
          const result = event.target?.result;
          if (result === undefined || result === null) {
            reject(new Error('File content is empty.'));
            return;
          }

          if (extension === 'csv') {
            resolve(this.parseCsvContent(result as string));
          } else {
            resolve(this.parseExcelContent(result as ArrayBuffer));
          }
        } catch (error) {
          reject(error);
        }
      };

      if (extension === 'csv') {
        reader.readAsText(file);
      } else {
        reader.readAsArrayBuffer(file);
      }
    });
  }

  /**
   * Parse CSV content
   */
  private parseCsvContent(csvText: string): { headers: string[]; records: Record<string, any>[] } {
    // Normalize CSV text: strip UTF-8 BOM if present and trim leading/trailing whitespace
    if (!csvText) {
      return { headers: [], records: [] };
    }

    // Remove UTF-8 BOM (\uFEFF) which can cause header mismatches
    let normalized = csvText.replace(/^\uFEFF/, '');
    // Trim only leading newlines/spaces that might precede header row
    normalized = normalized.replace(/^\s+/, '');

    // Use raw: true to prevent XLSX from parsing dates/numbers during the read phase.
    // This ensures "2025-11-25" stays as a string and isn't converted to a serial number
    // or Date object with potential timezone shifts.
    const workbook = XLSX.read(normalized, { type: 'string', raw: true });
    return this.extractSheetData(workbook);
  }

  /**
   * Parse Excel content
   */
  private parseExcelContent(arrayBuffer: ArrayBuffer): { headers: string[]; records: Record<string, any>[] } {
    const workbook = XLSX.read(new Uint8Array(arrayBuffer), { type: 'array' });
    return this.extractExcelSheetData(workbook);
  }

  /**
   * Extract data from Excel sheet - intelligently finds header row by looking for "Case Number"
   */
  private extractExcelSheetData(workbook: XLSX.WorkBook): { headers: string[]; records: Record<string, any>[] } {
    // Search all sheets for the one containing our headers
    let targetSheet: XLSX.WorkSheet | null = null;
    let headerRowIndex = -1;
    let headerColStart = 0;

    for (const sheetName of workbook.SheetNames) {
      const sheet = workbook.Sheets[sheetName];
      if (!sheet) continue;

      // Convert entire sheet to array of arrays to search for "Case Number"
      const sheetData = XLSX.utils.sheet_to_json(sheet, { header: 1, raw: false, defval: '' }) as (string | number)[][];

      // Search for "Case Number" in any cell
      for (let rowIdx = 0; rowIdx < sheetData.length; rowIdx++) {
        const row = sheetData[rowIdx];
        for (let colIdx = 0; colIdx < row.length; colIdx++) {
          const cellValue = String(row[colIdx] || '').trim();
          if (cellValue === 'Case Number') {
            targetSheet = sheet;
            headerRowIndex = rowIdx;
            headerColStart = colIdx;
            break;
          }
        }
        if (targetSheet) break;
      }
      if (targetSheet) break;
    }

    if (!targetSheet || headerRowIndex === -1) {
      throw new Error('Excel file must contain a header row with "Case Number" as the first column header.');
    }

    // Extract headers from the worksheet directly using the sheet's range so we don't
    // lose columns after "Estimated Miles" when arrays are sparse.
    const headers: string[] = [];
    const ref = targetSheet['!ref'];
    const range = ref
      ? XLSX.utils.decode_range(ref)
      : { s: { r: headerRowIndex, c: headerColStart }, e: { r: headerRowIndex, c: headerColStart } };

    for (let col = headerColStart; col <= range.e.c; col++) {
      const cellAddress = XLSX.utils.encode_cell({ r: headerRowIndex, c: col });
      const cell = targetSheet[cellAddress];
      const val = cell ? String(XLSX.utils.format_cell(cell)).trim() : '';
      headers.push(val);
    }

    // remove purely-empty headers but keep positional alignment with data columns
    const cleanedHeaders = headers.map(h => h || '').map(h => h.trim());

    // Extract data rows (everything after the header row), starting at the identified column
    const records: Record<string, any>[] = [];
    for (let rowIdx = headerRowIndex + 1; rowIdx <= range.e.r; rowIdx++) {
      const record: Record<string, any> = {};
      let hasValue = false;

      // Map each cell in the data row to its corresponding header
      for (let colIdx = 0; colIdx < cleanedHeaders.length; colIdx++) {
        const header = cleanedHeaders[colIdx];
        if (!header) {
          continue; // skip columns without headers
        }

        const cellAddress = XLSX.utils.encode_cell({ r: rowIdx, c: headerColStart + colIdx });
        const cell = targetSheet[cellAddress];
        const cellValue = cell ? XLSX.utils.format_cell(cell) : '';

        let value: any = cellValue;
        if (typeof cellValue === 'string') {
          value = cellValue.trim();
        }

        if (value !== '' && value !== null && value !== undefined) {
          hasValue = true;
        }

        record[header] = value ?? '';
      }

      // Skip rows that are completely empty
      if (!hasValue) {
        continue;
      }

      // Skip summary/total rows - check if "Case Number" column contains summary keywords
      const caseNumberValue = String(record['Case Number'] || '').trim().toLowerCase();
      const summaryKeywords = ['total', 'sum', 'count', 'average', 'subtotal', 'confidential', 'copyright'];
      const isSummaryRow = summaryKeywords.some(keyword => caseNumberValue.includes(keyword));

      if (isSummaryRow) {
        continue;
      }

      // Skip rows with empty Case Number (likely template rows or separators)
      if (!caseNumberValue) {
        continue;
      }

      records.push(record);
    }

    const nonEmptyHeaders = cleanedHeaders.filter(h => h !== '');
    return { headers: nonEmptyHeaders, records };
  }

  /**
   * Extract data from sheet (generic method for CSV)
   */
  private extractSheetData(workbook: XLSX.WorkBook): { headers: string[]; records: Record<string, any>[] } {
    const firstSheetName = workbook.SheetNames[0];
    if (!firstSheetName) {
      return { headers: [], records: [] };
    }

    const firstSheet = workbook.Sheets[firstSheetName];
    const sheetRows = XLSX.utils.sheet_to_json(firstSheet, { header: 1, raw: false, defval: '' }) as (string | number)[][];
    const headerRow = sheetRows[0] ?? [];
    const headers = headerRow.map(cell => String(cell).trim());

    // Use raw:true to prevent XLSX from coercing date strings (like '2025-11-25')
    // into Date objects that may shift with timezone. We'll keep strings as-is.
    const dataRows = XLSX.utils.sheet_to_json(firstSheet, { raw: true, defval: '' }) as Record<string, any>[];
    const records: Record<string, any>[] = [];

    dataRows.forEach(row => {
      const record: Record<string, any> = {};
      let hasValue = false;

      headers.forEach(header => {
        const rawValue = row[header];
        let value: any = rawValue;

        // Preserve CSV date strings; if a Date slipped in, convert to YYYY-MM-DD string (local, no TZ)
        if (rawValue instanceof Date) {
          // Use UTC parts to avoid timezone shifts changing the day
          const pad = (n: number) => (n < 10 ? '0' + n : String(n));
          value = `${rawValue.getUTCFullYear()}-${pad(rawValue.getUTCMonth() + 1)}-${pad(rawValue.getUTCDate())}`;
        } else if (typeof rawValue === 'number') {
          // If an Excel serial number sneaks in, convert to a safe date string
          const n = rawValue;
          if (n > 25568 && n < 60000) { // heuristic range
            // Compute in UTC to avoid TZ shifts
            const excelEpoch = Date.UTC(1899, 11, 30);
            const days = Math.floor(n);
            const ms = Math.round((n - days) * 86400000);
            const t = excelEpoch + days * 86400000 + ms;
            const d = new Date(t);
            const pad = (x: number) => (x < 10 ? '0' + x : String(x));
            value = `${d.getUTCFullYear()}-${pad(d.getUTCMonth() + 1)}-${pad(d.getUTCDate())}`;
          }
        } else if (typeof rawValue === 'string') {
          value = rawValue.trim();
        }

        if (value !== '' && value !== null && value !== undefined) {
          hasValue = true;
        }

        record[header] = value ?? '';
      });

      if (hasValue) {
        records.push(record);
      }
    });

    return { headers, records };
  }

  /**
   * Normalize header string for comparison - removes spaces, symbols, and converts to lowercase
   */
  private normalizeHeaderForComparison(header: string): string {
    // Normalize to a compact alphanumeric-only lowercase string so
    // header comparisons are robust to punctuation, hyphens, parentheses,
    // and other non-alphanumeric differences coming from Excel.
    // Examples:
    //  - "Pick-Up Lat"  -> "pickuplat"
    //  - "Pick up Lat*" -> "pickuplat"
    //  - "Pick‑Up Lat"  -> "pickuplat" (handles different dash characters)
    return header
      .replace(/\*/g, '') // remove asterisks
      .replace(/["'`\u2018\u2019\u201A\u201B]/g, '') // remove quotes/apostrophes
      .replace(/\\/g, '') // remove backslashes
      .replace(/\s+/g, '') // remove whitespace
      .replace(/[^a-zA-Z0-9]/g, '') // remove any remaining non-alphanumeric characters
      .toLowerCase();
  }

  /**
   * Validate headers based on file format
   */
  private validateHeaders(headers: string[], fileFormat: FileFormat): TripTicketHeaderValidationResult {
    if (fileFormat === FileFormat.CSV) {
      // Only require a small subset of CSV headers (case-insensitive)
      const requiredCsvHeaders: string[] = [
        'Trip_id',
        'appt_date',
        'REQ_PU',
        'REQ_DO',
        'VEHICLE_REQ',
        'Purpose',
        'DEVICE',
        'PU_NOTE',
        'pu_location_name',
        'pu_street',
        'pu_city',
        'pu_state',
        'pu_county',
        'pu_zipcode',
        'lat_pu',
        'lon_pu',
        'do_location_name',
        'DO_STREET',
        'do_city',
        'do_state',
        'do_county',
        'do_zipcode',
        'lat_do',
        'lon_do',
        'CLIENT_F_NAME',
        'CLIENT_L_NAME',
        'customer_dob',
        'Gender',
        'Email address',
        'customer_phone',
        'Status',
        'num_attendant'
      ];

      const normalizedHeaders = headers.map(h => this.normalizeHeaderForComparison(h));

      // Find which required headers are missing (report using the canonical names)
      const missingRequired = requiredCsvHeaders.filter(req =>
        !normalizedHeaders.includes(this.normalizeHeaderForComparison(req))
      );

      return {
        isValid: missingRequired.length === 0,
        missingHeaders: missingRequired,
        unexpectedHeaders: [] // don't treat extra or other missing non-required columns as errors
      };
    } else if (fileFormat === FileFormat.EXCEL) {
      // Required Excel headers for Trip Exchange template
      const requiredHeaders = [
        'First Name',
        'Last Name',
        'Origin Street',
        'Origin City',
        'Pick-up Long',
        'Pick-Up Lat',
        'Drop-Off Lat',
        'Drop-Off Long',
        'Final Stop Street',
        'Final Stop City'
      ];

      // Normalize headers for comparison
      const normalizedUploadedHeaders = headers.map(h => this.normalizeHeaderForComparison(h));

      // Check for normalized matches of required headers
      const missingRequired = requiredHeaders.filter((requiredName: string) => {
        const normalizedRequired = this.normalizeHeaderForComparison(requiredName);
        return !normalizedUploadedHeaders.includes(normalizedRequired);
      });

      // If headers are missing, dump debug info to help trace mismatches (kept at debug level)
      if (missingRequired.length > 0) {
        try {
          console.debug('Excel header validation failed. Uploaded headers:', headers);
          console.debug('Normalized uploaded headers:', normalizedUploadedHeaders);
          console.debug('Required headers (checked):', requiredHeaders);
          console.debug('Missing required headers (original names):', missingRequired);
          console.debug('Missing required headers (normalized):', missingRequired.map(h => this.normalizeHeaderForComparison(h)));
        } catch (_e) {
          // Swallow debug-time errors
        }
      }

      return {
        isValid: missingRequired.length === 0,
        missingHeaders: missingRequired,
        unexpectedHeaders: []
      };
    }

    // Default fallback
    return { isValid: false, missingHeaders: ['Unknown file format'], unexpectedHeaders: [] };
  }



  /**
   * Validate that required Excel columns have data in each row
   */
  private validateExcelRowData(records: Record<string, any>[], headers: string[]): { isValid: boolean; error?: string } {
    // Required fields per user spec
    const requiredFields = [
      'First Name',
      'Last Name',
      'Origin Street',
      'Origin City',
      'Pick-Up Lat',
      'Pick-up Long',
      'Drop-Off Lat',
      'Drop-Off Long',
      'Final Stop Street',
      'Final Stop City'
    ];

    // Phone requirement: at least one of Home Phone OR Cell Phone
    const phoneFields = ['Home Phone', 'Cell Phone'];

    // Find actual header names using normalized comparison
    const actualRequiredHeaders = requiredFields.map(requiredName => {
      const normalizedRequired = this.normalizeHeaderForComparison(requiredName);
      return headers.find(h => this.normalizeHeaderForComparison(h) === normalizedRequired) || requiredName;
    });

    const actualPhoneHeaders = phoneFields.map(phoneName => {
      const normalizedPhone = this.normalizeHeaderForComparison(phoneName);
      return headers.find(h => this.normalizeHeaderForComparison(h) === normalizedPhone) || phoneName;
    });

    for (let i = 0; i < records.length; i++) {
      const row = records[i];
      const rowNumber = i + 1; // Row number in the data (1-indexed)

      // Check required fields
      for (const headerName of actualRequiredHeaders) {
        const value = row[headerName];
        if (!value || (typeof value === 'string' && value.trim() === '')) {
          return {
            isValid: false,
            error: `Data row ${rowNumber}: Required field '${headerName.replace('*', '')}' is empty or missing.`
          };
        }
      }

      // Check phone requirement: at least one must be populated
      const hasPhone = actualPhoneHeaders.some(phoneHeader => {
        const value = row[phoneHeader];
        return value && (typeof value !== 'string' || value.trim() !== '');
      });

      if (!hasPhone) {
        return {
          isValid: false,
          error: `Row ${rowNumber}: Either 'Home Phone' or 'Cell Phone' must be provided.`
        };
      }
    }

    return { isValid: true };
  }

  /**
   * Transform a data row into a TripTicketRequestDTO object
   */
  private transformToTripTicketDTO(row: Record<string, any>, loggedProviderId: any, fileFormat: FileFormat): TripTicketUploadRecord {
    console.log('Transforming row:', row);

    // Helper function to convert string to boolean
    const toBoolean = (value: string | undefined): boolean => {
      if (!value) return false;
      return value.toLowerCase() === 'true' || value.toLowerCase() === 'yes' || value === '1';
    };

    // Get value function that handles both CSV and Excel formats (normalized comparison)
    const get = (csvKey: string, excelKey?: string): any => {
      if (fileFormat === FileFormat.EXCEL && excelKey) {
        // Normalized lookup for Excel
        const normalizedExcelKey = this.normalizeHeaderForComparison(excelKey);

        for (const key in row) {
          const normalizedKey = this.normalizeHeaderForComparison(key);
          if (normalizedKey === normalizedExcelKey) {
            return row[key] || '';
          }
        }
        return '';
      }
      // For CSV, use normalized lookup
      if (csvKey) {
        const normalizedCsvKey = this.normalizeHeaderForComparison(csvKey);
        for (const key in row) {
          if (this.normalizeHeaderForComparison(key) === normalizedCsvKey) {
            return row[key] || '';
          }
        }
      }
      return '';
    };

    // Helper function to parse time string
    const parseTime = (timeStr: string | undefined): string | null => {
      if (!timeStr) return null;
      return timeStr.trim();
    };

    // Helper function to parse date-time string and split into date and time
    const parseDateTimeString = (dateTimeStr: string | undefined): { date: string; time: string } => {
      if (!dateTimeStr || typeof dateTimeStr !== 'string') {
        return { date: '', time: '' };
      }

      const trimmed = dateTimeStr.trim();
      // Expected format: "M/dd/yyyy hh:mm A" or similar variations
      // Split on space to separate date from time
      const parts = trimmed.split(' ');

      if (parts.length >= 2) {
        // First part is date, rest is time
        const datePart = parts[0];
        const timePart = parts.slice(1).join(' ');
        return { date: datePart, time: timePart };
      } else if (parts.length === 1) {
        // Only date provided
        return { date: parts[0], time: '' };
      }

      return { date: '', time: '' };
    };

    // Helper function to create AddressDTO for the new format
    const createPickUpAddressDTO = (): AddressDTO => ({
      addressId: 0,
      street1: get('pu_street', 'Origin Street'),
      street2: '',
      city: get('pu_city', 'Origin City'),
      state: get('pu_state', 'Origin State'),
      county: get('pu_county', ''),
      zipcode: get('pu_zipcode', 'Origin ZIP'),
      latitude: parseFloat(get('lat_pu', 'Pick-Up Lat') || '0'),
      longitude: parseFloat(get('lon_pu', 'Pick-up Long') || '0'),
      commonName: get('pu_location_name', ''),
      phoneNumber: '',
      phoneExtension: '',
      addressType: ''
    });

    const createDropOffAddressDTO = (): AddressDTO => ({
      addressId: 0,
      street1: get('DO_STREET', 'Final Stop Street'),
      street2: '',
      city: get('do_city', 'Final Stop City'),
      state: get('do_state', 'Final Stop State'),
      county: get('do_county', ''),
      zipcode: get('do_zipcode', 'Final Stop ZIP'),
      latitude: parseFloat(get('lat_do', 'Drop-Off Lat') || '0'),
      longitude: parseFloat(get('lon_do', 'Drop-Off Long') || '0'),
      commonName: get('do_location_name', ''),
      phoneNumber: '',
      phoneExtension: '',
      addressType: ''
    });



    // Format is determined by fileFormat parameter, no need to check field presence

    // Compute timeWindowAfter from Start stop estimated times when available
    const parseToMoment = (input: any): moment.Moment | null => {
      if (input === undefined || input === null) return null;
      const s = String(input).trim();
      if (!s) return null;
      // Try common time/datetime formats first (strict), then fallback to moment's parser
      const formats: any[] = ['HH:mm', 'H:mm', 'h:mm A', 'h:mma', 'YYYY-MM-DDTHH:mm', 'YYYY-MM-DD HH:mm', 'MM/DD/YYYY HH:mm', moment.ISO_8601];
      let m = moment(s, formats, true);
      if (m.isValid()) return m;
      m = moment(s);
      return m.isValid() ? m : null;
    };

    let computedTimeWindowAfter = parseInt(get('', 'Pick-Up Window') ?? '0', 10); // CSV doesn't have pickup window
    try {
      // CSV doesn't have separate arrival/departure times like Excel
      // For CSV, REQ_PU and REQ_DO are used for pickup/dropoff requirements
      if (fileFormat === FileFormat.EXCEL) {
        const dep = parseToMoment(get('', 'Trip Time'));
        const arr = parseToMoment(get('', 'Trip Time'));
        if (dep && arr) {
          let diff = arr.diff(dep, 'minutes');
          if (diff < 0) diff = 0; // clamp negative diffs to 0
          computedTimeWindowAfter = diff;
        }
      }
    } catch (_e) {
      // keep existing/default value on parse error
    }

    // Parse "Date & Time Of Trip" for Excel and split into date + time
    const dateTimeOfTrip = get('', 'Date & Time Of Trip');
    const { date: parsedDate, time: parsedTime } = parseDateTimeString(dateTimeOfTrip);
    const tripType = get('', 'Pick-Up/Drop-Off Time');


    // Note: parsedTime not currently used as we use Scheduled Start/Ends for timing

    // For Excel: use "Scheduled Start" and "Scheduled Ends" for pickup/dropoff times
    // Note: These are currently not used but available if needed in future
    // const scheduledStart = get('', 'Scheduled Start');
    // const scheduledEnds = get('', 'Scheduled Ends');


    console.log(' For trip date using ' + get('appt_date', ''));

    const rawAppt = get('appt_date', '');
    const normalizedTripDate = this.normalizeTripDate(fileFormat === FileFormat.EXCEL ? parsedDate : rawAppt);
    console.debug('[Upload] fileFormat=', fileFormat, 'raw appt_date=', rawAppt, 'parsedDate(excel)=', parsedDate, 'normalized=', normalizedTripDate);

    return {
      appointmentTime: get('appointmentTime') || null,
      timeWindowAfter: computedTimeWindowAfter,
      // Address objects - use appropriate format based on file type
      customerAddress: undefined, // Not used in either CSV or Excel templates
      pickUpAddress: createPickUpAddressDTO(),
      dropOffAddress: createDropOffAddressDTO(),

      // Required fields - handle name mapping
      // NEW: Case Number -> originTripId
      originTripId: get('Trip_id', 'Trip Name') || '0',
      originProviderId: parseInt(get('originProviderId') ?? loggedProviderId ?? '0', 10),

      // NEW: Scheduled Ends -> requestedDropOffTime for Excel
      requestedDropOffTime: parseTime(
        fileFormat === FileFormat.EXCEL ? (tripType === 'pick-up' ? '': parsedTime ) : get('REQ_DO', '')
      ) || '',

      // Customer name - NEW Excel headers: "First Name*" and "Last Name*"
      customerFirstName: get('CLIENT_F_NAME', 'First Name') || '',
      customerLastName: get('CLIENT_L_NAME', 'Last Name') || '',

      // Customer middle name - NEW Excel: "Middle Name"
      customerMiddleName: get('customerMiddleName', 'Middle Name'),

      // Customer nickname - NEW Excel: "Nickname"
      customerNickName: get('customerNickName', 'Nickname'),

      // Customer demographics
      customerGender: get('Gender', 'Gender'),
      customerRace: get('', 'Race'),
      customerEthnicity: get('Ethnicity', 'Ethnicity'),
      customerDOB: get('customer_dob', 'Date Of Birth'),
      customerEmail: get('Email address', 'Email'),

      // Customer phone numbers - NEW Excel: "Home Phone" and "Cell Phone*"
      customerHomePhone: get('customer_phone', 'Home Phone'),
      customerMobilePhone: get('', 'Cell Phone'),

      // Customer emergency contact info - NEW Excel headers
      customerEmergencyContactName: get('customerEmergencyContactName', 'Primary Emergency Contact Name'),
      customerEmergencyContactRelationship: get('customerEmergencyContactRelationship', 'Primary Emergency Contact Relationship'),
      customerEmergencyPhone: get('customerEmergencyPhone', 'Primary Emergency Contact Phone Number'),

      // Customer caregiver info - NEW Excel: "Caregiver Details"
      customerCaregiverName: get('customerCaregiverName', 'Caregiver Details'),
      customerCaregiverContactInfo: get('customerCaregiverContactInfo', 'Caregiver Details'),

      // Customer addresses and billing - NEW Excel: "Home Address Line 1", "Billing Address"
      customerMailingBillingAddress: get('customerMailingBillingAddress', 'Billing Address'),

      // Customer disability and assistance info
      // NEW: "Disability Type" and "Mobility Device" -> customerMobilityFactors
      customerDisability: get('', 'Disability Type'),
      customerMobilityFactors: get('DEVICE', 'Mobility Device') || get('', 'Level of Service'),
      customerCareInfo: get('', ''),
      customerPrimaryLanguage: get('Language', 'Primary Language'),

      // Customer economic/veteran status
      customerVeteran: toBoolean(get('customerVeteran', 'Veteran Status')),
      customerPovertyLevel: get('customerPovertyLevel', 'Poverty Level'),

      // Customer service level - NEW: "Vehicle Type" -> customerServiceLevel
      customerServiceLevel: get('VEHICLE_REQ', 'Vehicle Type') || '',
      customerSeatsRequired: parseInt(get('', '') ?? '1', 10),

      // Customer identifiers
      originCustomerId: get('CLIENT_ID', '') ?? '0',

      // Trip purpose - NEW: "Reason(s) for Trip" -> tripPurpose
      tripPurpose: get('Purpose', 'Reason(s) for Trip') || '',

      // Vehicle and driver information
      vehicleId: get('Vehicle ID', ''),
      driverFirstName: get('Driver ID', ''),

      // Provider information - NEW: "Actual Provider"
      providerName: get('Provider Name', 'Actual Provider'),

      // Trip date - normalize to MM/DD/YY without timezone shifts
      // - For CSV, source is 'appt_date' which may be 'YYYY-MM-DD' or 'MM/DD/YY'
      // - For Excel, parsedDate may vary; normalize both paths consistently
      tripDate: normalizedTripDate,

      // Trip timing - NEW: "Scheduled Start" -> requestedPickupTime for Excel
      requestedPickupTime: parseTime(
        fileFormat === FileFormat.EXCEL ? (tripType === 'pick-up' ? parsedTime: '' ) : get('REQ_PU', '')
      ) || '',

      timeWindowBefore: parseInt(get('timeWindowBefore') ?? '0', 10),

      // Trip status and reasons
      status: get('Status', '') || 'PENDING',
      noShowReason: get('No-Show Reason', ''),

      // Funding and billing - NEW: "Funding Source", "Cost of Trip"
      customerFundingBillingInformation: get('FUNDING_SOURCE', 'Funding Source'),
      fundingType: get('', ''),

      // Guest counts
      numGuests: parseInt(get('num_attendant', '') ?? '0', 10),

      // Notes
      customerNotes: get('PU_NOTE', '') || get('', ''),

      // Trip details
      estimatedDistance: parseFloat(get('estimatedDistance', 'Estimated Miles') ?? '0'),
      estimatedTripTravelTime: parseInt(get('estimatedTripTravelTime') ?? '0', 10),
      numAttendants: parseInt(get('numAttendants') ?? '0', 10),

      // Boolean flags
      outsideCoreHours: toBoolean(get('outsideCoreHours')),
      customerInformationWithheld: toBoolean(get('customerInformationWithheld')),
      tripIsolation: toBoolean(get('tripIsolation')),
      tripServiceAnimal: toBoolean(get('tripServiceAnimal')),

      // Additional trip information
      schedulingPriority: get('schedulingPriority'),
      tripFunders: get('tripFunders'),
      tripNotes: get('tripNotes') || get('customerNotes'),

      // ADDITIONAL EXCEL FIELDS
      tripDirection: get('', 'Pick-Up/Drop-Off Time'),
      requestedPickupDirection: get('REQ_PU', ''),
      requestedDropoffDirection: get('REQ_DO', ''),

      fareType: get('Fare_type', ''),
      fareCollected: parseFloat(get('Fare Collected', 'Cost of Trip') || '0'),

      actualPickupArriveTime: get('Actual pick-up arrive time', ''),
      actualPickupDepartTime: get('Actual pick-up depart time', 'Actual pick-up depart time'),
      actualDropOffArriveTime: get('Actual drop-off arrive time', 'Actual drop-off arrive time'),
      actualDropOffDepartTime: get('Actual drop-off depart time', 'Actual drop-off depart time'),

      // Both CSV and Excel: "Driver ID" (separate from driver name)
      driverId: get('Driver ID', 'Driver ID'),

      // Excel: "Other (free field)" - catch-all field (CSV doesn't have this)
      otherInformation: get('', 'Other (free field)')

      // TODO UNMAPPED EXCEL FIELDS:
      // The following Excel fields may need future DTO extensions or special handling:
      // - "Customer home address" (free text vs structured AddressDTO)
      // - Need to implement customerLowIncome boolean conversion from "Poverty Level" text
    };
  }

  /**
   * Validates a collection of trip tickets
   */
  private validateTicketCollection(records: TripTicketUploadRecord[]): UploadValidationResult {
    const validationResult: UploadValidationResult = {
      isValid: true,
      errors: []
    };

    records.forEach((record, index) => {
      const errors = this.validateTripTicketRecord(record);
      if (errors.length > 0) {
        validationResult.isValid = false;
        validationResult.errors.push({ row: index + 1, errors });
      }
    });

    return validationResult;
  }

  /**
   * Validate a single trip ticket record
   */
  private validateTripTicketRecord(record: TripTicketUploadRecord): string[] {
    const errors: string[] = [];

    console.log('Validating record:', record);
    // Required fields validation
    const requiredFields = [
      { value: record.customerFirstName, label: 'Customer First Name' },
      { value: record.customerLastName, label: 'Customer Last Name' },
      { value: record.originTripId, label: 'Origin Trip ID' }
    ];

    requiredFields.forEach(({ value, label }) => {
      if (value === undefined || value === null || value === '') {
        errors.push(`${label} is required`);
      }
    });

    // Require at least one of requestedPickupTime OR requestedDropOffTime
    const hasPickupTime = record.requestedPickupTime !== undefined && record.requestedPickupTime !== null && record.requestedPickupTime !== '';
    const hasDropoffTime = record.requestedDropOffTime !== undefined && record.requestedDropOffTime !== null && record.requestedDropOffTime !== '';
    if (!hasPickupTime && !hasDropoffTime) {
      errors.push('Either Requested Pickup Time or Requested Drop-off Time must be provided');
    }

    // Address validation
    const addressFields: { field: keyof TripTicketRequestDTO; label: string; required: boolean }[] = [
      { field: 'customerAddress', label: 'customerAddress', required: false },
      { field: 'pickUpAddress', label: 'pickUpAddress', required: true },
      { field: 'dropOffAddress', label: 'dropOffAddress', required: true }
    ];

    addressFields.forEach(({ field, label, required }) => {
      const address = record[field] as AddressDTO | null | undefined;

      if (!address) {
        if (required) {
          errors.push(`${label} is required`);
        }
        return;
      }

      const shouldValidate = required || this.addressHasStructuredData(address);
      if (!shouldValidate) {
        return;
      }

      if (!address.street1 || address.street1.length < 1 || address.street1.length > 1000) {
        errors.push(`${label}.street1 must be between 1 and 1000 characters`);
      }

      if (address.street2 && address.street2.length > 250) {
        errors.push(`${label}.street2 must not exceed 250 characters`);
      }

      if (!address.city || address.city.length < 1 || address.city.length > 100) {
        errors.push(`${label}.city must be between 1 and 100 characters`);
      }

      if (
        address.latitude !== undefined &&
        address.latitude !== null &&
        (isNaN(address.latitude) || address.latitude < -90 || address.latitude > 90)
      ) {
        errors.push(`${label}.latitude must be a valid number between -90 and 90`);
      }

      if (
        address.longitude !== undefined &&
        address.longitude !== null &&
        (isNaN(address.longitude) || address.longitude < -180 || address.longitude > 180)
      ) {
        errors.push(`${label}.longitude must be a valid number between -180 and 180`);
      }
    });

  // Time format validation - accept HH:mm or HH:mm:ss (24-hour)
  const timeRegex = /^([01]?\d|2[0-3]):([0-5]\d)(?::([0-5]\d))?$/;

    // Normalize known time fields in-place if necessary
    const normalizedPickup = this.normalizeTimeString(record.requestedPickupTime);
    if (normalizedPickup) {
      record.requestedPickupTime = normalizedPickup;
    }

    const normalizedDropoff = this.normalizeTimeString(record.requestedDropOffTime);
    if (normalizedDropoff) {
      record.requestedDropOffTime = normalizedDropoff;
    }

    const normalizedAppointment = this.normalizeTimeString(record.appointmentTime ?? undefined);
    if (normalizedAppointment) {
      record.appointmentTime = normalizedAppointment;
    }

    const timeFields: Array<{ value: string | undefined | null; label: string }> = [
      { value: record.requestedPickupTime, label: 'requestedPickupTime' },
      { value: record.requestedDropOffTime, label: 'requestedDropOffTime' },
      { value: record.appointmentTime ?? undefined, label: 'appointmentTime' }
    ];

    timeFields.forEach(({ value, label }) => {
      if (value && !timeRegex.test(value)) {
        errors.push(`${label} must be in HH:mm or HH:mm:ss format`);
      }
    });

    // Numeric field validation
    const numericFields = [
      { value: record.customerSeatsRequired ? Number(record.customerSeatsRequired) : 1, min: 0, label: 'Customer Seats Required' },
      { value: record.estimatedDistance ? Number(record.estimatedDistance) : 0, min: 0, label: 'Estimated Distance' },
      { value: record.estimatedTripTravelTime ? Number(record.estimatedTripTravelTime) : 0, min: 0, label: 'Estimated Trip Travel Time' },
      { value: record.numAttendants ? Number(record.numAttendants) : 0, min: 0, label: 'Number of Attendants' },
      { value: record.numGuests ? Number(record.numGuests) : 0, min: 0, label: 'Number of Guests' },
      { value: record.timeWindowBefore ? Number(record.timeWindowBefore) : 0, min: 0, label: 'Time Window Before' },
      { value: record.timeWindowAfter ? Number(record.timeWindowAfter) : 0, min: 0, label: 'Time Window After' }
    ];

    numericFields.forEach(({ value, min, label }) => {
      if (value !== undefined && value !== null && String(value).trim() !== '') {
        const numericValue = Number(value);
        if (isNaN(numericValue) || numericValue < min) {
          errors.push(`${label} must be a number ${min > 0 ? `greater than or equal to ${min}` : 'greater than or equal to 0'}`);
        }
      }
    });

    return errors;
  }

  /**
   * Check if address has structured data
   */
  private addressHasStructuredData(address: Partial<AddressDTO>): boolean {
    const keysToCheck: (keyof AddressDTO)[] = ['street1', 'street2', 'city', 'state', 'zipcode', 'county'];
    return keysToCheck.some(key => {
      const value = address[key];
      if (typeof value === 'number') {
        return !isNaN(value);
      }
      return value !== undefined && value !== null && value.toString().trim() !== '';
    });
  }

  /**
   * Normalize a time string to 24-hour HH:mm format
   */
  private normalizeTimeString(time: string | undefined | null): string | null {
    if (time === undefined || time === null) {
      return null;
    }

    const raw = String(time).trim();
    if (!raw) {
      return null;
    }

    // If already in 24-hour HH:mm or HH:mm:ss format, normalize to HH:mm (strip seconds)
    const hh24withOptionalSec = /^([01]?\d|2[0-3]):([0-5]\d)(?::([0-5]\d))?$/;
    const match24 = raw.match(hh24withOptionalSec);
    if (match24) {
      const hour = match24[1].length === 1 ? '0' + match24[1] : match24[1];
      const minute = match24[2];
      return `${hour}:${minute}`;
    }

    // Match 12-hour formats like '1:23 pm', '01:23PM', with optional seconds: '1:23:45 pm'
    const hh12 = /^(\d{1,2}):(\d{2})(?::(\d{2}))?\s*(am|pm)$/i;
    const m = raw.match(hh12);
    if (m) {
      let hour = parseInt(m[1], 10);
      const minute = m[2];
      const meridiem = m[4].toLowerCase();

      if (hour < 1 || hour > 12) {
        return null;
      }

      if (meridiem === 'am') {
        if (hour === 12) hour = 0;
      } else {
        if (hour !== 12) hour = hour + 12;
      }

      const hh = hour < 10 ? '0' + hour : String(hour);
      return `${hh}:${minute}`;
    }

    return null;
  }

  /**
   * Normalize a date-like value into MM/DD/YY without timezone shifts.
   * Accepts common inputs: 'YYYY-MM-DD', 'MM/DD/YY', 'MM/DD/YYYY', 'M/D/YY', 'YYYY/MM/DD'.
   * Falls back to returning the original trimmed string when parsing fails.
   */
  private normalizeTripDate(value: any): string {
    if (value === undefined || value === null) return '';
    const s = String(value).trim();
    if (!s) return '';

    // 1) Handle ISO-like strings that may include a time or timezone
    //    e.g., '2025-11-25', '2025-11-25T00:00:00Z', '2025-11-25 00:00:00-07:00'
    const isoLike = /^(\d{4})-(\d{2})-(\d{2})(?:[T\s].*)?$/.exec(s);
    if (isoLike) {
      const y = Number(isoLike[1]);
      const mo = Number(isoLike[2]) - 1;
      const dnum = Number(isoLike[3]);
      const d = new Date(y, mo, dnum); // local date, no TZ shift
      return new Intl.DateTimeFormat('en-US', { year: '2-digit', month: '2-digit', day: '2-digit' }).format(d);
    }

    // 2) Handle Excel serial dates (if they leak through from XLSX parsing)
    //    Excel epoch is 1899-12-30 for Windows (accounts for the 1900 leap year bug)
    if (/^\d+(?:\.\d+)?$/.test(s)) {
      const n = Number(s);
      // Accept a reasonable range for modern dates
      if (n > 25568 && n < 60000) { // ~1970-01-01 to ~2064-11-07
        // Compute using UTC to preserve the calendar day regardless of local TZ
        const excelEpochUTC = Date.UTC(1899, 11, 30);
        const days = Math.floor(n);
        const ms = Math.round((n - days) * 86400000);
        const utcTime = excelEpochUTC + days * 86400000 + ms;
        const d = new Date(utcTime);
        const pad = (x: number) => (x < 10 ? '0' + x : String(x));
        return `${pad(d.getUTCMonth() + 1)}/${pad(d.getUTCDate())}/${String(d.getUTCFullYear()).slice(-2)}`;
      }
    }

    // Try strict parsing first to avoid unintended coercions
    const formats = ['YYYY-MM-DD', 'MM/DD/YYYY', 'M/D/YYYY', 'MM/DD/YY', 'M/D/YY', 'YYYY/MM/DD'];
    const m = moment(s, formats, true);
    if (m.isValid()) {
      return m.format('MM/DD/YY');
    }

    // Last resort: return original string
    return s;
  }

  /**
   * Build template error message
   */
  private buildTemplateErrorMessage(validation: TripTicketHeaderValidationResult): string {
    const details: string[] = ['The uploaded file does not match the expected template.'];

    if (validation.missingHeaders.length) {
      details.push(`Missing columns: ${validation.missingHeaders.join(', ')}`);
    }

    if (validation.unexpectedHeaders.length) {
      details.push(`Unexpected columns: ${validation.unexpectedHeaders.join(', ')}`);
    }

    details.push('Download the sample template for the required structure.');
    return details.join('\n');
  }

  /**
   * Emit state changes
   */
  private emitState(): void {
    this.stateSubject.next({ ...this.uploadState });
  }
}
