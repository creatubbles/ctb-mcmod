#!/bin/bash

#REQUIRED_VARIABLES
GITHUB_ACCESS_TOKEN=${GITHUB_ACCESS_TOKEN:?'You need to configure the GITHUB_ACCESS_TOKEN environment variable!'}
GITHUB_REPO_OWNER=${GITHUB_REPO_OWNER:?'You need to configure the REPO_OWNER environment variable!'}
GITHUB_ACCESS_TOKEN=${GITHUB_ACCESS_TOKEN:?'You need to configure the REPO_OWNER environment variable!'}

CURRENT_BRANCH=$(git rev-parse --abbrev-ref head)
LATEST_COMMIT=$(git rev-parse --short head)

#OPTIONAL_VARIABLES - if not defined, default values are assigned to generate uniq version names
GIT_RELEASE_TAG_NAME=${GIT_RELEASE_TAG_NAME:="v"$CURRENT_BRANCH}
GIT_RELEASE_VERSION=${GIT_RELEASE_VERSION="${GIT_RELEASE_TAG_NAME}-${LATEST_COMMIT}"}

set -e

CREATE_RELEASE_COMMAND="curl -H 'Accept: application/json' -H 'Authorization: Bearer ${GITHUB_ACCESS_TOKEN}' -d '{ \"tag_name\": \"${GIT_RELEASE_TAG_NAME}\", \"target_commitish\": \"${CURRENT_BRANCH}\", \"name\": \"${GIT_RELEASE_VERSION}\", \"draft\": true}' -X POST https://api.github.com/repos/${GITHUB_REPO_OWNER}/${GITHUB_REPO_NAME}/releases"

JSON=`eval $CREATE_RELEASE_COMMAND` 
BASE_UPLOAD_URL=`echo $JSON | awk '
BEGIN { FS=", " }
{
  URL=""
  for(i=1; i<=NF; i++) {
    split($i, args, ": ")
    if (args[1]=="\"upload_url\"") {
      split(args[2], url, "{")
      URL=substr(url[1], 2)
    }
  }
  printf URL
}
'`
for upload in $(ls -1 ./build/libs/*.jar)
do 
  file=`basename $upload`
  UPLOAD_URL=$BASE_UPLOAD_URL"?name='"$file"'"
  UPLOAD_COMMAND="curl -i -H 'Content-Type: application/java-archive' -H 'Accept: application/json' -H 'Authorization: Bearer ${GITHUB_ACCESS_TOKEN}' -T $x -X POST ${UPLOAD_URL}"
  #echo $UPLOAD_COMMAND
  eval $UPLOAD_COMMAND
done

