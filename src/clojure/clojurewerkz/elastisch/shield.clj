;; Copyright 2011-2016 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; Licensed under the Apache License, Version 2.0 (the "License");
;; you may not use this file except in compliance with the License.
;; You may obtain a copy of the License at
;;
;;     http://www.apache.org/licenses/LICENSE-2.0
;;
;; Unless required by applicable law or agreed to in writing, software
;; distributed under the License is distributed on an "AS IS" BASIS,
;; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;; See the License for the specific language governing permissions and
;; limitations under the License.


(ns clojurewerkz.elastisch.shield
  (:require [clojure.string :as string]
            [clojurewerkz.elastisch.rest :as rest-client]
						[clojurewerkz.elastisch.rest-raw :as rr-client]
            [clojurewerkz.elastisch.native.conversion :as cnv])

  (:import [clojure.lang IPersistentList IPersistentMap]
           clojurewerkz.elastisch.rest.Connection
           org.elasticsearch.client.Client
           org.elasticsearch.client.transport.TransportClient
           org.elasticsearch.shield.ShieldPlugin))

;;TODO: should use Spec here to add some protective Speck
(defrecord ShieldUser
  [^String username
   ^String password
   ^IPersistentList roles
   ^String full_name
   ^String email
   ^IPersistentMap metadata])

(defrecord ShieldRole
  [^IPersistentList cluster
   ^IPersistentList indices ;;list of ShieldRoleIndex
   ^IPersistentList run_as])

(defrecord ShieldRoleIndex
  [^IPersistentList names
   ^IPersistentList privileges
   ^IPersistentList fields
   ^IPersistentList query])

(defn remove-empty-fields
  "removes dictionary items with nil or empty value"
  [data-doc]
  (->> data-doc
    (remove
      (fn [[k v]] (or (nil? v) (and (coll? v) (empty? v)))))
    (into {})))

(defn init-user
  "Initializes a new user data map for request"
  ([^IPersistentMap shield-user-table]
    (map->ShieldUser shield-user-table))
  ([^String username ^String password]
    (init-user {:username username
                :password password}))
  ([^String username ^String password ^IPersistentList roles]
    (init-user {:username username
                :password password
                :roles (vec roles)})))

