(ns hgm.db.core-test
  (:use clojure.test)
  (:require [clojure.string :as str])
  (:use clojure.tools.logging)
  (:use clojure.instant)
  (:use hgm.db.core :reload)
  (:import com.amazonaws.auth.BasicAWSCredentials
           com.amazonaws.services.dynamodb.AmazonDynamoDBClient
           com.amazonaws.AmazonServiceException
           [com.amazonaws.services.dynamodb.model
            ConditionalCheckFailedException
            ])
)


(do ;;Do random setup stuff

  (def properties (-> (clojure.java.io/resource "aws.properties.clj")
                    (clojure.java.io/reader)
                    (java.io.PushbackReader.)
                    (read)))

  (def cred (select-keys properties [:access_key :secret_key]))

  (def test_table (:test_table properties))
  (def client (create-ddb-client cred))


  ;;Test items to play with
  (def test_items [{:hash 88 :range 1 :apple "1" :num 123 :ten 10 :not_ten 10.1}
                   {:hash 88 :range 2 :apple "2" :num 123 :ten 10 :not_ten 10.1}
                   {:hash 88 :range 3 :apple "3" :l [1 2 3 4.01] :banana "bbb" :num 123}
                   {:hash 88 :range 4 :apple "4" :num 123 :ten 10 :not_ten 10.1}
                   {:hash 88 :range 5 :apple "5" :num 123 :ten 10 :not_ten 10.1}])

  ;;Clear the table
  (defn delete-all []

      (doseq [item (scan test_table {})]
        (delete-item test_table (create-key (:hash item) (:range item))))
      (is (empty? (scan test_table {})))
      )

  ;;Create a test table if it's not already created
  (try
    (with-client client
      (create-table
        test_table
        {:read_units 10 :write_units 5}
        {:name "hash" :type :number}
        {:name "range" :type :number})
      ) (catch Exception e ()))


  )



(deftest test-paged-query

  (with-client client
    (delete-all)

    ;;add lots of items
    (doseq [i (range 20)]
      (put-item test_table {:hash 888 :range i :apple (str "apple" i)}))

    ;;Make the paging size small so we can test that it pages through
    (binding [*query_paging_limit* 3]
      (let [result (query test_table 888 {:range_condition [:GE 0]})]
        ;;Make sure it still returns 20, despite the small paging size
        (is (= 20 (count result)))
      ))
  )
)

