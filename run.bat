@echo off
chcp 65001 > nul
echo === Personal Finance Manager ===
echo.
mvn clean compile exec:java
pause