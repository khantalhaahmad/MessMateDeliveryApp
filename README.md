# 🚚 MessMate Delivery App

A production-ready **Delivery Agent Android Application** for the MessMate platform, enabling real-time order management, earnings tracking, and seamless delivery operations.

---

## 📱 Features

### 🔐 Authentication

* Phone Number Login (OTP आधारित)
* Secure token-based authentication
* Persistent login using SharedPreferences

---

### 📦 Order Management

* View **available delivery orders**
* Accept / Reject orders
* Real-time order updates
* Order details with pickup & drop locations

---

### 🚀 Delivery Flow

* Order Status Tracking:

  * Accepted
  * Picked Up
  * Delivered
* Smooth workflow for delivery agents

---

### 💰 Earnings Dashboard

* Today’s earnings
* Total completed deliveries
* Weekly insights (extendable)

---

### 🟢 Agent Status

* Go **Online / Offline**
* Receive orders only when online

---

## 🏗️ Tech Stack

* **Language:** Java
* **Architecture:** MVVM
* **Networking:** Retrofit + OkHttp
* **State Handling:** LiveData
* **Local Storage:** SharedPreferences
* **Backend:** Node.js (MessMate API)
* **Realtime (Planned):** Socket.IO

---

## 📂 Project Structure

```
com.messmate.delivery
│
├── network        # API client, interceptors
├── repository     # Data layer
├── viewmodel      # Business logic
├── ui             # Activities & Fragments
├── socket         # Realtime updates
└── utils          # Constants & helpers
```

---

## ⚙️ Setup Instructions

1. Clone the repository:

```bash
git clone https://github.com/khantalhaahmad/MessMateDeliveryApp.git
```

2. Open in **Android Studio**

3. Add your backend URL in:

```
Constants.java
```

4. Run the app on emulator / device

---

## 🔥 Current Status

* ✅ Authentication (OTP login)
* ✅ Dashboard UI
* ✅ API integration (basic)
* 🚧 Live orders system (in progress)
* 🚧 Realtime updates (Socket)

---

## 🚀 Future Improvements

* Live tracking (Google Maps integration)
* Push Notifications (Firebase)
* Advanced earnings analytics
* Dark mode support

---

## 👨‍💻 Author

**Talha Ahmad Khan**
Aspiring Software Engineer | Android & Full-Stack Developer

---

## ⭐ Support

If you like this project, consider giving it a ⭐ on GitHub!

---
