#!/usr/bin/env bash
set -euo pipefail

cd "./pipeline/"
SPRING_PROFILES_ACTIVE=default,sqlite \
SPRING_JPA_SHOW_SQL=false \
mvn --batch-mode -s maven-settings.xml spring-boot:run
