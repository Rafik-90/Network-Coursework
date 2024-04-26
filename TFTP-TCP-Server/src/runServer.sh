#!/bin/bash

# Stop the script if any command fails
set -e

# Your Java JDK path
JAVA_HOME="/Users/rafiksongoku/Library/Java/JavaVirtualMachines/openjdk-22/Contents/Home"

# Directory where your project is located
PROJECT_DIR="/Users/rafiksongoku/Documents/NetCoursework/TFTP-TCP-Server"

# Ensure that Maven has compiled the project and the classes are up to date
mvn -f "$PROJECT_DIR/pom.xml" clean package

# Run the server and append output to server.log
"$JAVA_HOME/bin/java" \
  -javaagent:/Applications/IntelliJ\ IDEA.app/Contents/lib/idea_rt.jar=52601:/Applications/IntelliJ\ IDEA.app/Contents/bin \
  -Dfile.encoding=UTF-8 \
  -Dsun.stdout.encoding=UTF-8 \
  -Dsun.stderr.encoding=UTF-8 \
  -classpath "$PROJECT_DIR/target/classes" \
  Server 127.0.0.1 >> "$PROJECT_DIR/server.log" 2>&1

# Note: '>>' is used to append to the server.log file without overwriting it,
# and '2>&1' is used to also redirect stderr to the same file as stdout.

#to execute the script from the shell, apply these command respectively

## To use this script: find the path of your script first then:
 #
 ## 1- Save it as runServer.sh in a convenient location within your client project directory.
 ## 2- Make it executable with chmod +x runServer.sh.
 ## 3- Run the script with ./runServer.sh.
#
#