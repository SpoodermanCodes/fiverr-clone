package com.example.fiverr.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class NotificationHelper {

    private static final String CHANNEL_BROWSE = "fiverr_browse_recommendations";
    private static final String CHANNEL_CART = "fiverr_cart_reminders";
    private static final String CHANNEL_ORDER = "fiverr_order_updates";
    private static final String CHANNEL_GENERAL = "fiverr_notifications";

    // -------------------------------------------------------------------------
    // 1. Browsing Recommendation Notification
    //    Data keys: type="browsing_recommendation", userName, recommendedGigs
    // -------------------------------------------------------------------------
    public static void showBrowsingRecommendation(Context context, String userName, String recommendedGigs) {
        createChannel(context, CHANNEL_BROWSE, "Recommendations",
                "Personalized gig recommendations", NotificationManager.IMPORTANCE_DEFAULT);

        String title = "Hey " + (userName != null && !userName.isEmpty() ? userName : "there") + "! 👋";
        String body;
        if (recommendedGigs != null && !recommendedGigs.isEmpty()) {
            body = "Based on your browsing, you might love: " + recommendedGigs;
        } else {
            body = "We've found some gigs tailored just for you. Check them out!";
        }

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_BROWSE)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build();

        notify(context, (int) System.currentTimeMillis(), notification);
    }

    // -------------------------------------------------------------------------
    // 2. Cart Reminder Notification
    //    Data keys: type="cart_reminder", productName, couponCode, discount
    // -------------------------------------------------------------------------
    public static void showCartReminder(Context context, String productName,
                                        String couponCode, String discount) {
        createChannel(context, CHANNEL_CART, "Cart Reminders",
                "Reminders about items you left behind", NotificationManager.IMPORTANCE_HIGH);

        String title = "You left something behind! 🛒";
        String product = (productName != null && !productName.isEmpty()) ? productName : "your item";
        String body;
        if (couponCode != null && !couponCode.isEmpty()) {
            String discountText = (discount != null && !discount.isEmpty()) ? discount : "";
            body = "\"" + product + "\" is waiting for you."
                    + (discountText.isEmpty() ? "" : " Save " + discountText + " with code: ")
                    + (couponCode.isEmpty() ? "" : couponCode + " 🎉");
        } else {
            body = "\"" + product + "\" is still waiting. Complete your order now!";
        }

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_CART)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build();

        notify(context, (int) System.currentTimeMillis(), notification);
    }

    // -------------------------------------------------------------------------
    // 3. Order Update Notification
    //    Data keys: type="order_update", orderId, deliveryDate, status
    // -------------------------------------------------------------------------
    public static void showOrderUpdate(Context context, String orderId,
                                       String deliveryDate, String status) {
        createChannel(context, CHANNEL_ORDER, "Order Updates",
                "Updates on your active orders", NotificationManager.IMPORTANCE_HIGH);

        String title = "Order Update 📦";
        String orderRef = (orderId != null && !orderId.isEmpty()) ? orderId : "your order";
        String statusText = (status != null && !status.isEmpty()) ? status : "In Progress";
        String deliveryText = (deliveryDate != null && !deliveryDate.isEmpty())
                ? deliveryDate : "soon";

        String body = "Order #" + orderRef + " — Status: " + statusText
                + "\nEstimated delivery: " + deliveryText;

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ORDER)
                .setSmallIcon(android.R.drawable.ic_menu_send)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build();

        notify(context, (int) System.currentTimeMillis(), notification);
    }

    // -------------------------------------------------------------------------
    // 4. Generic notification (fallback)
    // -------------------------------------------------------------------------
    public static void showGeneric(Context context, String title, String body) {
        createChannel(context, CHANNEL_GENERAL, "Fiverr Notifications",
                "General notifications", NotificationManager.IMPORTANCE_DEFAULT);

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_GENERAL)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title != null ? title : "Fiverr Clone")
                .setContentText(body != null ? body : "You have a new notification")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(body != null ? body : "You have a new notification"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build();

        notify(context, (int) System.currentTimeMillis(), notification);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------
    private static void createChannel(Context context, String channelId, String name,
                                      String description, int importance) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private static void notify(Context context, int id, Notification notification) {
        NotificationManager manager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(id, notification);
        }
    }
}
