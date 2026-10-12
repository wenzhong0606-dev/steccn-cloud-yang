@echo off
setlocal
cd /d "%~dp0"
echo EAM prototype static web server
echo URL: http://127.0.0.1:8765/
echo Keep this window open while using the prototype.
echo After the server starts, open the URL in your browser.
where py >nul 2>nul
if %errorlevel%==0 goto use_py
where python >nul 2>nul
if %errorlevel%==0 goto use_python
echo ERROR: Python not found. Please install Python 3 or run another static HTTP server.
pause
exit /b 1
:use_py
py -3 -m http.server 8765 --bind 127.0.0.1
goto done
:use_python
python -m http.server 8765 --bind 127.0.0.1
:done
if errorlevel 1 (
 echo Server failed. Check whether port 8765 is already in use.
 pause
)
endlocal
