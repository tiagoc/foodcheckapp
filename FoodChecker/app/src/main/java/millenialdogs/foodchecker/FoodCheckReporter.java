package millenialdogs.foodchecker;

import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class FoodCheckReporter extends AsyncTask {

    String endpoint = "http://192.168.10.30:5000/foodcheck";

    JSONObject endpointResponse;

    TextToSpeech speechConverter;

    static boolean reporting = false;

    public FoodCheckReporter(TextToSpeech t2s) {
        speechConverter = t2s;
    }

    public FoodCheckReporter(TextToSpeech t2s, String url) {
        speechConverter = t2s;
        endpoint = url;
    }

    @Override
    protected Object doInBackground(Object[] params) {
        FoodCheckReporter.reporting = true;
        try {
            final URL url = new URL(endpoint);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(5000);
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            BufferedReader rd = new BufferedReader(new InputStreamReader(in));
            String line;
            StringBuffer response = new StringBuffer();
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            in.close();
            String responseStr = response.toString();
            endpointResponse = new JSONObject(responseStr);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    @Override
    protected void onPostExecute(Object result) {
        try {
            String humanString = new String("The food is " + endpointResponse.getString("name") +
                    " being kept at " + endpointResponse.getInt("temperature") + " degrees celsius with " +
                    endpointResponse.getInt("humidity") + " percent humidity.");
            speechConverter.speak(humanString, TextToSpeech.QUEUE_FLUSH, null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        FoodCheckReporter.reporting = false;
    }
}
