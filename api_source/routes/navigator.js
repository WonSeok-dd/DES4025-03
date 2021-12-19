const e = require("express");
const express = require("express");
const data=require("./restApi.js");
const router = express.Router();

router.get('/getnavigator',async(req,res,next)=>{

    try{
        let startx = req.query.startx;
        let starty = req.query.starty;
        let end = req.query.end;
        let json = {
            description: null
        }
        let endx = (await data.GetPOIsearch(end)).desx;
        let endy = (await data.GetPOIsearch(end)).desy;

        json.description = await data.GetNavigator(startx,starty,endx,endy);

        res.json(json);

    }catch(e){
        console.log(e);
        next(e);
    }
})

module.exports = router;