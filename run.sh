#!/bin/bash
set -e

DIR="$(cd "$(dirname "$0")" && pwd)"
JAVAFX="$DIR/libs/javafx-sdk-23.0.2/lib"
OUT="$DIR/out"

# Locate a JDK (with javac)
find_javac() {
    if command -v javac >/dev/null 2>&1; then
        echo "$(command -v javac)"
        return
    fi
    for d in "$JAVA_HOME/bin" /usr/lib/jvm/*/bin; do
        if [ -x "$d/javac" ]; then
            echo "$d/javac"
            return
        fi
    done
    return 1
}

JAVAC="$(find_javac || true)"
if [ -z "$JAVAC" ]; then
    echo "ERROR: javac no encontrado. Instala un JDK o configura JAVA_HOME." >&2
    exit 1
fi

[ -d "$OUT" ] || mkdir -p "$OUT"

"$JAVAC" --module-path "$JAVAFX" \
    --add-modules javafx.controls,javafx.fxml \
    -d "$OUT" \
    -sourcepath "$DIR/src" \
    "$DIR/src/Horse.java" "$DIR/src/HorseRaceApp.java"

cp "$DIR/src/styles.css" "$OUT/styles.css"

java --module-path "$JAVAFX" \
    --add-modules javafx.controls,javafx.fxml \
    -cp "$OUT" \
    HorseRaceApp