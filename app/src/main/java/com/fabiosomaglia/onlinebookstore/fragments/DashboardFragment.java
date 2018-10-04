package com.fabiosomaglia.onlinebookstore.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.fabiosomaglia.onlinebookstore.R;
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


public class DashboardFragment extends Fragment {

    JSONObject json = new JSONObject();
    Gson gson = new Gson();
    String urlLogin = LoginActivity.urlLogin;
    View view = null;
    ListView listViewBooks;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        listViewBooks = view.findViewById(R.id.lv_dashboard);

        try {
            json.put("action", "dashboard");
            new Dashboard().execute(urlLogin, json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return view;
    }

    private class Dashboard extends AsyncTask<String, Void, String> {

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
                        t.detach(DashboardFragment.this).attach(DashboardFragment.this).commitAllowingStateLoss();
                    }
                });

                AlertDialog alert = prenotaDialog.create();
                alert.setTitle("Errore di connessione");
                alert.show();
            }
            if(!data.equals("")) {
                view.findViewById(R.id.loadingPanel).setVisibility(View.GONE);

                Book[] arrayBook = gson.fromJson(data, Book[].class);
                listViewBooks.setAdapter(new BookAdapter(getActivity(), arrayBook));
            }

        }
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Dashboard");
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


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

}