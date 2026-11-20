#!/usr/bin/env bash
set -euo pipefail

cd pipeline-ui

npm ci

# Dereference symlinks so Vite can resolve modules from this directory
find . -maxdepth 5 -type l ! -path './node_modules/*' | while read -r f; do
  cp -Lf "$f" "${f}.deref" && mv "${f}.deref" "$f"
done

npm run generate:clean-client-code
npm run local-with-mock
