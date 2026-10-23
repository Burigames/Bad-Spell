# Source from bash for this managed environment. Does not write secrets or global Java configuration.
export JAVA_HOME=/workspace/toolchains/jdk-25.0.5.0.2
export GRADLE_USER_HOME=/workspace/.gradle
export PATH="$JAVA_HOME/bin:$PATH"
if [[ -n "${HTTPS_PROXY:-}" ]]; then
    badspell_proxy_options=$(python3 - <<'PY'
import os
from urllib.parse import urlsplit
proxy=urlsplit(os.environ['HTTPS_PROXY'])
if proxy.username or proxy.password:
    raise SystemExit('Authenticated proxy requires platform Java configuration; no credentials will be exported')
if proxy.hostname and proxy.port:
    print(f'-Dhttps.proxyHost={proxy.hostname} -Dhttps.proxyPort={proxy.port} -Dhttp.proxyHost={proxy.hostname} -Dhttp.proxyPort={proxy.port} -Dhttp.nonProxyHosts=localhost|127.*')
PY
) || return 1
    export JAVA_TOOL_OPTIONS="${JAVA_TOOL_OPTIONS:-} $badspell_proxy_options -Djavax.net.ssl.trustStore=/etc/ssl/certs/java/cacerts"
    unset badspell_proxy_options
fi
