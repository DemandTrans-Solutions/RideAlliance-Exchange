// Declare the console as an ambient value so that TypeScript doesn't complain.
declare var console: any;

// Import the application components and services.
import { Injectable } from '@angular/core';
import { Logger, LogLevel } from './default-log.service';
import { GlobalService } from './global.service';
import { environment } from '../../../environments/environment';

/**
 * Console implementation of the logger that outputs to the browser console
 * with appropriate formatting and respects environment settings.
 */
@Injectable({
  providedIn: 'root',
})
export class ConsoleLogService extends Logger {
  private displayStatus: boolean;

  constructor(private _globalService: GlobalService) {
    super();
    this.displayStatus = environment.production ? false : this._globalService.LOGENABLE || true;
  }

  /**
   * Override error method from base Logger
   */
  public override error(...args: any[]): void {
    // Error messages are always displayed regardless of log level
    args[0] = this.formatMessage('ERROR', args[0]);
    console && console.error && console.error(...args);
  }

  /**
   * Override info method from base Logger
   */
  public override info(...args: any[]): void {
    if (this.shouldLog(LogLevel.INFO)) {
      args[0] = this.formatMessage('INFO', args[0]);
      console && console.info && console.info(...args);
    }
  }

  /**
   * Override log method from base Logger
   */
  public override log(...args: any[]): void {
    if (this.shouldLog(LogLevel.INFO)) {
      args[0] = this.formatMessage('LOG', args[0]);
      console && console.log && console.log(...args);
    }
  }

  /**
   * Override warn method from base Logger
   */
  public override warn(...args: any[]): void {
    if (this.shouldLog(LogLevel.WARN)) {
      args[0] = this.formatMessage('WARN', args[0]);
      console && console.warn && console.warn(...args);
    }
  }

  /**
   * Override debug method from base Logger
   */
  public override debug(...args: any[]): void {
    if (this.shouldLog(LogLevel.DEBUG)) {
      args[0] = this.formatMessage('DEBUG', args[0]);
      console && console.debug && console.debug(...args);
    }
  }

  /**
   * Format log messages with consistent timestamp and level indicators
   */
  private formatMessage(level: string, message: any): string {
    return `[${level}]: ${message}: ${new Date().toISOString()}`;
  }

  /**
   * Determine if a message should be logged based on configuration
   */
  private shouldLog(requiredLevel: LogLevel): boolean {
    return this.displayStatus && this.level >= requiredLevel;
  }
}
