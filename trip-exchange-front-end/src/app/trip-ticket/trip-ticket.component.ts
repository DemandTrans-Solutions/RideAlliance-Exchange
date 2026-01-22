// Complete Angular 18 migrated version of trip-ticket component
import { Component, OnInit, OnDestroy, Input, ViewChild, ElementRef, HostListener } from '@angular/core';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { Router, ActivatedRoute } from '@angular/router';
import { Location } from '@angular/common';
import { TripTicketService } from './trip-ticket.service';
import { ConfirmationService } from 'primeng/api';
import { TokenService } from '../shared/service/token.service';
import { ListService } from '../shared/service/list.service';
import { NotificationEmitterService } from '../shared/service/notification-emitter.service';
import { TicketFilter, AdvanceFilter, TripTime, Range, SummaryReport, FilterParameter } from './ticket-filter';
import { LocalStorageService } from '../shared/service/local-storage.service';
import { HeaderEmitterService } from '../shared/service/header-emmiter.service';
import { FundSourceService } from '../admin/fund-source/fund.service';
import { ProviderPartnersService } from '../admin/provider-partners/provider-partners.service';
import { Subject, timer } from 'rxjs';
import { takeUntil, finalize, throttleTime } from 'rxjs/operators';
import { Table } from 'primeng/table';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import moment from 'moment-timezone';
// Declare Leaflet Maps API globally
import * as L from 'leaflet';
import { TripTicketExportData } from '../models/trip-export-data.dto';
import { UberRequest, UberRideOption, UberRideRequest, UberGuest, CoordinatesWithPlace, Scheduling, TripSummary } from './uber-request';
import { TripResultRequestDTO } from './trip-ticket.service';
import { TripTicketExportService } from './trip-ticket-export.service';
import { TripTicketUploadService, TripTicketUploadRecord } from './trip-ticket-upload.service';
import { TripTicketWithUIProperties } from '../models/detailed-trip-ticket.dto';
import { tick } from '@angular/core/testing';




@Component({
  selector: 'app-trip-ticket',
  templateUrl: './trip-ticket.component.html',
  styleUrls: ['./trip-ticket.component.scss'],
  host: {
    '(document:click)': 'onClick($event)',
  },
})
export class TripTicketComponent implements OnInit, OnDestroy {
  private currentTimeInterval: any = null;
  // Track the current active filter for pagination
  private activeFilterState: {
    ticketFilter: TicketFilter,
    advancedFilter: AdvanceFilter,
    tripTime: TripTime
  } | null = null;
  @ViewChild('tripTicketTable') tripTicketTable?: Table;

  // Inputs and models
  @Input() ticketFilter: TicketFilter = new TicketFilter();
  @Input() range: Range = { fromDate: '', toDate: '', providerId: 0, reportTicketFilterStatus: [] };
  @Input() summaryReport: SummaryReport = new SummaryReport();
  advancedFilter: AdvanceFilter = new AdvanceFilter();
  tripTime: TripTime = new TripTime();

  ticketId: any = null;

  // Properties needed by the HTML template
  providers: any[] = [];
  originatingProvider: any[] = [];
  providerListForComments: any[] = [];
  providerPartnerList: any[] = [];

  // Private destroy subject for subscription management
  private destroy$ = new Subject<void>();

  // Data properties
  TicketsList: TripTicketWithUIProperties[] = [];
  filterData: any = {};
  showGrid: boolean = true;
  show: boolean = true;
  dropdown: boolean = false;
  savefilters: boolean = false;
  Filters: boolean = false;
  saveFilterDropdown: boolean = false;
  UpdateFilters: boolean = false;
  UpdateFilterBlock: boolean = false;
  fromDateEntered: boolean = false;
  search: boolean = false;
  clear: boolean = false;
  isProviderMatch: boolean = false;

  requestedPickupDate: any = null;

  // Missing properties for HTML template
  showTicket: boolean = false;
  rowsPerPage: number = 10;

  // Dynamic pagination properties
  private readonly MIN_ROWS_PER_PAGE = 5;
  private readonly MAX_ROWS_PER_PAGE = 100;
  private readonly ROW_HEIGHT_ESTIMATE = 42; // Precise height per table row in pixels (p-datatable-sm)
  private readonly TABLE_HEADER_HEIGHT = 40; // Height of the table header row
  private readonly VIEWPORT_BUFFER = 270; // Buffer matching CSS calc((100vh - 270px) * 0.95)
  rowsPerPageOptions: number[] = [5, 10, 25, 50, 100];
  private resizeHandler: () => void;
  private resizeTimeout: any;
  private readonly RESIZE_DEBOUNCE_TIME = 250; // Debounce resize events
  isDynamicPaginationEnabled: boolean = true; // Always enabled for smart pagination
  private filterRequestInFlight: boolean = false;
  private pendingLazyEvent: any = null;
  private pendingLazyEventSignature: string | null = null;
  private lastLazyEventSignature: string | null = null;
  private lastCalculatedOptimalRows: number = 10; // Cache last calculated value

  claimantNameToApprove: string = '';
  claimantNameToDecline: string = '';
  claimantList: any[] = [];
  claimantName: any;
  claimantNameForComment: any;
  claimantNameForTicket: any;
  claimForProvider: boolean = false;
  claimFromGrid: any;
  claimFromGridAsAdmin: boolean = false;

