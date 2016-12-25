# Elastisch Shield Support

`elastisch.shield` adds [Elastic Shield](https://www.elastic.co/products/x-pack/security) support to [Elastisch](http://clojureelasticsearch.info).


## Project Maturity

elastisch.shield is *very* young.


## Artifacts

... artifacts are [released to Clojars](https://clojars.org/clojurewerkz/elastisch.shield). If you are using Maven, add the following repository
definition to your `pom.xml`:

``` xml
<repository>
  <id>clojars.org</id>
  <url>http://clojars.org/repo</url>
</repository>
```

### The Most Recent Release

With Leiningen:

    [clojurewerkz/elastisch.shield "1.0.0-alpha1"]


With Maven:

    <dependency>
      <groupId>clojurewerkz</groupId>
      <artifactId>elastisch.shield</artifactId>
      <version>1.0.0-alpha1</version>
    </dependency>


## Documentation & Examples

```clojure

  (require '[clojurewerkz.elastisch.shield :as shield] :reload)

  (def shield-user (shield/init-user "es_admin" "toor123"))
  (def test-user (shield/init-user "es_test" "qwerty123" ["test"]))

  ;using rest-client to make authorized calls
  (def srconn (shield/connect-rest
                "http://127.0.0.1:9200"
                (:username shield-user)
                (:password shield-user)))
                
  (require '[clojurewerkz.elastisch.rest.admin :as radmin])
  (radmin/cluster-health srconn)
  (shield/info srconn)

  
  ;; manage Shield license
  (shield/get-license srconn)
  (def lic-file (slurp "resources/elastisch-shield-license-v2.json"))
  (shield/update-license srconn lic-file)

  ;; manage Shield users
  (shield/authenticate srconn)
  (shield/clear-cache srconn)
  (shield/add-user srconn test-user)
  (shield/get-users srconn)
  (shield/get-users srconn ["es_test"])
  (shield/delete-user srconn (:username test-user))


  ;;using native client to make authorized calls
  (def sconn (shield/connect-native  [["127.0.0.1" 9300]]
                                     (:username shield-user)
                                     (:password shield-user)
                                     {"cluster.name" "shield-test"}))

  (require '[clojurewerkz.elastisch.native.index :as index])
  (index/create sconn "testindex")
  (index/stats sconn)

```

## Community & Support

[Elastisch has a mailing
list](https://groups.google.com/forum/#!forum/clojure-elasticsearch). Feel
free to join it and ask any questions you may have.

To subscribe for announcements of releases, important changes and so on, please follow [@ClojureWerkz](https://twitter.com/clojurewerkz) on Twitter.



## Supported Clojure versions

This projects supports the same set of Clojure versions as Elastisch.


## Elastisch Is a ClojureWerkz Project

Elastisch is part of the [group of Clojure libraries known as ClojureWerkz](http://clojurewerkz.org), together with

 * [Langohr](http://clojurerabbitmq.info)
 * [Neocons](http://clojureneo4j.info)
 * [Monger](http://clojuremongodb.info)  
 * [Cassaforte](http://clojurecassandra.info)

and several others.


## Development

elastisch.shield uses [Leiningen
2](https://github.com/technomancy/leiningen/blob/master/doc/TUTORIAL.md). Make
sure you have it installed and then run tests against supported
Clojure versions using

    lein all test

Then create a branch and make your changes on it. Once you are done
with your changes and all tests pass, submit a pull request on GitHub.



## License

Copyright (C) 2016 Michael S. Klishin, Alex Petrov, and The ClojureWerkz Team.

Double licensed under the [Eclipse Public License](http://www.eclipse.org/legal/epl-v10.html) (the same as Clojure) or
the [Apache Public License 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).
