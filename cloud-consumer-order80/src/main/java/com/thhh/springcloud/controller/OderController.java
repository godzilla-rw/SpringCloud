package com.thhh.springcloud.controller;

import com.thhh.springcloud.entities.CommonResult;
import com.thhh.springcloud.entities.Payment;
import com.thhh.springcloud.lb.MyLoadBalance;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("consumer")
public class OderController {
    //public static final String PAYMENT_URL = "http://localhost:8001";
    public static final String PAYMENT_URL = "http://CLOUD-PAYMENT-SERVICE";

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private MyLoadBalance myLoadBalance;

    @Resource
    private DiscoveryClient discoveryClient;

    @PostMapping("payment/create")
    public CommonResult create(Payment payment){
        log.info("===================== > " + payment.toString());
        return restTemplate.postForObject(PAYMENT_URL + "/payment/create", payment, CommonResult.class);
    }

    @PostMapping("payment/createForEntity")
    public CommonResult createForEntity(Payment payment){
        log.info("===================== > " + payment.toString());
        ResponseEntity<CommonResult> responseEntity = restTemplate.postForEntity(PAYMENT_URL + "/payment/create", payment, CommonResult.class);
        return new CommonResult(200, responseEntity.getStatusCode() + " ========= " + responseEntity.getHeaders() + " ========= " + responseEntity.getBody());
    }

    @GetMapping("payment/get/{id}")
    public CommonResult getPaymentById(@PathVariable("id") Long id){
        return restTemplate.getForObject(PAYMENT_URL + "/payment/get/" + id, CommonResult.class);
    }

    @GetMapping("payment/getForEntity/{id}")
    public CommonResult getPaymentForEntity(@PathVariable("id") Long id){
        ResponseEntity<CommonResult> responseEntity = restTemplate.getForEntity(PAYMENT_URL + "/payment/get/" + id, CommonResult.class);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            return new CommonResult(200, responseEntity.getBody() + "," + responseEntity.getStatusCode() + "," + responseEntity.getHeaders());
        }else {
            return new CommonResult(444, "数据获取失败，ID = " + id);
        }
    }

    @GetMapping(value = "/payment/lb")
    public String getPaymentByLb() {
        //按照服务名称获取服务集群list
        List<ServiceInstance> serviceInstances = discoveryClient.getInstances("CLOUD-PAYMENT-SERVICE");
        //非法值判断
        if (serviceInstances.size() <= 0 || serviceInstances == null){
            return null;
        }

        //调用我们自定义的lb算法选中一个ServiceInstance返回
        ServiceInstance instances = myLoadBalance.instances(serviceInstances);

        //获取这个ServiceInstance的uri，即服务的真实请求地址
        URI uri = instances.getUri();

        //调用restTemplate进行http网络调用，注意：uri只包含了IP+端口，不包含请求路径
        return restTemplate.getForObject(uri + "/payment/lb", String.class);
    }

    public static void main(String[] args) {
        System.out.println("push something to GitHub");
        
        System.out.println("pull something to local");
    }
}