(defn init-role
  "initializes a new native Shield role"
  [clusters shield-indices]
  (->ShieldRole clusters 
                (vec (map #(map->ShieldRoleIndex %) shield-indices))
                []))


(defn info
  "returns a basic information about the Shield"
  [rest-conn]
  (rest-client/get rest-conn
                   (rest-client/url-with-path rest-conn "_shield")))

(defn authenticate
  "submits a request with basic auth header and returns information about authorized user"
  [rest-conn]
  (rest-client/get rest-conn
                   (rest-client/url-with-path rest-conn "_shield/authenticate")))

(defn clear-cache
  "evicts users from the user cache"
  ([^Connection rest-conn]
    (clear-cache rest-conn ["default"] nil))
  ([^Connection rest-conn ^IPersistentList realm-names]
    (clear-cache rest-conn realm-names nil))
  ([^Connection rest-conn ^IPersistentList realm-names ^IPersistentList usernames]
    (let [endpoint-uri (rest-client/url-with-path
                         rest-conn
                         "_shield/realm"
                         (string/join "," realm-names)
                         "_clear_cache"
                         (when-not (empty? usernames)
                          (str "?usernames=" (string/join "," usernames))))]
      (rest-client/post rest-conn endpoint-uri))))

;;-- LICENSE endpoints --------------------------------------------------------

(defn get-license
  "returns details  of currently installed ES license"
  [^Connection rest-conn]
  (rest-client/get rest-conn (rest-client/url-with-path rest-conn "_license")))


(defn update-license
  "uploads or re-uploads a the ES license"
  [^Connection rest-conn ^String license-content]
  (rr-client/post-raw rest-conn
											(rest-client/url-with-path rest-conn "_license")
											{:query-params {:acknowledge true}
											 :content-type :json
											 :body (str license-content)}))


;;-- USER endpoints -----------------------------------------------------------
(defn add-user
  "creates a new native user.
  Params:
  @rest-client - initialized elastisch shielded REST-client 
  @username  - string, 1 < len < 30
  @password  - string, 6 < len
  @roles     - vector, 1 < len"
  ([^Connection rest-conn ^ShieldUser new-user]
    (let [shield-uri (rest-client/url-with-path rest-conn "_shield/user" (:username new-user))]
      (rest-client/post rest-conn
                        shield-uri
                        {:body (remove-empty-fields (dissoc new-user :username))
                         :throw-exceptions false}))))

(defn get-users
  ([^Connection rest-conn]
    (rest-client/get rest-conn
                     (rest-client/url-with-path rest-conn "_shield/user")))
  ([^Connection rest-conn ^IPersistentList user-names]
    (rest-client/get rest-conn
                     (rest-client/url-with-path rest-conn "_shield/user" (string/join "," user-names)))))

(defn delete-user
  [^Connection rest-conn ^String user-name]
  (rest-client/delete rest-conn
                      (rest-client/url-with-path rest-conn "_shield/user" user-name)))

(defn add-role
  "adds a new native Shield role
  more details: https://www.elastic.co/guide/en/shield/current/defining-roles.html"
  [^Connection rest-conn ^String role-name ^ShieldRole role]
  (rest-client/post
    rest-conn
    (rest-client/url-with-path rest-conn "_shield/role" role-name)
    {:body (remove-empty-fields role)}))

(defn get-roles
  "fetches a list of Shield roles"
  [^Connection rest-conn]
  (rest-client/get rest-conn
                   (rest-client/url-with-path rest-conn "_shield/role")))

(defn delete-role
  "deletes the role by its name"
  [^Connection rest-conn ^String role-name]
  (rest-client/delete rest-conn
                      (rest-client/url-with-path rest-conn "_shield/role" role-name)))


(defn ^Client connect-rest
  ([^String username ^String password]
    (connect-rest (rr-client/default-url) username password {}))
  ([^String uri ^String username ^String password]
    (connect-rest uri username password {}))
  ([^String uri ^String username ^String password ^IPersistentMap opts]
    (rest-client/connect uri 
                         (merge {:basic-auth [username password]} opts))))

(defn ^Client connect-native
  "Connects to one or more shielded Elasticsearch cluster nodes using
  TCP/IP communication transport. Returns the client."
  ([^IPersistentList pairs ^String username ^String password]
    (connect-native pairs username password {}))
  ([^IPersistentList pairs ^String username ^String password ^IPersistentMap settings]
     (let [settings-with-auth (assoc settings "shield.user" (str username ":" password))
           tcb (doto (TransportClient/builder)
                 (.addPlugin ShieldPlugin)
                 (.settings (cnv/->settings settings-with-auth)))
           tc (.build tcb)]
       (doseq [[host port] pairs]
         (.addTransportAddress tc (cnv/->socket-transport-address host port)))
       tc)))

(comment
  ;example workflow

  (require '[clojurewerkz.elastisch.shield :as shield] :reload-all)
  
  (def shield-user (shield/init-user "es_admin" "toor123"))
  ;using shield end-points
  (println shield-user)
  (def test-user (shield/init-user "es_test" "qwerty123" ["test"]))

  ;using rest-client to make authorized calls
  (def srconn (shield/connect-rest
                "http://127.0.0.1:9200"
                (:username shield-user)
                (:password shield-user)))
  (require '[clojurewerkz.elastisch.rest.admin :as radmin])
  (radmin/cluster-health srconn)

  (shield/info srconn)
  (shield/authenticate srconn)
  (shield/clear-cache srconn)
  (shield/get-license srconn)

  (require '[clojure.java.io :as io])
  (def lic-file (slurp "resources/elastisch-shield-license-v2.json"))
  (shield/update-license srconn lic-file)

  (shield/add-user srconn shield-user test-user)
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

  )
