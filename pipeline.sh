cd /home/git/kufar
git pull origin master
kill -9 $(ps aux | grep 'kufar.jar' | awk '{print $2}') 2>/dev/null
nohup java -jar kufar.jar > kufarV.log 2>&1 &