package YccStudio.impl;

import java.io.File;
import java.util.Calendar;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eatthepath.pushy.apns.ApnsClient;
import com.eatthepath.pushy.apns.ApnsClientBuilder;
import com.eatthepath.pushy.apns.PushNotificationResponse;
import com.eatthepath.pushy.apns.util.ApnsPayloadBuilder;
import com.eatthepath.pushy.apns.util.SimpleApnsPayloadBuilder;
import com.eatthepath.pushy.apns.util.SimpleApnsPushNotification;
import com.eatthepath.pushy.apns.util.concurrent.PushNotificationFuture;

import YccStudio.IYccApnsPush;

public class YccApnsPushImpl implements IYccApnsPush {

    private static Logger logger=LoggerFactory.getLogger(YccApnsPushImpl.class);
    
    @SuppressWarnings({
        "unused", "rawtypes"
    })
    @Override
    public PushNotificationResponse push(File certFile, String certPassword, String topic, String deviceToken, String pushMessage, String pushMessageTitle) {

        //可參考 deviceToken=TokenUtil.sanitizeTokenString("375c78f0411c3246ffcc85faae33438a080a854ad131ba55df9709fc259020ed");
        //可參考 topic="tw.com.senao.splus2rInhouse";

        PushNotificationResponse pushNotificationResponse=null;
        ApnsClient apnsClient=null;
        try {
            //EventLoopGroup eventLoopGroup=new NioEventLoopGroup(10); //注意: EventLoopGroup的線程數不要配置超過ConcurrentConnections連接數
            
            apnsClient=new ApnsClientBuilder().
                setApnsServer(ApnsClientBuilder.PRODUCTION_APNS_HOST). //api.push.apple.com
                setClientCredentials(certFile, certPassword).

                //setProxyHandlerFactory(new Socks5ProxyHandlerFactory(new InetSocketAddress("PROXY主機",3128), "PROXY帳號", "PROXY密碼")).  //透過一般方式使用PROXY
                //setProxyHandlerFactory(HttpProxyHandlerFactory.fromSystemProxies(ApnsClientBuilder.DEVELOPMENT_APNS_HOST)). //透過JAVA系統設定使用PROXY https://docs.oracle.com/javase/7/docs/api/java/net/doc-files/net-properties.html

                //APNs服務器可以保證同時發送1500條消息, 當超過這個限制時, Pushy會緩存消息, 所以我們不必擔心異步操作發送的消息過多
                //setConcurrentConnections(10). //[注意] EventLoopGroup的線程數不要配置超過ConcurrentConnections連接數
                //setEventLoopGroup(eventLoopGroup). //[注意] EventLoopGroup的線程數不要配置超過ConcurrentConnections連接數
                
                build();
            
            //要發送的
            SimpleApnsPushNotification pushNotification;
            ApnsPayloadBuilder payloadBuilder=new SimpleApnsPayloadBuilder(); //ApnsPayloadBuilder
            payloadBuilder.setAlertBody(pushMessage);
            if(pushMessageTitle!=null) payloadBuilder.setAlertTitle(pushMessageTitle);
            payloadBuilder.setSound("default");
            
            String payload=payloadBuilder.build(); //推播有效載荷JSON

            pushNotification=new SimpleApnsPushNotification(deviceToken, topic, payload);

            PushNotificationFuture<SimpleApnsPushNotification, PushNotificationResponse<SimpleApnsPushNotification>> sendNotificationFuture=apnsClient.sendNotification(pushNotification);

            //取得APNs回應
            pushNotificationResponse=sendNotificationFuture.get();

            if(pushNotificationResponse.isAccepted()) {
                logger.debug("Push Notification Accepted By APNs Gateway");
            } else {
                logger.debug("Push Notification Rejected By APNs Gateway 拒絕理由 getRejectionReason()="+pushNotificationResponse.getRejectionReason()+", getTokenInvalidationTimestamp()="+pushNotificationResponse.getTokenInvalidationTimestamp());
            }

            sendNotificationFuture.whenComplete((response, cause) -> {
                if(response!=null) {
                    logger.debug("推播成功 (已完成)");
                } else {
                    logger.debug("推播失敗 (表示實際上發送通知或等待回覆時出了點問題)");
                }
            });

        } catch(Exception e) {
            logger.error(e.getMessage());
        }
        finally {
            if(apnsClient!=null) {
                CompletableFuture<Void> closeFuture=apnsClient.close();
            }
        }
        
        return pushNotificationResponse;
    }
    
