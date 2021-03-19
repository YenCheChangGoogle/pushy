package com.eatthepath.pushy.console;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.SSLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eatthepath.pushy.apns.ApnsClient;
import com.eatthepath.pushy.apns.ApnsClientBuilder;
import com.eatthepath.pushy.apns.PushNotificationResponse;
import com.eatthepath.pushy.apns.util.ApnsPayloadBuilder;
import com.eatthepath.pushy.apns.util.SimpleApnsPayloadBuilder;
import com.eatthepath.pushy.apns.util.SimpleApnsPushNotification;
import com.eatthepath.pushy.apns.util.TokenUtil;
import com.eatthepath.pushy.apns.util.concurrent.PushNotificationFuture;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

//<dependency>
//  <groupId>com.relayrides</groupId>
//  <artifactId>pushy</artifactId>
//  <version>0.9.3</version>
//</dependency>
//使用 com.eatthepath.pushy.apns.ApnsClient 範例

public class PushyDemo {

    private static Logger logger=LoggerFactory.getLogger(PushyDemo.class);

    //TODO 測試
    public PushNotificationResponse test() throws InvalidKeyException, SSLException, NoSuchAlgorithmException, IOException, InterruptedException, URISyntaxException {

        //TLS authentication
        File p12File=new File("D:/Senao/bitbucket.org/prod_appsvc.git/WebContent/apps_cert_files/SPLUS2R.p12");
        String p12Password="qwer1234";

        EventLoopGroup eventLoopGroup=new NioEventLoopGroup(10); //注意: EventLoopGroup的線程數不要配置超過ConcurrentConnections連接數

        final ApnsClient apnsClient=new ApnsClientBuilder().

        //setApnsServer(ApnsClientBuilder.DEVELOPMENT_APNS_HOST). //api.sandbox.push.apple.com
        setApnsServer(ApnsClientBuilder.PRODUCTION_APNS_HOST). //api.push.apple.com

        setClientCredentials(p12File, p12Password).

        //setProxyHandlerFactory(new Socks5ProxyHandlerFactory(new InetSocketAddress("PROXY主機",3128), "PROXY帳號", "PROXY密碼")).  //透過一般方式使用PROXY
        //setProxyHandlerFactory(HttpProxyHandlerFactory.fromSystemProxies(ApnsClientBuilder.DEVELOPMENT_APNS_HOST)). //透過JAVA系統設定使用PROXY https://docs.oracle.com/javase/7/docs/api/java/net/doc-files/net-properties.html

        //APNs服務器可以保證同時發送1500條消息，當超過這個限制時，Pushy會緩存消息，所以我們不必擔心異步操作發送的消息過多
        //setConcurrentConnections(10). //注意: EventLoopGroup的線程數不要配置超過ConcurrentConnections連接數
        //setEventLoopGroup(eventLoopGroup). //注意: EventLoopGroup的線程數不要配置超過ConcurrentConnections連接數

        build();

        //要發送的
        final SimpleApnsPushNotification pushNotification;
        final ApnsPayloadBuilder payloadBuilder=new SimpleApnsPayloadBuilder(); //ApnsPayloadBuilder
        payloadBuilder.setAlertBody("YenCheChang 推播 範例");
        payloadBuilder.setAlertTitle("YenCheChang Title 範例");
        final String payload=payloadBuilder.build(); //推播有效載荷JSON
        final String deviceToken=TokenUtil.sanitizeTokenString("375c78f0411c3246ffcc85faae33438a080a854ad131ba55df9709fc259020ed");
        String topic="tw.com.senao.splus2rInhouse";
        pushNotification=new SimpleApnsPushNotification(deviceToken, topic, payload);

        final PushNotificationFuture<SimpleApnsPushNotification, PushNotificationResponse<SimpleApnsPushNotification>> sendNotificationFuture;
        sendNotificationFuture=apnsClient.sendNotification(pushNotification);

        PushNotificationResponse<SimpleApnsPushNotification> pushNotificationResponse=null;
        try {
            //取得APNs回應
            pushNotificationResponse=sendNotificationFuture.get();

            if(pushNotificationResponse.isAccepted()) {
                logger.debug("Sends a push notification to the APNs gateway (Accepted)");
            } else {
                logger.debug("Sends a push notification to the APNs gateway (Rejected) 拒絕理由 getRejectionReason()="+pushNotificationResponse.getRejectionReason()+", getTokenInvalidationTimestamp()="+pushNotificationResponse.getTokenInvalidationTimestamp()+", deviceToken="+deviceToken);
            }

            sendNotificationFuture.whenComplete((response, cause) -> {
                if(response!=null) {
                    logger.debug("推播成功 (已完成)");
                } else {
                    logger.debug("推播失敗 (表示實際上發送通知或等待回覆時出了點問題)");
                }
            });

        } catch(ExecutionException exe) {
            exe.printStackTrace();
        }

        //結束推播處理
        CompletableFuture<Void> closeFuture=apnsClient.close();

        return pushNotificationResponse;
    }

    public static void main(String[] args) {

        PushyDemo p=new PushyDemo();
        try {
            p.test();
        } catch(InvalidKeyException|NoSuchAlgorithmException|IOException|InterruptedException|URISyntaxException e) {
            e.printStackTrace();
        }

    }

}
