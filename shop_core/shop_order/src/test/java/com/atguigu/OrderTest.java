package com.atguigu;

import com.atguigu.entity.TOrder;
import com.atguigu.service.TOrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
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

}
