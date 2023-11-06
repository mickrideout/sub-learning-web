# sub-learning-web

The clojure code for the [sublearning.com](http://sublearning.com) website


## Requirements


Leiningen to compile and run clojure is required:

```apt install leiningen```

## Usage

### Compiling

Compile the clojure uberjar:

```lein uberjar```

### Running

To start a web server for the application, run:

```java -Dport=80 -Ddatabase-url=jdbc:postgresql://localhost/<database>?user=<prod-user>&password=<prod-password> -jar target/uberjar/sub-learning-web.jar```

## License

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
