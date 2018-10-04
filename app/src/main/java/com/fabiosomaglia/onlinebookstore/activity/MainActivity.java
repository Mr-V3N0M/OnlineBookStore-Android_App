package com.fabiosomaglia.onlinebookstore.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.fabiosomaglia.onlinebookstore.R;
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

public class MainActivity extends AppCompatActivity {
    JSONObject json = new JSONObject();
    Gson gson = new Gson();
    final String TAG = this.getClass().getName();
    String urlLogin = LoginActivity.urlLogin;
    ListView listViewBooks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Creo un link sul floatingActionBtn
        FloatingActionButton login = findViewById(R.id.floatingActionButton);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goToLogin = new Intent(getApplicationContext(), LoginActivity.class);
                goToLogin.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(goToLogin);
            }
        });
        listViewBooks = findViewById(R.id.lv_mainActivity);

        try {
            json.put("action", "dashboard");
            new MainListView().execute(urlLogin, json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private class MainListView extends AsyncTask<String, Void, String> {
        String data = "";
        HttpURLConnection httpURLConnection = null;
        Book[] arrayBook;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            Log.i("TAG", params[0]);
            Log.i("TAG", params[1]);

            try {
                httpURLConnection = (HttpURLConnection) new URL(urlLogin).openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setConnectTimeout(20000);
                httpURLConnection.setDoOutput(true);

                // Invio il json alla Servlet
                DataOutputStream wr = new DataOutputStream(httpURLConnection.getOutputStream());
                wr.writeBytes(json.toString());
                wr.flush();
                wr.close();

                // Ricevo il json dalla Servlet
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

            // Scrivo gli elementi del json nell'array
            arrayBook = gson.fromJson(data, Book[].class);

            return data;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (s.equals("[]")) {
                findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                listViewBooks.setEmptyView(findViewById(R.id.empty));
            } else if (s.equals("")) {
                findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                AlertDialog.Builder prenotaDialog = new AlertDialog.Builder(MainActivity.this);
                prenotaDialog.setMessage("Connessione con il server non riuscita");
                prenotaDialog.setCancelable(false);
                prenotaDialog.setPositiveButton("Riprova", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        finish();
                        startActivity(getIntent());
                    }
                });

                AlertDialog alert = prenotaDialog.create();
                alert.setTitle("Errore di connessione");
                alert.show();
            }

            if (!s.equals("")) {
                findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                listViewBooks.setAdapter(new BookAdapter(MainActivity.this, arrayBook));
            }
        }
    }


    private class BookAdapter extends BaseAdapter {

        private Context mContext;
        Book[] arrayBook;

        public BookAdapter(Context context, Book[] array) {
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


    boolean twice; // Ho creato questa variabile per controllare se il tasto indietro viene cliccato 2 volte di fila

    @Override
    public void onBackPressed() {
        // Se clicco due volte il tasto indietro di android l'app si chiude
        Log.d(TAG, "click");

        if (twice == true) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // chiude tutto cio che era aperto nell'app
            startActivity(intent);
            finish();
            System.exit(0); // chiude l'app senza far uscire errori
        }

        Log.d(TAG, "twice: " + twice);
        Toast.makeText(MainActivity.this, "Premi di nuovo per chiudere l'app", Toast.LENGTH_SHORT).show();
        // Ora se l'utente clicca nuovamente il tasto indietro chiudo l'app (2000 vuol dire 2 secondi)
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                twice = false;
                Log.d(TAG, "twice: " + twice);
            }
        }, 2000);
        twice = true;
    }
}

