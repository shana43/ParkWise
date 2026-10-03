@echo off
REM ==========================================
REM ParkWise Web Server — Build & Run
REM ==========================================
echo.
echo  =============================================
echo     ParkWise Web Server
echo  =============================================
echo.

if not exist "lib\mysql-connector-j.jar" (
    echo [*] Downloading MySQL JDBC Driver...
    if not exist lib mkdir lib
    powershell -Command "Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.2.0/mysql-connector-j-8.2.0.jar' -OutFile 'lib\mysql-connector-j.jar'"
    if errorlevel 1 ( echo [ERROR] Download failed. Manually place mysql-connector-j.jar in lib\. & pause & exit /b 1 )
    echo [OK] Driver downloaded.
)

if not exist "out" mkdir out

REM Copy db.properties into output so it's on the classpath
copy /Y db.properties out\db.properties >nul

echo [*] Compiling...
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
  src\com\parkwise\web\Json.java ^
  src\com\parkwise\web\BaseHandler.java ^
  src\com\parkwise\web\AuthHandler.java ^
  src\com\parkwise\web\DashboardHandler.java ^
  src\com\parkwise\web\ParkingHandler.java ^
  src\com\parkwise\web\VehicleHandler.java ^
  src\com\parkwise\web\ReportsHandler.java ^
  src\com\parkwise\web\StaticHandler.java ^
  src\com\parkwise\web\WebServer.java 2>&1

if errorlevel 1 (
    echo.
    echo [ERROR] Compilation failed! See errors above.
    pause & exit /b 1
)
echo [OK] Compiled successfully.
echo.
echo [*] Starting web server...
echo.
java -cp "out;lib\mysql-connector-j.jar;lib\h2.jar" com.parkwise.web.WebServer
pause
