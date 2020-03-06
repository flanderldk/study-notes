package com.flanderstudy.wxlogin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestTemplate;

@Controller
public class WxLoginController {

    @Autowired
    private RestTemplate restTemplate;

}
