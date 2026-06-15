#!/usr/bin/env bash

# get base dir regardless of execution location
SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do # resolve $SOURCE until the file is no longer a symlink
  DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"
  SOURCE="$(readlink "$SOURCE")"
  [[ "$SOURCE" != /* ]] && SOURCE="$DIR/$SOURCE" # if $SOURCE was a relative symlink, we need to resolve it relative to the path where the symlink file was located
done
SOURCE=$([[ "$SOURCE" == /* ]] && echo "$SOURCE" || echo "$PWD/${SOURCE#./}")
basedir=$(dirname "$SOURCE")
. "$basedir"/scripts/init.sh

buildshstash() {
  STASHED=$(git stash)
}

buildshunstash() {
  if [[ "$STASHED" != "No local changes to save" ]]; then
    git stash pop
  fi
}

case "$1" in
"p" | "patch" | "apply")
  (
    set -e
    cd "$basedir"
    scripts/apply.sh "$basedir" || exit 1
  )
  ;;
"b" | "bu" | "build")
  (
    basedir
    cd ScalableLux-Patched
    ./gradlew clean build || exit 1
  )
  ;;

"rb" | "rbp" | "rebuild")
  (
    set -e
    cd "$basedir"
    scripts/rebuildpatches.sh "$basedir" || exit 1
  )
  ;;
"am" | "amend")
  (
    cd "$basedir"/ScalableLux-Patched/
    git add .
    git commit --amend --no-edit
    cd "$basedir"
    scripts/rebuildpatches.sh "$basedir" || exit 1
  )
  ;;
"up" | "upstream")
  (
    cd "$basedir"
    scripts/upstream.sh "$2" || exit 1
  )
  ;;
*)
  echo "build.sh build tool command. This provides a variety of commands to build and manage the build"
  echo "environment. For all of the functionality of this command to be available, you must first run the"
  echo "'setup' command. View below for details. For essential building and patching, you do not need to do the setup."
  echo ""
  echo " Normal commands:"
  echo "  * p, patch        | Apply all patches to top of Paper without building it"
  echo "  * b, build        | Build"
  echo "  * rb, rebuild     | Rebuild patches"
  echo "  * am, amend       | Amend current edits to last patches"
  echo "  * up, upstream    | Updates upstream"
  ;;
esac

unset -f buildshstash
unset -f buildshunstash

