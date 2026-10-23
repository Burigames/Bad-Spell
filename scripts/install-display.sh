#!/usr/bin/env bash
set -euo pipefail
# Optional local-only graphical QA. APT verifies Debian signatures and package checksums.
badspell_apt=/workspace/toolchains/apt
mkdir -p "$badspell_apt"/{lists/partial,archives/partial,log,empty} /workspace/toolchains/x11
cat > "$badspell_apt/cloud.conf" <<'CONFIG'
Dir::Etc::parts "/workspace/toolchains/apt/empty";
Dir::Etc::main "/workspace/toolchains/apt/empty/main";
Dir::Etc::sourcelist "/workspace/toolchains/apt/cloud.sources.list";
Dir::Etc::sourceparts "/workspace/toolchains/apt/empty";
Dir::State::lists "/workspace/toolchains/apt/lists";
Dir::Cache::archives "/workspace/toolchains/apt/archives";
Dir::Cache::pkgcache "/workspace/toolchains/apt/pkgcache.bin";
Dir::Cache::srcpkgcache "/workspace/toolchains/apt/srcpkgcache.bin";
Dir::Log "/workspace/toolchains/apt/log";
APT::Sandbox::User "agent";
CONFIG
python3 - <<'PY'
import os
from urllib.parse import urlsplit
from pathlib import Path
proxy=urlsplit(os.environ.get('HTTPS_PROXY',''))
if proxy.username or proxy.password:
    raise SystemExit('Use platform package-manager proxy support; credentials will not be written')
if proxy.hostname and proxy.port:
    with Path('/workspace/toolchains/apt/cloud.conf').open('a') as f:
        f.write(f'Acquire::https::Proxy "{proxy.scheme}://{proxy.hostname}:{proxy.port}";\n')
PY
cat > "$badspell_apt/cloud.sources.list" <<'SOURCES'
deb [signed-by=/usr/share/keyrings/debian-archive-keyring.gpg] https://deb.debian.org/debian trixie main
deb [signed-by=/usr/share/keyrings/debian-archive-keyring.gpg] https://security.debian.org/debian-security trixie-security main
SOURCES
export APT_CONFIG="$badspell_apt/cloud.conf"
apt-get update
cd "$badspell_apt/archives"
apt-get download xvfb=2:21.1.16-1.3+deb13u4 xdotool=1:3.20160805.1-5.1 libxdo3=1:3.20160805.1-5.1
for package in xvfb_*deb13u4_amd64.deb xdotool_*.deb libxdo3_*.deb; do
    dpkg-deb -x "$package" /workspace/toolchains/x11
done
