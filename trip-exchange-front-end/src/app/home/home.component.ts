import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError, finalize, map } from 'rxjs/operators';
import { LocalStorageService } from '../shared/service/local-storage.service';
import { TripTicketService } from '../trip-ticket/trip-ticket.service';

interface HomeStat {
  icon: string;
  tone: 'info' | 'pending' | 'ok' | 'danger';
  labelKey: string;
  value: number | string;
  delta: string;
}

interface HomeActivity {
  icon: string;
  tone: 'info' | 'pending' | 'ok' | 'danger';
  text: string;
  time: string;
}

interface DashboardSummary {
  availableToClaim: number;
  awaitingApproval: number;
  tripsToday: number;
  completedToday: number;
  upcomingToday: number;
  expiringSoon: number;
}

interface DashboardData {
  tickets: any[];
  totalTickets: number;
  partners: any[];
}

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css'],
})
export class HomeComponent implements OnInit {
  public readonly today = new Date();
  public readonly userName: string;
  public loading = true;
  public hasError = false;
  public dashboardLastUpdated = new Date();
  public activePartnerCount = 0;
  public totalTickets = 0;

  public stats: HomeStat[] = [
    {
      icon: 'ph-tray',
      tone: 'info',
      labelKey: 'HOME.STATS.AVAILABLE_TO_CLAIM',
      value: '...',
      delta: 'Loading trip data',
    },
    {
      icon: 'ph-hourglass',
      tone: 'pending',
      labelKey: 'HOME.STATS.AWAITING_APPROVAL',
      value: '...',
      delta: 'Loading claims',
    },
    {
      icon: 'ph-steering-wheel',
      tone: 'ok',
      labelKey: 'HOME.STATS.TRIPS_TODAY',
      value: '...',
      delta: 'Loading schedule',
    },
    {
      icon: 'ph-warning',
      tone: 'danger',
      labelKey: 'HOME.STATS.EXPIRING_SOON',
      value: '...',
      delta: 'Loading deadlines',
    },
  ];

  public activities: HomeActivity[] = [];

  constructor(
    private readonly router: Router,
    private readonly localStorage: LocalStorageService,
    private readonly tripTicketService: TripTicketService
  ) {
    this.userName = this.localStorage.get('name') || '';
  }

  public ngOnInit(): void {
    this.loadDashboard();
  }

  public navigateTo(route: string): void {
    this.router.navigate([route]);
  }

  public get greetingKey(): string {
    const hour = new Date().getHours();
    if (hour < 12) {
      return 'HOME.GREETING_MORNING';
    }
    if (hour < 18) {
      return 'HOME.GREETING_AFTERNOON';
    }
    return 'HOME.GREETING_EVENING';
  }

  public get networkStatusLabel(): string {
    if (this.loading) {
      return 'Checking';
    }

    return this.hasError ? 'Limited' : 'Online';
  }

  public get networkStatusClass(): string {
    return this.hasError ? 'status-pending' : 'status-active';
  }

  public get networkStatusCopy(): string {
    if (this.hasError) {
      return 'Some dashboard data could not be refreshed. Trip ticket actions are still available.';
    }

    if (this.activePartnerCount === 0) {
      return 'No approved partner providers were returned for your organization.';
    }

    const providerText = this.activePartnerCount === 1 ? 'partner provider' : 'partner providers';
    return `${this.activePartnerCount} approved ${providerText} available for network coordination.`;
  }

  private loadDashboard(): void {
    const providerId = this.getProviderId();
    const role = this.getRole();
    const tickets$ = this.getTickets(role).pipe(catchError(() => of(null)));
    const partners$ = providerId
      ? this.tripTicketService.getApprovedProviderPartner(providerId).pipe(catchError(() => of([])))
      : of([]);

    this.loading = true;
    this.hasError = false;

    forkJoin({ ticketsResponse: tickets$, partners: partners$ })
      .pipe(
        map(({ ticketsResponse, partners }) => this.normalizeDashboardData(ticketsResponse, partners)),
        finalize(() => {
          this.loading = false;
          this.dashboardLastUpdated = new Date();
        })
      )
      .subscribe({
        next: data => this.applyDashboardData(data),
        error: () => {
          this.hasError = true;
          this.applyDashboardData({ tickets: [], totalTickets: 0, partners: [] });
        },
      });
  }

  private getTickets(role: string) {
    if (role === 'ROLE_ADMIN' || role === 'ROLE_SUPERADMIN') {
      return this.tripTicketService.getAllTicketsList(1, 200, 'requested_pickup_date', 1);
    }

    return this.tripTicketService.getTicketsList(1, 200, 'requested_pickup_date', 1);
  }

  private normalizeDashboardData(ticketsResponse: any, partnersResponse: any): DashboardData {
    const tickets = this.extractArray(ticketsResponse, ['data', 'tickets', 'content', 'items', 'results']);
    const partners = this.extractArray(partnersResponse, ['data', 'providers', 'content', 'items', 'results']);
    const totalTickets =
      this.readNumber(ticketsResponse, ['total', 'totalCount', 'totalRecords', 'totalElements', 'count']) ?? tickets.length;

    if (!ticketsResponse) {
      this.hasError = true;
    }

    return { tickets, totalTickets, partners };
  }

