package com.kony.push.customfcmservice;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.konylabs.android.KonyApplication;
import com.konylabs.android.KonyMain;
import com.konylabs.fcm.KonyFCMService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class CustomFCMService extends KonyFCMService {

    private static int pushMsgNotificationId = 0;

    @Override
    public void onMessageReceived(com.google.firebase.messaging.RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d("JohnVinodh","on Message Received method");
    }

    @Override
    public void showPushMessageNotification(Context context, Map<String, String> data) {
        String pkgName = context.getPackageName();
        int resId = context.getResources().getIdentifier("notify_push_msg", "string", pkgName);
        String enableNotifyPushMsg = context.getString(resId);
        if (enableNotifyPushMsg != null && enableNotifyPushMsg.equalsIgnoreCase("true")) //Check if push is enabled or not
        {
            resId = context.getResources().getIdentifier("notify_push_msg_icon", "string", pkgName);
            String iconName = null;
            if(resId != 0)
                iconName = context.getString(resId);

            if(iconName == null || iconName.equals("icon")){
                resId = context.getResources().getIdentifier("app_notify_push_msg_icon", "string", pkgName);
                iconName = context.getString(resId);
            }

            int icon = context.getResources().getIdentifier(iconName, "drawable", pkgName);
            if (icon == 0)
                icon = context.getResources().getIdentifier("icon", "drawable", pkgName);

            resId = context.getResources().getIdentifier("notify_push_msg_title_from_payload", "string", pkgName);
            String titleFromPayload = context.getString(resId);
            String title = null;
            String desc = null;
            String randomKey = null;
            Boolean pickTitleAndDescriptionFromPushPayloadKey = getPreferenceBoolean(context,"respect_default_push_title_and_description_keys",false);
            if(pickTitleAndDescriptionFromPushPayloadKey){
                title = data.get("title");
                desc = data.get("content");
            }
            if(title == null){
                if (titleFromPayload != null && titleFromPayload.equalsIgnoreCase("true")) {
                    resId = context.getResources().getIdentifier("notify_push_msg_title_keys", "string", pkgName);
                    if (resId != 0) {
                        String titleKeysStr = context.getString(resId);
                        if (titleKeysStr != null && titleKeysStr.trim().length() > 0) {
                            String[] titleKeys = titleKeysStr.split(",");
                            for (int i = 0; i < titleKeys.length; ++i) {
                                if ((title = data.get(titleKeys[i])) != null)
                                    break;
                            }
                        }
                    }

                }
                if (title == null) {
                    resId = context.getResources().getIdentifier("notify_push_msg_default_title", "string", pkgName);
                    title = context.getString(resId);
                }

                if(title == null){
                    title = data.get("title");
                }
            }
            resId = context.getResources().getIdentifier("notify_push_msg_desc_from_payload", "string", pkgName);
            String descFromPayload = context.getString(resId);

            if(desc == null){
                if (descFromPayload != null && descFromPayload.equalsIgnoreCase("true")) {
                    resId = context.getResources().getIdentifier("notify_push_msg_desc_keys", "string", pkgName);
                    if (resId != 0) {
                        String descKeysStr = context.getString(resId);
                        if (descKeysStr != null && descKeysStr.trim().length() > 0) {
                            String[] descKeys = descKeysStr.split(",");
                            for (int i = 0; i < descKeys.length; ++i) {
                                if ((desc = data.get(descKeys[i])) != null)
                                    break;
                            }
                        }
                    }
                    if(desc == null){
                        desc = data.get("content");
                    }
                    if (desc == null) {
                        //Take some randon value from payload bundle
                        if (randomKey == null) {
                            Set<String> keySet = data.keySet();
                            if (keySet != null && !keySet.isEmpty()) {
                                Iterator<String> it = keySet.iterator();
                                if (it.hasNext())
                                    randomKey = it.next();
                            }
                        }

                        desc = data.get(randomKey);
                    }
                }
                if (desc == null) {
                    resId = context.getResources().getIdentifier("notify_push_msg_default_desc", "string", pkgName);
                    desc = context.getString(resId);
                }

            }
            resId = context.getResources().getIdentifier("notify_push_msg_sound", "string", pkgName);
            String sound = context.getString(resId);

            resId = context.getResources().getIdentifier("notify_push_msg_vibrate", "string", pkgName);
            String vibrate = context.getString(resId);

            resId = context.getResources().getIdentifier("notify_push_msg_lights", "string", pkgName);
            String lights = context.getString(resId);

            resId = context.getResources().getIdentifier("notify_push_msg_clear", "string", pkgName);
            String clear = context.getString(resId);

            resId = context.getResources().getIdentifier("notify_push_msg_priority", "string", pkgName);
            String priorityValue = resId!=0 ? context.getString(resId) : "";

            resId = context.getResources().getIdentifier("notify_push_msg_visibility", "string", pkgName);
            String visibilityValue = resId!=0 ? context.getString(resId) : "";

            resId = context.getResources().getIdentifier("notify_push_msg_background_color", "string", pkgName);
            String backgroundColorFromConfig = resId!=0 ? context.getString(resId) : "";

            int notificationId = generatePushMessageNotificationId(context);
            PendingIntent contentIntent = createNotificationPendingIntent(context, data, notificationId);

            int priorityIntValue =	Notification.PRIORITY_DEFAULT;
            boolean shouldVibrate = false;

            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(icon)
                            .setAutoCancel(true)
                            .setContentText(desc).setWhen(System.currentTimeMillis()).setContentIntent(contentIntent);
            if(title !=null){
                //default configuration for notify_push_msg_default_title in pushconfig.xml was "New Push Message"
                //current default configuration for notify_push_msg_default_title is ""
                //if the user configures the notify_push_msg_default_title then only its respected
                if(!(title.equalsIgnoreCase("New Push Message") || title.isEmpty()))
                    notificationBuilder.setContentTitle(title);
            }
            int flag = 0;
            if(clear != null && clear.equalsIgnoreCase("true"))
                flag |= Notification.FLAG_NO_CLEAR;
            if(sound != null && sound.equalsIgnoreCase("true"))
                flag |=Notification.DEFAULT_SOUND;
            if(vibrate != null && vibrate.equalsIgnoreCase("true")){
                flag |=Notification.DEFAULT_VIBRATE;
                shouldVibrate =true;
            }
            if(lights != null && lights.equalsIgnoreCase("true"))
                flag |=Notification.DEFAULT_LIGHTS;
            if(flag >0)
                notificationBuilder.setDefaults(flag);

            ArrayList<NotificationCompat.Action> wearbleActions = new ArrayList<NotificationCompat.Action>();
            ArrayList<NotificationCompat.Action> actions = new ArrayList<NotificationCompat.Action>();

            String categoryId = data.get("category");
            //KonyNotificationSettingsDB db = new KonyNotificationSettingsDB();

            if(priorityValue != null && !priorityValue.equalsIgnoreCase("")){
                try{
                    priorityIntValue = Integer.parseInt(priorityValue);
                    notificationBuilder.setPriority(priorityIntValue);
                }
                catch (NumberFormatException ex) {

                    Log.e("CustomFCMService","NumberFormat Exception ::"+ ex.getMessage());
                }
            }

            if(visibilityValue != null && !visibilityValue.equalsIgnoreCase("")){
                try{
                    notificationBuilder.setVisibility(Integer.parseInt(visibilityValue));
                }
                catch (NumberFormatException ex) {
                    Log.e("CustomFCMService","NumberFormat Exception ::"+ ex.getMessage());

                }
            }

           /* if (categoryId != null && categoryId.length() > 0) {
                String actionIds[] = db.getRegisteredActionsByCategory(categoryId);
                if (actionIds != null) {
                    for (String actionId : actionIds) {
                        if (actionId.length() > 0) {
                            String[] actionInfo = db.getActionsInfoById(actionId);
                            if (actionInfo != null) {
                                Integer visibleOn = Integer.valueOf(actionInfo[2]);

                                PendingIntent wearPendingIntent = createNotificationPendingIntentForAction(context, data, actionId, notificationId);
                                try {
                                    resId = context.getResources().
                                            getIdentifier(actionInfo[1].substring(0, actionInfo[1].indexOf(".")), "drawable", context.getPackageName());
                                } catch (Exception e) {
                                    resId = 0;
                                }
                                NotificationCompat.Action action = new NotificationCompat.Action.Builder(
                                        resId, actionInfo[0], wearPendingIntent)
                                        .build();

                                if (visibleOn == ACTION_VISIBLEON_WATCH_ONLY) {
                                    wearbleActions.add(action);
                                } else { //means both
                                    wearbleActions.add(action);
                                    actions.add(action);
                                }
                            }
                        }
                    }
                    notificationBuilder.extend(new NotificationCompat.WearableExtender().addActions(wearbleActions));
                    for (NotificationCompat.Action action : actions) {
                        notificationBuilder.addAction(action);
                    }
                }

                HashMap<String, Object> map = db.getPropertyInfo(categoryId);
                db.close();

                if (map != null && map.size() > 0) {
                    Set<String> keySet = map.keySet();
                    for (String key : keySet) {
                        Object value = map.get(key);
                        if (key.equalsIgnoreCase("priority")) {
                            int priority = (int) Double.parseDouble((String) value);
                            priorityIntValue = priority;
                            notificationBuilder.setPriority(priority);
                        }
                    }
                }
            }*/

//When you target Android 8.0 (API level 26), you must implement one or more notification channels to display notifications to your users
            int mSDKVersion = Build.VERSION.SDK_INT;
            if (mSDKVersion >= 26) {

                NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                String channelId = "push_" + getPackageName();
                resId = context.getResources().getIdentifier("notify_push_msg_channel_title", "string", pkgName);
                String channelName = resId!=0 ? context.getString(resId) : "Remote Notifications";
                resId = context.getResources().getIdentifier("notify_push_msg_channel_desc", "string", pkgName);
                String channelDescription = resId!=0 ?  context.getString(resId) : "All remote notifications will be displayed under this category";

                NotificationChannel channel = new NotificationChannel(channelId,
                        channelName,
                        getEquivalentImportanceValueForNotificationPriority(priorityIntValue));
                channel.setDescription(
                        channelDescription);
                channel.enableLights(true);
                channel.enableVibration(shouldVibrate);
                //channel.setShowBadge(KonyStartupInitializer.preferences.getPreferenceBoolean(KonyNotificationSettingsManager.KEY_PUSH_NOTIFICATION_SHOW_BADGE,KonyNotificationSettingsManager.showBadgeDefaultValue));
                //it will create channel or update if channel already exists with same id
                mNotificationManager.createNotificationChannel(channel);
                notificationBuilder.setChannelId(channelId);

            }

            Notification notification = notificationBuilder.build();
/*            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            if (clear != null && clear.equalsIgnoreCase("true"))
                notification.flags |= Notification.FLAG_NO_CLEAR;
            if (sound != null && sound.equalsIgnoreCase("true"))
                notification.defaults |= Notification.DEFAULT_SOUND;
            if (vibrate != null && vibrate.equalsIgnoreCase("true"))
                notification.defaults |= Notification.DEFAULT_VIBRATE;
            if (lights != null && lights.equalsIgnoreCase("true"))
                notification.defaults |= Notification.DEFAULT_LIGHTS;*/

            // Get an instance of the NotificationManager service
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            // Build the notification and issues it with notification manager.
            notificationManager.notify(notificationId, notification);

        }
    }

    private final int generatePushMessageNotificationId(Context context) {
        ++pushMsgNotificationId;
        if (pushMsgNotificationId > getMaxNotificationsCount(context))
            pushMsgNotificationId = 1;

        return pushMsgNotificationId;
    }

    public  int getEquivalentImportanceValueForNotificationPriority(int priority)
    {
        switch(priority){
            case Notification.PRIORITY_DEFAULT:
                return NotificationManager.IMPORTANCE_DEFAULT;
            case Notification.PRIORITY_HIGH:
                return NotificationManager.IMPORTANCE_HIGH;
            case Notification.PRIORITY_LOW:
                return NotificationManager.IMPORTANCE_LOW;
            case Notification.PRIORITY_MIN:
                return NotificationManager.IMPORTANCE_MIN;
            case Notification.PRIORITY_MAX:
                return NotificationManager.IMPORTANCE_HIGH;
            default:
                return NotificationManager.IMPORTANCE_DEFAULT;
        }
    }

    private final int getMaxNotificationsCount(Context context) {
        int maxNoti = 1;
        String pkgName = context.getPackageName();
        int resId = context.getResources().getIdentifier("notify_push_msg_notifications_count", "string", pkgName);
        if (resId != 0) {
            String maxNotificationsStr = context.getString(resId);
            try {
                maxNoti = Integer.parseInt(maxNotificationsStr);
                if (maxNoti > 50) {
                    maxNoti = 50;
                }
            } catch (Exception exception) {}
        }

        return maxNoti;
    }

    public boolean getPreferenceBoolean(Context context, String key, boolean defaultvalue) {
        SharedPreferences pref = context.getSharedPreferences("KonyPrefs", 0);
        return pref.getBoolean(key, defaultvalue);
    }
}
