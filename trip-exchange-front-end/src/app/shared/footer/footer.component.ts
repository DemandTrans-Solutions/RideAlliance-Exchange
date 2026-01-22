import { Component } from '@angular/core';
import { GlobalService } from '../service/global.service';

@Component({
  selector: 'app-footer',
  templateUrl: './footer.component.html',
  styleUrls: ['./footer.component.scss'],
})
export class FooterComponent {
  public date = new Date();
  public year = this.date.getFullYear();
  public appVersion: string;

  constructor(public globalService: GlobalService) {
    this.appVersion = this.globalService.appVersion;
  }
}
