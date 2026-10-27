#!/usr/bin/env bash
set -euo pipefail

cd "./pipeline/"
mvn --batch-mode clean verify
