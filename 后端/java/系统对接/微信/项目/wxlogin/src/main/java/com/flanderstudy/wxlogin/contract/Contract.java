package com.flanderstudy.wxlogin.contract;

public interface Contract {

    //AppID
    String APPID = "wxb71fe33d826b2e5b";
    //AppSecret
    String APPSECRET = "fa3172f96a9ad96fe1f5a775c1bfaee1";

    String CODE_URL= "https://open.weixin.qq.com/connect/qrconnect?appid=%s&redirect_uri=%s&response_type=code&scope=snsapi_login&state=%s#wechat_redirect";
}
