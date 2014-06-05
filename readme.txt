To run development environment do the following:

mvn compile gwt:compile
mvn gwt:run-codeserver

... and in the second console window:

mvn spring-boot:run -Dstart-class=pl.ismop.web.Development

... and go to http://localhost:8080