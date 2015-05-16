#env bash 

run() {
    if [ -z $1 ]; then
        INI="production.ini"
    else
        INI="development.ini"
    fi
    mvn exec:java -Dexec.mainClass=goldpoisk_parser.Parser\
        -Dlog4j.configurationFile=log4j.xml\
        -Dexec.cleanupDaemonThreads=false\
        -Dexec.args=$INI
}

for opt in $@; do
    case $opt in
        --dev)
            #development
            run dev
            exit 0
            break
            ;;
        --prod)
            #production
            break;
            ;;
        *)
            ;;
    esac
done

#production
run
