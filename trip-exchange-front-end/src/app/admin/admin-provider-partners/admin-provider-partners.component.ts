import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Message } from 'primeng/api';
import { Logger } from '../../shared/service/default-log.service';
import { AdminProviderPartnersService } from './admin-provider-partners.service';
import { TokenService } from '../../shared/service/token.service';
import { NotificationEmitterService } from '../../shared/service/notification-emitter.service';
import { LocalStorageService } from '../../shared/service/local-storage.service';

import { ListService } from '../../shared/service/list.service';
import { AdminEmitterService } from '../../shared/service/admin-emitter.service';

@Component({
  selector: 'app-users',
  templateUrl: 'admin-provider-partners.component.html',
  styleUrls: ['admin-provider-partners.component.scss'],
  providers: [AdminProviderPartnersService, ListService],
})
export class AdminProviderPartnersComponent implements OnInit {
  providersList: any;
  adminProviderPartner: boolean = false;
  choice: any;
  providerPartnersId: any;

  constructor(
    public _router: Router,
    public _adminProviderPartnersService: AdminProviderPartnersService,
    public _listService: ListService,
    public _localStorage: LocalStorageService,
    public _notificationService: NotificationEmitterService,
    public _adminEmitter: AdminEmitterService,
    public _tokenService: TokenService
  ) {
    this._adminEmitter.header.subscribe(msg => this.admin(msg));
  }
  ngOnInit() {
    if (typeof this._tokenService.get() == 'undefined') {
      this._router.navigate(['/login']);
    }
    this.choice = this._localStorage.get('choice');
    this._listService.queryProvider().subscribe(
      response => {
        this.providersList = response;
      },
      error => {}
    );
  }

  admin(msg) {
    this.choice = this._localStorage.get('choice');
  }

  //select and go to  provider vise provider-partners through site admin
  searchProviderPartner(providerPartnersId) {
    let providerPartnersName: any;
    for (let i = 0, iLen = this.providersList.length; i < iLen; i++) {
      if (this.providersList[i].providerId == providerPartnersId) {
        providerPartnersName = this.providersList[i].providerName;
      }
    }
    this._localStorage.set('adminChoiceProvider', providerPartnersId);
    this._localStorage.set('adminChoiceProviderName', providerPartnersName);
    if (this._localStorage.get('choice') == 'providerPartner') {
      this._router.navigate(['/admin/providerPartners']);
    } else if (this._localStorage.get('choice') == 'service') {
      this._router.navigate(['/admin/serviceArea']);
    }
  }
}
