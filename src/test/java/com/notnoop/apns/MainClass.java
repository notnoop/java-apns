package com.notnoop.apns;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsDelegate;
import com.notnoop.apns.ApnsNotification;
import com.notnoop.apns.ApnsService;
import com.notnoop.apns.DeliveryError;
import com.notnoop.exceptions.InvalidSSLConfig;

public class MainClass {

    /**
     * @param args
     * @throws FileNotFoundException 
     * @throws InvalidSSLConfig 
     */
    public static void main(String[] args) throws InvalidSSLConfig, FileNotFoundException {
        if (args.length != 3) {
            System.err.println("Usage: test <p|s> <cert> <cert-password>\ntest p ./cert abc123");
            System.exit(777);
        }
        
        ApnsDelegate delegate = new ApnsDelegate() {
            public void messageSent(ApnsNotification message, boolean resent) {
               System.out.println("Sent message "+message+" Resent: "+resent);                
            }

            public void messageSendFailed(ApnsNotification message, Throwable e) {
                System.out.println("Failed message "+message);
                
            }

            public void connectionClosed(DeliveryError e, int messageIdentifier) {
                System.out.println("Closed connection: "+messageIdentifier+"\n   deliveryError "+e.toString());                
            }

            public void cacheLengthExceeded(int newCacheLength) {
                System.out.println("cacheLengthExceeded "+newCacheLength);          
                
            }

            public void notificationsResent(int resendCount) {
                System.out.println("notificationResent "+resendCount);                
            }
        };
            
        final ApnsService svc = APNS.newService()
                .withAppleDestination(args[0].equals("p"))
                .withCert(new FileInputStream(args[1]), args[2])
                .withDelegate(delegate)
                .build();
        
        final String goodToken = "eb69b183ec173a0da29e488f0fb72f922cb026cad894cc66f8b908531982bf81";
        //final String goodToken =   "d974c9256db442d7a48d1d7341050a8fcdc9312f48965946f0bc38b09f074275";
             
        String payload = APNS.newPayload().alertBody("Wrzlmbrmpf dummy alert").build();
        
        svc.start();
        System.out.println("Sending to good token");
        ApnsNotification goodMsg = svc.push(goodToken, payload); 
        System.out.println("Good msg id: "+goodMsg.getIdentifier());

        
//        System.out.println("Sending to bad token");
//        ApnsNotification badMsg = svc.push(badToken, payload);
//        System.out.println("Bad msg id: "+ badMsg.getIdentifier());

        System.out.println("Getting inactive devices");
        
        Map<String, Date> inactiveDevices = svc.getInactiveDevices();
    
        for (Entry<String, Date> ent : inactiveDevices.entrySet()) {
            System.out.println("Inactive "+ent.getKey()+" at date "+ent.getValue());
        }
        System.out.println("Stopping service");
        svc.stop();
    }
}
