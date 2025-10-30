# KeyBase License Security System

## Overview
KeyBase now includes a hardware-based license protection system that prevents unauthorized use of the software.

## How It Works

### 1. Hardware ID Generation
- The system generates a unique Hardware ID based on:
  - CPU Processor ID (via WMIC)
  - Motherboard Serial Number (via WMIC)
- This ID is hashed using SHA-256 to create a unique identifier for each computer

### 2. License Key Encryption
- The Hardware ID is encrypted using AES encryption with a unique encryption PIN
- The encryption PIN is generated from the Hardware ID + a secret key
- Only the correct license key for a specific computer will work on that computer

### 3. License Verification
- During application startup (splash screen), the license is verified
- If no license file is found, user is prompted to enter a license key
- If license is invalid, application will not start

## For End Users

### First Run
1. When you run KeyBase for the first time, you'll see a license activation dialog
2. The dialog shows your computer's Hardware ID
3. Contact your administrator and provide them with the Hardware ID
4. Enter the license key provided by your administrator
5. Click OK to activate

### License File Location
- License file: `keybase.lic` (stored in the application root directory)
- **Do not delete or modify this file**

## For Administrators

### Generating License Keys

#### Method 1: Using the License Key Generator GUI
1. Run `generate_license.bat`
2. The License Key Generator window will open
3. Get the Hardware ID from the client computer
4. Enter the Hardware ID in the "Hardware ID" field
5. Click "Generate License Key"
6. Copy the generated key and send it to the client

#### Method 2: Test Current Machine
1. In the License Key Generator, click "Test Current Machine"
2. This will generate a license key for the computer running the generator
3. Useful for testing or self-activation

### Distribution
- Send only the **License Key** to clients
- Keep the Hardware ID information for your records
- Each license key is unique to one computer

## Files

### Application Files
- `src/LicenseManager.java` - Core license management logic
- `src/LicenseKeyGenerator.java` - License key generator utility
- `src/KeyBase.java` - Updated to include license verification
- `keybase.lic` - License file (generated after activation)

### Batch Files
- `run.bat` - Run the main KeyBase application
- `generate_license.bat` - Run the License Key Generator

## Security Features

1. **Hardware Binding**: License keys only work on the specific computer they were generated for
2. **Encryption**: AES encryption protects the license data
3. **Hash-based Verification**: SHA-256 hashing prevents tampering
4. **No Bypass**: Application will not start without valid license

## Troubleshooting

### "Invalid license key for this computer"
- The license key was generated for a different computer
- Get a new license key using your current Hardware ID

### "License file not found"
- Normal for first run
- Enter your license key when prompted

### Hardware changes
- If CPU or motherboard is changed, a new license key will be needed
- This is by design to prevent license sharing

## Technical Details

### License Key Format
- **Length**: 14 characters (including dashes)
- **Format**: XXXX-XXXX-XXXX-XX
- **Example**: A3F2-8B4D-C1E5-9A
- **Generation**: First 14 characters of SHA-256 hash of (HardwareID + Secret Base)
- **Case Insensitive**: Keys can be entered in any case

### Hardware Detection
- Uses WMIC (Windows Management Instrumentation Command-line)
- Detects: CPU ProcessorId + Motherboard SerialNumber
- Hashing: SHA-256 for security
- Fallback to username + OS name if WMIC fails
- Cross-platform compatible (Windows tested)

## Support

For license-related issues:
1. Provide your Hardware ID (shown in activation dialog)
2. Describe the error message
3. Contact your system administrator
