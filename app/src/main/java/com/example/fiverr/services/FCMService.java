package com.example.fiverr.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.fiverr.utils.NotificationHelper;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class FCMService extends FirebaseMessagingService {

    private static final String TAG = "FCMService";

    // Notification types sent via FCM data payload
    public static final String TYPE_BROWSING_RECOMMENDATION = "browsing_recommendation";
    public static final String TYPE_CART_REMINDER = "cart_reminder";
    public static final String TYPE_ORDER_UPDATE = "order_update";

    /**
     * Called when a new FCM token is generated.
     * Log it so you can copy from Logcat for testing.
     */
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        // ★ Copy this token from Logcat to use in Firebase Console → Test Message
        Log.d(TAG, "═══════════════════════════════════════════════");
        Log.d(TAG, "FCM TOKEN (copy for testing):  " + token);
        Log.d(TAG, "═══════════════════════════════════════════════");
    }

    /**
     * Called when an FCM message arrives.
     *
     * How to test from Firebase Console:
     *  1. Go to Firebase Console → Engage → Messaging → New Campaign → Firebase Notification
     *  2. Fill title/body, then tap "Additional Options"
     *  3. Under "Custom data", add key-value pairs from one of the test payloads below
     *  4. Under "Test on device", paste the FCM token from Logcat
     *
     * ──────────────────────────────────────────────────────────
     * TEST PAYLOAD 1 — Browsing Recommendation:
     *   type = browsing_recommendation
     *   userName = John
     *   recommendedGigs = Logo Design, Web Development, Content Writing
     *
     * TEST PAYLOAD 2 — Cart Reminder:
     *   type = cart_reminder
     *   productName = Premium Logo Package
     *   couponCode = SAVE20
     *   discount = 20%
     *
     * TEST PAYLOAD 3 — Order Update:
     *   type = order_update
     *   orderId = ORD-2024-1234
     *   deliveryDate = April 10, 2026
     *   status = In Progress
     * ──────────────────────────────────────────────────────────
     */
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, "FCM message received from: " + remoteMessage.getFrom());

        // Always log any data payload
        Map<String, String> data = remoteMessage.getData();
        if (!data.isEmpty()) {
            Log.d(TAG, "FCM data payload: " + data.toString());
        }

        // Get the 'type' field from the data payload to route to correct handler
        String type = data.get("type");

        if (type != null) {
            switch (type) {
                case TYPE_BROWSING_RECOMMENDATION:
                    handleBrowsingRecommendation(data);
                    break;

                case TYPE_CART_REMINDER:
                    handleCartReminder(data);
                    break;

                case TYPE_ORDER_UPDATE:
                    handleOrderUpdate(data);
                    break;

                default:
                    // Unknown type — fall through to show generic notification
                    showFallbackNotification(remoteMessage);
                    break;
            }
        } else {
            // No type key — show whatever title/body is in the notification payload
            showFallbackNotification(remoteMessage);
        }
    }

    // -------------------------------------------------------------------------
    // Handlers for each notification type
    // -------------------------------------------------------------------------

    /**
     * Browsing recommendation: personalized with user's name and recommended gigs.
     * Required data keys: userName, recommendedGigs
     */
    private void handleBrowsingRecommendation(Map<String, String> data) {
        String userName = data.get("userName");
        String recommendedGigs = data.get("recommendedGigs");
        Log.d(TAG, "Showing browsing recommendation for user: " + userName);
        NotificationHelper.showBrowsingRecommendation(this, userName, recommendedGigs);
    }

    /**
     * Cart reminder: product name left in cart + discount coupon.
     * Required data keys: productName, couponCode, discount
     */
    private void handleCartReminder(Map<String, String> data) {
        String productName = data.get("productName");
        String couponCode = data.get("couponCode");
        String discount = data.get("discount");
        Log.d(TAG, "Showing cart reminder for product: " + productName + " coupon: " + couponCode);
        NotificationHelper.showCartReminder(this, productName, couponCode, discount);
    }

    /**
     * Order update: order ID + estimated delivery date + status.
     * Required data keys: orderId, deliveryDate, status
     */
    private void handleOrderUpdate(Map<String, String> data) {
        String orderId = data.get("orderId");
        String deliveryDate = data.get("deliveryDate");
        String status = data.get("status");
        Log.d(TAG, "Showing order update: orderId=" + orderId + " delivery=" + deliveryDate);
        NotificationHelper.showOrderUpdate(this, orderId, deliveryDate, status);
    }

    /**
     * Fallback for messages without a recognized type field.
     * Uses the standard notification payload title/body if present.
     */
    private void showFallbackNotification(RemoteMessage remoteMessage) {
        String title = "Fiverr Clone";
        String body = "You have a new notification";

        if (remoteMessage.getNotification() != null) {
            if (remoteMessage.getNotification().getTitle() != null) {
                title = remoteMessage.getNotification().getTitle();
            }
            if (remoteMessage.getNotification().getBody() != null) {
                body = remoteMessage.getNotification().getBody();
            }
        }

        // Also check data payload for title/body overrides
        Map<String, String> data = remoteMessage.getData();
        if (data.containsKey("title")) title = data.get("title");
        if (data.containsKey("body")) body = data.get("body");

        NotificationHelper.showGeneric(this, title, body);
    }
}
