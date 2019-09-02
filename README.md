# TacOcaT

A simple bar/restaurant manager app

## Usage in 10 esay steps (Tested on Ubuntu only, but other \*nixes may work too)

1. `sudo apt install postgresql`
2. `mkdir ~/web-server`
3. `cd ~/web-server/`
3. `git clone https://github.com/dsmoss/tacocat.git`
4. `cd tacocat/src/sql/`
5. `./setup`
6. `./make-backup`
7. `cd ~/web-server/tacocat/target/`
8. `java -jar tacocat-$VERSION-standalone.jar`
9. point browser to http://localhost:8080
10. PROFIT!

An initial user will be created for you, tacocat, with password TacOcaT2019.
I suggest you change that on your first login.

If you want to change your favicon.ico and related resources, just overwrite the contents of `resources/ico` and run `lein uberjar` afterwards.

## HTTPS and Apache

In order to get HTTPS, you will need a reverse proxy. Personally, I went with Apache.

To set up I followed the following steps:

- `cd ~/web-server/tacocat/apache`
- `sudo apt install apache2`
- `sudo a2enmod headers proxy proxy_http ssl proxy_wstunnel rewrite`
- `sudo a2dissite 000-default.conf`
- `sudo cp 001-seeq.conf 001-seeq-ssl.conf /etc/apache2/sites-available/`
- `sudo a2ensite 001-seeq.conf`
- `sudo mkdir /etc/apache2/sites-available/ssl`
- Make a key/cert pair (Plenty of tutorials on the web; this is beyond the scope of this document)
- `sudo cp tacocat.crt tacocat.key /etc/apache2/sites-available/ssl`
- `sudo service apache2 restart`
- Website should be available on https://localhost (albeit with warnings if your cert is self signed)

You should be aware that this is **not** recommended. This project is meant to be run on an intranet and not exposed to the wider internet. Logging in behind a reverse proxy like this will also validate 127.0.0.1, not your device, which means effectively everyone who looks at the site will be considered to be **you** by the system. This may or may not be fixed in the future.

## License

Copyright © 2019 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
