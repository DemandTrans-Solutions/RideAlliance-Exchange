import { Component, OnInit, Input, ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { Message } from 'primeng/api';
import { ConfirmationService } from 'primeng/api';
import { ProviderPartnersService } from './provider-partners.service';
import { TokenService } from '../../shared/service/token.service';
import { ListService } from '../../shared/service/list.service';
import { NotificationEmitterService } from '../../shared/service/notification-emitter.service';
import { LocalStorageService } from '../../shared/service/local-storage.service';
import { AdminEmitterService } from '../../shared/service/admin-emitter.service';
// Logger import removed (unused)
import { ProviderPartners } from './provider-partners';

@Component({
  selector: 'app-users',
  templateUrl: 'provider-partners.component.html',
  styleUrls: ['provider-partners.component.scss'],
  providers: [ProviderPartnersService, ListService],
})
export class ProviderPartnersComponent implements OnInit {
  @Input() providerPartners: ProviderPartners = new ProviderPartners();
  public userListData: any;
  public showGrid: boolean = true;
  public edit: boolean = false;
  public cancel: boolean = false;
  public endPartnership: boolean = false;
  public editPartnership: boolean = false;
  public approvedPartnership: boolean = false;
  public declinedPartnership: boolean = false;
  public pageName: string = '';
  public editUserIndex: number | undefined = undefined;
  public editUserObj: any;
  public editProviderObj: any;
  public providerPartner: any;
  public providersList: any;
  public allProvidersList: any; // Complete list of providers for dropdown
  public rolesList: any;
  public userId: number | undefined = undefined;
  public mask = ['(', /[1-9]/, /\d/, /\d/, ')', ' ', /\d/, /\d/, /\d/, '-', /\d/, /\d/, /\d/, /\d/];
  public msgs: Message[] = [];
  public editedproviderPartnerId: number | undefined = undefined;
  public array: Array<any>;
  public multiple: string = 'multiple';
  public providerPartnersId: any; // Selected provider ID from dropdown

  constructor(
    private _router: Router,
    private _providerPartnersService: ProviderPartnersService,
    private _listService: ListService,
    private _localStorage: LocalStorageService,
    private _notificationService: NotificationEmitterService,
    private _confirmationService: ConfirmationService,
    private _tokenService: TokenService,
    private _adminEmitter: AdminEmitterService,
    private _cd: ChangeDetectorRef
  ) {
    this.array = [];
    this.userListData = [];
    this._adminEmitter.header.subscribe(msg => this.admin(msg));
  }

  /**
   * Update the local userListData with the modified partner object so the template updates immediately.
   * If an exact match by providerPartnerId is found, merge the updated fields; otherwise, prepend it.
   */
  updateLocalPartner(updatedPartner: any) {
    if (!updatedPartner) return;
    if (!this.userListData || !Array.isArray(this.userListData)) {
      this.userListData = [updatedPartner];
      this._cd.detectChanges();
      return;
    }

    const idx = this.userListData.findIndex(
      (p: any) => p.providerPartnerId === updatedPartner.providerPartnerId
    );
    if (idx > -1) {
      // Merge to preserve any properties the server doesn't return
      this.userListData[idx] = Object.assign({}, this.userListData[idx], updatedPartner);
    } else {
      // If not present (new item), add to the top of the list
      this.userListData.unshift(updatedPartner);
    }

    // Trigger change detection so the DOM updates immediately
    try {
      this._cd.detectChanges();
    } catch (e) {
      // ignore
    }
  }
  public providerId = this._localStorage.get('providerId');
  public providerPartnersName = this._localStorage.get('adminChoiceProviderName');
  public role = this._localStorage.get('Role');

  ngOnInit() {
    if (typeof this._tokenService.get() == 'undefined') {
      this._router.navigate(['/login']);
    }

    // Load all providers for the dropdown
    this.loadAllProviders();


    // For admin role, check if there is a saved provider selection
    if (this.role === 'ROLE_ADMIN') {
      const savedProviderId = this._localStorage.get('adminChoiceProvider');
      if (savedProviderId) {
        this.providerPartnersId = savedProviderId;
        this.providerPartnersName = this._localStorage.get('adminChoiceProviderName');
      }
    }
    this.getProviderPartner();
    if ( this.role === 'ROLE_ADMIN') {
      this.setAdminLists();
    } else {
      this.setLists();
    }
  }

  admin(_msg: any) {
    // Handle any admin-related messages
  }

  // Load all providers for the dropdown
  loadAllProviders() {
    this._listService.queryProvider().subscribe(
      response => {
        this.allProvidersList = response;
      },
      error => {
        this._notificationService.error('Error Message', error);
      }
    );
  }




  // Load provider partner data based on selected provider
  loadProviderPartnerData(providerId: any) {
    if (!providerId) return;

    // Find provider name from the list
    let providerName = '';
    for (let i = 0; i < this.allProvidersList.length; i++) {
      if (this.allProvidersList[i].providerId == providerId) {
        providerName = this.allProvidersList[i].providerName;
        break;
      }
    }

    // Save the selected provider to local storage
    this._localStorage.set('adminChoiceProvider', providerId);
    this._localStorage.set('adminChoiceProviderName', providerName);
    this.providerPartnersName = providerName;

    // Reload the provider partner data
    this.getProviderPartner();
  }

  // To add ProviderPartner
  addProviderPartner() {
    this.showGrid = false;
    this.pageName = 'Add';
    this.providerPartners = new ProviderPartners();
  }
  // Get Provider Partner list
  getProviderPartner() {
    this._providerPartnersService.query().subscribe(
      response => {
        this.userListData = Array.isArray(response) ? response : [];

        if (this.userListData.length > 0) {
          const role = this._localStorage.get('Role');
          for (let i = 0, iLen = this.userListData.length; i < iLen; i++) {
            if (this.userListData[i].requestStatus.status == 'BreakPartnership') {
              this.userListData[i].requestStatus.status = 'Dissolved';
            }
            if (role == 'ROLE_PROVIDERADMIN') {
              this.userListData[i].Login = this.providerId;
            } else if (role == 'ROLE_ADMIN') {
              this.userListData[i].Login = this._localStorage.get('adminChoiceProvider');
            }
          }
        }
      },
      _error => {
        this.userListData = [];
      }
    );
  }

  setAdminLists() {
    // We need the list of providers for the coordinator/provider dropdown.
    // Using queryProviderPartner() required adminChoiceProvider to be set which
    // resulted in an empty dropdown for ROLE_ADMIN users who hadn't selected a
    // provider yet. Querying providers returns the full list (or filtered by
    // providerId for non-admin roles) and avoids the dependency on
    // adminChoiceProvider when first opening the page.
    this._listService.queryProvider().subscribe(
      response => {
        this.providersList = response;
      },
      error => {
        this._notificationService.error('Error Message', error);
      }
    );
  }


  setLists() {
    this._listService.queryProviderPartner().subscribe(
      response => {
        this.providersList = response;
      },
      error => {
        this._notificationService.error('Error Message', error);
      }
    );
  }

  // save Add/Edited Provider-Partners
  addEditProviderPartner(providerPartnersObj: ProviderPartners) {
    if (this.pageName == 'Add') {
      this.providerPartners = providerPartnersObj;
      if (this.role == 'ROLE_ADMIN') {
        this.providerPartners.requesterProviderId = this._localStorage.get('adminChoiceProvider');
        this.providerPartners.requesterProviderName = this.providerPartnersName;
      } else if (this.role == 'ROLE_PROVIDERADMIN') {
        this.providerPartners.requesterProviderId = this._localStorage.get('providerId');
        this.providerPartners.requesterProviderName = this._localStorage.get('providerName');
      }
      this.providerPartners.isTrustedPartnerForCoordinator = false;
      this.providerPartners.isTrustedPartnerForRequester = false;
      for (let i = 0, iLen = this.providersList.length; i < iLen; i++) {
        if (this.providerPartners.coordinatorProviderId == this.providersList[i].providerId) {
          this.providerPartners.coordinatorProviderName = this.providersList[i].providerName;
        }
      }
      this._providerPartnersService.save(this.providerPartners).subscribe(
        response => {
          this._notificationService.success('Success Message', 'Partnership sent successfully');
          this.showGrid = true;
          if (this.role == 'ROLE_ADMIN') {
            response.Login = this._localStorage.get('adminChoiceProvider');
          } else if (this.role == 'ROLE_PROVIDERADMIN') {
            response.Login = this._localStorage.get('providerId');
          }
          response.requestStatus.status = 'Pending';
          if (typeof this.userListData == 'undefined') {
            this.userListData = [];
            this.array.push(response);
            this.userListData = this.array;
          } else {
            this.userListData.unshift(response);
          }
        },
        _error => {
          this._notificationService.error('Error Message', 'You are already partners');
        }
      );
    } else {
      this._providerPartnersService
        .update(providerPartnersObj.providerPartnerId, providerPartnersObj)
        .subscribe(
          response => {
            this._notificationService.success('Success Message', 'Status updated successfully');
            this.showGrid = true;
            this.updateLocalPartner(response || providerPartnersObj);
          },
          _error => {}
        );
    }
  }

  cancelEdit() {
    this.showGrid = true;
    if (this.pageName == 'Edit') {
      if (typeof this.editUserIndex !== 'undefined' && this.userListData && this.userListData.length > this.editUserIndex) {
        this.userListData[this.editUserIndex] = this.editUserObj;
      }
    }
  }

  // status changed to end Partnership
  statusUpdateEndPartnership(providerPartners: ProviderPartners) {
    this._confirmationService.confirm({
      message: 'Do you want to End Partnership ?',
      accept: () => {
        providerPartners.requestStatus.status = 'Dissolved';
        providerPartners.requestStatus.providerPartnerStatusId = 5;
        this._providerPartnersService
          .update(providerPartners.providerPartnerId, providerPartners)
          .subscribe(
            response => {
              this._notificationService.success('Success Message', 'Status updated successfully');
              this.updateLocalPartner(response || providerPartners);
              this.getProviderPartner();
            },
            _error => {}
          );
      },
      reject: () => {},
    });
  }

  // status changed to cancell Partnership
  statusUpdateCancelled(providerPartners: any) {
    this._confirmationService.confirm({
      message: 'Do you want to Cancel Partnership ?',
      accept: () => {
        providerPartners.requestStatus.status = 'Cancelled';
        providerPartners.requestStatus.providerPartnerStatusId = 3;
        this._providerPartnersService
          .update(providerPartners.providerPartnerId, providerPartners)
          .subscribe(
            response => {
              this._notificationService.success('Success Message', 'Status updated successfully');
              this.updateLocalPartner(response || providerPartners);
            },
            _error => {}
          );
      },
      reject: () => {},
    });
  }

  // status changed to Approved Partnership
  statusUpdateApproved(providerPartners: any) {
    this._confirmationService.confirm({
      message: 'Do you want to Approve Partnership ?',
      accept: () => {
        providerPartners.requestStatus.status = 'Approved';
        providerPartners.requestStatus.providerPartnerStatusId = 2;
        this._providerPartnersService
          .update(providerPartners.providerPartnerId, providerPartners)
          .subscribe(
            response => {
              this._notificationService.success('Success Message', 'Status updated successfully');
              this.updateLocalPartner(response || providerPartners);
              this.getProviderPartner();
            },
            _error => {}
          );
      },
      reject: () => {},
    });
  }

  // status changed to Decline Partnership
  statusUpdateDeclined(providerPartners: any) {
    this._confirmationService.confirm({
      message: 'Do you want to Decline Partnership ?',
      accept: () => {
        providerPartners.requestStatus.status = 'Declined';
        providerPartners.requestStatus.providerPartnerStatusId = 4;
        this._providerPartnersService
          .update(providerPartners.providerPartnerId, providerPartners)
          .subscribe(
            response => {
              this._notificationService.success('Success Message', 'Status updated successfully');
              this.updateLocalPartner(response || providerPartners);
            },
            _error => {}
          );
      },
      reject: () => {},
    });
  }

  trustedStatus(providerPartner: any) {
    this._providerPartnersService
      .update(providerPartner.providerPartnerId, providerPartner)
      .subscribe(
        response => {
          this._notificationService.success('Success Message', 'Status updated successfully');
          this.updateLocalPartner(response || providerPartner);
          this.showGrid = true;
        },
        _error => {}
      );
  }

  // Edit Provider Partner
  editProviderPartners(i: number, providerPartners: any) {
    // Add debug logging
    console.log('Edit Provider Partners clicked, index:', i);
    console.log('Provider Partner object:', JSON.stringify(providerPartners, null, 2));

    this._confirmationService.confirm({
      message: 'Do you want to Edit Partnership ?',
      accept: () => {
        this.edit = true;
        this.pageName = 'Edit';
        this.showGrid = false;

        // More detailed logging inside the confirm dialog
        console.log('Confirmation accepted');
        console.log('Login ID:', providerPartners.Login);
        console.log('Requester Provider ID:', providerPartners.requesterProviderId);
        console.log('Coordinator Provider ID:', providerPartners.coordinatorProviderId);

        if (providerPartners.Login == providerPartners.requesterProviderId) {
          providerPartners.name = providerPartners.coordinatorProviderName;
          console.log('Setting name to coordinator:', providerPartners.name);
        } else {
          providerPartners.name = providerPartners.requesterProviderName;
          console.log('Setting name to requester:', providerPartners.name);
        }

        // Ensure we have a proper object with all required properties before assignment
        this.providerPartner = { ...providerPartners };
        this.editProviderObj = Object.assign({}, this.providerPartner);

        // Log the final object being used for editing
        console.log(
          'Final provider partner object for editing:',
          JSON.stringify(this.providerPartner, null, 2)
        );

        // Force change detection if needed
        setTimeout(() => {
          console.log(
            'Edit form should be visible now, pageName:',
            this.pageName,
            'showGrid:',
            this.showGrid
          );
        }, 0);
      },
      reject: () => {
        console.log('Edit confirmation rejected');
      },
    });
  }

  // reverted to Admin Provider Partner
  back() {
    this._router.navigate(['/admin/adminProviderPartners']);
  }

  // Method for global filtering in PrimeNG table
  filterGlobal(event: any) {
    // Implementation for global filtering
    const _value = event.target.value;
    // Table global filtering will be handled by PrimeNG
  }

  // Method to determine CSS class based on status
  getStatusClass(status: string): string {
    switch (status.toLowerCase()) {
      case 'approved':
        return 'status-positive';
      case 'pending':
        return 'status-pending';
      case 'declined':
      case 'cancelled':
        return 'status-negative';
      default:
        return 'status-neutral';
    }
  }
}
