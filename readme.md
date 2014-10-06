To run development environment do the following:

- `mvn compile gwt:compile`,
- `mvn gwt:run-codeserver`

... and in the second console window:

- `mvn spring-boot:run -Dstart-class=pl.ismop.web.Development`

... and go to http://localhost:8080

To build and deploy an instance do the following:

- `mvn clean package`

... to produce the final jar with all the dependencies and run it like that:

- `java -jar {jar-file-path}`

To deploy the ISMOP production instance:

- build the project with Jenkins (https://jenkins.dev.cyfronet.pl/view/ISMOP/job/UI%20Deployment/),
- login to `ui.moc.ismop.edu.pl` with the servers account,
- execute the following commands:
  - `cd /home/servers/apps/ismop-web`,
  - `./deploy.sh`