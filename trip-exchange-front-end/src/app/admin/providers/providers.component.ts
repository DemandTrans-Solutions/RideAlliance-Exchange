import { Component, ViewChild } from '@angular/core';
import { Input, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ConfirmationService, MessageService } from 'primeng/api';
import { Table } from 'primeng/table';
import { Logger } from '../../shared/service/default-log.service';
import { ProviderService } from './provider.service';
import { TokenService } from '../../shared/service/token.service';
import { NotificationEmitterService } from '../../shared/service/notification-emitter.service';
import { LocalStorageService } from '../../shared/service/local-storage.service';
import { ensureBreakpointWorks } from '../../shared/debug/debugHelper';
import { GeocodingService } from '../../shared/service/geocoding.service';
import moment from 'moment';

@Component({
  selector: 'app-providers',
  templateUrl: 'providers.component.html',
  styleUrls: ['providers.component.scss'],
  providers: [ProviderService],
})
export class ProvidersComponent implements OnInit {
  @ViewChild('dt') dt: Table;

  public providerListData: any[] = [];
  public provider: any;
  public providerId: number;
  public mask = ['(', /[1-9]/, /\d/, /\d/, ')', ' ', /\d/, /\d/, /\d/, '-', /\d/, /\d/, /\d/, /\d/];
  public showGrid = true;
  public height = true;
  public pageName = '';
  public editProviderIndex: number;
  public editProviderObj: any;
  public tripTicketProvisionalTime_hours: any;
  public tripTicketProvisionalTime_minutes: any;
  public providerList: any[] = [];
  public latitude: any;
  public longitude: any;
  public lat: any;
  public long: any;
  public loggerRole = this.localStorage.get('Role');

  constructor(
    private router: Router,
    private logger: Logger,
    private providerService: ProviderService,
    private notificationService: NotificationEmitterService,
    private localStorage: LocalStorageService,
    private confirmationService: ConfirmationService,
    private tokenService: TokenService,
    private messageService: MessageService,
    private geocodingService: GeocodingService
  ) {}

  ngOnInit() {
    if (!this.tokenService.get()) {
      this.router.navigate(['/login']);
      return;
    }

    // Add debug helper for breakpoint binding
    ensureBreakpointWorks('providers.component - ngOnInit');

    this.logger.debug('ProvidersComponent', 'Test');
    this.getProviders();
    if (screen.width > 1024) {
      this.height = false;
    }
  }

  filterGlobal(event: Event): void {
    if (this.dt) {
      this.dt.filterGlobal((event.target as HTMLInputElement).value, 'contains');
    }
  }

  onReject() {
    this.messageService.clear('c');
  }

  // Add Provider
  addProvider() {
    this.showGrid = false;
    this.pageName = 'Add';
    this.tripTicketProvisionalTime_hours = 0;
    this.tripTicketProvisionalTime_minutes = 0;
    this.provider = {};
    this.provider.providerAddress = {};
    this.provider.tripTicketProvisionalTime = new Date();
  }

  // to get LatLong from zipCode
  getLatLong(provider: any) {
    if (!provider.providerAddress.zipcode) {
      return;
    }

    // Pass the state code to the geocoding service if available
    const stateCode = provider.providerAddress.state || null;

    this.geocodingService
      .geocodeZipcode(provider.providerAddress.zipcode, 'us', stateCode)
      .subscribe({
        next: result => {
          // Format the coordinates to match the expected format (6 decimal places)
          const lat = parseFloat(result.lat).toFixed(6);
          const lon = parseFloat(result.lon).toFixed(6);
          this.close(lat, lon);
        },
        error: error => {
          console.warn('Geocoding failed:', error);
          this.notificationService.warn(
            'Geocoding Warning',
            'Could not automatically determine coordinates from zipcode. Using approximate coordinates based on state.'
          );
        },
      });
  }

  close(latitude: string, longitude: string) {
    this.provider.providerAddress.latitude = latitude;
    this.provider.providerAddress.longitude = longitude;
  }

  closer() {
    return this.provider.providerAddress.latitude;
  }

  // get Provider List
  getProviders() {
    // Add debug helper for breakpoint binding
    ensureBreakpointWorks('providers.component - getProviders');

    this.providerList = [];
    if (this.loggerRole === 'ROLE_PROVIDERADMIN') {
      this.providerService.get(this.localStorage.get('providerId')).subscribe({
        next: response => {
          this.providerList.push(response);
          this.providerListData = this.providerList;
          this.localStorage.set('providerName', response.providerName);
        },
        error: error => {
          this.notificationService.error('Error', error);
        },
      });
    } else if (this.loggerRole === 'ROLE_ADMIN') {
      this.providerService.query().subscribe({
        next: response => {
          this.providerListData = response;
        },
        error: error => {
          this.notificationService.error('Error', error);
        },
      });
    }
  }

