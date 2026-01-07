package com.example.wear.presentation

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM_WEAR", "Token: $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("FCM_WEAR", "From: ${message.from}")
        Log.d("FCM_WEAR", "Notification: ${message.notification?.title} - ${message.notification?.body}")
        Log.d("FCM_WEAR", "Data: ${message.data}")
    }
}
