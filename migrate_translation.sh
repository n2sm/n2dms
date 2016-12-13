#!/bin/bash
CWD="${0%/*}"

is_bsd=false
if [ "$(uname)" == 'Darwin' ]; then
  is_bsd=true
fi

usage() {
  echo "Usage: $0 new_en-GB.sql old_ja-JP.sql"
  echo "Options:"
  echo "  -h, --help   print this usage and exit"
  exit 1
}

for OPT in $*
do
  case $OPT in
    '-h'|'--help' )
      usage
      ;;
    '--'|'-' )
      shift 1
      param+=( "$@" )
      break
      ;;
    -*)
      echo "$PROGNAME: illegal option -- '$(echo $1 | sed 's/^-*//')'" 1>&2
      exit 1
      ;;
    *)
      if [[ ! -z "$1" ]] && [[ ! "$1" =~ ^-+ ]]; then
        #param=( ${param[@]} "$1" )
        param+=( "$1" )
        shift 1
      fi
    ;;
  esac
done

if [ ${#param[@]} != 2 ]; then
    usage
fi

new_en_sql=${param[0]}
old_ja_sql=${param[1]}

JA_JP="ja-JP"
REGEXP="INSERT INTO OKM_TRANSLATION[[:space:]]*.*[[:space:]]*VALUES[[:space:]]*\([[:space:]]*\'([^\']*)\'[[:space:]]*,[[:space:]]*\'([^\']*)\'[[:space:]]*,[[:space:]]*\'([^\']*)\'[[:space:]]*,[[:space:]]*\'([^\']*)\'[[:space:]]*\)"

lines_ja=()

getTextJa() {
  module=$1
  key=$2
  local line
  echo ${lines_ja[@]} | /usr/bin/grep "\'${key}\'" "${old_ja_sql}" | /usr/bin/grep "\'${module}\'" | while read line; do
    if [[ $line =~ $REGEXP ]]; then
      # TR_MODULE, TR_KEY, TR_TEXT, TR_LANGUAGE0
      echo "${BASH_REMATCH[3]}" # TR_TEXT
      return
    fi
  done
}

not_translated=()

# whole japanese file into the array
IFS=$'\r\n' GLOBIGNORE='*' command eval 'lines_ja=($(cat ${old_ja_sql}))'

n=0
while read line || [ -n "$line" ]; do
  (( n++ ))
  if [[ ! $line =~ $REGEXP ]]; then
    echo $line
    continue
  fi

  # TR_MODULE, TR_KEY, TR_TEXT, TR_LANGUAGE0
  module=${BASH_REMATCH[1]}
  key=${BASH_REMATCH[2]}
  text=${BASH_REMATCH[3]}
  language=${BASH_REMATCH[4]}

  text_ja=$(getTextJa $module $key)

  if [ -n "${text_ja}" ]; then
    line=`echo $line | sed -e "s@\'${text}\'@\'${text_ja}\'@" | sed -e "s@\'${language}\'@\'${JA_JP}\'@"`
  else
    line=`echo $line | sed -e "s@\'${language}\'@\'${JA_JP}\'@"`
    not_translated=("${not_translated[@]}" "${n}:${line}")
  fi

  echo "${line}"
done < ${new_en_sql}

if [ ${#not_translated[@]} -gt 0 ]; then
  echo ""
  echo "### The following are not translated"
  for item in "${not_translated[@]}"; do
    echo "# ${item}"
  done
fi

