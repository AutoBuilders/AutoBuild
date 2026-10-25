#!/usr/bin/env bash

set -euo pipefail

cd pipeline-ui

echo "debug logs"
ls
echo "ended debug logs"

npm ci
npm run lint
npm run generate:clean-client-code
npm run test:coverage-pipeline
