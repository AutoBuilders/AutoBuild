#!/usr/bin/env bash
set -euo pipefail

cd "./pipeline/"
mvn --batch-mode -s maven-settings.xml verify -P integration-test -DskipTests
