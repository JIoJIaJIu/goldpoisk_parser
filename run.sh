#env bash 
mvn exec:java -Dexec.mainClass=goldpoisk_parser.Parser -Dlog4j.configurationFile=log4j.xml -Dexec.cleanupDaemonThreads=false
