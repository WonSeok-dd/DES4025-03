# 시각장애인을 위한 스마트 선글라스 - 멋진코끼리팀
=============

## YouTube 발표 영상 링크
https://www.youtube.com/watch?v=8NaU5azHdhg&list=PLysGR-hSRFyGE45dD5FJXEER9x7MNO9tc&index=4




**1. RASA 오픈소스**

   가. 챗봇의 종류
   
     - 열린 대화 구조: 어떤 질문이든 답할 수 있도록 열려 있는 구조를 기반으로 하는데, 분명히 챗봇이 대답하지 못하는 것이 나올 수 있다는 것을 염두해야 한다. 
     - 닫힌 대화 구조: 원하는 목적지까지 갈 수 있도록 계속 가이드를 해주는 구조이다. 버튼/선택형으로 구조를 설계해 대화를 이어 나가는 닫힌 구조의 챗봇은 룰 베이스(Rule-base) 챗봇, 시나리오형 챗봇이라고도 한다.

   나. FAQ형 챗봇 사용
   
     - FAQ형 챗봇은 시나리오 기반(룰베이스)과 지능형 대하 기반(인공지능) 두 가지를 혼합하여 만든 챗봇이다
     - 사용자가 버튼을 눌러가면서 답을 찾을 수 있도록 제안하는 방식의 ‘룰베이스(Rule Base)’와 사용자가 채팅하여 질문하면 답변을 제공하는 자연어 처리 방식의 지능형 대화’를 하나의 챗봇에 담았다.
   
   다. RASA의 구성
   
 ![image description](https://i.esdrop.com/d/igmccyiogpxf/W7gqi1YjWB.JPG)


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

