# Sharings back end API

## Problem description

In your preferred language, write a back end API application that receives, stores and list sharings. Those sharings consist of recipient emails and
selections. The client will send both as lists, which means a single sharing specify multiple recipients and multiple selections.

## Solution notes
This solution is written on Scala language with list of the libraries:
* [akka-http](https://doc.akka.io/docs/akka-http/current/index.html). Application HTTP-transport is implemented with this library. 
* [akka](https://akka.io/). Application storage layer is using an actor interface.
* [spray-json](https://github.com/spray/spray-json). JSON serialization/deserialization.

## Project structure

All project is stored in `ekb.validol.sharing.backend` package. The way this application is divided into layers is in compliance with SOLID principles.
Also for tests and supporting simplicity application is splitted by layers due CAKE-pattern (a.k.a Baklava code). Here is a short
description of project structure:
* `controller` package. This part is responsible for application data flow. Here you can find `MainController` which is 
responsible for all requests handling. This application is quite small that's why it contains only one controller, but the application
is designed in way of containing dozens of controllers. You can add new controller and you don't need to do any modifications, just extension ([open/close principle](https://en.wikipedia.org/wiki/Open%E2%80%93closed_principle)).
Also `MainController` is stateless therefore it's thread-safe.
* `model` package. Here you can find all application business models. All models are designed in according to [DDD](https://en.wikipedia.org/wiki/Domain-driven_design) principles.
The most interesting entity is `Selection`. Here you can find an abstraction of possible selections. Also `Cell` entity implements `Ordering` trait
that's why we can compare/sort `Cell` entities which is really useful. Also in `Format` object you can find all stuff about serialization/deserialization.
* `storage` package. This part is responsible for data storing. Current version supports only in memory data storage. This storage is absolutely thread-safe, because it 
stores data in Actor. If we need persistence we can easily replace Actor with Persistent Actor without any code changing. I decided to implement 
this part in the simplest way without any heavy abstractions and relations. In this situations all this abstractions decrease performance. For example it's 
obvious that sometimes (even often) users can duplicate selections or have cross selections and I think the best way to solve this problem would be 
asynchronous task or event another micro service. Realtime application should chase only the aim of performance and availability.
* `utils` package. Package for some helpful tools such as email validator. Nothing interesting at all.
* `transport` package. This is the public API package. Current application has only HTTP RESTFull interface. In according to 
[Richardson's Maturity Model](https://martinfowler.com/articles/richardsonMaturityModel.html) we can refer this application to Level 3. Of course application is
too small but  however I built it with this model in my mind. The most interesting class is `HttpRequestHandler` because it contains the main handler function. 
It's really interesting to notice how application routes all requests from outside. For this aim I used scala abstraction which is called [PartialFunction](https://www.scala-lang.org/api/2.12.8/scala/PartialFunction.html).
This abstraction provides us really powerful opportunity to determine only routes that we want and nothing else. `handleAsync` function consists of 3 partial functions:

1. `convertRequest` which does converting from `HttpRequest` => `ApiRequest` model.
2. This step depends on controllers that we have in our application. If we have controller which is defined for current `ApiRequest` we send message to it.
If we don't - we send an error to client.
3. After controller's calculation application passes the result to `convertResponse` partial function which does converting from `ApiResponse` => `HttpResponse`.

In short words this part of the system works like dynamic flow. It's really powerful and gives us wide opportunities to extend an application. Here is a small scheme how it works:

[data flow scheme](https://drive.google.com/open?id=1fDDW0nO7zXPJ_WN399UMgOYXYJrd_Ey2)



  

