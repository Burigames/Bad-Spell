#!/usr/bin/env bash
set -euo pipefail
cd /workspace/Bad-Spell
mkdir -p /workspace/toolchains/downloads
badspell_archive=/workspace/toolchains/downloads/badspell-jdk25.tar.gz
if [[ ! -f "$badspell_archive" ]]; then
    curl -fL --retry 2 https://download.oracle.com/java/25/latest/jdk-25_linux-x64_bin.tar.gz -o "$badspell_archive"
fi
# Pin the verified release; a future change to Oracle's latest URL must fail, never silently upgrade.
printf '%s  %s\n' d430d3f9e91435d2d58d366b1dabf14b23fcf8629ed4b041757c1be444055946 "$badspell_archive" | sha256sum -c -
if [[ ! -x /workspace/toolchains/jdk-25.0.5.0.2/bin/javac ]]; then
    tar -xzf "$badspell_archive" -C /workspace/toolchains
fi
if [[ ! -x /workspace/toolchains/x11/usr/bin/Xvfb ]]; then
    ./scripts/install-display.sh
fi
source scripts/cloud-env.sh
./scripts/test-core.sh
./gradlew --no-daemon :core:check build --max-workers=2
