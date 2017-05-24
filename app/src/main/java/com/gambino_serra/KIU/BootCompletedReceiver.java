package com.gambino_serra.KIU;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * La classe modella l'attivazione del servizio di notifica all'avvio del sistema.
 */
public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, NotificationService.class);
        context.startService(serviceIntent);
    }
}