@echo off
REM Build bot plugin JARs for Banana Republic
REM Run from the project root: cd test-jar\test-bot && build-bots.bat
REM Requires: mvn compile already run (target\classes must exist)

set CLASSES_DIR=..\..\target\classes
set SRC_DIR=src
set OUT_DIR=out

if not exist "%OUT_DIR%" mkdir "%OUT_DIR%"

echo === Compiling GreedyBot ===
javac -cp "%CLASSES_DIR%" -d "%OUT_DIR%" "%SRC_DIR%\GreedyBot.java"
if errorlevel 1 ( echo FAILED GreedyBot.java & exit /b 1 )

echo === Packaging greedy-bot.jar ===
jar cf greedy-bot.jar -C "%OUT_DIR%" GreedyBot.class
echo Created: greedy-bot.jar

echo === Compiling AggressiveBot ===
javac -cp "%CLASSES_DIR%" -d "%OUT_DIR%" "%SRC_DIR%\AggressiveBot.java"
if errorlevel 1 ( echo FAILED AggressiveBot.java & exit /b 1 )

echo === Packaging aggressive-bot.jar ===
jar cf aggressive-bot.jar -C "%OUT_DIR%" AggressiveBot.class
echo Created: aggressive-bot.jar

echo === Compiling SmartBot ===
javac -cp "%CLASSES_DIR%" -d "%OUT_DIR%" "%SRC_DIR%\SmartBot.java"
if errorlevel 1 ( echo FAILED SmartBot.java & exit /b 1 )

echo === Packaging smart-bot.jar ===
jar cf smart-bot.jar -C "%OUT_DIR%" SmartBot.class
echo Created: smart-bot.jar

echo.
echo === Build complete! ===
echo   greedy-bot.jar     - 4:1 bank trader, maximizes resource diversity
echo   aggressive-bot.jar - Hunts ORE+BANANA+WHEAT for Dev Cards
echo   smart-bot.jar      - Adaptive: picks easiest goal, uses harbours
