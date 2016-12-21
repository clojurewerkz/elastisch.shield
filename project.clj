(defproject clojurewerkz/elastisch.shield "1.0.0-SNAPSHOT"
  :description "Elastic Shield support for Elastisch"
  :dependencies [[org.elasticsearch.plugin/shield "2.3.3"]]
  :profiles {:1.8 {:dependencies [[org.clojure/clojure "1.8.0"]]}
             :master {:dependencies [[org.clojure/clojure "1.9.0-master-SNAPSHOT"]]}
             :dev {:dependencies [[timgluz/elastisch "3.0.0-beta2"]
																	[clj-time "0.12.2"]]
                   :resource-paths ["test/resources"]
                   :plugins [[codox "0.10.0"]]
                   :codox {:source-paths ["src/clojure"]}}}
  :aliases {"all" ["with-profile" "dev:dev,1.8:dev,master"]
						"devrepl" ["with-profile" "user,dev,master"]}

  :repositories {
		"sonatype" {:url "http://oss.sonatype.org/content/repositories/releases"
							  :snapshots false
							  :releases {:checksum :fail}}
	  "sonatype-snapshots" {:url "http://oss.sonatype.org/content/repositories/snapshots"
												 :snapshots true
												 :releases {:checksum :fail :update :always}}
		"elastic" {:url "https://maven.elasticsearch.org/releases"
			  			 :snapshots true
			   	 		 :releases {:checksum :fail :update :always}}}

  :global-vars {*warn-on-reflection* true}
  :test-selectors {:native :native
                   :rest :rest
                   :all (constantly true)
                   :default (constantly true)}
 
  :javac-options      ["-target" "1.6" "-source" "1.6"]
  :jvm-opts           ["-Dfile.encoding=utf-8"]
  :source-paths       ["src/clojure"]
  :java-source-paths  ["src/java"]
  :mailing-list {:name "clojure-elasticsearch"
                 :archive "https://groups.google.com/group/clojure-elasticsearch"
                 :post "clojure-elasticsearch@googlegroups.com"})
