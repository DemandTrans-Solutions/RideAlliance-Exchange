import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { Router } from '@angular/router';
import { ConstantService } from '../shared/service/constant-service';
import { SharedHttpClientService } from '../shared/service/shared-http-client.service';
import { environment } from '../../environments/environment';
import { FirebaseAuthService } from '../shared/service/firebase-auth.service';

@Injectable({
  providedIn: 'root',
})
export class ForgotPasswordService {
  private webServiceUrl = this.constantService.WEBSERVICE_URL + 'forgotCrendential/sendMail';
  private apiUrl = environment.apiUrl;

  constructor(
    private router: Router,
    private http: HttpClient,
    private constantService: ConstantService,
    private _sharedHttpClientService: SharedHttpClientService,
    private firebaseAuthService: FirebaseAuthService
  ) {}

  sendEmail(email: string): Observable<any> {
    // Use Firebase to send password reset email
    return this.firebaseAuthService.sendPasswordReset(email).pipe(
      map(() => ({ message: 'Password reset email sent' })),
      catchError((err) => {
        // reuse shared error handler shape
        return this._sharedHttpClientService.handleError(err);
      })
    );
  }

  getResetPasswordLink(email: string): Observable<any> {
    return this.http.post(`${this.apiUrl}auth/reset-password-link`, { email });
  }
}
