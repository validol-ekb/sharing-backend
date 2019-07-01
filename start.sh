#!/usr/bin/env bash

exec java \
    -XX:NativeMemoryTracking=summary \
    -XshowSettings:vm \
    -jar /app/assembly.jar