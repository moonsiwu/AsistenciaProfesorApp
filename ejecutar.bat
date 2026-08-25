@echo off
chcp 65001 > nul
echo ========================================================
echo   Iniciando Sistema de Control de Asistencia
echo ========================================================
echo.

cd /d "%~dp0AsistenciaProfesorApp"

if not exist "bin" mkdir "bin"

echo Compilando codigo fuente Java...
dir /s /b src\main\java\*.java > sources.txt
javac -encoding UTF-8 -cp "lib\*" -d bin @sources.txt
del sources.txt

if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Hubo un problema al compilar el proyecto.
    pause
    exit /b %errorlevel%
)

echo Copiando archivos de configuracion...
copy /Y src\main\resources\* bin\ > nul

echo Ejecutando aplicacion con interfaz moderna...
echo.
java -Dfile.encoding=UTF-8 -cp "bin;lib\*" com.institucion.asistencia.Main

pause
