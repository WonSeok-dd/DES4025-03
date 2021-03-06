package com.example.socketbutton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

public class MainActivity extends AppCompatActivity {

    public Button SocketButton, ObjectButton, NavigatinButton;

    //Socket
    private Socket socket;
    static final String HOST = "192.168.191.165";
    static final int PORT = 9999;

    //TTS
    private TextToSpeech hellotts;
    private TextToSpeech tts;

    //STT
    SpeechRecognizer mRecognizer;
    final int PERMISSION = 1;
    Intent soundIntent;

    //νμ¬ μμΉ
    Double longitude; //κ²½λ - startx
    Double latitude; //μλ - starty

    //Navigating
    static final String helloMessage = "μμΌμ°κ²°μ νλ €λ©΄ μμͺ½μ μΌμͺ½λ²νΌ, μ₯μ λ¬Ό νμ§μλΉμ€λ₯Ό μ΄μ©νλ €λ©΄ μμͺ½μ μ€λ₯Έμͺ½λ²νΌ, λ€λΉκ²μ΄μ μλΉμ€λ₯Ό μ΄μ©νλ €λ©΄ μλμͺ½λ²νΌμ λλ¬μ£ΌμΈμ";
    static final String navigationMessage = "μνμλ λͺ©μ μ§λ₯Ό λ§μν΄μ£ΌμΈμ";
    static final String againMessage = "λͺ©μ μ§λ₯Ό μ¬μ€μ  ν©λλ€ λ€μνλ² μλμͺ½ νλ©΄μ ν°μΉν΄μ£ΌμΈμ";

    static RequestQueue requestQueue;

    String guideMessage = ""; //κ²½λ‘ μλ΄ λ©μμ§
    String endPoint = ""; //μλ ₯ν λͺ©μ μ§

    boolean is_threadrun = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SocketButton = findViewById(R.id.SocketButton);
        ObjectButton = findViewById(R.id.ObjectButton);
        NavigatinButton = findViewById(R.id.NavigatingButton);
        
