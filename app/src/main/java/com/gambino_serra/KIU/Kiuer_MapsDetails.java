package com.gambino_serra.KIU;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * La classe modella l'interazione per l'inoltro della richiesta di coda.
 */
public class Kiuer_MapsDetails extends DialogFragment {

    public TextView ora_richiesta;
    public EditText casella_descrizione;
    public Bundle bundle;
    public Kiuer_MapsDetails() {}
    private Boolean giusto = false;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final Bundle bundle = getArguments();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();



        do {


            builder.setView(inflater.inflate(R.layout.fragment_kiuer_details_maps, null))

                    .setTitle(getResources().getString(R.string.send_request).toUpperCase() + " " + bundle.get("nome").toString().toUpperCase() + "?")

                    .setPositiveButton(R.string.invia_richiesta, new DialogInterface.OnClickListener() {


                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(DialogInterface dialog, int id) {


                                String orario = new String();
                                orario = ora_richiesta.getText().toString().substring(3, 8) + ":00";


                                //CONTROLLA SE ORARIO E' NEI LIMITI DELLA DISPONIBILITà
                                if (orario.compareTo(bundle.getString("disp_inizio").toString()) >= 0 &&
                                        orario.compareTo(bundle.getString("disp_fine").toString()) < 0) {

                                    giusto = true;
                                    Log.d("Hey", "-" + orario + "- è vivo -" + bundle.getString("disp_inizio").toString() + "-");


                                    if (!bundle.get("ora_richiesta").equals(getResources().getString(R.string.set_request_time))) { //TODO
                                        //Registra nel database di altervista la richiesta inviata dal Kiuer
                                        String url = "http://www.davideantonio2.altervista.org/kiuer_inviaRichiesta.php";

                                        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, ((Kiuer_Maps) getActivity()), ((Kiuer_Maps) getActivity())) {
                                            @Override
                                            protected Map<String, String> getParams() throws AuthFailureError {
                                                Map<String, String> params = new HashMap<>();
                                                params.put("ID_kiuer", bundle.get("ID_kiuer").toString());
                                                params.put("ID_helper", bundle.get("ID_helper").toString());
                                                params.put("orario", bundle.get("ora_richiesta").toString());
                                                params.put("pos_latitudine", bundle.get("pos_latitudine").toString());
                                                params.put("pos_longitudine", bundle.get("pos_longitudine").toString());
                                                params.put("luogo", bundle.get("luogo").toString());
                                                params.put("descrizione", casella_descrizione.getText().toString());
                                                return params;
                                            }
                                        };

                                        Volley.getInstance(getActivity().getApplicationContext()).addToRequestQueue(stringRequest);
                                        dialog.dismiss();
                                    } else {
                                        Toast.makeText(getActivity().getApplicationContext(), R.string.set_request_time, Toast.LENGTH_SHORT).show();
                                        DialogFragment newFragment = new Kiuer_MapsDetails();
                                        newFragment.setArguments(bundle);
                                        newFragment.show(getFragmentManager(), "Kiuer_Maps");
                                    }


                                } else {
                                    Log.d("Hey", "-" + orario + "- è morto -" + bundle.getString("disp_fine").toString() + "-");

                                    Toast.makeText(getContext(), R.string.errore_orario, Toast.LENGTH_LONG).show();
                                    giusto = false;
                                }
                        }
                    })
                    .setNeutralButton(R.string.annulla, new DialogInterface.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });

        return builder.create();

        }while(giusto==false);

    }

    /**
     * Inizializzazione dei campi che visualizzano i dettagli del Helper.
     */
    @Override
    public void onStart() {
        super.onStart();
        TextView testo = (TextView) this.getDialog().findViewById(R.id.msg_richiesta);
        RatingBar rating = (RatingBar) this.getDialog().findViewById(R.id.rating_helper);
        ora_richiesta = (TextView) this.getDialog().findViewById(R.id.ora_richiesta);
        casella_descrizione = (EditText) this.getDialog().findViewById(R.id.casella_descrizione);

        bundle = getArguments();
        rating.setRating(bundle.getFloat("rating") / bundle.getInt("cont_feedback"));
        rating.setIsIndicator(true);
        testo.setText(getResources().getString(R.string.hour_rate) + " " + bundle.get("tariffa_oraria") + "€\n"
                + getResources().getString(R.string.queue_done) + " " + bundle.get("cont_feedback") + "\n"
                + getResources().getString(R.string.start_availability) + " " + bundle.get("disp_inizio").toString().substring(0, 5) + "\n"
                + getResources().getString(R.string.end_availability) + " " + bundle.get("disp_fine").toString().substring(0, 5) );


        casella_descrizione.setText(bundle.get("descrizione").toString());

        GradientDrawable gd = new GradientDrawable();
        gd.setColor(0xBFBFBFBF);
        gd.setSize(200, 90);
        gd.setCornerRadius(10);
        gd.setShape(GradientDrawable.RECTANGLE);
        gd.setStroke(7, Color.rgb(170, 170, 170));
        ora_richiesta.setBackground(gd);

        ora_richiesta.setText("   " + bundle.get("ora_richiesta").toString() + "   ");

        ora_richiesta.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                bundle.putString("descrizione", casella_descrizione.getText().toString());
                getDialog().dismiss();
                ((Kiuer_Maps) getActivity()).showTimePickerRichiesta();
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
    }

}