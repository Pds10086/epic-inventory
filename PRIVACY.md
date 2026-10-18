# Privacy Policy

Last updated: 2026-10-17

## Overview
Epic 库存 (Epic Inventory) is an unofficial, independent Android app that lets you view your Epic Games account inventory. This app is **not affiliated with, endorsed by, or sponsored by Epic Games, Inc.**

## Data We Process
All data processing happens on your device.

| Data | Purpose | Storage |
|------|---------|---------|
| OAuth access/refresh token | Call Epic Games public APIs | AES-256 (Android Keystore) |
| Library/catalog metadata | Show your inventory | Cached in memory only |
| Theme preference | Light/dark mode | DataStore on device |
| Notification preferences | Weekly free games reminders | DataStore on device |

We do **not** operate any backend server. We do **not** collect analytics, telemetry, or any personal data on our side.

## Network Requests
The app only communicates with Epic Games public endpoints (api.epicgames.com, catalog-public-service-prod, store-public-service-prod, achievements-public-service-prod, library-service). All requests include the user-supplied OAuth token in the standard `Authorization: Bearer` header.

## Account Deletion
To delete all data, go to Settings → Accounts → Delete, or use the system Settings → Apps → Epic Inventory → Clear Data.

## Open Source
This project is open source. You can inspect the source code, build, and audit yourself.

## Contact
For privacy concerns: open a GitHub issue on the project repository.
