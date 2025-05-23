# E-MedicalServices Project (Java Swing & MySQL)

A Java Swing desktop application for managing basic medical service interactions between Patients, Doctors, and Administrators, using a MySQL database.

## Technologies Used
*   Java (JDK 11+)
*   Java Swing (GUI)
*   MySQL (Database)
*   MySQL Connector/J (JDBC)

## Setup Instructions

### Prerequisites
1.  JDK 11 (or newer) installed.
2.  MySQL Server installed and running.
3.  MySQL Connector/J JAR file.

### Database Configuration
1.  **Create Database:** In MySQL, create a database (e.g., `ServiciiMedicaleDB`).
    ```sql
    CREATE DATABASE ServiciiMedicaleDB;
    USE ServiciiMedicaleDB;
    ```
2.  **Create Tables:** Execute the following SQL script in your `ServiciiMedicaleDB` database:
    ```sql
    -- PASTE YOUR FULL, COMPLETE CREATE TABLE SQL SCRIPT HERE
    -- This must include: Clinics, Admins, Doctors, Patients, Appointments, ConsultationRecords, Feedbacks
    -- with all columns, data types, PRIMARY KEYs, FOREIGN KEYs, UNIQUE, NOT NULL constraints.
    -- Example for ONE table:
    -- CREATE TABLE Clinics (id INT AUTO_INCREMENT PRIMARY KEY, nume_clinica VARCHAR(255) NOT NULL UNIQUE, detalii TEXT);
    -- ... (YOU MUST PROVIDE THE REST FOR ALL TABLES) ...
    ```
3.  **Update JDBC Connection:**
    Modify `src/db/DatabaseConnection.java` with your MySQL URL, username, and password:
    ```java
    // private static final String URL = "jdbc:mysql://127.0.0.1:3306/ServiciiMedicaleDB";
    // private static final String USER = "your_mysql_user";
    // private static final String PASSWORD = "your_mysql_password";
    ```

### Project Setup (IntelliJ IDEA Example)
1.  Open the project.
2.  Add `mysql-connector-java-x.x.xx.jar` to module dependencies (`File` > `Project Structure...` > `Modules` > `Dependencies` > `+` > `JARs or directories...`).

## How to Run
1.  Ensure MySQL server is running with the configured database and tables.
2.  Compile the project.
3.  Run the `main()` method in `src/main/App.java`.

## Core Functionalities
*   **Admin:** Manages Doctor accounts and Clinics.
*   **Patient:** Registers, logs in, views profile, searches/views Doctors, books/cancels/modifies appointments, views consultation history, leaves feedback.
*   **Doctor:** Logs in, views/filters appointments, completes consultations (enters diagnosis/prescription), views patient history (with them), views feedback.

## Author
[Bratu Nicolae]
