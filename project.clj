(defproject hgm "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [
                 [org.clojure/clojure "1.4.0"]
                 [compojure "1.1.5"]
                 [ring/ring-jetty-adapter "1.1.0"]
                 [ring-json-response "0.2.0"]
                 [org.clojure/tools.logging "0.2.3"]
                 [org.clojure/tools.trace "0.7.1"]
                 [com.amazonaws/aws-java-sdk "1.3.27"]
                 [commons-logging/commons-logging "1.1"]
                 [com.cemerick/friend "0.1.3"]
                 [cheshire "5.0.1"]
                 [environ "0.3.0"]
                ]
  :min-lein-version "2.0.0"
  :main hgm.core)
