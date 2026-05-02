@echo off
set JAVA_HOME=C:\Program Files\Java\jdk-21.0.10
set APP_JAR=%~dp0KumaraCoconutOil-1.0-SNAPSHOT.jar
set JAVAFX_JARS=%USERPROFILE%\.m2\repository\org\openjfx\javafx-controls\21.0.11\javafx-controls-21.0.11-win.jar;%USERPROFILE%\.m2\repository\org\openjfx\javafx-fxml\21.0.11\javafx-fxml-21.0.11-win.jar;%USERPROFILE%\.m2\repository\org\openjfx\javafx-graphics\21.0.11\javafx-graphics-21.0.11-win.jar;%USERPROFILE%\.m2\repository\org\openjfx\javafx-base\21.0.11\javafx-base-21.0.11-win.jar

"%JAVA_HOME%\bin\java" --module-path "%JAVAFX_JARS%" --add-modules javafx.controls,javafx.fxml -jar "%APP_JAR%"