    @SuppressWarnings("rawtypes")
    @Override
    public PushNotificationResponse push(String certFilePath, String certPassword, String topic, String deviceToken, String pushMessage, String pushMessageTitle) {
        return this.push(new java.io.File(certFilePath), certPassword, topic, deviceToken, pushMessage, pushMessageTitle);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public java.util.List<PushNotificationResponse> push(File certFile, String certPassword, String topic, String[] deviceTokens, String pushMessage, String pushMessageTitle) {
        
        java.util.ArrayList<PushNotificationResponse> result=new java.util.ArrayList<PushNotificationResponse>();
        ApnsClient apnsClient=null;
        try {
            //EventLoopGroup eventLoopGroup=new NioEventLoopGroup(10); //注意: EventLoopGroup的線程數不要配置超過ConcurrentConnections連接數
            
            apnsClient=new ApnsClientBuilder().
                setApnsServer(ApnsClientBuilder.PRODUCTION_APNS_HOST). //api.push.apple.com
                setClientCredentials(certFile, certPassword).

                //setProxyHandlerFactory(new Socks5ProxyHandlerFactory(new InetSocketAddress("PROXY主機",3128), "PROXY帳號", "PROXY密碼")).  //透過一般方式使用PROXY
                //setProxyHandlerFactory(HttpProxyHandlerFactory.fromSystemProxies(ApnsClientBuilder.DEVELOPMENT_APNS_HOST)). //透過JAVA系統設定使用PROXY https://docs.oracle.com/javase/7/docs/api/java/net/doc-files/net-properties.html

                //APNs服務器可以保證同時發送1500條消息, 當超過這個限制時, Pushy會緩存消息, 所以我們不必擔心異步操作發送的消息過多
                //setConcurrentConnections(10). //[注意] EventLoopGroup的線程數不要配置超過ConcurrentConnections連接數
                //setEventLoopGroup(eventLoopGroup). //[注意] EventLoopGroup的線程數不要配置超過ConcurrentConnections連接數
                
                build();
            
            //要發送的
            SimpleApnsPushNotification pushNotification;
            ApnsPayloadBuilder payloadBuilder=new SimpleApnsPayloadBuilder(); //ApnsPayloadBuilder
            payloadBuilder.setAlertBody(pushMessage);
            if(pushMessageTitle!=null) payloadBuilder.setAlertTitle(pushMessageTitle);
            payloadBuilder.setSound("default");
            
            String payload=payloadBuilder.build(); //推播有效載荷JSON
            
            for(String deviceToken:deviceTokens) {
                pushNotification=new SimpleApnsPushNotification(deviceToken, topic, payload);
                PushNotificationFuture<SimpleApnsPushNotification, PushNotificationResponse<SimpleApnsPushNotification>> sendNotificationFuture=null;
                com.eatthepath.pushy.apns.PushNotificationResponse pushNotificationResponse=null;
                try {
                    sendNotificationFuture=apnsClient.sendNotification(pushNotification); //Sends a push notification to the APNs gateway
                    pushNotificationResponse=sendNotificationFuture.get(); //取得當下那筆APNs回應
                    
                    if(pushNotificationResponse.isAccepted()) {
                        //logger.debug("Sends a push notification to the APNs gateway (Accepted)");
                    } else {
                        logger.error("Sends a push notification to the APNs gateway (Rejected), getRejectionReason()="+pushNotificationResponse.getRejectionReason()+", getTokenInvalidationTimestamp()="+pushNotificationResponse.getTokenInvalidationTimestamp()+", deviceToken="+deviceToken+" ");
                    }

                    sendNotificationFuture.whenComplete((response, cause) -> {
                        if(response!=null) {
                            //logger.debug("推播成功 (已完成) deviceToken="+deviceToken);
                        } else {
                            logger.error("推播失敗 (表示實際上發送通知或等待回覆時出了點問題) deviceToken="+deviceToken);
                        }
                    });                    
                    
                }catch(Exception e) {
                    logger.error(e.getMessage());
                }
                finally {
                    if(pushNotificationResponse!=null) result.add(pushNotificationResponse);
                }
            }
            
        } catch(Exception e) {
            logger.error(e.getMessage());
        }
        finally {
            if(apnsClient!=null) {
                @SuppressWarnings("unused")
                CompletableFuture<Void> closeFuture=apnsClient.close();
            }
        }
        
        return result;
    }
    
    @SuppressWarnings("rawtypes")
    public java.util.List<PushNotificationResponse> push(String certFilePath, String certPassword, String topic, String[] deviceTokens, String pushMessage, String pushMessageTitle) {
        return this.push(new java.io.File(certFilePath), certPassword, topic, deviceTokens, pushMessage, pushMessageTitle);
    }

    @SuppressWarnings({
        "static-access", "unused"
    })
    public static void main(String args[]) {
        File certFilePath=new File("D:/Senao/bitbucket.org/prod_appsvc.git/WebContent/apps_cert_files/SPLUS2R.p12");
        String certPassword="qwer1234";
        String topic="tw.com.senao.splus2rInhouse";
        String deviceToken="375c78f0411c3246ffcc85faae33438a080a854ad131ba55df9709fc259020ed";
        String pushMessage="Hello 張晏哲跟自己問好! 工作太多囉!!!";
        String pushMessageTitle="測試";
        
        //單一筆推播測試
        IYccApnsPush demo=new YccApnsPushImpl();
        demo.push(certFilePath, certPassword, topic, deviceToken, pushMessage, pushMessageTitle);
        
        Calendar cal=Calendar.getInstance();
        java.text.NumberFormat nf=java.text.NumberFormat.getInstance();
        nf.setMaximumIntegerDigits(2);
        nf.setMinimumIntegerDigits(2);
        String now=cal.get(cal.YEAR)+"-"+nf.format(1+cal.get(cal.MONTH))+"-"+nf.format(cal.get(cal.DAY_OF_MONTH))+" "+nf.format(cal.get(cal.HOUR_OF_DAY))+":"+nf.format(cal.get(cal.MINUTE))+":"+nf.format(cal.get(cal.SECOND));
        logger.debug(now);
        pushMessage+=" "+now;
        
        //多筆推播測試
        String deviceToken2="490bc459f2cffcf2d989ed7b784b3cdf9051b14f459976785c830402505460ce";
        String[] deviceTokens= {deviceToken, deviceToken2};
        //demo.push(certFilePath, certPassword, topic, deviceTokens, pushMessage, pushMessageTitle);
    }

}
