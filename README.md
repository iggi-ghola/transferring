# transferring
RESTful API for money transfers between accounts.

Use commands:
```
gradlew test
```
to run fast tests,
```
gradlew slowTests
gradlew allTests
```
to run only slow test or fast and slow tests,
```
gradlew stressTests -Dstress.time.minutes=n
```
to run stress tests for n minutes (n=1 by default),
```
gradlew installDist
cd build/install
```
to build self-run application

```
gradlew run
```
to run application from sources

What would be better in this code:
- REST is not good enough: ERROR codes are always 5XX.
- REST is not good enough in terms of methods and URIs:
```
@GET
@Path("/account/list")
``` 
and
```
@PUT
@Path("/account/create")
```
- Big coupling of logics, DAO classes have validation and money transfer checks.