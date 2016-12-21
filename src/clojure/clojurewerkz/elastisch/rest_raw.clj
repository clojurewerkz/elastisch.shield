;; Copyright 2011-2016 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; Licensed under the Apache License, Version 2.0  (the "License");
;; you may not use this file except in compliance with the License.
;; You may obtain a copy of the License at
;;
;;     http://www.apache.org/licenses/LICENSE-2.0
;;
;; Unless required by applicable law or agreed to in writing, software
;; distributed under the License is distributed on an "AS IS" BASIS,
;; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;; See the License for the specific language governing permissions and
;; limitations under the License.)

(ns clojurewerkz.elastisch.rest-raw
	(:require [clj-http.client :as http]
						[cheshire.core :as json])
	(:import clojurewerkz.elastisch.rest.Connection))


(defn default-url []
	(or (System/getenv  "ELASTICSEARCH_URL")
			(System/getenv  "ES_URL")
			"http://localhost:9200"))

(defn parse-safely
	[json-str]
	(try
		(json/decode json-str true)
		(catch Exception e
			(throw  (ex-info  "NotValidJSON"
												{:message  (str  "Failed to parse " json-str)
												 :reason  (.getMessage e)})))))

;;uploading ES license requires post method that doesnt encode body to json
(defn post-raw
	"post the raw body with out encoding it to json"
	[^Connection conn ^String uri  {:keys  [body] :as options}]
	(-> uri
		(http/post (merge (.http-opts conn)
											{:accept :json}
											options))
		(:body)
		(parse-safely)))
