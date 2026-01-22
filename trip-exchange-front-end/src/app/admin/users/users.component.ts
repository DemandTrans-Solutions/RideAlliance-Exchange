import { Component, OnInit, Input, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { Message } from 'primeng/api';
import { ConfirmationService } from 'primeng/api';
import { UserService } from './users.service';
import { TokenService } from '../../shared/service/token.service';
import { ListService } from '../../shared/service/list.service';
import { NotificationEmitterService } from '../../shared/service/notification-emitter.service';
import { LocalStorageService } from '../../shared/service/local-storage.service';
import { User } from './user';
import { NgForm } from '@angular/forms';
import { FirebaseAuthService } from '../../shared/service/firebase-auth.service';

@Component({
  selector: 'app-users',
  templateUrl: 'users.component.html',
  styleUrls: ['users.component.scss'],
  providers: [UserService, ListService],
})
export class UsersComponent implements OnInit {
  @Input() user: User = new User();
  @ViewChild('userForm') userForm!: NgForm;

  public data: any;
  public userListData: any;
  showGrid: boolean = true;
  height: boolean = true;
  pageName: string = '';
  editUserIndex: number = -1;
  editUserObj: any;
  public providersList: any;
  public rolesList: any;
  public userId: number = 0;
  public mask = ['(', /[1-9]/, /\d/, /\d/, ')', ' ', /\d/, /\d/, /\d/, '-', /\d/, /\d/, /\d/, /\d/];
  public msgs: Message[] = [];
  role: any;

  constructor(
    public _userService: UserService,
    public _listService: ListService,
    public _notificationService: NotificationEmitterService,
    public _confirmationService: ConfirmationService,
    public _tokenService: TokenService,
    public _router: Router,
    public _localStorage: LocalStorageService
    ,
    public _firebaseAuth: FirebaseAuthService
  ) {}

  ngOnInit() {
    if (typeof this._tokenService.get() == 'undefined') {
      this._router.navigate(['/login']);
    }
    this.role = this._localStorage.get('Role');
    if (screen.width > 1024) {
      this.height = false;
    }
    this.getUsers();
  }

  // get users list
  getUsers() {
    this._userService.query().subscribe(
      response => {
        this.userListData = response;
      },
      (_error: any) => {
        this._notificationService.error('error', _error);
      }
    );
  }

  // add user
  addUser() {
    this.userId = 0;
    this.showGrid = false;
    this.pageName = 'Add';
    this.setLists();
    this.user = new User();
  }

  // edit user
  editUser(userObj: User) {
    this.showGrid = false;
    this.pageName = 'Edit';
    this.setLists();
    this.editUserIndex = this.findSelectedUserIndex(userObj);
    this.user = this.userListData[this.editUserIndex];
    this.editUserObj = Object.assign({}, this.user);
  }

  findSelectedUserIndex(userData: User): number {
    return this.userListData.indexOf(userData);
  }

  // update user status activate/deactivate
  updateStatus(userObj: User) {
    let status: any;
    if (userObj.isActive == false) {
      status = '/activate';
      this._confirmationService.confirm({
        message: 'Do you want to Activate user ?',
        accept: () => {
          userObj.isActive = !userObj.isActive;
          this._userService.statusUpdate(userObj.id, userObj, status).subscribe(
            (_response: any) => {
              this._notificationService.success('Success Message', 'Status updated successfully.');
            },
            (_error: any) => {
              this._notificationService.error('error', _error);
            }
          );
        },
        reject: () => {},
      });
    } else {
      status = '/deactivate';
      this._confirmationService.confirm({
        message:
          'Do you want to Deactivate user ?  User cannot login to application once deactivated.',
        accept: () => {
          userObj.isActive = !userObj.isActive;
          this._userService.statusUpdate(userObj.id, userObj, status).subscribe(
            (_response: any) => {
              this._notificationService.success('Success Message', 'Status updated successfully.');
            },
            (_error: any) => {
              this._notificationService.error('error', _error);
            }
          );
        },
        reject: () => {},
      });
    }
  }

  setLists() {
    // If provider list already exist
    if (!this.providersList) {
      this._listService.queryProvider().subscribe(
        response => {
          this.providersList = response;
        },
        (_error) => {
          this._notificationService.error('error', _error);
        }
      );
    }
    // If role list already exist
    if (!this.rolesList) {
      this._listService.queryRoles().subscribe(
        response => {
          this.rolesList = response;
        },
        (_error) => {
          this._notificationService.error('error', _error);
        }
      );
    }
  }

  // TO add and edit User
  addEditUser(_userObj: any) {
    if (this.pageName == 'Add') {
      this._userService.save(this.user).subscribe(
        (_response: any) => {
            for (let i = 0, iLen = this.providersList.length; i < iLen; i++) {
              if (this.providersList[i].providerId == _response.providerId) {
                _response.providerName = this.providersList[i].providerName;
              }
            }
            this.userListData.unshift(_response);

            // Create Firebase auth user for email/password sign-in
            const tempPassword = this.generateTempPassword();
            this._firebaseAuth.createUserWithEmailAndPassword(_response.email, tempPassword).subscribe({
            next: (_firebaseUser) => {
              this._notificationService.success(
                'Success Message',
                'User saved successfully and email is sent to your mail box. Please activate the account.'
              );
              this.showGrid = true;
            },
            error: (err) => {
              // If Firebase creation fails, warn but keep the backend user
              this._notificationService.warn('Warning', 'User created in authorization backend but failed to create Firebase account: ' + (err && err.message ? err.message : err));
              this.showGrid = true;
            }
          });
        },
        (_error: any) => {
          this._notificationService.error('error', _error);
          this.userId = 0;
          this.showGrid = true;
        }
      );
    } else {
      this._userService.update(this.user.id, this.user).subscribe(
      (_response: any) => {
        this.userListData[this.editUserIndex] = _response;
          this._notificationService.success('Success Message', 'user updated successfully.');
          this.showGrid = true;
        },
        (_error: any) => {
          this._notificationService.error('error', _error);
          this.userId = 0;
          this.showGrid = true;
        }
      );
    }
  }

  // revert from edit page
  cancelEdit() {
    this.showGrid = true;
    this.user = new User();
    if (this.pageName == 'Edit') {
      this.userListData[this.editUserIndex] = this.editUserObj;
    }
    // this.getUsers();
  }

  // generate a temporary password with required complexity
  private generateTempPassword(): string {
    const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_+[]{}|;:,.<>?';
    let pw = '';
    for (let i = 0; i < 12; i++) {
      pw += chars.charAt(Math.floor(Math.random() * chars.length));
    }
    return pw;
  }

  // Keys for notification checkbox properties on the User model
  notificationKeys: string[] = [
    'isNotifyPartnerCreatesTicket',
    'isNotifyPartnerUpdateTicket',
    'isNotifyClaimedTicketRescinded',
    'isNotifyClaimedTicketExpired',
    'isNotifyNewTripClaimAwaitingApproval',
    'isNotifyNewTripClaimAutoApproved',
    'isNotifyTripClaimApproved',
    'isNotifyTripClaimDeclined',
    'isNotifyTripClaimRescinded',
    'isNotifyTripCommentAdded',
    'isNotifyTripResultSubmitted',
    'isNotifyTripReceived',
    'isNotifyTripCancelled',
    'isNotifyTripExpired',
    'isNotifyTripWeeklyReport',
    'isNotifyTripClaimCancelled',
    'isNotifyTripPriceMismatched'
  ];

  // Compute whether all notification checkboxes are currently selected
  get notifyAllSelected(): boolean {
    if (!this.user) return false;
    return this.notificationKeys.every(k => !!(this.user as any)[k]);
  }

  // Toggle all notification checkboxes on/off
  toggleNotifications(): void {
    const newVal = !this.notifyAllSelected;
    this.notificationKeys.forEach(k => {
      (this.user as any)[k] = newVal;
    });
  }

  // Add missing filterGlobal method
  filterGlobal(_event: any) {
    // Implementation for global filtering
    // const value = event.target.value; // unused: table global filtering will be handled by PrimeNG
    // Table global filtering will be handled by PrimeNG
  }

  // Check if form is valid - simplified logic
  isFormValid(): boolean {
    // If form isn't available yet, return false
    if (!this.userForm) return false;

    // Check if all required fields have values
    return (
      !!this.user.email &&
      !!this.user.name &&
      !!this.user.jobTitle &&
      !!this.user.phoneNumber &&
      (this.pageName === 'Edit' || !!this.user.providerId) && // Only check providerId for Add mode
      !!this.user.userRole
    );
  }
}
