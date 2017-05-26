package com.gambino_serra.KIU;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * La classe modella la Dialog relativa alla chiusura della coda (lato Helper).
 */
public class Helper_CloseQueue extends DialogFragment {

    public Helper_CloseQueue() {}

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final Bundle bundle = getArguments();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        builder.setTitle(R.string.close_queue);
        builder.setView(inflater.inflate(R.layout.fragment_helper_close_queue, null))

                //Memorizza sul DB l'avvenuta chiusura della coda da parte dell'Helper
                .setPositiveButton(R.string.chiudi_coda, new DialogInterface.OnClickListener() {
                    @TargetApi(Build.VERSION_CODES.M)
                    public void onClick(DialogInterface dialog, int id) {

                        final RatingBar rating = (RatingBar) getDialog().findViewById(R.id.rating_helper);

                        String url = "http://www.davideantonio2.altervista.org/chiudi_coda_helper.php";
                        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) { }
                        },
                                ((Helper_Home) getActivity())) {
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                Map<String, String> params = new HashMap<>();
                                params.put("rating", String.valueOf(Float.toString(rating.getRating())));
                                params.put("ID_richiesta", bundle.get("ID").toString());
                                params.put("ID_helper", bundle.get("ID_helper").toString());
                                params.put("ID_kiuer", bundle.get("ID_kiuer").toString());
                                return params;
                            }
                        };

                        Volley.getInstance(getActivity().getApplicationContext()).addToRequestQueue(stringRequest);

                        Toast.makeText(getActivity().getApplicationContext(), R.string.queue_closed, Toast.LENGTH_SHORT).show();

                        dialog.dismiss(); // dismette positivo o neutrale

                        ((Helper_Home) getActivity()).onResume(); //aggiornamento della lista in Helper_Home
                    }
                })
                .setNeutralButton(R.string.goback, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel(); // dismette con rifiuto
                        }
                });
        return builder.create();
    }

    /**
     * Il metodo inizializza la Dialog con i dati ricevuti dal Bundle
     */
    @Override
    public void onStart() {
        super.onStart();
        Bundle bundle = getArguments();
        TextView text = (TextView) this.getDialog().findViewById(R.id.dettagli2);
        TextView nome = (TextView) this.getDialog().findViewById(R.id.text_nome_1h);
        nome.setText(bundle.get("nome").toString());
        text.setText( getResources().getString(R.string.hour_start_queue) + "  " + bundle.get("orario_inizio").toString().substring(11, 16) + "\n\n"
                    + getResources().getString(R.string.hour_end_queue) + "  " + bundle.get("orario_fine").toString().substring(11, 16) + "\n\n"
                    + getResources().getString(R.string.time_queue) + "  " + calcoloOre(bundle.get("orario_inizio").toString(), bundle.get("orario_fine").toString()) + "\n\n"
                    + getResources().getString(R.string.payment) + "  " + calcoloCompenso(bundle.get("orario_inizio").toString(), bundle.get("orario_fine").toString(), bundle.getInt("tariffa_oraria")) + "€\n");
    }

    /**
     * Il metodo calcola la durata della coda (ore:minuti).
     */
    private String calcoloOre(String orarioInizio, String orarioFine) {
        String ore = "";
        String minuti = "";
        String tempo = "00:00";
        try {

            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            fmt.setLenient(false);

            // Parse delle due stringhe.
            Date d1 = fmt.parse(orarioInizio);
            Date d2 = fmt.parse(orarioFine);

            // Calcola la differenza in millisecondi.
            long millisDiff = d2.getTime() - d1.getTime();

            // Calcola giorni/ore/minuti/secondi.
            int seconds = (int) (millisDiff / 1000 % 60);
            int minutes = (int) (millisDiff / 60000 % 60);
            int hours = (int) (millisDiff / 3600000 % 24);
            int days = (int) (millisDiff / 86400000);

            if (hours >= 10) ore = String.valueOf(hours);
            else ore = "0" + String.valueOf(hours);

            if (minutes >= 10) minuti = String.valueOf(minutes);
            else minuti = "0" + String.valueOf(minutes);

            tempo = ore + ":" + minuti;
        }
        catch (Exception e) { System.err.println(e); }
        return tempo;
    }

    /**
     * Il metodo calcola il compenso in base alla durata della coda e la tariffa.
     */
    private String calcoloCompenso(String orarioInizio, String orarioFine, int tariffa) {

        double compenso = 0.0;
        DecimalFormat df2 = new DecimalFormat("##.##");
        try {
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            fmt.setLenient(false);

            // Parse delle due stringhe.
            Date d1 = fmt.parse(orarioInizio);
            Date d2 = fmt.parse(orarioFine);

            // Calcola la differenza in millisecondi.
            long millisDiff = d2.getTime() - d1.getTime();

            int minutes = (int) (millisDiff / 60000);

            compenso = (Double.valueOf(tariffa) / 60) * minutes;

        }
        catch (Exception e) { System.err.println(e); }
        return String.valueOf(df2.format(compenso));
    }
}