        //Navigation Permissionκ°μ 
        AndPermission.with(this)
                .runtime()
                .permission(
                        Permission.ACCESS_FINE_LOCATION,
                        Permission.ACCESS_COARSE_LOCATION)
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {

                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {

                    }
                })
                .start();

        //μ± μ¬μ©λ² hello TTSνκΈ°
        hellotts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                hellotts.speak(helloMessage, TextToSpeech.QUEUE_FLUSH, null);
            }
        });

        //TTS μ€μ 
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != android.speech.tts.TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.KOREAN);
                }
            }
        });

        //Permission μ²΄ν¬
        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        if (Build.VERSION.SDK_INT >= 23) {
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.INTERNET, Manifest.permission.RECORD_AUDIO
            }, PERMISSION);
        }

        //RecognizerIntent κ°μ²΄ μμ±
        soundIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        soundIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        soundIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");

        //μμ²­κ°μ²΄ μμ±
        requestQueue = Volley.newRequestQueue(this);
        
        //νμ¬μμΉ μ’νκ° κ°±μ  Thread
        getLocationThread glThread = new getLocationThread();
        glThread.start();

        //μμΌ μ°κ²° λ²νΌ
        SocketButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Connect Socket Thread
                ConnectThread connectThread = new ConnectThread();
                connectThread.start();
            }
        });

        //μ₯μ λ¬Ό νμ§ λ²νΌ
        ObjectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StartThread sthread = new StartThread();
                sthread.start();
            }
        });
        
        //λ€λΉκ²μ΄μ λ²νΌ
        NavigatinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                while (true) {
                    if (is_threadrun == false) {
                        
                        //λͺ©μ μ§ μλ ₯μμ²­ TTS
                        tts.setPitch(1.0f);
                        tts.setSpeechRate(1.0f);
                        tts.speak(navigationMessage, TextToSpeech.QUEUE_FLUSH, null);
                        
                        //λͺ©μ μ§ μλ ₯μλ΅ STT
                        try {
                            Thread.sleep(3000); // λͺ©μ μ§ μλ ₯μμ²­ TTSμν¨
                            mRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
                            mRecognizer.setRecognitionListener(listener);
                            mRecognizer.startListening(soundIntent);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        
                        //threadμλ μ€λΉ λ° λ€λΉκ²μ΄μ μμ
                        is_threadrun = true;
                        navigationThread nt = new navigationThread();
                        nt.start();
                        break;
                        
                    } else {
                        //threadμλ λ©μΆ€ λ° λͺ©μ μ§ μ΄κΈ°ν λ° λͺ©μ μ§ μ¬μλ ₯μμ²­ TTS
                        is_threadrun = false;
                        endPoint = "";

                        tts.setPitch(1.0f);
                        tts.setSpeechRate(1.0f);
                        tts.speak(againMessage, TextToSpeech.QUEUE_FLUSH, null);
                        try {
                            Thread.sleep(5000);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    }
                }
            }
        });
    }
    
    //λͺ©μ μ§ μλ ₯ STT listener
    private RecognitionListener listener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle bundle) {
            Toast.makeText(getApplicationContext(), "λͺ©μ μ§ μλ ₯ μμ±μΈμμ μμ", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onBeginningOfSpeech() {
        }

        @Override
        public void onRmsChanged(float v) {
        }

        @Override
        public void onBufferReceived(byte[] bytes) {
        }

        @Override
        public void onEndOfSpeech() {
        }

        @Override
        public void onError(int i) {
            String message;
            switch (i) {
                case SpeechRecognizer.ERROR_AUDIO:
                    message = "μ€λμ€ μλ¬";
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    message = "ν΄λΌμ΄μΈνΈ μλ¬";
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    message = "νΌλ―Έμ μμ";
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    message = "λ€νΈμν¬ μλ¬";
                    break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    message = "λ€νΈμ νμμμ";
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    message = "μ°Ύμ μ μμ";
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    message = "RECOGNIZERκ° λ°μ¨";
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    message = "μλ²κ° μ΄μν¨";
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    message = "λ§νλ μκ°μ΄κ³Ό";
                    break;
                default:
                    message = "μ μ μλ μ€λ₯μ";
                    break;
            }
        }

        @Override
        public void onResults(Bundle results) {
            // λ§μ νλ©΄ ArrayListμ λ¨μ΄λ₯Ό λ£κ³  textViewμ λ¨μ΄λ₯Ό μ΄μ΄μ€λλ€.
            ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            for (int i = 0; i < matches.size(); i++) {
                endPoint += matches.get(i);
            }
            System.out.println(endPoint);
        }

        @Override
        public void onPartialResults(Bundle bundle) {
        }

        @Override
        public void onEvent(int i, Bundle bundle) {
        }
    };
    
    @Override
    protected void onStop() {
        super.onStop();
        try {
            socket.close(); //μμΌμ λ«λλ€.
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
        if (hellotts != null){
            hellotts.stop();
            hellotts.shutdown();;
            hellotts = null;
        }
        super.onDestroy();
    }

    //Socketμ°κ²° μ€λ λ
    class ConnectThread extends Thread {

        public void run() {
            try {
                //ν΄λΌμ΄μΈνΈ μμΌ μμ±
                socket = new Socket(HOST, PORT);
                System.out.println("Socket μμ±, μ°κ²°.");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tts.setPitch(1.0f);
                        tts.setSpeechRate(1.0f);
                        tts.speak("μμΌ μ°κ²° μλ£", TextToSpeech.QUEUE_FLUSH, null);
                    }
                });

            } catch (UnknownHostException uhe) { // μμΌ μμ± μ μ λ¬λλ νΈμ€νΈ(www.unknown-host.com)μ IPλ₯Ό μλ³ν  μ μμ.
                System.out.println(" μμ± Error : νΈμ€νΈμ IP μ£Όμλ₯Ό μλ³ν  μ μμ.(μλͺ»λ μ£Όμ κ° λλ νΈμ€νΈ μ΄λ¦ μ¬μ©)");
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Error : νΈμ€νΈμ IP μ£Όμλ₯Ό μλ³ν  μ μμ.(μλͺ»λ μ£Όμ κ° λλ νΈμ€νΈ μ΄λ¦ μ¬μ©)", Toast.LENGTH_SHORT).show();

                    }
                });
            } catch (IOException ioe) { // μμΌ μμ± κ³Όμ μμ I/O μλ¬ λ°μ.
                System.out.println(" μμ± Error : λ€νΈμν¬ μλ΅ μμ");
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Error : λ€νΈμν¬ μλ΅ μμ", Toast.LENGTH_SHORT).show();

                    }
                });
            } catch (SecurityException se) { // security managerμμ νμ©λμ§ μμ κΈ°λ₯ μν.

                System.out.println(" μμ± Error : λ³΄μ(Security) μλ°μ λν΄ λ³΄μ κ΄λ¦¬μ(Security Manager)μ μν΄ λ°μ. (νλ‘μ(proxy) μ μ κ±°λΆ, νμ©λμ§ μμ ν¨μ νΈμΆ)");
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Error : λ³΄μ(Security) μλ°μ λν΄ λ³΄μ κ΄λ¦¬μ(Security Manager)μ μν΄ λ°μ. (νλ‘μ(proxy) μ μ κ±°λΆ, νμ©λμ§ μμ ν¨μ νΈμΆ)", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IllegalArgumentException le) { // μμΌ μμ± μ μ λ¬λλ ν¬νΈ λ²νΈ(65536)μ΄ νμ© λ²μ(0~65535)λ₯Ό λ²μ΄λ¨.

                System.out.println(" μμ± Error : λ©μλμ μλͺ»λ νλΌλ―Έν°κ° μ λ¬λλ κ²½μ° λ°μ.(0~65535 λ²μ λ°μ ν¬νΈ λ²νΈ μ¬μ©, null νλ‘μ(proxy) μ λ¬)");
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), " Error : λ©μλμ μλͺ»λ νλΌλ―Έν°κ° μ λ¬λλ κ²½μ° λ°μ.(0~65535 λ²μ λ°μ ν¬νΈ λ²νΈ μ¬μ©, null νλ‘μ(proxy) μ λ¬)", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    //Socketν΅μ  μμ μ€λ λ
    class StartThread extends Thread {

        int bytes;
        String Dtmp;

        public StartThread() {

        }

        public String byteArrayToHex(byte[] a) {
            StringBuilder sb = new StringBuilder();
            for (final byte b : a)
                sb.append(String.format("%02x ", b & 0xff));
            return sb.toString();
        }

        public String hexToAscii(String hexStr) {
            StringBuilder output = new StringBuilder("");

            String[] arr = hexStr.split(" ");
            for (int i = 0; i < arr.length; i++) {
                output.append((char) Integer.parseInt(arr[i], 16));
            }
            return output.toString();
        }

        public String strToAns(String str){
            int countarr[] = new int[21];
            String arr[] = str.split("/");
            String result = "";
            String objects[] = {" μ°¨λ"," μ΄λ₯μ°¨λ"," μ¬λ"," κ³λ¨"," μ νΈλ±"," κΈ°λ₯"," μ§μ λ°©μ§ λ΄"};
            String direc[] = {"μΌμͺ½μ","μ€λ₯Έμͺ½μ","μ λ°©μ"};
            Vector<String> v = new Vector<>();
            Vector<String> v2 = new Vector<>();
            Vector<String> v3= new Vector<>();

            for (int i = 0; i < arr.length; i++) {
                if (arr[i].contains("car")) {
                    if (arr[i].charAt(arr[i].length() - 1) == 'l') {
                        countarr[0]++;
                    } else if (arr[i].charAt(arr[i].length() - 1) == 'r') {
                        countarr[1]++;
                    } else {
                        countarr[2]++;
                    }

                } else if (arr[i].contains("cycle")) {
                    if (arr[i].charAt(arr[i].length() - 1) == 'l') {
                        countarr[3]++;
                    } else if (arr[i].charAt(arr[i].length() - 1) == 'r') {
                        countarr[4]++;
                    } else {
                        countarr[5]++;
                    }

                } else if (arr[i].contains("person")) {
                    if (arr[i].charAt(arr[i].length() - 1) == 'l') {
                        countarr[6]++;
                    } else if (arr[i].charAt(arr[i].length() - 1) == 'r') {
                        countarr[7]++;
                    } else {
                        countarr[8]++;
                    }

                } else if (arr[i].contains("stair")) {
                    if (arr[i].charAt(arr[i].length() - 1) == 'l') {
                        countarr[9]++;
                    } else if (arr[i].charAt(arr[i].length() - 1) == 'r') {
                        countarr[10]++;
                    } else {
                        countarr[11]++;
                    }

                } else if (arr[i].contains("signal")) {
                    if (arr[i].charAt(arr[i].length() - 1) == 'l') {
                        countarr[12]++;
                    } else if (arr[i].charAt(arr[i].length() - 1) == 'r') {
                        countarr[13]++;
                    } else {
                        countarr[14]++;
                    }

                } else if (arr[i].contains("power")) {
                    if (arr[i].charAt(arr[i].length() - 1) == 'l') {
                        countarr[15]++;
                    } else if (arr[i].charAt(arr[i].length() - 1) == 'r') {
                        countarr[16]++;
                    } else {
                        countarr[17]++;
                    }

                } else if (arr[i].contains("bolard")) {
                    if (arr[i].charAt(arr[i].length() - 1) == 'l') {
                        countarr[18]++;
                    } else if (arr[i].charAt(arr[i].length() - 1) == 'r') {
                        countarr[19]++;
                    } else {
                        countarr[20]++;
                    }

                }
            }

            int cnt = 0;
            for(int i=0;i<21;i+=3)
            {
                List<String> tmplist = new ArrayList<String>();

                int cnt2 = 0;
                boolean flag = false;
                for(int j=i;j<i+3;j++)
                {
                    if(countarr[j]!=0)
                    {
                        String tmp = "";
                        tmp+=direc[j-i];
                        tmp+=objects[i/3];
                        tmplist.add(tmp);
                        cnt2+=countarr[j];
                        flag =true;

                    }
                }
                if(cnt2>=2)
                {
                    String tmp = "";
                    tmplist.clear();
                    tmp = "μ λ°©μ λ€μμ";
                    tmp+=objects[i/3];
                    tmplist.add(tmp);
                }

                if(flag == true)
                    cnt++;

                for(int j=0;j<tmplist.size();j++)
                {
                    if(tmplist.get(j).charAt(0)== 'μΌ')
                        v.add(tmplist.get(j).substring(3));
                    else if(tmplist.get(j).charAt(0) == 'μ€')
                        v2.add(tmplist.get(j).substring(4));
                    else if(tmplist.get(j).charAt(0) == 'μ ')
                    {
                        v3.add(tmplist.get(j).substring(3));
                    }
                }
                if(cnt == 3)
                    break;
            }
            if(!v3.isEmpty())
                result+="μ λ°©μ";
            for(int i=0;i<v3.size();i++)
            {
                result+=v3.get(i);
                if(i!=v3.size()-1)
                    result+="κ³Ό";
                else
                    result+="μ΄ μμ΅λλ€ ";
            }

            if(!v.isEmpty())
                result+="μΌμͺ½μ";
            for(int i=0;i<v.size();i++)
            {
                result+=v.get(i);
                if(i!=v.size()-1)
                    result+="κ³Ό";
                else
                    result+="μ΄ μμ΅λλ€ ";
            }
            if(!v2.isEmpty())
                result+="μ€λ₯Έμͺ½μ";
            for(int i=0;i<v2.size();i++)
            {
                result+=v2.get(i);
                if(i!=v2.size()-1)
                    result+="κ³Ό";
                else
                    result+="μ΄ μμ΅λλ€ ";
            }
            return result;

        }

        public void run() {

            // λ°μ΄ν° μμ 
            try {
                System.out.println("λ°μ΄ν° μμ  μ€λΉ");
                while (true) {
                    byte[] buffer = new byte[1024];

                    InputStream input = socket.getInputStream();
                    bytes = input.read(buffer);
                    System.out.println("byte = " + bytes);

                    //Byte Hexa(ex) 37 38 ...)λ‘ λ°κΏμ Dtmp Stringμ μ μ₯.
                    Dtmp = byteArrayToHex(buffer);
                    Dtmp = Dtmp.substring(0, bytes * 3);

                    //Dtmp Stringμ λ¬Έμμ΄λ‘ λ³ν, μμ± μΆλ ₯
                    String ans = strToAns(hexToAscii(Dtmp));
                    tts.setPitch(1.0f);
                    tts.setSpeechRate(1.0f);
                    tts.speak(ans, TextToSpeech.QUEUE_FLUSH, null);
                    System.out.println(hexToAscii(Dtmp));
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("μμ  μλ¬");
            }
        }
    }

    //νμ¬ μμΉ νλ¨
    public void startLocationService() {
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        try {
            Location location = manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                String message = "νμ¬ μμΉ : longitude: " + longitude + "\nlatitude: " + latitude;
            }

            GPSListener gpsListener = new GPSListener(); //GPSlistenerκ°μ²΄ μμ±
            long minTime = 10000;
            float minDistance = 0;

            manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistance, gpsListener); //GPSlinstenerκ°μ²΄ μ λ¬
            
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
    
    //νμ¬ μμΉ νλ¨μ μ¬μ©
    class GPSListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            longitude = location.getLongitude();
            latitude = location.getLatitude();
        }
        @Override
        public void onProviderEnabled(String provider) {
        }
        @Override
        public void onProviderDisabled(String provider) {
        }
    }
    
    //Navigationκ²½λ‘ μμ²­
    public void makeRequest(Double longitude1, Double latitude1, String end) {

        String str_longitude1 = longitude1.toString();
        String str_latitude1 = latitude1.toString();
        String urlstr = "http://3.34.56.85:3001/getnavigator?startx=" + str_longitude1 + "&starty=" + str_latitude1 + "&end=" + end; //(domain=10.0.2.2:3001)(domain=ngrok)
        StringRequest request = new StringRequest(Request.Method.GET, urlstr,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        JSONObject jsonObject = null;
                        try {
                            jsonObject = new JSONObject(response);
                            String str = jsonObject.getString("description");
                            str = str.replace("{", "");
                            str = str.replace("}", "");
                            str = str.replace("\"navigator\":", "");
                            str = str.replace("\"", "");
                            guideMessage = str;

                            tts.setPitch(1.0f);
                            tts.setSpeechRate(1.0f);
                            tts.speak(guideMessage, TextToSpeech.QUEUE_FLUSH, null);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_LONG).show();
            }
        }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                return params;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(
                2000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        request.setShouldCache(false);
        requestQueue.add(request);
    }

    //νμ¬ μμΉ νλ¨ Thread
    class getLocationThread extends Thread {

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        startLocationService();
                    }
                });
            }
        };

        @Override
        public void run() {
            try {
                Timer timer = new Timer();
                timer.schedule(timerTask, 0, 1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //Navgation Thread
    class navigationThread extends Thread {

        @Override
        public void run() {
            try {
                Thread.sleep(5000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (endPoint.equals("") != true) {
                String start = endPoint + "λ‘ κ²½λ‘μλ΄λ₯Ό μμν©λλ€.";
                tts.setPitch(1.0f);
                tts.setSpeechRate(1.0f);
                tts.speak(start, TextToSpeech.QUEUE_FLUSH, null);
                try {
                    Thread.sleep(5000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                makeRequest(longitude, latitude, endPoint);

            } else {
                String nodest = "λͺ©μ μ§κ° μ€μ λμ§ μμμ΅λλ€.";
                tts.setPitch(1.0f);
                tts.setSpeechRate(1.0f);
                tts.speak(nodest, TextToSpeech.QUEUE_FLUSH, null);
                is_threadrun = false;
            }
            while (is_threadrun) {
                try {
                    Thread.sleep(60000);
                    makeRequest(longitude, latitude, endPoint);
                } catch (Exception e) {
                    System.out.println("μ€ν¨");
                }
            }
            try {
                Thread.sleep(2000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}