  // edit Provider
  editProvider(providerObj) {
    console.log(providerObj);
    this.showGrid = false;
    this.pageName = 'Edit';
    this.editProviderIndex = this.findSelectedProviderIndex(providerObj);
    this.provider = this.providerListData[this.editProviderIndex];
    this.tripTicketProvisionalTime_hours = providerObj.tripTicketProvisionalTime.slice(0, 2);
    this.tripTicketProvisionalTime_minutes = providerObj.tripTicketProvisionalTime.slice(3, 5);
    const cdt = moment(providerObj.tripTicketExpirationTimeOfDay, 'HH:mm:ss');
    const dateTimeObj = cdt.toDate();
    providerObj.tripTicketExpirationTimeOfDay = dateTimeObj;
    this.editProviderObj = Object.assign({}, this.provider);
  }

  updateStatus(providerObj) {
    let status: any;
    if (providerObj.isActive === false) {
      status = '/activateProvider';
      this.confirmationService.confirm({
        header: 'Activate Provider',
        message: `Are you sure you want to activate this provider "${providerObj.providerName}"?`,
        icon: 'pi pi-exclamation-triangle',
        acceptLabel: 'Yes, Activate',
        rejectLabel: 'No, Cancel',
        accept: () => {
          providerObj.isActive = !providerObj.isActive;
          this.providerService.statusUpdate(providerObj.providerId, providerObj, status).subscribe({
            next: response => {
              this.notificationService.success(
                'Success',
                'Provider has been activated successfully.'
              );
            },
            error: error => {
              this.notificationService.error('Error', error);
            },
          });
        },
        reject: () => {
          // Action cancelled
        },
      });
    } else {
      status = '/deactivateProvider';
      this.confirmationService.confirm({
        header: 'Deactivate Provider',
        message: `Are you sure you want to deactivate provider "${providerObj.providerName}"?\n\nImportant: All users related to this provider will also be deactivated.`,
        icon: 'pi pi-exclamation-circle',
        acceptLabel: 'Yes, Deactivate',
        rejectLabel: 'No, Cancel',
        accept: () => {
          providerObj.isActive = !providerObj.isActive;
          this.providerService.statusUpdate(providerObj.providerId, providerObj, status).subscribe({
            next: response => {
              this.notificationService.success(
                'Success',
                'Provider has been deactivated successfully.'
              );
            },
            error: error => {
              this.notificationService.error('Error', error);
            },
          });
        },
        reject: () => {
          // Action cancelled
        },
      });
    }
  }

  // save Add/Edit Provider
  addEditProvider(tripTicketProvisionalTime_hours, tripTicketProvisionalTime_minutes) {
    let expirationTime: any;
    let time: any;
    this.provider.tripTicketProvisionalTime = (
      tripTicketProvisionalTime_hours.toString() +
      ':' +
      tripTicketProvisionalTime_minutes.toString() +
      ':' +
      '00'
    ).toString();
    if (typeof this.provider.tripTicketExpirationTimeOfDay === 'undefined') {
      const now = moment(new Date()).format('h:mm:ss');
      this.provider.tripTicketExpirationTimeOfDay = now;
    } else {
      this.provider.tripTicketExpirationTimeOfDay = moment(
        this.provider.tripTicketExpirationTimeOfDay
      ).format('h:mm:ss');
    }
    expirationTime = this.provider.tripTicketExpirationTimeOfDay;
    if (this.pageName === 'Add') {
      this.providerService.save(this.provider).subscribe(
        response => {
          this.providerListData.unshift(this.provider);
          this.notificationService.success('Success Message', 'Provider saved successfully.');
          this.showGrid = true;
          this.getProviders();
        },
        error => {
          this.notificationService.error('Error Message', error);
          this.showGrid = true;
        }
      );
    } else {
      this.providerService.update(this.provider.providerId, this.provider).subscribe(
        response => {
          this.showGrid = true;
          this.notificationService.success('Success Message', 'Provider updated successfully.');
          this.providerListData[this.editProviderIndex] = response;
        },
        error => {
          this.notificationService.error('Error Message', error);
          this.showGrid = true;
        }
      );
    }
  }
  // revert from edit page
  cancelEdit() {
    this.providerListData = [];
    this.showGrid = true;
    this.getProviders();
  }

  // to get the selected provider Index
  findSelectedProviderIndex(providerData): number {
    return this.providerListData.indexOf(providerData);
  }

  /**
   * Navigate to working hours management for a provider
   * @param provider The provider to manage working hours for
   */
  viewWorkingHours(provider: any): void {
    // Store provider info in localStorage for context in the working hours component
    if (this.loggerRole === 'ROLE_ADMIN') {
      this.localStorage.set('adminChoiceProvider', provider.providerId);
      this.localStorage.set('adminChoiceProviderName', provider.providerName);
    }

    // Navigate to the working hour component with the provider ID
    this.router.navigate([`/admin/working-hour/${provider.providerId}`]);
  }
}
