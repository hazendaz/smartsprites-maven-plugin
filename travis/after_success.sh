#!/bin/bash
#
# Copyright (c) 2012-2017 Hazendaz.
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of The Apache Software License,
# Version 2.0 which accompanies this distribution, and is available at
# http://www.apache.org/licenses/LICENSE-2.0.txt
#
# Contributors:
#     Hazendaz (Jeremy Landis).
#


# Get Commit Message
commit_message=$(git log --format=%B -n 1)
echo "Current commit detected: ${commit_message}"

# We build for several JDKs on Travis.
# Some actions, like analyzing the code (Coveralls) and uploading
# artifacts on a Maven repository, should only be made for one version.
 
# If the version is 1.8, then perform the following actions.
# 1. Upload artifacts to Sonatype.
# 2. Use -q option to only display Maven errors and warnings.
# 3. Use --settings to force the usage of our "settings.xml" file.
# 4. Notify Coveralls.
# 5. Deploy site

if [ $TRAVIS_REPO_SLUG == "hazendaz/smartsprites-maven-plugin" ] && [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ "$TRAVIS_BRANCH" == "master" ] && [[ "$commit_message" != *"[maven-release-plugin]"* ]]; then

  if [ $TRAVIS_JDK_VERSION == "oraclejdk8" ]; then

    # Deploy to sonatype
    # TODO Turn this on once gems are configured
    # ./mvnw clean deploy -q -Dinvoker.skip=true --settings ./travis/settings.xml
    # echo -e "Successfully deployed SNAPSHOT artifacts to Sonatype under Travis job ${TRAVIS_JOB_NUMBER}"

	# Deploy to coveralls
    # ./mvnw clean test jacoco:report coveralls:report -q --settings ./travis/settings.xml
    # echo -e "Successfully ran coveralls under Travis job ${TRAVIS_JOB_NUMBER}"

    # Deploy to site
    # Cannot currently run site this way
	# ./mvnw site site:deploy -q --settings ./travis/settings.xml
	# echo -e "Successfully deploy site under Travis job ${TRAVIS_JOB_NUMBER}"
  else
    echo "Java Version does not support additional activity for travis CI"
  fi
else
  echo "Travis Pull Request: $TRAVIS_PULL_REQUEST"
  echo "Travis Branch: $TRAVIS_BRANCH"
  echo "Travis build skipped"
fi