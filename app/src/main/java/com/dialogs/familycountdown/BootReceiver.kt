package com.dialogs.familycountdown

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Kiosk: bring the board back after a power cycle. On Android 10+ an activity
 * may only be started from here if the user has granted "Display over other
 * apps" (SYSTEM_ALERT_WINDOW); the editor's Kiosk row surfaces that switch.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != "android.intent.action.QUICKBOOT_POWERON") return
        val launch = Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        runCatching { context.startActivity(launch) }
    }
}
