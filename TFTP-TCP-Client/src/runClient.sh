#!/bin/bash

# Stop the script if any command fails
set -e

# Your Java JDK path
JAVA_HOME="/Users/rafiksongoku/Library/Java/JavaVirtualMachines/openjdk-22/Contents/Home"

# Directory where your client project is located
PROJECT_DIR="/Users/rafiksongoku/Documents/NetCoursework/TFTP-TCP-Client"

# Ensure that Maven has compiled the project and the classes are up to date
mvn -f "$PROJECT_DIR/pom.xml" clean package

# Run the client and append output to client.log
# Use 'tee' to see output in the console as well as log it
"$JAVA_HOME/bin/java" \
  -javaagent:/Applications/IntelliJ\ IDEA.app/Contents/lib/idea_rt.jar=54408:/Applications/IntelliJ\ IDEA.app/Contents/bin \
  -Dfile.encoding=UTF-8 \
  -Dsun.stdout.encoding=UTF-8 \
  -Dsun.stderr.encoding=UTF-8 \
  -classpath "$PROJECT_DIR/target/classes" \
  Client 127.0.0.1 | tee -a "$PROJECT_DIR/client.log"

# The client will accept user input, and the output will be logged in client.log
# this work load is extra just to keep myself always up to date with running script of any project on different shell, im
# fully aware that this is not graded work, but i just wanted to add it to my work as it is interesting, you could run the script from the script
#or from the shell bash by executing like this:

### firstly you have to run server or make sure a server is on around you roughly then: use the steps below:

# To use this script: first off, find the path to your script then :

# 1- Save it as runClient.sh in a convenient location within your client project directory.
# 2- Make it executable with chmod +x runClient.sh.
# 3- Run the script with ./runClient.sh.


#                           RRQ OR WRQ PACKET
##########################################################################
# Operation Code  // File Name // 0 //      Mode    //        0         //
##########################################################################
#    2 BYTES   //   STRING    // 1 BYTE    //   STRING  //    1 byte    //
##########################################################################
