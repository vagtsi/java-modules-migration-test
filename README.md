# java-modules-migration-test
Minimal examples of figuring out solutions for migrating to java 9 modules for different use cases.

## elasticsearch-app
Small java module application accessing elasticssearch with it's [High Level REST Client](https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high.html) api. Using the (elasticsearch-restclient) uber jar.

## elasticsearch-restclient
Packaging the [High Level REST Client](https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high.html) libraries with dependencies into one uber jar with dedicated `Automatic-Module-Name` to solve the split packages of the many referenced libraries as discussed in 

- [Java 9: requiring elasticsearch from module-info.java fails with "module not found"](https://github.com/elastic/elasticsearch/issues/28984)
- and to be fixed with [Make it possible to use the high level rest client with modularized (jigsaw) applications](https://github.com/elastic/elasticsearch/issues/38299) 

For creating the uber jar this project uses the [Gradle Shadow Plugin](https://imperceptiblethoughts.com/shadow/).


## immutables-app-merged
Small application using Java [Immutables](https://immutables.github.io/) processor generating classes depedneing on `javax.annotation` and `JSR305` providing the same split packages. Although this is a well know issue for java modules it's not solved from the creatoes of the libraries. As workaround a merged uber jar available in maven central as [jsr305-and-javax.annotation-api](https://github.com/statisticsnorway/jsr305-and-javax.annotation-api/tree/master) is used here.

## immutables-app-patched
Same application as (immutables-app-merged) but using the original two libraries and patching the compiler arguments with the [gradle-modules-plugin](https://github.com/java9-modularity/gradle-modules-plugin).

Note: this is not working for the Eclipse IDE right now due to issues with the gradle generated `.classpath` file!