(ns yetibot-stackstorm.plugins.commands.stackstorm
  (:require
    [taoensso.timbre :refer [info warn]]
    [yetibot-stackstorm.plugins.api.stackstorm :as api]
    [clojure.string :refer [join]]
    [yetibot.core.hooks :refer [cmd-hook]]))


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

(defn list-aliases
  "st2 alias list # list configured StackStorm aliases"
  [_]
  (let [as (api/list-aliases)]
    (if (= 200 (:status as))
      (map api/format-alias-short (:body as))
      ;; error
      (str (:status as) " " (:body as)))))

(defn list-executions
  "st2 ex list # list recent executions"
  [_]
  (map api/format-execution-short (:body (api/executions-list))))

(defn show-execution
  "st2 ex show <execution-id> # show the result of an execution"
  [{[_ id] :match}]
  (let [r (-> (api/executions-get id)
              :body
              api/format-execution)]
    ; (info r)
    r
    ))

(defn show-alias
  "st2 alias show <alias-name> # show an alias and its formats"
  [{[_ a] :match}]
  (report-if-error
    (api/get-alias "portico.portico_debug")
    (fn [{body :body}]
      (api/format-alias body))))

;; attempt to match against one of the internal aliases
(defn match-alias
  [{args :match}]
  )

(cmd-hook #"st2"
  #"alias list" list-aliases
  #"alias show\s+(\w+)" show-alias
  #"ex(ecution)? list" list-executions
  #"ex(ecution)? show\s+(\w+)" show-execution
  _ match-alias
  )
