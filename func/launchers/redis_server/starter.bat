@echo off
chcp 65001 > nul
echo [启动 redis]
sc query |find /i "redis"
if not errorlevel 1 goto:eof
echo 正在启动redis...
cd /d F:\phpstudy_pro\Extensions\redis3.0.504
redis-server --service-start
echo redis启动完毕！
exit
