(ns yetibot-stackstorm.plugins.commands.stackstorm
  (:require
    [taoensso.timbre :refer [info warn]]
    [yetibot-stackstorm.plugins.api.stackstorm :as api]
    [yetibot-stackstorm.plugins.models.stackstorm :as model]
    [clojure.string :refer [join split]]
    [yetibot.core.hooks :refer [cmd-hook]]))



(defn list-aliases
  "stackstorm alias list # list configured StackStorm aliases"
  [_]
  (let [as (api/list-aliases)]
    (if (= 200 (:status as))
      (map api/format-alias-short (:body as))
      ;; error
      (str (:status as) " " (:body as)))))

(defn list-executions
  "stackstorm ex list # list recent executions"
  [_]
  (map api/format-execution-short (:body (api/executions-list))))

(defn show-execution
  "stackstorm show <execution-id> # show the result of an execution"
  [{[_ id] :match}]
  (info id)
  (let [r (-> (api/get-execution id)
              :body
              api/format-execution)]
    r))

(defn show-alias
  "stackstorm alias show <alias-name> # show an alias and its formats"
  [{[_ a] :match}]
  (api/report-if-error
    (api/get-alias a)
    (fn [{body :body}]
      (api/format-alias body))))


(defn reload-aliases
  "stackstorm reload-aliases reload aliases and register them as yetibot commands"
  [_]
  (let [grouped-aliases (model/reload-aliases)]
    (str
      "Wired StackStorm aliases: "
      (join ", " (map first grouped-aliases)))))

(defonce load-aliases (future (model/reload-aliases)))

(cmd-hook #"stackstorm"
  #"alias list" list-aliases
  #"alias show\s+(\S+)" show-alias
  #"ex(ecution)? list" list-executions
  #"show\s+(\S+)" show-execution
  #"reload.+aliases" reload-aliases)
