package com.gambino_serra.KIU;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.kosalgeek.android.json.JsonConverter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Il Service è delegato alla gestione delle notifiche in background dell'applicazione.
 */
public class NotificationService extends IntentService {

    final private static String MY_PREFERENCES = "kiuPreferences";
    final private static String IDUTENTE = "IDutente";
    final private static String TIPO_UTENTE = "tipoUtente";

    int notificheHelper = 0;
    int notificheKiuer = 0;

    public NotificationService() {
        super("NotificationService");
        }

    @Override
    protected void onHandleIntent(Intent i) {

        Log.d("NotificationService", "Service check avviato");

        final SharedPreferences prefs = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        final Intent notificationIntent = new Intent(getApplicationContext(), Login.class);
        final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        final Notification notification;

        //Notifica dell'applicazione in primo piano
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notification = new Notification.Builder(getApplicationContext())
                    .setOngoing(true).setContentText(getResources().getString(R.string.waiting_message))
                    .setSmallIcon(R.drawable.ic_letter_k)
                    .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher))
                    .setPriority(Notification.PRIORITY_MAX)
                    .setContentIntent(pendingIntent).build();
            notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;
            notification.flags |= Notification.FLAG_NO_CLEAR;
            notification.flags |= Notification.FLAG_ONGOING_EVENT;
            }
        else {
            notification = new Notification.Builder(getApplicationContext())
                    .setOngoing(true).setContentText(getResources().getString(R.string.waiting_message))
                    .setSmallIcon(R.drawable.ic_notification)
                    .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher))
                    .setPriority(Notification.PRIORITY_MAX)
                    .setContentIntent(pendingIntent).build();
            notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;
            notification.flags |= Notification.FLAG_NO_CLEAR;
            notification.flags |= Notification.FLAG_ONGOING_EVENT;
            }

        // Permette di mantenere il Service attivo evitando che venga rimosso dal sistema bloccando la notifica in primo piano
        startForeground(99999, notification);

        //Usato per il log del numero richieste
        int numeroRichiestaDB = 0;

        while (true) {

            //Se l'utente è loggato come Helper gestisce le notifiche relative all'helper
            if (prefs.getString(TIPO_UTENTE, "").equals("H")) {
                //Richiesta dati dal database di altervista per verificare se sono presenti nuove richieste di coda da notificare
                String url = "http://www.davideantonio2.altervista.org/helper_check_richieste.php";
                StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        //Se la risposta è "null" --> nessuna notifica, altrimenti verifica notifiche
                        if (response.equals("null\n")) {
                            notificheHelper = 0;
                            }
                        else {
                            ArrayList<Json_Richiesta> productList = new JsonConverter<Json_Richiesta>().toArrayList(response, Json_Richiesta.class);
                            int contaNotifiche = 0;
                            //Verifica il numero di notifiche da visualizzare
                            for (Json_Richiesta object : productList) {
                                contaNotifiche++;
                                }
                            //Se il numero di notifiche da visualizzare > numero notifiche già visualizzate
                            if (contaNotifiche > notificheHelper) {
                                //Aggiorna numero notifiche visualizzate
                                notificheHelper = contaNotifiche;
                                //Visualizza la notifica se sono presenti notifiche da visualizzare
                                if (contaNotifiche != 0) {
                                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext())
                                            //.setSmallIcon(R.drawable.ic_notifications)
                                            .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                                            .setContentTitle(getResources().getString(R.string.kiu))
                                            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                                            .setContentText(getResources().getQuantityString(R.plurals.incoming_queue_request, contaNotifiche, contaNotifiche).toString());
                                    Intent resultIntent = new Intent(getApplicationContext(), Helper_Notification.class);
                                    PendingIntent resultPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                                    mBuilder.setContentIntent(resultPendingIntent);
                                    int mNotificationId = 001;
                                    NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                    mNotifyMgr.notify(mNotificationId, mBuilder.build());
                                }
                            }
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) { }
                })
                {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        params.put("IDutente", prefs.getString(IDUTENTE, "").toString());
                        return params;
                        }
                };
                Volley.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
            } //fine gestione notifiche Helper

            //Se l'utente è loggato come Kiuer gestisce le notifiche relative al Kiuer
            else if (prefs.getString(TIPO_UTENTE, "").equals("K")) {

                //Richiesta dati dal database di altervista per verificare se sono presenti nuove richieste di coda accettate/rifiutate da notificare
                String url = "http://www.davideantonio2.altervista.org/kiuer_check_richieste.php";
                StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        //Se la risposta è "null" --> nessuna notifica, altrimenti verifica notifiche
                        if (response.equals("null\n")) {
                            NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                            mNotifyMgr.cancel(001);
                            mNotifyMgr.cancel(002);
                            notificheKiuer = 0;
                        }
                        else {
                            ArrayList<Json_Richiesta> productList = new JsonConverter<Json_Richiesta>().toArrayList(response, Json_Richiesta.class);
                            int contaNotifiche = 0;
                            int contaAccettata = 0;
                            int contaRifiutata = 0;
                            for (Json_Richiesta object : productList) {
                                contaNotifiche++;
                                //Verifica richieste di coda accettate e rifiutate
                                if (object.stato_richiesta.toString().equals("1")) {
                                    contaAccettata++;
                                    }
                                else if (object.stato_richiesta.toString().equals("2")) {
                                    contaRifiutata++;
                                    }
                                }
                            //Se il numero di notifiche da visualizzare > numero notifiche già visualizzate
                            if (contaNotifiche > notificheKiuer) {
                                notificheKiuer = contaNotifiche;

                                //Visualizza la notifica di richiesta accettate se sono presenti notifiche da visualizzare
                                if ((contaNotifiche != 0) && (contaAccettata != 0)) {
                                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext())
                                            .setSmallIcon(R.drawable.ic_notifications)
                                            .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                                            .setContentTitle(getResources().getString(R.string.kiu))
                                            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                                            .setContentText(getResources().getQuantityString(R.plurals.request_accepted_notification, contaAccettata, contaAccettata).toString());
                                    Intent resultIntent = new Intent(getApplicationContext(), Kiuer_Notification.class);
                                    PendingIntent resultPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                                    mBuilder.setContentIntent(resultPendingIntent);
                                    int mNotificationId = 001;
                                    NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                    mNotifyMgr.notify(mNotificationId, mBuilder.build());
                                    }

                                //Visualizza la notifica di richieste rifiutate se sono presenti notifiche da visualizzare
                                if ((contaNotifiche != 0) && (contaRifiutata != 0)) {
                                    NotificationCompat.Builder mBuilder2 = new NotificationCompat.Builder(getApplicationContext())
                                            //.setSmallIcon(R.drawable.ic_notifications)
                                            .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                                            .setContentTitle(getResources().getString(R.string.kiu))
                                            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                                            .setContentText(getResources().getQuantityString(R.plurals.request_rejected_notification, contaRifiutata, contaRifiutata).toString());
                                    Intent resultIntent2 = new Intent(getApplicationContext(), Kiuer_Notification.class);
                                    PendingIntent resultPendingIntent2 = PendingIntent.getActivity(getApplicationContext(), 0, resultIntent2, PendingIntent.FLAG_UPDATE_CURRENT);
                                    mBuilder2.setContentIntent(resultPendingIntent2);
                                    int mNotificationId = 002;
                                    NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                    mNotifyMgr.notify(mNotificationId, mBuilder2.build());
                                    }
                            }
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) { }
                })
                {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        params.put("IDutente", prefs.getString(IDUTENTE, "").toString());
                        return params;
                        }
                };
                Volley.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);

            }//fine gestione notifiche Kiuer
            Log.d("PROVA SERVICE", "Richiesta database n°." + numeroRichiestaDB++);
            try { Thread.sleep(10000); }
            catch (InterruptedException e) { }
        }
    }

    @Override
    public void onDestroy() {
        Log.i("PROVA SERVICE", "Distruzione Service");
        }
}
