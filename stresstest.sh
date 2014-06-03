#!/bin/sh
rm builds.logs
while mvn clean install; do date >>builds.log; done