(deftest test-create-attribute-value
;(do
  (is (create-attribute-value "hello"))
  (is (thrown? AssertionError (create-attribute-value "")))
  (is (create-attribute-value " "))
  (is (thrown? IllegalArgumentException (create-attribute-value nil)))
  (is (create-attribute-value #{"a" "b" "c"}))
  (is (thrown? Exception (create-attribute-value #{"a" "b" "c" "" })))
  (is (thrown? Exception (create-attribute-value #{"a" "b" "c" nil })))
  (is (create-attribute-value 5))
  (is (create-attribute-value 5.0))
  (is (create-attribute-value [1 2 3 4.0]))
  (is (thrown? Exception (create-attribute-value [1 2 3 4 nil])))

  ;;Test various ways create-expected-attribute-value calls
  (is (create-expected-attribute-value {}))
  (is (create-expected-attribute-value {:exists true}))
  (is (create-expected-attribute-value {:exists nil}))
  (is (create-expected-attribute-value {:exists false}))
  (is (create-expected-attribute-value {:value 44}))
  (is (create-expected-attribute-value {:exists false}))
  )


(deftest test-update
;(do


  (with-client client
    ;;delete existing
    (delete-all)

    ;;Test update with no existing
    (update-item test_table (create-key 88 2) {:a "apple" :b "banana"})
    (is (= {:hash 88 :range 2 :a "apple" :b "banana"}
           (get-item test_table (create-key 88 2))))

    ;;Test update with existing
    (update-item test_table (create-key 88 2) {:b "BANANA" :c "cupcake"})
    (is (= {:hash 88 :range 2 :a "apple" :b "BANANA" :c "cupcake"}
          (get-item test_table (create-key 88 2))))

    ;;Test conditional updates
    ;;doesn't exist
    (is (thrown? ConditionalCheckFailedException
          (update-item test_table (create-key 88 2) {:c "cupcake"}
            {:expected {:c {:exists false}}})))
    ;;Exists + wrong value
    (is (thrown? ConditionalCheckFailedException
          (update-item test_table (create-key 88 2) {:c "cupcake"}
            {:expected {:c {:exists true :value "notcupcake"}}})))
    ;;doesn't exist success
    (update-item test_table (create-key 88 2) {:d "donut"}
            {:expected {:d {:exists false}}})
    (is (= {:hash 88 :range 2 :a "apple" :b "BANANA" :c "cupcake" :d "donut"}
        (get-item test_table (create-key 88 2))))
    ;;Exists + expected value success
    (update-item test_table (create-key 88 2) {:d "DONUT"}
      {:expected {:d {:exists true :value "donut"}}})
    (is (= {:hash 88 :range 2 :a "apple" :b "BANANA" :c "cupcake" :d "DONUT"}
          (get-item test_table (create-key 88 2))))

    ;;Test different update actions
    ;;Put - string
    (update-item test_table (create-key 88 3) {:a "apple"})
    (update-item test_table (create-key 88 3) {:a {:action "PUT" :value "APPLE"}})
    (is (= {:hash 88 :range 3 :a "APPLE"} (get-item test_table (create-key 88 3))))
    ;;Put - number
    (update-item test_table (create-key 88 3) {:a 100})
    (update-item test_table (create-key 88 3) {:a {:action "PUT" :value 102}})
    (is (= {:hash 88 :range 3 :a 102} (get-item test_table (create-key 88 3))))
    ;;Put - string set
    (update-item test_table (create-key 88 3) {:a #{"i" "ii"}})
    (update-item test_table (create-key 88 3) {:a {:action "PUT" :value #{"iii"}}})
    (is (= {:a #{"iii"}, :hash 88, :range 3} (get-item test_table (create-key 88 3))))
    ;;Put - number set
    (update-item test_table (create-key 88 3) {:a #{1 2 3.0 4.1}})
    (update-item test_table (create-key 88 3) {:a {:action :PUT :value #{5}}})
    (is (= {:a #{5}, :hash 88, :range 3} (get-item test_table (create-key 88 3))))
    ;;Add - number
    (update-item test_table (create-key 88 3) {:a 100})
    (update-item test_table (create-key 88 3) {:a {:action "ADD" :value 7}})
    (is (= {:hash 88 :range 3 :a 107} (get-item test_table (create-key 88 3))))
    (update-item test_table (create-key 88 3) {:a {:action "ADD" :value -3}})
    (is (= {:hash 88 :range 3 :a 104} (get-item test_table (create-key 88 3))))
    ;;Add - string set
    (update-item test_table (create-key 88 3) {:a #{"a" "b"}})
    (update-item test_table (create-key 88 3) {:a {:action "ADD" :value #{"c"}}})
    (is (= {:hash 88 :range 3 :a #{"a" "b" "c"}} (get-item test_table (create-key 88 3))))
    ;;Add - number set
    (update-item test_table (create-key 88 3) {:a #{1 2}})
    (update-item test_table (create-key 88 3) {:a {:action "ADD" :value [3]}})
    (update-item test_table (create-key 88 3) {:a {:action "DELETE" :value [2]}})
    (is (= {:hash 88 :range 3 :a #{1 3}} (get-item test_table (create-key 88 3))))
    ;;Delete
    (update-item test_table (create-key 88 3) {:a 1 :b 2})
    (update-item test_table (create-key 88 3) {:a {:action "DELETE"}})
    (is (= {:hash 88 :range 3 :b 2} (get-item test_table (create-key 88 3))))

  ))

(deftest test-put-update-get-and-query

  (with-client client
    ;;delete existing
    (delete-all)

    ;;Add 5 items to the table
    (doseq [i (range 5)]
      (put-item test_table (test_items i)))

    ;;Make sure the values we get back are right
    (let [result (query test_table 88 {:range_condition [:GE 0]})]
      (is (= (:hash (result 0)) (:hash (test_items 0))))
      (is (= (:banana (result 2)) (:banana (test_items 2))))
      (is (=  (:num (result 4)) (:num (test_items 4))))
      (is (= 10 (:ten (result 0)) ))
      (is (= 10.1 (:not_ten (result 0)) ))
      (is (= (:l (result 2)) (set (:l (test_items 2)))))
      )

    ;;Test doing a put with an empty string value
    (is (thrown? AssertionError (put-item test_table {:hash 88 :range 4 :empty ""})))
    (is (thrown? IllegalArgumentException (put-item test_table {:hash 88 :range 4 :nulll nil})))

    ;;Test a few range conditions
    (is (= 3 (count (query test_table 88 {:range_condition [:GE 3]}))))
    (is (= 2 (count (query test_table 88 {:range_condition [:GT 3]}))))
    (is (= 2 (count (query test_table 88 {:range_condition [:GE 0] :limit 2}))))
    (is (= 3 (count (query test_table 88 {:range_condition [:BETWEEN 2 4]}))))
    (is (= 0 (count (query test_table 88 {:range_condition [:GE 47]}))))
    (is (= 0 (count (query test_table -1 {:range_condition [:GE 0]}))))

    ;;Test Get vs Query
    (is (= (first (query test_table 88 {:range_condition [:EQ 2]}))
          (get-item test_table {:hash_key 88 :range_key 2})))

    ;;Test that getting something that doesn't exist is nil
    (is (nil? (get-item test_table {:hash_key 89 :range_key 9876})))

    ;;Test updating an existing one
    (update-item test_table {:hash_key 88 :range_key 2} {:b "BANANA" :c "CUPCAKE" :d "DONUT"})
    (is (= {:apple "2", :b "BANANA", :c "CUPCAKE", :d "DONUT", :hash 88, :num 123, :range 2 :ten 10 :not_ten 10.1}
          (first (query test_table 88 {:range_condition [:EQ 2]}))))

    ;;Test updating a new one
    (update-item test_table {:hash_key 88 :range_key 47} {:b "BANANA" :c "CUPCAKE" :d "DONUT" :number 1.2})
    (is (= {:b "BANANA" :c "CUPCAKE" :d "DONUT" :number 1.2}
          (dissoc (first (query test_table 88 {:range_condition [:EQ 47]})) :hash :range )))

  ))

(deftest test-conditional-put-and-update
;(do

  (with-client client


    (let [test_item {:hash 88 :range 49 :a "apple"}
          test_item_v2 {:hash 88 :range 49 :a "apple2"}
          test_item_v3 {:hash 88 :range 49 :a "apple3"}]

      ;;Delete the item if it already exists
      (delete-item test_table {:hash_key 88 :range_key 49})
      (is (nil? (get-item test_table {:hash_key 88 :range_key 49})))

      ;;Test putting an item with the condition that it already exists
      (is (thrown? ConditionalCheckFailedException
            (put-item test_table test_item {:expected {:hash {:exists true :value 88}}})))
      (is (nil? (get-item test_table {:hash_key 88 :range_key 49})))

      ;;Add an item to the table
      (put-item test_table test_item)
      (is (= test_item (get-item test_table {:hash_key 88 :range_key 49})))

      ;;Attempt to add it again, only if the item doesn't exist
      (is (thrown? ConditionalCheckFailedException
        (put-item test_table test_item_v2 {:expected {:hash {:exists false}}})))
      ;;Attempt to add it again, if the item does exist, but the value is wrong
      (is (thrown? ConditionalCheckFailedException
            (put-item test_table test_item_v2 {:expected {:hash {:exists true :value 0 }}})))
      ;;Put the item correctly...
      (put-item test_table test_item_v2 {:expected {:hash {:exists true :value 88 }}
                                                    :range {:exists true :value 49 }})
      (is (= test_item_v2 (get-item test_table {:hash_key 88 :range_key 49})))


      (is (thrown? ConditionalCheckFailedException
            (update-item test_table
              {:hash_key 88 :range_key 49}
              {:b "banana"}
              {:expected {:a {:exists true :value "not this value"}}})))







    )))


(deftest test-scan
;(do
  (with-client client
    ;;delete existing
    (delete-all)

    ;;Add 5 items to the table
    (doseq [i (range 5)]
      (put-item test_table (test_items i)))

    ;;Test scanning with attributes
    (is (= (map #(select-keys % [:hash :range]) (take 5 test_items))
          (binding [*scan_paging_limit* 2]
            (scan test_table {:attributes_to_get ["hash" "range"]}))
          ))
    )
)
