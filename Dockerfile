# Stage 1: Build the application
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# Copy the entire project
COPY . .

# Create the output directory
RUN mkdir -p out

# Compile the application
RUN javac -d out -cp "lib/mysql-connector-j.jar:lib/h2.jar" \
    src/com/parkwise/db/DatabaseConnection.java \
    src/com/parkwise/model/Vehicle.java \
    src/com/parkwise/model/ParkingSlot.java \
    src/com/parkwise/model/ParkingRecord.java \
    src/com/parkwise/model/Payment.java \
    src/com/parkwise/dao/AdminDAO.java \
    src/com/parkwise/dao/VehicleDAO.java \
    src/com/parkwise/dao/ParkingSlotDAO.java \
    src/com/parkwise/dao/ParkingRecordDAO.java \
    src/com/parkwise/dao/PaymentDAO.java \
    src/com/parkwise/service/ParkingService.java \
    src/com/parkwise/web/Json.java \
    src/com/parkwise/web/BaseHandler.java \
    src/com/parkwise/web/AuthHandler.java \
    src/com/parkwise/web/DashboardHandler.java \
    src/com/parkwise/web/ParkingHandler.java \
    src/com/parkwise/web/VehicleHandler.java \
    src/com/parkwise/web/ReportsHandler.java \
    src/com/parkwise/web/StaticHandler.java \
    src/com/parkwise/web/WebServer.java

# Copy db properties to the output
RUN cp db.properties out/ || true

# Stage 2: Create the runtime image
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy compiled classes and libraries from builder
COPY --from=builder /app/out ./out
COPY --from=builder /app/lib ./lib
COPY --from=builder /app/web ./web

# Start the application
CMD ["java", "-cp", "out:lib/mysql-connector-j.jar:lib/h2.jar", "com.parkwise.web.WebServer"]
