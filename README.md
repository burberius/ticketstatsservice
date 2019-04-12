# Ticket Statistics Service
This is just the result of a job application coding test. It uses the latest
(dated 12.04.2019) versions of the dependencies, but unfortunately it needs
the current SNAPSHOT version of *springfox* to work with Spring Boot 2.2 and
two classes to overwrite calls to old Spring APIs (see issue 
[2932 in springflox](https://github.com/springfox/springfox/issues/2932)).

During the coding the source code was stored in GitLab and it used GitLab CI
to build, test and package the code and also create a docker image and deploy
it to some simple virtual machine to show it live and also do a simple
system test with a shell script (*test.sh*).

## Problem
When enabling swagger in the application file, the project starts up fine and
the swagger ui can be viewed, but all the Spring tests fail with the following
error message:
```
java.lang.IllegalStateException: Unable to find a @SpringBootConfiguration, you need to use @ContextConfiguration or @SpringBootTest(classes=...) with your test
```
I tried to find the problem by debugging deep into the code, but it was to
time-consuming so I gave up after some hours.

## REST interface
The OpenAPI/Swagger definition of the rest interface can be viewed with
http://localhost:8080/swagger-ui.html

## Security
The security part is not implemented in this project because it must match 
the rest of the environment.

In an environment built of several microservices the best security solution
is to use SSO (Single Sign On). This means there is an authentication service
that checks the provided credentials of the customer/client and on success
returns a token, which the browser/client will send in future requests to
the services. The services can then check this token against the
authentication service and also check the authorization. The most common
protocol for that is OAuth2.

This service is not the first system so it doesn't need the authentication
part, it just has to check the token against the authentication service.

Documentation of how to implement it with Spring Boot and OAuth2 can be
found here: https://www.baeldung.com/sso-spring-security-oauth2

## Code Quality
### Test coverage
The test coverage is currently not representative as there are not many
classes. There is only few code and many generated methods from Lombok.
Those are not all used but it is dumb to forcible test them to just get
the coverage up. From the real code (TicketRestController) 100% are tested!

The report can be view under *target/jacoco/index.html*
after a successful build.

### Forbidden APIs
The forbidden APIs check looks for problematic usage of methods without
locale or character set. There are no findings here!

For more information see https://github.com/policeman-tools/forbidden-apis

### OWASP dependency check
This maven plugin checks all used dependencies for known, published
vulnerabilities. There are currently no findings.

For more information see https://www.owasp.org/index.php/OWASP_Dependency_Check

### Spotbugs
Spotbugs does a static code analysis to find bugs in Java code. There
are no findings here.

For more information see https://spotbugs.github.io/

## Monitoring
The service provides actuator endpoints via an extra port 8081, only on
localhost for security reasons.

### Examples
* http://localhost:8081/actuator/health
* http://localhost:8081/actuator/info
* http://localhost:8081/actuator/metrics/http.server.requests
* http://localhost:8081/actuator/metrics/stats.WAITING
