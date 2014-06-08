package edu.example.myapplication5.app;

import android.app.Activity;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;

//////////////////////////////////////////
//
// Webアクセスのサンプルコード
//　最寄り駅検索
//
//////////////////////////////////////////
public class MainActivity extends Activity {

    private EditText latText  = null;
    private EditText lonText = null;
    private TextView resText = null;
    public String searchWord = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        latText = (EditText) findViewById(R.id.editText1);
        lonText = (EditText) findViewById(R.id.editText2);
        resText = (TextView) findViewById(R.id.textView3);
        Button button = (Button) findViewById(R.id.button1);
        button.setOnClickListener(mButton1Listener);
    }

    private View.OnClickListener mButton1Listener = new View.OnClickListener() {
        public void onClick(View v) {
            String latitude = latText.getText().toString();
            String longitude= lonText.getText().toString();
            String requestURL = "http://map.simpleapi.net/stationapi?y="
                    + latitude + "&x=" + longitude +"&output=json";
//			resText.setText(requestURL);
            Log.d("そのままのURL１",requestURL);
            Toast.makeText(getApplicationContext(), requestURL,
                    Toast.LENGTH_LONG).show();
            Task task = new Task();
            task.execute(requestURL);
        }
    };

    protected class Task extends AsyncTask<String, String, String>
    {
        @Override
        protected String doInBackground(String... params)
        {
            HttpClient client = new DefaultHttpClient();
            HttpGet get = new HttpGet(params[0]);
            String rtn = "";
            try{
                HttpResponse response = client.execute(get);
                StatusLine statusLine = response.getStatusLine();
                if(statusLine.getStatusCode() == HttpURLConnection.HTTP_OK){
                    byte[] result = EntityUtils.toByteArray(response.getEntity());
                    rtn = new String(result, "UTF-8");
                } else {
                    Log.d("sub", "@@@@@@@@@@ else pattern @@@@@@@@@@");
                }
            }
            catch (Exception e) {
                Log.d("catch error", e.toString());
            }
            return rtn;
        }

        @Override
        protected void onPostExecute(String result)
        {
            try {
                if (searchWord != null) {
                    Log.d("second debag unordered", result);
                    JSONObject json = new JSONObject(result);
                    resText.setText(json.toString());
                    searchWord = null;
                    return;
                }
                else if (searchWord == null) {
                    // JSONArrayがエントリのため、これをしないと例外で落ちる
                    String jsonBase = "{\"root\":" + result + "}";
                    JSONObject json = new JSONObject(jsonBase);
                    Log.d("first debag ordered", json.toString());
                    Toast.makeText(getApplicationContext(), json.toString(),
                            Toast.LENGTH_LONG).show();
                    JSONObject obj = json.getJSONArray("root").getJSONObject(0);
                    String name = obj.getString("city");
                    resText.setText(name);
                    searchWord = name;
                    String newsSearchURI = "http://ajax.googleapis.com/ajax/services/search/news?v=1.0&q=" + searchWord;
                    Log.d("そのままのURL２", newsSearchURI);
                    Task nextTask = new Task();
                    nextTask.execute(newsSearchURI);

                }
            }
            catch (JSONException e) {
                resText.setText("Json Error!!!" + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
