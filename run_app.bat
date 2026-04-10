@echo off
set "MODULE_PATH=E:\COURS\COURS L2\JAVA\RESSOURCES_DE_JAVA\openjfx-21.0.9_windows-x64_bin-sdk\javafx-sdk-21.0.9\lib"
java --module-path "%MODULE_PATH%" --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base -cp "bin;lib/mysql-connector-j-8.0.32.jar" com.hotel.MainApp > out.txt 2>&1
echo Exit Code: %errorlevel% >> out.txt
