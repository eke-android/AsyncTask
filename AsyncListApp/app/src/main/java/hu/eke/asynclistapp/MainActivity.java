package hu.eke.asynclistapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "AsyncListApp";
    private final static String JSON_URL = "https://adobe.github.io/Spry/data/json/array-02.js";

    private Button loadButton;
    private ProgressBar progressBar;
    private ListView listView;
    private ColorItemAdapter adapter;
    private ArrayList<ColorItem> listItems = new ArrayList<>();

    private AsyncTask<String, Integer, String> task;

    private Gson gson = new GsonBuilder().setLenient().create();
    private Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://adobe.github.io/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build();
    private JsonService service = retrofit.create(JsonService.class);

    public interface JsonService {
        @GET("Spry/data/json/array-02.js")
        Call<List<ColorItem>> getColors();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadButton = (Button) findViewById(R.id.button);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        listView = (ListView) findViewById(R.id.listView);

        adapter = new ColorItemAdapter(this, listItems);
        listView.setAdapter(adapter);
    }

    private Call<List<ColorItem>> call;
    public void onLoadClick(View view) {
        // TODO Start download in background
        //task = new DownloadJsonTask();
        //task.execute("teszt");

        call = service.getColors();
        call.enqueue(new Callback<List<ColorItem>>() {
            @Override
            public void onResponse(Call<List<ColorItem>> call, Response<List<ColorItem>> response) {
                List<ColorItem> colorList = response.body();
                Log.v("Download ended", colorList.toString());
                listItems.addAll(colorList);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<List<ColorItem>> call, Throwable t) {

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        // TODO Cancel download if it is running
        if(call != null) {
            call.cancel();
        }
    }

    // Gradle-ben lévő useLibrary nélkül már nem is elérhető 23-as target mellett
    // infó: https://developer.android.com/about/versions/marshmallow/android-6.0-changes.html#behavior-apache-http-client
    private String downloadJsonLegacy() {
        String jsonResponse = null;
        HttpClient httpclient = new DefaultHttpClient();
        try {
            HttpGet httpGet = new HttpGet(JSON_URL);
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            jsonResponse = httpclient.execute(httpGet,
                    responseHandler);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            httpclient.getConnectionManager().shutdown();
        }

        return jsonResponse;
    }

    // Ez az új preferált módja a netes kommunikációnak
    private String downloadJson() {
        String jsonResponse = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(JSON_URL);
            urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            // Read the whole InputStream
            Scanner s = new Scanner(in).useDelimiter("\\A");
            jsonResponse = s.hasNext() ? s.next() : null;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        return jsonResponse;
    }

    private ArrayList<ColorItem> parseJson(String jsonString) {
        ArrayList<ColorItem> items = null;
        try {
            Gson gson = new Gson();
            Type collectionType = new TypeToken<ArrayList<ColorItem>>() {
            }.getType();
            items = gson.fromJson(jsonString, collectionType);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return items;
    }

    private class DownloadJsonTask extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        protected String doInBackground(String... urls) {
            Log.v("Download started", urls[0]);

            for (int i = 0; i < 100; i += 20) {
                publishProgress((int) i);
                // Escape early if cancel() is called
                if (isCancelled()) break;

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return downloadJsonLegacy();
        }

        protected void onProgressUpdate(Integer... progress) {
            progressBar.setProgress(progress[0]);
        }

        protected void onPostExecute(String result) {
            progressBar.setVisibility(View.INVISIBLE);

            if (result != null) {
                Log.v("Download ended", result);
                ArrayList<ColorItem> colorList = parseJson(result);
                listItems.addAll(colorList);
                adapter.notifyDataSetChanged();
            }
        }


        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }
}
