(defproject sub-learning-web "1.0.1"

  :description "Code for the sublearning.com website"
  :url "http://sublearning.com"

  :dependencies [[bouncer "1.0.1"]
                 [buddy "1.3.0"]
                 [compojure "1.5.2"]
                 [conman "0.6.3"]
                 [cprop "0.1.10"]
                 [funcool/struct "1.0.0"]
                 [luminus-immutant "0.2.3"]
                 [luminus-migrations "0.3.0"]
                 [luminus-nrepl "0.1.4"]
                 [markdown-clj "0.9.98"]
                 [metosin/ring-http-response "0.8.2"]
                 [mount "0.1.11"]
                 [org.clojure/clojure "1.11.1"]
                 [org.clojure/tools.cli "1.0.219"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.postgresql/postgresql "42.0.0"]
                 [org.webjars.bower/tether "1.4.0"]
                 [org.webjars/bootstrap "4.0.0-alpha.5"]
                 [org.webjars/font-awesome "4.7.0"]
                 [org.webjars/jquery "3.1.1"]
                 [org.webjars/webjars-locator-jboss-vfs "0.1.0"]
                 [ring-middleware-format "0.7.2"]
                 [ring-webjars "0.1.1"]
                 [ring/ring-core "1.6.0-RC1"]
                 [ring/ring-defaults "0.2.3"]
                 [selmer "1.10.6"]
                 [org.clojure/core.rrb-vector "0.0.13"]
                 [org.flatland/ordered "1.5.7"]]
  :dev-dependencies [[info.cukes "1.2.5"]]

  :min-lein-version "2.0.0"

  :jvm-opts ["-server" "-Dconf=.lein-env"]
  :source-paths ["src/clj"]
  :test-paths ["test/clj"]
  :resource-paths ["resources"]
  :target-path "target/%s/"
  :main ^:skip-aot sub-learning-web.core
  :migratus {:store :database :db ~(get (System/getenv) "DATABASE_URL")}

  :plugins [[lein-cprop "1.0.1"]
            [migratus-lein "0.4.4"]
            [lein-environ "1.0.1"]
            [lein-immutant "2.1.0"]
            [lein-ancient "LATEST"]]

  :immutant {
    :war {
        :name "sub-learning"
        :destination "~/bin/wildfly/standalone/deployments"
        :context-path "/sub-learning"
    }
  }

  :profiles
  {:uberjar {:omit-source true
             :aot :all
             :uberjar-name "sub-learning-web.jar"
             :source-paths ["env/prod/clj"]
             :resource-paths ["env/prod/resources"]}

   :dev           [:project/dev :profiles/dev]
   :test          [:project/dev :project/test :profiles/test]

   :project/dev  {:dependencies [[prone "1.1.4"]
                                 [ring/ring-mock "0.3.0"]
                                 [ring/ring-devel "1.5.1"]
                                 [pjstadig/humane-test-output "0.8.1"]]
                  :plugins      [[com.jakemccrary/lein-test-refresh "0.18.1"]]

                  :source-paths ["env/dev/clj"]
                  :resource-paths ["env/dev/resources"]
                  :repl-options {:init-ns user}
                  :injections [(require 'pjstadig.humane-test-output)
                               (pjstadig.humane-test-output/activate!)]}
   :project/test {:resource-paths ["env/test/resources"]}
   :profiles/dev {}
   :profiles/test {}})
