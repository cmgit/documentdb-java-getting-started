---
services: documentdb
platforms: java
author: arramac
---

# Developing a Java app using DocumentDB
This sample shows you how to use the Microsoft Azure DocumentDB service to store and access data from a Java application.

## Running this sample

* Before you can run this sample, you must have the following prerequisites:

   * An active Azure account. If you don't have one, you can sign up for a [free account](https://azure.microsoft.com/free/). Alternatively, you can use the [Azure DocumentDB Emulator](https://azure.microsoft.com/documentation/articles/documentdb-nosql-local-emulator) for this tutorial.
   * JDK 1.7+ (Run `apt-get install default-jdk` if you don't have JDK)
   * Maven (Run `apt-get install maven` if you don't have Maven)

* Then, clone this repository using `git clone git@github.com:arramac/documentdb-java-getting-started.git`

* Next, substitute the endpoint and authorization key in Program.java with your DocumentDB account's values. 

* From a command prompt or shell, run `mvn package` to compile and resolve dependencies.

* From a command prompt or shell, run `mvn exec:java -D exec.mainClass=GetStarted.Program` to run the application.

## About the code
The code included in this sample is intended to get you quickly started with a Java application that connects to Azure DocumentDB.

## More information

- [Azure DocumentDB Documentation](https://azure.microsoft.com/documentation/services/documentdb/)
- [Azure DocumentDB Java SDK](https://docs.microsoft.com/azure/documentdb/documentdb-sdk-java)
- [Azure DocumentDB Java SDK Reference Documentation](http://azure.github.io/azure-documentdb-java/)

