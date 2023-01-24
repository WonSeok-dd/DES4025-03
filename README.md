# ✈️ 프로젝트 소개
> **시각장애인을 위한 스마트 선글라스**
- **동국대학교 2021 기업사회맞춤형 캡스톤디자인2 프로젝트, 팀 멋진코끼리**
- **프로젝트 기간** : 2021.09.07 ~ 2021.12.17 (3개월)
- **YouTube 발표 영상 링크** : https://www.youtube.com/watch?v=8NaU5azHdhg&list=PLysGR-hSRFyGE45dD5FJXEER9x7MNO9tc&index=4
- **온라인 포스터** : https://drive.google.com/file/d/1ra0BARaPDO-F_RKt2eJ07YMIEnfoZN3C/view?usp=sharing
- 영상처리 기술, 객체탐지 딥러닝 기술, GPS를 통한 네비게이팅을 활용하여 시각장애인들의 안전하고 편리한 외출을 보조하기 위한 ‘시각장애인을 위한 안전 길안내 서비스‘ 

<br><br>
# 📄 프로젝트 흐름도
![image](https://user-images.githubusercontent.com/75558861/214293305-1f1b1664-a1bc-47c0-acf4-0df8d0f8b38d.png)

<br><br>
# 🛠 개발 환경
> ![Java](https://img.shields.io/badge/java-007396?style=for-the-badge&logo=java&logoColor=white)
> ![Android](https://img.shields.io/badge/Android_Studio-3DDC84?style=for-the-badge&logo=android-studio&logoColor=white)
> <br>
> ![JavaScript](https://img.shields.io/badge/JavaScript-323330?style=for-the-badge&logo=javascript&logoColor=F7DF1E)
> ![Node.js](https://img.shields.io/badge/Node.js-339933?style=for-the-badge&logo=nodedotjs&logoColor=white)
> <br>
> ![Python](https://img.shields.io/badge/python-3776AB?style=for-the-badge&logo=python&logoColor=white)
> ![Tensorflow](https://img.shields.io/badge/TensorFlow-FF6F00?style=for-the-badge&logo=TensorFlow&logoColor=white)
> <br>
> ![AWS](https://img.shields.io/badge/amazonaws-232F3E?style=for-the-badge&logo=amazonaws&logoColor=white)

<br><br>
# 👩‍💻 기능 설계

## 1️⃣ 내비게이션 기능

|**API Server 설계 방향성**|
|----|
|본 프로젝트의 대상자는 시각장애인이므로 네비게이션을 위해 <br> 앱을 연동하고 여러 가지 트리거(ex버튼)를 배치하는 것은 적합하지 않다는 판단이 있었다.|
|따라서 **Android App**을 구동하면 사용자의 '현재 위치 = 시작점(위도, 경도)'을 스레드에서 계속하여 추출하고 <br> 이때 사용자가 '도착점(목적지명)'을 입력하면 자동으로 경로안내를 시작하는 로직으로 방향성을 잡고 <br> '시작점 (위도, 경도)와 도착점(목적지명)' 으로만 요청을 보냈을 때 <br> 실시간으로 경로안내를 하는 API 서버를 구축해야 한다는 결론에 이르렀다.|

|**API Server 와 Android App 간의 HTTP 통신 과정**|
|----|
|**설계한 API서버**에 '시작점(위도, 경도)와 도착점(목적지명)'으로 요청이 들어오면 <br> 경로안내를 제공하기 위해 '도착점(위도, 경도)'을 추출해야 했다. <br> 따라서 '도착점(위도, 경도)'을 추출하고 '시작점(위도, 경도) 도착점(위도, 경도)'로 새롭게 변환한다.|
|변환 값으로 **T-MAP 보행자 경로 안내 API서버**로 GET요청을 통해 관련 정보들이 담긴 JSON 파일을 받아온다. <br> 이 값에서 현재 실시간으로 경로안내 정보만을 파싱하여 경로안내 요청을 보낸 곳으로 응답한다.|

```
가. Android Studio - NavigationThread
      
1. 각 단계마다 thread sleep을 통해 STT 및 TTS실행 
2. 사용자가 Android App에서 네비게이션 시작 버튼을 클릭시 시각장애인을 위한 목적지 입력요청 메시지 TTS출력
3. 사용자가 Android App에서 목적지입력 STT실행 
4. 사용자가 목적지 입력시 설계한 API Server로 주기적으로 요청을 보내 경로안내 시작 
```

```
나. Node.js - Routing
   
- /getnavigator : 
1. querystring으로 '현재 위치 = 시작점(위도, 경도)와 도착점(목적지명)'를 획득
2. 문자열인 '도착점(목적지명)'를 설계한 GetPOIsearch를 통해 '도착점(위도, 경도)'으로 변환 
3. T-MAP 보행자 경로 안내 API서버로 GET요청을 통해 사용자에게 보행자 경로 안내 서비스를 제공 
```

```
다. Node.js - Async Function
 
- GetPOIsearch : 
1. T-MAP 명칭(POI)통합검색 API서버로 GET요청을 통해 
   사용자가 입력한 '도착점(목적지명)'를 '도착점(위도, 경도)'으로 변환
     
- GetNavigator : 
1. T-MAP 보행자 경로 안내 API서버로 POST요청을 통해 
  사용자에게 보행자 경로안내 서비스를 제공
```    

 

## 2️⃣ 객체 인식 기능

|**CoralBoard 와 Android App 간의 TCP socket 통신 과정**|
| -------------- |
|CoralBoard의 detect.py 에서 socket 라이브러리를 import하여 **TCP Server**를 구축| 
|**TCP Server**의 IP주소와 PORT에 맞게 **Android App**에서 Client socket을 생성하여 연결|
|CoralBoard에서 탐지한 객체의 일치 확률, 객체의 이름, 객체의 위치값을 **Android App**에서 정제하여 사용자에게 TTS로 출력|

```
가. Android Studio - ConnectThread 
       
1. 사용자가 Android App에서 소켓 연결 시작 버튼 클릭 
2. TCP Server의 IP주소와 PORT에 맞게 Client Socket을 생성하여 CoralBoard와 연결 
3. 시각장애인을 위한 CoralBoard연결 완료 메시지 TTS출력
```

```
나. Android Studio - StartThread
      
1. 사용자가 Android App에서 객체 인식 시작 버튼을 클릭
2. socket 통신을 시작
3. socket 으로부터 전달받은 byte배열을 16진수로 변환 뒤, 문자열로 변환
4. 객체의 이름, 객체의 위치로 전달하기 위해 출력값 정제
5. 시각장애인을 위한 객체인식 메시지 TTS출력  
```

<br><br>
# 👥 멤버
|이름|담당 업무|
|:------:|--------------|
|박상준|Mobilenet SSD 객체탐지 딥러닝 데이터셋 구축 및 학습|
|최용태|Mobilenet SSD 딥러닝 모델 구축, Coral board 제어 및 통신환경 개발|
|정원석|내비게이션 API 서버 구축, Android application 개발|
|홍진원|내비게이션 API 서버 구축, Android application 개발|
