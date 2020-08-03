#!/bin/bash

set -eo pipefail

git submodule update --init
mvn package
java -jar target/ieml-reasoner-1.0-SNAPSHOT-jar-with-dependencies.jar
