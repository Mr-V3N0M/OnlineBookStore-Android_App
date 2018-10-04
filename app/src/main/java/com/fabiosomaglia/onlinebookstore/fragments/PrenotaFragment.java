package com.fabiosomaglia.onlinebookstore.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.fabiosomaglia.onlinebookstore.R;
import com.fabiosomaglia.onlinebookstore.UserSessionManager;
import com.fabiosomaglia.onlinebookstore.activity.LoginActivity;
import com.fabiosomaglia.onlinebookstore.model.Book;
import com.google.gson.Gson;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;


public class PrenotaFragment extends Fragment {

    JSONObject json = new JSONObject();
    Gson gson = new Gson();
    UserSessionManager session = LoginActivity.getSessionLogin();
    String urlLogin = LoginActivity.urlLogin;
    View view = null;
    ListView listViewBooks;
    String idLibro;
    Fragment fragment;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_prenota, container, false);

        listViewBooks = view.findViewById(R.id.lv_prenota);

        try {
            json.put("action", "prenota");
            new Prenota().execute(urlLogin, json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return view;
    }

    private class Prenota extends AsyncTask<String, Void, String> {

        String data = "";
        HttpURLConnection httpURLConnection = null;

        @Override
        protected String doInBackground(String... params) {
            Log.i("TAG", params[0]);
            Log.i("TAG", params[1]);

            try {
                httpURLConnection = (HttpURLConnection) new URL(urlLogin).openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setConnectTimeout(20000);
                httpURLConnection.setDoOutput(true);

                // Here we send the json
                DataOutputStream wr = new DataOutputStream(httpURLConnection.getOutputStream());
                wr.writeBytes(json.toString());
                wr.flush();
                wr.close();

                // Here we receive the json
                InputStream in = httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(in);
                int inputStreamData = inputStreamReader.read();

                while (inputStreamData != -1) {
                    char current = (char) inputStreamData;
                    inputStreamData = inputStreamReader.read();
                    data += current;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return data;
        }


        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);
            Log.i("TAG", data);
            if(data.equals("[]")) {
                view.findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                listViewBooks.setEmptyView(view.findViewById(R.id.empty));
            } else if(data.equals("")) {
                view.findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                AlertDialog.Builder prenotaDialog = new AlertDialog.Builder(getActivity());
                prenotaDialog.setMessage("Connessione con il server non riuscita");
                prenotaDialog.setCancelable(false);
                prenotaDialog.setPositiveButton("Riprova", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        FragmentTransaction t = getActivity().getSupportFragmentManager().beginTransaction();
                        t.setReorderingAllowed(false);
                        t.detach(PrenotaFragment.this).attach(PrenotaFragment.this).commitAllowingStateLoss();
                    }
                });

                AlertDialog alert = prenotaDialog.create();
                alert.setTitle("Errore di connessione");
                alert.show();
            }
            if(!data.equals("")) {
                view.findViewById(R.id.loadingPanel).setVisibility(View.GONE);

                // Mette tutti i libri nella classe Book
                final Book[] arrayBook = gson.fromJson(data, Book[].class);
                listViewBooks.setAdapter(new BookAdapterPrenota(getActivity(), arrayBook));

                // Prendo le informazioni dell'utente che vuole prenotare il libro
                HashMap<String, String> user = session.getUserDetails();
                final String username = user.get(UserSessionManager.KEY_USERNAME);


                // Quando un libro viene cliccato appare un AlertDialog per confermare la prenotazione
                listViewBooks.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {

                        TextView textView = view.findViewById(R.id.tv_nomeLibro);
                        String text = textView.getText().toString();
                        // Creazione dell'AlertDialog
                        AlertDialog.Builder prenotaDialog = new AlertDialog.Builder(getActivity());
                        prenotaDialog.setMessage("Confermi di voler prenotare " + text + "?");
                        prenotaDialog.setCancelable(false);
                        // Si attiva quando clicco "Prenota"
                        prenotaDialog.setPositiveButton("Prenota", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                // Inserisco l'id del libro dalla TextView
                                TextView tvIdLibro = view.findViewById(R.id.tv_idLibro);
                                idLibro = tvIdLibro.getText().toString();

                                // Faccio una richiesta alla servlet per prenotare l'id del libro
                                // Sono necessari l'id e il nome del libro
                                try {
                                    json.put("action", "controlloSePrenotabile");
                                    json.put("cspID", idLibro);
                                    json.put("cspUsername", username);

                                    new ControlloSePrenotabile().execute(urlLogin, json.toString());

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        })
                                // Si attiva quando clicco "Cancella"
                                .setNegativeButton("Cancella", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });
                        AlertDialog alert = prenotaDialog.create();
                        alert.setTitle("Conferma prenotazione");
                        alert.show();

                    }
                });
            }
        }
    }


    private class ControlloSePrenotabile extends AsyncTask<String, Void, String> {

        String rispostaJson = "";
        HttpURLConnection httpURLConnection = null;

        @Override
        protected String doInBackground(String... strings) {
            try {
                httpURLConnection = (HttpURLConnection) new URL(urlLogin).openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);

                // Here we send the json
                DataOutputStream wr = new DataOutputStream(httpURLConnection.getOutputStream());
                wr.writeBytes(json.toString());
                wr.flush();
                wr.close();

                // Here we receive the json
                InputStream in = httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(in);
                int inputStreamData = inputStreamReader.read();

                // Controllo cosa mi ha ritornato il json
                while (inputStreamData != -1) {
                    char current = (char) inputStreamData;
                    inputStreamData = inputStreamReader.read();
                    rispostaJson += current;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            fragment = new RiepilogoPrenotazioneFragment();

            if(rispostaJson.contains("true")) { // Se il libro è prenotabile
                // Aggiungo l'animazione tra fragments quando voglio vedere il riassunto della prenotazione
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                // Creazione dell'animazione di transizione
                ft.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);
                ft.replace(R.id.content_frame, fragment, "fragment");
                // Avvio l'animazione
                ft.commit();
            } else if (rispostaJson.contains("false")) { // Se non si è riusciti a prenotarlo
                Snackbar.make(getActivity().findViewById(android.R.id.content), "Abbiamo riscontrato un problema. Riprova più tardi", Snackbar.LENGTH_LONG).show();
                return "aggiorna";
            }
            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s.equals("aggiorna")) { // Se è stato riscontarato un problema nella prenotazione, viene aggiornata la lista dei libri disponibili
                try {
                    json.put("action", "prenota");
                    new Prenota().execute(urlLogin, json.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Prenota un libro");
    }


    private class BookAdapterPrenota extends BaseAdapter {

        private Context mContext;
        Book[] arrayBook;

        public BookAdapterPrenota(Context context, Book[] array) {
            mContext = context;
            arrayBook = array;
        }

        @Override
        public int getCount() {
            return arrayBook.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            View rowMain = layoutInflater.inflate(R.layout.list_view_design, parent, false);

            TextView nameTextView = rowMain.findViewById(R.id.tv_nomeLibro);
            nameTextView.setText(arrayBook[position].getTitle());
            TextView authorTextView = rowMain.findViewById(R.id.tv_autoreLibro);
            authorTextView.setText(arrayBook[position].getAuthor() + "  -");
            TextView publisherTextView = rowMain.findViewById(R.id.tv_editoreLibro);
            publisherTextView.setText(arrayBook[position].getPublisher() + "  -");
            TextView idTextView = rowMain.findViewById(R.id.tv_idLibro);
            idTextView.setText(Integer.toString(arrayBook[position].getBookId()));

            return rowMain;
        }
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

}