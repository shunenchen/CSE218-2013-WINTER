(ns hgm.api.core
  (:require [hgm.db.core :as db]))

(defmacro defapi [name doc args & body]
  `(defn ~name ~doc ~args
     (try (let [r# (do ~@body)]
            {:status 200
             :body {:data r#}})
          (catch java.lang.Throwable t# {:status 500 :body "WHOOPS"}))))

;;; API CODE GOES HERE
(defapi get-forwards
  "Get a list of all forwards in `team'"
  [team]
  (db/get-forwards team))

