const express = require("express");
const morgan = require("morgan");
const restapiRouter = require("./routes/navigator");

const app = express();
app.set("port", process.env.PORT || 3001);

app.use(morgan("dev"));
app.use(express.json());
app.use(express.urlencoded({extended : false}));

app.use("/",restapiRouter);


app.use((req,res,next)=>{
    const error = new Error(`${req.method} ${req.url}라우터가 없습니다`);
    error.status = 404;
    next(error);
});

app.use((err,req,res,next)=>{
    res.status(err.status || 500).send(err.message);
});

app.listen(app.get("port"), ()=>{
    console.log(app.get("port"),"번 포트에서 대기 중");
});
