(defproject yetibot-stackstorm "0.1.1"
  :description "yetibot Stackstorm plugin"
  :url "https://github.com/devth/yetibot.core"
  :scm {:name "git" :url "https://github.com/devth/yetibot-stackstorm.git"}
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :lein-release {:deploy-via :clojars}
  :signing {:gpg-key "C9764E34"}
  :deploy-repositories [["releases" :clojars]]
  :main yetibot.core.init
  :aot [yetibot.core.init]
  :jvm-opts ["-Xmx8g" "-server"]
  :repl-options {:init-ns yetibot.core.repl
                 :timeout 120000
                 :welcome (println "Welcome to the yetibot StackStorm development repl!")}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [yetibot.core "0.2.49"]])
