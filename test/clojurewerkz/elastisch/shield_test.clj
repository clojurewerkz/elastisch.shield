;; Copyright (c) 2011-2016 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.


(ns clojurewerkz.elastisch.shield-test
  (:require [clojurewerkz.elastisch.shield :as shield]
            [clojurewerkz.elastisch.fixtures :as fx]
            [clojure.test :refer :all]))

(use-fixtures :each fx/reset-indexes)

(def es-admin {:username "es_admin"
               :password "toor123"})

(def es-test {:username "es_test1"
              :password "test123"
              :roles ["test_role"]})

(deftest ^{:shield true} test-info-api
  (let [conn (shield/connect-rest (:username es-admin) (:password es-admin))]
    (testing "returns expected response for authorized user"
      (let [res (shield/info conn)]
        (is (false? (empty? res)))
        (is (contains? res :status))
        (is (contains? res :cluster_name))
        (is (= "shield-test" (:cluster_name res)))))))

(deftest ^{:shield true} test-user-authenticate-api
  (let [conn (shield/connect-rest (:username es-admin) (:password es-admin))]
    (testing "returns expected response for authorized user"
      (let [res (shield/authenticate conn)]
        (is (false? (empty? res)))
        (is (contains? res :username))
        (is (= (:username es-admin) (:username res)))
        (is (contains? res :roles))
        (is (= ["admin"] (:roles res)))))))

(deftest ^{:shield true} test-license-api
  (let [conn (shield/connect-rest (:username es-admin) (:password es-admin))]
    (testing "returns details of currently installed license"
      (let [res (shield/get-license conn)]
        (is (false? (empty? res)))
        (is (contains? res :license))
        (is (contains? (get res :license) :status)))

    ;;NB! must update fixture every year on 18th December
    (testing "updates current license"
      (let [lic-file (slurp "resources/elastisch-shield-license-v2.json")
            res (shield/update-license conn lic-file)]
        (is (false? (empty? res)))
        (is (contains? res :acknowledged))
        (is (true? (:acknowledged res)))
        (is (contains? res :license_status))
        (is (= "valid" (:license_status res))))))))

(deftest ^{:shield true} test-clear-cache-api
  (let [conn (shield/connect-rest (:username es-admin) (:password es-admin))]
    (testing "clears the cache of default realm"
      (let [res (shield/clear-cache conn)]
        (is (false? (empty? res)))
        (is (= (contains? res :cluster_name)))))
    (testing "clears the cache of the specified realm"
      (let [res (shield/clear-cache conn ["default"])]
        (is (false? (empty? res)))
        (is (= (contains? res :cluster_name)))))
    (testing "clears the cache of the user on the specified realm"
      (let [res (shield/clear-cache conn ["default"] [(:username es-admin)])]
        (is (false? (empty? res)))
        (is (= (contains? res :cluster_name)))))))

(deftest ^{:shield true} test-CRUD-new-user
  (let [conn (shield/connect-rest (:username es-admin) (:password es-admin))]
    (testing "returns empty list when no users added"
      (let [res (shield/get-users conn)]
        (is (empty? res))))

    (testing "creates a new user"
      (let [res (shield/add-user conn es-test)]
        (is (false? (empty? res)))
        (is (contains? res :user))
        (is (true? (get-in res [:user :created])))))
    
    (testing "returns freshly added new user"
      (let [res (shield/get-users conn)]
        (is (false? (empty? res)))
        (is (contains? res :es_test1))
        (is (= (:username es-test) (get-in res [:es_test1 :username])))))

    (testing "deletes user"
      (let [res (shield/delete-user conn (:username es-test))]
        (is (false? (empty? res)))
        (is (contains? res :found))
        (is (true? (:found res)))))))


(def new-role (shield/init-role ["all"]
                                [{:names ["test_idx1"]
                                  :privileges ["all"]}]))

(deftest ^{:shield true} test-CRUD-new-role
  (let [conn (shield/connect-rest (:username es-admin) (:password es-admin))]
    (testing "returns empty dictionary if no roles"
      (let [res (shield/get-roles conn)]
        (is (true? (empty? res)))))

    (testing "creates a new shield role"
      (let [res (shield/add-role conn "test_role_1" new-role)]
        (is (false? (empty? res)))
        (is (contains? res :role))
        (is (true? (get-in res [:role :created])))))

    (testing "returns freshly created role"
      (let [res (shield/get-roles conn)]
        (is (false? (empty? res)))
        (is (contains? res :test_role_1))
        (is (= ["test_idx1"]
               (-> res :test_role_1 :indices first :names)))))

    (testing "deletes the created role"
      (let [res (shield/delete-role conn "test_role_1")]
        (is (false? (empty? res)))
        (is (contains? res :found))
        (is (true? (:found res)))))))
