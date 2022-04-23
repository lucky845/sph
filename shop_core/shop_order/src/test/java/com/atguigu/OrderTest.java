package com.atguigu;

import com.atguigu.entity.TOrder;
import com.atguigu.entity.TOrderDetail;
import com.atguigu.mapper.TOrderMapper;
import com.atguigu.service.TOrderDetailService;
import com.atguigu.service.TOrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * @author lucky845
 * @since 2022年04月23日
 */
@SpringBootTest
public class OrderTest {

    @Resource
    private TOrderService tOrderService;

    @Resource
    private TOrderDetailService tOrderDetailService;

    @Resource
    private TOrderMapper tOrderMapper;

    @DisplayName("保存订单分库分表")
    @Test
    public void test01() {
        for (int i = 0; i < 1; i++) {
            TOrder tOrder = new TOrder();
            tOrder.setOrderPrice(99);
            String uuid = UUID.randomUUID().toString();
            tOrder.setTradeNo(uuid);
            tOrder.setOrderStatus("未支付");
            int userId = new Random().nextInt(20);
            System.out.println("userId = " + userId);
            tOrder.setUserId(Long.parseLong(userId + ""));
            tOrderService.save(tOrder);
        }
    }

    @DisplayName("保存订单与订单详情分库分表")
    @Test
    public void test02() {
        TOrder tOrder = new TOrder();
        tOrder.setTradeNo("enjoy6288");
        tOrder.setOrderPrice(9900);
        tOrder.setUserId(3L); //node-1--->table-4
        tOrderService.save(tOrder);


        TOrderDetail iphone13 = new TOrderDetail();
        iphone13.setOrderId(tOrder.getId());
        iphone13.setSkuName("Iphone13");
        iphone13.setSkuNum(1);
        iphone13.setSkuPrice(6000);
        iphone13.setUserId(3L); //要进行分片计算
        tOrderDetailService.save(iphone13);

        TOrderDetail sanxin = new TOrderDetail();
        sanxin.setOrderId(tOrder.getId());
        sanxin.setSkuName("三星");
        sanxin.setSkuNum(2);
        sanxin.setSkuPrice(3900);
        sanxin.setUserId(3L); //要进行分片计算
        tOrderDetailService.save(sanxin);
        System.out.println("保存完成....");
    }

    @DisplayName("查询订单与订单详情")
    @Test
    public void test03() {
        List<TOrder> orderList = tOrderMapper.queryOrderByUserId(3L);
        for (TOrder tOrder : orderList) {
            System.out.println(tOrder);
        }
    }

}
