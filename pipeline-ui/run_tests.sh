#!/usr/bin/env bash

set -euo pipefail

cd pipeline-ui

echo "debug logs"
ls
echo "ended debug logs"

npm ci

# Bazel runfiles are symlinks into the source tree. Vite and ESLint resolve
# modules relative to each file's real path, missing the runfiles node_modules.
# Dereference all source symlinks so tools resolve from this directory instead.
find . -maxdepth 4 -type l ! -path './node_modules/*' | while read -r f; do
  cp -Lf "$f" "${f}.deref" && mv "${f}.deref" "$f"
done

./node_modules/.bin/eslint src
npm run generate:clean-client-code
npm run test:coverage-pipeline

