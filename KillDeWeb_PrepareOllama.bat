@echo off
echo Stopping Dr.Web services...
net stop "Dr.Web Control Service" /y
net stop "Dr.Web Scanning Engine" /y
echo Killing remaining processes...
taskkill /f /im dw*.exe
echo Done! Heavy coding mode activated.
pause