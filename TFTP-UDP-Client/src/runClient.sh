#!/bin/bash

# Define the path to the Java executable
JAVA_EXEC="/Users/rafiksongoku/Library/Java/JavaVirtualMachines/openjdk-22/Contents/Home/bin/java"

# Set system properties for encoding
SYSTEM_PROPERTIES="-Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8"

# Set the classpath to the compiled classes directory
CLASSPATH="-classpath /Users/rafiksongoku/Documents/NetCoursework/TFTP-UDP-Client/target/classes"

# Main class to run, and server parameters such as host address
MAIN_CLASS="Client"
SERVER_PARAMS="127.0.0.1"

# Define the log file path
LOG_FILE="/Users/rafiksongoku/Documents/NetCoursework/TFTP-UDP-Client/logs/client.log"

# Ensure the directory for the log file exists
mkdir -p "$(dirname "$LOG_FILE")"

# Run the Java application with the specified settings
echo "Running client without IntelliJ IDEA javaagent..."
$JAVA_EXEC $SYSTEM_PROPERTIES $CLASSPATH $MAIN_CLASS $SERVER_PARAMS | tee "$LOG_FILE"
