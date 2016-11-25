[![build status](https://gitlab.dev.cyfronet.pl/ismop/eitwid/badges/master/build.svg)](https://gitlab.dev.cyfronet.pl/ismop/eitwid/commits/master)

System requirements:
- Java version 8 (use Oracle JDK)
- maven version 3.3

In order to run a local server you need to deploy a config file in the project tree. Go to `src/main/resources/config`
(create this directory if it does not yet exist) and open/create a file named `application.properties` with
the following contents:

```
#this config will override the one in classpath:application.properties
debug = true
maps.google.key = <API key for google maps access - ask MK for details>

dap.token = <secret key for DAP access - ask PN for details>
dap.endpoint = https://dap-dev.moc.ismop.edu.pl/api/v1

secret.token = <secret token used for user registration - ask MK/DH for details>

hypgen.endpoint = https://hypgen-dev.moc.ismop.edu.pl/api
hypgen.user = <hypgen ISMOP username - ask MP for details>
hypgen.pass = <hypgen ISMOP password - ask MP for details>

spring.mail.username = <Spring email acct - ask DH for details>
spring.mail.password = <Spring email password - ask DH for details>
```

To run a development environment do the following:

- `mvn gwt:run`,

... and in the second console window:

- `mvn spring-boot:run -Dstart-class=pl.ismop.web.Development`

... and go to http://localhost:8080

To build and deploy an instance do the following:

- `mvn clean package`

... to produce the final jar with all the dependencies and run it like that:

- `java -jar {jar-file-path}`

To deploy the ISMOP production instance:

- create a tag, and change the production branch to a desired tag and push to gitlab repository
