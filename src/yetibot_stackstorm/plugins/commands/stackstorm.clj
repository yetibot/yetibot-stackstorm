(ns yetibot-stackstorm.plugins.commands.stackstorm
  (:require
    [taoensso.timbre :refer [info]]
    [yetibot-stackstorm.plugins.api.stackstorm :as api]
    [clojure.string :refer [join]]
    [yetibot.core.hooks :refer [cmd-hook]]))

(defn list-aliases
  [_]
  (let [as (api/list-aliases)]
    (if (= 200 (:status as))
      (map api/format-alias-short (:body as))
      ;; error
      (str (:status as) " " (:body as)))))

(defn show-execution
  "st2 show <execution-id> # show the result of an execution"
  [{[_ id] :match}]
  (str "```"
       (-> (api/executions-get id)
           :body
           api/format-execution)
       "```"))

(cmd-hook #"st2"
  #"list" list-aliases
  #"show.+(\w+)" show-execution)

