import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ConstantService } from '../shared/service/constant-service';
import { LocalStorageService } from '../shared/service/local-storage.service';
import { HeaderEmitterService } from '../shared/service/header-emmiter.service';
import { AdminEmitterService } from '../shared/service/admin-emitter.service';

@Component({
  selector: 'app-admin',
  templateUrl: 'admin.component.html',
  styleUrls: ['admin.component.scss'],
})
export class AdminComponent implements OnInit {
  public securedData: any = [];
  public role: string;
  public userName: boolean = false;
  constructor(
    public _localStorage: LocalStorageService,
    public _router: Router,
    public _headerEmitter: HeaderEmitterService,
    public _adminEmitter: AdminEmitterService
  ) {}

  ngOnInit() {
    this._headerEmitter.header.emit((this.userName = true));
    this.role = this._localStorage.get('Role');
  }
  checkUser() {
    if (this.role == 'ROLE_ADMIN') {
      this._router.navigate(['/admin/adminProviderPartners']);
      this._localStorage.set('choice', 'providerPartner');
      this._adminEmitter.header.emit((this.userName = true));
    } else {
      this._router.navigate(['/admin/providerPartners']);
    }
  }
  checkService() {
    if (this.role == 'ROLE_ADMIN') {
      this._router.navigate(['/admin/serviceArea']);
      this._localStorage.set('choice', 'service');
      this._adminEmitter.header.emit((this.userName = true));
    } else {
      // Add a timeout to ensure the DOM is ready before the component tries to access DOM elements
      this._router.navigate(['/admin/serviceArea']).then(() => {
        // Optional: You can emit an event here if needed to signal successful navigation
      });
    }
  }
}
