#!/bin/bash
# Run Kairowan-Ktor with proxy disabled

export JAVA_OPTS="-Djava.net.useSystemProxies=false -DsocksProxyHost= -DsocksProxyPort= -DhttpProxyHost= -DhttpProxyPort= -DhttpsProxyHost= -DhttpsProxyPort="

./gradlew :kairowan-app:run