  private applyDashboardData(data: DashboardData): void {
    const summary = this.buildSummary(data.tickets);
    this.totalTickets = data.totalTickets;
    this.activePartnerCount = data.partners.length;
    this.stats = this.buildStats(summary);
    this.activities = this.buildActivities(data.tickets);
  }

  private buildSummary(tickets: any[]): DashboardSummary {
    const now = new Date();
    const twoHoursFromNow = new Date(now.getTime() + 2 * 60 * 60 * 1000);
    const providerId = this.getProviderId();

    return tickets.reduce<DashboardSummary>(
      (summary, ticket) => {
        const status = this.getStatus(ticket);
        const pickupDate = this.getPickupDate(ticket);
        const isToday = pickupDate ? this.isSameDay(pickupDate, now) : false;
        const isCompleted = status === 'completed';
        const pendingClaims = this.getPendingClaims(ticket);

        if (status === 'available' && !this.isOwnOriginTicket(ticket, providerId)) {
          summary.availableToClaim += 1;
        }

        if (this.isOwnOriginTicket(ticket, providerId)) {
          summary.awaitingApproval += pendingClaims.length;
        }

        if (isToday) {
          summary.tripsToday += 1;
          if (isCompleted) {
            summary.completedToday += 1;
          } else if (pickupDate && pickupDate >= now) {
            summary.upcomingToday += 1;
          }
        }

        if (
          pickupDate &&
          pickupDate >= now &&
          pickupDate <= twoHoursFromNow &&
          !['cancelled', 'completed', 'expired', 'rescinded'].includes(status)
        ) {
          summary.expiringSoon += 1;
        }

        return summary;
      },
      {
        availableToClaim: 0,
        awaitingApproval: 0,
        tripsToday: 0,
        completedToday: 0,
        upcomingToday: 0,
        expiringSoon: 0,
      }
    );
  }

  private buildStats(summary: DashboardSummary): HomeStat[] {
    return [
      {
        icon: 'ph-tray',
        tone: 'info',
        labelKey: 'HOME.STATS.AVAILABLE_TO_CLAIM',
        value: summary.availableToClaim,
        delta: this.totalTickets > 0 ? `${this.totalTickets} total visible tickets` : 'No tickets returned',
      },
      {
        icon: 'ph-hourglass',
        tone: 'pending',
        labelKey: 'HOME.STATS.AWAITING_APPROVAL',
        value: summary.awaitingApproval,
        delta: summary.awaitingApproval === 1 ? 'pending claim on your ticket' : 'pending claims on your tickets',
      },
      {
        icon: 'ph-steering-wheel',
        tone: 'ok',
        labelKey: 'HOME.STATS.TRIPS_TODAY',
        value: summary.tripsToday,
        delta: `${summary.completedToday} completed - ${summary.upcomingToday} upcoming`,
      },
      {
        icon: 'ph-warning',
        tone: 'danger',
        labelKey: 'HOME.STATS.EXPIRING_SOON',
        value: summary.expiringSoon,
        delta: 'scheduled within 2 hours',
      },
    ];
  }

  private buildActivities(tickets: any[]): HomeActivity[] {
    const providerId = this.getProviderId();

    return [...tickets]
      .sort((a, b) => this.getActivityDate(b).getTime() - this.getActivityDate(a).getTime())
      .map(ticket => this.toActivity(ticket, providerId))
      .filter((activity): activity is HomeActivity => activity !== null)
      .slice(0, 5);
  }

  private toActivity(ticket: any, providerId: string): HomeActivity | null {
    const status = this.getStatus(ticket);
    const id = ticket?.id ?? ticket?.trip_ticket_id ?? ticket?.tripTicketId ?? 'unknown';
    const customerName = this.getCustomerName(ticket);
    const originator = ticket?.originator?.providerName || ticket?.origin_provider_name || ticket?.providerName || 'A provider';
    const pendingClaims = this.getPendingClaims(ticket);
    const date = this.getActivityDate(ticket);

    if (this.isOwnOriginTicket(ticket, providerId) && pendingClaims.length > 0) {
      const claimant = pendingClaims[0]?.claimant_provider_name || pendingClaims[0]?.claimantProviderName || 'A partner';
      return {
        icon: 'ph-hourglass',
        tone: 'pending',
        text: `${claimant} is awaiting approval on trip #${id}`,
        time: this.formatRelativeTime(date),
      };
    }

    if (status === 'completed') {
      return {
        icon: 'ph-flag-checkered',
        tone: 'ok',
        text: `Trip #${id} marked completed${customerName ? ' - ' + customerName : ''}`,
        time: this.formatRelativeTime(date),
      };
    }

    if (status === 'expired') {
      return {
        icon: 'ph-clock-countdown',
        tone: 'danger',
        text: `Trip #${id} expired${customerName ? ' - ' + customerName : ''}`,
        time: this.formatRelativeTime(date),
      };
    }

    if (status === 'available') {
      return {
        icon: 'ph-ticket',
        tone: 'info',
        text: `${originator} posted trip #${id}${customerName ? ' - ' + customerName : ''}`,
        time: this.formatRelativeTime(date),
      };
    }

    if (status === 'approved') {
      return {
        icon: 'ph-check-circle',
        tone: 'ok',
        text: `Trip #${id} was approved${customerName ? ' - ' + customerName : ''}`,
        time: this.formatRelativeTime(date),
      };
    }

    return {
      icon: 'ph-info',
      tone: 'info',
      text: `Trip #${id} status changed to ${this.titleCase(status || 'updated')}`,
      time: this.formatRelativeTime(date),
    };
  }

