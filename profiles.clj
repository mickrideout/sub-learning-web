;; WARNING
;; The profiles.clj file is used for local environment variables, such as database credentials.
;; This file is listed in .gitignore and will be excluded from version control by Git.

{:profiles/dev  {:env {:database-url "postgresql://localhost/<dev-user>?user=subdev&password=<dev-password>"}}
 :profiles/test {:env {:database-url "postgresql://localhost/<test-user>?user=subtest&password=<test-password>"}}}
