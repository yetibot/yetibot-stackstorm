(ns yetibot-stackstorm.plugins.models.stackstorm
  (:require
    [clojure.string :as s :refer [join split] :as s]
    [yetibot.core.models.help :as help]
    [yetibot.core.hooks :refer [cmd-hook-resolved cmd-unhook]]
    [yetibot-stackstorm.plugins.api.stackstorm :as api]
    [taoensso.timbre :refer [info warn]]))

(defn explode-formats
  "create a separate entry for each alias format"
  [as]
  (reduce (fn [acc a]
            (concat
              acc
              (map #(-> a (dissoc :formats) (assoc :format %))
                   (:formats a))))
          [] as))

(defn group-aliases-by-format-prefix
  "Aliases need to be grouped by the first word in their format string in order
   to with with yetibot's existing cmd-hook mechanism. Expects exploded aliases
   (should have a single `format` instead of `formats` key)."
  [as]
  (group-by
    (fn [a]
      (-> a :format (split #"\s") first))
    as))

(defonce hooked-aliases (atom []))

(defn remove-aliases [] (map cmd-unhook @hooked-aliases))

(def join-space (partial join " "))

(defn create-regex [fmt]
  (re-pattern
    (s/replace
      fmt
      #"\{\{.+\}\}"
      "(\\\\S+)")))

(defn verify-permissions
  "If authorized is configured, verify the user. If authorized is not
   configured, allow all users."
  [user]
  (let [c (api/config)]
    (if-let [auth (:authorized (api/config))]
      (do
        (info "verify permissions for" user "against" auth)
        (auth (:id user)))
      (do
        (info ":authorized is not configured, allow user by default")
        true))))

(defn wire-st2-alias [[prefix aliases]]
  (swap! hooked-aliases conj prefix)
  (let [re-prefix (re-pattern prefix)]
    (info "wire" re-prefix aliases)
    (apply
      cmd-hook-resolved
      re-prefix
      (mapcat
        (fn [exploded-alias]
          (when (:enabled exploded-alias)
            (let [fmt (:format exploded-alias)
                  without-prefix (-> fmt (split #"\s") rest join-space)]
              [(create-regex without-prefix)
               (fn [{:keys [raw args user chat-source]}]
                 (info "st2" prefix "matches with" args)
                 (if (verify-permissions user)
                   (api/report-if-error
                     (api/run-alias
                       (:name exploded-alias)
                       fmt
                       (str prefix " " args)
                       (pr-str chat-source))
                     :body)
                   (str "💥 You are not authorized to run StackStorm aliases, "
                        (:username user)
                        ". Contact your Yetibot admin to gain privileges. 💥")))])))
        aliases))))

(defn format-help-string [s]
  (-> s
      (s/replace #"\{\{\s?" "<")
      (s/replace #"\s?\}\}" ">")))

(defn add-help [[prefix aliases]]
  (help/add-docs
    prefix
    (map (fn [a]
           (str
             (format-help-string (:format a))
             " # "
             (:description a)))
         aliases)))

(defn reload-aliases []
  "to match and run an alias, yetibot simply needs to:
     1. match a command against one of st2's alias formats
     2. post to st2 via run-alias with the user input and alias format"
  (let [aliases (:body (api/list-aliases))
        grouped-aliases (-> aliases
                            explode-formats
                            group-aliases-by-format-prefix)]
    (remove-aliases)
    (doall (map (fn [a]
                  (wire-st2-alias a)
                  (add-help a))
                grouped-aliases))
    grouped-aliases))
