package com.example.fiverr.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

import com.example.fiverr.db.DatabaseHelper;
import com.example.fiverr.models.User;

public class SMSReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) return;
        if (!"android.provider.Telephony.SMS_RECEIVED".equals(intent.getAction())) return;

        Bundle bundle = intent.getExtras();
        if (bundle == null) return;

        Object[] pdus = (Object[]) bundle.get("pdus");
        if (pdus == null) return;

        String format = bundle.getString("format");

        for (Object pdu : pdus) {
            SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu, format);
            String messageBody = smsMessage.getMessageBody().trim().toLowerCase();

            processCommand(context, messageBody);
        }
    }

    private void processCommand(Context context, String message) {
        DatabaseHelper db = DatabaseHelper.getInstance(context);

        if (message.startsWith("activate ")) {
            String idStr = message.substring("activate ".length()).trim();
            try {
                int userId = Integer.parseInt(idStr);
                User user = db.getUserById(userId);
                if (user != null) {
                    db.updateUserStatus(userId, "active");
                    Toast.makeText(context, "User " + userId + " activated via SMS", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, "SMS Error: User ID " + idStr + " not found", Toast.LENGTH_LONG).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(context, "SMS Error: Invalid user ID", Toast.LENGTH_SHORT).show();
            }
        } else if (message.startsWith("deactivate ")) {
            String idStr = message.substring("deactivate ".length()).trim();
            try {
                int userId = Integer.parseInt(idStr);
                User user = db.getUserById(userId);
                if (user != null) {
                    db.updateUserStatus(userId, "dormant");
                    Toast.makeText(context, "User " + userId + " deactivated via SMS", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(context, "SMS Error: User ID " + idStr + " not found", Toast.LENGTH_LONG).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(context, "SMS Error: Invalid user ID", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
