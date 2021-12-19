const { default: axios, Axios } = require("axios");
const { urlencoded } = require("express");
const express = require("express");
//x : 경도 y:위도
const GetNavigator = async(startX,starty,endX,endY)=>{
    try{
        let result = await axios({
            withCredentials: true,
            method : 'POST',
            url: "https://apis.openapi.sk.com/tmap/routes/pedestrian?version=1",
            async: false,
            params : {
                "appKey": "l7xxcfefdc48e5484976b1712b4452c69462",
                "startX" : startX,
                "startY" : starty,
                "endX": endX,
                "endY": endY,
                "startName": "출발지",
                "endName": "목적지",
                "reqCoordType": "WGS84GEO",
                "resCoordType": "EPSG3857"     
            }
        });
        let json = {
            navigator:null
        }
        json.navigator = result.data.features[0].properties.description;
        
        return json;
        
    }catch(error){
        console.log(error);
    }

}

const GetPOIsearch = async(destination)=>{
    try{
        searchKeyword = encodeURIComponent(destination);
        appKey = "l7xxcfefdc48e5484976b1712b4452c69462";
        resCoordType = "WGS84GEO";
        url = `https://apis.openapi.sk.com/tmap/pois?version=1&appKey=${appKey}&searchKeyword=${searchKeyword}&resCoordType=${resCoordType}`;
        let result= await axios.get(`${url}`);
        let json = {
            desx : null,
            desy : null
        }
        json.desy = result.data.searchPoiInfo.pois.poi[0].frontLat; //목적지 위도
        json.desx = result.data.searchPoiInfo.pois.poi[0].frontLon; // 목적지 경도
        
        return json;
    }catch(e){
        console.log(e)
    }
}

module.exports.GetNavigator = GetNavigator;
module.exports.GetPOIsearch = GetPOIsearch;
