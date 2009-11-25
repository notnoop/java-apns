java-apns is a Java client for Apple's Push Notification Service (APNS).
The library aims to provide a highly scalable interface to the Apple
server, while still being simple and modular.

The interface aims to require very minimal code to achieve the most common
cases, but have it be reconfigurable so you can even use your own networking
connections or JSON library if necessary.

Features:
--------------
  *  Easy to use, high performance APNS Service API
  *  Supports Apple Feedback service
  *  Easy to use with Apple's certificates
  *  Easy to extend and reuse
  *  Easy to integrate with dependency injection frameworks
  *  Easy to setup custom notification payloads


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
                .customField("secret", "what do you think?");
		        .localizedKey("GAME_PLAY_REQUEST_FORMAT")
		        .localizedArguments("Jenna", "Frank")
		        .actionKey("Play").build();

	service.push(token, payload.toString());
