import { Injectable } from '@angular/core';
import { environment } from '../../../environments/environment';

// Define the interface that all loggers must implement
export interface ILogger {
  error(...args: any[]): void;
  info(...args: any[]): void;
  log(...args: any[]): void;
  warn(...args: any[]): void;
  debug(...args: any[]): void;
}

// LogLevel enumeration for different logging levels
export enum LogLevel {
  OFF = 0,
  ERROR = 1,
  WARN = 2,
  INFO = 3,
  DEBUG = 4,
}

/**
 * Default logger that can be used as a base implementation or as a dependency injection token.
 * Each platform can then override this with a platform-specific logger implementation.
 */
@Injectable({
  providedIn: 'root',
})
export class Logger implements ILogger {
  // Get the current log level from environment
  protected level: LogLevel = this.getLogLevel();

  constructor() {}

  public error(...args: any[]): void {
    if (this.level >= LogLevel.ERROR) {
      this.logInternal(console.error, args);
    }
  }

  public info(...args: any[]): void {
    if (this.level >= LogLevel.INFO) {
      this.logInternal(console.info, args);
    }
  }

  public log(...args: any[]): void {
    if (this.level >= LogLevel.INFO) {
      this.logInternal(console.log, args);
    }
  }

  public warn(...args: any[]): void {
    if (this.level >= LogLevel.WARN) {
      this.logInternal(console.warn, args);
    }
  }

  public debug(...args: any[]): void {
    if (this.level >= LogLevel.DEBUG) {
      this.logInternal(console.debug, args);
    }
  }

  protected getLogLevel(): LogLevel {
    // Set default log level based on environment
    const logLevelString = environment.logLevel || 'error';

    switch (logLevelString.toLowerCase()) {
      case 'debug':
        return LogLevel.DEBUG;
      case 'info':
        return LogLevel.INFO;
      case 'warn':
        return LogLevel.WARN;
      case 'error':
        return LogLevel.ERROR;
      case 'off':
        return LogLevel.OFF;
      default:
        return LogLevel.ERROR;
    }
  }

  private logInternal(logFn: (...args: any[]) => void, args: any[]): void {
    const timestamp = new Date().toISOString();
    const formattedArgs = [`[${timestamp}]`, ...args];
    logFn.apply(console, formattedArgs);
  }
}
