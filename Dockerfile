# Stage 1: Build the application
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

# Copy all source files
COPY . .

# Download dependencies securely with correct permissions
RUN mkdir -p lib && \
    wget -qO lib/mysql-connector-j.jar https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.2.0/mysql-connector-j-8.2.0.jar && \
    wget -qO lib/h2.jar https://repo1.maven.org/maven2/com/h2database/h2/2.2.224/h2-2.2.224.jar && \
    chmod 644 lib/*.jar

RUN mkdir -p out

# Compile the web server and dependencies
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

RUN printf "db.url=jdbc:h2:/tmp/parkwise;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1\ndb.username=sa\ndb.password=\n" > out/db.properties

# Stage 2: Create the runtime image
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy compiled classes and libraries from builder
COPY --from=builder /app/out ./out
COPY --from=builder /app/lib ./lib
COPY --from=builder /app/web ./web

# Make sure we use colon ':' for classpath separator in Linux/Docker
CMD ["java", "-Djava.net.preferIPv4Stack=true", "-cp", "out:lib/mysql-connector-j.jar:lib/h2.jar", "com.parkwise.web.WebServer"]
