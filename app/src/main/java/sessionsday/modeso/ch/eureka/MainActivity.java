package sessionsday.modeso.ch.eureka;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

/**
 * Yandex mail: mahmoudgalal85@yandex.com
 */
public class MainActivity extends Activity{


    private Spinner fromLanguagesSpinner;
    private Spinner toLanguagesSpinner;
    private Button translateBtn;
    private ImageButton speakBtn;
    private TextView translationTxt;
    private EditText transText;
    private boolean ttsDataChecked = false;

    private TextToSpeech mTts;

    private static final String YANDEX_SERVICE_KEY = "trnsl.1.1.20151216T125716Z.2430891c0f0dae67.fa7bc2e2d6d3abacc17d7c06cdc4218d76fdf0ff";
    private static  final String YANDEX_SERVICE_TRANSLATION_BASE_URL = "https://translate.yandex.net/api/v1.5/tr.json/translate?key=" +
            YANDEX_SERVICE_KEY ;
    private static final String TAG = "DEBUG_TAG";
    private static final int MY_DATA_CHECK_CODE = 1201 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fromLanguagesSpinner = (Spinner) findViewById(R.id.from_lang_spinner);
        toLanguagesSpinner = (Spinner) findViewById(R.id.to_lang_spinner);
        translateBtn = (Button) findViewById(R.id.translate_btn);
        translationTxt = (TextView) findViewById(R.id.translation_txt);
        transText  = (EditText) findViewById(R.id.trans_text);
        speakBtn = (ImageButton) findViewById(R.id.speak_btn);

        //allow speaking only while english is selected
        if(fromLanguagesSpinner.getSelectedItemPosition()!=0)
            speakBtn.setEnabled(false);
        fromLanguagesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //allow speaking only while english is selected
                if(i!=0)
                    speakBtn.setEnabled(false);
                else
                    speakBtn.setEnabled(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        translateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isInternetConnectionExist(MainActivity.this))
                {
                    TranslateTask task = new TranslateTask();
                    task.execute(transText.getText().toString(), fromLanguagesSpinner.getSelectedItem().toString(), toLanguagesSpinner.getSelectedItem().toString());
                }
                else
                    Toast.makeText(MainActivity.this, "No internet connection!!.", Toast.LENGTH_LONG).show();
            }
        });

        speakBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = transText.getText().toString();
                if (text != null && text.length() >= 2) {
                    if(ttsDataChecked == false) {
                        Log.i(TAG,"Checking TTS data existence!");
                        Intent checkIntent = new Intent();
                        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
                        startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);
                    }
                    else
                    {
                        Log.i(TAG,"TTS data exists just speak!");
                        speakText(text);
                    }
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mTts != null)
            mTts.shutdown();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                ttsDataChecked = true;
                // success, create the TTS instance
                if(mTts== null)
                mTts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int initStatus) {
                        if (initStatus == TextToSpeech.SUCCESS) {
                            Log.i(TAG, "TTS data initialized just speak!");
                            mTts.setLanguage(Locale.US);
                            Toast.makeText(MainActivity.this, "Speech initialized correctly,speak now.", Toast.LENGTH_SHORT).show();
                        }
                        else if (initStatus == TextToSpeech.ERROR) {
                            Toast.makeText(MainActivity.this, "Sorry! Text To Speech failed...", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            } else {
                // missing data, install it
                Intent installIntent = new Intent();
                installIntent.setAction(
                        TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installIntent);
            }
        }
    }

    /**
     * Speaks the supplied word
     * @param text
     */
    private void speakText(String text)
    {
        if(mTts != null && !mTts.isSpeaking())
        //speak straight away
        mTts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }
    private String readStream(InputStream is)
    {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String read;
        try {
            while ((read = br.readLine()) != null) {
                sb.append(read);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }

        return sb.toString();
    }

    private String transLate(String text,String from,String to)
    {
        String ret = "";
        try
        {
            String qString = "&text="+text;
            qString+= "&lang="+from+"-"+to;
            qString+= "&format=plain";
            URL url = new URL(YANDEX_SERVICE_TRANSLATION_BASE_URL +qString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {
                InputStream in = urlConnection.getInputStream();
                ret = readStream(in);
                Log.i(TAG,"returned response = "+ret);
            }finally {
                urlConnection.disconnect();
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return ret;
    }
    public static boolean isInternetConnectionExist(Context context)
    {
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }


    /**
     * Fires the translation process asynchronously
     */
    class TranslateTask extends AsyncTask<String,Void,String>
  {
      private ProgressDialog pd;
      @Override
      protected void onPreExecute() {
          super.onPreExecute();
          pd = ProgressDialog.show(MainActivity.this,"","Processing...",true,false);
      }
      @Override
      protected String doInBackground(String... strings) {
          StringBuilder sb = new StringBuilder("\n");
          String jsonString = transLate(strings[0],strings[1],strings[2]);
          //Transforming the string response to JSON object and back to a formated string
          try {
              JSONObject json = new JSONObject(jsonString);
              JSONArray jsonArray = json.getJSONArray("text");
              if(jsonArray != null)
              {
                  int len = jsonArray.length();
                  for(int i=0;i<len;i++)
                  {
                      sb.append(jsonArray.getString(i));
                      sb.append("\n");
                  }
              }
          }catch (Exception ex){
              ex.printStackTrace();
          }
          return sb.toString();
      }

      @Override
      protected void onPostExecute(String s) {
          super.onPostExecute(s);
          translationTxt.setText(Html.fromHtml(s));
          if(pd != null)
              pd.dismiss();
      }
  }
}
