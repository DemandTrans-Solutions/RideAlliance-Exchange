const fs = require('fs');
const path = require('path');

// Create a simple HTML file that uses the SVG as favicon
// This is a workaround since we can't directly convert SVG to ICO
const htmlContent = `
<!DOCTYPE html>
<html>
<head>
  <title>Favicon Template</title>
  <link rel="icon" type="image/svg+xml" href="./src/assets/images/logos/trip-exchange-favicon.svg">
</head>
<body>
  <h1>Favicon Test</h1>
</body>
</html>
`;

// Write the HTML file
fs.writeFileSync('favicon-test.html', htmlContent);
console.log('Created favicon-test.html with SVG favicon link');

// Update index.html to use SVG favicon
const indexPath = './src/index.html';
let indexContent = fs.readFileSync(indexPath, 'utf8');

// Replace the favicon link
indexContent = indexContent.replace(
	'<link rel="icon" type="image/x-icon" href="favicon.ico" />',
	'<link rel="icon" type="image/svg+xml" href="assets/images/logos/trip-exchange-favicon.svg" />'
);

// Write the updated index.html
fs.writeFileSync(indexPath, indexContent);
console.log('Updated index.html to use SVG favicon');

console.log('Done! The application now uses the Trip Exchange logo as favicon.');