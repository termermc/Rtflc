#!/bin/bash
javadoc -d doc/ -sourcepath src/main/java/ -subpackages net.termer.rtflc -classpath build/libs/Rtflc-*-all.jar
