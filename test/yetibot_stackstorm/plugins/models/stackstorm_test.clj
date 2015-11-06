(ns yetibot-stackstorm.plugins.models.stackstorm-test
  (:require
    [clojure.string :as s]
    [yetibot-stackstorm.plugins.models.stackstorm :refer :all]
    [clojure.test :refer :all]))

(deftest format-test
  ;; create a regex that matches this exact format
  (let [f "pack deploy {{packs}}"
        s "pack deploy github"
        r (create-regex f)]
    (println r)
    (re-find r s)))


(let [a "{{module-name=ping}} {{limit}} {{cwd=/home/admin/ansibleter}} {{playbook=/home/admin/ansibleter/foo.yml}}"]
  (create-regex a))

(def test-aliases
  [

   {:name "st2_executions_re_run"
    :description "Re-run an action execution."
    :enabled true
    :formats ["st2 re-run execution {{ id }}" "st2 executions re-run {{ id }}"]
    :action_ref "st2.executions.re_run"
    :ref "st2.st2_executions_re_run"
    :id "5626dca6ba60d94dcb485da2"
    :pack "st2"}

   {:name "st2_rules_list"
    :description "List available StackStorm rules."
    :enabled true
    :formats ["st2 list rules" "st2 list rules from {{ pack }}"]
    :action_ref "st2.rules.list"
    :ref "st2.st2_rules_list"
    :id "5626dca6ba60d94dcb485da3"
    :pack "st2"}

   {:name "hubot_deploy"
    :description "Deploy a specific git branch of deployed Hubot"
    :enabled true
    :formats ["hubot deploy {{branch}}" "hubot deploy {{branch}} on {{hosts}}"]
    :action_ref "hubot.deploy"
    :ref "hubot.hubot_deploy"
    :id "5626dc8aba60d94cb9b7dc06"
    :pack "hubot"}

   ])


(deftest explode-formats-test
  (explode-formats test-aliases))


(deftest group-aliases-by-format-prefix-test
  (println (-> test-aliases explode-formats group-aliases-by-format-prefix)))
