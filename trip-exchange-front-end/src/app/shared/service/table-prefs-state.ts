import { TablePreferencesService } from './table-preferences.service';

/**
 * Small composition helper that holds a client-side PrimeNG table's persisted
 * page-size and sort state for a single table, identified by `tableId`.
 *
 * Usage in a component:
 *   prefs = new TablePrefsState(this._tablePreferences, 'report:new-trip-ticket');
 *   // template:
 *   //   [rows]="prefs.rows"
 *   //   [sortField]="prefs.sortField"
 *   //   [sortOrder]="prefs.sortOrder ?? 1"
 *   //   (onPage)="prefs.onPage($event)"
 *   //   (onSort)="prefs.onSort($event)"
 *
 * Each table passes its own unique tableId so preferences are remembered
 * per-report (not shared across reports).
 */
export class TablePrefsState {
  rows: number = 10;
  sortField: string | undefined;
  sortOrder: number | undefined;

  constructor(
    private prefs: TablePreferencesService,
    private tableId: string,
    defaultRows: number = 10
  ) {
    this.rows = defaultRows;
    const saved = this.prefs.load(this.tableId);
    if (saved.rows) this.rows = saved.rows;
    if (saved.sortField) this.sortField = saved.sortField;
    if (saved.sortOrder) this.sortOrder = saved.sortOrder;
  }

  /** Wire to PrimeNG (onPage) — fires on page-size and page-index changes. */
  onPage(event: { rows?: number }): void {
    if (typeof event.rows === 'number') {
      this.rows = event.rows;
      this.prefs.save(this.tableId, { rows: this.rows });
    }
  }

  /** Wire to PrimeNG (onSort) — fires when a sortable column header is clicked. */
  onSort(event: { field?: string; order?: number }): void {
    this.sortField = event.field;
    this.sortOrder = event.order;
    this.prefs.save(this.tableId, {
      sortField: this.sortField,
      sortOrder: this.sortOrder,
    });
  }
}
