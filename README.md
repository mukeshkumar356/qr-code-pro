# QR Code Pro

**A native Android QR code generator and scanner — customizable colors, save/share, and camera-based scanning via ZXing.**

[![CI](https://github.com/mukeshkumar356/qr-code-pro/actions/workflows/android-ci.yml/badge.svg)](https://github.com/mukeshkumar356/qr-code-pro/actions/workflows/android-ci.yml)
[![Platform](https://img.shields.io/badge/platform-Android-3DDC84?logo=android&logoColor=white)](#)
[![Language](https://img.shields.io/badge/language-Kotlin-7F52FF?logo=kotlin&logoColor=white)](#)
[![License](https://img.shields.io/badge/license-MIT-lightgrey)](LICENSE)

## Overview

A two-in-one QR/barcode utility: generate a styled QR code from any text/URL, and scan existing codes with the camera — all offline, no account, no tracking.

## ✨ Features

- **Generate** — turn any text/URL into a QR code with 4 foreground and 4 background color presets
- **Scan** — camera-based QR/barcode scanning via [ZXing](https://github.com/zxing/zxing) (`journeyapps/zxing-android-embedded`), supports all standard barcode formats
- Save the generated code as a PNG, or share it directly via any app
- Auto-detects URLs in scan results and offers to open them
- One-tap copy of scan results to clipboard

## 🛠️ Tech Stack

- Kotlin, View Binding
- [ZXing core](https://github.com/zxing/zxing) for QR/barcode encode+decode
- `FileProvider` for secure image sharing across apps

## 🚀 Build it yourself

```bash
git clone https://github.com/mukeshkumar356/qr-code-pro.git
cd qr-code-pro
./gradlew assembleDebug
```

## 📄 License

MIT — see [LICENSE](LICENSE).

---

Built by **[Mukesh Kumar](https://github.com/mukeshkumar356)**
