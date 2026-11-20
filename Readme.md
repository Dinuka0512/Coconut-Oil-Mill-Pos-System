# Dilshan Oil Mill POS - Windows installer

`dist/Dilshan Oil Mill Setup.exe` is the single setup file for a target Windows machine.

## What the setup checks

- 64-bit Windows and at least 600 MB free disk space
- MySQL Server installed, service running, and listening on `localhost:3306`
- Java 21 runtime: bundled inside the application installer (no system Java needed)
- Bebas Neue font: bundled inside the application (no system font install needed)

MySQL must already be installed and running on the target machine. The app connects as
`root` / `root` and creates the `Oil` database and all tables automatically on first
launch (the schema script is bundled in the JAR).

## Installing

1. Copy `dist/Dilshan Oil Mill Setup.exe` to the target machine.
2. Run it. The requirement check is shown first; if any check fails, fix it and re-run.
3. Click **Install Application** and complete the application installer wizard.
4. Launch **Dilshan Oil Mill** from the Start menu. The database is prepared on first start.

## Rebuilding the EXE

On a Windows machine with JDK 21 at `C:\Program Files\Java\jdk-21.0.10`, WiX Toolset 3,
and MySQL 8 running locally with `root` / `root`:

```powershell
.\build-installer.ps1
```

This builds the JAR, creates the jpackage application installer, embeds it into the
prerequisite-checking setup EXE, and writes both files into `dist/`.

The bundled font source is `src/main/resources/fonts/BebasNeue-Regular.ttf`, and the
fresh-install schema is `src/main/resources/db/schema.sql`.
