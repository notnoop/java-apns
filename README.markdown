Build status:

   * Main fork [![Build Status](https://travis-ci.org/notnoop/java-apns.png)](https://travis-ci.org/notnoop/java-apns)
   * Development [![Build Status](https://travis-ci.org/java-apns/java-apns.png)](https://travis-ci.org/java-apns/java-apns)

Development - Version 1.0.0
---------------------------
There currently is a perelease for 1.0.0 which fixes a number of problems over 0.2.3. However there's still
a CI test that sporadically fails, so 1.0.0 is NOT meant for production, yet. Further development for
1.0.0 happens on [java-apns/java-apns] (https://github.com/java-apns/java-apns)


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

Sample Code
----------------

To send a notification, you can do it in two steps:

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
     
If you want to use enhanced notifications for error handling, you first have to write a adapter class that implements ApnsDelegate. 
Within the messageSendFailed method you then may implement your custom code, for example:

     @Override
     public void messageSendFailed(ApnsNotification message, Throwable e)
     {
          System.err.println("MessageSendFailed: " + e.getMessage());
     }

Next you may instantiate your custom delegate and submit it to your push notification using the withDelegate method of the ApnsServiceBuilder. A very basic example may look like this:

     public void pushMessage(List<String> receivers, String message, String certificatePath, String certificatePass)
     {
          // create a new delagate
          ApnsDelegate delagate = new CustomApnsDelegate();
     
          // build a new apns service and submit the created delagate to it
          ApnsService service = APNS.newService().withCert(certificatePath, certpass).withSandboxDestination().withDelegate(delagate).build();
     
          // compose your push notification
          String payload = APNS.newPayload().alertBody(message).badge(1).noActionButton().build();
     
          // push the notification
          try
          {
               System.out.println("Pushing notification.");
               service.push(receivers, payload);
          }
          catch(Exception e)
          {
               //TODO error handling
               System.out.println("Push failed.");
          }
     }
      
When an error occures while delivering your message, APNS will return an error code before closing the socket. This error code will be received by your ApnsDelegate adapter class where the messageSendFailed method will be called. Also an exception on the ApnsService.push method will be risen.

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
