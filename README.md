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

![data flow scheme](https://ucd15f5defe2f2fe4c2f8387581a.previews.dropboxusercontent.com/p/thumb/AAeDNw25D4o3iz_-Bly_q2YgJjMhBnSB9UnfQwqke00W6x2QPnuduzsT6t8ZgUS38hhimMS8dIjY74OWcCIHyfK6F-0MwrTouTBgNHVBCmTbd6mQyb0SAT12QpepNNZe0K6kKXy9D_P4atKLmjTWRRRJMLg73dPD50aC-buHJjsfITMdEghO7OE60Szi8Vx79e5Onv54lL_Un32PNhqeiZh-oQzLmHUZk-XGLkPZ17Srvj05G6JiA_7B2isZHIWPUKBHOtBG-Ira9JY1cZAhqkPKGhyHaxi5dqWDpst_6Y_ew2FxeFq64ZEaSqPeWjKQiEzOdDUwqigK1wgVgYe_9X8npPO_wY6fOtWjX_zHeyunrG3PD0exqSAKixTMXhPVDjc/p.png?fv_content=true&size_mode=5)

## How to run
For simplicity I wrapped application with docker. You can build & run it just with commands
```bash
$ sbt docker
$ docker-compose up
```
If you don't have `docker-composer` you can use docker directly
```bash
$ docker run -it --rm --name sharing-backend -p 9999:9999 sharing-backend/sharing-backend
```
After this commands application will start on `0.0.0.0:9999` address. You can check it easily by command
```bash
curl -XGET 'http://localhost:9999/sharings'
```
and you will see an output:
```json
[]
```
it means that there aren't any sharing in the storage.
If you want to add something to the application just run this command
```bash
curl -XPOST 'http://localhost:9999/sharings' -H "Content-Type: application/json" -d '{"users":["foo@bar.com","example@gmail.com"],"selections":["HRReport!A1","Actuals!B1:B100","Assumptions","Dashboard!C1:C4"]}
```
As a response you will receive a JSON-object with `sharing` uuid:
```json
{"id":"6755ce20-d9f4-468e-8ad6-8505ead69116"}
```
That's all.
All docker configuration is hardcoded and of course it isn't a production ready configuration. 
For production it's more convenient to pass parameters via environment variables.
  
## Questions

1. If you had to execute some logic (for example generating a new document) every time a sharing is added/modified, where would you
   put that piece of code? What changes would you have to do in your project?
2. How was your API design process and what was your motivation to on the choice of patterns.

## My answers

1. I think the best way to solve this issue would be to add such abstraction as request context and inject this context directly to every request. If we speak about current application
the most convenient way is to pass this context via implicit variable at constructor or at method signature, the same way as ExecutionContext is passing now. It's pretty easy to implement now. 
We need just to change signature of controller and nothing else.

2. It was really interesting. I've spent about 2-3 hours during modeling and thinking about application data model. 
Then I've started implementing of main interfaces in the application. Work of software architect is full of compromises and 
every time you need to think about domain model, performance, supporting, etc. I think my application is more about performance and supporting and 
the most obvious way to improve it is to add persistent storage, something lightweight (redis, rockDB, etc.) instead of Full relational databases. 
I hope you will like it and I'll have an opportunity to tell you more about this application and my experience at all. Thank you!         



  