  // Activity related properties
  activity: boolean = false;
  // Local UI state for overflow menu in the action column
  overflowMenuOpen: boolean = false;
  update: boolean = false;
  createClaims: boolean = false;
  updateClaim: boolean = false;
  claimAction: string = '';
  fromDateTimeSpan: boolean = false;
  toDateTimeSpan: boolean = false;
  displayDialog: boolean = false;
  viewActionModal: boolean = false;
  isActive: boolean = false;
  userName: boolean = false;
  onceClaim: boolean = false;
  Ticket: TripTicketWithUIProperties;
  providerId: number = 0;
  mask = ['(', /[1-9]/, /\d/, /\d/, ')', ' ', /\d/, /\d/, /\d/, '-', /\d/, /\d/, /\d/, /\d/];
  pageName: string = "";
  viewTicketIndex: number = 0;
  editProviderObj: any;
  tripTicketProvisionalTime_hours: any;
  tripTicketProvisionalTime_minutes: any;
  filterName: any;
  TicketStatus: any;
  ticketFilterstatus: any;
  FundingSource: any;
  ClaimingProvider: any;
  OriginatingProvider: any;
  schedulingPriority = "Scheduling_Priority :";
  filteredCustomersAddress: any;
  filteredPickupAddresses: any[] = [];
  filteredDropoffAddresses: any[] = [];
  selectedCustomerAddress: any;
  selectedPickupAddress: any;
  selectedDropoffAddress: any;
  filterList: any;
  Rescinded_Trip_Tickets: any;
  changeCustomerAdd: boolean = false;
  changeFromDate: boolean = false;
  changeToDate: boolean = false;
  changeCustomerName: boolean = false;
  changePickupAdd: boolean = false;
  changeDropOffAdd: boolean = false;
  changeCustomerIdentifier: boolean = false;
  changeSeatesReqMin: boolean = false;
  changeSeatesReqMax: boolean = false;
  changeToDecline: boolean = false;
  changeToApproved: boolean = false;
  filterId: any = null;
  selectedFilterName: string = '';
  comment: any;
  ticketActivity: any[] = []; // Explicitly typed as array
  commentAdded: any[] = []; // Explicitly typed as array
  tripTicketCreate: any[] = []; // Explicitly typed as array
  claims: any[] = []; // Explicitly typed as array
  ticketStatus: any[] = []; // Explicitly typed as array
  ticketUpdate: any[] = []; // Explicitly typed as array
  myarray: any;
  pendingCount: number = 0;
  status: any;
  claim: any;
  claimsArray: any;
  temArray: any;
  ticketClaimsArray: any;
  test: boolean = false;
  pickupDate: any;
  pickupTime: any = { hours: 0, minutes: 0 };
  dropOffDate: any;
  dropoffTime: any = { hours: 0, minutes: 0 };
  screen: any;
  dateValue: boolean = true;
  dateValue1: Date = new Date();
  dateValue2: Date = new Date();
  dates: any = {};
  hospitalservicevalue: boolean = false;
  isTripTicketCancel: boolean = false;
  isRqstTime: boolean = false;
  fareValue: any;
  notes: any;
  showAddComment: boolean = false;
  comments: any;
  ticketupdateFlag: boolean = false;
  isRow: any;
  ticketTempActivity: any;
  ticketTemp: any;
  ticketTempStatus: any;
  ticketTempUpdate: any;
  ticketCommentTemp: any;
  ticketClaimsTemp: any;
  ticketTempProvid: any;
  ticketTempTimeStamp: any;
  ticketTempId: any;
  ticketClaimTempId: any;
  ticketUpdateDate: any;
  isComment: boolean = false;
  isTempShow: boolean = true;
  loggedRole: any;
  loggedProviderId: any;
  showClaimInput: boolean = false;
  showEditInput: boolean = false;
  geocoder: any;
  directionsService: any;
  directionsRenderer: any;
  claimantProviderList: any;
  isShowMap: boolean = false;
  updateClaimIndex: any;
  approveClaimIndex: any;
  declineClaimIndex: any;
  rescindClaimIndex: any;
  fare: any;
  requestedFare: any;
  ackStatus: any = false;
  originatorProviderId: any = null;
  provisionalPickupTime: any;
  isFliter: any;
  filterids: any;
  id: any;
  filterIdName: any;
  filterIDs: any;
  filterClaimsNotCreated: any;
  fetched_size: any = 0;
  statusesArray: any = [];
  fundResource: any = [];
  saveFilterId: any;
  activeid: any;
  otherFundName: any;
  fundId: any;
  fundNameTemp: any;
  isFundCheck: boolean = false;
  isEnable: boolean = false;
  tempName: any;
  valuename: any;
  index: any;
  providerNameForCreateClaimsForAdmin: any;
  sizes: any = ['0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '10', '11', '12+'];
  fundFilter: any;
  fundingSourceFilter: any;
  hospitalServiceFilter: any;
  hospitalArea: any;
  hospitalService: any;
  eligibility: any;
  eligibilityFilter: any;
  geographic: any;
  geographicFilter: any;
  partnersName: any;
  isServiceFilter: boolean = false;
  reqPickUpStartTime: any;
  reqPickUpEndTime: any;
  sortField: string = 'status';
  sortOrder: number = -1;
  rows: number = 10;
  staticRows: number = 10;
  first: number = 0;
  exportColumns: any[] = [];
  cols: any[] = [];
  loading: boolean = false;
  totalRecords: number = 0;

  customerEligibilityList: any[] = [];
  companySuggestions: any[] = [];
  addressList: any[] = [];


  commentForm!: FormGroup;
  claimForm!: FormGroup;

  // Claim related properties
  claimId: string = '';
  eligibleForCreateClaim: number = 0;
  isTrustedPartner: number = 0;
  isOriginator: boolean = false;
  workingHourCheck: any;
  isEligibleForCreateClaim: boolean = false;
  calculatedProposedFare: number = 0;




  // Missing properties needed for showModalOfCreateClaimsFromGrid
  createClaimPickupDate: boolean = false;
  proposedPickupTime: string | null = '';
  validPickupTime: boolean = false;
  _pickupDate: any = null;
  dropoffDate: string = '';
  rowId: any = null;
  fromGrid: boolean = false;
  dateForCheck: Date | null = null;
  updateClaimInfo: any = null;
  ticketID: any = null;
  isClaiment: boolean = false;
  isInServiceArea: number = 0;
  isCostChecked: boolean = true;
  isClaim: boolean = false;
  isRequestedPriceHigh: boolean = false;
  isSubmittedPriceHigh: boolean = false;
  claimStatus: string = '';
  isPickupDateNull: boolean = false;
  claimentCost: number = 0;
  isNoChange: boolean = true;

  // Fund-related properties
  arrayOtherFund: string[] = [];
  arrayFund: string[] = [];
  fundSourceName: string = '';
  iserror: string | null = null;
  isUpdateFund: boolean = false;
  isFunds: boolean = false;
  indexFund: number = 0;
  isNote: boolean = false;
  // Dropdown state for summary report
  showSummaryDropdown: boolean = false;

  // ViewChild for the trip note input field
  @ViewChild('tripNoteInput') tripNoteInput!: ElementRef;

  // Map related properties
  commentForProvider: boolean = false;
  savedFilterName: string = '';
  nameSave: boolean = false;
  quickSummary: boolean = false;

  // Leaflet map properties
  leafletMap: L.Map | null = null;
  leafletMarkers: L.Marker[] = [];
  leafletRoute: L.Polyline | null = null;
  leafletOptions = {
    layers: [
      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
        maxZoom: 18
      })
    ],
    zoom: 10,
    center: L.latLng(39.0, -77.0)
  };

  // Default Leaflet marker icons configuration
  private defaultIcon = L.icon({
    iconUrl: 'assets/images/leaflet/marker-icon.png',
    iconRetinaUrl: 'assets/images/leaflet/marker-icon-2x.png',
    shadowUrl: 'assets/images/leaflet/marker-shadow.png',
    iconSize: [25, 41],
    iconAnchor: [12, 41],
    popupAnchor: [1, -34],
    shadowSize: [41, 41]
  });

  // Uber-compliant pickup marker: gray circle with black dot
  private pickupIcon = L.divIcon({
    className: 'uber-pickup-marker',
    iconSize: [32, 32],
    iconAnchor: [16, 32],
    html: `
      <svg width="32" height="32" viewBox="0 0 32 32" xmlns="http://www.w3.org/2000/svg">
        <!-- Outer circle: white background with subtle dark stroke for contrast -->
        <circle cx="16" cy="16" r="14" fill="#ffffff" stroke="#303030" stroke-width="2"/>
        <!-- Inner dot: green accent for pickup (matches description icon) -->
        <circle cx="16" cy="16" r="5" fill="#4CAF50"/>
      </svg>
    `
  });

  // Uber-compliant drop-off marker: gray square with black dot
  private dropoffIcon = L.divIcon({
    className: 'uber-dropoff-marker',
    iconSize: [32, 32],
    iconAnchor: [16, 32],
    html: `
      <svg width="32" height="32" viewBox="0 0 32 32" xmlns="http://www.w3.org/2000/svg">
        <!-- Outer rounded square: white with dark stroke -->
        <rect x="4" y="4" width="24" height="24" rx="4" fill="#ffffff" stroke="#303030" stroke-width="2"/>
        <!-- Inner dot: orange accent for dropoff -->
        <circle cx="16" cy="16" r="5" fill="#FF7043"/>
      </svg>
    `
  });

  // Route loading spinner flag
  isRouteLoading: boolean = false;

  // Timezone support properties
  public currentTime: Date = new Date();
  public selectedTimezone: string = Intl.DateTimeFormat().resolvedOptions().timeZone; // Default to browser timezone
  public availableTimezones = [
    { label: 'Eastern Time', value: 'America/New_York' },
    { label: 'Central Time', value: 'America/Chicago' },
    { label: 'Mountain Time', value: 'America/Denver' },
    { label: 'Pacific Time', value: 'America/Los_Angeles' },
    { label: 'UTC', value: 'UTC' },
  ];

  // File upload properties
  public selectedFile: File | null = null;
  public uploadProgress: number = 0;
  public uploadError: string | null = null;
  public isUploading: boolean = false;
  public uploadSuccess: boolean = false;
  public showUploadDialog: boolean = false;
  public uploadResult: any = null;
  public allowedFileTypes = '.csv,.xlsx,.xls';
  public maxFileSize = 10 * 1024 * 1024; // 10MB
  // Path to the actual Excel template used for trip exchange import/export
  // Previously pointed to a CSV which caused an .xlsx download with CSV content (Excel error)
  private readonly uploadTemplatePath = 'assets/templates/trip-exchange-import-export-template.xlsx';

  // Edit dialog properties
  showEditDialog: boolean = false;
  isSaving: boolean = false;
  editTicketForm!: FormGroup;
  currentEditingTicket: any;

  // Trip result completion properties
  showTripResultStep: boolean = false;
  tripResultForm!: FormGroup;
  completedStatusId: string | null = null;

  ticketCollection: TripTicketUploadRecord[] = [];

  // Add missing properties
  public errorMessage: string = '';
  public isDragging: boolean = false;
  // Upload dialog originating provider selection
  public selectedOriginatingProvider: string | null = null;
  public uploadOriginatingProviderOptions: any[] = [];

  // Cancellation reason dialog properties
  public showCancellationReasonDialog: boolean = false;
  public cancellationReason: string = '';
  private ticketToCancel: any = null;

  // Uber integration state
  showUberDialog: boolean = false;
  uberLoading: boolean = false;
  uberBookingInProgress: boolean = false;
  uberBookingConfirmed: boolean = false;
  uberBookingReference: string = '';
  uberError: string | null = null;
  pickupLatLng: { lat: number; lng: number } | null = null;
  dropoffLatLng: { lat: number; lng: number } | null = null;
  uberPickupDateTimeISO: string = '';
  uberPromisedDropOff: boolean = false;
  uberMinDateTime: string = '';
  showUberOptions: boolean = false;
  uberRideOptions: UberRideOption[] = [];
  selectedUberOption: UberRideOption | null = null;
  uberMapLayer: 'standard' | 'satellite' = 'standard';
  uberLeafletMap: L.Map | null = null;
  uberPickupMarker: L.Marker | null = null;
  uberDropoffMarker: L.Marker | null = null;
  uberRouteLine: L.Polyline | null = null;
  uberLeafletOptions: any = {
    layers: [
      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', { maxZoom: 19, attribution: '© OpenStreetMap' })
    ],
    zoom: 14,
    center: L.latLng(39.0, -77.0)
  };

  // Selection properties for export functionality
  selectedTickets: any[] = [];
  selectAll: boolean = false;
  isExporting: boolean = false;
  private isInitialLoad: boolean = true;

  // Navigation state management
  private pendingDetailView: { ticketId: any } | null = null;

  // Ride status integration state
  showRideStatusDialog: boolean = false;
  rideStatusLoading: boolean = false;
  rideStatusError: string | null = null;
  currentTripSummary: TripSummary | null = null;
  rideStatusPickupLatLng: { lat: number; lng: number } | null = null;
  rideStatusDropoffLatLng: { lat: number; lng: number } | null = null;
  rideStatusVehicleLatLng: { lat: number; lng: number } | null = null;
  rideStatusMapLayer: 'standard' | 'satellite' = 'standard';
  rideStatusLeafletMap: L.Map | null = null;
  rideStatusPickupMarker: L.Marker | null = null;
  rideStatusDropoffMarker: L.Marker | null = null;
  rideStatusVehicleMarker: L.Marker | null = null;
  rideStatusRouteLine: L.Polyline | null = null;
  showRiderTracking: boolean = false;
  rideStatusLeafletOptions: any = {
    layers: [
      L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', { maxZoom: 19, attribution: '© OpenStreetMap' })
    ],
    zoom: 14,
    center: L.latLng(39.0, -77.0)
  };

  constructor(
    private _tripTicketService: TripTicketService,
    private _listService: ListService,
    private _confirmation: ConfirmationService,
    private _router: Router,
    private _route: ActivatedRoute,
    private _location: Location,
    private _tokenService: TokenService,
    private _headerEmitterService: HeaderEmitterService,
    private _localStorage: LocalStorageService,
    private _notification: NotificationEmitterService,
    private _fundSourceService: FundSourceService,
    private _providerPartnersService: ProviderPartnersService,
    private formBuilder: FormBuilder,
    private _domSanitizer: DomSanitizer,
    private _exportService: TripTicketExportService,
    private _uploadService: TripTicketUploadService
  ) {

    this.pageName = "Trip Tickets";
    this.screen = window.screen;

    this.dateValue1 = new Date();
    this.dateValue2 = new Date();

    this.ticketFilter.ticketFilterstatus = [];
    this.ticketFilter.claimingProviderName = [];
    this.ticketFilter.originatingProviderName = [];
    this.ticketFilter.tripTime = [];
    this.ticketFilter.advancedFilterParameter = [];
    this.claimantList = [];

    this.advancedFilter.customer_first_name = '';
    this.advancedFilter.customer_last_name = '';
    this.tripTime.pickUpDateTime = null;
    this.tripTime.dropOffDateTime = null;

    this.filterList = [];
    this.temArray = [];
    this.ticketClaimsArray = [];
    this.statusesArray = [];
    this.ticketActivity = [];
    this.ticketFilter.hospitalityServiceArea = [];
    this.ticketFilter.fundingSourceList = [];
    this.ticketFilter.customerEligibility = [];
    this.ticketFilter.selectedGeographicVal = [];
    this.ticketFilter.reqPickUpStartAndEndTime = [];
    this.ticketActivity = [];
    this.commentAdded = [];
    this.tripTicketCreate = [];
    this.claims = [];
    this.ticketStatus = [];
    this.ticketUpdate = [];

    this.Rescinded_Trip_Tickets = [
      {
        filterId: 'SHOW',
        filterName: 'Show Rescinded'
      },
      {
        filterId: 'HIDE',
        filterName: 'Hide Rescinded'
      },
      {
        filterId: 'ONLY',
        filterName: 'Only Rescinded'
      }

    ]


    this.initializeForms();

    // Bind the resize handler to maintain proper context
    this.resizeHandler = this.debouncedCalculateOptimalRowsPerPage.bind(this);

    // Listen for window resize events to recalculate pagination
    window.addEventListener('resize', this.resizeHandler);

    this.initializeEditForm();

    // Subscribe to upload service state changes
    this._uploadService.state$.pipe(
      takeUntil(this.destroy$)
    ).subscribe(state => {
      this.selectedFile = state.selectedFile;
      this.uploadProgress = state.uploadProgress;
      this.uploadError = state.uploadError;
      this.isUploading = state.isUploading;
      this.uploadSuccess = state.uploadSuccess;
      this.uploadResult = state.uploadResult;
      this.ticketCollection = state.ticketCollection;
      this.isDragging = state.isDragging;
      this.errorMessage = state.errorMessage;
    });
  }

  // ---------------- Uber Integration Methods ----------------
  openUberDialog(ticket: any): void {
    console.log('Opening Uber dialog for ticket:', ticket);
    this.showUberDialog = true;
    this.uberError = null;
    this.uberBookingConfirmed = false;
    this.showUberOptions = false;
    this.uberRideOptions = [];
    this.selectedUberOption = null;
    // derive pickup coordinates from ticket if available
    const pickupLat = ticket?.pickup_address?.latitude ?? ticket?.pickup_address?.lat ?? 39.0;
    const pickupLng = ticket?.pickup_address?.longitude ?? ticket?.pickup_address?.lng ?? -77.0;
    this.pickupLatLng = { lat: pickupLat, lng: pickupLng };
    // derive dropoff coordinates from ticket if available
    const dropoffLat = ticket?.drop_off_address?.latitude ?? ticket?.drop_off_address?.lat ?? (pickupLat + 0.01);
    const dropoffLng = ticket?.drop_off_address?.longitude ?? ticket?.drop_off_address?.lng ?? (pickupLng + 0.01);
    this.dropoffLatLng = { lat: dropoffLat, lng: dropoffLng };
    // initialize pickup time default: prefer ticket requested pickup date/time when available,
    // otherwise fall back to (now + 15m)
    let defaultPickupDate: Date | null = null;
    try {
      const reqDate = ticket?.requested_pickup_date || ticket?.requestedPickupDate || ticket?.requested_dropoff_date ||  null;
      const reqTime =   (ticket?.requested_dropoff_date && ticket?.requested_dropoff_time) ? ticket?.requested_dropoff_time : (ticket?.requested_pickup_time || ticket?.requestedPickupTime || null);

      if (reqDate) {
        // Prefer raw date string from backend (likely YYYY-MM-DD or ISO). Use time if available.
        const timePart = (typeof reqTime === 'string' && reqTime.length >= 5) ? reqTime.slice(0, 5) : '00:00';
        const iso = `${reqDate}T${timePart}`;
        const parsed = new Date(iso);
        if (!isNaN(parsed.getTime())) {
          defaultPickupDate = parsed;
        }
      }
    } catch (e) {
      // ignore and fall back
      defaultPickupDate = null;
    }
    if (!defaultPickupDate) {
      defaultPickupDate = new Date(Date.now() + 15 * 60 * 1000);
    }
    this.uberPickupDateTimeISO = this.toLocalDateTimeInputValue(defaultPickupDate);
    this.uberMinDateTime = this.toLocalDateTimeInputValue(new Date());
    this.uberPromisedDropOff = ticket?.requested_dropoff_date ? true :  false;
    console.log('uberPromisedDropOff:', this.uberPromisedDropOff);
    setTimeout(() => {
      if (this.uberLeafletMap) {
        this.uberLeafletMap.invalidateSize();
        // Center map between pickup and dropoff
        const bounds = L.latLngBounds([
          [this.pickupLatLng!.lat, this.pickupLatLng!.lng],
          [this.dropoffLatLng!.lat, this.dropoffLatLng!.lng]
        ]);
        this.uberLeafletMap.fitBounds(bounds.pad(0.2));
        this.addOrMoveUberMarkers();
        this.drawUberRouteLine();
      }
    }, 250);
  }

  toLocalDateTimeInputValue(date: Date): string {
    const pad = (n: number) => n.toString().padStart(2, '0');
    return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
  }

  /**
   * Parse a local datetime string (YYYY-MM-DDTHH:mm) as America/Denver wall time
   * and return a Date representing the correct absolute instant (UTC) for that Denver time.
   * This avoids browser-local timezone shifting for users outside Denver.
   */
  private parseDenverLocalToDate(localIso: string): Date {
    if (!localIso || typeof localIso !== 'string') return new Date(NaN);
    const parts = localIso.split('T');
    if (parts.length !== 2) return new Date(NaN);
    const [yStr, mStr, dStr] = parts[0].split('-');
    const [hStr, minStr] = parts[1].split(':');
    const y = parseInt(yStr, 10);
    const m = parseInt(mStr, 10);
    const d = parseInt(dStr, 10);
    const hh = parseInt(hStr, 10);
    const mm = parseInt(minStr, 10);
    if ([y, m, d, hh, mm].some(v => Number.isNaN(v))) return new Date(NaN);

    // Start with a UTC guess for the same wall-clock components
    const utcGuess = Date.UTC(y, m - 1, d, hh, mm);

    // Determine Denver offset at that instant using Intl (e.g., GMT-7 or UTC-07:00)
    let offsetMinutes = -420; // default MST fallback
    try {
      const fmt = new Intl.DateTimeFormat('en-US', {
        timeZone: 'America/Denver',
        timeZoneName: 'shortOffset',
        year: 'numeric', month: '2-digit', day: '2-digit',
        hour: '2-digit', minute: '2-digit', hour12: false
      });
      const denverParts = fmt.formatToParts(new Date(utcGuess));
      const tz = denverParts.find(p => p.type === 'timeZoneName')?.value || '';
      // Try patterns like UTC-07:00 or GMT-7
      const m1 = tz.match(/([+-]\d{1,2})(?::?(\d{2}))?/);
      if (m1) {
        const h = parseInt(m1[1], 10);
        const mins = m1[2] ? parseInt(m1[2], 10) : 0;
        // Offset is hours from UTC for local = UTC + offset
        offsetMinutes = h * 60 + (h >= 0 ? mins : -mins);
      } else {
        const m2 = tz.match(/GMT([+-]\d{1,2})/);
        if (m2) offsetMinutes = parseInt(m2[1], 10) * 60;
      }
    } catch {
      // keep fallback
    }

    // Convert Denver local to UTC: UTC = local - offset
    const utcMs = Date.UTC(y, m - 1, d, hh, mm) - offsetMinutes * 60 * 1000;
    return new Date(utcMs);
  }

  onUberMapReady(map: L.Map): void {
    this.uberLeafletMap = map;
    if (this.pickupLatLng && this.dropoffLatLng) {
      this.addOrMoveUberMarkers();
      this.drawUberRouteLine();
      // Center map between pickup and dropoff
      const bounds = L.latLngBounds([
        [this.pickupLatLng.lat, this.pickupLatLng.lng],
        [this.dropoffLatLng.lat, this.dropoffLatLng.lng]
      ]);
      this.uberLeafletMap.fitBounds(bounds.pad(0.2));
    }
  }

  addOrMoveUberMarkers(): void {
    if (!this.uberLeafletMap) return;
    // Pickup marker
    try {
      if (this.uberPickupMarker) {
        // If marker exists but was removed from map (e.g., dialog close), re-add it
        if (!(this.uberLeafletMap.hasLayer(this.uberPickupMarker))) {
          this.uberPickupMarker.addTo(this.uberLeafletMap);
        }
        this.uberPickupMarker.setLatLng([this.pickupLatLng!.lat, this.pickupLatLng!.lng]);
      } else {
        this.uberPickupMarker = L.marker([this.pickupLatLng!.lat, this.pickupLatLng!.lng], {
          draggable: true,
          icon: this.pickupIcon,
          zIndexOffset: 1000
        }).addTo(this.uberLeafletMap);
        this.uberPickupMarker.on('dragend', (e: any) => {
          const pos = e.target.getLatLng();
          this.pickupLatLng = { lat: pos.lat, lng: pos.lng };
          this.drawUberRouteLine();
        });
      }
    } catch (e) {
      console.warn('Error adding/updating uberPickupMarker:', e);
    }
    // Drop-off marker
    try {
      if (this.uberDropoffMarker) {
        if (!(this.uberLeafletMap.hasLayer(this.uberDropoffMarker))) {
          this.uberDropoffMarker.addTo(this.uberLeafletMap);
        }
        this.uberDropoffMarker.setLatLng([this.dropoffLatLng!.lat, this.dropoffLatLng!.lng]);
      } else {
        this.uberDropoffMarker = L.marker([this.dropoffLatLng!.lat, this.dropoffLatLng!.lng], {
          draggable: true,
          icon: this.dropoffIcon,
          zIndexOffset: 1000
        }).addTo(this.uberLeafletMap);
        this.uberDropoffMarker.on('dragend', (e: any) => {
          const pos = e.target.getLatLng();
          this.dropoffLatLng = { lat: pos.lat, lng: pos.lng };
          this.drawUberRouteLine();
        });
      }
    } catch (e) {
      console.warn('Error adding/updating uberDropoffMarker:', e);
    }
  }
  // Draw a dotted curved line between pickup and drop-off
  drawUberRouteLine(): void {
    if (!this.uberLeafletMap || !this.pickupLatLng || !this.dropoffLatLng) return;
    // Remove previous line
    if (this.uberRouteLine) {
      this.uberLeafletMap.removeLayer(this.uberRouteLine);
      this.uberRouteLine = null;
    }
    // Create a quadratic bezier curve with the highest point at the midpoint, offset perpendicularly
    const p1 = L.latLng(this.pickupLatLng.lat, this.pickupLatLng.lng);
    const p2 = L.latLng(this.dropoffLatLng.lat, this.dropoffLatLng.lng);
    // Midpoint
    const midLat = (p1.lat + p2.lat) / 2;
    const midLng = (p1.lng + p2.lng) / 2;
    // Perpendicular offset (normalized)
    const dx = p2.lng - p1.lng;
    const dy = p2.lat - p1.lat;
    const length = Math.sqrt(dx * dx + dy * dy);
    let offsetLat = 0, offsetLng = 0;
    if (length > 0) {
      // Perpendicular direction (unit vector)
      offsetLat = -dx / length;
      offsetLng = dy / length;
    }
    const curveStrength = 0.16; // increased for more pronounced curve
    // Control point is at the midpoint, offset perpendicularly
    const ctrlLat = midLat + offsetLat * curveStrength;
    const ctrlLng = midLng + offsetLng * curveStrength;
    // Generate points along the curve
    const curvePoints: L.LatLng[] = [];
    for (let t = 0; t <= 1.0; t += 0.05) {
      const lat = (1 - t) * (1 - t) * p1.lat + 2 * (1 - t) * t * ctrlLat + t * t * p2.lat;
      const lng = (1 - t) * (1 - t) * p1.lng + 2 * (1 - t) * t * ctrlLng + t * t * p2.lng;
      curvePoints.push(L.latLng(lat, lng));
    }
    this.uberRouteLine = L.polyline(curvePoints, {
      color: '#1FBAD6',
      weight: 3,
      opacity: 0.8,
      dashArray: '8 8',
      lineCap: 'round',
      lineJoin: 'round',
    }).addTo(this.uberLeafletMap);
  }

  setUberMapLayer(layer: 'standard' | 'satellite'): void {
    if (layer === this.uberMapLayer) return;
    this.uberMapLayer = layer;
    if (!this.uberLeafletMap) return;
    // remove existing base layers
    this.uberLeafletMap.eachLayer((l: any) => {
      if (l instanceof L.TileLayer) {
        this.uberLeafletMap?.removeLayer(l);
      }
    });
    let tile: L.TileLayer;
    if (layer === 'standard') {
      tile = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', { maxZoom: 19, attribution: '© OpenStreetMap' });
    } else {
      // Use Esri World Imagery for real satellite imagery
      tile = L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}', {
        maxZoom: 19,
        attribution: 'Tiles © Esri — Source: Esri, i-cubed, USDA, USGS, AEX, GeoEye, Getmapping, Aerogrid, IGN, IGP, UPR-EGP, and the GIS User Community'
      });
    }
    tile.addTo(this.uberLeafletMap);
    if (this.pickupLatLng) {
      this.addOrMoveUberMarkers();
    }
  }

  requestUberOptions(): void {
    this.uberError = null;
    if (!this.pickupLatLng || !this.dropoffLatLng) {
      this.uberError = 'Missing pickup or drop-off location';
      return;
    }
    // Both pickupLatLng and dropoffLatLng are available for backend call
    this.uberLoading = true;
    const uberRequest: UberRequest = {
      tripTicketId: this.Ticket.id,
      pickupLatitude: this.pickupLatLng.lat,
      pickupLongitude: this.pickupLatLng.lng,
      dropoffLatitude: this.dropoffLatLng.lat,
      dropoffLongitude: this.dropoffLatLng.lng,
      promisedDropOff: this.uberPromisedDropOff,
      // Parse the input value as Denver-local time to preserve 9:30 AM Denver
      requestedPickupTime: this.parseDenverLocalToDate(this.uberPickupDateTimeISO)
    };

    this._tripTicketService.requestUberOptions(uberRequest).pipe(
      takeUntil(this.destroy$)
    ).subscribe(
      response => {
        this.uberLoading = false;
        console.log('Received Uber ride options:', response);
        this.uberRideOptions = (response.rideOptions ?? []).map(opt => ({
          ...opt,
          estimatedPickupTime: typeof opt.estimatedPickupTime === 'string'
            ? new Date(opt.estimatedPickupTime)
            : opt.estimatedPickupTime,
            estimatedDropoffTime: opt.estimatedDropoffTime && typeof opt.estimatedDropoffTime === 'string'
            ? new Date(opt.estimatedDropoffTime)
            : opt.estimatedDropoffTime,
        }));
        this.showUberOptions = true;
      },
      (_error: any) => {
        this._notification.error("Error", "Failed to request Uber trip");
        this.uberLoading = false;
      }
    );
  }

  resetUberConfig(): void {
    this.showUberOptions = false;
    this.selectedUberOption = null;
    this.uberRideOptions = [];
    this.uberBookingInProgress = false;
    this.uberBookingConfirmed = false;
    this.uberError = null;
  }

  selectUberOption(opt: UberRideOption): void {
    this.selectedUberOption = opt;
  }


  private formatPhoneNumberToE164(phoneNumber: string | undefined): string | undefined {
    if (!phoneNumber) return undefined;

    // Remove all non-numeric characters
    const digitsOnly = phoneNumber.replace(/\D/g, '');

    // Check if we have a valid 10-digit US number
    if (digitsOnly.length === 10) {
      return `+1${digitsOnly}`;
    }

    // If the number already has the country code (11 digits starting with 1)
    if (digitsOnly.length === 11 && digitsOnly.startsWith('1')) {
      return `+${digitsOnly}`;
    }

    // Return the original number if it's already in E.164 format
    if (phoneNumber.startsWith('+1') && phoneNumber.length === 12) {
      return phoneNumber;
    }

    // Return undefined for invalid numbers
    console.warn('Invalid phone number format:', phoneNumber);
    return undefined;
  }

  /**
   * Transform the selected Ticket and UberRideOption into an UberRideRequest object
   * Maps data according to the Java API requirements
   */
  private transformToUberRideRequest(ticket: any, selectedOption: UberRideOption): UberRideRequest {
    // Create guest from ticket customer information
    const guest: UberGuest = {
      first_name: ticket.customer_first_name || '',
      last_name: ticket.customer_last_name || '',
      phone_number: this.formatPhoneNumberToE164(ticket.customer_mobile_phone) || this.formatPhoneNumberToE164(ticket.customer_home_phone) || undefined,
      email: ticket.customer_email || undefined
    };

    // Create pickup coordinates from ticket or current pickup coordinates
    const pickup: CoordinatesWithPlace = {
      latitude: this.pickupLatLng?.lat || ticket.pickup_address?.latitude || 0,
      longitude: this.pickupLatLng?.lng || ticket.pickup_address?.longitude || 0,
      place: null
    };

    // Create dropoff coordinates from ticket or current dropoff coordinates
    const dropoff: CoordinatesWithPlace = {
      latitude: this.dropoffLatLng?.lat || ticket.drop_off_address?.latitude || 0,
      longitude: this.dropoffLatLng?.lng || ticket.drop_off_address?.longitude || 0,
      place: null
    };

    // Create scheduling with pickup time converted to UNIX timestamp
    const scheduling: Scheduling = {
      pickup_time: selectedOption.estimatedPickupTime ? Math.floor(selectedOption.estimatedPickupTime.getTime()) : Math.floor(Date.now()),
      dropoff_time: null
    };

    // Build the complete UberRideRequest
    const uberRideRequest: UberRideRequest = {
      guest: guest,
      pickup: pickup,
      dropoff: dropoff,
      note_for_driver: ticket.trip_notes || undefined,
      additional_guests: null,
      communication_channel: "SMS",
      product_id: selectedOption.productId,
      fare_id: selectedOption.fareId,
      sender_display_name: "DRCOG",
      call_enabled: true,
      trip_ticket_id: ticket.id,
      scheduling: scheduling,
      uber_fare: selectedOption.price

    };

    return uberRideRequest;
  }

  confirmUberSelection(): void {
    if (!this.selectedUberOption || !this.Ticket) return;

    this.uberBookingInProgress = true;
    this.uberError = null;


    // Transform the ticket and selected option to UberRideRequest
    const uberRideRequest = this.transformToUberRideRequest(this.Ticket, this.selectedUberOption);


    // Send the UberRideRequest to the bookUberOption endpoint
    this._tripTicketService.bookUberOption(uberRideRequest).pipe(
      takeUntil(this.destroy$)
    ).subscribe(
      response => {
        this.uberBookingInProgress = false;
        this.uberBookingConfirmed = true;
        this.uberBookingReference = response.uberConfirmationId;
      },
      _error => {
        console.error('Error booking Uber ride:', _error);
        this._notification.error("Error", "Failed to request Uber trip");
        this.uberBookingInProgress = false;
        this.uberError = "Failed to book Uber ride. Please try again.";
      }
    );
  }
  // -----------------------------------------------------------

  // ---------------- Ride Status Integration Methods ----------------
  openRideStatusDialog(ticket: any): void {
    this.showRideStatusDialog = true;
    this.rideStatusError = null;
    this.currentTripSummary = null;
    this.showRiderTracking = false;

    // Set up map coordinates from ticket
    const pickupLat = ticket?.pickup_address?.latitude ?? ticket?.pickup_address?.lat ?? 39.0;
    const pickupLng = ticket?.pickup_address?.longitude ?? ticket?.pickup_address?.lng ?? -77.0;
    this.rideStatusPickupLatLng = { lat: pickupLat, lng: pickupLng };

    const dropoffLat = ticket?.drop_off_address?.latitude ?? ticket?.drop_off_address?.lat ?? (pickupLat + 0.01);
    const dropoffLng = ticket?.drop_off_address?.longitude ?? ticket?.drop_off_address?.lng ?? (pickupLng + 0.01);
    this.rideStatusDropoffLatLng = { lat: dropoffLat, lng: dropoffLng };

    console.debug('Ride Status Dialog opened with coordinates:', {
      pickup: this.rideStatusPickupLatLng,
      dropoff: this.rideStatusDropoffLatLng,
      ticketData: {
        pickup_address: ticket?.pickup_address,
        drop_off_address: ticket?.drop_off_address
      }
    });

    // Initialize map after a short delay to ensure DOM is ready
    setTimeout(() => {
      if (this.rideStatusLeafletMap && this.rideStatusPickupLatLng && this.rideStatusDropoffLatLng) {
        const bounds = L.latLngBounds([
          [this.rideStatusPickupLatLng.lat, this.rideStatusPickupLatLng.lng],
          [this.rideStatusDropoffLatLng.lat, this.rideStatusDropoffLatLng.lng]
        ]);
        this.rideStatusLeafletMap.fitBounds(bounds.pad(0.2));
        this.addOrMoveRideStatusMarkers();
        this.drawRideStatusRouteLine();
      }
    }, 250);

    // Load ride status data
    this.loadRideStatus(ticket.id);
  }

  loadRideStatus(tripTicketId?: string): void {
    const ticketId = tripTicketId || this.Ticket?.id;
    if (!ticketId) return;

    this.rideStatusLoading = true;
    this.rideStatusError = null;

    this._tripTicketService.currentRideStatus(ticketId.toString()).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (response: TripSummary) => {
        this.rideStatusLoading = false;
        this.currentTripSummary = response;

        // Update vehicle location if available
        if (response.vehicle_location) {
          this.rideStatusVehicleLatLng = {
            lat: response.vehicle_location.latitude,
            lng: response.vehicle_location.longitude
          };
          this.updateRideStatusMap();
        }
      },
      error: (_error: any) => {
        console.error('Error loading ride status:', _error);
        this._notification.error('Error', 'Failed to load ride status');
        this.rideStatusLoading = false;
      }
    });
  }

  onRideStatusMapReady(map: L.Map): void {
    this.rideStatusLeafletMap = map;
    // Ensure the map knows its container size (dialog may have just become visible)
    try {
      // Small timeout to allow dialog show animation to finish
      setTimeout(() => {
        if (this.rideStatusLeafletMap) {
          this.rideStatusLeafletMap.invalidateSize();
        }

        // Place markers and draw route
        this.addOrMoveRideStatusMarkers();
        this.drawRideStatusRouteLine();

        // If we already have pickup/dropoff coordinates, fit bounds so both are visible
        if (this.rideStatusPickupLatLng && this.rideStatusDropoffLatLng && this.rideStatusLeafletMap) {
          this.fitRideStatusBounds();
        }
      }, 150);
    } catch (e) {
      console.warn('onRideStatusMapReady: failed to invalidate size or fit bounds', e);
      this.addOrMoveRideStatusMarkers();
      this.drawRideStatusRouteLine();
    }
  }

  /**
   * Fit map view to include pickup/dropoff (and vehicle if present).
   * Uses a fallback zoom for tiny bounds (very close points) to avoid excessive zoom-in.
   */
  private fitRideStatusBounds(): void {
    if (!this.rideStatusLeafletMap || !this.rideStatusPickupLatLng || !this.rideStatusDropoffLatLng) return;

    try {
      const map = this.rideStatusLeafletMap;
      // Invalidate size just before changing view to avoid leaflet centering issues inside dialogs
      map.invalidateSize();

      const points: L.LatLngExpression[] = [
        [this.rideStatusPickupLatLng.lat, this.rideStatusPickupLatLng.lng],
        [this.rideStatusDropoffLatLng.lat, this.rideStatusDropoffLatLng.lng]
      ];

      if (this.rideStatusVehicleLatLng) {
        points.push([this.rideStatusVehicleLatLng.lat, this.rideStatusVehicleLatLng.lng]);
      }

      const bounds = L.latLngBounds(points as L.LatLngExpression[]);

      // If the two points are extremely close, use a sensible zoom level instead of fitBounds
      const ne = bounds.getNorthEast();
      const sw = bounds.getSouthWest();
      const latDiff = Math.abs(ne.lat - sw.lat);
      const lngDiff = Math.abs(ne.lng - sw.lng);

      const TINY_THRESHOLD = 0.0005; // ~50m
      if (latDiff < TINY_THRESHOLD && lngDiff < TINY_THRESHOLD) {
        const center = bounds.getCenter();
        // Use zoom 16 as a default 'street' level; clamp between map min/max if possible
        const fallbackZoom = 16;
        map.setView(center, fallbackZoom);
      } else {
        map.fitBounds(bounds.pad(0.12));
      }
    } catch (err) {
      console.warn('fitRideStatusBounds error', err);
    }
  }

  setRideStatusMapLayer(layer: 'standard' | 'satellite'): void {
    if (!this.rideStatusLeafletMap) return;

    this.rideStatusMapLayer = layer;
    this.rideStatusLeafletMap.eachLayer((leafletLayer) => {
      if (leafletLayer instanceof L.TileLayer) {
        this.rideStatusLeafletMap!.removeLayer(leafletLayer);
      }
    });

    let tileLayer: L.TileLayer;
    if (layer === 'satellite') {
      tileLayer = L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}', {
        maxZoom: 19,
        attribution: '© Esri'
      });
    } else {
      tileLayer = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '© OpenStreetMap'
      });
    }
    this.rideStatusLeafletMap.addLayer(tileLayer);
  }

  addOrMoveRideStatusMarkers(): void {
    if (!this.rideStatusLeafletMap) return;

    console.debug('addOrMoveRideStatusMarkers: map ready, pickup:', this.rideStatusPickupLatLng, 'dropoff:', this.rideStatusDropoffLatLng, 'vehicle:', this.rideStatusVehicleLatLng);

    const safeSetMarker = (marker: L.Marker | null, latlng: { lat: number; lng: number } | null, icon: L.Icon | L.DivIcon, popupText?: string) => {
      if (!latlng) return marker;
      try {
        if (marker) {
          // If the marker was previously removed from the map its DOM element may be missing.
          // Ensure it's present on the map before attempting to setLatLng which touches the DOM (_leaflet_pos).
          if (!this.rideStatusLeafletMap!.hasLayer(marker) || !marker.getElement()) {
            marker.addTo(this.rideStatusLeafletMap!);
          }
          marker.setLatLng([latlng.lat, latlng.lng]);
        } else {
          const created = L.marker([latlng.lat, latlng.lng], { icon }).addTo(this.rideStatusLeafletMap!);
          if (popupText) created.bindPopup(popupText);
          marker = created;
        }
      } catch (err) {
        // Defensive fallback: if something went wrong (missing DOM node, internal leaflet state), recreate the marker
        console.warn('safeSetMarker failed, recreating marker', popupText, err);
        try {
          if (marker && this.rideStatusLeafletMap && this.rideStatusLeafletMap.hasLayer(marker)) {
            this.rideStatusLeafletMap.removeLayer(marker);
          }
        } catch (e) {
          // ignore
        }
        const recreated = L.marker([latlng.lat, latlng.lng], { icon }).addTo(this.rideStatusLeafletMap!);
        if (popupText) recreated.bindPopup(popupText);
        marker = recreated;
      }
      return marker;
    };

    // Pickup marker
    this.rideStatusPickupMarker = safeSetMarker(this.rideStatusPickupMarker, this.rideStatusPickupLatLng, this.pickupIcon, 'Pickup Location');

    // Dropoff marker
    this.rideStatusDropoffMarker = safeSetMarker(this.rideStatusDropoffMarker, this.rideStatusDropoffLatLng, this.dropoffIcon, 'Drop-off Location');

    // Vehicle marker (if location available)
    if (this.rideStatusVehicleLatLng) {
      this.rideStatusVehicleMarker = safeSetMarker(this.rideStatusVehicleMarker, this.rideStatusVehicleLatLng, this.vehicleIcon, 'Vehicle Location');
    }
  }

  drawRideStatusRouteLine(): void {
    if (!this.rideStatusLeafletMap || !this.rideStatusPickupLatLng || !this.rideStatusDropoffLatLng) return;

    // Remove previous line
    if (this.rideStatusRouteLine) {
      this.rideStatusLeafletMap.removeLayer(this.rideStatusRouteLine);
      this.rideStatusRouteLine = null;
    }

    // Create a simple line connecting pickup and dropoff
    const latlngs: L.LatLngExpression[] = [
      [this.rideStatusPickupLatLng.lat, this.rideStatusPickupLatLng.lng] as L.LatLngTuple,
      [this.rideStatusDropoffLatLng.lat, this.rideStatusDropoffLatLng.lng] as L.LatLngTuple
    ];

    this.rideStatusRouteLine = L.polyline(latlngs, {
      color: '#1FBAD6',
      weight: 3,
      opacity: 0.7,
      dashArray: '10, 5'
    }).addTo(this.rideStatusLeafletMap);
  }

  updateRideStatusMap(): void {
    if (!this.rideStatusLeafletMap) return;

    this.addOrMoveRideStatusMarkers();

    // Adjust bounds to include vehicle location if available
    if (this.rideStatusPickupLatLng && this.rideStatusDropoffLatLng) {
      // Use centralized fit logic to handle tiny bounds and vehicle inclusion
      this.fitRideStatusBounds();
    }
  }

  // Create vehicle icon for map marker
  private vehicleIcon = L.divIcon({
    className: 'vehicle-marker',
    iconSize: [32, 32],
    iconAnchor: [16, 16],
    html: `
      <svg width="32" height="32" viewBox="0 0 32 32" xmlns="http://www.w3.org/2000/svg">
        <circle cx="16" cy="16" r="14" fill="#2196F3" stroke="#fff" stroke-width="2"/>
        <path d="M10 14h12l-1 4H11z" fill="#fff"/>
        <circle cx="12" cy="20" r="1.5" fill="#fff"/>
        <circle cx="20" cy="20" r="1.5" fill="#fff"/>
      </svg>
    `
  });

  // Helper methods for ride status display
  formatTimestamp(timestamp: number): string {
    return new Date(timestamp).toLocaleString();
  }

  formatDuration(seconds: number): string {
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);

    if (hours > 0) {
      return `${hours}h ${minutes}m`;
    }
    return `${minutes}m`;
  }

  getStatusClass(status: string): string {
    const statusLower = status.toLowerCase();
    if (statusLower.includes('completed') || statusLower.includes('arrived')) {
      return 'status-success';
    }
    if (statusLower.includes('en_route') || statusLower.includes('in_progress')) {
      return 'status-info';
    }
    if (statusLower.includes('cancelled') || statusLower.includes('failed')) {
      return 'status-danger';
    }
    return 'status-secondary';
  }

  getStarArray(rating: number): boolean[] {
    const stars: boolean[] = [];
    for (let i = 1; i <= 5; i++) {
      stars.push(i <= Math.floor(rating));
    }
    return stars;
  }

  toggleRiderTracking(): void {
    this.showRiderTracking = !this.showRiderTracking;
  }

  getSafeTrackingUrl(url: string): SafeResourceUrl {
    return this._domSanitizer.bypassSecurityTrustResourceUrl(url);
  }

  /**
   * Opens the tracking URL in a new browser tab/window
   * This is the recommended approach instead of iframe due to CSP restrictions
   */
  openTrackingUrl(url: string): void {
    if (!url) {
      this._notification.error('Error', 'No tracking URL available');
      return;
    }
    // Open in new tab with security best practices
    window.open(url, '_blank', 'noopener,noreferrer');
  }


  canCancelTrip(tripSummary: TripSummary | null): boolean {
    if ( this.loggedRole === 'ROLE_READONLY' ) return false;
    if (!tripSummary || !tripSummary.status) return false;
    return true;
  }


  /**
   * Cancel the current trip by calling the service endpoint
   */
  cancelCurrentTrip(tripSummary: TripSummary | null): void {
    if (!tripSummary || !this.Ticket) {
      this._notification.error('Error', 'No trip information available to cancel');
      return;
    }

    // Confirm with user before canceling
    this._confirmation.confirm({
      message: 'Are you sure you want to cancel this trip? This action cannot be undone.',
      header: 'Cancel Trip Confirmation',
      icon: 'ph ph-warning',
      accept: () => {
        this.rideStatusLoading = true;
        this._tripTicketService.cancelUberTrip(this.Ticket.id.toString()).pipe(
          takeUntil(this.destroy$)
        ).subscribe({
          next: (_response) => {

            if ( _response.result === true ) {
              this._notification.success('Success', 'Uber Trip has been cancelled successfully.');
              // Refresh ride status to reflect cancellation
              this.loadRideStatus(this.Ticket.id?.toString());
            } else {
              this._notification.error('Error', 'Failed to cancel Uber trip. Please try again.');
            }
            this.rideStatusLoading = false;
          },
          error: (_error: any) => {
            console.error('Error cancelling trip:', _error);
            this.rideStatusLoading = false;
            this._notification.error('Error', 'Failed to cancel trip. Please try again.');
          }
        });
      },
      reject: () => {
        // User cancelled the confirmation dialog, do nothing
      }
    });
  }
  // -----------------------------------------------------------



  /**
   * Handle browser back button to prevent users from being logged out
   * when navigating within the trip-ticket component views
   */
  @HostListener('window:popstate', ['$event'])
  onBrowserBackButtonClick(_event: PopStateEvent): void {


    // If we're currently showing the detail view, go back to grid view instead of navigating away
    if (!this.showGrid) {


      // Switch back to grid view
      this.backToGrid();

      // Use a small delay to ensure the state is updated before URL change
      setTimeout(() => {
        this._location.replaceState('/tripTicket');
      }, 0);

      return;
    }

    // If we're in activity view, go back to detail view
    if (this.activity) {


      this.backToTicketView();

      // Update URL to show detail view with ticket ID
      if (this.Ticket && this.Ticket.id) {
        setTimeout(() => {
          this._location.replaceState(`/tripTicket?view=detail&ticketId=${this.Ticket.id}`);
        }, 0);
      }

      return;
    }


    // If we're in grid view, allow normal navigation (but this might log out the user)
    // This is the expected behavior for truly leaving the trip ticket page
  } ngOnInit(): void {
    console.debug('🚀 ngOnInit started, isInitialLoad:', this.isInitialLoad);
    // Start interval to update current time every 5 seconds for timezone display
    this.currentTimeInterval = setInterval(() => {
      this.updateCurrentTime();
    }, 60000);

    // Check for URL parameters to restore view state
    this._route.queryParams.pipe(
      takeUntil(this.destroy$)
    ).subscribe(params => {
      const view = params['view'];
      const ticketId = params['ticketId'];

      if (view === 'detail' && ticketId) {
        // Find and show the ticket detail view
        // This will be handled after tickets are loaded
        this.pendingDetailView = { ticketId };
      }
    });
    // Expose component for debugging purposes
    if (typeof window !== 'undefined') {
      (window as any).tripTicketComponent = this;
    }

    // Unsubscribe from header subscription with 5-minute throttle
    // This prevents the component from refreshing too frequently
    this._headerEmitterService.header.pipe(
      throttleTime(5 * 60 * 1000), // 5 minutes = 5 * 60 * 1000 milliseconds
      takeUntil(this.destroy$)
    ).subscribe(
      () => {
        //console.log('Trip ticket auto-refresh triggered (5-minute throttle)');
        //this.getTicketsList(); // Disable auto-refresh for now
      }
    );

    // Set up a separate 5-minute auto-refresh timer
    timer(5 * 60 * 1000, 5 * 60 * 1000).pipe(
      takeUntil(this.destroy$)
    ).subscribe(() => {
      //console.log('Trip ticket 5-minute auto-refresh timer triggered');
      //this.getTicketsList(); // Disable auto-refresh for now
    });

    if (typeof this._tokenService.get() === 'undefined') {
      this._router.navigate(['/login']);
      return;
    }

    this.geographic = [
      { value: '1', label: 'Entirely Outside of Service Area' },
      { value: '2', label: 'Pickup location within service area' },
      { value: '3', label: 'Drop off within service area' }
    ]

    // Initialize eligibility Array using service
    this._tripTicketService.customerEligibility().pipe(
      takeUntil(this.destroy$)
    ).subscribe(
      (_response: any) => {
        this.eligibility = [];
        for (let index = 0; index < _response.length; index++) {
          if (_response[index] == null) {
            // Skip null values
          } else {
            const obj = { value: _response[index], label: _response[index] }
            this.eligibility.push(obj);
          }
        }
      },
      _error => {
        this._notification.error('Error', 'Failed to load customer eligibility options');
      }
    );

    // Initialize ticket status dropdown using ListService
    this._listService.queryStatus().pipe(
      takeUntil(this.destroy$)
    ).subscribe(
      (_response2: any) => {
        const statusList: any = [];
        for (let i = 0, iLen = _response2.length; i < iLen; i++) {
          const value = (_response2[i].statusId).toString();
          const label = _response2[i].type;
          statusList.push({ value: value, label: label });
          // Store the "Completed" status ID for later use
          if (label.toLowerCase() === 'completed') {
            this.completedStatusId = value;
          }
        }

        // Sort ticket statuses alphabetically by label (case-insensitive, trimmed)
        statusList.sort((a: any, b: any) => {
          const al = (a?.label ?? '').toString().trim().toLowerCase();
          const bl = (b?.label ?? '').toString().trim().toLowerCase();
          return al.localeCompare(bl);
        });
        this.TicketStatus = statusList;
      },
      _error => {
        this._notification.error('Error', 'Failed to load ticket status list');
      }
    );

    // Initialize claiming provider dropdown using ListService
    this._listService.queryProviderFilter().pipe(
      takeUntil(this.destroy$)
    ).subscribe(
      (_response3: any) => {
        const claimingProviderList: any = [];
        for (let i = 0, iLen = _response3.length; i < iLen; i++) {
          const value = (_response3[i].providerId).toString();
          const label = _response3[i].providerName;
          claimingProviderList.push({ value: value, label: label });
        }
        this.ClaimingProvider = claimingProviderList;
      },
      _error => {
        this._notification.error('Error', 'Failed to load claiming provider list');
      }
    );


    this._tripTicketService.getHospitalServiceList().pipe(
      takeUntil(this.destroy$)
    ).subscribe(
      (_response4: any) => {
        const arrayHospi: any = [];
        for (let i = 0, iLen = _response4.length; i < iLen; i++) {
          const value = (_response4[i].id).toString();
          const label = _response4[i].name;
          arrayHospi.push({ value: value, label: label });
        }
        this.hospitalArea = arrayHospi;
      },
      _error => {
        this._notification.error('Error', 'Failed to load hospital service area list');
      }
    );



    // Initialize originating provider dropdown using ListService
    this._listService.queryOriginatingProviderFilter().pipe(
      takeUntil(this.destroy$)
    ).subscribe(
      (_saveResponse: any) => {
        const originatingProviderList: any = [];
        for (let i = 0, iLen = _saveResponse.length; i < iLen; i++) {
          const value = (_saveResponse[i].providerId).toString();
          const label = _saveResponse[i].providerName;
          originatingProviderList.push({ value: value, label: label });
        }
        // Sort originating providers alphabetically by label for stable UX
        originatingProviderList.sort((a: any, b: any) => {
          if (!a?.label) return -1;
          if (!b?.label) return 1;
          return String(a.label).localeCompare(String(b.label));
        });
        this.OriginatingProvider = originatingProviderList;
        // Populate uploadOriginatingProviderOptions now that OriginatingProvider is loaded
        try {
          if (this.loggedRole === 'ROLE_ADMIN') {
            this.uploadOriginatingProviderOptions = [...this.OriginatingProvider];
          } else {
            const myProviderId = String(this.loggedProviderId);
            const found = this.OriginatingProvider.find((p: any) => String(p.value) === myProviderId);
            this.uploadOriginatingProviderOptions = found ? [found] : [];
            this.selectedOriginatingProvider = found ? found.value : null;
          }
        } catch (e) {
          console.warn('Failed to set uploadOriginatingProviderOptions after load', e);
          this.uploadOriginatingProviderOptions = [];
        }
      },
      _error => {
        this._notification.error('Error', 'Failed to load originating requestor list');
      }
    );

    // Get logged in user info
    this.loggedRole = this._localStorage.get('Role');
    this.loggedProviderId = this._localStorage.get('providerId');
    this.dates = { start: null, end: null };



    // Prepare originating provider options for upload dialog based on role
    try {
      if (Array.isArray(this.OriginatingProvider)) {
        if (this.loggedRole === 'ROLE_ADMIN') {
          // Admin sees all originating providers
          this.uploadOriginatingProviderOptions = [...this.OriginatingProvider];
        } else {
          // Non-admins see only their provider entry
          const myProviderId = String(this.loggedProviderId);
          const found = this.OriginatingProvider.find((p: any) => String(p.value) === myProviderId);
          this.uploadOriginatingProviderOptions = found ? [found] : [];
          this.selectedOriginatingProvider = found ? found.value : null;
        }
      }
    } catch (e) {
      console.warn('Failed to prepare upload originating requestor options', e);
      this.uploadOriginatingProviderOptions = [];
    }

    // Validate providerId before proceeding
    if (this.loggedProviderId === null || this.loggedProviderId === undefined ||
      String(this.loggedProviderId).trim() === '' || String(this.loggedProviderId) === 'null') {
      console.warn('Invalid or missing providerId detected:', this.loggedProviderId);
      // Don't call summaryList if providerId is invalid
      this.loggedProviderId = null;
    }

    if (this.loggedRole == 'ROLE_PROVIDERADMIN' || this.loggedRole == 'ROLE_PROVIDERUSER' || this.loggedRole == 'ROLE_ADMIN') {
      // Initialize funding sources using FundSourceService - only call summaryList if we have a valid providerId
      if (this.loggedProviderId !== null && this.loggedProviderId !== undefined) {
        this.summaryList();
      } else {
        console.warn('Skipping summaryList call due to invalid providerId');
      }
      this._fundSourceService.getAllFundSources().pipe(
        takeUntil(this.destroy$)
      ).subscribe(
        (response: any) => {
          const fundSourceArray: any[] = [];
          this.fundResource = [];
          // removed unused variable otherFundItem

          // Sort funds with "Other" at the end
          response.forEach((element: any) => {
            if (element.status == true) {
              if ((element.name == "Other") || (element.name == "other")) {
                this.otherFundName = element;
                this.fundId = element.fundsrc_id;
              } else {
                this.fundResource.push(element);
              }
            }
          });

          // Add "Other" at the end of the array
          if (this.otherFundName !== undefined) {
            this.fundResource.push(this.otherFundName);
            this.isEnable = true;
          }

          // Build FundingSource array for dropdown
          for (let i = 0, iLen = this.fundResource.length; i < iLen; i++) {
            const value = this.fundResource[i].name;
            const label = this.fundResource[i].name;
            fundSourceArray.push({
              value: value,
              label: label
            });
          }

          // Set FundingSource array for the dropdown
          this.FundingSource = fundSourceArray;
          this.fundFilter = response;

          // Also populate statusesArray for other uses
          this.statusesArray = [];
          for (let i = 0, iLen = this.fundResource.length; i < iLen; i++) {
            if (this.fundResource[i].status == true) {
              this.statusesArray.push({ label: this.fundResource[i].name, value: this.fundResource[i].fundsrc_id });
            }
          }
        },
        _error => {
          this._notification.error('Error', 'Failed to load fund sources');
        }
      );
    }

    // Handle filter states
    this.isFliter = this._localStorage.get('isFilter');
    this.filterId = this._localStorage.get('filterId');

    if (this.isFliter == "true") {
      this.selectedFilterName = this._localStorage.get('filterName');
      this.filterId = this._localStorage.get('filterId');
      this.selectedFilter(this.filterId);
    }

    this.saveFilterDropdown = false;
    this.Filters = true;
    this.getFilterList();

    // Set temporary show flag based on screen width
    if (screen.width > 1024) {
      this.isTempShow = false;
    }

    // Initialize table columns
    this.cols = [
      { field: 'ticket_id', header: 'TTS ID' },
      { field: 'requested_pickup_date', header: 'Requested Pickup Date' },
      { field: 'requested_pickup_time', header: 'Requested Pickup Time' },
      { field: 'requested_dropoff_date', header: 'Requested Dropoff Date' },
      { field: 'requested_dropOff_time', header: 'Requested Dropoff Time' },
      { field: 'pickup_address', header: 'Pickup Address' },
      { field: 'drop_off_address', header: 'Dropoff Address' },
      { field: 'customer', header: 'Customer' }
    ];

    this.exportColumns = this.cols.map(col => ({ title: col.header, dataKey: col.field }));
    console.debug('📋 ngOnInit calling getTicketsList (call #1)');
    this.getTicketsList();

    // Calculate optimal rows per page after initial load
    // This needs to happen after the component is fully initialized
    console.debug('⏱️ ngOnInit setting timeout for calculateOptimalRowsPerPage');
    setTimeout(() => {
      console.debug('⏰ setTimeout fired, calling calculateOptimalRowsPerPage');
      this.calculateOptimalRowsPerPage();
    }, 100);
  }

  /**
   * Calculate optimal rows per page based on actual available table height
   * to eliminate vertical scrollbars while maximizing visible tickets
   */
  private calculateOptimalRowsPerPage(): void {
    console.debug('📐 calculateOptimalRowsPerPage called, isInitialLoad:', this.isInitialLoad, 'TicketsList length:', this.TicketsList?.length || 0);
    try {
      // Get the actual table container element if it exists
      const tableContainer = document.querySelector('.p-datatable-scrollable-body');
      let availableHeight: number;

      if (tableContainer) {
        // Use the actual scrollable container height
        const containerRect = tableContainer.getBoundingClientRect();
        availableHeight = containerRect.height;
      } else {
        // Fallback: calculate based on the CSS scrollHeight value
        // The CSS sets scrollHeight="calc((100vh - 270px) * 0.95)"
        const viewportHeight = window.innerHeight;
        const calculatedScrollHeight = (viewportHeight - 270) * 0.95;
        availableHeight = calculatedScrollHeight;
      }

      // Subtract space for table header within the scrollable area
      const tableHeaderHeight = 40; // Height of the actual table header row
      const availableRowSpace = availableHeight - tableHeaderHeight;

      // Calculate number of rows that can fit without scrolling
      // Use a more precise row height estimation
      const preciseRowHeight = 42; // More accurate based on p-datatable-sm styling
      const calculatedRows = Math.floor(availableRowSpace / preciseRowHeight);

      // Cap the number of rows per page based on screen height
      let dynamicMaxRows = 25;
      if (availableHeight < 900) {
        dynamicMaxRows = 7;
      } else if (availableHeight < 1200) {
        dynamicMaxRows = 15;
      }
      const optimalRows = Math.max(
        this.MIN_ROWS_PER_PAGE,
        Math.min(calculatedRows, dynamicMaxRows)
      );


      // Only update if the value has changed to avoid unnecessary re-renders
      if (optimalRows !== this.rowsPerPage) {
        // previousRows removed (unused)
        this.rowsPerPage = optimalRows;
        this.rows = optimalRows;

        // Update rowsPerPageOptions to include our calculated value
        this.updateRowsPerPageOptions(optimalRows);

        // Force PrimeNG table to update by triggering change detection
        if (this.tripTicketTable) {
          // Reset to first page when rows per page changes
          this.first = 0;

          // Force the table to recognize the new rows value
          setTimeout(() => {
            if (this.tripTicketTable) {
              this.tripTicketTable.rows = optimalRows;
              this.tripTicketTable.first = 0;
              // Only refresh tickets if this is not the initial load (i.e., user triggered resize)
              if (!this.isInitialLoad && this.TicketsList && this.TicketsList.length > 0) {
                this.getTicketsList();
              }
            }
          }, 0);
        }
      }
    } catch (error) {
      console.error('Error calculating optimal rows per page:', error);
      // Fallback to default value
      this.rowsPerPage = 10;
      this.rows = 10;
    }
  }

  /**
   * Debounced version of calculateOptimalRowsPerPage to avoid excessive calculations during resize
   */
  private debouncedCalculateOptimalRowsPerPage(): void {
    if (this.resizeTimeout) {
      clearTimeout(this.resizeTimeout);
    }

    this.resizeTimeout = setTimeout(() => {
      this.calculateOptimalRowsPerPage();
    }, this.RESIZE_DEBOUNCE_TIME);
  }

  /**
   * Update the rows per page options to include the calculated optimal value
   */
  private updateRowsPerPageOptions(optimalRows: number): void {
    const standardOptions = [5, 10, 25, 50, 100];

    // Add our calculated value to the options if it's not already there
    if (!standardOptions.includes(optimalRows)) {
      this.rowsPerPageOptions = [...standardOptions, optimalRows].sort((a, b) => a - b);
    } else {
      this.rowsPerPageOptions = standardOptions;
    }
  }

  ngOnDestroy(): void {
    // Clear the current time interval to avoid memory leaks
    if (this.currentTimeInterval) {
      clearInterval(this.currentTimeInterval);
      this.currentTimeInterval = null;
    }
    // Complete the subject to unsubscribe from all subscriptions
    this.destroy$.next();
    this.destroy$.complete();

    // Clear any pending resize timeout
    if (this.resizeTimeout) {
      clearTimeout(this.resizeTimeout);
    }

    // Remove resize event listener to prevent memory leaks
    if (this.resizeHandler) {
      window.removeEventListener('resize', this.resizeHandler);
    }
  }

  getFilterList(): void {
    this._listService.filterList().pipe(
      takeUntil(this.destroy$)
    ).subscribe((response: any) => {
      this.filterList = response;
    });
  }

  private logDataStructure(data: any): void {
    if (!data) {

      return;
    }

    if (Array.isArray(data)) {

      if (data.length > 0) {
        //console.log('First item structure:', Object.keys(data[0]));
        //console.log('Sample item:', data[0]);
      }
    } else {
      //console.log('Data is an object with properties:', Object.keys(data));
      if (data.data && Array.isArray(data.data)) {
        //console.log('data.data is an array with', data.data.length, 'items');
        if (data.data.length > 0) {
          //console.log('First item structure:', Object.keys(data.data[0]));
          //console.log('Sample data.data item:', data.data[0]);
        }
      }
    }
  }


  private initializeForms() {

    this.commentForm = this.formBuilder.group({
      comment: ['', Validators.required],
    });


    this.claimForm = this.formBuilder.group({
      fare: [{ value: null, disabled: false }],
      notes: [{ value: '', disabled: false }],
      pickupDate: [{ value: '', disabled: false }],
      proposedPickupTime: [{ value: '', disabled: false }],
      dropoffDate: [{ value: '', disabled: true }],
      dropoffTime: [{ value: '', disabled: true }],
    });
  }

  private resetAndInitializeForm() {

    // Re-create the comment form entirely
    this.commentForm = this.formBuilder.group({
      comment: ['', Validators.required],
    });

  }

  getTicketsList(): void {
    this.loading = true;
    // Convert first index to page number (pages are 1-based)
    const pageNumber = Math.floor(this.first / this.rows) + 1;
    if (this.loggedRole == 'ROLE_PROVIDERADMIN' || this.loggedRole == 'ROLE_PROVIDERUSER') {
      console.debug('Getting tickets for provider user/admin', this.loggedRole);
      this._tripTicketService.getTicketsList(pageNumber, this.rows, this.sortField, this.sortOrder).pipe(
        takeUntil(this.destroy$),
        finalize(() => this.loading = false)
      ).subscribe(
        (response: any) => {
          this.processTicketResponse(response);
        },
        _error => {
          this.loading = false;
          this._notification.error('Error loading tickets', 'Failed to load trip tickets');
        }
      );
    } else {

      this._tripTicketService.getAllTicketsList(pageNumber, this.rows, this.sortField, this.sortOrder).pipe(
        takeUntil(this.destroy$),
        finalize(() => this.loading = false)
      ).subscribe(
        (response: any) => {
          this.processTicketResponse(response);
        },
        _error => {
          this.loading = false;
          this._notification.error('Error loading tickets', 'Failed to load trip tickets');
        }
      );
    }
  }


  processTicketResponse(response: any): void {
    this.logDataStructure(response);

    // Reset selection state when loading new tickets
    this.selectedTickets = [];
    this.selectAll = false;

    // Mark initial load as complete after first successful response
    this.isInitialLoad = false;

    // Handle different response formats for admin
    if (response && typeof response === 'object') {
      // Align component pagination state with backend metadata when available
      const backendPageSize = response.size ?? response.pageSize ?? response.pageable?.pageSize;
      if (typeof backendPageSize === 'number' && backendPageSize > 0) {
        this.rows = backendPageSize;
      }

      const backendPageNumber = response.number ?? response.pageNumber ?? response.pageable?.pageNumber;
      if (typeof backendPageNumber === 'number' && backendPageNumber >= 0) {
        this.first = backendPageNumber * this.rows;
      }

      // Check all possible property names for pagination data
      const dataArray = response.data || response.tickets || response.content || response;
      const totalCount = response.total || response.totalCount || response.totalRecords || response.totalElements || response.count;

      if (Array.isArray(dataArray)) {
        // If backend didn't paginate (plain array), handle client-side paging
        if (Array.isArray(response) || totalCount === undefined) {
          this.totalRecords = dataArray.length;
          const start = this.first;
          const end = this.first + this.rows;
          this.TicketsList = dataArray.slice(start, end);
        } else {
          // Backend provided pagination metadata; assume slice already present
          this.TicketsList = dataArray;

          // Only set totalRecords if we have a valid count from backend
          if (typeof totalCount === 'number' && totalCount >= 0) {
            this.totalRecords = totalCount;
          } else {
            // Fallback: if no pagination info, estimate based on current page
            if (dataArray.length === this.rows) {
              // Likely there are more pages
              this.totalRecords = Math.max(this.first + dataArray.length + 1, this.totalRecords);
            } else {
              // This might be the last page
              this.totalRecords = this.first + dataArray.length;
              console.warn('Admin estimated totalRecords for last page:', this.totalRecords);
            }
          }
        }
      } else {
        console.error('Admin: No valid data array found in response');
        this.TicketsList = [];
        this.totalRecords = 0;
      }
    } else {
      console.error('Admin: Unexpected response format:', response);
      this.TicketsList = [];
      this.totalRecords = 0;
    }

    if (typeof this.TicketsList == 'undefined') {
      this.TicketsList = [];
    } else {
      // Process tickets data for admin view
      this.TicketsList.forEach((ticket, index) => {

        // Format times (same as provider workflow)
        this.formatTicketTimes(ticket);

        // Format customer name
        ticket._customerName =
          (ticket.customer_first_name || '') + ' ' + (ticket.customer_last_name || '');

        // Format passenger info
        ticket._passenger = `Passenger : ${ticket.customer_seats_required || 0} ${ticket.customer_mobility_factors && ticket.customer_mobility_factors !== 'None' ? '[' + ticket.customer_mobility_factors + ']' : ''}`;

        ticket._mobility_device = `${ticket.customer_mobility_factors && ticket.customer_mobility_factors !== 'None' ? ticket.customer_mobility_factors  : ''}`;

        ticket._attendants = `${ticket.customer_seats_required || 0}`;


        // Format status
        ticket._status = ticket.status?.type || 'Available';

        // Initialize myTicket to 'false' by default
        ticket.myTicket = false;

        // Initialize selected property for export functionality
        ticket.selected = false;

        // Format pickup and dropoff datetime for display
        ticket._requested_pickupDateTime = (ticket.requested_pickup_date || '') + ' ' + (ticket.requested_pickup_time || '');
        // Only show dropoff date/time if both are present and valid
        if (
          ticket.requested_dropoff_date &&
          ticket.requested_dropoff_date !== '-' &&
          ticket.requested_dropoff_time &&
          ticket.requested_dropoff_time !== ''
        ) {
          ticket._requested_dropOffDateTime = ticket.requested_dropoff_date + ' ' + ticket.requested_dropoff_time;
        } else {
          ticket._requested_dropOffDateTime = '';
        }

        // Address formatting
        if (ticket.pickup_address && ticket.pickup_address!.street1 == null) {
          ticket.pickup_address!.street1 = "";
        }
        if (ticket.drop_off_address && ticket.drop_off_address!.street1 == null) {
          ticket.drop_off_address!.street1 = "";
        }

        // Handle claimant providers display
        if (!ticket.claimantProviders || ticket.claimantProviders === " ") {
          ticket.claimantProviders = "No Claimants";
        }

        // Ensure every ticket has an originator object
        if (!ticket.originator) {
          if (ticket.origin_provider_id && ticket.origin_provider_id! > 0) {
            // Valid provider ID - fetch provider name
            ticket.originator = {
              providerId: ticket.origin_provider_id,
              providerName: 'Unknown Provider' // Set default value
            };

            // Fetch provider name asynchronously
            this._tripTicketService.getProvider(ticket.origin_provider_id!.toString())
              .pipe(takeUntil(this.destroy$))
              .subscribe({
                next: (provider: any) => {
                  if (provider && provider.name) {
                    ticket.originator!.providerName = provider.name;
                  } else {
                    // Provider response did not include a name; keep the default providerName
                  }
                },
                error: (_error: Error) => {
                  console.error('Error fetching provider name for admin tickets:', _error);
                }
              });
          } else {
            // Invalid or 0 provider ID - create default originator
            ticket.originator = {
              providerId: ticket.origin_provider_id || 0,
              providerName: 'No Provider'
            };
          }
        }

        // Initialize claims array if needed
        if (typeof this.ticketClaimsArray[index] == "undefined") {
          this.ticketClaimsArray[index] = [];
        }

        // Process claims
        const claims = ticket.trip_Claims ?? [];

        if (claims.length > 0) {

          // Process each claim
          //for (let j = 0, jLen = claims.length; j < jLen; j++) {
          //claims.forEach((claim: any, j: number) => {
          for (const claim of claims) {

            // Count pending claims
            if (claim.status?.type === 'Pending') {
              this.pendingCount++;
            }

            // Format pickup time if available
            this.formatClaimTime(claim);

            // Store claim information for current user
            if (claim.claimant_provider_id === this.loggedProviderId) {

              // Make a copy of the claim so we don't accidentally modify the original
              this.ticketClaimsArray[index] = { ...claim };
              if (claim.status?.type === 'Cancelled' || claim.status?.type === 'Rescinded' || claim.status?.type === 'Declined') {
                // Handle cancelled, rescinded, or declined claims
                ticket.myTicket = false;
              } else {
                ticket.myTicket = true;

              }

              if (claim.status?.type === 'Pending' || claim.status?.type === 'Approved') {
                // Set claimant property
                ticket.claimant = {
                  providerName: claim.claimant_provider_name || '',
                  providerId: claim.claimant_provider_id || 0
                };
              }
            }
          }
          // Set claimantProviders display text if there are claims
          if (claims.length > 0) {
            // Extract provider names for display

            const claimantNames = claims
                                  .filter((c: any) => c.status?.type !== 'Cancelled' && c.status?.type !== 'Rescinded' && c.status?.type !== 'Declined')
                                  .map((c: any) => c.claimant_provider_name).filter(Boolean);
            ticket.claimantProviders = claimantNames.join(', ') || 'No Claimants';
          }
        } else {
          // No claims for this ticket
          ticket.claimantProviders = 'No Claimants';
        }
      });
    }

    // Handle pending detail view navigation (from URL parameters)
    if (this.pendingDetailView && this.TicketsList.length > 0) {
      const ticket = this.TicketsList.find(t => t.id?.toString() === this.pendingDetailView!.ticketId);
      if (ticket) {
        // Clear the pending state before calling viewTicket to avoid infinite loop
        this.pendingDetailView = null;
        this.viewTicket(ticket);
      } else {
        // Ticket not found, clear the pending state and stay in grid view
        this.pendingDetailView = null;
        this._router.navigate([], {
          relativeTo: this._route,
          queryParams: {},
          queryParamsHandling: 'replace'
        });
      }
    }
  }



  /**
   * Handle lazy loading events from p-table (pagination, sorting, filtering)
   */
  loadTicketsLazy(event: any): void {
    console.debug('🔄 loadTicketsLazy called, isInitialLoad:', this.isInitialLoad, 'event:', event);

    // Update pagination parameters
    const newFirst = event.first || 0;
    const newRows = event.rows || this.rowsPerPage;

    this.first = newFirst;
    this.rows = newRows;

    // If user manually changed rows per page, update our component property
    if (newRows !== this.rowsPerPage) {
      this.rowsPerPage = newRows;
    }

    // Update sorting parameters - only if sortField is defined in the event
    if (event.sortField !== undefined) {
      this.sortField = event.sortField;
      this.sortOrder = event.sortOrder || 1;
    }

    const eventSignature = this.computeLazyEventSignature(event);

    // Skip loading tickets during initial load to prevent duplicate API calls
    // The initial load is already handled by ngOnInit
    if (this.isInitialLoad) {
      return;
    }

    // If a filter is active, use it for pagination
    if (this.activeFilterState) {
      if (this.filterRequestInFlight) {
        if (eventSignature !== this.lastLazyEventSignature) {
          this.pendingLazyEvent = { ...event };
          this.pendingLazyEventSignature = eventSignature;
        }
        return;
      }

      this.lastLazyEventSignature = eventSignature;
      this.searchByCurrentFilter(
        this.activeFilterState.ticketFilter,
        this.activeFilterState.advancedFilter,
        this.activeFilterState.tripTime,
        { resetPage: false }
      );
    } else {
      if (eventSignature === this.lastLazyEventSignature) {
        return;
      }
      this.lastLazyEventSignature = eventSignature;
      // Load tickets with new parameters
      this.getTicketsList();
    }
  }

  private computeLazyEventSignature(event: any): string {
    const first = typeof event.first === 'number' ? event.first : this.first;
    const rows = typeof event.rows === 'number' ? event.rows : this.rows;
    const sortField = event.sortField ?? this.sortField ?? '';
    const sortOrder = typeof event.sortOrder === 'number' ? event.sortOrder : this.sortOrder ?? 1;
    return `${first}|${rows}|${sortField}|${sortOrder}`;
  }

  // Helper method to format ticket times
  private formatTicketTimes(ticket: any): void {
    // Defensive checks: treat undefined, null, empty strings as "no value" rather than falling back to 'today'
    const hasPickupDate = ticket.requested_pickup_date !== undefined && ticket.requested_pickup_date !== null && String(ticket.requested_pickup_date).trim() !== '';
    const hasPickupTime = ticket.requested_pickup_time !== undefined && ticket.requested_pickup_time !== null && String(ticket.requested_pickup_time).trim() !== '';

    if (!hasPickupDate) {
      ticket._requested_pickup_date = '';
      ticket._requested_pickup_time = '';
    } else {
      // Format pickup time only when a valid pickup time string is provided
      if (!hasPickupTime) {
        ticket._requested_pickup_time = '';
      } else if (typeof ticket.requested_pickup_time !== 'string') {
        console.warn('Unexpected requested_pickup_time format:', ticket.requested_pickup_time);
        ticket._requested_pickup_time = '';
      } else {
        const timeParts = ticket.requested_pickup_time.split(':');
        if (timeParts.length >= 2) {
          const hour = parseInt(timeParts[0], 10);
          const minute = timeParts[1];
          if (isNaN(hour)) {
            console.warn('Invalid hour format:', timeParts[0]);
            ticket._requested_pickup_time = '';
          } else if (hour > 12) {
            ticket._requested_pickup_time = (hour - 12).toString().padStart(2, '0') + ':' + minute + ' PM';
          } else if (hour === 12) {
            ticket._requested_pickup_time = hour + ':' + minute + ' PM';
          } else if (hour === 0) {
            ticket._requested_pickup_time = '12:' + minute + ' AM';
          } else {
            ticket._requested_pickup_time = hour.toString().padStart(2, '0') + ':' + minute + ' AM';
          }
        } else {
          console.warn('Invalid time format for pickup:', ticket.requested_pickup_time);
          ticket._requested_pickup_time = '';
        }
      }

      // Format pickup date
      try {
        const formatted = moment(ticket.requested_pickup_date);
        ticket._requested_pickup_date = formatted.isValid() ? formatted.format('MM/DD/YY') : '';
        if (!formatted.isValid()) console.warn('Invalid pickup date:', ticket.requested_pickup_date);
      } catch (e) {
        console.warn('Error formatting pickup date:', e);
        ticket._requested_pickup_date = '';
      }
    }

    const hasDropoffDate = ticket.requested_dropoff_date !== undefined && ticket.requested_dropoff_date !== null && String(ticket.requested_dropoff_date).trim() !== '';
    const hasDropoffTime = ticket.requested_dropOff_time !== undefined && ticket.requested_dropOff_time !== null && String(ticket.requested_dropOff_time).trim() !== '';

    if (!hasDropoffDate) {
      ticket._requested_dropoff_date = '';
      ticket._requested_dropoff_time = '';
    } else {
      // Only format dropoff time when provided
      if (!hasDropoffTime) {
        ticket._requested_dropoff_time = '';
      } else if (typeof ticket.requested_dropOff_time !== 'string') {
        console.warn('Unexpected requested_dropOff_time format:', ticket.requested_dropOff_time);
        ticket._requested_dropoff_time = '';
      } else {
        const timeParts = ticket.requested_dropOff_time.split(':');
        if (timeParts.length >= 2) {
          const hour = parseInt(timeParts[0], 10);
          const minute = timeParts[1];
          if (isNaN(hour)) {
            console.warn('Invalid hour format for dropoff:', timeParts[0]);
            ticket._requested_dropoff_time = '';
          } else if (hour > 12) {
            ticket._requested_dropoff_time = (hour - 12).toString().padStart(2, '0') + ':' + minute + ' PM';
          } else if (hour === 12) {
            ticket._requested_dropoff_time = hour + ':' + minute + ' PM';
          } else if (hour === 0) {
            ticket._requested_dropoff_time = '12:' + minute + ' AM';
          } else {
            ticket._requested_dropoff_time = hour.toString().padStart(2, '0') + ':' + minute + ' AM';
          }
        } else {
          console.warn('Invalid time format for dropoff:', ticket.requested_dropOff_time);
          ticket._requested_dropoff_time = '';
        }
      }

      // Format dropoff date
      try {
        const formatted = moment(ticket.requested_dropoff_date);
        ticket._requested_dropoff_date = formatted.isValid() ? formatted.format('MM/DD/YY') : '';
        if (!formatted.isValid()) console.warn('Invalid dropoff date:', ticket.requested_dropoff_date);
      } catch (e) {
        console.warn('Error formatting dropoff date:', e);
        ticket._requested_dropoff_date = '';
      }
    }
  }

  // Helper method to format claim times
  private formatClaimTime(claim: any): void {
    if (!claim) {
      console.log('formatClaimTime: claim is null or undefined');
      return;
    }

    try {


      if (claim.proposed_pickup_time) {
        // Ensure we're not trying to split undefined values
        if (typeof claim.proposed_pickup_time !== 'string') {
          // Try to convert to string if possible
          if (claim.proposed_pickup_time instanceof Date) {
            claim.proposed_pickup_time = claim.proposed_pickup_time.toISOString();
          } else {
            console.warn('Unexpected proposed_pickup_time format:', claim.proposed_pickup_time);
            claim._proposed_pickup_time = '-';
            return;
          }
        }

        // Check if the proposed_pickup_time contains a T (ISO format)
        if (claim.proposed_pickup_time.includes('T')) {
          // Parse the full datetime and format it
          const dt = moment(claim.proposed_pickup_time);
          if (dt.isValid()) {
            // Format the date part
            claim._proposed_pickup_date = dt.format('MM/DD/YY');
            // Format the time part
            claim._proposed_pickup_time = dt.format('HH:mm');

            return;
          }
        }

        // Fall back to splitting based on colons
        const timeParts = claim.proposed_pickup_time.split(':');
        if (timeParts.length >= 2) {
          const hour = parseInt(timeParts[0], 10);
          const minute = timeParts[1];

          if (isNaN(hour)) {
            console.warn('Invalid hour in claim time:', timeParts[0]);
            claim._proposed_pickup_time = '-';
          } else {
            claim._proposed_pickup_time = hour.toString().padStart(2, '0') + ':' + minute;
          }

        } else {
          console.warn('Invalid time format in claim:', claim.proposed_pickup_time);
          claim._proposed_pickup_time = '-';
        }
      } else {
        console.log('No proposed_pickup_time found on claim');
        claim._proposed_pickup_time = '-';
      }

      // Always set a user-friendly string for ackStatus
      if (claim.ackStatus === true) {
        claim.ackStatusString = 'Yes';
      } else {
        claim.ackStatusString = 'No';
      }


    } catch (e) {
      console.error('Error formatting claim time:', e);
      claim._proposed_pickup_time = '-';
    }
  }

  formatDateMMDDYYYY(value: string|Date|undefined|null): string | null {
    if (!value) return null;
    const d = value instanceof Date ? value : new Date(value);
    if (isNaN(d.getTime())) return null;
    return new Intl.DateTimeFormat('en-US', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    }).format(d);
  }

  /**
   * Convert a UTC timestamp to America/Denver timezone and format as MM/DD/YYYY hh:mm A
   * @param utcTimestamp - UTC timestamp string (e.g., "2025-11-24T20:28:12.220821Z")
   * @returns Formatted date string in Denver timezone (e.g., "11/24/2025 01:28 PM")
   */
  formatUtcToDenver(utcTimestamp: string | null | undefined): string {
    if (!utcTimestamp) return '-';

    try {
      // Parse the UTC timestamp
      const utcDate = new Date(utcTimestamp);
      if (isNaN(utcDate.getTime())) return '-';

      // Get the year to determine if we're in DST
      const year = utcDate.getUTCFullYear();

      // Calculate DST boundaries for America/Denver
      // DST starts: Second Sunday in March at 2:00 AM MST (9:00 AM UTC)
      const marchFirst = new Date(Date.UTC(year, 2, 1));
      const dstStart = new Date(Date.UTC(year, 2, (14 - marchFirst.getUTCDay()) % 7 + 8, 9));

      // DST ends: First Sunday in November at 2:00 AM MDT (8:00 AM UTC)
      const novFirst = new Date(Date.UTC(year, 10, 1));
      const dstEnd = new Date(Date.UTC(year, 10, (7 - novFirst.getUTCDay()) % 7 + 1, 8));

      // Determine if the date is in DST (MDT: UTC-6) or standard time (MST: UTC-7)
      const isDST = utcDate >= dstStart && utcDate < dstEnd;
      const offsetHours = isDST ? -6 : -7;

      // Apply the offset to get Denver local time
      const denverTime = new Date(utcDate.getTime() + offsetHours * 60 * 60 * 1000);

      // Format the date components
      const month = String(denverTime.getUTCMonth() + 1).padStart(2, '0');
      const day = String(denverTime.getUTCDate()).padStart(2, '0');
      const fullYear = denverTime.getUTCFullYear();

      // Format the time components
      let hours = denverTime.getUTCHours();
      const minutes = String(denverTime.getUTCMinutes()).padStart(2, '0');
      const ampm = hours >= 12 ? 'PM' : 'AM';
      hours = hours % 12;
      hours = hours === 0 ? 12 : hours;
      const hoursStr = String(hours).padStart(2, '0');

      return `${month}/${day}/${fullYear} ${hoursStr}:${minutes} ${ampm}`;
    } catch (e) {
      console.error('Error converting UTC to Denver timezone:', e);
      return '-';
    }
  }


  viewTicket(ticket: any): void {
    //console.log(ticket);

    if (ticket.origin_provider_id === this.loggedProviderId) {
      this.isProviderMatch = true;
    } else {
      this.isProviderMatch = false;
    }


    this.loadRideStatus(ticket.id);

    // Reset UI state
    this.showGrid = false;
    this.displayDialog = false; // Ensure comment dialog is closed
    this.showClaimInput = false;
    this.showEditInput = false;
    this.activity = false;
    this.isComment = false;
    this.isShowMap = false;
    this.update = false;

    // Clear temporary arrays
    this.ticketTempActivity = [];
    this.ticketTempStatus = [];
    this.ticketTempUpdate = [];
    this.ticketCommentTemp = [];
    this.ticketClaimsTemp = [];
    this.ticketTempId = [];
    this.ticketTempProvid = [];
    this.ticketTempTimeStamp = [];
    this.ticketClaimTempId = [];

    // Initialize activity arrays as empty arrays
    this.initializeActivityArrays();

    // Store ticket data
    this.ticketTemp = ticket;
    this.Ticket = ticket;

    // Store ticketId in localStorage if needed
    this._localStorage.set('ticketId', ticket.id);

    // Process pickup date and time
    this.pickupDate = ticket._requested_pickup_date;
    if (typeof ticket.requested_pickup_time === 'string' && ticket.requested_pickup_time.length >= 5) {
      this.proposedPickupTime = ticket.requested_pickup_time.slice(0, 5);
      this.requestedPickupDate = ticket.requested_pickup_date;
    } else {
      ticket.requested_pickup_time = "- ";
      this.proposedPickupTime = "-";
      this.requestedPickupDate = "- ";
    }

    // Set page name
    this.pageName = 'Edit';

    // Find ticket index
    this.viewTicketIndex = this.findSelectedTicketIndex(ticket);
    this.pendingCount = 0;
    this.ticketId = ticket.id;

    // Fetch provider lists using ListService
    this._listService.originatingProvider(ticket.originator.providerId).pipe(
      takeUntil(this.destroy$)
    ).subscribe(
      response => {
        this.providerListForComments = response;
      }
    );

    this._listService.originatingProvider(ticket.originator.providerId).pipe(
      takeUntil(this.destroy$)
    ).subscribe(
      response => {
        this.providerPartnerList = response;
      }
    );

    // If status is 'Available', get additional provider data for comments
    if (ticket.status.statusId === "1") {
      this._listService.originatingProvider(ticket.originator.providerId).pipe(
        takeUntil(this.destroy$)
      ).subscribe(
        response => {
          const tempArray = response;
          this.providerListForComments = [];
          for (let i = 0; i < tempArray.length; i++) {
            if (tempArray[i].providerId === ticket.originator.providerId) {
              this.providerListForComments.push(tempArray[i]);
            } else if (ticket.claimant && tempArray[i].providerId === ticket.claimant.providerId) {
              this.providerListForComments.push(tempArray[i]);
            }
          }
        }
      );
    }

    // Check if current user is the originator
    if (ticket.originator.providerId === this.loggedProviderId) {
      this.onceClaim = true;
    }

    // Process status information
    if (ticket.status.type === "Available") {
      if (ticket.trip_Claims) {
        for (let j = 0, jLen = ticket.trip_Claims.length; j < jLen; j++) {
          if (ticket.trip_Claims[j].status.type === "Pending") {
            this.pendingCount++;
          }
        }
      }

      if (this.pendingCount === 0) {
        this.status = this.pendingCount + " Claims ";
      } else if (this.pendingCount === 1) {
        this.status = this.pendingCount + " Claim Pending ";
      } else {
        this.status = this.pendingCount + " Claims Pending";
      }
    } else if (ticket.status.type === "Completed") {
      this.status = "Completed";
    } else {
      this.status = "Awaiting Result";
    }

    // Process address information

    if (this.Ticket.customer_address) {
      if (!this.Ticket.customer_address.street1 || this.Ticket.customer_address.street1 === null || this.Ticket.customer_address.street1.trim() === "") {
        this.Ticket.customer_address.street1 = "";
      }
      if (!this.Ticket.customer_address.city || this.Ticket.customer_address.city === null || this.Ticket.customer_address.city.trim() === "") {
        this.Ticket.customer_address.city = "";
      }
      if (!this.Ticket.customer_address.zipcode || this.Ticket.customer_address.zipcode === null || this.Ticket.customer_address.zipcode.trim() === "") {
        this.Ticket.customer_address.zipcode = "";
      }
      if (!this.Ticket.customer_address.state || this.Ticket.customer_address.state === null || this.Ticket.customer_address.state.trim() === "") {
        this.Ticket.customer_address.state = "";
      }

      this.Ticket.customer_address.commonName = this.Ticket.customer_address.street1 + " " +
        this.Ticket.customer_address.city + " " +
        this.Ticket.customer_address.zipcode + " " +
        this.Ticket.customer_address.state;


    }

    if (this.Ticket.pickup_address) {
      if (this.Ticket.pickup_address.street1 === null) {
        this.Ticket.pickup_address.commonName = "-";
      } else {
        this.Ticket.pickup_address.commonName = this.Ticket.pickup_address.street1 + ", " +
          this.Ticket.pickup_address.city + "-" +
          this.Ticket.pickup_address.zipcode + ", " +
          this.Ticket.pickup_address.state;
      }
    }

    if (this.Ticket.drop_off_address) {
      if (this.Ticket.drop_off_address.street1 === null) {
        this.Ticket.drop_off_address.street1 = "-";
      } else {
        this.Ticket.drop_off_address.commonName = this.Ticket.drop_off_address.street1 + ", " +
          this.Ticket.drop_off_address.city + "-" +
          this.Ticket.drop_off_address.zipcode + ", " +
          this.Ticket.drop_off_address.state;
      }
    }

    // Process customer DOB if present
    if (this.Ticket.customer_dob) {
      console.log('Original customer_dob:', this.Ticket.customer_dob);
      this.Ticket.customer_dob = this.formatDateMMDDYYYY(this.Ticket.customer_dob);
    }

    // Process comments if available - convert UTC timestamps to America/Denver timezone
    if (this.Ticket.trip_ticket_comments) {
      for (const comment of this.Ticket.trip_ticket_comments) {
        comment.created_at = this.formatUtcToDenver(comment.created_at);
        //this.Ticket.created_at = comment.created_at;
      }
    }

    // Fetch claims for this ticket
    this._tripTicketService.getClaims(ticket.id).pipe(
      takeUntil(this.destroy$)
    ).subscribe(
      response => {
        this.ticketClaimsArray = [];
        this.ticketClaimsArray = response;
        this.claimsArray = [];

        //this.logClaimsData('Raw claims response:', this.ticketClaimsArray);



        if (this.ticketClaimsArray && this.ticketClaimsArray.length > 0) {
          // Fix: Remove the filtering logic that might be preventing claims from showing
          // Just show all claims regardless of provider filtering for now
          for (let i = 0, iLen = this.ticketClaimsArray.length; i < iLen; i++) {
            if (this.ticketClaimsArray[i]) {
              // Ensure time formatting is applied
              this.formatClaimTime(this.ticketClaimsArray[i]);
              this.claimsArray.push(this.ticketClaimsArray[i]);

              // Debug each claim's provider ID
              console.log(`Claim ${i} provider ID:`, this.ticketClaimsArray[i].claimant_provider_id);
            }
          }

          this.Ticket.trip_Claims = this.claimsArray;
          //this.logClaimsData('After processing, final claims array:', this.claimsArray);
          //this.logClaimsData('After processing, Ticket.trip_Claims:', this.Ticket.trip_Claims);

          // Additional formatting for each claim
          if (this.Ticket.trip_Claims && this.Ticket.trip_Claims.length > 0) {
            for (let i = 0, iLen = this.Ticket.trip_Claims.length; i < iLen; i++) {
              // Set a user-friendly string for ackStatus if not already set
              if (typeof this.Ticket.trip_Claims[i].ackStatusString === 'undefined') {
                if (this.Ticket.trip_Claims[i].ackStatus === true) {
                  this.Ticket.trip_Claims[i].ackStatusString = 'Yes';
                } else {
                  this.Ticket.trip_Claims[i].ackStatusString = 'No';
                }
              }
              this.Ticket.trip_Claims[i].created_at = this.formatUtcToDenver(this.Ticket.trip_Claims[i].created_at);
              if (this.loggedProviderId === this.Ticket.trip_Claims[i].claimant_provider_id) {
                this.onceClaim = true;
              }
              console.log('Claimant Provider ID:', this.Ticket.trip_Claims[i].claimant_provider_id);
              console.log('Logged Provider ID:', this.loggedProviderId);
              if (this.loggedProviderId === this.Ticket.originator?.providerId) {
                this.test = true;
                console.log('Provider match found:', this.loggedProviderId, this.Ticket.originator?.providerId);
              } else {
                this.test = false;
                console.log('No provider match:', this.loggedProviderId, this.Ticket.originator?.providerId);
              }
            }
          }
        }
      }
    );

    // Process partners data
    const partnersNameArray: any[] = [];
    if (this.Ticket.trip_Claims) {
      for (const claim of this.Ticket.trip_Claims) {
        const providerId = claim.claimant_provider_id;
        if (providerId) {
          this._providerPartnersService.getProvider(providerId).pipe(
            takeUntil(this.destroy$)
          ).subscribe(
            response => {
              partnersNameArray.push(response);
              this.partnersName = partnersNameArray;
            }
          );
        }
      }
    }

    // Save a copy of the ticket for editing
    this.editProviderObj = Object.assign({}, this.Ticket);

    // Update URL to reflect detail view state with ticket ID
    this._router.navigate([], {
      relativeTo: this._route,
      queryParams: { view: 'detail', ticketId: ticket.id },
      queryParamsHandling: 'merge'
    });
  }

  findSelectedTicketIndex(ticket: any): number {
    return this.TicketsList.indexOf(ticket);
  }



  cancelTicket(ticket: any): void {
    this._confirmation.confirm({
      message: 'Are you sure you want to cancel this trip ticket?',
      accept: () => {
        // Store the ticket and show reason dialog
        this.ticketToCancel = ticket;
        this.cancellationReason = '';
        this.showCancellationReasonDialog = true;
      }
    });
  }

  /**
   * Submit cancellation with reason
   */
  submitCancellation(): void {
    if (!this.cancellationReason || this.cancellationReason.trim() === '') {
      this._notification.warn('Warning', 'Please enter a reason for cancellation');
      return;
    }

    if (!this.ticketToCancel) {
      this._notification.error('Error', 'No ticket selected for cancellation');
      this.showCancellationReasonDialog = false;
      return;
    }

    // Close the reason dialog
    this.showCancellationReasonDialog = false;

    // Proceed with cancellation using the entered reason
    this.isTripTicketCancel = true;

    this._tripTicketService.cancelTripTicket({
      ticketId: this.ticketToCancel.id,
      reason: this.cancellationReason.trim()
    })
      .pipe(
        takeUntil(this.destroy$),
        finalize(() => {
          this.isTripTicketCancel = false;
          this.ticketToCancel = null;
          this.cancellationReason = '';
        })
      )
      .subscribe({
        next: (_response: any) => {
          this._notification.success("Success", "Trip Ticket has been cancelled successfully");
          this.showGrid = true;
          this.displayDialog = false;
          this.getTicketsList();
        },
        error: (_error: any) => {
          this._notification.error("Error", "Failed to cancel Trip Ticket");
        }
      });
  }

  /**
   * Cancel the cancellation reason dialog
   */
  cancelCancellationDialog(): void {
    this.showCancellationReasonDialog = false;
    this.cancellationReason = '';
    this.ticketToCancel = null;
  }

  rescindTicket(ticket: any): void {
    this._confirmation.confirm({
      message: 'Are you sure you want to rescind this trip ticket?',
      accept: () => {
        this.isTripTicketCancel = true;
        this._tripTicketService.rescindTripTicket(ticket.id)
          .pipe(
            takeUntil(this.destroy$),
            finalize(() => {
              this.isTripTicketCancel = false;
            })
          )
          .subscribe({
            next: (_response: any) => {
              this._notification.success("Success", "Trip Ticket has been rescinded successfully");
              this.showGrid = true;
              this.displayDialog = false;
              this.getTicketsList();
            },
            error: (_error: any) => {
              this._notification.error("Error", "Failed to rescind Trip Ticket");
            }
          });
      }
    });
  }


  rescindTripTicket(): void {
    this.rescindTicket(this.Ticket);
  }


  public getCurrentTimeInSelectedTimezone(): string {
    const date = new Date();
    return new Intl.DateTimeFormat('en-US', {
      year: 'numeric',
      month: 'short',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      hour12: true,
      timeZone: this.selectedTimezone,
    }).format(date);
  }

  private updateCurrentTime() {
    this.currentTime = new Date();
  }


  public updateTimezone() {
    this.updateCurrentTime();
  }

  public getTimezoneAbbr(): string {
    const date = new Date();
    return date.toLocaleTimeString('en-us', { timeZoneName: 'short' }).split(' ')[2];
  }


  updateTicket(): void {
    if (this.update === false && this.isRqstTime === false) {
      this.update = true;
      this.activity = false;
      this.isComment = false;
    } else {
      this.update = false;
      this.isRqstTime = false;
    }
  }

  backToGrid(): void {
    this.showGrid = true;
    this.displayDialog = false;
    this.show = true;
    this.createClaims = false;
    this.showClaimInput = false;
    this.showEditInput = false;
    this.activity = false;
    this.update = false;
    this.fare = '';
    this.requestedFare = '';
    this.provisionalPickupTime = '';
    this.notes = '';
    this.isShowMap = false;
    this.isComment = false;

    // Clear pending detail view to prevent re-navigation
    this.pendingDetailView = null;

    // Clear URL parameters to show we're back in grid view
    this._router.navigate([], {
      relativeTo: this._route,
      queryParams: {},
      queryParamsHandling: 'replace'
    });

    // Ensure activity arrays are properly initialized
    this.initializeActivityArrays();

    if (this.directionsRenderer) {
      this.directionsRenderer.setMap(null);
    }
  }

  advanceFilter(): void {
    this.dropdown = !this.dropdown;
  }

  saveFilter(): void {
    this.savefilters = true;
  }

  saveFilterClose(): void {
    this.savefilters = false;
  }

  checkOperatingHour(_event: any, _time: string): void {
    // Implementation needed
  }

  searchByCurrentFilter(ticketFilter: TicketFilter, advancedFilter: AdvanceFilter, tripTime: TripTime, options?: { resetPage?: boolean }): void {
    // Explicitly clean ticketFilter.advancedFilterParameter array
    if (Array.isArray(ticketFilter.advancedFilterParameter)) {
      ticketFilter.advancedFilterParameter = ticketFilter.advancedFilterParameter.filter(param => {
        if (!param || typeof param !== 'object') return false;
        if (param.value === undefined || param.value === null) return false;
        if (typeof param.value === 'string' && (param.value.trim() === '' || param.value.trim().toLowerCase() === 'undefined')) return false;
        return true;
      });
    }
    // Clean arrays in ticketFilter and advancedFilter: remove elements with undefined or 'undefined' values
    const cleanArray = (arr: any[]) => Array.isArray(arr) ? arr.filter(x => {
      if (x === undefined || x === null) return false;
      if (typeof x === 'string' && (x.trim() === '' || x.trim().toLowerCase() === 'undefined')) return false;
      if (typeof x === 'object' && x !== null) {
        // If object has a 'value' property, check it
        if ('value' in x && (x.value === undefined || x.value === null || x.value === '' || (typeof x.value === 'string' && x.value.toLowerCase() === 'undefined'))) return false;
      }
      return true;
    }) : arr;

    // Clean all array properties in ticketFilter
    Object.keys(ticketFilter).forEach(key => {
      const tf = ticketFilter as any;
      if (Array.isArray(tf[key])) {
        tf[key] = cleanArray(tf[key]);
      }
    });
    Object.keys(advancedFilter).forEach(key => {
      const af = advancedFilter as any;
      if (Array.isArray(af[key])) {
        af[key] = cleanArray(af[key]);
      }
    });
    // Save the current filter state for pagination
    this.activeFilterState = {
      ticketFilter: JSON.parse(JSON.stringify(ticketFilter)),
      advancedFilter: JSON.parse(JSON.stringify(advancedFilter)),
      tripTime: JSON.parse(JSON.stringify(tripTime))
    };
    //console.log('searchByCurrentFilter called with:', ticketFilter, advancedFilter, tripTime);
    // Reset pagination when performing a new search (unless paginating an active filter)
    const resetPage = options?.resetPage !== false; // default true
    if (resetPage) {
      this.first = 0;
      this.lastLazyEventSignature = null;
      this.pendingLazyEvent = null;
      this.pendingLazyEventSignature = null;
    }
    this.loading = true;
    this.filterRequestInFlight = true;
    const filter: any = {};

    // Process ticket filter status
    if (typeof ticketFilter.ticketFilterstatus === 'undefined') {
      ticketFilter.ticketFilterstatus = [];
    }
    if (ticketFilter.ticketFilterstatus.length > 0) {
      const ticketStatusArr: string[] = [];
      for (let inx = 0; inx < ticketFilter.ticketFilterstatus.length; inx++) {
        const currentStatus = ticketFilter.ticketFilterstatus[inx];

        // Skip null or undefined values
        if (!currentStatus) {
          continue;
        }

        // Handle different data structures that could be in the array
        if (typeof currentStatus === 'string') {
          // Handle plain strings (like "1")
          const statusString = String(currentStatus); // Ensure it's treated as a string
          if (statusString.charAt(0) === ' ') {
            ticketStatusArr.push(statusString.slice(1));
          } else {
            ticketStatusArr.push(statusString);
          }
        } else if (typeof currentStatus === 'object' && currentStatus !== null) {
          // Handle object with value property (like {value: "1"})
          if (!currentStatus.value || currentStatus.value === "") {
            continue;
          }

          if (currentStatus.value.slice(0, 1) === " ") {
            ticketStatusArr.push(
              currentStatus.value.slice(1, currentStatus.value.length)
            );
          } else {
            ticketStatusArr.push(currentStatus.value);
          }
        }
      }


      filter['ticketFilterstatus'] = ticketStatusArr.length > 0 ? ticketStatusArr : null;
    }

    // Process claiming provider name
    if (typeof ticketFilter.claimingProviderName === 'undefined') {
      ticketFilter.claimingProviderName = [];
    }
    if (ticketFilter.claimingProviderName.length > 0) {
      const claimingProvArr: string[] = [];
      for (let index = 0; index < ticketFilter.claimingProviderName.length; index++) {
        const currentProvider = ticketFilter.claimingProviderName[index];

        // Skip null or undefined values
        if (!currentProvider) {
          continue;
        }

        // Handle different data structures that could be in the array
        if (typeof currentProvider === 'string') {
          const providerString = String(currentProvider);
          claimingProvArr.push(providerString);
        } else if (typeof currentProvider === 'object' && currentProvider !== null) {
          // Handle object with value property
          if (!currentProvider.value || currentProvider.value === "") {
            continue;
          }
          claimingProvArr.push(currentProvider.value);
        }
      }

      filter['claimingProviderName'] = claimingProvArr.length > 0 ? claimingProvArr : null;
    }

    if ((<HTMLInputElement>document.getElementById('Rescinded_Trip_Tickets')) == null) {
      ticketFilter.rescindedApplyStatusParameter = " ";
    }
    else {
      ticketFilter.rescindedApplyStatusParameter = (<HTMLInputElement>document.getElementById('Rescinded_Trip_Tickets')).value;
    }


    // Process originating provider name
    if (typeof ticketFilter.originatingProviderName === 'undefined') {
      ticketFilter.originatingProviderName = [];
    }
    if (ticketFilter.originatingProviderName.length > 0) {
      const originatingProvArr: string[] = [];
      for (let index = 0; index < ticketFilter.originatingProviderName.length; index++) {
        const currentProvider = ticketFilter.originatingProviderName[index];

        // Skip null or undefined values
        if (!currentProvider) {
          continue;
        }

        // Handle different data structures that could be in the array
        if (typeof currentProvider === 'string') {
          const providerString = String(currentProvider);
          originatingProvArr.push(providerString);
        } else if (typeof currentProvider === 'object' && currentProvider !== null) {
          // Handle object with value property
          if (!currentProvider.value || currentProvider.value === "") {
            continue;
          }
          originatingProvArr.push(currentProvider.value);
        }
      }

      filter['originatingProviderName'] = originatingProvArr.length > 0 ? originatingProvArr : null;
    }

    // Process hospitality service area
    if (typeof ticketFilter.hospitalityServiceArea === 'undefined') {
      ticketFilter.hospitalityServiceArea = [];
    }
    if (ticketFilter.hospitalityServiceArea.length > 0) {
      const hospitalityArr: string[] = [];
      for (let index = 0; index < ticketFilter.hospitalityServiceArea.length; index++) {
        const currentItem = ticketFilter.hospitalityServiceArea[index];

        // Skip null or undefined values
        if (!currentItem) {
          continue;
        }

        // Handle different data structures that could be in the array
        if (typeof currentItem === 'string') {
          const itemString = String(currentItem);
          hospitalityArr.push(itemString);
        } else if (typeof currentItem === 'object' && currentItem !== null) {
          // Handle object with value property
          if (!currentItem.value || currentItem.value === "") {
            continue;
          }
          hospitalityArr.push(currentItem.value);
        }
      }

      filter['hospitalityServiceArea'] = hospitalityArr.length > 0 ? hospitalityArr : null;
    }

    // Process funding source list
    if (typeof ticketFilter.fundingSourceList === 'undefined') {
      ticketFilter.fundingSourceList = [];
    }
    if (ticketFilter.fundingSourceList.length > 0) {
      const fundingArr: string[] = [];
      for (let inxFund = 0; inxFund < ticketFilter.fundingSourceList.length; inxFund++) {
        const currentFund = ticketFilter.fundingSourceList[inxFund];

        // Skip null or undefined values
        if (!currentFund) {
          continue;
        }

        // Handle different data structures that could be in the array
        if (typeof currentFund === 'string') {
          const fundString = String(currentFund);
          if (fundString.charAt(0) === ' ') {
            fundingArr.push(fundString.slice(1));
          } else {
            fundingArr.push(fundString);
          }
        } else if (typeof currentFund === 'object' && currentFund !== null) {
          // Handle object with value property
          if (!currentFund.value || currentFund.value === "") {
            continue;
          }

          if (currentFund.value.charAt(0) === ' ') {
            fundingArr.push(currentFund.value.slice(1));
          } else {
            fundingArr.push(currentFund.value);
          }
        }
      }

      filter['fundingSourceList'] = fundingArr.length > 0 ? fundingArr : null;
    }

    // Process customer eligibility
    if (typeof ticketFilter.customerEligibility === 'undefined') {
      ticketFilter.customerEligibility = [];
    }
    if (ticketFilter.customerEligibility.length > 0) {
      const eligibilityArr: string[] = [];
      for (let inxEligi = 0; inxEligi < ticketFilter.customerEligibility.length; inxEligi++) {
        const currentItem = ticketFilter.customerEligibility[inxEligi];

        // Skip null or undefined values
        if (!currentItem) {
          continue;
        }

        // Handle different data structures that could be in the array
        if (typeof currentItem === 'string') {
          const itemString = String(currentItem);
          if (itemString.charAt(0) === ' ') {
            eligibilityArr.push(itemString.slice(1));
          } else {
            eligibilityArr.push(itemString);
          }
        } else if (typeof currentItem === 'object' && currentItem !== null) {
          // Handle object with value property
          if (!currentItem.value || currentItem.value === "") {
            continue;
          }

          if (currentItem.value.charAt(0) === ' ') {
            eligibilityArr.push(currentItem.value.slice(1));
          } else {
            eligibilityArr.push(currentItem.value);
          }
        }
      }

      filter['customerEligibility'] = eligibilityArr.length > 0 ? eligibilityArr : null;
    }

    // Process geographic values
    if (typeof ticketFilter.selectedGeographicVal === 'undefined') {
      ticketFilter.selectedGeographicVal = [];
    }
    if (ticketFilter.selectedGeographicVal.length > 0) {
      const geographicArr: string[] = [];
      for (let inxGeo = 0; inxGeo < ticketFilter.selectedGeographicVal.length; inxGeo++) {
        const currentItem = ticketFilter.selectedGeographicVal[inxGeo];

        // Skip null or undefined values
        if (!currentItem) {
          continue;
        }

        // Handle different data structures that could be in the array
        if (typeof currentItem === 'string') {
          const itemString = String(currentItem);
          if (itemString.charAt(0) === ' ') {
            geographicArr.push(itemString.slice(1));
          } else {
            geographicArr.push(itemString);
          }
        } else if (typeof currentItem === 'object' && currentItem !== null) {
          // Handle object with value property
          if (!currentItem.value || currentItem.value === "") {
            continue;
          }

          if (currentItem.value.charAt(0) === ' ') {
            geographicArr.push(currentItem.value.slice(1));
          } else {
            geographicArr.push(currentItem.value);
          }
        }
      }

      filter['selectedGeographicVal'] = geographicArr.length > 0 ? geographicArr : null;
    }

    // Process pick up time range
    if (typeof ticketFilter.reqPickUpStartAndEndTime === 'undefined') {
      ticketFilter.reqPickUpStartAndEndTime = [];
    }
    if (ticketFilter.reqPickUpStartAndEndTime.length > 0) {
      const timeRangeArr: string[] = [];
      for (let j = 0, iLen = ticketFilter.reqPickUpStartAndEndTime.length; j < iLen; j++) {
        if (!ticketFilter.reqPickUpStartAndEndTime[j] || !ticketFilter.reqPickUpStartAndEndTime[j].toString()) {
          continue;
        }

        const timeRangeString = ticketFilter.reqPickUpStartAndEndTime[j].toString();
        if (timeRangeString.slice(0, 1) === " ") {
          timeRangeArr.push(timeRangeString.slice(1));
        } else {
          timeRangeArr.push(timeRangeString);
        }
      }

      filter['reqPickUpStartAndEndTime'] = timeRangeArr;
    }

    // Process trip time (pickup/dropoff dates)
    const tripTimeParams = this.buildTripTimeParameters(tripTime);
    ticketFilter.tripTime = tripTimeParams.slice();
    filter['tripTime'] = tripTimeParams.length > 0 ? tripTimeParams.map(entry => `${entry.name}=${entry.value}`) : null;


    const operatingHoursParams = this.buildOperatingHoursParameters();
    ticketFilter.operatingHours = operatingHoursParams.slice();
    filter['operatingHours'] = operatingHoursParams.length > 0 ? operatingHoursParams.map(entry => `${entry.name}=${entry.value}`) : null;

    // Process advanced filter parameters
    if (typeof ticketFilter.advancedFilterParameter === 'undefined') {
      ticketFilter.advancedFilterParameter = [];
    }
    if (ticketFilter.advancedFilterParameter.length > 0) {
      //for (let i = 0, iLen = ticketFilter.advancedFilterParameter.length; i < iLen; i++) {
      for (const param of ticketFilter.advancedFilterParameter) {

        if (!param || typeof param !== 'object' || typeof param.name !== 'string') {
          continue;
        }
        const advancedParameter = param.name.split(':');

        if (advancedParameter[0].trim() === 'customer_first_name') {
          ticketFilter.advancedFilterParameter.push({
            name: 'customerFirstName :',
            value: advancedParameter[0].trim()
          });
          this.search = true;
        } else if (advancedParameter[0].trim() === 'customer_address.addressId') {

          ticketFilter.advancedFilterParameter.push({
            name: 'customerAddress.addressId :',
            value: advancedParameter[0].trim()
          });
          this.search = true;
        } else if (advancedParameter[0].trim() === 'pickup_address.addressId') {
          ticketFilter.advancedFilterParameter.push({
            name: 'pickupAddress.addressId :',
            value: advancedParameter[0].trim()
          });
          this.search = true;
        } else if (advancedParameter[0].trim() === 'drop_off_address.addressId') {
          ticketFilter.advancedFilterParameter.push({
            name: 'dropOffAddress.addressId :',
            value: advancedParameter[0].trim()
          });
          this.search = true;
        } else if (advancedParameter[0].trim() === 'customer_identifiers') {
          ticketFilter.advancedFilterParameter.push({
            name: 'customerIdentifiers :',
            value: advancedParameter[0].trim()
          });
          this.search = true;
        } else if (advancedParameter[0].trim() === 'customer_last_name') {
          ticketFilter.advancedFilterParameter.push({
            name: 'customerLastName :',
            value: advancedParameter[0].trim()
          });
          this.search = true;
        }
      }
    } else {
      // Helper to check if a value is valid
      const isValid = (val: any) => val !== undefined && val !== null && !(typeof val === 'string' && (val.trim() === '' || val.trim().toLowerCase() === 'undefined'));

      if (isValid(advancedFilter.customer_first_name)) {
        ticketFilter.advancedFilterParameter.push({
          name: 'customerFirstName :',
          value: advancedFilter.customer_first_name
        });
      }
      if (isValid(advancedFilter.customer_last_name)) {
        ticketFilter.advancedFilterParameter.push({
          name: 'customerLastName :',
          value: advancedFilter.customer_last_name
        });
      }
      if (isValid(advancedFilter.customer_address_addressId)) {
        ticketFilter.advancedFilterParameter.push({
          name: 'customerAddress.addressId :',
          value: advancedFilter.customer_address_addressId
        });
      }
      if (isValid(advancedFilter.pickup_address_addressId)) {
        ticketFilter.advancedFilterParameter.push({
          name: 'pickupAddress.addressId :',
          value: advancedFilter.pickup_address_addressId
        });
      }
      if (isValid(advancedFilter.drop_off_address_addressId)) {
        ticketFilter.advancedFilterParameter.push({
          name: 'dropOffAddress.addressId :',
          value: advancedFilter.drop_off_address_addressId
        });
      }
      if (isValid(advancedFilter.customer_identifiers)) {
        ticketFilter.advancedFilterParameter.push({
          name: 'customerIdentifiers :',
          value: advancedFilter.customer_identifiers
        });
      }
    }
    // Convert advancedFilterParameter to string array in format 'NAME_VALUE: KEY_VALUE'
    filter['advancedFilterParameter'] = ticketFilter.advancedFilterParameter.length > 0
      ? ticketFilter.advancedFilterParameter.map(param => `${param.name}${param.value}`)
      : null;


    if (ticketFilter.schedulingPriority !== null &&
      ticketFilter.schedulingPriority !== ' ' &&
      ticketFilter.schedulingPriority !== 'Scheduling_Priority :') {
      filter['schedulingPriority'] = ticketFilter.schedulingPriority;
    }

    if (this.ticketFilter.rescindedApplyStatusParameter !== null &&
      this.ticketFilter.rescindedApplyStatusParameter !== ' ') {
      filter['rescindedApplyStatusParameter'] = this.ticketFilter.rescindedApplyStatusParameter;
    }


    if (ticketFilter.seatsRequiredMin !== null && ticketFilter.seatsRequiredMin !== 0) {
      filter['seatsRequiredMin'] = ticketFilter.seatsRequiredMin;
    }

    if (ticketFilter.seatsRequiredMax !== null && ticketFilter.seatsRequiredMax !== 0) {
      filter['seatsRequiredMax'] = ticketFilter.seatsRequiredMax;
    }




    filter['isServiceFilterApply'] = ticketFilter.isServiceFilterApply;
    let userId: any
    if (this.loggedRole == "ROLE_ADMIN") {

      userId = parseInt(this._localStorage.get('selecteProvidersUserId'))
      if (!userId) {
        userId = parseInt(this._localStorage.get('userId'))
      }
    }
    else {

      userId = parseInt(this._localStorage.get('userId'))
    }

    filter['userId'] = userId;
    filter['sortField'] = this.sortField;
    filter['sortOrder'] = this.sortOrder;
    // Include pagination parameters so filtered requests align with table state
    const currentPageNumber = this.rows > 0 ? Math.floor(this.first / this.rows) + 1 : 1;
    filter['pagenumber'] = currentPageNumber;
    filter['pagesize'] = this.rows;
    console.log('Search filter:', filter);

    this._tripTicketService.searchFilter(filter).pipe(
      takeUntil(this.destroy$),
      finalize(() => {
        this.loading = false;
        this.filterRequestInFlight = false;

        if (this.pendingLazyEvent && this.pendingLazyEventSignature && this.pendingLazyEventSignature !== this.lastLazyEventSignature) {
          const nextEvent = { ...this.pendingLazyEvent };
          const nextSignature = this.pendingLazyEventSignature;
          this.pendingLazyEvent = null;
          this.pendingLazyEventSignature = null;

          if (this.activeFilterState) {
            this.lastLazyEventSignature = nextSignature;
            this.loadTicketsLazy(nextEvent);
          }
        } else {
          this.pendingLazyEvent = null;
          this.pendingLazyEventSignature = null;
        }
      })
    ).subscribe(
      (_saveResponse: any) => {
        this.processTicketResponse(_saveResponse);

        // Note, there are pervious logic here in a previous version, but it was not clear what it was doing.
        // I assume it should follow the same logic as the basic ticket display


        this.savefilters = false;
        this._localStorage.set('isFilter', 'true');
        this.clear = true;
      },
      _error => {
        this.loading = false;
        this._notification.error('Error', 'Failed to filter tickets');
      }
    );
  }

  saveFilterName(ticketFilter: TicketFilter, advancedFilter: AdvanceFilter, tripTime: TripTime): void {
    // Process UI state controls
    this.savefilters = false;
    this.saveFilterDropdown = true;

    // Process ticket filter status
    if (typeof ticketFilter.ticketFilterstatus === 'undefined') {
      ticketFilter.ticketFilterstatus = [];
    }

    // Process claiming provider name
    if (typeof ticketFilter.claimingProviderName === 'undefined') {
      ticketFilter.claimingProviderName = [];
    }

    // Process hospitality service area
    if (typeof ticketFilter.hospitalityServiceArea === 'undefined') {
      ticketFilter.hospitalityServiceArea = [];
    }

    if (ticketFilter.claimingProviderName[0] && ticketFilter.claimingProviderName[0].value === "") {
      ticketFilter.claimingProviderName = [];
    }

    if (ticketFilter.hospitalityServiceArea[0] && ticketFilter.hospitalityServiceArea[0].value === "") {
      ticketFilter.hospitalityServiceArea = [];
    }

    if (ticketFilter.isServiceFilterApply === true || (typeof ticketFilter.isServiceFilterApply === 'string' && ticketFilter.isServiceFilterApply === "true")) {
      this.hospitalservicevalue = true;
    } else {
      this.hospitalservicevalue = false;
    }

    // Process advanced filter parameters
    if (advancedFilter.customer_first_name === null) {
      advancedFilter.customer_first_name = '';
    }

    if (advancedFilter.customer_last_name === null) {
      advancedFilter.customer_last_name = '';
    }

    if (advancedFilter.customer_address_addressId === null) {
      advancedFilter.customer_address_addressId = '';
    }

    if (advancedFilter.pickup_address_addressId === null) {
      advancedFilter.pickup_address_addressId = '';
    }

    if (advancedFilter.drop_off_address_addressId === null) {
      advancedFilter.drop_off_address_addressId = '';
    }

    if (advancedFilter.customer_identifiers === null) {
      advancedFilter.customer_identifiers = '';
    }

    const filter: any = {
      // Use the value from the input field (ngModel)
      filterName: ticketFilter.filterName,
      claimingProviderName: ticketFilter.claimingProviderName,
      originatingProviderName: ticketFilter.originatingProviderName,
      ticketFilterstatus: ticketFilter.ticketFilterstatus,
      //filterId: "",
      isActive: true,
      isServiceFilterApply: this.hospitalservicevalue,
      userId: parseInt(this._localStorage.get('userId')),
      fundingSourceList: ticketFilter.fundingSourceList,
      customerEligibility: ticketFilter.customerEligibility,
      selectedGeographicVal: ticketFilter.selectedGeographicVal,
      reqPickUpStartAndEndTime: ticketFilter.reqPickUpStartAndEndTime,
      hospitalityServiceArea: ticketFilter.hospitalityServiceArea,
      // Render advancedFilterParameter as array of strings like filter['advancedFilterParameter']
      advancedFilterParameter: ticketFilter.advancedFilterParameter && ticketFilter.advancedFilterParameter.length > 0
        ? ticketFilter.advancedFilterParameter.map(param => `${param.name}${param.value}`)
        : null,
      schedulingPriority: ticketFilter.schedulingPriority !== 'Scheduling_Priority :' ? ticketFilter.schedulingPriority : '',
      rescindedApplyStatusParameter: ticketFilter.rescindedApplyStatusParameter,
      seatsRequiredMin: ticketFilter.seatsRequiredMin !== undefined && ticketFilter.seatsRequiredMin !== null && ticketFilter.seatsRequiredMin !== 0 ? String(ticketFilter.seatsRequiredMin) : '',
      seatsRequiredMax: ticketFilter.seatsRequiredMax !== undefined && ticketFilter.seatsRequiredMax !== null && ticketFilter.seatsRequiredMax !== 0 ? String(ticketFilter.seatsRequiredMax) : '',
      tripTime: this.buildTripTimeParameters(tripTime)
    };

    //console.log('filter is :', filter);

    if (!this.filterId || this.filterId === 'null' || this.filterId === '') {
      // this is a new filter
      //console.log('Creating new filter:', filter);
      // Save the filter
      this._tripTicketService.saveFilter(filter).pipe(
        takeUntil(this.destroy$)
      ).subscribe(
        _response => {
          this._notification.success("Success", "Filter saved successfully");
          this.getFilterList();
          this.Filters = true;
          this.filterName = '';
        },
        _error => {
          this._notification.error("Error", "Failed to save filter");
        }
      );
    } else {
      // this is an existing filter to update
      filter.filterId = this.filterId;
      //console.log('Updating existing filter:', filter);
      // Update the existing filter
      this._tripTicketService.update(filter.filterId, filter).pipe(
        takeUntil(this.destroy$)
      ).subscribe(
        _response => {
          this._notification.success("Success", "Filter updated successfully");
          this.getFilterList();
          this.Filters = true;
          this.filterName = '';
        },
        _error => {
          this._notification.error("Error", "Failed to update filter");
        }
      );
    }

  }

  updateSaveFilterClose(): void {
    this.UpdateFilterBlock = false;
  }

  selectFilterByName(_ticketFilter: TicketFilter, _advancedFilter: AdvanceFilter, _tripTime: TripTime): void {
    if (!this.filterId || this.filterId === 'null' || this.filterId === '') {
      // Do not call API if filterId is not valid
      return;
    }
    this.loading = true;
    this.saveFilterDropdown = false;

    this._tripTicketService.getFilterById(this.filterId).pipe(
      takeUntil(this.destroy$),
      finalize(() => this.loading = false)
    ).subscribe(
      (_saveResponse: any) => {

        if (_saveResponse.filterName) {
          this.search = true;
          this.clear = true;
          this.selectedFilterName = _saveResponse.filterName;
          this._localStorage.set('filterName', _saveResponse.filterName);
          this._localStorage.set('filterId', _saveResponse.filterId);
          this.selectedFilter(_saveResponse.filterId);
        }

        this.checkForOutsideService(_saveResponse.isServiceFilterApply);
      },
      _error => {
        this.loading = false;
        this._notification.error("Error", "Failed to get filter");
      }
    );
  }

  checkForOutsideService(isServiceFilterApply: boolean): void {
    // Implementation for outside service check
    if (isServiceFilterApply === true) {
      this.hospitalservicevalue = true;
      this.isServiceFilter = true;
    } else {
      this.hospitalservicevalue = false;
      this.isServiceFilter = false;
    }
  }

  /**
   * Alias for checkForOutsideService to maintain compatibility with old template
   * @param isServiceFilterApply whether the service filter is applied
   */
  checkForOutSideService(isServiceFilterApply: boolean): void {
    this.checkForOutsideService(isServiceFilterApply);
  }

  selectedFilter(filterId: string): void {
    this.loading = true;
    this._localStorage.set('filterId', filterId);
    this.UpdateFilterBlock = true;

    this._tripTicketService.getFilterById(filterId).pipe(
      takeUntil(this.destroy$),
      finalize(() => this.loading = false)
    ).subscribe(
      (_saveResponseOuter: any) => {
        // Store filter data for use in other methods
        //console.log('Filter response from selectedFilter():', _saveResponseOuter);
        this.filterData = _saveResponseOuter;

        // Restore all filter state from the response
        const filter = _saveResponseOuter;
        // Defensive: ensure arrays are arrays
        // Do not include advancedFilterParameter in the spread, set it after parsing only
        const {
          advancedFilterParameter: _ignoredAdvancedFilterParameter, // ignore this
          ...filterRest
        } = filter;
        this.ticketFilter = {
          ...this.ticketFilter,
          ...filterRest,
          ticketFilterstatus: Array.isArray(filter.ticketFilterstatus) ? filter.ticketFilterstatus : [],
          claimingProviderName: Array.isArray(filter.claimingProviderName) ? filter.claimingProviderName : [],
          originatingProviderName: Array.isArray(filter.originatingProviderName) ? filter.originatingProviderName : [],
          hospitalityServiceArea: Array.isArray(filter.hospitalityServiceArea) ? filter.hospitalityServiceArea : [],
          fundingSourceList: Array.isArray(filter.fundingSourceList) ? filter.fundingSourceList : [],
          customerEligibility: Array.isArray(filter.customerEligibility) ? filter.customerEligibility : [],
          selectedGeographicVal: Array.isArray(filter.selectedGeographicVal) ? filter.selectedGeographicVal : [],
          reqPickUpStartAndEndTime: Array.isArray(filter.reqPickUpStartAndEndTime) ? filter.reqPickUpStartAndEndTime : [],
          tripTime: Array.isArray(filter.tripTime) ? filter.tripTime : [],
        };
        // Now assign the parsed advancedFilterParameter array only
        this.ticketFilter.advancedFilterParameter = [];

        // Defensive: Remove empty/blank values from arrays
        const cleanArray = (arr: any[]) => Array.isArray(arr) ? arr.filter(x => x && x !== '') : [];
        this.ticketFilter.ticketFilterstatus = cleanArray(this.ticketFilter.ticketFilterstatus);
        this.ticketFilter.claimingProviderName = cleanArray(this.ticketFilter.claimingProviderName);
        this.ticketFilter.originatingProviderName = cleanArray(this.ticketFilter.originatingProviderName);
        this.ticketFilter.hospitalityServiceArea = cleanArray(this.ticketFilter.hospitalityServiceArea);
        this.ticketFilter.fundingSourceList = cleanArray(this.ticketFilter.fundingSourceList);
        this.ticketFilter.customerEligibility = cleanArray(this.ticketFilter.customerEligibility);
        this.ticketFilter.selectedGeographicVal = cleanArray(this.ticketFilter.selectedGeographicVal);
        this.ticketFilter.reqPickUpStartAndEndTime = cleanArray(this.ticketFilter.reqPickUpStartAndEndTime);

        // Parse advancedFilterParameter array into objects and advancedFilter
        const paramArray = Array.isArray(filter.advancedFilterParameter) ? filter.advancedFilterParameter : [];

        this.ticketFilter.advancedFilterParameter = [];
        const advancedFilter: AdvanceFilter = {
          customer_first_name: '',
          customer_last_name: '',
          customer_address_addressId: '',
          pickup_address_addressId: '',
          drop_off_address_addressId: '',
          customer_identifiers: '',
          isServiceFilterApply: filter.isServiceFilterApply || false
        };
        paramArray.forEach((entry: string, _idx: number) => {
          if (typeof entry === 'string' && entry.includes(':')) {
            const [rawKey, ...rest] = entry.split(':');
            const key = rawKey.trim();
            const value = rest.join(':').trim();
            // Only add if value is not undefined, not the string 'undefined', and not empty after trim
            if (
              value !== undefined &&
              value !== '' &&
              value.toLowerCase() !== 'undefined'
            ) {

              this.ticketFilter.advancedFilterParameter.push({ name: key + ' :', value });
              // Map to advancedFilter keys
              if (key === 'customerFirstName') advancedFilter.customer_first_name = value;
              else if (key === 'customerLastName') advancedFilter.customer_last_name = value;
              else if (key === 'customerAddress.addressId') advancedFilter.customer_address_addressId = value;
              else if (key === 'pickupAddress.addressId') advancedFilter.pickup_address_addressId = value;
              else if (key === 'dropOffAddress.addressId') advancedFilter.drop_off_address_addressId = value;
              else if (key === 'customerIdentifiers') advancedFilter.customer_identifiers = value;
            }
          }
        });
        this.advancedFilter = advancedFilter;
        // TripTime: initialize with all required properties
        this.tripTime = {
          pickUpDateTime: null,
          dropOffDateTime: null,
          pickupDateFrom: null,
          pickupDateTo: null,
          customerFirstName: '',
          seatsRequiredMin: 0,
          seatsRequiredMax: 0
        };
        if (this.ticketFilter.tripTime && Array.isArray(this.ticketFilter.tripTime) && this.ticketFilter.tripTime.length > 0) {
          for (const entry of this.ticketFilter.tripTime as any[]) {
            if (typeof entry === 'string' && (entry as string).includes('=')) {
              const [name, value] = (entry as string).split('=');
              const normalizedName = name.trim().replace(/:$/, '');
              if (normalizedName === 'pickUpDateTime') {
                this.tripTime.pickUpDateTime = value ? new Date(moment(value).format('YYYY-MM-DDTHH:mm:ss')) : null;
              } else if (normalizedName === 'dropOffDateTime') {
                this.tripTime.dropOffDateTime = value ? new Date(moment(value).format('YYYY-MM-DDTHH:mm:ss')) : null;
              }
            } else if (typeof entry === 'object' && entry !== null && 'name' in entry && 'value' in entry) {
              const normalizedName = typeof entry.name === 'string' ? entry.name.trim().replace(/:$/, '') : '';
              if (normalizedName === 'pickUpDateTime') {
                this.tripTime.pickUpDateTime = entry.value ? new Date(moment(entry.value).format('YYYY-MM-DDTHH:mm:ss')) : null;
              } else if (normalizedName === 'dropOffDateTime') {
                this.tripTime.dropOffDateTime = entry.value ? new Date(moment(entry.value).format('YYYY-MM-DDTHH:mm:ss')) : null;
              }
            }
          }
        }

        // Restore other UI properties
        this.selectedFilterName = filter.filterName || '';
        this.filterId = filter.filterId || filterId;
        this._localStorage.set('filterName', this.selectedFilterName);
        this._localStorage.set('filterId', this.filterId);

        // Defensive: handle nulls for advancedFilter
        Object.keys(advancedFilter).forEach(k => { if (!advancedFilter[k]) advancedFilter[k] = ''; });

        // Defensive: handle nulls for tripTime
        if (!this.tripTime.pickUpDateTime) this.tripTime.pickUpDateTime = null;
        if (!this.tripTime.dropOffDateTime) this.tripTime.dropOffDateTime = null;

        this.ticketFilter.tripTime = this.buildTripTimeParameters(this.tripTime);

        //console.log('Restored and running ticketFilter:', this.ticketFilter);

        // Trigger a search with the restored filter
        this.searchByCurrentFilter(this.ticketFilter, this.advancedFilter, this.tripTime);
      },
      (_error: any) => {
        this.loading = false;
        this._notification.error("Error", "Failed to get filter");
      }
    );
  }

  updateSaveFilter(): void {
    this.UpdateFilters = true;
    this.Filters = false;

    for (let i = 0, iLen = this.filterList.length; i < iLen; i++) {
      if (this.filterId === this.filterList[i].filterId) {
        this.selectedFilterName = this.filterList[i].filterName;
      }
    }
  }

  updateFilter(ticketFilter: TicketFilter, selectedFilterName: string, advancedFilter: AdvanceFilter, tripTime: TripTime): void {
    if (!selectedFilterName || selectedFilterName.trim() === '') {
      this._notification.error('Error', 'Filter name is required');
      return;
    }

    // Create filter data directly in this method
    const filterData: any = {
      isActive: true,
      userId: parseInt(this._localStorage.get('userId')),
      isServiceFilterApply: ticketFilter.isServiceFilterApply || false,
      filterName: selectedFilterName,
      filterId: this.filterId,
      ticketFilterstatus: ticketFilter.ticketFilterstatus || [],
      claimingProviderName: ticketFilter.claimingProviderName || [],
      originatingProviderName: ticketFilter.originatingProviderName || [],
      hospitalityServiceArea: ticketFilter.hospitalityServiceArea || [],
      fundingSourceList: ticketFilter.fundingSourceList || [],
      customerEligibility: ticketFilter.customerEligibility || [],
      selectedGeographicVal: ticketFilter.selectedGeographicVal || [],
      reqPickUpStartAndEndTime: ticketFilter.reqPickUpStartAndEndTime || [],
      schedulingPriority: ticketFilter.schedulingPriority || '',
      rescindedApplyStatusParameter: ticketFilter.rescindedApplyStatusParameter || '',
      seatsRequiredMin: ticketFilter.seatsRequiredMin && ticketFilter.seatsRequiredMin !== 0 ? String(ticketFilter.seatsRequiredMin) : null,
      seatsRequiredMax: ticketFilter.seatsRequiredMax && ticketFilter.seatsRequiredMax !== 0 ? String(ticketFilter.seatsRequiredMax) : null,
      // Advanced filter parameters
      advancedFilterParameter: {
        customer_first_name: advancedFilter.customer_first_name || '',
        customer_last_name: advancedFilter.customer_last_name || '',
        customer_address_addressId: advancedFilter.customer_address_addressId || '',
        pickup_address_addressId: advancedFilter.pickup_address_addressId || '',
        drop_off_address_addressId: advancedFilter.drop_off_address_addressId || '',
        customer_identifiers: advancedFilter.customer_identifiers || ''
      },
      // Trip time data
      tripTime: {
        pickUpDateTime: this.formatTripTimeValue(tripTime.pickUpDateTime),
        dropOffDateTime: this.formatTripTimeValue(tripTime.dropOffDateTime)
      }
    };

    // Update filter
    this._tripTicketService.update(this.filterId, filterData).pipe(
      takeUntil(this.destroy$)
    ).subscribe(
      (_response: any) => {
        this._notification.success('Success', 'Filter updated successfully');
        this.UpdateFilters = false;
        this.getFilterList();
      },
      (_error: any) => {
        this._notification.error('Error', 'Failed to update filter');
      }
    );
  }

  /**
   * Sets trip cancellation flag to true
   */
  tripCancel(): void {
    this.isTripTicketCancel = true;
  }

  /**
   * Handle document click events to close dropdowns
   */
  onClick(event: MouseEvent): void {
    const target = event.target as HTMLElement;
    if (!target) return;

    // close legacy dropdown-content elements
    if (!target.matches('.dropbtn')) {
      const dropdowns = document.getElementsByClassName('dropdown-content');
      for (let i = 0; i < dropdowns.length; i++) {
        const openDropdown = dropdowns[i];
        if (openDropdown.classList.contains('show')) {
          openDropdown.classList.remove('show');
        }
      }
    }

    // Close the action overflow menu if clicking outside of it
    const actionOverflowEl = target.closest ? target.closest('.action-overflow') : null;
    if (!actionOverflowEl) {
      this.overflowMenuOpen = false;
    }
  }

  /**
   * Display modal dialog for adding comments
   */
  showCommentModal(): void {
    // Reset the comment form before showing dialog
    this.resetAndInitializeForm();
    // Show the dialog
    this.displayDialog = true;
  }

  /**
   * Show from date time picker
   */
  fromDateTime(): void {
    this.fromDateTimeSpan = true;
  }

  /**
   * Show to date time picker
   */
  toDateTime(): void {
    this.toDateTimeSpan = true;
  }

  /**
   * Filter customer addresses based on input
   */

  filterCustomerAddress(event: any): void {
    const addressWord = event.query;
    if (addressWord.length >= 3) {
      this._tripTicketService.getaddress(addressWord).pipe(
        takeUntil(this.destroy$)
      ).subscribe(
        (response: any) => {
          if (response) {
            this.filteredCustomersAddress = Array.isArray(response) ? response : Object.values(response);
          } else {
            this.filteredCustomersAddress = [];
          }
        },
        (_error: any) => {
          this.filteredCustomersAddress = [];
          this._notification.error("Error", "Failed to fetch addresses");
        }
      );
    } else {
      this.filteredCustomersAddress = [];
    }
  }

  filterPickupAddress(event: any): void {
    const addressWord = event.query;
    if (addressWord.length >= 3) {
      this._tripTicketService.getaddress(addressWord).pipe(
        takeUntil(this.destroy$)
      ).subscribe(
        (response: any) => {
          if (response) {
            this.filteredPickupAddresses = Array.isArray(response) ? response : Object.values(response);
          } else {
            this.filteredPickupAddresses = [];
          }
        },
        (_error: any) => {
          this.filteredPickupAddresses = [];
          this._notification.error("Error", "Failed to fetch addresses");
        }
      );
    } else {
      this.filteredPickupAddresses = [];
    }
  }

  filterDropoffAddress(event: any): void {
    const addressWord = event.query;
    if (addressWord.length >= 3) {
      this._tripTicketService.getaddress(addressWord).pipe(
        takeUntil(this.destroy$)
      ).subscribe(
        (response: any) => {
          if (response) {
            this.filteredDropoffAddresses = Array.isArray(response) ? response : Object.values(response);
          } else {
            this.filteredDropoffAddresses = [];
          }
        },
        (_error: any) => {
          this.filteredDropoffAddresses = [];
          this._notification.error("Error", "Failed to fetch addresses");
        }
      );
    } else {
      this.filteredDropoffAddresses = [];
    }
  }


  private formatTripTimeValue(input: Date | string | null | undefined): string | null {
    if (!input) {
      return null;
    }

    if (input instanceof Date) {
      return isNaN(input.getTime()) ? null : moment(input).format('YYYY-MM-DD HH:mm');
    }

    if (typeof input === 'string') {
      const trimmed = input.trim();
      if (!trimmed) {
        return null;
      }

      const supportedFormats = ['YYYY-MM-DD HH:mm', 'YYYY-MM-DDTHH:mm:ss', 'MM/DD/YYYY hh:mm A'];
      let parsed = moment(trimmed, supportedFormats, true);

      if (!parsed.isValid()) {
        const fallback = new Date(trimmed);
        parsed = isNaN(fallback.getTime()) ? moment.invalid() : moment(fallback);
      }

      return parsed.isValid() ? parsed.format('YYYY-MM-DD HH:mm') : null;
    }

    return null;
  }

  private buildTripTimeParameters(source: TripTime | null | undefined): FilterParameter[] {
    const params: FilterParameter[] = [];
    if (!source) {
      return params;
    }

    const pickup = this.formatTripTimeValue(source.pickUpDateTime);
    if (pickup) {
      params.push({ name: 'pickUpDateTime', value: pickup });
    }

    const dropOff = this.formatTripTimeValue(source.dropOffDateTime);
    if (dropOff) {
      params.push({ name: 'dropOffDateTime', value: dropOff });
    }

    return params;
  }

  public changeFromDateTime(pickUpDateTime: Date): void {
    // mirror original logic from bak component
    if (pickUpDateTime != null) {
      this.tripTime.pickUpDateTime = pickUpDateTime;
      this.tripTime.dropOffDateTime = null;
      this.fromDateEntered = true;
      this.search = true;
      this.clear = true;
    } else {
      this.tripTime.pickUpDateTime = pickUpDateTime;
      this.tripTime.dropOffDateTime = null;
      this.fromDateEntered = true;
      this.search = false;
      this.clear = true;
      //this.notificationEmitterService.error('Invalid Data', 'Enter Valid Date');
    }
    this.fromDateTimeSpan = true;
    this.ticketFilter.tripTime = this.buildTripTimeParameters(this.tripTime);
  }


  public changeToDateTime(dropOffDateTime: Date): void {
    this.tripTime.dropOffDateTime = dropOffDateTime;
    this.toDateTimeSpan = true;
    this.search =
      this.checkDateEntered(this.tripTime.pickUpDateTime, dropOffDateTime) !== 'Invalid';
    this.ticketFilter.tripTime = this.buildTripTimeParameters(this.tripTime);
  }
  private checkDateEntered(pickUpDate: Date | null, dropOffDate: Date | null): string {
    if (typeof pickUpDate !== 'undefined' || typeof dropOffDate !== 'undefined') {
      if (pickUpDate != null && dropOffDate != null) {
        if (dropOffDate.getTime() >= pickUpDate.getTime()) {
          this.fromDateEntered = false;
          this.search = true;
          this.clear = true;
          return 'Valid';
        } else {
          this.fromDateEntered = false;
          this.search = false;
          this.clear = true;
          //this.notificationEmitterService.error('Invalid Data', 'Enter Valid Date');
          return 'Invalid';
        }
      } else {
        if (!this.fromDateEntered) {
          this.search = true;
          this.clear = false;
          return 'Valid';
        } else {
          this.search = false;
          this.clear = true;
          //this.notificationEmitterService.error('Invalid Data', 'Enter Valid Date');
          return 'Invalid';
        }
      }
    }
    return 'Undefined';
  }

  /**
   * Check if all required values are entered
   */
  checkValueEntered(): void {
    // Validate form inputs
    const isValid = true; // This would typically check actual form values

    if (isValid) {
      this.search = true;
      this.clear = true;
    } else {
      this.search = false;
      this.clear = true;
    }
  }

  modifyAdvancedFilter(key: string, value: string): boolean {
    if (!Array.isArray(this.ticketFilter.advancedFilterParameter)) {
      this.ticketFilter.advancedFilterParameter = [];
    }
    // Remove any existing entry with this key
    this.ticketFilter.advancedFilterParameter = this.ticketFilter.advancedFilterParameter.filter(param => {
      return !(param && typeof param === 'object' && typeof param.name === 'string' && param.name.trim().replace(/\s*:/, '') === key);
    });

    if (key === 'customer_first_name' && value && value.trim() !== '') {
      this.advancedFilter.customer_first_name = value.trim();
      this.changeCustomerAdd = false;
      this.search = true;
      return true;
    } else if (key === 'customer_address_addressId' && value) {
      // If value is an object with addressId, use that; otherwise, use string
      if (
        value &&
        typeof value === 'object' &&
        value !== null &&
        Object.prototype.hasOwnProperty.call(value, 'addressId') &&
        typeof (value as any).addressId !== 'undefined'
      ) {
        this.advancedFilter.customer_address_addressId = (value as any).addressId;
        this.selectedCustomerAddress = value; // Store the full object for display
      } else if (typeof value === 'string') {
        this.advancedFilter.customer_address_addressId = value.trim();
        this.selectedCustomerAddress = null; // Clear the selected object
      } else {
        this.advancedFilter.customer_address_addressId = '';
        this.selectedCustomerAddress = null;
      }
      this.changeCustomerAdd = false;
      this.search = true;
      return true;
    } else if (key === 'pickup_address_addressId' && value) {
      // If value is an object with addressId, use that; otherwise, use string
      if (
        value &&
        typeof value === 'object' &&
        value !== null &&
        Object.prototype.hasOwnProperty.call(value, 'addressId') &&
        typeof (value as any).addressId !== 'undefined'
      ) {
        this.advancedFilter.pickup_address_addressId = (value as any).addressId;
        this.selectedPickupAddress = value; // Store the full object for display
      } else if (typeof value === 'string') {
        this.advancedFilter.pickup_address_addressId = value.trim();
        this.selectedPickupAddress = null; // Clear the selected object
      } else {
        this.advancedFilter.pickup_address_addressId = '';
        this.selectedPickupAddress = null;
      }
      this.changePickupAdd = false;
      this.search = true;
      return true;
    } else if (key === 'drop_off_address_addressId' && value) {
      // If value is an object with addressId, use that; otherwise, use string
      if (
        value &&
        typeof value === 'object' &&
        value !== null &&
        Object.prototype.hasOwnProperty.call(value, 'addressId') &&
        typeof (value as any).addressId !== 'undefined'
      ) {
        this.advancedFilter.drop_off_address_addressId = (value as any).addressId;
        this.selectedDropoffAddress = value; // Store the full object for display
      } else if (typeof value === 'string') {
        this.advancedFilter.drop_off_address_addressId = value.trim();
        this.selectedDropoffAddress = null; // Clear the selected object
      } else {
        this.advancedFilter.drop_off_address_addressId = '';
        this.selectedDropoffAddress = null;
      }
      this.changeDropOffAdd = false;
      this.search = true;
      return true;
    } else if (key === 'customer_identifiers' && value && value.trim() !== '') {
      this.advancedFilter.customer_identifiers = value.trim();
      this.changeCustomerIdentifier = false;
      this.search = true;
      return true;
    } else if (key === 'customer_last_name' && value && value.trim() !== '') {
      this.advancedFilter.customer_last_name = value.trim();
      this.changeCustomerName = false;
      this.search = true;
      return true;
    }
    return false;
  }


  /**
   * Handle change in customer first name
   */
  changeCustomerFirstName(customer_first_name: string, _tripTime: TripTime): void {
    if (!this.modifyAdvancedFilter('customer_first_name', customer_first_name)) {
      this.changeCustomerName = true;
      this.search = false;
    }
  }

  /**
   * Handle change in customer last name
   */
  changeCustomerLastName(customer_last_name: string, _tripTime: TripTime): void {
    if (!this.modifyAdvancedFilter('customer_last_name', customer_last_name)) {
      this.changeCustomerName = true;
      this.search = false;
    }
  }



  /**
   * Handle change in customer address
   */
  changeCustomerAddress(customer_address: any, _tripTime: TripTime): void {
    // Only process if an object with addressId was selected (not typing)
    if (customer_address && typeof customer_address === 'object' && customer_address.addressId) {
      if (!this.modifyAdvancedFilter('customer_address_addressId', customer_address)) {
        this.changeCustomerAdd = true;
        this.search = false;
      }

    }
    // If it's a string (user is typing), don't process - let autocomplete handle it
  }

  /**
   * Handle change in pickup address
   */
  changePickupAddress(pickup_address: any, _tripTime: TripTime): void {
    // Only process if an object with addressId was selected (not typing)
    if (pickup_address && typeof pickup_address === 'object' && pickup_address.addressId) {
      //console.log('🔍 DEBUG: changePickupAddress called with:', pickup_address, tripTime);
      if (!this.modifyAdvancedFilter('pickup_address_addressId', pickup_address)) {
        this.changePickupAdd = true;
        this.search = false;
      }
    }
    // If it's a string (user is typing), don't process - let autocomplete handle it
  }

  /**
   * Handle change in dropoff address
   */
  changeDropOffAddress(drop_off_address: any, _tripTime: TripTime): void {
    // Only process if an object with addressId was selected (not typing)
    if (drop_off_address && typeof drop_off_address === 'object' && drop_off_address.addressId) {
      if (!this.modifyAdvancedFilter('drop_off_address_addressId', drop_off_address)) {
        this.changeDropOffAdd = true;
        this.search = false;
      }
    }
    // If it's a string (user is typing), don't process - let autocomplete handle it
  }

  /**
   * Handle change in customer identifiers
   */
  changeCustomerIdentifiers(customer_identifiers: string, _tripTime: TripTime): void {
    if (!this.modifyAdvancedFilter('customer_identifiers', customer_identifiers)) {
      this.changeCustomerIdentifier = true;
      this.search = false;
    }
  }

  /**
   * Handle change in minimum seats required
   */
  changeSeatesRequiredMin(seatsRequiredMin: number, _tripTime: TripTime): void {
    if (seatsRequiredMin) {
      this.changeSeatesReqMin = false;
      this.search = true;

    } else {
      this.changeSeatesReqMin = true;
      this.search = false;
    }
  }

  /**
   * Handle change in maximum seats required
   */
  changeSeatesRequiredMax(seatsRequiredMax: number, _tripTime: TripTime): void {
    if (seatsRequiredMax) {
      this.changeSeatesReqMax = false;
      this.search = true;

    } else {
      this.changeSeatesReqMax = true;
      this.search = false;
    }
  }

  /**
   * Clear search filters
   */
  clearFilter(ticketFilter: TicketFilter, advancedFilter: AdvanceFilter, tripTime: TripTime): void {

    // Reset the active filter state and pagination
    this.activeFilterState = null;
    this.first = 0;

    // Reset filter properties
    ticketFilter.ticketFilterstatus = [];
    ticketFilter.claimingProviderName = [];
    ticketFilter.originatingProviderName = [];
    ticketFilter.hospitalityServiceArea = [];
    ticketFilter.fundingSourceList = [];
    ticketFilter.customerEligibility = [];
    ticketFilter.selectedGeographicVal = [];
    ticketFilter.reqPickUpStartAndEndTime = [];
    ticketFilter.tripTime = [];
    ticketFilter.advancedFilterParameter = [];
    ticketFilter.schedulingPriority = 'Scheduling_Priority :';
    ticketFilter.rescindedApplyStatusParameter = '';
    ticketFilter.seatsRequiredMin = 0;
    ticketFilter.seatsRequiredMax = 0;
    ticketFilter.isServiceFilterApply = false;

    // Reset advanced filter
    advancedFilter.customer_first_name = '';
    advancedFilter.customer_last_name = '';
    advancedFilter.customer_address_addressId = '';
    advancedFilter.pickup_address_addressId = '';
    advancedFilter.drop_off_address_addressId = '';
    advancedFilter.customer_identifiers = '';

    // Reset trip time
    tripTime.pickUpDateTime = null;
    tripTime.dropOffDateTime = null;

    // Reset autocomplete fields and their filtered results
    this.selectedCustomerAddress = null;
    this.selectedPickupAddress = null;
    this.selectedDropoffAddress = null;
    this.filteredCustomersAddress = [];
    this.filteredPickupAddresses = [];
    this.filteredDropoffAddresses = [];

    // Reset UI state flags
    this.search = false;
    this.fromDateTimeSpan = false;
    this.toDateTimeSpan = false;
    this.hospitalservicevalue = false;
    this.dateValue = true;
    this.savefilters = false;

    // Reset flags for required fields
    this.changeCustomerName = false;
    this.changeCustomerAdd = false;
    this.changePickupAdd = false;
    this.changeDropOffAdd = false;
    this.changeCustomerIdentifier = false;
    this.changeSeatesReqMin = false;
    this.changeSeatesReqMax = false;
    this.changeFromDate = false;
    this.changeToDate = false;
    this.changeToDecline = false;
    this.changeToApproved = false;

    // Reset selection state
    this.selectedTickets = [];
    this.selectAll = false;

    // Reset pagination to first page
    this.first = 0;
    this.filterId = '';
    this.selectedFilterName = '';
    this._localStorage.set('filterName', '');
    this._localStorage.set('filterId', '');

    this.filterRequestInFlight = false;
    this.pendingLazyEvent = null;
    this.pendingLazyEventSignature = null;
    this.lastLazyEventSignature = null;

    // Reset table filter state if table exists
    if (this.tripTicketTable) {
      this.tripTicketTable.reset();
    }

    this.search = false;
    // Get clean list of tickets
    this.getTicketsList();
  }

  /**
   * Add a comment to a ticket
   */
  addComment(id: any, comments: string): void {
    //console.log('🔍 DEBUG: Adding comment to ticket ID:', id, 'with text:', comments);

    if (!comments || comments.trim() === '') {
      this._notification.error('Error', 'Comment text is required');
      return;
    }

    // Check form validity
    if (!this.commentForm.valid) {
      this._notification.error('Error', 'Please fill out the comment form correctly');
      return;
    }

    let userId: any
    if (this.loggedRole == "ROLE_ADMIN") {
      userId = parseInt(this._localStorage.get('selecteProvidersUserId'))
    }
    else {
      userId = parseInt(this._localStorage.get('userId'))
    }



    // Create comment object
    const commentObj = {
      body: comments,
      user_name: ' ',
      trip_ticket_id: id,
      user_id: userId,
      name_of_provider: ' ',
      created_at: ' ',
    };

    this._tripTicketService.saveComment(id, commentObj).pipe(
      takeUntil(this.destroy$)
    ).subscribe(
      (_response: any) => {
        this._notification.success('Success', 'Comment added successfully');

        // Close the comment dialog first
        this.displayDialog = false;
        this.comments = '';
        this.commentForm.reset();



        // Refresh the comments using the dedicated getComments endpoint
        if (this.Ticket && this.Ticket.id) {


          // Fetch fresh comments data
          this._tripTicketService.getComments(this.Ticket.id).pipe(
            takeUntil(this.destroy$)
          ).subscribe({
            next: (commentsResponse: any) => {

              // Update the ticket's comment data
              if (commentsResponse && Array.isArray(commentsResponse)) {

                this.Ticket.trip_ticket_comments = commentsResponse;

                // Format the comment dates and times for display (convert UTC to America/Denver timezone)
                for (let i = 0, iLen = this.Ticket.trip_ticket_comments.length; i < iLen; i++) {
                  this.Ticket.trip_ticket_comments[i].created_at = this.formatUtcToDenver(this.Ticket.trip_ticket_comments[i].created_at);
                }
              } else {
                // If no comments returned, ensure we have an empty array
                if (!this.Ticket.trip_ticket_comments) {
                  this.Ticket.trip_ticket_comments = [];
                }
              }
            },
            error: (error: any) => {
              console.error('🔍 DEBUG: Error refreshing ticket comments from getComments:', error);
              // Fall back to full ticket refresh if comment refresh fails

              this.viewTicket(this.Ticket);
            }
          });
        }
      },
      (_saveError: any) => {
        console.error('🔍 DEBUG: Comment save error:', _saveError);
        this._notification.error('Error', 'Failed to add comment');
      }
    );
  }

  /**
   * View ticket activity history
   */
  viewActivity(ticket: any): void {
    if (!ticket || !ticket.id) {
      this._notification.error('Error', 'No ticket selected to view activity');
      return;
    }
    //console.log('🔍 DEBUG: Viewing activity for ticket ID:', ticket.id);

    window.scrollTo(0, 0);
    this.activity = true;

    // Ensure all activity arrays are properly initialized as arrays
    this.initializeActivityArrays();
    //console.log('🔍 DEBUG: Initializing activity arrays:', this.commentAdded, this.claims, this.ticketActivity);
    //console.log('🔍 DEBUG: Fetching activity for ticket ID:', ticket.id);
    this._tripTicketService.viewActivity(ticket.id).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (response: any) => {
        //console.log('🔍 DEBUG: Activity response received:', response);
        if (!response) {
          this.activity = false;
          this._notification.error('Error', 'No activity found for this ticket');
          return;
        }
        // Validate response is an array
        if (!Array.isArray(response)) {
          console.error('viewActivity: Response is not an array:', typeof response, response);
          this.activity = false;
          this._notification.error('Error', 'Invalid activity data received');
          return;
        }

        this.ticketActivity = response;
        // Re-initialize arrays after receiving response
        this.initializeActivityArrays();

        // Process each activity item to format dates and extract details
        for (let i = this.ticketActivity.length - 1; i >= 0; i--) {
          const activityItem = this.ticketActivity[i];
          if (!activityItem) continue;

          // Format date and time
          // Convert UTC timestamp to Denver timezone
          const created = activityItem.created_at;
          if (created && typeof created === 'string') {
            try {
              // Clean the date string by removing timezone identifiers in square brackets
              // Example: "2025-11-26T13:41:32-07:00[GMT-07:00]" becomes "2025-11-26T13:41:32-07:00"
              const cleanedCreated = created.replace(/\[.*?\]$/, '');

              // Parse as UTC and convert to Denver timezone
              const dateTime = moment.utc(cleanedCreated).tz('America/Denver');

              if (dateTime.isValid()) {
                // Format date as MM/DD/YY in Denver timezone
                activityItem.date = dateTime.format('MM/DD/YY');

                // Format time as HH:MM AM/PM in Denver timezone
                activityItem.time = dateTime.format('hh:mm A');
              } else {
                console.warn(`Invalid date format in activity item created_at: ${created}`);
                // Set default placeholder values
                activityItem.date = 'Unknown Date';
                activityItem.time = 'Unknown Time';
              }
            } catch (error) {
              console.error(`Error formatting date/time for activity item: ${error}`);
              activityItem.date = 'Error';
              activityItem.time = 'Error';
            }

            // Process by action type with array safety checks
            const action = activityItem.action;
            if (action === "Comment added") {
              try {
                // Split safely checking for null or undefined
                const actionDetails = activityItem.actionDetails || '';
                const body = actionDetails.includes('=') ? actionDetails.split('=') : ['comment', actionDetails];
                activityItem.comments = body.length > 1 ? body[1] : actionDetails;
                this.safeArrayPush(this.commentAdded, activityItem, 'commentAdded');
              } catch (error) {
                console.error(`Error processing comment details: ${error}`);
                activityItem.comments = 'Error parsing comment';
                this.safeArrayPush(this.commentAdded, activityItem, 'commentAdded');
              }
            }
            else if (action === "Claim created") {
              try {
                // Extract claim details more safely
                const details = activityItem.actionDetails || '';
                // Check for various delimiter formats
                let detailsArray = [];

                if (details.includes('&')) {
                  detailsArray = details.split('&');
                } else if (details.includes(',')) {
                  detailsArray = details.split(',');
                } else {
                  detailsArray = [details]; // Just one item
                }

                // Extract claim provider info
                activityItem.claimant_provider = ''; // Default empty value

                for (let j = 0; j < detailsArray.length; j++) {
                  const item = detailsArray[j];
                  if (!item) continue;

                  const keyValue = item.includes('=') ? item.split('=') : [item, ''];
                  const key = keyValue[0]?.trim();

                  if (key === "claimant_provider") {
                    activityItem.claimant_provider = keyValue[1] || '';
                  }
                }

                // If no provider found but details has a provider name pattern, try to extract it
                if (!activityItem.claimant_provider && details.includes('provider')) {
                  const providerMatch = details.match(/provider[^=]*=([^,&]+)/i);
                  if (providerMatch && providerMatch.length > 1) {
                    activityItem.claimant_provider = providerMatch[1];
                  }
                }

                this.safeArrayPush(this.claims, activityItem, 'claims');
              } catch (error) {
                console.error(`Error processing claim details: ${error}`);
                activityItem.claimant_provider = 'Error parsing provider';
                this.safeArrayPush(this.claims, activityItem, 'claims');
              }
            }
            else if (action === "Claim approved" || action === "Ticket approved") {
              activityItem.status = "Approved";
              // Extract provider info if available
              try {
                const details = activityItem.actionDetails || '';
                if (details.includes('claimant_provider=')) {
                  const providerMatch = details.match(/claimant_provider=([^,&]+)/);
                  if (providerMatch && providerMatch.length > 1) {
                    activityItem.claimant_provider = providerMatch[1];
                  }
                }
              } catch (error) {
                console.warn(`Error extracting provider from approved claim: ${error}`);
              }
              if (action === "Ticket approved") {
                this.safeArrayPush(this.ticketStatus, activityItem, 'ticketStatus');
              }
              else {
                this.safeArrayPush(this.claims, activityItem, 'claims');
              }
            }
            else if (action === "Claim declined") {
              activityItem.status = "Declined";
              // Extract provider info if available
              try {
                const details = activityItem.actionDetails || '';
                if (details.includes('claimant_provider=')) {
                  const providerMatch = details.match(/claimant_provider=([^,&]+)/);
                  if (providerMatch && providerMatch.length > 1) {
                    activityItem.claimant_provider = providerMatch[1];
                  }
                }
              } catch (error) {
                console.warn(`Error extracting provider from declined claim: ${error}`);
              }
              this.safeArrayPush(this.claims, activityItem, 'claims');
            }
            else if (action === "Claim price Mismatch") {
              activityItem.status = "Price Mismatch";
              // Extract provider info if available
              try {
                const details = activityItem.actionDetails || '';
                if (details.includes('claimant_provider=')) {
                  const providerMatch = details.match(/claimant_provider=([^,&]+)/);
                  if (providerMatch && providerMatch.length > 1) {
                    activityItem.claimant_provider = providerMatch[1];
                  }
                }
              } catch (error) {
                console.warn(`Error extracting provider from declined claim: ${error}`);
              }
              this.safeArrayPush(this.claims, activityItem, 'claims');
            }
            else if (action === "Ticket rescinded") {
              activityItem.status = "Rescinded";
              // Extract provider info if available
              try {
                const details = activityItem.actionDetails || '';
                if (details.includes('claimant_provider=')) {
                  const providerMatch = details.match(/claimant_provider=([^,&]+)/);
                  if (providerMatch && providerMatch.length > 1) {
                    activityItem.claimant_provider = providerMatch[1];
                  }
                }
              } catch (error) {
                console.warn(`Error extracting provider from declined claim: ${error}`);
              }
              this.safeArrayPush(this.ticketStatus, activityItem, 'ticketStatus');
            }
            else if (action === "Claim rescinded") {
              activityItem.status = "Rescinded";
              // Extract provider info if available
              try {
                const details = activityItem.actionDetails || '';
                if (details.includes('claimant_provider=')) {
                  const providerMatch = details.match(/claimant_provider=([^,&]+)/);
                  if (providerMatch && providerMatch.length > 1) {
                    activityItem.claimant_provider = providerMatch[1];
                  }
                }
              } catch (error) {
                console.warn(`Error extracting provider from declined claim: ${error}`);
              }
              this.safeArrayPush(this.claims, activityItem, 'claims');
            }
            else if (action === "Ticket created" || action === "Trip ticket created") {
              // Try to extract ticket creation details
              try {
                const details = activityItem.actionDetails || '';
                if (details.includes('originator_provider=')) {
                  const providerMatch = details.match(/originator_provider=([^,&]+)/);
                  if (providerMatch && providerMatch.length > 1) {
                    activityItem.originator_provider = providerMatch[1];
                  }
                }
              } catch (error) {
                console.warn(`Error extracting details from ticket creation: ${error}`);
              }
              this.safeArrayPush(this.tripTicketCreate, activityItem, 'tripTicketCreate');
            }
            else if (action === "Trip ticket cancelled" || action === "Ticket cancelled") {
              activityItem.status = "Cancelled";
              // Extract cancellation details if available
              try {
                const details = activityItem.actionDetails || '';
                if (details.includes('originator_provider=')) {
                  const providerMatch = details.match(/originator_provider=([^,&]+)/);
                  if (providerMatch && providerMatch.length > 1) {
                    activityItem.originator_provider = providerMatch[1];
                  }
                }
              } catch (error) {
                console.warn(`Error extracting cancellation details: ${error}`);
              }
              this.safeArrayPush(this.ticketStatus, activityItem, 'ticketStatus');
            }
            else if (action === "Status changed") {
              // Try to extract status info
              try {
                const details = activityItem.actionDetails || '';
                if (details.includes('status=')) {
                  const statusMatch = details.match(/status=([^,&]+)/);
                  if (statusMatch && statusMatch.length > 1) {
                    activityItem.statusValue = statusMatch[1];
                  }
                }
              } catch (error) {
                console.warn(`Error extracting status change details: ${error}`);
              }
              this.safeArrayPush(this.ticketStatus, activityItem, 'ticketStatus');
            }
            else if (action === "Ticket updated") {
              // Try to extract update info
              try {
                const details = activityItem.actionDetails || '';
                const detailsParts = details.split(',').map((part: string) => part.trim());
                activityItem.updateDetails = detailsParts;
              } catch (error) {
                console.warn(`Error extracting ticket update details: ${error}`);
              }
              this.safeArrayPush(this.ticketUpdate, activityItem, 'ticketUpdate');
            }
            else {
              // Handle any other action types
              //console.log(`Unhandled activity action type: ${action}`);
              // Put it in the general activity array
              this.safeArrayPush(this.ticketActivity, activityItem, 'ticketActivity');
            }
          } else {
            console.warn('Activity item has no created_at date:', activityItem);
          }
        }

        // Final safety check - ensure all arrays are still arrays
        this.validateActivityArrays();
        // Make sure activity items have valid date/time values
        this.ensureValidActivityDates();
      },
      error: (error: any) => {
        this.activity = false;
        // Re-initialize arrays on error to prevent template issues
        this.initializeActivityArrays();
        this._notification.error('Error', 'Failed to load activity: ' + (error.message || error));
      }
    });
  }

  /**
   * Initialize all activity arrays as empty arrays
   */
  private initializeActivityArrays(): void {
    this.commentAdded = Array.isArray(this.commentAdded) ? [] : [];
    this.claims = Array.isArray(this.claims) ? [] : [];
    this.tripTicketCreate = Array.isArray(this.tripTicketCreate) ? [] : [];
    this.ticketStatus = Array.isArray(this.ticketStatus) ? [] : [];
    this.ticketUpdate = Array.isArray(this.ticketUpdate) ? [] : [];


  }

  /**
   * Safely push an item to an array, ensuring the target is an array
   */
  private safeArrayPush(targetArray: any, item: any, arrayName: string): void {

    if (!Array.isArray(targetArray)) {
      console.error(`${arrayName} is not an array:`, typeof targetArray, targetArray);
      // Re-initialize the array based on the property name
      switch (arrayName) {

        case 'commentAdded':
          this.commentAdded = [];
          this.commentAdded.push(item);
          break;
        case 'claims':
          this.claims = [];
          this.claims.push(item);
          break;
        case 'tripTicketCreate':
          this.tripTicketCreate = [];
          this.tripTicketCreate.push(item);
          break;
        case 'ticketStatus':
          this.ticketStatus = [];
          this.ticketStatus.push(item);
          break;
        case 'ticketUpdate':
          this.ticketUpdate = [];
          this.ticketUpdate.push(item);
          break;
        default:
          console.error(`Unknown array name: ${arrayName}`);
      }
    } else {
      targetArray.push(item);
    }
  }

  /**
   * Validate that all activity arrays are actually arrays
   */
  private validateActivityArrays(): void {
    const arrays = {
      commentAdded: this.commentAdded,
      claims: this.claims,
      tripTicketCreate: this.tripTicketCreate,
      ticketStatus: this.ticketStatus,
      ticketUpdate: this.ticketUpdate
    };

    let hasInvalidArrays = false;
    for (const [name, array] of Object.entries(arrays)) {
      if (!Array.isArray(array)) {
        console.error(`${name} is not an array:`, typeof array, array);
        hasInvalidArrays = true;

        // Force re-initialization
        switch (name) {
          case 'commentAdded':
            this.commentAdded = [];
            break;
          case 'claims':
            this.claims = [];
            break;
          case 'tripTicketCreate':
            this.tripTicketCreate = [];
            break;
          case 'ticketStatus':
            this.ticketStatus = [];
            break;
          case 'ticketUpdate':
            this.ticketUpdate = [];
            break;
        }
      }
    }

    if (hasInvalidArrays) {
      console.warn('Some activity arrays were not arrays and have been re-initialized');
    }
  }

  /**
   * Ensure all activity items have valid date/time values
   * This prevents "Invalid date" from appearing in the activity timeline
   */
  private ensureValidActivityDates(): void {
    const now = moment();
    const defaultDate = now.format('MM/DD/YY');
    const defaultTime = now.format('hh:mm A');

    // Helper function to check and set valid dates
    const validateDates = (items: any[]) => {
      if (!items || !Array.isArray(items)) return;

      //for (let i = 0; i < items.length; i++) {
      for (const item of items) {
        if (!item) continue;

        // Check date format
        if (!item.date || item.date === 'Invalid date' || item.date === 'Unknown Date' || item.date === 'Error') {
          console.log('Fixing invalid date for activity item:', item);
          item.date = defaultDate;
        }

        // Check time format
        if (!item.time || item.time === 'Invalid date' || item.time === 'Unknown Time' || item.time === 'Error') {
          console.log('Fixing invalid time for activity item:', item);
          item.time = defaultTime;
        }

        // Fix any other date-related fields for claims
        if (item.proposedPickupDate === 'Invalid date') {
          item.proposedPickupDate = defaultDate;
        }

        if (item.proposedPickupTime === 'Invalid date') {
          item.proposedPickupTime = defaultTime;
        }
      }
    };

    // Check and fix dates in all activity arrays
    validateDates(this.commentAdded);
    validateDates(this.claims);
    validateDates(this.tripTicketCreate);
    validateDates(this.ticketStatus);
    validateDates(this.ticketUpdate);
    validateDates(this.ticketActivity);
  }

  /**
   * Return from activity view to ticket view
   */
  backToTicketView(): void {
    this.activity = false;
    this.showGrid = false;
    this.update = false;
    this.createClaims = false;
    this.updateClaim = false;

    // Update URL to reflect detail view (remove any activity-specific params)
    if (this.Ticket && this.Ticket.id) {
      this._router.navigate([], {
        relativeTo: this._route,
        queryParams: { view: 'detail', ticketId: this.Ticket.id },
        queryParamsHandling: 'replace'
      });
    }

    // Ensure activity arrays are reset safely
    this.initializeActivityArrays();
  }

  /**
   * Edit a claim for originator provider
   */
  EditClaimForOriginator(ticketId: any, ticketData: any, actions: string, ticket: any, claimantName: string): void {
    // Handle edit claim functionality
    this.claimAction = 'Edit Claim';
    this.showGrid = false;
    this.createClaims = true;

    // Load claim data for editing
    this._tripTicketService.getClaimForEdit(ticketId, claimantName).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (response: any) => {
        // Populate form with claim data
        this.claimId = response.id;
        this.fare = response.proposedFare;
        this.requestedFare = response.requestedFare || 0;
        this.notes = response.notes || '';

        // Handle pickup date and time
        if (response.proposedPickupTime) {
          const pickupDateTime = moment(response.proposedPickupTime);
          this.pickupDate = new Date(pickupDateTime.format('YYYY-MM-DD'));
          this.pickupTime = {
            hours: pickupDateTime.hours(),
            minutes: pickupDateTime.minutes()
          };
        }
      },
      error: (error: any) => {
        this._notification.error('Error', 'Failed to load claim for editing: ' + (error.message || error));
      }
    });
  }

  /**
   * Show modal for creating claims
   */
  showModalOfCreateClaims(ticketId: any, ticketData: any, actions: string, ticket: any, claimantName: string): void {
    this.Ticket = ticketData;
    let isUpdate = false;

    const _providerArray: any[] = [];

    if (this.loggedRole === "ROLE_PROVIDERADMIN" || this.loggedRole === "ROLE_PROVIDERUSER") {
      claimantName = this.loggedProviderId;
      this.claimFromGrid = true;
    } else {
      this.claimFromGridAsAdmin = true;
      isUpdate = true;
    }

    if (ticket.origin_provider_id === this.loggedProviderId) {
      this.eligibleForCreateClaim = 1;
      this.isTrustedPartner = 0;
      this.isOriginator = true;
    } else {
      this.isClaiment = true;
      const provider = {
        claimantProviderId: claimantName,
        tripTicketId: ticketId
      };

      this._tripTicketService.checkForServiceArea(provider).pipe(
        takeUntil(this.destroy$)
      ).subscribe({
        next: (response: any) => {
          if (response.isEligibleForService === true) {
            this.isInServiceArea = 1;
          } else {
            this.isInServiceArea = 0;
          }
          this.createClaims = true;

          const obj = {
            tripTicketId: ticketId,
            claimantProviderId: claimantName
          };

          this._tripTicketService.checkForWorkingHours(obj).pipe(
            takeUntil(this.destroy$)
          ).subscribe({
            next: (response: any) => {
              this.workingHourCheck = response;
              if (!response.isEligibleForCreateClaim) {
                this.eligibleForCreateClaim = 0;
              } else {
                this.eligibleForCreateClaim = 1;
              }
              this.isEligibleForCreateClaim = this.workingHourCheck.isEligibleForCreateClaim;
            }
          });

          this._localStorage.set('serviceAreaId', response.serviceId);
        },
        error: () => {
          //this._notification.error('Error Message', "You can not claim the ticket. No active service area is available");
        }
      });
    }

    if (isUpdate) {
      this._tripTicketService.getCostForTripTicket(ticketId, claimantName).pipe(
        takeUntil(this.destroy$)
      ).subscribe({
        next: (response: any) => {

          this.requestedFare = response.totalCostOfOrgProvider;
          this.fare = response.totalCostOfClaimantProvider;
          this.calculatedProposedFare = response.totalCostOfClaimantProvider;

          const myDiv1 = document.getElementById('fare');
          const myDiv2 = document.getElementById('rqstfare');

          this.requestedFare = parseFloat(this.requestedFare as string);
          this.fare = parseFloat(this.fare as string);

          if ((response.totalCostOfClaimantProvider === "") || (response.totalCostOfOrgProvider === "")) {
            this._notification.error('Error Message', 'Cost For Provider is not defined. Please define cost.');
            (document.getElementById('fare') as HTMLInputElement).disabled = true;
          } else {
            if ((this.requestedFare === this.fare) || (this.requestedFare > this.fare)) {
              if (myDiv1) myDiv1.style.color = 'green';
              if (myDiv2) myDiv2.style.color = 'green';
              this.isClaim = true;

              if (this.requestedFare > this.fare) {
                this.isRequestedPriceHigh = false;
                this.isSubmittedPriceHigh = true;
              }
            } else {
              if (myDiv1) myDiv1.style.color = 'red';
              if (myDiv2) myDiv2.style.color = 'red';
              this.isClaim = true;
              this.isSubmittedPriceHigh = false;
              this.isRequestedPriceHigh = true;
            }
          }
        }
      });
    }

    this._pickupDate = ticketData.expiration_date;

    this._tripTicketService.getApprovedProviderPartner(claimantName).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (response: any[]) => {
        for (let index = 0; index < response.length; index++) {
          if ((this.originatorProviderId === response[index].coordinatorProviderId) ||
            (this.originatorProviderId === response[index].requesterProviderId)) {
            if (response[index].isTrustedPartnerForCoordinator &&
              response[index].isTrustedPartnerForRequester) {
              this.isTrustedPartner = 0;
            } else {
              this.isTrustedPartner = 1;
            }
          }
        }

        if (ticket.origin_provider_id === this.loggedProviderId) {
          this.isTrustedPartner = 0;
        }
      }
    });

    this._tripTicketService.getTicketDetails(ticketId, this._localStorage.get("providerId")).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (response: any) => {
        if (actions === "update") {
          this.claimAction = 'Update Claim';
          this.createClaims = true;
          this.updateClaim = true;
          this.fromGrid = false;
          //console.log('Edit claim with ticketData:', ticketData);

          const time = (ticketData.proposed_pickup_time).split('T');


          this.requestedFare = ticketData.requester_fare;
          this.claimStatus = ticketData.status.type;
          this.fare = ticketData.proposed_fare;

          this.ackStatus = ticketData.ackStatus;
          this.notes = ticketData.notes;
          this.proposedPickupTime = time[1];
          this._localStorage.set('claimId', ticketData.id);
          this.claimId = ticketData.id;
          this.updateClaimInfo = ticketData;
          //console.log('Set Claim ID:', this.claimId);


          const myDiv1 = document.getElementById('fare');
          const myDiv2 = document.getElementById('rqstfare');

          if ((this.requestedFare === this.fare) || (this.requestedFare > this.fare)) {
            if (myDiv1) myDiv1.style.color = 'green';
            if (myDiv2) myDiv2.style.color = 'green';
            this.isClaim = true;

            if (this.requestedFare > this.fare) {
              this.isRequestedPriceHigh = false;
              this.isSubmittedPriceHigh = true;
            }
          } else {
            if (myDiv1) myDiv1.style.color = 'red';
            if (myDiv2) myDiv2.style.color = 'red';
            this.isClaim = true;
            this.isSubmittedPriceHigh = false;
            this.isRequestedPriceHigh = true;
          }

          if (ticket.origin_provider_id === this.loggedProviderId) {
            this.eligibleForCreateClaim = 1;
            this.isTrustedPartner = 0;
          } else {
            const obj = {
              tripTicketId: ticketId,
              claimantProviderId: this._localStorage.get("providerId")
            };

            this._tripTicketService.checkForWorkingHours(obj).pipe(
              takeUntil(this.destroy$)
            ).subscribe({
              next: (response: any) => {
                this.workingHourCheck = response;

                if (!response.isEligibleForCreateClaim) {
                  this.eligibleForCreateClaim = 0;
                } else {
                  this.eligibleForCreateClaim = 1;
                }
                this.isEligibleForCreateClaim = this.workingHourCheck.isEligibleForCreateClaim;
              }
            });
          }

          if (ticketData.proposed_pickup_time !== null) {
            this.createClaimPickupDate = true;
            const test = (ticketData.proposed_pickup_time).split('T');
            this.proposedPickupTime = test[1];
            this.pickupDate = new Date(test[0]);

            console.log('Proposed pickup time set to:', this.proposedPickupTime);
            console.log('Pickup date set to:', this.pickupDate);
            console.log('for ticketData:', ticketData);


            const date = test[0].split("-", 3);
            const month = date[1];
            const date1 = date[2];
            const year = date[0];
            const dropoffDate = month.concat("/", date1, "/", year);
            this.pickupDate = dropoffDate;
            this.dateForCheck = new Date(this.dropoffDate);
          } else {
            this.proposedPickupTime = "-";
            this.pickupDate = "-";
          }

          if (ticket.requested_dropoff_date !== null) {
            const temp = (ticket.requested_dropoff_date).split('T');
            const date = temp[0].split("-", 3);
            const month = date[1];
            const date1 = date[2];
            const year = date[0];
            const dropoffDate = month.concat("/", date1, "/", year);

            this.dropoffDate = dropoffDate;
            this.dropoffTime = moment(ticket._requested_dropOffDateTime).format('LT');
          } else {
            this.dropoffDate = "-";
            this.dropoffTime = "-";
          }
        } else {
          this.claimAction = 'Create Claim';
          isUpdate = true;

          if (ticket.requested_dropoff_date && ticket.requested_dropoff_date !== null) {

            const date = ticketData.requested_dropoff_date.split("-", 3);
            const month = date[1];
            const date1 = date[2];
            const year = date[0];
            const dropoffDate = month.concat("/", date1, "/", year);

            this.dropoffDate = dropoffDate;
            this.dropoffTime = moment(ticket._requested_dropOffDateTime).format('LT');
          } else {
            this.dropoffDate = "-";
            this.dropoffTime = "-";
          }

          if (ticketData._requested_pickup_date && ticketData._requested_pickup_date !== "-") {
            this.pickupDate = ticketData._requested_pickup_date;
            this.createClaimPickupDate = false;
          } else if ((ticketData._requested_pickup_date === null) || (ticketData._requested_pickup_date === "-")) {
            this.isPickupDateNull = true;
            this.createClaimPickupDate = true;
            this.pickupDate = this.dropoffDate;
            this.proposedPickupTime = null;
          } else {
            this.createClaimPickupDate = !this.createClaimPickupDate;
            this.pickupDate = this.dropoffDate;
          }

          if (response.status.type === "Approved") {
            this._notification.error('Error Message', "Sorry, Claim has been already approved to another provider.");
          } else {
            if (this.loggedRole === "ROLE_PROVIDERADMIN" || this.loggedRole === "ROLE_PROVIDERUSER") {
              this._tripTicketService.getCostForTripTicket(ticketId, this.loggedProviderId).pipe(
                takeUntil(this.destroy$)
              ).subscribe({
                next: (response: any) => {

                  this.requestedFare = response.totalCostOfOrgProvider;
                  this.fare = response.totalCostOfClaimantProvider;
                  this.calculatedProposedFare = response.totalCostOfClaimantProvider;

                  const myDiv1 = document.getElementById('fare');
                  const myDiv2 = document.getElementById('rqstfare');

                  this.requestedFare = parseFloat(this.requestedFare as string);
                  this.fare = parseFloat(this.fare as string);

                  if ((response.totalCostOfClaimantProvider === "") || (response.totalCostOfOrgProvider === "")) {
                    this._notification.error('Error Message', 'Cost For Provider is not defined. Please define cost.');
                    (document.getElementById('fare') as HTMLInputElement).disabled = true;
                  } else {
                    if ((this.requestedFare === this.fare) || (this.requestedFare > this.fare)) {
                      if (myDiv1) myDiv1.style.color = 'green';
                      if (myDiv2) myDiv2.style.color = 'green';
                      this.isClaim = true;

                      if (this.requestedFare > this.fare) {
                        this.isRequestedPriceHigh = false;
                        this.isSubmittedPriceHigh = true;
                      }
                    } else {
                      if (myDiv1) myDiv1.style.color = 'red';
                      if (myDiv2) myDiv2.style.color = 'red';
                      this.isClaim = true;
                      this.isSubmittedPriceHigh = false;
                      this.isRequestedPriceHigh = true;
                    }
                  }
                },
                error: () => {
                  (document.getElementById('fare') as HTMLInputElement).disabled = true;
                  this._notification.error('Error Message', 'Cost For Claimant Provider is not defined. Please define cost first');
                }
              });

              const provider = {
                claimantProviderId: this._localStorage.get('providerId'),
                tripTicketId: ticketId
              };

              this._tripTicketService.checkForServiceArea(provider).pipe(
                takeUntil(this.destroy$)
              ).subscribe({
                next: (response: any) => {
                  if (response.isEligibleForService === true) {
                    this.isInServiceArea = 1;
                    this.createClaims = true;
                    this._localStorage.set('serviceAreaId', response.serviceId);
                  } else {
                    this.isInServiceArea = 0;
                    this.createClaims = true;
                    this._localStorage.set('serviceAreaId', response.serviceId);
                  }
                },
                error: () => {
                  this._notification.error('Error Message', "You can not claim the ticket. No active service area is available");
                }
              });

              const obj = {
                tripTicketId: ticketId,
                claimantProviderId: this._localStorage.get("providerId")
              };

              this._tripTicketService.checkForWorkingHours(obj).pipe(
                takeUntil(this.destroy$)
              ).subscribe({
                next: (response: any) => {
                  this.workingHourCheck = response;

                  if (!response.isEligibleForCreateClaim) {
                    this.eligibleForCreateClaim = 0;
                  } else {
                    this.eligibleForCreateClaim = 1;
                  }
                  this.isEligibleForCreateClaim = this.workingHourCheck.isEligibleForCreateClaim;
                }
              });
            } else {
              this.createClaims = true;
            }
          }
        }
      }
    });
  }

  /**
   * Show modal for creating claims from the grid view
   */
  showModalOfCreateClaimsFromGrid(ticketId: any, ticketData: any, action: string): void {
    // Initialize originator and claimant status
    //console.log('showModalOfCreateClaimsFromGrid called with ticketId:', ticketId, 'and action:', action + ' for ticketData:', ticketData);
    this.Ticket = ticketData;

    this.originatorProviderId = ticketData.origin_provider_id;
    this.ticketID = ticketId;
    this.claimFromGrid = false;

    if (this.originatorProviderId == this.loggedProviderId) {
      this.isOriginator = true;
      this.isClaiment = false;
    } else {
      this.isOriginator = false;
      this.isClaiment = true;
    }

    // Store the original pickup date
    this._pickupDate = ticketData.requested_pickup_date;

    // Handle dropoff date and time
    if (ticketData.requested_dropoff_date && typeof ticketData.requested_dropoff_date === 'string') {
      const _temp = new Date(ticketData.requested_dropoff_date);
      const date = ticketData.requested_dropoff_date.split('-', 3);
      if (date.length === 3) {
        const month = date[1];
        const date1 = date[2];
        const year = date[0];
        const dropoffDate = month.concat('/', date1, '/', year);
        this.dropoffDate = dropoffDate;
      } else {
        this.dropoffDate = '-';
      }
      this.dropoffTime = ticketData._dropoffDateTime ? moment(ticketData._dropoffDateTime).format('LT') : '-';
    } else {
      this.dropoffDate = '-';
      this.dropoffTime = '-';
    }

    // Handle pickup date and time
    console.log('Create claim modal from ticketData:', ticketData);
    if ( ticketData.requested_pickup_date &&  ticketData.requested_pickup_date !== '-') {
      this.pickupDate = ticketData.requested_pickup_date;
      this.createClaimPickupDate = false;
      this.isPickupDateNull = false;
    } else if ((ticketData.requested_pickup_date == null) || (ticketData.requested_pickup_date == '-')) {
      this.isPickupDateNull = true;
      this.createClaimPickupDate = true;
      this.pickupDate = this.dropoffDate;
      this.proposedPickupTime = '';
    } else {
      this.createClaimPickupDate = !this.createClaimPickupDate;
      this.pickupDate = this.dropoffDate;
    }

    if (ticketData.requested_pickup_time ) {
      this.proposedPickupTime = (ticketData.requested_pickup_time).slice(0, 5);
      this.validPickupTime = true;
    } else {
      this.proposedPickupTime = '';
      this.validPickupTime = false;
      if ( ticketData.requested_dropoff_time ) {
        this.dropoffTime = (ticketData.requested_dropoff_time).slice(0, 5);
      }
    }

    // Store row ID and trip number
    this.rowId = ticketId;
    this._localStorage.set('Originator Trip Number', ticketData.requester_trip_id);

    // Get ticket details and handle based on action
    this._tripTicketService.getTicketDetails(ticketId, this._localStorage.get("providerId")).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (response: any) => {
        if (action === 'createClaim') {
          this.handleCreateClaimAction(response, ticketId);
        } else {
          this.handleUpdateClaimAction(response, ticketData);
        }
      },
      error: (_error: any) => {
        this._notification.error('Error', 'Failed to get ticket details');
      }
    });
  }

  /**
   * Handle create claim action
   */
  private handleCreateClaimAction(response: any, ticketId: any): void {
    this.claimAction = 'Create Claim';
    this.updateClaim = false;
    this.notes = '';
    this.fare = '';

    if (response.status?.type === 'Approved') {
      this._notification.error('Error Message', 'Sorry, you cannot claim this ticket');
      return;
    }

    if (this.loggedRole === 'ROLE_PROVIDERADMIN' || this.loggedRole === 'ROLE_PROVIDERUSER') {
      // Check service area eligibility
      const provider = {
        claimantProviderId: this._localStorage.get('providerId'),
        tripTicketId: ticketId
      };

      this._tripTicketService.checkForServiceArea(provider).pipe(
        takeUntil(this.destroy$)
      ).subscribe({
        next: (serviceResponse: any) => {
          this.isInServiceArea = serviceResponse.isEligibleForService ? 1 : 0;
          this.createClaims = true;
          this._localStorage.set('serviceAreaId', serviceResponse.serviceId);

          // Get cost information
          this.getCostForTicket(ticketId);

          // Check trusted partner status
          this.checkTrustedPartnerStatus();

          // Check working hours
          this.checkWorkingHours(ticketId);
        },
        error: (_error: any) => {
          this._notification.error('Error Message', 'You cannot claim the ticket. No active service area is available');
        }
      });
    } else {
      // Admin can create claims without eligibility checks
      this.createClaims = true;
      // Disable next button for non-provider admins
      setTimeout(() => {
        const btnNext = document.getElementById('btnNext') as HTMLButtonElement;
        if (btnNext) {
          btnNext.disabled = true;
        }
      }, 100);
    }
  }

  /**
   * Handle update claim action
   */
  private handleUpdateClaimAction(response: any, ticketData: any): void {
    this.claimAction = 'Update Claim';
    this.updateClaim = true;
    this.fromGrid = true;

    if (this.loggedRole === 'ROLE_PROVIDERADMIN' || this.loggedRole === 'ROLE_PROVIDERUSER') {
      this.createClaims = true;

      // Check trusted partner status
      this.checkTrustedPartnerStatus();

      // Find and populate existing claim data
      for (let i = 0; i < ticketData.trip_Claims.length; i++) {
        if (ticketData.trip_Claims[i].claimant_provider_id == this.loggedProviderId) {
          this.populateExistingClaimData(ticketData, ticketData.trip_Claims[i]);
          break;
        }
      }
    } else {
      this.createClaims = true;
    }
  }

  /**
   * Get cost information for the ticket
   */
  private getCostForTicket(ticketId: any): void {
    this._tripTicketService.getCostForTripTicket(ticketId, this.loggedProviderId).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (response: any) => {
        if ((response.totalCostOfClaimantProvider === '') || (response.totalCostOfOrgProvider === '')) {
          this.isCostChecked = false;
        }
        const totalCost = Number((parseFloat(response.totalCostOfOrgProvider) || 0).toFixed(2));


        this.requestedFare = totalCost;
        this.fare = totalCost;
        this.calculatedProposedFare = this.fare;

        // Ensure the inputs display two decimal places (e.g. 16.10)
        // We update the DOM value directly so template-driven inputs show the formatted string
        // without changing the numeric model used for comparisons elsewhere.
        setTimeout(() => {
          const fareInput = document.getElementById('fare') as HTMLInputElement | null;
          const rqstInput = document.getElementById('rqstfare') as HTMLInputElement | null;
          if (fareInput) {
            fareInput.value = totalCost.toFixed(2);
          }
          if (rqstInput) {
            rqstInput.value = totalCost.toFixed(2);
          }
        }, 0);

        if ((response.totalCostOfClaimantProvider === '') || (response.totalCostOfOrgProvider === '')) {
          this._notification.error('Error Message', 'Cost For Provider is not defined. Please define cost.');
          setTimeout(() => {
            const fareInput = document.getElementById('fare') as HTMLInputElement;
            if (fareInput) {
              fareInput.disabled = true;
            }
          }, 100);
        } else {
          this.updateFareColors();
        }
      },
      error: (_error: any) => {
        setTimeout(() => {
          const fareInput = document.getElementById('fare') as HTMLInputElement;
          if (fareInput) {
            fareInput.disabled = true;
          }
        }, 100);
        this._notification.error('Error Message', 'Cost For Claimant Provider is not defined. Please define cost first');
      }
    });
  }

  /**
   * Check trusted partner status
   */
  private checkTrustedPartnerStatus(): void {
    this._tripTicketService.getApprovedProviderPartner(this._localStorage.get('providerId')).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (response: any) => {
        //console.log('Trusted partner response for providerId:' + this._localStorage.get('providerId'), response);
        this.isTrustedPartner = 1; // Default to not trusted

        for (let index = 0; index < response.length; index++) {
          if ((this.originatorProviderId == response[index].coordinatorProviderId) ||
            (this.originatorProviderId == response[index].requesterProviderId)) {
            if (response[index].isTrustedPartnerForCoordinator && response[index].isTrustedPartnerForRequester) {
              this.isTrustedPartner = 0; // Trusted

            } else {
              this.isTrustedPartner = 1; // Not trusted

            }
            break;
          }
        }
      },
      error: (_error: any) => {
        this.isTrustedPartner = 1; // Default to not trusted on error
        console.error('Error getting trusted provider status ', _error)
      }
    });
  }

  /**
   * Check working hours eligibility
   */
  private checkWorkingHours(ticketId: any): void {
    const obj = {
      tripTicketId: ticketId,
      claimantProviderId: this._localStorage.get('providerId')
    };

    this._tripTicketService.checkForWorkingHours(obj).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (response: any) => {
        this.workingHourCheck = response;
        this.eligibleForCreateClaim = response.isEligibleForCreateClaim ? 1 : 0;
        this.isEligibleForCreateClaim = response.isEligibleForCreateClaim;
      },
      error: (_error: any) => {
        this.eligibleForCreateClaim = 0;
        this.isEligibleForCreateClaim = false;
      }
    });
  }

  /**
   * Populate existing claim data for update
   */
  private populateExistingClaimData(ticketData: any, claimData: any): void {
    // Handle pickup date and time for existing claim
    if ((ticketData._requested_pickup_date == null) || (ticketData._requested_pickup_date == '-')) {
      this.createClaimPickupDate = true;
      this.pickupDate = ticketData._requested_dropoff_date;
    } else {
      if (claimData.proposed_pickup_time !== null) {
        const temp = moment(claimData.proposed_pickup_time).format('L');
        const test = (claimData.proposed_pickup_time).split('T');
        this.proposedPickupTime = test[1];
        this.pickupDate = temp;
      } else {
        this.proposedPickupTime = '';
        this.pickupDate = '';
      }
    }

    // Populate claim data
    this.updateClaimInfo = claimData;
    this.fare = claimData.proposed_fare;
    this.requestedFare = claimData.requester_fare;
    this.notes = claimData.notes;
    this.claimStatus = ticketData.status.type;
    this.ackStatus = claimData.ackStatus;
    this._localStorage.set('claimId', claimData.id);

    // Update fare colors
    setTimeout(() => {
      this.updateFareColors();
    }, 100);
  }

  /**
   * Update fare input colors based on price comparison
   */
  private updateFareColors(): void {
    setTimeout(() => {
      const fareDiv = document.getElementById('fare');
      const requestedFareDiv = document.getElementById('rqstfare');

      if (fareDiv && requestedFareDiv) {
        if ((this.requestedFare == this.fare) || (this.requestedFare > this.fare)) {
          fareDiv.style.color = 'green';
          requestedFareDiv.style.color = 'green';
          this.isClaim = true;
          if (this.requestedFare > this.fare) {
            this.isRequestedPriceHigh = false;
            this.isSubmittedPriceHigh = true;
          } else {
            this.isRequestedPriceHigh = false;
            this.isSubmittedPriceHigh = false;
          }
        } else {
          fareDiv.style.color = 'red';
          requestedFareDiv.style.color = 'red';
          this.isClaim = true;
          this.isSubmittedPriceHigh = false;
          this.isRequestedPriceHigh = true;
        }
      }
    }, 100);
  }

  /**
   * Handle fare input changes
   */
  onChangeInput(event: any, fare: string): void {
    // Accept either an event object or a raw string value
    let rawValue: any = event;
    if (event && typeof event === 'object' && 'target' in event) {
      rawValue = event.target.value;
    }

    // Convert string input to number and handle NaN
    const fareValue = parseFloat(String(rawValue));

    if (isNaN(fareValue)) {
      this._notification.error('Error', 'Please enter a valid fare amount');
      return;
    }

    // Update the appropriate fare property (store as number with 2 decimals)
    const normalized = Number(fareValue.toFixed(2));
    if (fare === 'proposedFare') {
      this.fare = normalized;
    } else if (fare === 'requestedFare') {
      this.requestedFare = normalized;
    }
  }

  // Handler for formatted (display) requested fare changes coming from the template
  onFormattedRequestedFareChange(value: string): void {
    const cleaned = String(value).replace(/[^0-9.\-]/g, '');
    const parsed = parseFloat(cleaned);
    this.requestedFare = isNaN(parsed) ? 0 : Number(parsed.toFixed(2));
  }

  // Handler for formatted (display) fare changes coming from the template
  onFormattedFareChange(value: string): void {
    const cleaned = String(value).replace(/[^0-9.\-]/g, '');
    const parsed = parseFloat(cleaned);
    this.fare = isNaN(parsed) ? 0 : Number(parsed.toFixed(2));
  }
  /**
   * Cancel the claim popup

   */
  cancelClaimPop(): void {
    this.createClaims = false;
    this.showGrid = this.activity ? false : true;
    this.update = false;
    this.claimFromGridAsAdmin = false;
    this.claimFromGrid = false;

    // Clear form fields
    this.notes = '';
    this.fare = 0;
    this.requestedFare = 0;
    this.pickupDate = null;
    this.pickupTime = { hours: 0, minutes: 0 };
    this.ackStatus = false;
  }

  /**
   * Create a claim for a trip ticket
   */
  createClaim(
    ticketId: any,
    notes: string,
    fare: number,
    proposedPickupTime: any,
    pickupDate: any,
    ackStatus: boolean,
    requestedFare: number
  ): void {
    if (!pickupDate) {
      this._notification.error('Error', 'Please select a pickup date');
      return;
    }

    const timeCheck = this.parseTime(proposedPickupTime);

    if (!timeCheck || (!timeCheck.hours && timeCheck.hours !== 0)) {
      //console.log('Invalid proposed pickup time from createClaim:', proposedPickupTime);
      this._notification.error('Error', 'Please enter a valid pickup time');
      return;
    }

    // Format pickup date and time into a datetime string
    const hours = timeCheck.hours < 10 ? '0' + timeCheck.hours : timeCheck.hours.toString();
    const minutes = timeCheck.minutes < 10 ? '0' + timeCheck.minutes : timeCheck.minutes.toString();
    const dateString = moment(pickupDate).format('YYYY-MM-DD');
    const proposedPickupDateTime = `${dateString}T${hours}:${minutes}:00`;

    // Prepare claim data
    // const claimData = {
    //   ticketId: ticketId,
    //   notes: notes || '',
    //   proposedFare: fare,
    //   requestedFare: requestedFare || 0,
    //   proposedPickupTime: proposedPickupDateTime,
    //   claimantProviderId: this.loggedProviderId,
    //   ackStatus: ackStatus || false
    // };


    const claimData = {
      claimant_provider_id: parseInt(this._localStorage.get("providerId")),
      claimant_provider_name: "",
      claimant_service_id: this._localStorage.get('serviceAreaId'),
      expiration_date: proposedPickupDateTime,
      id: 0,
      ackStatus: ackStatus,
      is_expired: false,
      notes: notes,
      proposed_fare: fare,
      requester_fare: requestedFare,
      calculatedProposedFare: fare,
      proposed_pickup_time: proposedPickupDateTime,
      status: {
        description: "",
        statusId: 14,
        type: "Pending"
      },
      trip_ticket_id: ticketId,
      version: 0,
    }





    // Submit claim
    this._tripTicketService.createClaim(claimData).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (_response: any) => {
        this._notification.success('Success', 'Claim created successfully');
        this.createClaims = false;

        // Fetch updated ticket data and return to view ticket
        this._tripTicketService.getTicketDetails(claimData.trip_ticket_id, this._localStorage.get("providerId")).pipe(
          takeUntil(this.destroy$)
        ).subscribe({
          next: (updatedTicket: any) => {
            this.viewTicket(updatedTicket);
          },
          error: (error: any) => {
            this._notification.error('Error', 'Failed to refresh ticket data: ' + (error.message || error));
          }
        });
      },
      error: (error: any) => {
        this._notification.error('Error', 'Failed to create claim: ' + (error.message || error));
      }
    });
  }

  /**
   * Create claim from the grid view (for standard users)
   */
  createClaimFromGrid(
    notes: string,
    fare: number,
    proposedPickupTime: any,
    pickupDate: any,
    ackStatus: boolean,
    requestedFare: number
  ): void {
    this.createClaim(
      this.Ticket.id,
      notes,
      fare,
      proposedPickupTime,
      pickupDate,
      ackStatus,
      requestedFare
    );
  }

  /**
   * Edit claim from the grid view
   */
  editClaimFromGrid(
    notes: string,
    fare: number,
    proposedPickupTime: any,
    pickupDate: any
  ): void {
    if (!this._localStorage.get('claimId')) {
      this._notification.error('Error', 'No claim selected for editing');
      return;
    }

    if (!pickupDate) {
      this._notification.error('Error', 'Please select a pickup date');
      return;
    }

    // Format pickup date and time into a datetime string
    // const hours = proposedPickupTime.hours.toString().padStart(2, '0');
    // const minutes = proposedPickupTime.minutes.toString().padStart(2, '0');
    // const dateString = moment(pickupDate).format('YYYY-MM-DD');
    // const proposedPickupDateTime = `${dateString}T${hours}:${minutes}:00`;

    const timeCheck = this.parseTime(proposedPickupTime);

    if (!timeCheck || (!timeCheck.hours && timeCheck.hours !== 0)) {
      //console.log('Invalid proposed pickup time from editClaimFromGrid:', proposedPickupTime);
      this._notification.error('Error', 'Please enter a valid pickup time');
      return;
    }

    // Format pickup date and time into a datetime string
    const hours = timeCheck.hours < 10 ? '0' + timeCheck.hours : timeCheck.hours.toString();
    const minutes = timeCheck.minutes < 10 ? '0' + timeCheck.minutes : timeCheck.minutes.toString();
    const dateString = moment(pickupDate).format('YYYY-MM-DD');
    const proposedPickupDateTime = `${dateString}T${hours}:${minutes}:00`;


    const editData = {
      id: this._localStorage.get('claimId'),
      proposed_fare: fare,
      notes: notes || '',
      proposed_pickup_time: proposedPickupDateTime,
    }

    // Submit edit
    this._tripTicketService.editClaim(this.rowId, editData).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (_response: any) => {
        this._notification.success('Success', 'Claim updated successfully');
        this.createClaims = false;
        this.showGrid = true;

        // Refresh ticket list
        this.getTicketsList();
      },
      error: (error: any) => {
        this._notification.error('Error', 'Failed to update claim: ' + (error.message || error));
      }
    });
  }

  /**
   * Edit an existing claim
   */
  editClaim(
    notes: string,
    fare: number,
    requestedFare: number,
    proposedPickupTime: any,
    pickupDate: any
  ): void {
    if (!this.claimId && !this._localStorage.get('claimId')) {
      this._notification.error('Error', 'No claim selected for editing');
      return;
    }

    if (!pickupDate) {
      this._notification.error('Error', 'Please select a pickup date');
      return;
    }
    // console.log('editClaim called with:', {
    //   notes,
    //   fare,
    //   requestedFare,
    //   proposedPickupTime,
    //   pickupDate
    // });

    // // Format pickup date and time into a datetime string
    // const hours = proposedPickupTime.hours.toString().padStart(2, '0');
    // const minutes = proposedPickupTime.minutes.toString().padStart(2, '0');
    // const dateString = moment(pickupDate).format('YYYY-MM-DD');
    // const proposedPickupDateTime = `${dateString}T${hours}:${minutes}:00`;

    const timeCheck = this.parseTime(proposedPickupTime);

    if (!timeCheck || (!timeCheck.hours && timeCheck.hours !== 0)) {

      this._notification.error('Error', 'Please enter a valid pickup time');
      return;
    }

    // Format pickup date and time into a datetime string
    const hours = timeCheck.hours < 10 ? '0' + timeCheck.hours : timeCheck.hours.toString();
    const minutes = timeCheck.minutes < 10 ? '0' + timeCheck.minutes : timeCheck.minutes.toString();
    const dateString = moment(pickupDate).format('YYYY-MM-DD');
    const proposedPickupDateTime = `${dateString}T${hours}:${minutes}:00`;
    // Prepare edit data
    const editData = {
      id: this._localStorage.get('claimId'),
      proposed_fare: fare,
      requester_fare: requestedFare || 0,
      notes: notes || '',
      proposed_pickup_time: proposedPickupDateTime,
    }


    // Submit edit
    this._tripTicketService.editClaim(this._localStorage.get('ticketId'), editData).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (_response: any) => {
        this._notification.success('Success', 'Claim updated successfully');
        this.createClaims = false;

        // this.createClaims = false;
        // this.activity = true;

        // // Refresh the activity view
        // if (this.Ticket && this.Ticket.id) {
        //   this.viewActivity(this.Ticket);
        // }

        // Fetch updated ticket data and return to view ticket
        this._tripTicketService.getTicketDetails(this._localStorage.get('ticketId'), this._localStorage.get("providerId")).pipe(
          takeUntil(this.destroy$)
        ).subscribe({
          next: (updatedTicket: any) => {
            // console.log('Updated ticket data:', updatedTicket);
            this.viewTicket(updatedTicket);
          },
          error: (error: any) => {
            this._notification.error('Error', 'Failed to refresh ticket data: ' + (error.message || error));
          }
        });

      },
      error: (error: any) => {
        this._notification.error('Error', 'Failed to update claim: ' + (error.message || error));
      }
    });
  }

  /**
   * Change claim status to approved
   */
  changeStatusToApproved(ticketId: any, claimId: string, row: any, ticket: any): void {
    this._confirmation.confirm({
      message: 'Are you sure you want to approve this claim?',
      accept: () => {
        const approveData = {
          ticketId: ticketId,
          claimId: claimId,
          claimantProviderId: row.claimant_provider_id
        };

        this._tripTicketService.changeStatusToApproved(approveData).pipe(
          takeUntil(this.destroy$)
        ).subscribe({
          next: (_response: any) => {
            this._notification.success('Success', 'Claim has been approved successfully');

            // Update UI state
            if (this.activity) {
              // If we're in activity view, refresh activity
              this.viewActivity(ticket);
            } else {
              // Otherwise refresh ticket list
              this.getTicketsList();
            }
          },
          error: (error: any) => {
            this._notification.error('Error', 'Failed to approve claim: ' + (error.message || error));
          }
        });
      }
    });
  }

  /**
   * Change claim status to declined
   */
  changeStatusToDecline(ticketId: any, claimId: string, row: any, ticket: any): void {
    this._confirmation.confirm({
      message: 'Are you sure you want to decline this claim?',
      accept: () => {
        const declineData = {
          ticketId: ticketId,
          claimId: claimId,
          claimantProviderId: row.claimant_provider_id
        };

        this._tripTicketService.changeStatusToDecline(declineData).pipe(
          takeUntil(this.destroy$)
        ).subscribe({
          next: (_response: any) => {
            this._notification.success('Success', 'Claim has been declined successfully');

            // Update UI state
            if (this.activity) {
              // If we're in activity view, refresh activity
              this.viewActivity(ticket);
            } else {
              // Otherwise refresh ticket list
              this.getTicketsList();
            }
          },
          error: (error: any) => {
            this._notification.error('Error', 'Failed to decline claim: ' + (error.message || error));
          }
        });
      }
    });
  }

  /**
   * Change claim status to rescinded
   */
  changeStatusToRescind(ticketId: number, claimId: number, row: any, ticket: any): void {
    this._confirmation.confirm({
      message: 'Are you sure you want to rescind this claim?',
      accept: () => {

        this._tripTicketService.changeStatusToRescind(ticketId, claimId)
          .subscribe({
            next: (_response: any) => {
              this._notification.success('Success', 'Claim has been rescinded successfully');

              // Update UI state
              if (this.activity) {
                // If we're in activity view, refresh activity
                this.viewActivity(ticket);
              } else {
                // Otherwise refresh ticket list
                this.getTicketsList();
              }
            },
            error: (error: any) => {
              this._notification.error('Error', 'Failed to rescind claim: ' + (error.message || error));
            }
          });
      }
    });
  }

  /**
   * Change status to rescind from grid view
   */
  changeStatusToRescindFromGrid(ticket: any): void {

    this._confirmation.confirm({
      message: 'Are you sure you want to rescind your claim for this ticket?',
      accept: () => {
        // Find the provider's claim
        const providerId = this._localStorage.get('providerId');
        const providerClaims = ticket.trip_Claims.filter(
          (claim: any) => claim.claimant_provider_id === providerId && claim.status.type !== 'Rescinded'
        );

        if (providerClaims.length > 0) {
          // Rescind the provider's claim
          this._tripTicketService.changeStatusToRescind(ticket.id, providerClaims[0].id).subscribe(
            () => {
              this._notification.success(
                'Success Message',
                'Claim rescinded successfully'
              );
              this.getTicketsList();
            },
            _error => {
              this._notification.error('Error Message', 'Failed to rescind claim');
            }
          );
        } else {
          this._notification.error('Error Message', 'No claim found for this provider to rescind');
        }
      },
    });
  }

  /**
   * Change status to approved from grid view
   */
  changeStatusToApprovedFromGrid(row: any): void {
    if (!row || !row.claimId) {
      this._notification.error('Error', 'Invalid claim data');
      return;
    }

    this._confirmation.confirm({
      message: 'Are you sure you want to approve this claim?',
      accept: () => {
        const approveData = {
          ticketId: row.ticketId,
          claimId: row.claimId,
          claimantProviderId: row.claimant_provider_id
        };

        this._tripTicketService.changeStatusToApproved(approveData).pipe(
          takeUntil(this.destroy$)
        ).subscribe({
          next: (_response: any) => {
            this._notification.success('Success', 'Claim has been approved successfully');
            this.getTicketsList(); // Refresh the list
          },
          error: (error: any) => {
            this._notification.error('Error', 'Failed to approve claim: ' + (error.message || error));
          }
        });
      }
    });
  }

  /**
   * Change status to decline from grid view
   */
  changeStatusToDeclineFromGrid(row: any): void {
    if (!row || !row.claimId) {
      this._notification.error('Error', 'Invalid claim data');
      return;
    }

    this._confirmation.confirm({
      message: 'Are you sure you want to decline this claim?',
      accept: () => {
        const declineData = {
          ticketId: row.ticketId,
          claimId: row.claimId,
          claimantProviderId: row.claimant_provider_id
        };

        this._tripTicketService.changeStatusToDecline(declineData).pipe(
          takeUntil(this.destroy$)
        ).subscribe({
          next: (_response: any) => {
            this._notification.success('Success', 'Claim has been declined successfully');
            this.getTicketsList(); // Refresh the list
          },
          error: (error: any) => {
            this._notification.error('Error', 'Failed to decline claim: ' + (error.message || error));
          }
        });
      }
    });
  }

  /**
   * Approve claim from grid by provider name
   */
  ApprovedFromGrid(claimantNameToApprove: string): void {
    if (!this.Ticket || !this.Ticket.id || !claimantNameToApprove) {
      this._notification.error('Error', 'Missing claim information');
      return;
    }

    this._confirmation.confirm({
      message: `Are you sure you want to approve ${claimantNameToApprove}'s claim?`,
      accept: () => {
        // Get claim ID from the claimant name
        this.getClaimList(claimantNameToApprove, this.Ticket);
      }
    });
  }

  /**
   * Decline claim from grid by provider name
   */
  declineFromGrid(claimantNameToDecline: string): void {
    if (!this.Ticket || !this.Ticket.id || !claimantNameToDecline) {
      this._notification.error('Error', 'Missing claim information');
      return;
    }

    this._confirmation.confirm({
      message: `Are you sure you want to decline ${claimantNameToDecline}'s claim?`,
      accept: () => {
        // Find claim by claimant name and decline it
        this._tripTicketService.getClaimByProviderName(this.Ticket.id, claimantNameToDecline).pipe(
          takeUntil(this.destroy$)
        ).subscribe({
          next: (response: any) => {
            if (response && response.claimId) {
              const declineData = {
                ticketId: this.Ticket.id,
                claimId: response.claimId,
                claimantProviderId: response.claimant_provider_id
              };

              this._tripTicketService.changeStatusToDecline(declineData).pipe(
                takeUntil(this.destroy$)
              ).subscribe({
                next: (_declineResponse: any) => {
                  this._notification.success('Success', 'Claim has been declined successfully');
                  this.viewActivity(this.Ticket); // Refresh activity view
                },
                error: (error: any) => {
                  this._notification.error('Error', 'Failed to decline claim: ' + (error.message || error));
                }
              });
            } else {
              this._notification.error('Error', 'Could not find claim for this provider');
            }
          },
          error: (error: any) => {
            this._notification.error('Error', 'Failed to get claim: ' + (error.message || error));
          }
        });
      }
    });
  }


  /**
   * Opens the upload dialog
   */
  public openUploadDialog(): void {
    this._uploadService.resetUploadState();
    this.showUploadDialog = true;
  }

  /**
    * Reset the upload state
    */
  private resetUploadState(): void {
    this._uploadService.resetUploadState();
    // Sync component state with service state
    const state = this._uploadService.getState();
    this.selectedFile = state.selectedFile;
    this.uploadProgress = state.uploadProgress;
    this.uploadError = state.uploadError;
    this.isUploading = state.isUploading;
    this.uploadSuccess = state.uploadSuccess;
    this.uploadResult = state.uploadResult;
    this.ticketCollection = state.ticketCollection;
    this.isDragging = state.isDragging;
    this.errorMessage = state.errorMessage;
  }

  /**
   * Handles file selection from the file input
   * @param event The file input change event
   */
  public async onFileSelected(event: Event): Promise<void> {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) {
      return;
    }

    const file = input.files[0];

    // Use the upload service to process the file
    const result = await this._uploadService.processFileSelection(
      file,
      this.selectedOriginatingProvider,
      this.loggedProviderId
    );

    // Update component state from service state
    this.syncWithUploadService();

    if (!result.success) {
      // Handle invalid template case
      if (result.error && result.error.includes('does not match the expected template')) {
        this._confirmation.confirm({
          header: 'Invalid File Format',
          message: 'The selected file does not match the required template. Would you like to download the latest template?',
          acceptLabel: 'Download Template',
          rejectLabel: 'Close',
          accept: () => this.downloadTemplate()
        });
      }

      this.clearSelectedFile(input);
    }
  }

  private clearSelectedFile(input: HTMLInputElement): void {
    input.value = '';
    this._uploadService.clearSelectedFile();
    this.syncWithUploadService();
  }

  /**
   * Sync component state with upload service state
   */
  private syncWithUploadService(): void {
    const state = this._uploadService.getState();
    this.selectedFile = state.selectedFile;
    this.uploadProgress = state.uploadProgress;
    this.uploadError = state.uploadError;
    this.isUploading = state.isUploading;
    this.uploadSuccess = state.uploadSuccess;
    this.uploadResult = state.uploadResult;
    this.ticketCollection = state.ticketCollection;
    this.isDragging = state.isDragging;
    this.errorMessage = state.errorMessage;
  }





  /**
   * Uploads the selected file to the server
   */
  public uploadFile(): void {
    const state = this._uploadService.getState();

    if (!state.selectedFile || !state.ticketCollection || state.ticketCollection.length === 0) {
      this._uploadService.setUploadError('Please select a valid file to upload');
      this.syncWithUploadService();
      return;
    }

    this._uploadService.setUploading(true);
    this._uploadService.setUploadProgress(0);
    this._uploadService.setUploadError(null);
    this.syncWithUploadService();

    // Create a simulated progress indicator
    const progressInterval = setInterval(() => {
      const currentState = this._uploadService.getState();
      if (currentState.uploadProgress < 90) {
        this._uploadService.setUploadProgress(currentState.uploadProgress + 10);
        this.syncWithUploadService();
      }
    }, 300);

    this._tripTicketService.uploadTickets(state.ticketCollection).subscribe({
      next: response => {
        clearInterval(progressInterval);
        this._uploadService.setUploadProgress(100);
        this._uploadService.setUploading(false);
        this._uploadService.setUploadSuccess(true, response);
        this.syncWithUploadService();

        // Refresh the ticket list after successful upload
        this.getTicketsList();

        // Show success message
        const successMessage = response.message || 'Trip tickets uploaded successfully';
        this._notification.success('Upload Successful', successMessage);

        // Close dialog after a delay to show 100% progress
        setTimeout(() => {
          this.showUploadDialog = false;
        }, 1500);
      },
      error: error => {
        clearInterval(progressInterval);
        this._uploadService.setUploading(false);
        this._uploadService.setUploadSuccess(false);
        this._uploadService.setUploadProgress(0);

        // Handle error message
        let errorMessage: string;
        if (error.error && error.error.message) {
          errorMessage = error.error.message;
        } else if (typeof error === 'string') {
          errorMessage = error;
        } else {
          errorMessage = 'Failed to upload file. Please try again.';
        }

        this._uploadService.setUploadError(errorMessage);
        this.syncWithUploadService();
        this._notification.error('Upload Failed', errorMessage);
      },
    });
  }

  /**
   * Cancels the file upload and closes the dialog
   */
  public cancelUpload(): void {
    this.showUploadDialog = false;
    this._uploadService.resetUploadState();
    this.syncWithUploadService();
  }


  /**
   * Get claims for the current ticket
   */
  getClaims(): void {
    if (!this.Ticket || !this.Ticket.id) {
      this._notification.error('Error', 'No ticket selected');
      return;
    }

    this._tripTicketService.getClaims(this.Ticket.id).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (response: any) => {
        this.claimsArray = [];

        // Filter claims based on user permissions
        if (response && response.length) {
          for (let i = 0; i < response.length; i++) {
            if (this.loggedProviderId === response[i].claimant_provider_id ||
              this.loggedProviderId === this.Ticket.originator.providerId) {
              this.claimsArray.push(response[i]);
            }
          }
        }

        // Update ticket claims data
        this.Ticket.trip_Claims = this.claimsArray;

        // Format dates and times
        for (let i = 0; i < this.Ticket.trip_Claims?.length; i++) {
          if (this.Ticket.trip_Claims[i].proposed_pickup_time) {
            const dateTime = new Date(this.Ticket.trip_Claims[i].proposed_pickup_time);

            // Format date as MM/DD/YYYY
            this.Ticket.trip_Claims[i].formattedDate = moment(dateTime).format('MM/DD/YYYY');

            // Format time as HH:MM AM/PM
            this.Ticket.trip_Claims[i].formattedTime = moment(dateTime).format('hh:mm A');

            // Get provider name for each claim
            this.loadProviderName(i);
          }
        }
      },
      error: (error: any) => {
        this._notification.error('Error', 'Failed to load claims: ' + (error.message || error));
      }
    });
  }

  /**
   * Load provider name for a claim
   */
  private loadProviderName(index: number): void {
    if (this.Ticket.trip_Claims[index]?.claimant_provider_id) {
      this._providerPartnersService.getProvider(this.Ticket.trip_Claims[index].claimant_provider_id).pipe(
        takeUntil(this.destroy$)
      ).subscribe({
        next: (response: any) => {
          if (response && response.name) {
            this.Ticket.trip_Claims[index].claimant_provider_name = response.name;
          }
        },
        error: (_error: any) => {
          // Silent error - name just won't be shown
        }
      });
    }
  }

  /**
   * Show tickets claimed by the current user
   */
  showMyTickets(): void {

    this.loading = true;
    // Reset pagination when switching to "my tickets" view
    this.first = 0;

    // Get tickets for the current provider
    this._tripTicketService.get(this.loggedProviderId, this.loggedRole).pipe(
      //this._tripTicketService.getMyTickets(this.loggedProviderId, this.first, this.rows, this.sortField, this.sortOrder).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (response: any) => {
        this.processTicketResponse(response);
        this._notification.info('Info', 'Showing my claimed tickets');
        this.loading = false;
      },
      error: (error: any) => {
        this.loading = false;
        this._notification.error('Error', 'Failed to load your tickets: ' + (error.message || error));
      }
    });
  }

  /**
   * Show all tickets (accessible to current user)
   */
  showAllTickets(): void {

    // console.log('showAllTickets called, getting all tickets...');
    // Reset pagination when switching back to all tickets view
    this.first = 0;
    // Use existing method to get all tickets
    this.getTicketsList();
  }


  /**
   * Handle provider selection for approving claims
   * @param claimantNameToApprove Provider name to approve
   * @param ticket The ticket object
   */
  providerSelected(claimantNameToApprove: string, ticket: any): void {
    console.log('providerSelected called with:', claimantNameToApprove, ticket);
    if (!ticket || !ticket.id || !claimantNameToApprove) {
      this._notification.error('Error', 'Missing claim information');
      return;
    }
    /*

    this._confirmation.confirm({
      message: `Are you sure you want to approve ${claimantNameToApprove}'s claim?`,
      accept: () => {
        // Get claim details and approve it
        this.getClaimList(claimantNameToApprove, ticket);
      }
    });
    */
    console.log('Validating provider selection for claim approval with provider:', claimantNameToApprove);
    if (claimantNameToApprove == "") {
      console.log('No claimant provider selected');
      this._notification.error('Error', 'No claimant provider selected');
      this.claimForProvider = false;
    }
    else if (ticket.originator.providerId == claimantNameToApprove) {
      console.log('Originator and claimant provider are the same:', ticket.originator.providerId);
      this._notification.error('Error', 'Origin Requestor and Claimant Provider cannot be the same');
      this.claimForProvider = false;
    }
    else {
      console.log('Proceeding to approve claim for provider:', claimantNameToApprove);
      this.providerNameForCreateClaimsForAdmin = claimantNameToApprove;
      this.claimForProvider = true;
      this.claimantName = claimantNameToApprove;
      //this.getClaimList(claimantNameToApprove, ticket);
    }
  }

  /**
   * Get claim list by provider name and take action
   * @param claimantNameToApprove The claimant provider name
   * @param ticket The ticket object
   */
  getClaimList(claimantNameToApprove: string, ticket: any): void {
    if (!ticket || !ticket.id || !claimantNameToApprove) {
      this._notification.error('Error', 'Missing claim information');
      return;
    }
    console.log('Fetching claim for provider:', claimantNameToApprove, 'on ticket ID:', ticket.id);
    this._tripTicketService.getClaimByProviderName(ticket.id, claimantNameToApprove).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (response: any) => {
        console.log('Claim fetch response:', response);
        if (response && response.claimId) {
          const approveData = {
            ticketId: ticket.id,
            claimId: response.claimId,
            claimantProviderId: response.claimant_provider_id
          };
          console.log('Approving claim with data:', approveData);
          this._tripTicketService.changeStatusToApproved(approveData).pipe(
            takeUntil(this.destroy$)
          ).subscribe({
            next: (_approveResponse: any) => {
              console.log('Claim approved successfully for provider:', claimantNameToApprove);
              this._notification.success('Success', 'Claim has been approved successfully');
              this.viewActivity(ticket); // Refresh activity view
            },
            error: (error: any) => {
              this._notification.error('Error', 'Failed to approve claim: ' + (error.message || error));
            }
          });
        } else {
          console.log('No claim found for provider:', claimantNameToApprove);
          //this._notification.error('Error', 'Could not find claim for this provider');
        }
      },
      error: (error: any) => {
        this._notification.error('Error', 'Failed to get claim: ' + (error.message || error));
      }
    });
  }

  /**
  * Converts a "HH:mm" string or object to { hours, minutes }.
  */
  private parseTime(time: any): { hours: number, minutes: number } | null {
    if (typeof time === 'object' && time !== null && 'hours' in time && 'minutes' in time) {
      return time;
    }
    if (typeof time === 'string' && /^\d{2}:\d{2}$/.test(time)) {
      const [hours, minutes] = time.split(':').map(Number);
      if (!isNaN(hours) && !isNaN(minutes)) {
        return { hours, minutes };
      }
    }
    return null;
  }


  /**
   * Create claim as admin
   */
  createClaimAsAdmin(
    ticketId: any,
    notes: string,
    fare: number,
    proposedPickupTime: any,
    pickupDate: any,
    ackStatus: boolean,
    requestedFare: number
  ): void {
    if (!this.providerNameForCreateClaimsForAdmin) {
      this._notification.error('Error', 'Please select a provider');
      return;
    }

    if (!pickupDate) {
      this._notification.error('Error', 'Please select a pickup date');
      return;
    }



    const timeCheck = this.parseTime(proposedPickupTime);

    if (!timeCheck || (!timeCheck.hours && timeCheck.hours !== 0)) {
      // console.log('Invalid proposed pickup time from ClaimAsAdmin:', proposedPickupTime);
      this._notification.error('Error', 'Please enter a valid pickup time');
      return;
    }

    // Format pickup date and time into a datetime string
    const hours = timeCheck.hours < 10 ? '0' + timeCheck.hours : timeCheck.hours.toString();
    const minutes = timeCheck.minutes < 10 ? '0' + timeCheck.minutes : timeCheck.minutes.toString();
    const dateString = moment(pickupDate).format('YYYY-MM-DD');
    const proposedPickupDateTime = `${dateString}T${hours}:${minutes}:00`;



    // // Prepare claim data
    // const claimData = {
    //   ticketId: ticketId,
    //   notes: notes || '',
    //   proposedFare: fare,
    //   requestedFare: requestedFare || 0,
    //   proposedPickupTime: proposedPickupDateTime,
    //   claimantProviderId: this.providerNameForCreateClaimsForAdmin,
    //   ackStatus: ackStatus || false
    // };



    const claimData = {

      claimant_provider_id: this.providerNameForCreateClaimsForAdmin,
      claimant_provider_name: "",
      claimant_service_id: this._localStorage.get('serviceAreaId'),
      expiration_date: proposedPickupDateTime,
      id: 0,
      ackStatus: ackStatus,
      is_expired: false,
      notes: notes,
      proposed_fare: fare,
      requester_fare: requestedFare,
      calculatedProposedFare: fare,
      proposed_pickup_time: proposedPickupDateTime,
      status: {
        description: "",
        statusId: 14,
        type: "Pending"
      },
      trip_ticket_id: ticketId,
      version: 0,
    }







    // Submit claim
    this._tripTicketService.createClaim(claimData).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (_response: any) => {
        this._notification.success('Success', 'Claim created successfully');
        this.createClaims = false;
        this.showGrid = true;
        this.claimFromGridAsAdmin = false;

        // Refresh ticket list
        this.getTicketsList();
      },
      error: (error: any) => {
        this._notification.error('Error', 'Failed to create claim: ' + (error.message || error));
      }
    });
  }

  /**
   * Handle ticket filter status change
   * @param event The change event from the multiselect component
   */
  ticketFilterStatusChange(_event: any): void {
    this.clear = true;
    this.search = true;

    if (this.tripTime && this.tripTime.pickUpDateTime && this.tripTime.dropOffDateTime) {
      const status = this.checkDateEntered(this.tripTime.pickUpDateTime, this.tripTime.dropOffDateTime);

      if (status === 'Valid') {
        this.search = true;
      } else {
        this.search = false;
      }
    }
  }

  /**
   * Handle fund selection check
   */
  onCheckFund(fundName: string): void {
    if (!fundName) return;

    // Check if "Other" fund is selected
    if (fundName.toLowerCase() === 'other') {
      this.isFundCheck = true;
    } else {
      this.isFundCheck = false;
      this.fundNameTemp = '';
    }
  }

  /**
   * Handle description changes for fund sources
   */
  onDescription(): void {
    this.arrayOtherFund = [];
    this.iserror = "Enter Funding Source";

    if (this.fundSourceName) {
      this.fundSourceName = this.fundSourceName.trim();

      if (this.fundSourceName === "") {
        this.iserror = "Enter Funding Source";
        this.fundSourceName = ''; // Empty string instead of null
      } else {
        this.iserror = null;
        this.isUpdateFund = true;
      }
    }

    // Copy and process array
    if (this.arrayFund && this.arrayFund.length) {
      this.arrayOtherFund = [...this.arrayFund];

      // Update the fund source array
      if (this.indexFund !== undefined && this.indexFund >= 0) {
        this.arrayOtherFund.splice(this.indexFund, 1);
      }

      if (this.fundSourceName) {
        this.arrayOtherFund.push(this.fundSourceName);
      }
    }
  }

  /**
   * Check fund validation flag
   */
  checkFund(): void {
    this.isFunds = true;
  }

  /**
   * Enable note editing
   * @param _trip_notes The current trip notes (unused but kept for method signature)
   */
  editNote(_trip_notes: string): void {
    this.isNote = true;

    // Use setTimeout to ensure DOM is ready
    setTimeout(() => {
      if (this.tripNoteInput && this.tripNoteInput.nativeElement) {
        this.tripNoteInput.nativeElement.focus();
      }
    }, 0);
  }

  /**
   * Handle claiming provider name changes in filter
   * @param event The change event from the multiselect component
   */
  claimingProviderNameChange(_event: any): void {
    this.clear = true;

    if (!this.ticketFilter?.claimingProviderName || this.ticketFilter.claimingProviderName.length === 0) {
      this.search = false;
    } else {
      if (this.tripTime) {
        const status = this.checkDateEntered(this.tripTime.pickUpDateTime, this.tripTime.dropOffDateTime);
        if (status === 'Valid' || status === 'Undefined') {
          this.search = true;
        } else {
          this.search = false;
        }
      } else {
        this.search = true;
      }
    }
  }

  /**
   * Handle originating provider name changes
   * @param event The change event from the multiselect component
   */
  coriginatingProviderNameChange(_event: any): void {
    this.clear = true;

    if (!this.ticketFilter?.originatingProviderName || this.ticketFilter.originatingProviderName.length === 0) {
      this.search = false;
    } else {
      if (this.tripTime) {
        const status = this.checkDateEntered(this.tripTime.pickUpDateTime, this.tripTime.dropOffDateTime);
        if (status === 'Valid' || status === 'Undefined') {
          this.search = true;
        } else {
          this.search = false;
        }
      } else {
        this.search = true;
      }
    }
  }

  /**
   * Handle funding source filter changes - adapted for Angular 18
   * @param fundingSourceFilter The selected funding source filter
   */
  onCheckFundFilter(fundingSourceFilter: any): void {
    this.clear = true;
    this.search = true;

    if (this.ticketFilter) {
      this.ticketFilter.fundingSourceList = fundingSourceFilter;
    }
  }

  /**
   * Handle hospital service filter changes
   * @param hospitalService The selected hospital service
   */
  onCheckHospitalServiceFilter(hospitalService: any): void {
    this.clear = true;
    this.search = true;

    if (this.ticketFilter) {
      this.ticketFilter.hospitalityServiceArea = hospitalService;
    }
  }

  /**
   * Handle eligibility filter changes
   * @param eligibilityFilter The selected eligibility filter
   */
  onCheckEligibilityFilter(eligibilityFilter: any): void {
    this.clear = true;
    this.search = true;

    if (this.ticketFilter) {
      this.ticketFilter.customerEligibility = eligibilityFilter;
    }
  }

  /**
   * Handle geographic filter changes
   * @param geographicFilter The selected geographic filter
   */
  checkGeographicFilter(geographicFilter: any): void {
    this.clear = true;
    this.search = true;

    if (this.ticketFilter) {
      this.ticketFilter.selectedGeographicVal = geographicFilter;
    }
  }

  /**
   * Select provider for comment functionality
   * @param claimantNameForComment Provider name for comment
   * @param _ticket The current ticket (unused in this implementation)
   */
  providerSelectedForComment(claimantNameForComment: string, _ticket: any): void {
    if (!claimantNameForComment) {
      this._notification.warn('Warning', 'Please select a valid provider');
      return;
    }

    this._tripTicketService.userIdForProvider(claimantNameForComment).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (response: any) => {
        this._localStorage.set('selecteProvidersUserId', response);
        this.commentForProvider = true;
      },
      error: (error: any) => {
        this._notification.error('Error', 'Failed to get provider user ID: ' + (error.message || error));
      }
    });
  }

  /**
   * Save filter name validation
   */
  filterNameSave(): void {
    // Debug: Log the filter name as user types

    if (this.ticketFilter.filterName) {
      this.ticketFilter.filterName = this.ticketFilter.filterName.trim();
      this.nameSave = this.ticketFilter.filterName !== '';
    } else {
      this.nameSave = false;
    }

  }

  /**
   * Show quick summary view
   */
  showQuickSummary(): void {
    this.quickSummary = true;
  }




  /**
   * Toggle the summary report dropdown (click-to-toggle)
   */
  toggleSummaryDropdown(): void {
    this.showSummaryDropdown = !this.showSummaryDropdown;
  }


  /**
   * Get summary report list
   */
  summaryList(): void {
    // console.log('Fetching summary report list...');
    const today = new Date();
    const reportRange = {
      fromDate: '',
      toDate: '',
      providerId: this.loggedProviderId,
      reportTicketFilterStatus: ['']
    };

    this._tripTicketService.getMinimunDate(this.loggedProviderId).pipe(
      takeUntil(this.destroy$)
    ).subscribe({
      next: (response: any) => {
        // Set date range
        if (response && response.date && response.date !== 'null') {
          reportRange.fromDate = response.date.slice(0, 17) + '00';
        } else {
          reportRange.fromDate = moment(today).format().slice(0, 17) + '00';
        }

        reportRange.toDate = moment(today).format().slice(0, 19);

        // Get summary reports within range
        // console.log('Summary Report range:', reportRange);
        this._tripTicketService.getSummaryReports(reportRange).pipe(
          takeUntil(this.destroy$)
        ).subscribe({
          next: (summaryResponse: any) => {
            // console.log('Summary report data:', summaryResponse);
            // Map summaryResponse to SummaryReport instance
            this.summaryReport = this.mapToSummaryReport(summaryResponse);


          },
          error: (error: any) => {
            this._notification.error('Error', 'Failed to load summary report: ' + (error.message || error));
          }
        });
      },
      error: (error: any) => {
        this._notification.error('Error', 'Failed to get minimum date: ' + (error.message || error));
      }
    });
  }


  /**
   * Maps a raw summary report object to a SummaryReport instance
   */
  private mapToSummaryReport(data: any): SummaryReport {
    const report = new SummaryReport();
    report['totalTicketCount'] = data.totalTicketCount ?? 0;
    report['rescindedTicketCount'] = data.rescindedTicketCount ?? 0;
    report['availabeTicketCount'] = data.availabeTicketCount ?? 0;
    report['approvedTicketCount'] = data.approvedTicketCount ?? 0;
    report['expiredTicketCount'] = data.expiredTicketCount ?? 0;
    report['completedTicketCount'] = data.completedTicketCount ?? 0;
    report['totalCliamsReceived'] = data.totalCliamsReceived ?? 0;
    report['rescindedCaimReceived'] = data.rescindedCaimReceived ?? 0;
    report['approvedClaimReceived'] = data.approvedClaimReceived ?? 0;
    report['pendingClaimReceived'] = data.pendingClaimReceived ?? 0;
    report['declinedClaimReceived'] = data.declinedClaimReceived ?? 0;
    report['totalCliamsSubmitted'] = data.totalCliamsSubmitted ?? 0;
    report['rescindedCaimSubmitted'] = data.rescindedCaimSubmitted ?? 0;
    report['approvedClaimSubmitted'] = data.approvedClaimSubmitted ?? 0;
    report['pendingClaimSubmitted'] = data.pendingClaimSubmitted ?? 0;
    report['declinedClaimSubmitted'] = data.declinedClaimSubmitted ?? 0;
    return report;
  }

  // Permission-related methods required by the HTML template

  /**
   * Determines row style class based on ticket status
   * Updated to match PrimeNG Angular 18 interface which expects (rowData, rowIndex)
   */
  lookupRowStyleClass(rowData: any, _rowIndex?: number): string {
    if (!rowData || !rowData.status) return '';

    if (rowData.status === 'Cancelled') {
      return 'cancelled-row';
    } else if (rowData.status === 'Rescinded') {
      return 'rescinded-row';
    } else if (rowData.status === 'Completed') {
      return 'completed-row';
    } else if (rowData.status === 'Available') {
      return '';
    }
    return '';
  }






  /**
   * Check if user can update a ticket
   */
  canEditTicket(ticket: any): boolean {
    if ( this.loggedRole === 'ROLE_READONLY' ) return false;
    // console.log('canEditTicket check for ticket:', ticket);
    if (!ticket || ticket.status?.type === 'Completed') return false;
    if (this.loggedRole === 'ROLE_ADMIN') return true;

    //return ticket.originator_provider_id === this.loggedProviderId &&
    //       ticket.status === 'Available';
    return (ticket.myTicket === true ||
      (ticket.origin_provider_id === this.loggedProviderId && ticket.status?.type === 'Available')
    );

  }

  showUberButton(ticket: any): boolean {
    //console.log('Checking Uber button visibility for ticket:', ticket , " with current trip summary:", this.currentTripSummary);
    //console.log('Show Uber button for ticket:', ticket);
    if ( this.loggedRole === 'ROLE_READONLY' ) return false;
    if ( this.currentTripSummary && this.currentTripSummary?.status?.indexOf('cancel') !== -1 ) return true; // trip is cancelled
    if ( this.currentTripSummary && this.currentTripSummary?.guest?.guest_id && ticket.status?.type !== 'Available' ) return false; // this is already an Uber Trip
    return ( this.currentTripSummary && (this.loggedRole === 'ROLE_ADMIN' ||   this.loggedProviderId === 32) );
  }


  showRideStatusButton(ticket: any): boolean {

    return this.currentTripSummary && ticket?.trip_Claims && ticket.trip_Claims.length > 0
  }

  /**
   * Check if user can rescind a ticket
   */
  canRescindTicket(ticket: any): boolean {
    if ( this.loggedRole === 'ROLE_READONLY' ) return false;
    if (!ticket) return false;
    if (this.loggedRole === 'ROLE_ADMIN') return true;
    // Originators can rescind while not Cancelled/Completed
    const isOriginatorEligible = (
      ticket.origin_provider_id === this.loggedProviderId &&
      ticket.status?.type !== 'Cancelled' &&
      ticket.status?.type !== 'Completed' &&
      ticket.status?.type !== 'Expired'
    );

    // Claimants can rescind only if the trip is more than 2 business days in advance
    const requestedPickup = this.getTicketRequestedPickupDateTime(ticket);
    const isClaimantEligible = (
      ticket.claimant_provider_id === this.loggedProviderId &&
      ticket.status?.type !== 'Cancelled' &&
      ticket.status?.type !== 'Completed' &&
      // ticket must be more then 2 business days in advance
      this.isMoreThanBusinessDaysAway(requestedPickup, 2)
    );
    return isOriginatorEligible || isClaimantEligible;
  }

  canCancelTicket(ticket: any): boolean {
    if ( this.loggedRole === 'ROLE_READONLY' ) return false;
    if (!ticket) return false;
    if (this.loggedRole === 'ROLE_ADMIN') return true;
    // Originators can cancel while not Cancelled/Completed
    const isOriginatorEligible = (
      ticket.origin_provider_id === this.loggedProviderId &&
      ticket.status?.type !== 'Cancelled' &&
      ticket.status?.type !== 'Completed' &&
      ticket.status?.type !== 'Expired'
    );

    const isClaimantEligible = (
      ticket.claimant_provider_id === this.loggedProviderId &&
      ticket.status?.type !== 'Cancelled' &&
      ticket.status?.type !== 'Completed' &&
      ticket.status?.type !== 'Rescinded'
    );
    return isOriginatorEligible || isClaimantEligible;
  }


  /**
   * Determine if a target date/time is more than N business days from now.
   * Business days are Mon–Fri; weekends are excluded. If target is null/invalid,
   * we return true to preserve previous behavior (do not block rescind when
   * schedule is unknown).
   */
  private isMoreThanBusinessDaysAway(target: Date | null, businessDays: number): boolean {
    if (!target || isNaN(target.getTime())) return true;

    const now = new Date();
    // Start counting from end of today to be lenient within the current day
    const cursor = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 23, 59, 59, 999);
    let counted = 0;

    while (counted < businessDays) {
      cursor.setDate(cursor.getDate() + 1);
      const day = cursor.getDay(); // 0=Sun, 6=Sat
      if (day !== 0 && day !== 6) {
        counted++;
      }
    }

    return target.getTime() > cursor.getTime();
  }

  /**
   * Attempt to extract the requested pickup datetime for a ticket.
   * Tries several field name variants and common date/time formats.
   * Returns a Date or null if it cannot be determined.
   */
  private getTicketRequestedPickupDateTime(ticket: any): Date | null {
    if (!ticket) return null;

    const pickFirst = (obj: any, ...keys: string[]) => {
      for (const k of keys) {
        const v = obj?.[k];
        if (v !== undefined && v !== null && v !== '' && v !== '-') return v;
      }
      return null;
    };

    // Try combined datetime fields first
    const combined = pickFirst(
      ticket,
      '_requested_pickUpDateTime',
      '_requested_pickupDateTime',
      'requested_pickUpDateTime',
      'requested_pickupDateTime',
      'requested_pickup_date_time',
      'requested_pickup_dateTime',
      'pickupDateTime',
      'pickup_date_time',
      '_requested_dropOffDateTime',
      '_requested_dropoffDateTime',
      'requested_dropOffDateTime',
      'requested_dropoffDateTime',
      'requested_dropOff_date_time',
      'requested_dropoff_dateTime',
      'dropoffDateTime',
      'dropOffDateTime',
      'dropOff_date_time',
    );

    const parseWithMoment = (value: any, fmts: string[]): Date | null => {
      if (typeof value !== 'string') return null;
      // moment is already used throughout this file
      const m = (window as any).moment ? (window as any).moment(value, fmts, true) : (moment as any)(value, fmts, true);
      return m && m.isValid ? m.toDate() : null;
    };

    // Common formats for combined datetime strings
    const combinedFormats = [
      'MM/DD/YYYY hh:mm A',
      'MM/DD/YYYY HH:mm',
      moment.ISO_8601 as unknown as string
    ];

    let dt: Date | null = null;
    if (combined) {
      dt = parseWithMoment(combined, combinedFormats);
      if (dt) return dt;
      // Try native Date as a last resort
      const native = new Date(combined);
      if (!isNaN(native.getTime())) return native;
    }

    // Fall back to separate date and time fields
    const dateStr = pickFirst(
      ticket,
      'requested_pickup_date',
      'requested_pickUp_date',
      '_requested_pickup_date',
      '_requested_pickUp_date'
    );
    const timeStr = pickFirst(
      ticket,
      'requested_pickup_time',
      'requested_pickUp_time',
      '_requested_pickup_time',
      '_requested_pickUp_time'
    );

    const dateFormats = ['MM/DD/YYYY', 'YYYY-MM-DD'];

    if (dateStr && timeStr) {
      // Try combined parsing
      const combinedStr = `${dateStr} ${timeStr}`;
      dt = parseWithMoment(combinedStr, [
        'MM/DD/YYYY hh:mm A',
        'MM/DD/YYYY HH:mm',
        'YYYY-MM-DD HH:mm',
        'YYYY-MM-DD hh:mm A'
      ]);
      if (dt) return dt;
    }

    if (dateStr) {
      dt = parseWithMoment(dateStr, dateFormats);
      if (dt) {
        // Assume end-of-day if only date present, to be lenient
        dt.setHours(23, 59, 59, 999);
        return dt;
      }
    }

    return null;
  }


  /**
   * Check if user can claim a ticket
   */
  canClaimTicket(ticket: any): boolean {
    if ( this.loggedRole === 'ROLE_READONLY' ) return false;
    if (!ticket) return false;
    if (ticket.status?.type === 'Cancelled' || ticket.status?.type === 'Completed' ) return false;
    if ( this.loggedRole === 'ROLE_ADMIN') return true;
    const isAlreadyClaimant = (ticket.trip_Claims ?? []).some((claim: any) =>
      claim.claimant_provider_id === this.loggedProviderId
    );

    //console.log('canClaimTicket check - isAlreadyClaimant:', isAlreadyClaimant, 'ticketId:', ticket);

    // Provider cannot claim their own ticket
    const isOwnTicket = ticket.origin_provider_id === this.loggedProviderId;

    // console.log('canClaimTicket check:', {
    //   ticketId: ticket.id,
    //   ticketStatus: ticket.status,
    //   isAlreadyClaimant,
    //   isOwnTicket,
    //   loggedProviderId: this.loggedProviderId,
    //   originatorProviderId: ticket.origin_provider_id,
    //   tripClaims: ticket.trip_Claims
    // });
    return !isAlreadyClaimant && !isOwnTicket;
  }

  /**
   * Check if user can approve a claim
   */
  canApproveClaim(ticket: any): boolean {
    if ( this.loggedRole === 'ROLE_READONLY' ) return false;
    if (!ticket) return false;

    // Only originators of the ticket can approve claims
    return ticket.origin_provider_id === this.loggedProviderId &&
      ticket.status.type === 'Pending';
  }

  /**
   * Check if user can decline a claim
   */
  canDeclineClaim(ticket: any): boolean {
    if ( this.loggedRole === 'ROLE_READONLY' ) return false;
    if (!ticket) return false;
    // Only originators of the ticket can decline claims
    return ticket.origin_provider_id === this.loggedProviderId &&
      ticket.status.type === 'Pending';
  }

  /**
   * Check if user can rescind a claim
   */
  /*
  canRescindClaim(ticket: any): boolean {

    if ( this.loggedRole === 'ROLE_READONLY' ) return false;
    if (!ticket) return false;
    // Only claimants can rescind their own claims
    return ticket.claimant_provider_id === this.loggedProviderId &&
      ticket.status.type === 'Pending';
  }*/

  /**
   * Check if user can edit a claim
   */
  canEditClaim(ticket: any): boolean {
    if (!ticket || this.loggedRole === 'ROLE_READONLY' ) return false;
    return ( this.loggedRole === 'ROLE_ADMIN' || (ticket.myTicket && ticket?.claimant?.providerId === this.loggedProviderId) )
  }





  /**
   * Calculate and display route on the map using Leaflet
   */
  calculateAndDisplayRoute(): void {
    // This method is now a no-op; use showMap() for Leaflet rendering
    this.showMap();
  }

  /**
   * Show map with route between pickup and dropoff using Leaflet
   */
  showMap(): void {


    // Check if we have valid ticket data
    if (!this.Ticket) {
      console.error('🗺️ ERROR: No ticket data available');
      this._notification.error('Error', 'No ticket selected for map view');
      return;
    }

    // Check pickup address
    if (!this.Ticket.pickup_address) {
      console.error('🗺️ ERROR: No pickup address in ticket data');
      this._notification.error('Error', 'Pickup address not available');
      return;
    }

    // Check dropoff address
    if (!this.Ticket.drop_off_address) {
      console.error('🗺️ ERROR: No dropoff address in ticket data');
      this._notification.error('Error', 'Dropoff address not available');
      return;
    }

    // Implementation uses Leaflet
    // Set a flag to show the map in the template, and trigger rendering

    this.isShowMap = true;


    setTimeout(() => {


      if (this.leafletMap) {

        this.leafletMap.invalidateSize();
        this.renderTripRouteOnMap();
      } else {

        // Try again after another delay to allow map to initialize
        setTimeout(() => {

          if (this.leafletMap) {

            this.leafletMap.invalidateSize();
            this.renderTripRouteOnMap();
          } else {
            console.error('🗺️ ERROR: leafletMap still not initialized after delay');
            this._notification.error('Error', 'Map failed to initialize');
          }
        }, 500);
      }
    }, 100);


  }

  /**
   * Called when the Leaflet map is ready
   */
  onTripTicketMapReady(map: L.Map): void {

    this.leafletMap = map;

    this.renderTripRouteOnMap();
  }

  /**
   * Render the trip route on the map using Leaflet
   */
  private async renderTripRouteOnMap(): Promise<void> {


    if (!this.leafletMap) {
      console.error('🗺️ ERROR: No leafletMap instance available');
      return;
    }


    // Remove old markers and route
    this.leafletMarkers?.forEach(marker => marker.remove());
    this.leafletMarkers = [];
    if (this.leafletRoute) {
      this.leafletRoute.remove();
      this.leafletRoute = null;
    }

    // Validate ticket and coordinates

    if (!this.Ticket || !this.Ticket.pickup_address || !this.Ticket.drop_off_address) {
      console.error('🗺️ ERROR: Ticket information is incomplete');
      this._notification.error('Error', 'Ticket information is incomplete');
      return;
    }


    const pickupLat = this.Ticket.pickup_address.latitude;
    const pickupLng = this.Ticket.pickup_address.longitude;
    const dropLat = this.Ticket.drop_off_address.latitude;
    const dropLng = this.Ticket.drop_off_address.longitude;



    if (isNaN(pickupLat) || isNaN(pickupLng) || isNaN(dropLat) || isNaN(dropLng)) {
      console.error('🗺️ ERROR: Invalid coordinates in ticket address data');
      this._notification.error('Error', 'Invalid coordinates in ticket address data');
      return;
    }

    const pickupCoords: L.LatLngTuple = [pickupLat, pickupLng];
    const dropoffCoords: L.LatLngTuple = [dropLat, dropLng];


    // Add markers      // Add pickup marker
    const pickup = this.Ticket.pickup_address;
    const dropoff = this.Ticket.drop_off_address;
    const pickupMarker = L.marker(pickupCoords, { icon: this.pickupIcon }).addTo(this.leafletMap!);
    pickupMarker.bindPopup(
      `<b>Pickup:</b><br>${pickup.street1 || ''}, ${pickup.city || ''}-${pickup.zipcode || ''}, ${pickup.state || ''
      }`
    );

    // Add dropoff marker
    const dropoffMarker = L.marker(dropoffCoords, { icon: this.dropoffIcon }).addTo(this.leafletMap!);
    dropoffMarker.bindPopup(
      `<b>Dropoff:</b><br>${dropoff.street1 || ''}, ${dropoff.city || ''}-${dropoff.zipcode || ''
      }, ${dropoff.state || ''}`
    );

    this.leafletMarkers = [pickupMarker, dropoffMarker];




    // Show loading spinner

    this.isRouteLoading = true;
    try {
      // Fetch route from OSRM
      // console.log('🗺️ Fetching route from OSRM');
      const url = `https://router.project-osrm.org/route/v1/driving/${pickupLng},${pickupLat};${dropLng},${dropLat}?overview=full&geometries=geojson`;

      // console.log('🗺️ OSRM URL:', url);

      const response = await fetch(url);


      if (response.ok) {
        const data = await response.json();


        if (data.routes && data.routes.length > 0) {
          console.log('🗺️ Route found, creating polyline');
          const route = data.routes[0];
          const routeCoords: L.LatLngTuple[] = route.geometry.coordinates.map((coord: number[]) => [coord[1], coord[0]]);


          this.leafletRoute = L.polyline(routeCoords, { color: 'blue', weight: 4 }).addTo(this.leafletMap!);
          this.leafletMap!.fitBounds(this.leafletRoute.getBounds(), { padding: [40, 40] });

          this.isRouteLoading = false;
          return;
        } else {
          console.warn('🗺️ No routes found in OSRM response');
        }
      } else {
        console.warn('🗺️ OSRM request failed with status:', response.status);
      }

      // If OSRM fails, fallback to straight line
      console.log('🗺️ Falling back to straight line route');
      this.leafletRoute = L.polyline([pickupCoords, dropoffCoords], { color: 'blue', weight: 4 }).addTo(this.leafletMap!);
      this.leafletMap!.fitBounds([pickupCoords, dropoffCoords], { padding: [40, 40] });

    } catch (err) {
      //console.error('🗺️ ERROR during route fetching:', err);
      // Fallback to straight line

      this.leafletRoute = L.polyline([pickupCoords, dropoffCoords], { color: 'blue', weight: 4 }).addTo(this.leafletMap!);
      this.leafletMap!.fitBounds([pickupCoords, dropoffCoords], { padding: [40, 40] });
      console.log('🗺️ Error fallback route rendered');
    } finally {
      this.isRouteLoading = false;
    }
  }


  toggleCollapse(sectionId: string, event: MouseEvent): void {
    // Prevent the default behavior which causes logout
    event.preventDefault();

    // Find the collapse element
    const collapseElement = document.getElementById(sectionId);

    if (collapseElement) {
      // Toggle the 'in' class which controls the expanded state
      collapseElement.classList.toggle('in');
    }
  }

  onTimeFromChange(event: any) {
    console.log('Pickup time changed:', event);
    this.reqPickUpStartTime = event;
    this.updateOperHoursSearchAvailability();
  }

  onTimeToChange(event: any) {
    console.log('Dropoff time changed:', event);
    this.reqPickUpEndTime = event;
    this.updateOperHoursSearchAvailability();
  }

  updateOperHoursSearchAvailability() {
    // Enable search when both operating hours times are provided
    if (this.reqPickUpStartTime && this.reqPickUpEndTime) {
      this.ticketFilter.operatingHours = this.buildOperatingHoursParameters();
      this.search = true;
      console.log('Operating hours search parameters updated:', JSON.stringify(this.ticketFilter));
    }
  }




  private buildOperatingHoursParameters(): FilterParameter[] {
    if (this.reqPickUpStartTime && this.reqPickUpEndTime) {
      const params: FilterParameter[] = [];
      params.push({ name: 'pickUpTime', value: this.reqPickUpStartTime });
      params.push({ name: 'dropOffTime', value: this.reqPickUpEndTime });
      return params;
    }
    return [];
  }


  /**
   * Get screen size category for better pagination tuning
   */
  public getScreenSizeCategory(): string {
    const width = window.innerWidth;
    const _height = window.innerHeight;

    if (width < 768) {
      return 'mobile';
    } else if (width < 1024) {
      return 'tablet';
    } else if (width < 1440) {
      return 'desktop';
    } else {
      return 'large-desktop';
    }
  }

  /**
   * Get information about current pagination optimization
   */
  getPaginationInfo(): any {
    return {
      isDynamicEnabled: true, // Always enabled
      currentRowsPerPage: this.rowsPerPage,
      lastCalculatedOptimal: this.lastCalculatedOptimalRows,
      screenCategory: this.getScreenSizeCategory(),
      viewportSize: {
        width: window.innerWidth,
        height: window.innerHeight
      },
      calculationParameters: {
        minRows: this.MIN_ROWS_PER_PAGE,
        maxRows: this.MAX_ROWS_PER_PAGE,
        rowHeight: this.ROW_HEIGHT_ESTIMATE,
        tableHeaderHeight: this.TABLE_HEADER_HEIGHT,
        viewportBuffer: this.VIEWPORT_BUFFER
      }
    };
  }




  // Add a debug method for claims
  private logClaimsData(message: string, claims: any): void {
    console.log('[CLAIMS DEBUG] ' + message, claims);

    // If there are claims, log additional details about the first one
    if (claims && claims.length > 0) {
      console.log('[CLAIMS DEBUG] First claim details:', {
        id: claims[0].id,
        claimant_provider_name: claims[0].claimant_provider_name,
        proposed_pickup_time: claims[0].proposed_pickup_time,
        _proposed_pickup_time: claims[0]._proposed_pickup_time,
        ackStatus: claims[0].ackStatus,
        ackStatusString: claims[0].ackStatusString,
        proposed_fare: claims[0].proposed_fare,
        status: claims[0].status
      });
    } else {
      console.log('[CLAIMS DEBUG] No claims found or empty array');
    }
  }/* eslint-disable @typescript-eslint/no-unused-vars */

  private initializeEditForm(): void {
    this.editTicketForm = this.formBuilder.group({
      customerFirstName: ['', Validators.required],
      customerLastName: ['', Validators.required],
      pickupDate: [null],
      dropoffDate: [null],
      ticketStatusId: [null],
      seatsRequired: [1, [Validators.required, Validators.min(1), Validators.max(10)]],
      notes: ['']
    }, { validators: [this.atLeastOneDateValidator] });

    this.tripResultForm = this.formBuilder.group({
      actualPickupArriveTime: [null],
      actualPickupDepartTime: [null, Validators.required],
      actualDropOffArriveTime: [null],
      actualDropOffDepartTime: [null, Validators.required],
      pickUpLatitude: [null],
      pickupLongitude: [null],
      dropOffLatitude: [null],
      dropOffLongitude: [null],
      vehicleId: [''],
      driverId: [''],
      numberOfGuests: [0],
      numberOfAttendants: [0],
      numberOfPassengers: [0],
      fareCollected: [null],
      noShowFlag: [false],
      cancellationReason: [''],
      noShowReason: ['']
    });
  }

  // Enable or disable all controls except ticketStatusId depending on whether the ticket
  // is editable (status.type === 'Available'). When controls are disabled, they won't
  // participate in validation, so the form can be submitted to change status only.
  private setEditability(isEditable: boolean): void {
    const controlsToToggle = ['customerFirstName', 'customerLastName', 'pickupDate', 'dropoffDate', 'seatsRequired', 'notes'];
    for (const name of controlsToToggle) {
      const ctrl = this.editTicketForm.get(name);
      if (!ctrl) continue;
      if (isEditable) {
        ctrl.enable({ emitEvent: false });
      } else {
        ctrl.disable({ emitEvent: false });
      }
    }

    // When editing is locked to status only, remove the atLeastOneDateValidator since
    // the date controls are disabled and would otherwise cause the form to be invalid.
    if (!isEditable) {
      this.editTicketForm.clearValidators();
      // Keep any built-in validators on statusId by re-adding only control-level validators
      this.editTicketForm.updateValueAndValidity({ emitEvent: false });
    } else {
      // Restore form-level validator when fully editable
      this.editTicketForm.setValidators([this.atLeastOneDateValidator]);
      this.editTicketForm.updateValueAndValidity({ emitEvent: false });
    }
  }

  // Form-level validator: at least one of pickupDate or dropoffDate must be present
  // ValidatorFn-compatible: accepts AbstractControl to satisfy FormGroup.setValidators
  private atLeastOneDateValidator = (control: import('@angular/forms').AbstractControl): { [key: string]: boolean } | null => {
    const group = control as FormGroup;
    const pickup = group.get('pickupDate')?.value;
    const drop = group.get('dropoffDate')?.value;
    return (pickup || drop) ? null : { atLeastOneDate: true };
  };

  editTicket(ticket: any): void {
   // console.log('Editing ticket:', ticket);
    this.currentEditingTicket = ticket;

    // Robust parsing: API contains inconsistent field names and date/time formats.
    // Try multiple common field name variants and normalize common date/time formats
    // before constructing Date objects. If either date or time is missing, leave the
    // form control null so the UI shows an empty picker instead of NaN.
    const pick = (obj: any, ...names: string[]) => {
      for (const n of names) {
        if (obj && Object.prototype.hasOwnProperty.call(obj, n)) {
          const v = obj[n];
          if (v !== undefined && v !== null && String(v).trim() !== '') return v;
        }
      }
      return null;
    };

    const normalizeDate = (d: any): string | null => {
      if (!d) return null;
      const s = String(d).trim();
      if (/^\d{4}-\d{2}-\d{2}$/.test(s)) return s; // 2025-09-27
      const m = s.match(/^(\d{1,2})\/(\d{1,2})\/(\d{2,4})$/);
      if (m) {
        let year = m[3];
        if (year.length === 2) {
          year = Number(year) > 50 ? `19${year}` : `20${year}`;
        }
        const mm = m[1].padStart(2, '0');
        const dd = m[2].padStart(2, '0');
        return `${year}-${mm}-${dd}`;
      }
      return null;
    };

    const normalizeTime = (t: any): string | null => {
      if (!t) return null;
      const s = String(t).trim();
      // 24h: HH:mm or HH:mm:ss
      const m24 = s.match(/^(\d{1,2}):(\d{2})(?::(\d{2}))?$/);
      if (m24) {
        const hh = m24[1].padStart(2, '0');
        const mm = m24[2];
        const ss = m24[3] ?? '00';
        return `${hh}:${mm}:${ss}`;
      }
      // 12h with AM/PM
      const m12 = s.match(/^(\d{1,2}):(\d{2})(?::(\d{2}))?\s*([ap]m)$/i);
      if (m12) {
        let hh = parseInt(m12[1], 10);
        const mm = m12[2];
        const ss = m12[3] ?? '00';
        const suffix = m12[4].toLowerCase();
        if (suffix === 'pm' && hh < 12) hh += 12;
        if (suffix === 'am' && hh === 12) hh = 0;
        return `${String(hh).padStart(2, '0')}:${mm}:${ss}`;
      }
      return null;
    };

    // (Diagnostics removed) previously logged ticket keys/values for debugging

    // Try to handle combined datetime fields like '_requested_dropOffDateTime' which some
    // records include (e.g. '09/27/25 05:50 PM'). If present, split into date and time parts.
    const pickupCombinedRaw = pick(ticket, '_requested_pickUpDateTime', '_requested_pickupDateTime', 'requested_pickUpDateTime', 'requested_pickupDateTime', '_requested_pickUpDateTime');
    const dropoffCombinedRaw = pick(ticket, '_requested_dropOffDateTime', '_requested_dropoffDateTime', 'requested_dropOffDateTime', 'requested_dropoffDateTime', '_requested_dropOffDateTime');

    let pickupDateRaw = pick(ticket, 'requested_pickup_date', 'requested_pickUp_date', '_requested_pickup_date', '_requested_pickUp_date');
    let pickupTimeRaw = pick(ticket, 'requested_pickup_time', 'requested_pickUp_time', '_requested_pickup_time', '_requested_pickUp_time');

    let dropoffDateRaw = pick(ticket, 'requested_dropoff_date', 'requested_dropOff_date', '_requested_dropoff_date', '_requested_dropOff_date');
    let dropoffTimeRaw = pick(ticket, 'requested_dropoff_time', 'requested_dropOff_time', '_requested_dropoff_time', '_requested_dropOff_time');

    const extractFromCombined = (combined: any): { date: string | null; time: string | null } => {
      if (!combined) return { date: null, time: null };
      const s = String(combined).trim();
      // Common format: mm/dd/yy(yy) [time] e.g. '09/27/25 05:50 PM' or '09/27/2025 17:50:00'
      const m = s.match(/^(\d{1,2}\/\d{1,2}\/\d{2,4})\s+(.+)$/);
      if (m) return { date: m[1], time: m[2] };
      return { date: null, time: null };
    };

    if (!pickupDateRaw && !pickupTimeRaw && pickupCombinedRaw) {
      const ex = extractFromCombined(pickupCombinedRaw);
      if (ex.date) pickupDateRaw = ex.date;
      if (ex.time) pickupTimeRaw = ex.time;
    }

    if (!dropoffDateRaw && !dropoffTimeRaw && dropoffCombinedRaw) {
      const ex = extractFromCombined(dropoffCombinedRaw);
      if (ex.date) dropoffDateRaw = ex.date;
      if (ex.time) dropoffTimeRaw = ex.time;
    }

    // Fallback: case-insensitive search for any ticket keys that look like pickup/dropoff date/time
    const findByRegex = (obj: any, regex: RegExp): any => {
      if (!obj) return null;
      const keys = Object.keys(obj);
      for (const k of keys) {
        if (regex.test(k)) {
          const v = obj[k];
          if (v !== undefined && v !== null && String(v).trim() !== '') return v;
        }
      }
      return null;
    };

    if (!pickupDateRaw) pickupDateRaw = findByRegex(ticket, /pick.*date/i);
    if (!pickupTimeRaw) pickupTimeRaw = findByRegex(ticket, /pick.*time/i);
    if (!dropoffDateRaw) dropoffDateRaw = findByRegex(ticket, /drop.*date/i);
    if (!dropoffTimeRaw) dropoffTimeRaw = findByRegex(ticket, /drop.*time/i);

    const pickupDateNorm = normalizeDate(pickupDateRaw);
    const pickupTimeNorm = normalizeTime(pickupTimeRaw);
    const dropoffDateNorm = normalizeDate(dropoffDateRaw);
    const dropoffTimeNorm = normalizeTime(dropoffTimeRaw);

    const pickupDateVal = pickupDateNorm && pickupTimeNorm ? new Date(`${pickupDateNorm}T${pickupTimeNorm}`) : null;
    const dropoffDateVal = dropoffDateNorm && dropoffTimeNorm ? new Date(`${dropoffDateNorm}T${dropoffTimeNorm}`) : null;


    this.editTicketForm.patchValue({
      customerFirstName: ticket.customer_first_name,
      customerLastName: ticket.customer_last_name,
      pickupDate: pickupDateVal,
      dropoffDate: dropoffDateVal,
      ticketStatusId: ticket?.status?.statusId ? String(ticket.status.statusId) : null,
      seatsRequired: ticket.customer_seats_required,
      notes: ticket.trip_notes
    });
    // If ticket status isn't 'Available', lock all fields except status
    const statusType = ticket?.status?.type ? String(ticket.status.type).trim() : '';
    const editable = statusType.toLowerCase() === 'available';
    this.setEditability(editable);

    this.showEditDialog = true;
  }

  onSubmitEdit(): void {
    if (this.editTicketForm.valid && !this.isSaving) {
      const formValue = this.editTicketForm.value;

      // Check if user selected "Completed" status
      if (formValue.ticketStatusId === this.completedStatusId) {
        // Initialize trip result form with defaults and show step 2
        this.initializeTripResultForm();
        this.showTripResultStep = true;
        return;
      }

      // Normal edit flow for non-completed status
      this.isSaving = true;
      const ticketUpdateData = {
        customer_first_name: formValue.customerFirstName,
        customer_last_name: formValue.customerLastName,
        requested_pickup_date: formValue.pickupDate ? moment(formValue.pickupDate).format('YYYY-MM-DD') : null,
        requested_pickup_time: formValue.pickupDate ? moment(formValue.pickupDate).format('HH:mm:ss') : null,
        requested_dropoff_date: formValue.dropoffDate ? moment(formValue.dropoffDate).format('YYYY-MM-DD') : null,
        requested_dropoff_time: formValue.dropoffDate ? moment(formValue.dropoffDate).format('HH:mm:ss') : null,
        status_id: formValue.ticketStatusId ? formValue.ticketStatusId  : null,
        customer_seats_required: formValue.seatsRequired,
        trip_notes: formValue.notes
      };

      this._tripTicketService.updateTripTicket(this.currentEditingTicket.id, ticketUpdateData)
        .pipe(
          takeUntil(this.destroy$),
          finalize(() => {
            this.isSaving = false;
          })
        )
        .subscribe({
          next: (_response: any) => {
            this._notification.success('Success', 'Trip ticket updated successfully');
            this.showEditDialog = false;
            this.resetEditDialog();
            this.getTicketsList();
          },
          error: (error: any) => {
            this._notification.error('Error', 'Failed to update trip ticket: ' + (error.message || error));
          }
        });
    }
  }

  /**
   * Initialize trip result form with default values from the ticket
   */
  private initializeTripResultForm(): void {
    const ticket = this.currentEditingTicket;

    // Helper functions from editTicket method to parse dates
    const pick = (obj: any, ...names: string[]) => {
      for (const n of names) {
        if (obj && Object.prototype.hasOwnProperty.call(obj, n)) {
          const v = obj[n];
          if (v !== undefined && v !== null && String(v).trim() !== '') return v;
        }
      }
      return null;
    };

    const normalizeDate = (d: any): string | null => {
      if (!d) return null;
      const s = String(d).trim();
      if (/^\d{4}-\d{2}-\d{2}$/.test(s)) return s;
      const m = s.match(/^(\d{1,2})\/(\d{1,2})\/(\d{2,4})$/);
      if (m) {
        let year = m[3];
        if (year.length === 2) {
          year = Number(year) > 50 ? `19${year}` : `20${year}`;
        }
        const mm = m[1].padStart(2, '0');
        const dd = m[2].padStart(2, '0');
        return `${year}-${mm}-${dd}`;
      }
      return null;
    };

    const normalizeTime = (t: any): string | null => {
      if (!t) return null;
      const s = String(t).trim();
      const m24 = s.match(/^(\d{1,2}):(\d{2})(?::(\d{2}))?$/);
      if (m24) {
        const hh = m24[1].padStart(2, '0');
        const mm = m24[2];
        const ss = m24[3] ?? '00';
        return `${hh}:${mm}:${ss}`;
      }
      const m12 = s.match(/^(\d{1,2}):(\d{2})(?::(\d{2}))?\s*([ap]m)$/i);
      if (m12) {
        let hh = parseInt(m12[1], 10);
        const mm = m12[2];
        const ss = m12[3] ?? '00';
        const suffix = m12[4].toLowerCase();
        if (suffix === 'pm' && hh < 12) hh += 12;
        if (suffix === 'am' && hh === 12) hh = 0;
        return `${String(hh).padStart(2, '0')}:${mm}:${ss}`;
      }
      return null;
    };

    // Get pickup and dropoff dates/times from ticket
    const pickupDateRaw = pick(ticket, 'requested_pickup_date', 'requested_pickUp_date', '_requested_pickup_date', '_requested_pickUp_date');
    const pickupTimeRaw = pick(ticket, 'requested_pickup_time', 'requested_pickUp_time', '_requested_pickup_time', '_requested_pickUp_time');
    const dropoffDateRaw = pick(ticket, 'requested_dropoff_date', 'requested_dropOff_date', '_requested_dropoff_date', '_requested_dropOff_date');
    const dropoffTimeRaw = pick(ticket, 'requested_dropoff_time', 'requested_dropOff_time', '_requested_dropoff_time', '_requested_dropOff_time');

    // Normalize and construct default dates
    const pickupDateNorm = normalizeDate(pickupDateRaw);
    const pickupTimeNorm = normalizeTime(pickupTimeRaw);
    const dropoffDateNorm = normalizeDate(dropoffDateRaw);
    const dropoffTimeNorm = normalizeTime(dropoffTimeRaw);

    const defaultPickupDate = pickupDateNorm && pickupTimeNorm ? new Date(`${pickupDateNorm}T${pickupTimeNorm}`) : null;
    const defaultDropoffDate = dropoffDateNorm && dropoffTimeNorm ? new Date(`${dropoffDateNorm}T${dropoffTimeNorm}`) : null;

    this.tripResultForm.patchValue({
      actualPickupArriveTime: null,
      actualPickupDepartTime: defaultPickupDate,
      actualDropOffArriveTime: null,
      actualDropOffDepartTime: defaultDropoffDate,
      pickUpLatitude: null,
      pickupLongitude: null,
      dropOffLatitude: null,
      dropOffLongitude: null,
      vehicleId: '',
      driverId: '',
      numberOfGuests: ticket.guests || 0,
      numberOfAttendants: ticket.attendants || 0,
      numberOfPassengers: (ticket.guests || 0) + (ticket.attendants || 0),
      fareCollected: null,
      noShowFlag: false
    });
  }

  /**
   * Submit trip result completion form
   */
  onSubmitTripResult(): void {
    if (this.tripResultForm.valid && !this.isSaving) {
      this.isSaving = true;
      const formValue = this.tripResultForm.value;
      const editFormValue = this.editTicketForm.value;

      // First update the trip ticket status to completed
      const updateData = {
        customer_first_name: editFormValue.customerFirstName,
        customer_last_name: editFormValue.customerLastName,
        requested_pickup_date: editFormValue.pickupDate ? moment(editFormValue.pickupDate).format('YYYY-MM-DD') : null,
        requested_pickup_time: editFormValue.pickupDate ? moment(editFormValue.pickupDate).format('HH:mm:ss') : null,
        requested_dropoff_date: editFormValue.dropoffDate ? moment(editFormValue.dropoffDate).format('YYYY-MM-DD') : null,
        requested_dropoff_time: editFormValue.dropoffDate ? moment(editFormValue.dropoffDate).format('HH:mm:ss') : null,
        status_id: Number(this.completedStatusId),
        customer_seats_required: editFormValue.seatsRequired,
        trip_notes: editFormValue.notes
      };

      // Create trip result data
      const tripResultData: TripResultRequestDTO = {
        tripTicketId: this.currentEditingTicket.id,
        tripDate: moment().format('YYYY-MM-DD'),
        actualPickupArriveTime: formValue.actualPickupArriveTime ? moment(formValue.actualPickupArriveTime).format('YYYY-MM-DDTHH:mm:ss') : undefined,
        actualPickupDepartTime: formValue.actualPickupDepartTime ? moment(formValue.actualPickupDepartTime).format('YYYY-MM-DDTHH:mm:ss') : undefined,
        actualDropOffArriveTime: formValue.actualDropOffArriveTime ? moment(formValue.actualDropOffArriveTime).format('YYYY-MM-DDTHH:mm:ss') : undefined,
        actualDropOffDepartTime: formValue.actualDropOffDepartTime ? moment(formValue.actualDropOffDepartTime).format('YYYY-MM-DDTHH:mm:ss') : undefined,
        pickUpLatitude: formValue.pickUpLatitude || undefined,
        pickupLongitude: formValue.pickupLongitude || undefined,
        dropOffLatitude: formValue.dropOffLatitude || undefined,
        dropOffLongitude: formValue.dropOffLongitude || undefined,
        vehicleId: formValue.vehicleId || undefined,
        driverId: formValue.driverId || undefined,
        numberOfGuests: formValue.numberOfGuests || undefined,
        numberOfAttendants: formValue.numberOfAttendants || undefined,
        numberOfPassengers: formValue.numberOfPassengers || undefined,
        fareCollected: formValue.fareCollected || undefined,
        noShowFlag: formValue.noShowFlag || false,
        cancellationReason: formValue.cancellationReason || undefined,
        noShowReason: formValue.noShowReason || undefined
      };

      // Create the trip result
      this._tripTicketService.createTripCompletedResult([tripResultData])
        .pipe(
          takeUntil(this.destroy$),
          finalize(() => {
            this.isSaving = false;
          })
        )
        .subscribe({
          next: (_tripResultResponse: any) => {
            this._notification.success('Success', 'Trip completed successfully');
            this.showEditDialog = false;
            this.resetEditDialog();
            this.getTicketsList();
          },
          error: (error: any) => {
            this._notification.error('Error', 'Failed to create trip result: ' + (error.message || error));
          }
        });
    }
  }

  /**
   * Reset edit dialog to step 1
   */
  resetEditDialog(): void {
    this.showTripResultStep = false;
  }

  /**
   * Go back to step 1 from trip result step
   */
  goBackToEditStep(): void {
    this.showTripResultStep = false;
  }









  /**
   * Resets the file selection and clears any error messages
   */
  public resetFileSelection(): void {
    this._uploadService.resetUploadState();
    // Reset the file input element
    const fileInput = document.querySelector('input[type="file"]') as HTMLInputElement;
    if (fileInput) {
      fileInput.value = '';
    }
    // State will be synced automatically via subscription
  }

  /**
   * Handles file drop events
   * @param event DragEvent containing the dropped files
   */
  onDrop(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();

    const files = event.dataTransfer?.files;
    if (files && files.length > 0) {
      this.onFileSelected({ target: { files: [files[0]] } } as any);
    }

    this._uploadService.setDragging(false);
    this.syncWithUploadService();
  }

  /**
   * Handles drag leave events
   * @param event DragEvent
   */
  onDragLeave(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this._uploadService.setDragging(false);
    this.syncWithUploadService();
  }

  /**
   * Handles drag over events
   * @param event DragEvent
   */
  onDragOver(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this._uploadService.setDragging(true);
    this.syncWithUploadService();
  }

  /**
   * Downloads the trip ticket template file
   */
  downloadTemplate(): void {
    const link = document.createElement('a');
    link.href = this.uploadTemplatePath;
    link.download = 'trip-exchange-import-export-template.xlsx';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }



  // Called when the Uber dialog is closed to refresh Trip Details
  onUberDialogClosed(): void {
    if (this.Ticket) {
      this.viewTicket(this.Ticket);
    }
  }


  // ---------------- Selection and Export Methods ----------------

  /**
   * Toggle select all functionality
   */
  toggleSelectAll(): void {
    const selectableTickets = this.TicketsList.filter(ticket =>
      ticket.status?.type !== 'Completed' && ticket.status?.type !== 'Expired'
    );

    if (this.selectAll) {
      // Select all selectable tickets
      selectableTickets.forEach(ticket => {
        ticket.selected = true;
      });
      this.selectedTickets = [...selectableTickets];
    } else {
      // Deselect all tickets
      this.TicketsList.forEach(ticket => {
        ticket.selected = false;
      });
      this.selectedTickets = [];
    }
  }

  /**
   * Handle individual ticket selection change
   */
  onTicketSelectionChange(): void {
    // Update selectedTickets array based on ticket.selected properties
    this.selectedTickets = this.TicketsList.filter(ticket => ticket.selected === true);

    // Update selectAll state based on current selections
    const selectableTickets = this.TicketsList.filter(ticket =>
      ticket.status?.type !== 'Completed' && ticket.status?.type !== 'Expired'
    );
    this.selectAll = selectableTickets.length > 0 && this.selectedTickets.length === selectableTickets.length;
  }

  /**
   * Check if a ticket can be selected (not completed/expired)
   */
  canSelectTicket(ticket: any): boolean {
    return ticket.status?.type !== 'Completed' && ticket.status?.type !== 'Expired';
  }

  /**
   * Get the count of selected tickets
   */
  get selectedTicketsCount(): number {
    return this.selectedTickets.length;
  }



  /**
   * Export selected tickets to CSV format
   */
  exportSelectedTicketsAsCSV(): void {
    if (this.selectedTickets.length === 0) {
      this._notification.warn('Warning', 'Please select at least one ticket to export');
      return;
    }

    this.isExporting = true;

    // Get selected ticket IDs
    const selectedIds = this.selectedTickets.map(ticket => ticket.id);

    // Call export service
    this._tripTicketService.exportTickets(selectedIds).pipe(
      takeUntil(this.destroy$),
      finalize(() => this.isExporting = false)
    ).subscribe(
      (response: any) => {
        //console.log('Export response:', response);
        // Support multiple response shapes: either an array, or an object with `data` array
        let exportRows: any[] | null = null;
        if (Array.isArray(response)) {
          exportRows = response;
        } else if (response && Array.isArray(response.data)) {
          exportRows = response.data;
        } else if (response && Array.isArray(response.results)) {
          // Some APIs return { results: [...] }
          exportRows = response.results;
        }

        if (exportRows && exportRows.length > 0) {
          // Generate CSV file using the export service
          this.generateCSVFile(exportRows);
        } else {
          console.warn('Export returned no rows:', response);
          this._notification.error('Error', 'No data received from export service');
        }
      },
      (error: any) => {
        console.error('Export error:', error);
        this._notification.error('Error', 'Failed to export trip data');
      }
    );
  }

  /**
   * Export selected tickets to Excel format (kept for potential future use)
   */
  exportSelectedTicketsAsExcel(): void {
    if (this.selectedTickets.length === 0) {
      this._notification.warn('Warning', 'Please select at least one ticket to export');
      return;
    }

    this.isExporting = true;

    // Get selected ticket IDs
    const selectedIds = this.selectedTickets.map(ticket => ticket.id);

    // Call export service
    this._tripTicketService.exportTickets(selectedIds).pipe(
      takeUntil(this.destroy$),
      finalize(() => this.isExporting = false)
    ).subscribe(
      (response: any) => {
        //console.log('Export response:', response);
        // Support multiple response shapes: either an array, or an object with `data` array
        let exportRows: any[] | null = null;
        if (Array.isArray(response)) {
          exportRows = response;
        } else if (response && Array.isArray(response.data)) {
          exportRows = response.data;
        } else if (response && Array.isArray(response.results)) {
          // Some APIs return { results: [...] }
          exportRows = response.results;
        }

        if (exportRows && exportRows.length > 0) {
          // Generate Excel file using the export service
          this.generateExcelFile(exportRows);
        } else {
          console.warn('Export returned no rows:', response);
          this._notification.error('Error', 'No data received from export service');
        }
      },
      (error: any) => {
        console.error('Export error:', error);
        this._notification.error('Error', 'Failed to export trip data');
      }
    );
  }

  /**
   * Clear all ticket selections
   */
  private clearSelection(): void {
    this.selectedTickets = [];
    this.selectAll = false;

    // Update all ticket selected properties
    if (this.TicketsList) {
      this.TicketsList.forEach(ticket => {
        ticket.selected = false;
      });
    }
  }

  /**
   * Generate CSV file from trip data
   */
  private generateCSVFile(tripData: TripTicketExportData[]): void {
    try {
      const csvBlob = this._exportService.generateCSVFile(tripData);
      const timestamp = this._exportService.generateTimestamp();
      const filename = `trip-export-${timestamp}.csv`;

      this._exportService.downloadFile(csvBlob, filename);

      // Notify success and clear selection
      this._notification.success('Success', `Successfully exported ${this.selectedTickets.length} trip ticket(s) to CSV file`);
      this.clearSelection();
    } catch (error) {
      console.error('CSV generation error:', error);
      this._notification.error('Error', 'Failed to generate CSV file. Please try again.');
    }
  }

  /**
   * Generate Excel file from trip data (kept for potential future use)
   */
  private generateExcelFile(tripData: TripTicketExportData[]): void {
    try {
      const excelBlob = this._exportService.generateExcelFile(tripData);
      const timestamp = this._exportService.generateTimestamp();
      const filename = `trip-export-${timestamp}.xlsx`;

      this._exportService.downloadFile(excelBlob, filename);

      // Notify success and clear selection
      this._notification.success('Success', `Successfully exported ${this.selectedTickets.length} trip ticket(s) to Excel file`);
      this.clearSelection();
    } catch (error) {
      console.error('Excel generation error:', error);
      this._notification.error('Error', 'Failed to generate Excel file. Please try again.');
    }
  }

}
