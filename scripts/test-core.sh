#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
: "${JAVA_HOME:?Set JAVA_HOME to a JDK 25 installation}"
version=$("$JAVA_HOME/bin/javac" -version 2>/dev/null)
case "$version" in 'javac 25'*) ;; *) printf 'JDK 25 required; found %s\n' "$version" >&2; exit 1;; esac
mkdir -p core/build/classes/manual
find core/src/main/java core/src/coreTest/java -name '*.java' -print > core/build/manual-sources.txt
"$JAVA_HOME/bin/javac" --release 25 -Xlint:all -Werror -d core/build/classes/manual @core/build/manual-sources.txt
"$JAVA_HOME/bin/java" -cp core/build/classes/manual com.badspell.CoreTests
