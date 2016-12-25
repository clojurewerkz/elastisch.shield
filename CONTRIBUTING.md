## Pre-requisites

The project uses [Leiningen 2](https://leiningen.org) and requires Elasticsearch `1.4.x` or more recent to be running locally.

Make sure you have those two installed and then run tests against all supported Clojure versions using

    lein all test


#### Testing with docker

* pull [Elasticsearch](https://hub.docker.com/_/elasticsearch/) image 

```
$> docker pull elasticsearch:2.3.3
```
nb! Tag must match with Elasticsearch version in the `project.clj`

* build image to test Elasticsearch Shield

```
cd resources
docker build -f Dockerfile --rm=true -t elastisch/shield:2.3.3 .
```


* run Docker instance

The custom file in the `resources/config/elasticsearch.yml` has all the required settings for full-scale test activated, so you dont have manually tweak them.

```
docker run -d -p 9200:9200 -p 9300:9300 \
	--name="shield-test"  elasticsearch/shield:2.3.3

curl -u es_admin:toor123 127.0.0.1:9200/_shield
```

* set environment variables

```
$> export ES_URL="http://127.0.0.1:9200" ;;to override URL for the rest-client
$> export ES_CLUSTER_NAME="shield-test"  ;; to override default cluster name
$> export ES_CLUSTER_HOST="127.0.0.1"    ;; to override default cluster IP address
```

* run tests

```
lein with-profile dev,1.8 test :only clojurewerkz.elastisch.shield-test
lein with-profile dev,1.8 test :native ;;only native client
lein with-profile dev,1.8 test 	      ;;all the tests with clj1.8
```

## Pull Requests

Then create a branch and make your changes on it. Once you are done with your changes and all
tests pass, write a [good, detailed commit message](http://tbaggery.com/2008/04/19/a-note-about-git-commit-messages.html) and submit a pull request on GitHub.

Don't forget to add your changes to `ChangeLog.md` and credit yourself!

