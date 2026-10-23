# Source from bash. Virtual display processes must be restarted in each new cloud task.
badspell_display_state=/workspace/.cache/badspell-display
mkdir -p "$badspell_display_state" /workspace/.cache/mesa
if [[ ! -x /workspace/toolchains/x11/usr/bin/Xvfb ]]; then
    printf '%s\n' 'Run ./scripts/install-display.sh first.' >&2
    return 1
fi
badspell_display_running=false
if [[ -s "$badspell_display_state/pid" && -s "$badspell_display_state/number" ]]; then
    badspell_display_pid=$(cat "$badspell_display_state/pid")
    if [[ "$(ps -p "$badspell_display_pid" -o comm= 2>/dev/null)" == Xvfb ]]; then
        badspell_display_running=true
    fi
fi
if [[ "$badspell_display_running" == false ]]; then
    : > "$badspell_display_state/number"
    nohup /workspace/toolchains/x11/usr/bin/Xvfb -displayfd 3 -screen 0 1280x800x24 -nolisten tcp -ac \
        3>"$badspell_display_state/number" >"$badspell_display_state/server.log" 2>&1 &
    printf '%s\n' "$!" > "$badspell_display_state/pid"
    for badspell_display_attempt in {1..20}; do
        [[ -s "$badspell_display_state/number" ]] && break
        sleep 0.1
    done
fi
if [[ ! -s "$badspell_display_state/number" ]]; then
    printf '%s\n' 'Virtual display failed; inspect /workspace/.cache/badspell-display/server.log' >&2
    return 1
fi
export DISPLAY=":$(cat "$badspell_display_state/number")"
export LIBGL_ALWAYS_SOFTWARE=1
export MESA_SHADER_CACHE_DIR=/workspace/.cache/mesa
# The cloud has no physical sound device; the null backend keeps audio initialization usable.
export ALSOFT_DRIVERS=null
unset badspell_display_running badspell_display_pid badspell_display_attempt badspell_display_state
