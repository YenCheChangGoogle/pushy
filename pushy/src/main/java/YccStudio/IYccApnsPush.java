package YccStudio;

import java.io.File;

import com.eatthepath.pushy.apns.PushNotificationResponse;

@SuppressWarnings("rawtypes")
public interface IYccApnsPush {
    
    public PushNotificationResponse push(File certFilePath, String certPassword, String topic, String deviceToken, String pushMessage, String pushMessageTitle, int badgeNumber, String us, String msgId);
    public PushNotificationResponse push(String certFilePath, String certPassword, String topic, String deviceToken, String pushMessage, String pushMessageTitle, int badgeNumber, String us, String msgId);
    
    public java.util.List<PushNotificationResponse> push(File certFile, String certPassword, String topic, String[] deviceTokens, String pushMessage, String pushMessageTitle, int badgeNumber, String us, String msgId);
    public java.util.List<PushNotificationResponse> push(String certFilePath, String certPassword, String topic, String[] deviceTokens, String pushMessage, String pushMessageTitle, int badgeNumber, String us, String msgId);
    
}
