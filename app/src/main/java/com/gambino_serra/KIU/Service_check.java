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
import com.gambino_serra.KIU.R;
//import com.gambino_serra.KIU.chat.ConversationsActivity;
//import com.gambino_serra.KIU.chat.service.ChatService;
//import com.gambino_serra.KIU.chat.service.ConversationServiceListener;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
import com.kosalgeek.android.json.JsonConverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Il Service è delegato alla gestione delle notifiche in background dell'applicazione.
 */
public class Service_check extends IntentService {

    final private static String MY_PREFERENCES = "kiuPreferences";
    final private static String IDUTENTE = "IDutente";
    final private static String TIPO_UTENTE = "tipoUtente";

    int notificheHelper = 0;
    int notificheKiuer = 0;
   // private DatabaseReference mRef;

    public Service_check() {

        super("Service_check");
    }

    @Override
    protected void onHandleIntent(Intent i) {

        Log.d("Service_check", "Service check avviato");

        final SharedPreferences prefs = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        final Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        final Notification notification;

        //Notifica dell'applicazione in primo piano
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notification = new Notification.Builder(getApplicationContext())
                    .setOngoing(true).setContentText(getResources().getString(R.string.waiting_message))
                    .setSmallIcon(R.drawable.ic_letter_k)
                    .setLargeIcon(BitmapFactory.decodeResource(this.getResources(),
                            R.mipmap.ic_launcher))
                    .setPriority(Notification.PRIORITY_MAX)
                    .setContentIntent(pendingIntent).build();
            notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;
            notification.flags |= Notification.FLAG_NO_CLEAR;
            notification.flags |= Notification.FLAG_ONGOING_EVENT;
        } else {
            notification = new Notification.Builder(getApplicationContext())
                    .setOngoing(true).setContentText(getResources().getString(R.string.waiting_message))
                    .setSmallIcon(R.drawable.ic_notification)
                    .setLargeIcon(BitmapFactory.decodeResource(this.getResources(),
                            R.mipmap.ic_launcher))
                    .setPriority(Notification.PRIORITY_MAX)
                    .setContentIntent(pendingIntent).build();
            notification.flags |= Notification.FLAG_FOREGROUND_SERVICE;
            notification.flags |= Notification.FLAG_NO_CLEAR;
            notification.flags |= Notification.FLAG_ONGOING_EVENT;
        }

        // Permette di mantenere il Service attivo evitando che venga rimosso
        // dal sistema bloccando la notifica in primo piano
        startForeground(99999, notification);


