// debugHelper.ts - This file helps ensure proper source map usage for breakpoints
/**
 * Helper function that can be called to help VSCode locate and bind breakpoints
 * Use this function at any location where you want to ensure breakpoints work
 */
export function ensureBreakpointWorks(message: string = 'Breakpoint checking'): void {
  // This function creates a clean debug point for the debugger to reliably hook into
  console.log(`Debug helper: ${message}`);
  const timestamp = new Date().toISOString();
  console.log(`Current time: ${timestamp}`);
}
