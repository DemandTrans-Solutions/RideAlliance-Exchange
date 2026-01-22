// This file can be replaced during build by using the `fileReplacements` array.
// `ng build --configuration production` replaces `environment.ts` with `environment.prod.ts`.
// The list of file replacements can be found in `angular.json`.

import { firebaseConfig } from './firebase.config';

export const environment = {
  production: false,
  //apiUrl: 'https://tripexchange.demandtrans-apis.com/api/',
  apiUrl: 'http://localhost:8080/api/',
  apiTimeout: 30000,
  version: '1.0.0',
  logLevel: 'debug', // 'debug' | 'info' | 'warn' | 'error'
  enableDebugTools: true,
  mockApiCalls: false,
  appCheckRecaptchaSiteKey: '6LeidMcrAAAAAEc1ZsmMOi7xzE6hGByncH1h9OkA', // reCAPTCHA v3 key for App Check
  firebase: firebaseConfig,
  // URL to redirect user to after completing password reset in Firebase email
  passwordResetRedirectUrl: 'http://localhost:4200/reset-password',
  // Base64-encoded shared AES key used to encrypt email for login/validateUser.
  // Leave empty to use Base64(plaintext) fallback compatible with server.
  loginValidateSharedKey: 'SXQ3Ts2EWvmWTWYWDW0VGXg2gokvo6STgy75Riv251c=',
};
