ES_VERSION=2.3.3

build:
	docker build --rm=true -t elastisch/shield:$(ES_VERSION) .

run:
	docker run -d -p 9200:9200 -p 9300:9300 elastisch/shield:$(ES_VERSION)

test:
	lein with-profile dev,master test :only clojurewerkz.elastisch.shield-test

repl:
	lein with-profile devrepl repl
