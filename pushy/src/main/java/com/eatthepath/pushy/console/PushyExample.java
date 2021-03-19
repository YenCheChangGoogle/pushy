package com.eatthepath.pushy.console;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.relayrides.pushy.apns.ApnsClient;
import com.relayrides.pushy.apns.ApnsClientBuilder;
import com.relayrides.pushy.apns.ClientNotConnectedException;
import com.relayrides.pushy.apns.PushNotificationResponse;
import com.relayrides.pushy.apns.util.ApnsPayloadBuilder;
import com.relayrides.pushy.apns.util.SimpleApnsPushNotification;

import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

//<dependency>
//    <groupId>com.relayrides</groupId>
//    <artifactId>pushy</artifactId>
//    <version>0.9.3</version>
//</dependency>
//使用 com.relayrides.pushy.apns.ApnsClient 範例

public class PushyExample {

    private static Logger logger=LoggerFactory.getLogger(PushyExample.class);

    public static void main(String[] args) throws Exception {
        final ApnsClient apnsClient=new ApnsClientBuilder().setClientCredentials(new File("D:/SenaoECBackendWorkspace/prod_appsvc_apache-tomcat-7.0.85/webapps/prod_appsvc/apps_cert_files/SPLUS2R.p12"), "qwer1234").build();

        //final Future<Void> connectFutrue=apnsClient.connect(ApnsClient.DEVELOPMENT_APNS_HOST);
        final Future<Void> connectFutrue=apnsClient.connect(ApnsClient.PRODUCTION_APNS_HOST);

        try {
            connectFutrue.await(10, TimeUnit.MINUTES);
        } catch(Exception e) {
            e.printStackTrace();
        }

        final ApnsPayloadBuilder payBuilder=new ApnsPayloadBuilder();
        payBuilder.setAlertBody("Pushy Example範例");
        String payload=payBuilder.buildWithDefaultMaximumLength();
        final String token="375c78f0411c3246ffcc85faae33438a080a854ad131ba55df9709fc259020ed";
        SimpleApnsPushNotification notification=new SimpleApnsPushNotification(token, "tw.com.senao.splus2rInhouse", payload);
        Future<PushNotificationResponse<SimpleApnsPushNotification>> responseFuture=apnsClient.sendNotification(notification);
        responseFuture.addListener(new GenericFutureListener<Future<PushNotificationResponse<SimpleApnsPushNotification>>>() {

            @Override
            public void operationComplete(Future<PushNotificationResponse<SimpleApnsPushNotification>> arg0) throws Exception {
                try {
                    final PushNotificationResponse<SimpleApnsPushNotification> pushNotificationResponse=arg0.get();

                    if(pushNotificationResponse.isAccepted()) {
                        logger.debug("Push Notification Accepted By APNs Gateway");
                    } else {
                        logger.debug("Push Notification Rejected By the APNs Gateway 拒絕理由 getRejectionReason()="+pushNotificationResponse.getRejectionReason()+", getTokenInvalidationTimestamp="+pushNotificationResponse.getTokenInvalidationTimestamp());
                    }
                } catch(final ExecutionException e) {
                    System.err.println("Failed to send push notification.");
                    e.printStackTrace();

                    if(e.getCause() instanceof ClientNotConnectedException) {
                        logger.debug("Waiting for client to reconnect…");
                        apnsClient.getReconnectionFuture().await();
                        logger.debug("Reconnected.");
                    }
                }
            }
        });

        Future<Void> disconnectFuture=apnsClient.disconnect();
        try {
            disconnectFuture.await(1, TimeUnit.HOURS);
        } catch(Exception e) {
            e.printStackTrace();
        }

    }
}
