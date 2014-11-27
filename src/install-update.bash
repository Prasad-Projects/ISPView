#!/bin/bash

javac -d ../bin/update -cp ../lib:../bin CreateDB.java

jar cfe ../bin/Updates.jar CreateDB ../bin/update/*.class
