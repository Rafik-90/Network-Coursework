#!/bin/bash

# Define the path to the Java executable
JAVA_EXEC="/Users/rafiksongoku/Library/Java/JavaVirtualMachines/openjdk-22/Contents/Home/bin/java"

# Define the Java agent with the IntelliJ IDEA debugger support
# Enclose the path in quotes to handle spaces correctly
JAVA_AGENT="-javaagent:\"/Applications/IntelliJ IDEA.app/Contents/lib/idea_rt.jar=54226:/Applications/IntelliJ IDEA.app/Contents/bin\""

# Set system properties for encoding
SYSTEM_PROPERTIES="-Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8"

# Set the classpath to the compiled classes directory
CLASSPATH="-classpath /Users/rafiksongoku/Documents/NetCoursework/TFTP-UDP-Server/target/classes"

# Main class to run, and server parameters such as host address
MAIN_CLASS="Server"
SERVER_PARAMS="127.0.0.1"

# Define the log file path
LOG_FILE="/Users/rafiksongoku/Documents/NetCoursework/TFTP-UDP-Server/logs/server.log"

# Ensure the directory for the log file exists
mkdir -p "$(dirname "$LOG_FILE")"

# Run the Java application with the specified settings and redirect output to the log file
echo "Running server with IntelliJ IDEA javaagent... Output will be logged to $LOG_FILE"
$JAVA_EXEC $SYSTEM_PROPERTIES $CLASSPATH $MAIN_CLASS $SERVER_PARAMS >> "$LOG_FILE" 2>&1
