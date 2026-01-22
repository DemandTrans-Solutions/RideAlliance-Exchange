import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ConstantService } from '../shared/service/constant-service';
import { LocalStorageService } from '../shared/service/local-storage.service';
import { HeaderEmitterService } from '../shared/service/header-emmiter.service';

@Component({
  selector: 'app-reports',
  templateUrl: 'reports.component.html',
  styleUrls: ['reports.component.scss'],
})
export class ReportsComponent implements OnInit {
  public userName: boolean = false;
  constructor(public _headerEmitter: HeaderEmitterService) {}
  ngOnInit() {
    this._headerEmitter.header.emit((this.userName = true));
  }
}
