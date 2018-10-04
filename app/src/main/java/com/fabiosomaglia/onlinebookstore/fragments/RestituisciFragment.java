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
import com.fabiosomaglia.onlinebookstore.model.Booking;
import com.google.gson.Gson;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


public class RestituisciFragment extends Fragment {

    UserSessionManager session = LoginActivity.getSessionLogin();
    JSONObject json = new JSONObject();
    Gson gson = new Gson();
    String urlLogin = LoginActivity.urlLogin;
    View view = null;
    ListView listViewBooks;
    String idPrenotazione;
    ArrayList<Booking> listBook;
    BaseAdapter adapter;

    // In modo da poter cancellare l'item corretto della ListView
    int positionItem;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_restituisci, container, false);

        listViewBooks = view.findViewById(R.id.lv_restituisci);

        // Prendo le informazioni dell'utente che vuole restituire il libro
        HashMap<String, String> user = session.getUserDetails();
        final String username = user.get(UserSessionManager.KEY_USERNAME);

        try {
            json.put("action", "restituisci");
            json.put("username", username);
            new Restituisci().execute(urlLogin, json.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return view;
    }


    private class Restituisci extends AsyncTask<String, Void, String> {

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
                        t.detach(RestituisciFragment.this).attach(RestituisciFragment.this).commitAllowingStateLoss();
                    }
                });

                AlertDialog alert = prenotaDialog.create();
                alert.setTitle("Errore di connessione");
                alert.show();
            }
            if(!data.equals("")) {
                view.findViewById(R.id.loadingPanel).setVisibility(View.GONE);

                // Mette tutti i libri nella classe Booking
                final Booking[] arrayBook = gson.fromJson(data, Booking[].class);
                listBook = new ArrayList<>(Arrays.asList(arrayBook));
                //listViewBooks.setAdapter(new BookAdapterRestituisci(getActivity(), arrayBook));
                adapter = new BookAdapterRestituisci(getActivity(), listBook);
                listViewBooks.setAdapter(adapter);

                // Quando un libro viene cliccato appare un AlertDialog per confermare la restituzione
                listViewBooks.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {

                        positionItem = position;
                        TextView textView = view.findViewById(R.id.tv_nomeLibro);
                        String text = textView.getText().toString();
                        // Creazione dell'AlertDialog
                        AlertDialog.Builder restituisciDialog = new AlertDialog.Builder(getActivity());
                        restituisciDialog.setMessage("Confermi di voler restituire " + text + "?");
                        restituisciDialog.setCancelable(false);
                        // Si attiva quando clicco "Restituisci"
                        restituisciDialog.setPositiveButton("Restituisci", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                // Faccio una richiesta alla servlet per restituire il libro
                                // Serve solamente l'id della prenotazione
                                // Inserisco l'id del libro dalla TextView
                                TextView tv_id_prenotazione = view.findViewById(R.id.tv_id_prestito);
                                idPrenotazione = tv_id_prenotazione.getText().toString();

                                try {
                                    json.put("action", "finePrenotazione");
                                    json.put("id_booking", idPrenotazione);

                                    new ControlloSeRestituibile().execute(urlLogin, json.toString());

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
                        AlertDialog alert = restituisciDialog.create();
                        alert.setTitle("Conferma restituzione");
                        alert.show();

                    }
                });
            }
        }
    }


    private class ControlloSeRestituibile extends AsyncTask<String, Void, String> {

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

            if(rispostaJson.contains("true")) {
                Snackbar.make(getActivity().findViewById(android.R.id.content), "Restituzione avvenuta con successo!", Snackbar.LENGTH_LONG).show();
                return "cancella";
            } else
                Snackbar.make(getActivity().findViewById(android.R.id.content), "Abbiamo riscontrato un problema. Riprova più tardi", Snackbar.LENGTH_LONG).show();
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            // Cancella la riga della listview
            if(s.equals("cancella")) {
                listBook.remove(positionItem);
                adapter.notifyDataSetChanged();
                if (listBook.isEmpty()) //faccio comparire la scritta che non c'è nessun libro da restituire quando restituisco l'ultimo libro in mio possesso
                    listViewBooks.setEmptyView(view.findViewById(R.id.empty));
            }

        }
    }


    private class BookAdapterRestituisci extends BaseAdapter {

        private Context mContext;
        ArrayList<Booking> arrayBook;

        public BookAdapterRestituisci(Context context, ArrayList<Booking> array) {

            mContext = context;
            arrayBook = array;

        }

        @Override
        public int getCount() {
            return arrayBook.size();
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
            View rowMain = layoutInflater.inflate(R.layout.list_view_design_restituzione, parent, false);

            TextView nameTextView = rowMain.findViewById(R.id.tv_nomeLibro);
            nameTextView.setText(arrayBook.get(position).getBook().getTitle());
            TextView authorTextView = rowMain.findViewById(R.id.tv_autoreLibro);
            authorTextView.setText(arrayBook.get(position).getBook().getAuthor() + "  -");
            TextView publisherTextView = rowMain.findViewById(R.id.tv_editoreLibro);
            publisherTextView.setText(arrayBook.get(position).getBook().getPublisher() + "  -");
            TextView idTextView = rowMain.findViewById(R.id.tv_idLibro);
            idTextView.setText(Integer.toString(arrayBook.get(position).getBook().getBookId()));
            TextView idPrestitoTextView = rowMain.findViewById(R.id.tv_id_prestito);
            idPrestitoTextView.setText(Integer.toString(arrayBook.get(position).getBookingId()));
            TextView inizioPrestitoTextView = rowMain.findViewById(R.id.tv_inizio_prestito);
            inizioPrestitoTextView.setText(arrayBook.get(position).getStartBooking());

            return rowMain;
        }
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Restituisci un libro");
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}