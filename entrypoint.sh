#!/bin/sh
set -e

# Railway는 $PORT 환경변수로 외부 노출 포트를 알려준다. 없으면 8080.
PORT="${PORT:-8080}"

# Tomcat server.xml의 HTTP 커넥터 포트를 $PORT로 치환
sed -i "s/Connector port=\"8080\"/Connector port=\"${PORT}\"/" \
    /usr/local/tomcat/conf/server.xml

# UTF-8 + 컨테이너 친화 옵션
export CATALINA_OPTS="-Dfile.encoding=UTF-8 -Duser.timezone=Asia/Seoul -Djava.security.egd=file:/dev/./urandom ${CATALINA_OPTS:-}"

echo "[entrypoint] Starting Tomcat on port ${PORT}"
exec catalina.sh run
