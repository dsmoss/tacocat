# tacocat

A simple bar/restaurant manager app

## Usage in 10 esay steps (Tested on Ubuntu only, but other \*nixes may work too)

1. sudo apt install postgresql
2. mkdir ~/web-server
3. cd ~/web-server/
3. git clone https://github.com/dsmoss/tacocat.git
4. cd tacocat/src/sql/
5. ./setup
6. ./make-backup
7. cd ~/web-server/tacocat/target/
8. java -jar tacocat-$VERSION-standalone.jar
9. point browser to http://localhost:8080
10. PROFIT!

An initial user will be created for you, tacocat, with password TacOcaT2019.
I suggest you change that on your first login.

## License

Copyright © 2019 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
