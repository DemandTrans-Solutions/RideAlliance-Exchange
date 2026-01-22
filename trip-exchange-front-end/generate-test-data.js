const fs = require('fs');

const firstNames = ['William', 'Emma', 'Richard', 'Susan', 'Joseph', 'Margaret', 'Thomas', 'Barbara', 'Charles', 'Linda', 'Daniel', 'Karen', 'Paul', 'Nancy', 'Mark', 'Lisa', 'Donald', 'Helen', 'George', 'Sandra'];
const middleNames = ['Lee', 'Grace', 'William', 'Elizabeth', 'Joseph', 'Marie', 'Edward', 'Rose', 'Alan', 'Jean', 'Scott', 'Kay', 'Francis', 'Louise', 'Ray', 'Anne', 'Eugene', 'May', 'Louis'];
const lastNames = ['Jones', 'Wilson', 'Anderson', 'Thomas', 'Jackson', 'White', 'Harris', 'Martin', 'Thompson', 'Garcia', 'Martinez', 'Robinson', 'Clark', 'Rodriguez', 'Lewis', 'Lee', 'Walker', 'Hall', 'Allen', 'Young'];
const genders = ['Male', 'Female'];
const races = ['White', 'Black', 'Asian', 'Hispanic', 'Native American', 'Pacific Islander'];
const languages = ['English', 'Spanish', 'Vietnamese', 'Chinese', 'Korean', 'Russian'];
const mobilityFactors = ['Wheelchair', 'Walker', 'Cane', 'None'];
const serviceLevels = ['Standard', 'Premium'];
const streets = ['Maple', 'Oak', 'Pine', 'Cedar', 'Elm', 'Birch', 'Main', 'Park', 'Lake', 'Hill', 'Forest', 'River', 'Valley', 'Mountain', 'Spring'];
const addressTypes = ['Home', 'Medical', 'Work', 'Other'];
const priorities = ['Normal', 'High', 'Low'];
const purposes = ['Medical', 'Therapy', 'Dental', 'Vision', 'Lab Work', 'Specialist'];
const statuses = ['Scheduled', 'Pending', 'Completed'];

function randomFromArray(arr) {
  return arr[Math.floor(Math.random() * arr.length)];
}

function generatePhoneNumber() {
  return `555-${String(Math.floor(Math.random() * 10000)).padStart(4, '0')}`;
}

function generateAddress(type, index) {
  const street = randomFromArray(streets);
  const number = Math.floor(Math.random() * 90000) + 10000;
  const zip = 97200 + index;
  const lat = 45.5000 + (Math.random() * 0.2);
  const lng = -122.6700 - (Math.random() * 0.2);
  const ext = Math.floor(Math.random() * 900) + 100;

  return {
    street1: `${number} ${street} St`,
    street2: Math.random() > 0.5 ? `Suite ${Math.floor(Math.random() * 500) + 1}` : '',
    city: 'Portland',
    state: 'OR',
    zipcode: String(zip),
    county: 'Multnomah',
    latitude: lat.toFixed(4),
    longitude: lng.toFixed(4),
    commonName: type,
    phoneNumber: `503-555-${String(Math.floor(Math.random() * 10000)).padStart(4, '0')}`,
    phoneExtension: String(ext),
    addressType: type
  };
}

function generateRecord(index) {
  const homeAddress = generateAddress('Home', index);
  const pickupAddress = { ...homeAddress }; // Usually same as home address
  const dropoffAddress = generateAddress('Medical', index);
  const tripId = 1011 + index;
  const customerId = 2011 + index;

  // Generate random times
  const pickupHour = Math.floor(Math.random() * 8) + 8; // 8 AM to 4 PM
  const pickupMinute = Math.floor(Math.random() * 4) * 15; // 0, 15, 30, 45
  const pickupTime = `${String(pickupHour).padStart(2, '0')}:${String(pickupMinute).padStart(2, '0')}`;
  const appointmentTime = `${String(pickupHour + 1).padStart(2, '0')}:${String(pickupMinute).padStart(2, '0')}`;
  const dropoffTime = `${String(pickupHour + 1).padStart(2, '0')}:${String((pickupMinute + 15) % 60).padStart(2, '0')}`;

  return [
    randomFromArray(firstNames),
    randomFromArray(middleNames),
    randomFromArray(lastNames),
    randomFromArray(genders),
    randomFromArray(races),
    `19${Math.floor(Math.random() * 30) + 70}-${String(Math.floor(Math.random() * 12) + 1).padStart(2, '0')}-${String(Math.floor(Math.random() * 28) + 1).padStart(2, '0')}`,
    generatePhoneNumber(),
    `ID${tripId}`,
    randomFromArray(['Medicaid', 'Medicare', 'Private']),
    randomFromArray(mobilityFactors),
    'Regular customer',
    randomFromArray(languages),
    generatePhoneNumber(),
    '1',
    randomFromArray(serviceLevels),
    homeAddress.street1,
    homeAddress.street2,
    homeAddress.city,
    homeAddress.state,
    homeAddress.zipcode,
    homeAddress.county,
    homeAddress.latitude,
    homeAddress.longitude,
    homeAddress.commonName,
    homeAddress.phoneNumber,
    homeAddress.phoneExtension,
    homeAddress.addressType,
    pickupAddress.street1,
    pickupAddress.street2,
    pickupAddress.city,
    pickupAddress.state,
    pickupAddress.zipcode,
    pickupAddress.county,
    pickupAddress.latitude,
    pickupAddress.longitude,
    pickupAddress.commonName,
    pickupAddress.phoneNumber,
    pickupAddress.phoneExtension,
    pickupAddress.addressType,
    dropoffAddress.street1,
    dropoffAddress.street2,
    dropoffAddress.city,
    dropoffAddress.state,
    dropoffAddress.zipcode,
    dropoffAddress.county,
    dropoffAddress.latitude,
    dropoffAddress.longitude,
    dropoffAddress.commonName,
    dropoffAddress.phoneNumber,
    dropoffAddress.phoneExtension,
    dropoffAddress.addressType,
    String(tripId),
    dropoffTime,
    pickupTime,
    appointmentTime,
    (Math.random() * 7 + 2).toFixed(1),
    String(Math.floor(Math.random() * 30) + 15),
    String(Math.floor(Math.random() * 2)),
    String(Math.floor(Math.random() * 2)),
    String(Math.random() > 0.8),
    String(Math.random() > 0.8),
    String(Math.random() > 0.8),
    String(Math.random() > 0.9),
    randomFromArray(priorities),
    String(Math.floor(Math.random() * 20) + 10),
    String(Math.floor(Math.random() * 20) + 10),
    randomFromArray(['Medicaid', 'Medicare', 'Private Insurance']),
    'Regular checkup',
    randomFromArray(purposes),
    String(customerId),
    randomFromArray(statuses)
  ].join(',');
}

// Read the existing file
const existingContent = fs.readFileSync('src/assets/test-data/trip-tickets.csv', 'utf8');
const lines = existingContent.split('\n');

// Keep the header and first 10 records
const newContent = lines.slice(0, 11);

// Generate 40 more records
for (let i = 0; i < 40; i++) {
  newContent.push(generateRecord(i));
}

// Write back to the file
fs.writeFileSync('src/assets/test-data/trip-tickets.csv', newContent.join('\n'));

console.log('Generated 40 new unique records and updated the CSV file.');
