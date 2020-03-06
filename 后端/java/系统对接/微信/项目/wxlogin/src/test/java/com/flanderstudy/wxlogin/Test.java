package com.flanderstudy.wxlogin;

import org.junit.runner.RunWith;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static com.flanderstudy.wxlogin.contract.Contract.*;

@RunWith(value = SpringJUnit4ClassRunner.class)
public class Test {

    @org.junit.Test
    public void wxLogin() throws UnsupportedEncodingException {
        String url = URLEncoder.encode("127.0.0.1", "utf-8");
        RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();
        String wxReturn = restTemplateBuilder.build().getForObject(String.format(CODE_URL, APPID, url, "success"), String.class);
        System.out.println(wxReturn);

    }
}
