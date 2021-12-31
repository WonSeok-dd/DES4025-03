# 시각장애인을 위한 스마트 선글라스
### YouTube 발표 영상 링크
https://www.youtube.com/watch?v=8NaU5azHdhg&list=PLysGR-hSRFyGE45dD5FJXEER9x7MNO9tc&index=4

<br/><br/>

## 1. 구성도
![image](https://user-images.githubusercontent.com/75558861/146684563-aea40b1d-6b76-46db-ae91-23f477fa18c3.png)

## 2. API Server

|**요약**|
|----|
|본 프로젝트의 대상자는 시각장애인이므로 네비게이션을 위해 앱을 연동하고 여러 가지 트리거(ex버튼)를 배치하는 것은 적합하지 않다는 판단이 있었다.|
|따라서 안드로이드 어플리케이션 내에 앱을 구동하면 사용자의 현재 위치의 경도와 위도 값을 스레드에서 계속하여 추출하고 이때 사용자가 목적지명을 입력하면 자동으로 경로안내를 시작하는 로직으로 방향성을 잡고 ‘시작점 (경도,위도) , 도착점(목적지명)’ 으로만 요청을 보냈을 때 실시간으로 경로안내를 하는 API 서버를 구축해야 한다는 결론에 이르렀다.|
|설계한 API서버에 시작점(경도,위도) 와 도착점(목적지명)으로 요청이 들어오면 경로안내를 제공하기 위해 목적지의 경도,위도 좌표를 추출해야 했다. 따라서 먼저 목적지의 위도,경도 값을 추출하고 시작점,도착점(경도/위도)로 새롭게 전환한다.|
|바뀐 값으로 T-MAP 보행자 경로 안내 API서버로 GET요청을 통해 관련 정보들이 담긴 JSON 파일을 받아온다. 이 값에서 현재 실시간으로 경로안내 정보만을 파싱하여 경로안내 요청을 보낸 곳으로 응답한다.|

   가. Node.js - Routing
   
     - /getnavigator : querystring으로 현재위치의 위도값, 현재위치의 경도값, 최종 목적지를 받는다.
     문자열인 최종 목적지를 설계한 비동기함수를 통해 위도,경도값으로 변환 후 T-MAP API에서 제공하는 보행자 경로 안내 API서버로 GET요청을 통해 
     사용자에게 보행자 경로안내 서비스를 제공 
 
   나. Node.js - Function
   
     - GetPOIsearch : T-MAP API에서 제공하는 명칭(POI)통합검색 API서버로 POST요청을 통해 사용자가 입력한 목적지를 위도,경도값으로 변환
     - GetNavigator : T-MAP API에서 제공하는 보행자 경로 안내 API서버로 GET요청을 통해 사용자에게 보행자 경로안내 서비스를 제공
    
   다. Android Studio - NavigationThread
      
     - 각 단계마다 thread sleep을 통해 STT 및 TTS실행 
     - 사용자가 Android App에서 네비게이션 시작 버튼을 클릭시 시각장애인을 위한 목적지입력요청 메시지 TTS출력
     - 사용자가 Android App에서 목적지입력 STT실행 
     - 사용자가 목적지 입력시 설계한 API Server로 주기적으로 요청을 보내 경로안내 시작 
 

## 3. CoralBoard 와 Android App 간의 TCP socket 통신

|**요약**|
| -------------- |
|CoralBoard의 detect.py 에서 socket 라이브러리를 import하여 TCP Server를 구축시킨다.| 
|구축한 TCP Server의 IP주소와 PORT에 맞게 Android App에서 Client socket을 생성하여 연결시킨다. |
|CoralBoard에서 탐지한 객체의 일치 확률, 객체의 이름, 객체의 위치값을 Android App에서 정제하여 사용자에게 TTS로 출력해준다.|

   가. ConnectThread 
       
      - 사용자가 Android App에서 소켓 연결 시작 버튼을 클릭시 
        구축한 TCP Server의 IP주소와 PORT에 맞게 Client Socket을 생성하여 CoralBoard와 연결하고 
        시각장애인을 위한 CoralBoard연결 완료 메시지 TTS출력

   나. StartThread
      
      - 사용자가 Android App에서 객체 인식 시작 버튼을 클릭시 socket 통신을 시작
      - socket으로부터 전달받은 byte배열을 16진수로 변환 뒤, 문자열로 변환
      - 객체의 이름, 객체의 위치로 전달하기 위해 출력값 정제 후 시각장애인을 위한 객체인식 메시지 TTS출력  


## 4. 역할 분담
|이름|담당 업무|
|:------:|--------------|
|박상준|Mobilenet SSD 객체탐지 딥러닝 데이터셋 구축 및 학습|
|최용태|Mobilenet SSD 딥러닝 모델 구축, Coral board 제어 및 통신환경 개발|
|정원석|Navigation API 서버 구축, Android application 개발|
|홍진원|Navigation API 서버 구축, Android application 개발|


