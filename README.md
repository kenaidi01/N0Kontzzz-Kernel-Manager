<h1 align="center" style="font-size: 48px;">N0Kontzzz Kernel Manager</h1>

![Platform](https://img.shields.io/badge/platform-Android-green?style=for-the-badge&logo=android)
![Language](https://img.shields.io/badge/language-Kotlin-purple?style=for-the-badge&logo=kotlin)
![UI](https://img.shields.io/badge/Jetpack-Compose-blue?style=for-the-badge&logo=jetpackcompose)
![License](https://img.shields.io/github/license/bimoalfarrabi/N0Kontzzz-Kernel-Manager?style=for-the-badge)
![Root Required](https://img.shields.io/badge/Root-Required-critical?style=for-the-badge&logo=android)
![GitHub commits](https://img.shields.io/github/commit-activity/t/bimoalfarrabi/N0Kontzzz-Kernel-Manager?style=for-the-badge)
[![Repo Size](https://img.shields.io/github/repo-size/bimoalfarrabi/N0Kontzzz-Kernel-Manager?style=for-the-badge&logo=github)](https://github.com/bimoalfarrabi/N0Kontzzz-Kernel-Manager)

**N0Kontzzz Kernel Manager** is a modern, rooted Android application. Built with Kotlin and Jetpack Compose, the app provides real-time monitoring and tuning of CPU performance, thermal behavior, and more for Poco F4 (munch), optimized for [N0Kontzzz](https://github.com/bimoalfarrabi/kernel_xiaomi_sm8250_n0kontzz) custom kernel.


## ✨ Features

- 📊 **Real-time CPU Monitoring**  
  View individual core in a clean, responsive UI.

- 🌡️ **Thermal Zone Status**  
  Retrieve and display system thermal zone data for advanced thermal debugging.

- ⚙️ **CPU Tuning**
  Apply governor changes on-the-fly with native shell execution via [libsu](https://github.com/topjohnwu/libsu).

- 💡 **Material 3 Expressive UI**  
  Elegant interface using the latest Jetpack Compose and Material Design 3 Expressive components.

- 🚀 **Fast & Minimal**  
  Lightweight architecture using MVVM pattern, ensuring smooth performance on rooted Poco F4 devices.

---

## 📱 Requirements

- ✅ Poco F4 (munch) running N0Kontzzz kernel.
- ✅ Root access (Magisk / KernelSU supported).

---

## 🔐 Permissions

- `root` access via libsu (automatic permission request).
- No internet access or telemetry. Operates 100% offline and private.

---

## 🛠 Built With

- [Kotlin](https://kotlinlang.org/)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [libsu by Topjohnwu](https://github.com/topjohnwu/libsu)
- MVVM Architecture (ViewModel + LiveData)
- Material Design 3

---

## 📂 Repository

Feel free to fork, open issues, or contribute via pull requests.

---
> [!TIP]
>
> - **Performance Mode**: For demanding tasks like gaming, switch to `performance` governor for maximum CPU speed.
> - **Balanced Profile**: `schedutil` governor for a balance between performance and power saving.


---

### Credits
- Forked from **[Xtra Kernel Manager](https://github.com/Gustyx-Power/Xtra-Kernel-Manager)** — used as the foundation for this app.
- **[Danda](https://github.com/Danda420)** — for contributions to app development and guidance in understanding Android internals.
- **[RvKernel Manager](https://github.com/Rve27/RvKernel-Manager)** —  inspired feature concepts and code references in this project.
---

## 📣 Disclaimer

> ⚠️ This app performs advanced functions that may affect system stability.  
> Use at your own risk — no responsibility for any damage resulting from misconfiguration.

---
