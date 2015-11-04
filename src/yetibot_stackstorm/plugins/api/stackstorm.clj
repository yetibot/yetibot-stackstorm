(ns yetibot-stackstorm.plugins.api.stackstorm
  (:require
    [taoensso.timbre :refer [info]]
    [clojure.string :refer [join]]
    [clj-http.client :as client]
    [clj-http.util :refer [url-decode url-encode]]
    [yetibot.core.config :refer [get-config reload-config]]))

(defn config []
  (get-config :yetibot-stackstorm :models :stackstorm))

(defn opts [& [m]]
  (merge {:accept :json
          :insecure? true
          :throw-exceptions false
          :coerce :always
          :as :json
          :headers {"St2-Api-Key" (:api-key (config))}}
         (or m {})))
(defn endpoint [path] (-> (config) :api-endpoint (str path)))

(defn list-aliases []
  (client/get
    (endpoint "/actionalias")
    (opts)))

(defn get-alias [alias-name]
  (client/get
    (endpoint (str "/actionalias/" (url-encode alias-name)))
    (opts)))

(defn format-alias-short [a]
  (str (if (:enabled a) "✅ "  "🚫 ") (:ref a) ": " (:description a)))

(defn format-alias [a]
  (concat
    [(format-alias-short a)
     (:action_ref a)
     (:id a)
     (str "pack: " (:pack a))]
    (:formats a)))

(defn run-alias [alias-name alias-format command]
  (client/post
    (endpoint "/v1/aliasexecution")
    (opts {:content-type :json
           :form-params {"name" alias-name
                         "format" alias-format
                         "command" command
                         "user" "admin"
                         "source_channel" "test"}})))

(defn executions-get [id]
  (client/get
    (endpoint (str "/executions/" (url-encode id)))
    (opts)))

(defn format-execution [ex]
  (concat
    [(str "Status: " (:status ex))
     (str "Start time: " (:start_timestamp ex))
     (str "Finish time: " (:start_timestamp ex))
     (-> ex :result :stdout)]
    (when (-> ex :result :failed)
      [(str "STDERR: " (-> ex :result :stderr))])))

(defn format-execution-short [ex]
  (join " "
        [(:id ex)
         (:start_timestamp ex)
         (:end_timestamp ex)
         (:status ex)
         (-> ex :liveaction :action)]))

(defn executions-list []
  (client/get
    (endpoint (str "/executions"))
    (opts)))
