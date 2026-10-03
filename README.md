# 🅿️ ParkWise - Smart Parking Management System

A complete, professional **Parking Lot Management System** built with **Java Swing + MySQL + JDBC** following OOP principles. Features a modern dark-themed desktop GUI with full CRUD operations, real-time slot management, automated billing, and comprehensive analytics.

---

## ✨ Features

| Feature | Description |
|---------|-------------|
| 🔐 **Admin Login** | Secure authentication with credential validation |
| 📊 **Dashboard** | Real-time stats: total/available/occupied slots, parked vehicles, revenue |
| 🚗 **Vehicle Entry** | Form with validation, automatic slot allocation, ticket generation |
| 🚪 **Vehicle Exit** | Search by ticket/vehicle, auto fee calculation, payment processing |
| 🅿️ **Parking Slots** | Visual grid with live Available/Occupied status, type filtering |
| 📜 **History** | Complete parking records with search and date range filters |
| 📝 **Vehicle Management** | Full CRUD: add, edit, delete, search vehicle records |
| 📈 **Reports** | Revenue analytics by date range, vehicle type breakdown |
| 🧾 **Billing** | Printable receipt generation with print dialog |

---

## 🏗️ Architecture

```
ParkWise/
├── database/
│   └── schema.sql              # Complete MySQL schema + sample data
├── lib/
│   └── mysql-connector-j.jar   # JDBC driver (auto-downloaded)
├── src/com/parkwise/
│   ├── ParkWiseApp.java        # Main entry point
│   ├── db/
│   │   └── DatabaseConnection.java  # Singleton JDBC connection
│   ├── model/                  # POJO model classes
│   │   ├── Vehicle.java
│   │   ├── ParkingSlot.java
│   │   ├── ParkingRecord.java
│   │   └── Payment.java
│   ├── dao/                    # Data Access Objects (CRUD)
│   │   ├── AdminDAO.java
│   │   ├── VehicleDAO.java
│   │   ├── ParkingSlotDAO.java
│   │   ├── ParkingRecordDAO.java
│   │   └── PaymentDAO.java
│   ├── service/
│   │   └── ParkingService.java # Business logic layer
│   └── ui/                     # Swing GUI
│       ├── UIUtils.java        # Design system & styled components
│       ├── LoginFrame.java     # Login screen
│       ├── MainFrame.java      # Main app with sidebar navigation
│       └── panels/
│           ├── DashboardPanel.java
│           ├── VehicleEntryPanel.java
│           ├── VehicleExitPanel.java
│           ├── ParkingSlotsPanel.java
│           ├── HistoryPanel.java
│           ├── VehicleManagementPanel.java
│           └── ReportsPanel.java
├── run.bat                     # Build & run script
└── README.md
```

---

## 🚀 Setup & Run

### Prerequisites
- **Java JDK 8+** (ensure `javac` and `java` are in PATH)
- **MySQL Server 5.7+** running on `localhost:3306`

### Step 1: Database Setup (H2 Embedded Default)
The application runs out of the box using an embedded H2 database. No setup is required. The database file `parkwise.mv.db` will be created automatically in the project folder.

### Optional: MySQL Setup
If you want to use MySQL instead:
1. Run `database/schema.sql` in MySQL.
2. Edit `db.properties` in the project root:
```properties
db.url=jdbc:mysql://localhost:3306/parkwise
db.username=root
db.password=YOUR_PASSWORD
```

### Step 2: Run the Application
```batch
# Double-click run.bat or start-web.bat, or execute:
cd C:\Users\DHARSHANA\ParkWise
run.bat
```
The scripts will automatically download necessary JDBC drivers, compile, and launch the application.

---

## 🔑 Default Login

| Field | Value |
|-------|-------|
| Username | `admin` |
| Password | `admin123` |

---

## 💰 Parking Rates

| Vehicle Type | Rate/Hour | Minimum Charge |
|-------------|-----------|----------------|
| Bike 🏍️ | ₹10 | ₹10 |
| Car 🚗 | ₹20 | ₹20 |

---

## 🅿️ Parking Capacity

| Type | Slots | Range |
|------|-------|-------|
| Bike | 30 | B01-B30 |
| Car | 20 | C01-C20 |
| **Total** | **50** | |

---

## 🗃️ Database Tables

| Table | Purpose |
|-------|---------|
| `admin` | Admin user credentials |
| `parking_slots` | All parking slots with status |
| `vehicles` | Registered vehicle information |
| `parking_records` | Entry/exit records with billing |
| `payments` | Payment transaction history |
| `rate_config` | Hourly rates by vehicle type |

---

## 📝 OOP Principles Used

- **Encapsulation** — Private fields with getters/setters in model classes
- **Singleton** — DatabaseConnection uses Singleton pattern
- **Separation of Concerns** — Model → DAO → Service → UI layers
- **MVC Architecture** — Model-View-Controller pattern throughout
- **Inheritance** — Custom Swing components extending JPanel/JFrame
- **Polymorphism** — Overridden paintComponent for custom rendering

---

## 🛠️ Technologies

- **Language:** Java 8+
- **GUI:** Java Swing
- **Database:** MySQL
- **Connectivity:** JDBC (MySQL Connector/J 8.2.0)
- **Design Pattern:** DAO, Singleton, MVC, Service Layer
