package com.gambino_serra.KIU;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * La classe modella la Dialog relativa ai dettagli di una specifica richiesta di coda (lato Kiuer).
 */
public class Kiuer_ShowHelperDetails extends DialogFragment {

    TextView title;
    Context context;

    public Kiuer_ShowHelperDetails() {}

    /**
     * onCreate della Dialog e Set dello stato dei bottoni nella UI(Button).
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final Bundle bundle = getArguments();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        context = getActivity();
        title = new TextView(context);
        title.setText(R.string.detail_helper_title);
        title.setGravity(Gravity.CENTER);
        title.setTextSize(25F);
        builder.setCustomTitle(title);
        builder.setView(inflater.inflate(R.layout.fragment_helper_details, null ))

                .setPositiveButton(R.string.chiudi_coda, new DialogInterface.OnClickListener() {
                    @TargetApi(Build.VERSION_CODES.M)
                    public void onClick(DialogInterface dialog, int id) {

                        //Se la coda è terminata avvia la Dialog relativa alla chiusura della coda altrimenti avvisa l'utente che non è possibile chiuderla
                        if (bundle.get("stato_coda").toString().equals(getResources().getString(R.string.queue_terminated))) {

                            DialogFragment newFragment = new Kiuer_CloseQueue();
                            newFragment.setArguments(bundle);
                            newFragment.show(getFragmentManager(), "Kiuer_CloseQueue");
                            dialog.dismiss(); // dismette positivo o neutrale
                        }
                        else {
                            Toast.makeText(getActivity().getApplicationContext(), R.string.queue_not_finished, Toast.LENGTH_LONG).show();
                            }
                    }
                })
                .setNeutralButton(R.string.goback, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel(); // dismette con rifiuto
                    }
                });
        return builder.create();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        Button contatta = (Button) this.getDialog().findViewById(R.id.contatta_helper);
        nome.setText(bundle.get("nome").toString());
        text.setText(bundle.get("text").toString());

        contatta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String telefono = "0987654321";
                //Bundle bundle = getArguments();
                //telefono = bundle.get("telefono").toString();
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + telefono));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
    }
}