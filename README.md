# 시각장애인을 위한 스마트 선글라스 - 멋진코끼리팀
## YouTube 발표 영상 링크
https://www.youtube.com/watch?v=8NaU5azHdhg&list=PLysGR-hSRFyGE45dD5FJXEER9x7MNO9tc&index=4



**API Server**
|--------------|
|1. 본 프로젝트의 대상자는 시각장애인이므로 네비게이션을 위해 앱을 연동하고 여러 가지 트리거(ex버튼)를 배치하는 것은 적합하지 않다는 판단이 있었다.|
|2. 따라서 안드로이드 어플리케이션 내에 앱을 구동하면 사용자의 현재 위치의 경도와 위도 값을 스레드에서 계속하여 추출하고 이때 사용자가 목적지명을 입력하면 자동으로 경로안내를 시작하는 로직으로 방향성을 잡고 ‘시작점 (경도,위도) , 도착점(목적지명)’ 으로만 요청을 보냈을 때 실시간으로 경로안내를 하는 API 서버를 구축해야 한다는 결론에 이르렀다.|
|3. 설계한 API서버에 시작점(경도,위도) 와 도착점(목적지명)으로 요청이 들어오면 경로안내를 제공하기 위해 목적지의 경도,위도 좌표를 추출해야 했다. 따라서 먼저 목적지의 위도,경도 값을 추출하고 시작점,도착점(경도/위도)로 새롭게 전환한다.|
|4. 바뀐 값으로 T-MAP 보행자 경로 안내 API서버로 GET요청을 통해 관련 정보들이 담긴 JSON 파일을 받아온다. 이 값에서 현재 실시간으로 경로안내 정보만을 파싱하여 경로안내 요청을 보낸 곳으로 응답한다.|

   가. Routing
   
     - getnavigator : querystring으로 현재위치의 위도값, 현재위치의 경도값, 최종 목적지를 받는다.
     문자열인 최종 목적지를 설계한 비동기함수를 통해 위도,경도값으로 변환 후 T-MAP API에서 제공하는 보행자 경로 안내 API서버로 GET요청을 통해 
     사용자에게 보행자 경로안내 서비스를 제공 
 
   나. Function
   
     - GetPOIsearch : T-MAP API에서 제공하는 명칭(POI)통합검색 API서버로 POST요청을 통해 사용자가 입력한 목적지를 위도,경도값으로 변환
     - GetNavigator : T-MAP API에서 제공하는 보행자 경로 안내 API서버로 GET요청을 통해 사용자에게 보행자 경로안내 서비스를 제공 
 

**2. API Server**
 
   가. Routing
   
     - /api/portuse, /api/inout : 해운항만물류 정보시스템-Port-Mis에서 제공하는 API서버에서 실시간으로 선박입출항정보, 시설사용허가현황 데이터 수집
     - /news : 네이버 뉴스 탭의 항만 관련 기사 실시간 크롤링
     - /notice : 주요 항만공사(울산,부산,인천,여수) 공지사항 실시간 크롤링
     - /weather : 네이버 날씨 실시간 크롤링  
 
   나. Function
   
     - GetPortUseData : 해운항만물류 정보시스템-Port-Mis에서 제공하는 API서버로부터 시설사용허가현황 데이터 수집을 위해 
     post요청시 필요한 data를 전송하기 위하여 비동기 함수 설계
     - GetInoutData : 해운항만물류 정보시스템-Port-Mis에서 제공하는 API서버로부터 선박입출항정보 데이터 수집을 위해 
     post요청시 필요한 data를 전송하기 위하여 비동기 함수 설계



![image description](https://i.esdrop.com/d/igmccyiogpxf/RMHBiVwZic.gif)
|이름|담당 업무|
|:------:|--------------|
|박상준|RASA 챗봇 설계, 환경 개발 및 유지 보수|
|최용태|RASA 챗봇 설계, 환경 개발 및 유지 보수|
|이윤표|RASA 챗봇 설계, Android application 개발|
|홍진원|data Crawling, API 서버 구축, Android application 개발|
|정원석|data Crawling, API 서버 구축, Android application 개발|