        //Acquisizione dati per la gestione delle notifiche inerenti la chat
        //String uid = prefs.getString(LOGGED_USER, "").toString();
        //String mail = prefs.getString(EMAIL, "").toString();
      //  mRef = FirebaseDatabase.getInstance().getReference("/chat/chatstatus/" + uid);
      //  mRef.addChildEventListener(new ConversationServiceListener(this));

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
                        } else {

                            ArrayList<JsonCheckRichiesta> productList = new JsonConverter<JsonCheckRichiesta>().toArrayList(response, JsonCheckRichiesta.class);
                            int contaNotifiche = 0;
                            //Verifica il numero di notifiche da visualizzare
                            for (JsonCheckRichiesta object : productList) {
                                contaNotifiche++;
                            }
                            //Se il numero di notifiche da visualizzare > numero notifiche già visualizzate
                            if (contaNotifiche > notificheHelper) {
                                //Aggiorna numero notifiche visualizzate
                                notificheHelper = contaNotifiche;
                                //Visualizza la notifica se sono presenti notifiche da visualizzare
                                if (contaNotifiche != 0) {
                                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext())
                                            .setSmallIcon(R.drawable.ic_notifications)
                                            .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                                            .setContentTitle(getResources().getString(R.string.kiu))
                                            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                                            .setContentText(getResources().getQuantityString(R.plurals.incoming_queue_request, contaNotifiche, contaNotifiche).toString());
                                    Intent resultIntent = new Intent(getApplicationContext(), NotificationHelper.class);
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
                    public void onErrorResponse(VolleyError error) {
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        params.put("IDutente", prefs.getString(IDUTENTE, "").toString());
                        return params;
                    }
                };
                MySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);

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
                        } else {

                            ArrayList<JsonCheckRichiesta> productList = new JsonConverter<JsonCheckRichiesta>().toArrayList(response, JsonCheckRichiesta.class);
                            int contaNotifiche = 0;
                            int contaAccettata = 0;
                            int contaRifiutata = 0;
                            for (JsonCheckRichiesta object : productList) {
                                contaNotifiche++;
                                //Verifica richieste di coda accettate e rifiutate
                                if (object.stato_richiesta.toString().equals("1")) {
                                    contaAccettata++;
                                } else if (object.stato_richiesta.toString().equals("2")) {
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
                                    Intent resultIntent = new Intent(getApplicationContext(), NotificationKiuer.class);
                                    PendingIntent resultPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                                    mBuilder.setContentIntent(resultPendingIntent);
                                    int mNotificationId = 001;
                                    NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                    mNotifyMgr.notify(mNotificationId, mBuilder.build());
                                }
                                //Visualizza la notifica di richieste rifiutate se sono presenti notifiche da visualizzare
                                if ((contaNotifiche != 0) && (contaRifiutata != 0)) {
                                    NotificationCompat.Builder mBuilder2 = new NotificationCompat.Builder(getApplicationContext())
                                            .setSmallIcon(R.drawable.ic_notifications)
                                            .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                                            .setContentTitle(getResources().getString(R.string.kiu))
                                            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                                            .setContentText(getResources().getQuantityString(R.plurals.request_rejected_notification, contaRifiutata, contaRifiutata).toString());
                                    Intent resultIntent2 = new Intent(getApplicationContext(), NotificationKiuer.class);
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
                    public void onErrorResponse(VolleyError error) {
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        params.put("IDutente", prefs.getString(IDUTENTE, "").toString());
                        return params;
                    }
                };
                MySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);

/*
                //Richiesta dati dal database di altervista per verificare se sono presenti nuove code iniziate da notificare
                String url2 = "http://www.kiu.altervista.org/notifica_coda_iniziata.php";
                StringRequest stringRequest2 = new StringRequest(Request.Method.POST, url2, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (!response.equals("null\n")) {

                            ArrayList<JsonCheckRichiesta> productList = new JsonConverter<JsonCheckRichiesta>().toArrayList(response, JsonCheckRichiesta.class);

                            //Visualizza le notifiche di coda iniziata
                            for (JsonCheckRichiesta object : productList) {

                                NotificationCompat.Builder mBuilder3 = new NotificationCompat.Builder(getApplicationContext())
                                        .setSmallIcon(R.drawable.ic_notifications)
                                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                                        .setContentTitle(getResources().getString(R.string.kiu))
                                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                                        .setContentText(object.nome + " " + getResources().getString(R.string.queue_started));
                                Intent resultIntent3 = new Intent(getApplicationContext(), KiuerHomeActivity.class);
                                PendingIntent resultPendingIntent3 = PendingIntent.getActivity(getApplicationContext(), 0, resultIntent3, PendingIntent.FLAG_UPDATE_CURRENT);
                                mBuilder3.setContentIntent(resultPendingIntent3);
                                int mNotificationId3 = 003;
                                NotificationManager mNotifyMgr3 = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                mNotifyMgr3.notify(mNotificationId3, mBuilder3.build());
                            }
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        params.put("email", prefs.getString(IDUTENTE, "").toString());
                        return params;
                    }
                };
                MySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest2);

                //Richiesta dati dal databse di altervista per verificare se sono presenti nuove code terminate da notificare
                String url3 = "http://www.kiu.altervista.org/notifica_coda_terminata.php";
                StringRequest stringRequest3 = new StringRequest(Request.Method.POST, url3, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (!response.equals("null\n")) {

                            ArrayList<JsonCheckRichiesta> productList = new JsonConverter<JsonCheckRichiesta>().toArrayList(response, JsonCheckRichiesta.class);

                            //Visualizza le notifiche di coda terminata
                            for (JsonCheckRichiesta object : productList) {

                                NotificationCompat.Builder mBuilder4 = new NotificationCompat.Builder(getApplicationContext())
                                        .setSmallIcon(R.drawable.ic_notifications)
                                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                                        .setContentTitle(getResources().getString(R.string.kiu))
                                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                                        .setContentText(object.nome + " " + getResources().getString(R.string.queue_end));
                                Intent resultIntent4 = new Intent(getApplicationContext(), KiuerHomeActivity.class);
                                PendingIntent resultPendingIntent4 = PendingIntent.getActivity(getApplicationContext(), 0, resultIntent4, PendingIntent.FLAG_UPDATE_CURRENT);
                                mBuilder4.setContentIntent(resultPendingIntent4);
                                int mNotificationId4 = 004;
                                NotificationManager mNotifyMgr4 = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                mNotifyMgr4.notify(mNotificationId4, mBuilder4.build());
                            }
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        params.put("email", prefs.getString(IDUTENTE, "").toString());
                        return params;
                    }
                };
                MySingleton.getInstance(getApplicationContext()).addToRequestQueue(stringRequest3);
*/

            }//fine gestione notifiche Kiuer
            Log.d("PROVA SERVICE", "Richiesta database n°." + numeroRichiestaDB++);
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
            }
        }
    }

//    /**
//     *  Il metodo gestisce la notifica quando viene ricevuto un nuovo messaggio e la conversazione
//     *  non è in corso (relativa Activity in primo piano)
//     * @param convname
//     */
//    @Override
//    public void handleNewMessage(String convname) {
//
//        final SharedPreferences prefs = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
//
//        if (!(convname.equals(prefs.getString(CONV_NAME, "")))) {
//
//            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext())
//                    .setSmallIcon(R.drawable.ic_notifications)
//                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
//                    .setContentTitle(getResources().getString(R.string.kiu_chat))
//                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
//                    .setContentText(getResources().getString(R.string.new_messages_received));
//            Intent resultIntent = new Intent(getApplicationContext(), ConversationsActivity.class);
//            PendingIntent resultPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//            mBuilder.setContentIntent(resultPendingIntent);
//            int mNotificationId = 010;
//            NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//            mNotifyMgr.notify(mNotificationId, mBuilder.build());
//        }
//    }

    @Override
    public void onDestroy() {
        Log.i("PROVA SERVICE", "Distruzione Service");
    }

}
