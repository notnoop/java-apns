Reference: [APNS Problems](http://redth.codes/the-problem-with-apples-push-notification-ser/)

java-apns maintains a per-connection sent queue (cachedNotifications) like described in the above article

   * java-apns queue is bounded (cacheNotification() will poll notifications out of the queue when the queue gets too big.)

   * It does not regularly check whether sent messages have been sent a few seconds ago to remove them from the sent queue.

So if we send a lot of notifications without failure the old notifications will fall off the queue. This is typically ok (since they are "older" and probably have been sent successfully). The queue only serves to cache all the messages sent in between the client sending a bad notification and the APNS server replying.

Improvements to consider:

  * Refactoring the queue into a full type, freeing the ApnsConnectionImpl from a lot of queue handling code. (no functional changes)

  * record the last send time in the notification (or a wrapper) and poll() messages out of the queue that are older than a certain threshold. (Would guarantee very short queues with low notification volume)

  * When queue gets full anyways (additionally to date handling) voluntarily inject a bad message to enforce an answer (+reconnect) from APNS (bad idea, SSL reconnect is expensive)
