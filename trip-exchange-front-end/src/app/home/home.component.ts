import { Component } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { Router } from '@angular/router';
import { NotificationEmitterService } from '../shared/service/notification-emitter.service';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css'],
})
export class HomeComponent {
  public title: string = 'home page works!';
  public mydate: Date = new Date();
  public myamount: number = 1000000;

  constructor(public _notificationEmitterService: NotificationEmitterService) {
    this._notificationEmitterService.error('Success Message', 'Provider saved successfully.');
  }
}
