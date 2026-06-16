#!/usr/bin/env bash

# get base dir regardless of execution location
SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do # resolve $SOURCE until the file is no longer a symlink
  DIR="$(cd -P "$(dirname "$SOURCE")" && pwd)"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE" # if $SOURCE was a relative symlink, we need to resolve it relative to the path where the>
done
. "$(dirname "$SOURCE")/init.sh"


(
  set -e
  PS1="$"

  function changelog() {
    base=$(git ls-tree HEAD "$1" | cut -d' ' -f3 | cut -f1)
    cd "$1" && git log --oneline "${base}"...HEAD
  }
  upstreamLog=$(changelog "$upstreamDir")

  updated=""
  logsuffix=""
  if [ -n "$upstreamLog" ]; then
    logsuffix="$logsuffix\n\nUpstream Changes:\n$upstreamLog"
  fi
  disclaimer="Upstream has released updates that appear to apply and compile correctly"

  if [ -n "$1" ]; then
    disclaimer=("$@")
  fi

  log="${UP_LOG_PREFIX}Updated Upstream\n\n${disclaimer[*]}${logsuffix}"

  echo -e "$log" | git commit -F -

) || exit 1

