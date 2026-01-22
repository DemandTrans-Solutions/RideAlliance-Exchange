import { Routes } from '@angular/router';
import { HomeComponent } from './home/home.component';
import { ProfileComponent } from './profile/profile.component';
import { ApplicationSettingsComponent } from './application-settings/application-settings.component';
import { AuthGuard } from './shared/guard/auth-guard.service';

export const AppRoutes: Routes = [
  { path: '', redirectTo: '/login', pathMatch: 'full' },
  { path: '404', redirectTo: '/login', pathMatch: 'full' },

  // Auth module - lazy loaded
  {
    path: '',
    loadChildren: () => import('./auth/auth.module').then(m => m.AuthModule),
  },

  // Profile and settings
  {
    path: 'profile',
    component: ProfileComponent,
    canActivate: [AuthGuard],
  },
  {
    path: 'applicationSettingsComponent',
    component: ApplicationSettingsComponent,
    canActivate: [AuthGuard],
  },

  // Trip Ticket module - lazy loaded
  {
    path: 'tripTicket',
    loadChildren: () => import('./trip-ticket/trip-ticket.module').then(m => m.TripTicketModule),
    canActivate: [AuthGuard],
  },

  // Admin module - lazy loaded
  {
    path: 'admin',
    loadChildren: () => import('./admin/admin.module').then(m => m.AdminModule),
    canActivate: [AuthGuard],
  },

  // Reports module - lazy loaded
  {
    path: 'reports',
    loadChildren: () => import('./reports/reports.module').then(m => m.ReportsModule),
    canActivate: [AuthGuard],
  },

  // Wildcard route for 404
  { path: '**', redirectTo: '/login' },
];
