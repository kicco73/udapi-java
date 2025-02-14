image: jar
	rm -rf protoserver/resources
	docker build --pull .

jar:
	./gradlew build jar

run:
	docker image rm -f udapi-java_rut-backend
	docker-compose up -d

snapshots:
	/usr/bin/env java -jar bin/libs/rut.jar \
		--no-graphdb --input-format tbx --datetime 2023-04-10T10:02+02:00 --creator kicco \
		--output-dir src/test/resources/cnr/ilc/tbx/sparql \
		-- src/test/resources/cnr/ilc/tbx/input/*

test:
	./gradlew test

clean:
	rm -rf protoserver/resources
	docker container rm -f udapi-java
	docker image rm -f udapi-java_rut-backend
