# Dilshan Oil Mill POS - Windows installer

`dist/Dilshan Oil Mill Setup.exe` is the single setup file for a target Windows machine.

## Setup flow

1. **Step 1 - Check disk space**  
   Setup checks for at least 600 MB free space. Shows ✓ if OK, X if failed.
2. **Step 2 - Check MySQL**  
   Setup checks if MySQL is installed/running on port `3306`.  
   - ✓ MySQL found: can continue.  
   - X MySQL not found: **setup cannot continue**. Install MySQL Server first, then run setup again. Setup does **not** auto-install MySQL.
3. **Step 3 - MySQL username & password**  
   Enter MySQL admin username/password (default `root`) and click **Test Connection**.  
   - ✓ Connection OK: can continue.  
   - X Connection failed: cannot continue until correct credentials are provided.
4. **Step 4 - Save DB config**  
   Setup creates/verifies the `Oil` database and a dedicated `oil_app` user, then saves app DB config to:  
   `%LOCALAPPDATA%\Dilshan Oil Mill\database.properties`
5. **Step 5 - Install & launch app**  
   Setup launches the bundled application installer. Java 21 runtime and required font are bundled (no separate Java/font install needed). After install, launch **Dilshan Oil Mill** from Start Menu.

## Installing

1. Copy `dist/Dilshan Oil Mill Setup.exe` to the target machine.
2. **Run as administrator**.
3. Follow the setup steps above. If MySQL is not installed, install MySQL Server manually before running setup.
4. After setup completes, launch **Dilshan Oil Mill** from the Start menu. Database tables are created/validated on first start.

## Rebuilding the EXE

On a Windows machine with JDK 21 at `C:\Program Files\Java\jdk-21.0.10` and WiX Toolset 3:

```powershell
.\build-installer.ps1
```

The bundled font source is `src/main/resources/fonts/BebasNeue-Regular.ttf`, and the
fresh-install schema is `src/main/resources/db/schema.sql`.

## Printing

- **80mm receipts** are sent as raw ESC/POS data to a thermal printer using a large
  double-size font (24 characters per line). Select the printer with the
  `dilshan.thermal.printer` JVM system property or the `THERMAL_PRINTER` environment
  variable. If neither is set, the system default print service is used.
- If the thermal printer is **not selected, not connected, or the print fails**, the app
  automatically generates an 80mm-sized PDF from the same receipt and opens it in the
  default PDF viewer, so the receipt is still visible and can be printed from there.
- **A4 bills and A4 reports** are rendered from HTML templates to PDF using the HTML/PDF renderer and opened in the default PDF viewer.
