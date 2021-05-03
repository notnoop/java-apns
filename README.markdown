Warning
-------

Apple will no longer support the legacy binary protocol after March 31, 2021. Java-Apns will stop working. See [details](https://developer.apple.com/news/?id=uzyxiriy) in their announcement.

It is recommended to use [Pushy](https://github.com/jchambers/pushy) instead, which supports Apple's HTTP/2-based APNs protocol.

Build status:

   * Main fork [![Build Status](https://travis-ci.org/notnoop/java-apns.png)](https://travis-ci.org/notnoop/java-apns)
   * Development [![Build Status](https://travis-ci.org/java-apns/java-apns.png)](https://travis-ci.org/java-apns/java-apns)

Development - Version 1.0.0
---------------------------
There currently is a perelease for 1.0.0 which fixes a number of problems over 0.2.3. 
There's still a CI test that sporadically fails on Travis-CI only, but not on other test
machines I have access to. Supposedly it is a still undetected race condition.

However 1.0.0 Beta fixes a *lot* of problems over 0.2.x, so even as it is called beta
I'd recommend to use the beta instead of the 0.2.3 even for production.

froh42 will return to develop for java-apns in October, so I expect the 1.0.0 final
to be released start of November. Edited!


Introduction
------------

java-apns is a Java client for Apple Push Notification service (APNs).
The library aims to provide a highly scalable interface to the Apple
server, while still being simple and modular.

The interface aims to require very minimal code to achieve the most common
cases, but have it be reconfigurable so you can even use your own networking
connections or JSON library if necessary.

Links: [Installation](http://wiki.github.com/notnoop/java-apns/installation)
- [Javadocs](http://notnoop.github.com/java-apns/apidocs/index.html)
- [Changelog](CHANGELOG)

Features:
--------------
  *  Easy to use, high performance APNS Service API
  *  Supports Apple Feedback service
  *  Support Enhanced Apple Push Notification
  *  Support MDM and Newsstand Notifications
  *  Easy to use with Apple certificates
  *  Easy to extend and reuse
  *  Easy to integrate with dependency injection frameworks
  *  Easy to setup custom notification payloads
  *  Supports connection pooling
  *  Supports re-transmission of Notifications after error

Getting started
---------------

Add the following dependencies to your `pom.xml` file:


        <dependency>
             <groupId>com.notnoop.apns</groupId>
             <artifactId>apns</artifactId>
             <version>1.0.0.Beta6</version>
        </dependency>

Sample Code
-----------

To send a notification, you can do it in three steps:

1. Setup the connection

        ApnsService service =
            APNS.newService()
            .withCert("/path/to/certificate.p12", "MyCertPassword")
            .withSandboxDestination()
            .build();

2. Create and send the message

        String payload = APNS.newPayload().alertBody("Can't be simpler than this!").build();
        String token = "fedfbcfb....";
        service.push(token, payload);

3. To query the feedback service for inactive devices:

        Map<String, Date> inactiveDevices = service.getInactiveDevices();
        for (String deviceToken : inactiveDevices.keySet()) {
            Date inactiveAsOf = inactiveDevices.get(deviceToken);
            ...
        }

That's it!

Custom Payloads
----------------

You can send a message payload, but providing custom fields and
localizable alert:

    String payload = APNS.newPayload()
                .badge(3)
                .customField("secret", "what do you think?")
                .localizedKey("GAME_PLAY_REQUEST_FORMAT")
                .localizedArguments("Jenna", "Frank")
                .actionKey("Play").build();

    service.push(token, payload);


Enhanced Notification Format
----------------

You can use the enhanced notification format to get feedback from Apple about notifications that were unable to be processed.

     String payload = APNS.newPayload()
                .badge(3)
                .customField("secret", "what do you think?");
                .localizedKey("GAME_PLAY_REQUEST_FORMAT")
                .localizedArguments("Jenna", "Frank")
                .actionKey("Play").build();

     int now =  (int)(new Date().getTime()/1000);

     EnhancedApnsNotification notification = new EnhancedApnsNotification(EnhancedApnsNotification.INCREMENT_ID() /* Next ID */,
         now + 60 * 60 /* Expire in one hour */,
         token /* Device Token */,
         payload);

     service.push(notification);


License
----------------

Licensed under the [New 3-Clause BSD License](http://www.opensource.org/licenses/BSD-3-Clause).

    Copyright 2009, Mahmood Ali.
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are
    met:

      * Redistributions of source code must retain the above copyright
        notice, this list of conditions and the following disclaimer.
      * Redistributions in binary form must reproduce the above
        copyright notice, this list of conditions and the following disclaimer
        in the documentation and/or other materials provided with the
        distribution.
      * Neither the name of Mahmood Ali. nor the names of its
        contributors may be used to endorse or promote products derived from
        this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
    "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
    LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
    A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
    OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
    SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
    LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
    OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.


Contact
---------------
Support mailing list: http://groups.google.com/group/java-apns-discuss 
