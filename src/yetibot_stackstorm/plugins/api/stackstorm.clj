(ns yetibot-stackstorm.plugins.api.stackstorm
  (:require
    [taoensso.timbre :refer [info warn]]
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

;; todo: extract a `report-error-or [response on-succ & [on-err]]` to core
(defn success? [res] (re-find #"^2" (str (:status res) "2")))

(defn report-if-error
  "Checks the stauts of the HTTP response for 2xx, and if not, reports the body."
  [res succ-fn]
  (if (success? res)
    (succ-fn res)
    (do
      (warn "stackstorm api error" res)
      (:body res))))

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

(defn run-alias [alias-name alias-format command source-channel]
  (client/post
    (endpoint "/v1/aliasexecution")
    (opts {:content-type :json
           :form-params {"name" alias-name
                         "format" alias-format
                         "command" command
                         "user" "admin"
                         "notification_route" "yetibot"
                         "source_channel" source-channel}})))

(defn get-execution [id]
  (client/get
    (endpoint (str "/executions/" (url-encode id)))
    (opts)))

(defn format-execution [ex]
  (concat
    (remove
      nil?
      [(str "Status: " (:status ex))
       (str "Start time: " (:start_timestamp ex))
       (when (not= "running" (:status ex))
         (str "Finish time: " (:start_timestamp ex)))
       (-> ex :result :stdout)])
    (when (not-empty (-> ex :result :stderr))
      [(str "STDERR: \n" (-> ex :result :stderr))])))

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
