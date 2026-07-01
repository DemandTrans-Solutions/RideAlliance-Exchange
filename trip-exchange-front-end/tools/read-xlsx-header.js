const XLSX = require('xlsx');
const path = require('path');
const fp = path.resolve(__dirname, '..', 'import-export', 'trip-export.xlsx');
try {
  const wb = XLSX.readFile(fp, { cellDates: true });
  const firstSheetName = wb.SheetNames[0];
  const ws = wb.Sheets[firstSheetName];
  const aoa = XLSX.utils.sheet_to_json(ws, { header: 1 });
  if (!aoa || aoa.length === 0) {
    console.error('No data in sheet');
    process.exit(2);
  }
  const header = aoa[0];
  // Print header as a single CSV-like line
  console.log(header.map(h => (h === undefined || h === null) ? '' : String(h)).join(','));
} catch (err) {
  console.error('Error reading xlsx:', err && err.message ? err.message : err);
  process.exit(1);
}
