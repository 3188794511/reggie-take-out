package com.lj.utils;

import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.sms.v20190711.SmsClient;
import com.tencentcloudapi.sms.v20190711.models.SendSmsRequest;
import com.tencentcloudapi.sms.v20190711.models.SendSmsResponse;

public class SMSUtils {
    private static String secretId = "AKIDFONBpFQPAg1CAsOhokSzLVWP6f6fL4xv";
    private static String secretKey = "MldblzLh4WBwjYWaZYfJbC06MmPGCNxo";
    private static String sdkAppId = "1400672768";
    private static String signName = "李敬的公众号";
    
    //短信验证码模板id
    public static final String SMS_LOGIN = "1391452";

    public static void sendMessage(String templateId,String[] templateParamSet, String[] phoneNumberSet){
        try{
            //实例化一个认证对象，入参需要传入腾讯云账户密钥对secretId，secretKey。
            Credential cred = new Credential(secretId,secretKey);
            // 实例化一个http选项，可选，没有特殊需求可以跳过
            HttpProfile httpProfile = new HttpProfile();
            /* SDK默认使用POST方法。
             * 如果你一定要使用GET方法，可以在这里设置。GET方法无法处理一些较大的请求 */
            httpProfile.setReqMethod("POST");
            httpProfile.setConnTimeout(60);
            httpProfile.setEndpoint("sms.tencentcloudapi.com");
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setSignMethod("HmacSHA256");
            clientProfile.setHttpProfile(httpProfile);
            SmsClient client = new SmsClient(cred, "ap-guangzhou",clientProfile);
            SendSmsRequest req = new SendSmsRequest();
            /* 短信应用ID: 短信SdkAppId在 [短信控制台] 添加应用后生成的实际SdkAppId，示例如1400006666 */
            req.setSmsSdkAppid(sdkAppId);
            /* 短信签名内容: 使用 UTF-8 编码，必须填写已审核通过的签名 */
            req.setSign(signName);
            /* 模板 ID: 必须填写已审核通过的模板 ID */
            req.setTemplateID(templateId);
            /* 模板参数: 模板参数的个数需要与 TemplateId 对应模板的变量个数保持一致，若无模板参数，则设置为空 */
            req.setTemplateParamSet(templateParamSet);
            /* 下发手机号码，采用 E.164 标准，+[国家或地区码][手机号]
             * 示例如：+8613711112222， 其中前面有一个+号 ，86为国家码，13711112222为手机号，最多不要超过200个手机号 */
            req.setPhoneNumberSet(phoneNumberSet);
            /* 通过 client 对象调用 SendSms 方法发起请求。注意请求方法名与请求对象是对应的
             * 返回的 res 是一个 SendSmsResponse 类的实例，与请求对象对应 */
            SendSmsResponse res = client.SendSms(req);
            // 输出json格式的字符串回包
            System.out.println(SendSmsResponse.toJsonString(res));
        }catch (TencentCloudSDKException e) {
            e.printStackTrace();
    }
    }
}
