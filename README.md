# Water Meter Photo Organizer

A Java Spring Boot console application designed to automate the renaming and organization of water meter reading photos stored in Google Drive.

Instead of manually renaming hundreds of photos taken during a meter reading route, this application reads the chronological metadata of the files directly from the cloud and automatically applies specific naming conventions based on the physical layout of the condominium.

## 🚀 Features

****Zero-Download** Metadata Extraction**: Utilizes the Google Drive API v3 (imageMediaMetadata and createdTime) to fetch chronological data without downloading heavy image files, making the script incredibly fast and efficient.

**Chronological Mapping**: Sorts photos based on the exact second they were taken, perfectly mirroring the physical walking route of the meter reader.

**Strategy Pattern Architecture**: Built with scalability in mind. It uses the Strategy design pattern to apply different naming rules depending on the condominium's layout.
**
Dynamic Extension Handling**: Automatically preserves the original file extension (e.g., .HEIC, .jpg, .png), preventing file corruption.

**Smart Numbering**: Handles complex routing logic (like zig-zag routes across multiple buildings) using modulo and integer division math.

## 🏗️ Architecture: The Strategy Pattern

The core naming logic is abstracted behind a StrategyInterface. This allows the application to scale to dozens of different condominiums without cluttering the main loop with if/else statements.

**Currently implemented strategies**:

**DefaultStrategy**: A standard sequential naming rule.

**VeredasStrategy**: A complex mathematical rule that maps a continuous timeline of photos into 17 distinct buildings, each with 20 apartments arranged in a 4-column zig-zag layout.

## 🛠️ Technologies Used

Java 26

Spring Boot (CommandLineRunner)

Google Drive API v3

## ⚙️ Setup and Execution

Google Cloud Credentials:

Create a project in the Google Cloud Console.

Enable the Google Drive API.

Generate a Service Account or OAuth 2.0 credentials and save the credentials.json file in your project resources.

### Run the Application:

Start the Spring Boot application.

The console will prompt you for the Google Drive Folder ID containing the raw photos.

Select the target Condominium Strategy (e.g., 1 for Default, 2 for Veredas).

The application will process, sort, and rename all files directly in the cloud.

💡 How it works (Veredas Example)

If the reader walks through 17 buildings taking 20 photos per building, the application sorts all 340 photos by their EXIF timestamps. Using mathematical operators (division and modulo), it calculates the exact building and apartment number based solely on the photo's chronological index, converting a raw file like IMG_999.HEIC into apto_01_11.HEIC.
