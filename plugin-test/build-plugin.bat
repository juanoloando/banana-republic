@echo off
REM Build test plugin JARs for Banana Republic card plugin system
REM Run this from the project root directory (d:\tugas-besar-2-frp)
REM Requires Maven to have already compiled the project (mvn compile)

set CLASSES_DIR=..\target\classes
set SRC_DIR=src
set OUT_DIR=out

REM Create output directory
if not exist "%OUT_DIR%" mkdir "%OUT_DIR%"

echo === Compiling BonusResourceCard ===
javac -cp "%CLASSES_DIR%" -d "%OUT_DIR%" "%SRC_DIR%\BonusResourceCard.java"
if errorlevel 1 (
    echo FAILED to compile BonusResourceCard.java
    exit /b 1
)

echo === Packaging bonus-resource-card.jar ===
jar cf bonus-resource-card.jar -C "%OUT_DIR%" BonusResourceCard.class
if errorlevel 1 (
    echo FAILED to create bonus-resource-card.jar
    exit /b 1
)
echo Created: bonus-resource-card.jar

echo === Compiling WildcardCard ===
javac -cp "%CLASSES_DIR%" -d "%OUT_DIR%" "%SRC_DIR%\WildcardCard.java"
if errorlevel 1 (
    echo FAILED to compile WildcardCard.java
    exit /b 1
)

echo === Packaging wildcard-card.jar ===
jar cf wildcard-card.jar -C "%OUT_DIR%" WildcardCard.class
if errorlevel 1 (
    echo FAILED to create wildcard-card.jar
    exit /b 1
)
echo Created: wildcard-card.jar

echo.
echo === Multi-card plugin (both cards in one JAR) ===
jar cf test-plugin-bundle.jar -C "%OUT_DIR%" BonusResourceCard.class -C "%OUT_DIR%" WildcardCard.class
echo Created: test-plugin-bundle.jar

echo.
echo === Build complete! ===
echo Test JARs are in: plugin-test\
echo   bonus-resource-card.jar  - Gives 2 Wood from bank
echo   wildcard-card.jar        - Gives 1 Banana from bank
echo   test-plugin-bundle.jar   - Both cards in one JAR
echo.
echo To test: Start a game, click SAVE button, choose "Load Card Plugin", select a JAR.