  private extractArray(source: any, keys: string[]): any[] {
    if (Array.isArray(source)) {
      return source;
    }

    if (!source || typeof source !== 'object') {
      return [];
    }

    for (const key of keys) {
      if (Array.isArray(source[key])) {
        return source[key];
      }
    }

    return [];
  }

  private readNumber(source: any, keys: string[]): number | null {
    if (!source || typeof source !== 'object') {
      return null;
    }

    for (const key of keys) {
      const value = Number(source[key]);
      if (Number.isFinite(value)) {
        return value;
      }
    }

    return null;
  }

  private getProviderId(): string {
    const providerId = this.localStorage.get('providerId');
    return providerId === null || providerId === undefined ? '' : String(providerId);
  }

  private getRole(): string {
    const role = this.localStorage.get('Role');
    return role === null || role === undefined ? '' : String(role);
  }

  private getStatus(ticket: any): string {
    return String(ticket?._status || ticket?.status?.type || ticket?.status || '').trim().toLowerCase();
  }

  private getPendingClaims(ticket: any): any[] {
    const claims = Array.isArray(ticket?.trip_Claims) ? ticket.trip_Claims : [];
    return claims.filter((claim: any) => String(claim?.status?.type || claim?.status || '').trim().toLowerCase() === 'pending');
  }

  private isOwnOriginTicket(ticket: any, providerId: string): boolean {
    if (!providerId) {
      return false;
    }

    const originProviderId =
      ticket?.origin_provider_id ?? ticket?.originProviderId ?? ticket?.originator?.providerId ?? ticket?.providerId;
    return String(originProviderId) === providerId;
  }

  private getCustomerName(ticket: any): string {
    const first = ticket?.customer_first_name || ticket?.customerFirstName || '';
    const last = ticket?.customer_last_name || ticket?.customerLastName || '';
    return `${first} ${last}`.trim();
  }

  private getPickupDate(ticket: any): Date | null {
    const date = ticket?.requested_pickup_date || ticket?.requestedPickupDate || ticket?._requested_pickupDateTime;
    const time = ticket?.requested_pickup_time || ticket?.requestedPickupTime || '';
    return this.parseDate(date, time);
  }

  private getActivityDate(ticket: any): Date {
    return (
      this.parseDate(ticket?.updated_at || ticket?.updatedAt) ||
      this.parseDate(ticket?.created_at || ticket?.createdAt) ||
      this.getPickupDate(ticket) ||
      new Date(0)
    );
  }

  private parseDate(dateValue: any, timeValue: any = ''): Date | null {
    if (!dateValue) {
      return null;
    }

    const raw = String(dateValue).trim();
    const time = String(timeValue || '').trim();
    const value = time && !raw.includes('T') ? `${raw}T${time}` : raw;
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? null : date;
  }

  private isSameDay(a: Date, b: Date): boolean {
    return a.getFullYear() === b.getFullYear() && a.getMonth() === b.getMonth() && a.getDate() === b.getDate();
  }

  private formatRelativeTime(date: Date): string {
    const timestamp = date.getTime();
    if (!Number.isFinite(timestamp) || timestamp <= 0) {
      return 'Recently updated';
    }

    const diffMs = Date.now() - timestamp;
    const absMs = Math.abs(diffMs);
    const minute = 60 * 1000;
    const hour = 60 * minute;
    const day = 24 * hour;

    if (absMs < minute) {
      return 'Just now';
    }

    if (absMs < hour) {
      const minutes = Math.round(absMs / minute);
      return diffMs >= 0 ? `${minutes} min ago` : `in ${minutes} min`;
    }

    if (absMs < day) {
      const hours = Math.round(absMs / hour);
      return diffMs >= 0 ? `${hours} hr ago` : `in ${hours} hr`;
    }

    const days = Math.round(absMs / day);
    return diffMs >= 0 ? `${days} days ago` : `in ${days} days`;
  }

  private titleCase(value: string): string {
    return value.replace(/\w\S*/g, text => text.charAt(0).toUpperCase() + text.slice(1).toLowerCase());
  }
}
