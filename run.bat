@echo off
REM ==========================================
REM ParkWise - Build and Run Script
REM ==========================================
echo.
echo  =============================================
echo     ParkWise - Parking Management System
echo  =============================================
echo.

REM Check if lib folder exists with JDBC driver
if not exist "lib\mysql-connector-j.jar" (
    echo [!] MySQL JDBC Driver not found in lib\
    echo [*] Downloading MySQL Connector/J...
    mkdir lib 2>nul
    powershell -Command "Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.2.0/mysql-connector-j-8.2.0.jar' -OutFile 'lib\mysql-connector-j.jar'"
    if errorlevel 1 (
        echo [ERROR] Failed to download MySQL JDBC driver.
        echo [INFO] Please manually download mysql-connector-j-8.x.x.jar
        echo        and place it in the lib\ folder as mysql-connector-j.jar
        pause
        exit /b 1
    )
    echo [OK] MySQL JDBC Driver downloaded successfully.
)

REM Check if H2 database jar exists
if not exist "lib\h2.jar" (
    echo [!] H2 Database not found in lib\
    echo [*] Downloading H2 Database Engine...
    mkdir lib 2>nul
    powershell -Command "Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/com/h2database/h2/2.2.224/h2-2.2.224.jar' -OutFile 'lib\h2.jar'"
    if errorlevel 1 (
        echo [WARN] Failed to download H2. App will only work with MySQL.
    ) else (
        echo [OK] H2 Database downloaded successfully.
    )
)

REM Create output directory
if not exist "out" mkdir out

REM Copy db.properties into output so it's on the classpath
copy /Y db.properties out\db.properties >nul

echo [*] Compiling Java source files...
echo.

REM Compile all Java files
javac -d out -cp "lib\mysql-connector-j.jar;lib\h2.jar" ^
    src\com\parkwise\db\DatabaseConnection.java ^
    src\com\parkwise\model\Vehicle.java ^
    src\com\parkwise\model\ParkingSlot.java ^
    src\com\parkwise\model\ParkingRecord.java ^
    src\com\parkwise\model\Payment.java ^
    src\com\parkwise\dao\AdminDAO.java ^
    src\com\parkwise\dao\VehicleDAO.java ^
    src\com\parkwise\dao\ParkingSlotDAO.java ^
    src\com\parkwise\dao\ParkingRecordDAO.java ^
    src\com\parkwise\dao\PaymentDAO.java ^
    src\com\parkwise\service\ParkingService.java ^
    src\com\parkwise\ui\UIUtils.java ^
    src\com\parkwise\ui\LoginFrame.java ^
    src\com\parkwise\ui\MainFrame.java ^
    src\com\parkwise\ui\panels\DashboardPanel.java ^
    src\com\parkwise\ui\panels\VehicleEntryPanel.java ^
    src\com\parkwise\ui\panels\VehicleExitPanel.java ^
    src\com\parkwise\ui\panels\ParkingSlotsPanel.java ^
    src\com\parkwise\ui\panels\HistoryPanel.java ^
    src\com\parkwise\ui\panels\VehicleManagementPanel.java ^
    src\com\parkwise\ui\panels\ReportsPanel.java ^
    src\com\parkwise\ParkWiseApp.java

if errorlevel 1 (
    echo.
    echo [ERROR] Compilation failed! Check errors above.
    pause
    exit /b 1
)

echo [OK] Compilation successful!
echo.
echo [*] Launching ParkWise...
echo.

REM Run the application (H2 + MySQL jars on classpath)
java -cp "out;lib\mysql-connector-j.jar;lib\h2.jar" com.parkwise.ParkWiseApp

pause
