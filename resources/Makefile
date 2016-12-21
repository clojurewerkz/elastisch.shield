ES_VERSION=2.3.3
ES_CLUSTER_NAME="shield-test"

build:
	docker build --rm=true -t elastisch/shield:$(ES_VERSION) .

run:
	docker run -d -p 9200:9200 -p 9300:9300 --name="$(ES_CLUSTER_NAME)" elastisch/shield:$(ES_VERSION)

test:
	lein with-profile test,dev test 

repl:
	lein with-profile devrepl repl
