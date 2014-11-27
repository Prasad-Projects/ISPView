#!/bin/bash

javac -d ../bin/ISPView -cp ../lib:../bin ISPView/cop/NotifyUser.java
javac -d ../bin/ISPView -cp ../lib:../bin ISPView/cop/NoResultsException.java
javac -d ../bin/ISPView -cp ../lib:../bin ISPView/cop/ToolsClass.java
javac -d ../bin/ISPView -cp ../lib:../bin ISPView/cop/UpdateThread.java
javac -d ../bin/ISPView -cp ../lib:../bin ISPView/cop/PruneThread.java

javac -d ../bin/ISPView -cp ../lib:../bin ISPView/cop/MainFrame.java

javac -d ../bin/ISPView -cp ../lib:../bin ISPView/cop/PatriciaTest.java

javac -d ../bin/ISPView -cp ../lib:../bin ISPView/cop/GraphClass.java
javac -d ../bin/ISPView -cp ../lib:../bin ISPView/cop/RegionGraphsThread.java
javac -d ../bin/ISPView -cp ../lib:../bin ISPView/cop/GraphThread.java
javac -d ../bin/ISPView -cp ../lib:../bin ISPView/cop/GraphTools.java

javac -d ../bin/ISPView -cp ../lib:../bin ISPView/cop/DBTest.java

jar cfe ../bin/ISPView.jar MainFrame ../bin/ISPView/*.class
