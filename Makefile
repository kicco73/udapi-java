image:
	rm -rf protoserver/resources
	./gradlew build jar
	docker build --pull .

jar:
	./gradlew build jar

run:
	docker image rm -f udapi-java_rut-backend
	docker-compose up -d

clean:
	rm -rf protoserver/resources
	docker container rm -f udapi-java
	docker image rm -f udapi-java_rut-backend
