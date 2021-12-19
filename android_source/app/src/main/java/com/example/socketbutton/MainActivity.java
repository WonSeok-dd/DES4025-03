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

    //현재 위치
    Double longitude; //경도 - startx
    Double latitude; //위도 - starty

    //Navigating
    static final String helloMessage = "소켓연결을 하려면 위쪽의 왼쪽버튼, 장애물 탐지서비스를 이용하려면 위쪽의 오른쪽버튼, 네비게이션 서비스를 이용하려면 아래쪽버튼을 눌러주세요";
    static final String navigationMessage = "원하시는 목적지를 말씀해주세요";
    static final String againMessage = "목적지를 재설정 합니다 다시한번 아래쪽 화면을 터치해주세요";

    static RequestQueue requestQueue;

    String guideMessage = ""; //경로 안내 메시지
    String endPoint = ""; //입력한 목적지

    boolean is_threadrun = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SocketButton = findViewById(R.id.SocketButton);
        ObjectButton = findViewById(R.id.ObjectButton);
        NavigatinButton = findViewById(R.id.NavigatingButton);
        
        //Navigation Permission강제
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

        //앱 사용법 hello TTS하기
        hellotts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                hellotts.speak(helloMessage, TextToSpeech.QUEUE_FLUSH, null);
            }
        });

        //TTS 설정
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != android.speech.tts.TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.KOREAN);
                }
            }
        });

        //Permission 체크
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

        //RecognizerIntent 객체 생성
        soundIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        soundIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        soundIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");

        //요청객체 생성
        requestQueue = Volley.newRequestQueue(this);
        
        //현재위치 좌표값 갱신 Thread
        getLocationThread glThread = new getLocationThread();
        glThread.start();

        //소켓 연결 버튼
        SocketButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Connect Socket Thread
                ConnectThread connectThread = new ConnectThread();
                connectThread.start();
            }
        });

        //장애물 탐지 버튼
        ObjectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StartThread sthread = new StartThread();
                sthread.start();
            }
        });
        
        //네비게이션 버튼
        NavigatinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                while (true) {
                    if (is_threadrun == false) {
                        
                        //목적지 입력요청 TTS
                        tts.setPitch(1.0f);
                        tts.setSpeechRate(1.0f);
                        tts.speak(navigationMessage, TextToSpeech.QUEUE_FLUSH, null);
                        
                        //목적지 입력응답 STT
                        try {
                            Thread.sleep(3000); // 목적지 입력요청 TTS위함
                            mRecognizer = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
                            mRecognizer.setRecognitionListener(listener);
                            mRecognizer.startListening(soundIntent);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        
                        //thread작동 준비 및 네비게이션 시작
                        is_threadrun = true;
                        navigationThread nt = new navigationThread();
                        nt.start();
                        break;
                        
                    } else {
                        //thread작동 멈춤 및 목적지 초기화 및 목적지 재입력요청 TTS
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
    
    //목적지 입력 STT listener
    private RecognitionListener listener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle bundle) {
            Toast.makeText(getApplicationContext(), "목적지 입력 음성인식을 시작", Toast.LENGTH_SHORT).show();
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
                    message = "오디오 에러";
                    break;
                case SpeechRecognizer.ERROR_CLIENT:
                    message = "클라이언트 에러";
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    message = "퍼미션 없음";
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    message = "네트워크 에러";
                    break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    message = "네트웍 타임아웃";
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    message = "찾을 수 없음";
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    message = "RECOGNIZER가 바쁨";
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    message = "서버가 이상함";
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    message = "말하는 시간초과";
                    break;
                default:
                    message = "알 수 없는 오류임";
                    break;
            }
        }

        @Override
        public void onResults(Bundle results) {
            // 말을 하면 ArrayList에 단어를 넣고 textView에 단어를 이어줍니다.
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
            socket.close(); //소켓을 닫는다.
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

    //Socket연결 스레드
    class ConnectThread extends Thread {

        public void run() {
            try {
                //클라이언트 소켓 생성
                socket = new Socket(HOST, PORT);
                System.out.println("Socket 생성, 연결.");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tts.setPitch(1.0f);
                        tts.setSpeechRate(1.0f);
                        tts.speak("소켓 연결 완료", TextToSpeech.QUEUE_FLUSH, null);
                    }
                });

            } catch (UnknownHostException uhe) { // 소켓 생성 시 전달되는 호스트(www.unknown-host.com)의 IP를 식별할 수 없음.
                System.out.println(" 생성 Error : 호스트의 IP 주소를 식별할 수 없음.(잘못된 주소 값 또는 호스트 이름 사용)");
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Error : 호스트의 IP 주소를 식별할 수 없음.(잘못된 주소 값 또는 호스트 이름 사용)", Toast.LENGTH_SHORT).show();

                    }
                });
            } catch (IOException ioe) { // 소켓 생성 과정에서 I/O 에러 발생.
                System.out.println(" 생성 Error : 네트워크 응답 없음");
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Error : 네트워크 응답 없음", Toast.LENGTH_SHORT).show();

                    }
                });
            } catch (SecurityException se) { // security manager에서 허용되지 않은 기능 수행.

                System.out.println(" 생성 Error : 보안(Security) 위반에 대해 보안 관리자(Security Manager)에 의해 발생. (프록시(proxy) 접속 거부, 허용되지 않은 함수 호출)");
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Error : 보안(Security) 위반에 대해 보안 관리자(Security Manager)에 의해 발생. (프록시(proxy) 접속 거부, 허용되지 않은 함수 호출)", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (IllegalArgumentException le) { // 소켓 생성 시 전달되는 포트 번호(65536)이 허용 범위(0~65535)를 벗어남.

                System.out.println(" 생성 Error : 메서드에 잘못된 파라미터가 전달되는 경우 발생.(0~65535 범위 밖의 포트 번호 사용, null 프록시(proxy) 전달)");
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getApplicationContext(), " Error : 메서드에 잘못된 파라미터가 전달되는 경우 발생.(0~65535 범위 밖의 포트 번호 사용, null 프록시(proxy) 전달)", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    //Socket통신 시작 스레드
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
            String objects[] = {" 차량"," 이륜차량"," 사람"," 계단"," 신호등"," 기둥"," 진입 방지 봉"};
            String direc[] = {"왼쪽에","오른쪽에","전방에"};
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
                    tmp = "전방에 다수의";
                    tmp+=objects[i/3];
                    tmplist.add(tmp);
                }

                if(flag == true)
                    cnt++;

                for(int j=0;j<tmplist.size();j++)
                {
                    if(tmplist.get(j).charAt(0)== '왼')
                        v.add(tmplist.get(j).substring(3));
                    else if(tmplist.get(j).charAt(0) == '오')
                        v2.add(tmplist.get(j).substring(4));
                    else if(tmplist.get(j).charAt(0) == '전')
                    {
                        v3.add(tmplist.get(j).substring(3));
                    }
                }
                if(cnt == 3)
                    break;
            }
            if(!v3.isEmpty())
                result+="전방에";
            for(int i=0;i<v3.size();i++)
            {
                result+=v3.get(i);
                if(i!=v3.size()-1)
                    result+="과";
                else
                    result+="이 있습니다 ";
            }

            if(!v.isEmpty())
                result+="왼쪽에";
            for(int i=0;i<v.size();i++)
            {
                result+=v.get(i);
                if(i!=v.size()-1)
                    result+="과";
                else
                    result+="이 있습니다 ";
            }
            if(!v2.isEmpty())
                result+="오른쪽에";
            for(int i=0;i<v2.size();i++)
            {
                result+=v2.get(i);
                if(i!=v2.size()-1)
                    result+="과";
                else
                    result+="이 있습니다 ";
            }
            return result;

        }

        public void run() {

            // 데이터 수신
            try {
                System.out.println("데이터 수신 준비");
                while (true) {
                    byte[] buffer = new byte[1024];

                    InputStream input = socket.getInputStream();
                    bytes = input.read(buffer);
                    System.out.println("byte = " + bytes);

                    //Byte Hexa(ex) 37 38 ...)로 바꿔서 Dtmp String에 저장.
                    Dtmp = byteArrayToHex(buffer);
                    Dtmp = Dtmp.substring(0, bytes * 3);

                    //Dtmp String을 문자열로 변환, 음성 출력
                    String ans = strToAns(hexToAscii(Dtmp));
                    tts.setPitch(1.0f);
                    tts.setSpeechRate(1.0f);
                    tts.speak(ans, TextToSpeech.QUEUE_FLUSH, null);
                    System.out.println(hexToAscii(Dtmp));
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("수신 에러");
            }
        }
    }

    //현재 위치 판단
    public void startLocationService() {
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        try {
            Location location = manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                String message = "현재 위치 : longitude: " + longitude + "\nlatitude: " + latitude;
            }

            GPSListener gpsListener = new GPSListener(); //GPSlistener객체 생성
            long minTime = 10000;
            float minDistance = 0;

            manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistance, gpsListener); //GPSlinstener객체 전달
            
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
    
    //현재 위치 판단시 사용
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
    
    //Navigation경로 요청
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

    //현재 위치 판단 Thread
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
                String start = endPoint + "로 경로안내를 시작합니다.";
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
                String nodest = "목적지가 설정되지 않았습니다.";
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
                    System.out.println("실패");
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