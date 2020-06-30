package com.wxipad.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wxipad.wechat.tools.beanConvert.GsonUtil;
import com.wxipad.wechat.tools.model.WechatApiMsg;
import com.wxipad.wechat.tools.model.WechatReturn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.SpringCodegen", date = "2019-03-16T23:33:44.718Z")

@Controller
public class WxipadApiController {
    private static final Logger log = LoggerFactory.getLogger(WxipadApiController.class);
    private final ObjectMapper objectMapper;
    private final HttpServletRequest request;

    @org.springframework.beans.factory.annotation.Autowired
    public WxipadApiController(ObjectMapper objectMapper, HttpServletRequest request) {
        this.objectMapper = objectMapper;
        this.request = request;
    }

    @ResponseBody
    @RequestMapping(value = "/wxipad/{cmd}",
            produces = {"application/xml", "application/json"},
            consumes = {"application/xml", "application/json"},
            method = RequestMethod.POST)
    public String wxipadCmdPost(@Valid @RequestBody WechatApiMsg wechatApiMsg) {
        String accept = null;
        WechatReturn wechatReturn = null;
        try {
            accept = request.getHeader("Accept");
            wechatReturn = CommonApi.execute(wechatApiMsg);
        } catch (Exception e) {
            wechatReturn = new WechatReturn();
            wechatReturn.setMsg(e.getMessage());
        }
        accept = GsonUtil.GsonString(wechatReturn);
        return accept;
    }

    /*@ResponseBody
    @RequestMapping(value = "/wxipad/batchWxipadPost",method = RequestMethod.POST)
    public List<WechatReturn> batchWxipadPost(@Valid @RequestBody List<WechatApiMsg> wechatApiMsg){
        List<WechatReturn> wechatReturns = new ArrayList<>();
        for (WechatApiMsg apiMsg : wechatApiMsg) {
            wechatReturns.add(CommonApi.execute(apiMsg));
        }

        return wechatReturns;
    }*/

}
