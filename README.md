
# Destined Souls 

A dating platform that connects two users who like each other. Upon successfully registering and logging in user will be prompted to the home page, where he'll be able to either Swipe potential candidates or Chat with his matches.  

Java non-blocking server built using [java.nio](https://docs.oracle.com/javase/8/docs/api/java/nio/package-summary.html). It consists of the main thread used to process incoming and outgoing data and a detached thread accepting connections on the server socket channel. Client-server communication has been achieved using **HTTP** (for general requests) and **Websockets** (for chat and notifications). Functionality for both protocols on the server has been written from scratch using only Java APIs.

## Technologies

**Client:** [![Angular](https://img.shields.io/badge/Angular-13-red)](https://angular.io/)

**Server:** [![Java](https://img.shields.io/badge/Java-11-blue)](https://www.oracle.com/java/technologies/javase/jdk11-archive-downloads.html)



## Build (Maven)

 1. Run Maven package 
 ```bash
 mvn package
 ```
 2. Run server-1.0-SNAPSHOT.jar
 ```bash
 java -jar Server\target\server-1.0-SNAPSHOT.jar
 ```
 3. You can access application on [port 3000](http://localhost:3000/#/)

## Acknowledgements

 - [Java NIO: Non-blocking Server](http://tutorials.jenkov.com/java-nio/non-blocking-server.html)
 - [Java WebSocket](https://github.com/TooTallNate/Java-WebSocket)
 

##  Developers

* [ Đorđe Tanasković 94/2017 ](https://github.com/djordjetane)
* [ Aleksa Veselić 317/2017 ](https://github.com/SgtCroWbaR)
