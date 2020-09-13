package com.ysy.tmall.order.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.ysy.tmall.order.vo.PayVo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "alipay")
@Component
@Data
public class AlipayTemplate {

    //在支付宝创建的应用的id
    private   String app_id = "2021000119639649";

    // 商户私钥，您的PKCS8格式RSA2私钥
    private  String merchant_private_key = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQChRafixqyBnfzOrzut+6MntjOOpFNHeJJvFQ6nBxU4FESeehZGetFIE4EnJFiXDC3g22KBmJjgPqEJNc70rWjNaG85CPc3+Ic6Sw6t5Ekg94knnCZ2j8BzLrTMF8AZQ4ZUc78CtxPutUzLubYswNfgACF6wGC8/NqAaGNnQgpTWWUKE37ZDtP8q5dMOzu/Mq1HMeGX3fdrHn7r+idlEJrYTY+Ofp8mArBJiiPRFeFtO4YM1lDdKQSDMx4URKtI5KxaDf+1xJ3wyyV/Ij60mY1OipEoQjcq6I6qrn0PHko5R+Hri2GwHMEJsJBIKOxyO3jdPBs25xwCZxGILAKm2YdBAgMBAAECggEALneGdwXtgG9H/zloRaoCiZeq5UedozMvZgg4L5IP4Bee6dadgnMyx8Su6ttDF0pwEaDEkjiom7wP5Dp3xfINB1o26cUWji7yr64eraXgGDqabypDvteOiOGFQGT7eYwGMWEkQSEFAjuExBJfVie+S5FeFCNH/EwSnPr24TKbDQZ3T7JriZT83FjudZBsTmxPxMhVvwSCljCEOwCXbwpwLSJkExocZjyCNJ5z4PMq0Lc4bqpTCMEjWWMNoPy1MDyO51FXLvIfbRdvbfH2NArQ1Vov0GJrvFeAXsSnAG7DqtJGZm+MftSSEQcjwnoNnoUVs/m6U3+OQOL/s5PSzWZ5jQKBgQDuJl7bdG44owDD5WG3uq+hXToYLF3/qq7fpg6pdRBFghFRf7gCfnhqpwelYTprtAFzf7QjPVI/28GSHSjozEmMsZT+6mUS/F+DaOAljo8u3/tpimV151EjG8yNUHakQq+FpHUi2ufJolmGqNljlq+w45JZqNzLHltWqNfGtNlFbwKBgQCtXCaxCBQ62/7CxiGL814E8bwZ2Y2xaFqMcuC8NedfJPztd5xjW25yW7EVSDcp6delOlRibbBKoeghjfRYvdiyy7RWBkLrJ01oO70L0mR8LIyQJfIsc0FZfw+glVJ+juWsij/yvrADPpkQWhOmutYqk2HKYKORz2GbTbDaAVuGTwKBgQDFR7/4p+DOxilSHmjunJX1ZJdFgytzKd+noZYvxqvuCKt0CgtS9ZWnNCrfU0XkBr/VAAsnzhqej9swwTssbl7XUByPd3i3W5177JsSQaBXQnCwo1cdbrwGC3dN8UP3Gs7wNKZcc17j57umE6XSG/f63upTn9EX6lPb3A6zqwj5vQKBgGOf12RY/jctAeSws8qXWuqAeNZqHXFcRNz9j4TZfVNCEE/vMuIuQvRZGRTf4UOm7JQ5RxEhc/QPwzS1PTxaLVPimInvIzm75lIGMoe+qpxphYW5KMi+m0lxKWvhI/0y5W4YLNO1HbVfndq1ySFBsZ3aAJKqopVClhcHuKAvs4clAoGAIV5qm/p7vYPAiyXcdseFHlwc7EkAhRUxgnBzCY0ml2zI3vP42Bh6zdEsgI0jXkK4QZhM03odj6cyl74r6yoHc/blcFtbtyk3YUPZZubFbhoFcq+eag9hEVpsv2+VN7wiqf4C1i+rlNMs/gk4nhAyLlwD13yperXB4x4iyACoEZ4=";
    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    private  String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA3gLIlpVrByYDAGsGq/IRMiG3nWwD0ie3JZPF68BfRMPXDD1pB44AV4pIsr3jbZmBYuhPIZzIGuJd7atkZs4xTCtGayb6beqsmMkVgpbMolpguuf7l9gLIpXJiF/bxIS/v3nRI8PyslzMqp6rILlmqukjk59ovi4AmN4/le4cfHcfz7iR6Eix40FPfmUs9YkbTZJufBbhnux4FBMMUqfbjnc77p6dcnfG488AGtHrYIfaYdfduclOrr8+6lOzw0sEksXx/NVj5laP8wiLf7u+3WmHmVYtDme/C7LHAPvUxAjKz1G9iIfUXMfsxxSPX52SN8Iw65eDIy8N72JhsvVBXQIDAQAB";
    // 服务器[异步通知]页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    // 支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息
    private  String notify_url;

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    //同步通知，支付成功，一般跳转到成功页
    private  String return_url;

    // 签名方式
    private  String sign_type = "RSA2";

    // 字符编码格式
    private  String charset = "utf-8";

    // 支付宝网关； https://openapi.alipaydev.com/gateway.do
    private  String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    //订单支付超时之间
    private String timeout="30m";

    public  String pay(PayVo vo) throws AlipayApiException {

        //AlipayClient alipayClient = new DefaultAlipayClient(AlipayTemplate.gatewayUrl, AlipayTemplate.app_id, AlipayTemplate.merchant_private_key, "json", AlipayTemplate.charset, AlipayTemplate.alipay_public_key, AlipayTemplate.sign_type);
        //1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl,
                app_id, merchant_private_key, "json",
                charset, alipay_public_key, sign_type);

        //2、创建一个支付请求 //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(return_url);
        alipayRequest.setNotifyUrl(notify_url);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = vo.getOut_trade_no();
        //付款金额，必填
        String total_amount = vo.getTotal_amount();
        //订单名称，必填
        String subject = vo.getSubject();
        //商品描述，可空
        String body = vo.getBody();

        alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\","
                + "\"total_amount\":\""+ total_amount +"\","
                + "\"subject\":\""+ subject +"\","
                + "\"body\":\""+ body +"\","
                + "\"timeout_express\":\""+timeout+"\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        String result = alipayClient.pageExecute(alipayRequest).getBody();

        //会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        System.out.println("支付宝的响应："+result);

        return result;

    }
}
