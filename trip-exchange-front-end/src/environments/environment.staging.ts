import { firebaseConfig } from './firebase.config';

export const environment = {
  production: false,
  //apiUrl: 'https://tripexchange.demandtrans-apis.com/api/',
  apiUrl: 'https://tripexchange.demandtrans-apis.com/api/',
  apiTimeout: 45000,
  version: '1.0.0',
  logLevel: 'warn', // Only log warnings and errors in staging
  enableDebugTools: true, // Enable debugging in staging
  mockApiCalls: false,
  firebase: firebaseConfig,
};
