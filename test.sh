#!/bin/bash

if [[ !$LUCEE_VERSION ]]; then
	LUCEE_VERSION=5.3.1.87-RC
fi

if [ -f "java/jars/lucee-light-$LUCEE_VERSION.jar" ]; then
	echo "lucee-light-$LUCEE_VERSION.jar already there, skipping download"
else 
	#download lucee jar
	echo "Downloading lucee-light-$LUCEE_VERSION.jar"
	curl --location -o java/jars/lucee-light-$LUCEE_VERSION.jar https://cdn.lucee.org/lucee-light-$LUCEE_VERSION.jar
fi


cd java

#compile java
gradle build

cd ..

cp java/jars/lucee-light-$LUCEE_VERSION.jar test/jars/

cp java/build/libs/foundeo-fuseless.jar test/jars/

cd test

gradle build

sam local start-api --port 3003 --debug &

SAM_PID=$!


#give it a chance to startup
echo -e "Sleeping for 5...\n"
sleep 5


echo "Running: http://127.0.0.1:3003/test.cfm"
http_code=$(curl -s -o /tmp/result.txt -w '%{http_code}' http://127.0.0.1:3003/test.cfm;)
echo "Finished with Status: $http_code "
echo -e "\n-----\n"
#output the result
cat /tmp/result.txt

echo -e "\n-----\n"

kill $SAM_PID

#echo "Testing Events"

#sam local generate-event s3 > /tmp/test-event.json
#sam local invoke FuselessTest --event /tmp/test-event.json




if [ "$http_code" -eq 200 ]; then
    exit 0
fi

#fail if status code is not 200
exit 1




