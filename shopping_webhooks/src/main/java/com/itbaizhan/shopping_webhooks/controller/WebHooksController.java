package com.itbaizhan.shopping_webhooks.controller;

import com.itbaizhan.shopping_common.service.MailService;
import com.itbaizhan.shopping_webhooks.dto.AlarmMessageDto;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/*
 * 告警控制器
 */
@RestController
public class WebHooksController {
    @DubboReference
    private MailService mailService;
    /**
     * SkyWalking告警的回调方法，也就是该接口是暴露给SkyWalking的，SkyWalking告警时会调用该接口，
     * 我们接受到SkyWalking传来的告警信息，可以处理告警信息
     * @param alarmMessageDtoList SkyWalking封装的告警信息
     */
    @PostMapping("/alarm")
    public void alarm(@RequestBody List<AlarmMessageDto> alarmMessageDtoList) {
        StringBuilder builder = new StringBuilder();
        alarmMessageDtoList.forEach(info -> {
            builder.append("\nscopeId:").append(info.getScopeId())
                    .append("\nScope实体:").append(info.getScope())
                    .append("\n告警消息:").append(info.getAlarmMessage())
                    .append("\n告警规则:").append(info.getRuleName())
                    .append("\n\n------------------------\n\n");
        });
        //     System.out.println(builder.toString());
        /**
         * 在企业开发时，可以在系统中对程序员邮箱进行维护，从数据库中找出该项目的相关负责人，向其发送邮件
         * 此处同学们给自己的邮箱发送告警信息即可
         */
        mailService.sendMail("fire1230@126.com",builder.toString(),"注意！百战商城项目告警");
    }
}

