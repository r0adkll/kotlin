#!/usr/bin/env bash

set -ex

VERSION_CODE=$(sed -nE 's/VERSION_NAME=(.*)/\1/p' gradle.properties)
echo "VERSION_CODE=$VERSION_CODE" >> "$GITHUB_ENV"
