java-apns is a Java client for Apple's Push Notification Service (APNS).
The library aims to provide a highly scalable interface to the Apple
server, while still being simple and modular.

The interface aims to require very minimal code to achieve the most common
cases, but have it be reconfigurable so you can even use your own networking
connections or JSON library if necessary.

Features:
--------------
  *  Easy to use, high performance APNS Service API
  *  Easy to use with Apple's certificates
  *  Easy to extend and reuse
  *  Easy to integrate with dependency injection frameworks


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

        String payload = APNS.alert("Can't be simpler that this!").build();
        String token = "fedfbcfb....";
        service.push(token, payload);

That's it!
