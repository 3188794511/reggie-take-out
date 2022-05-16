package com.lj.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lj.entity.Orders;

public interface OrderService extends IService<Orders> {
    /**
     * 支付订单
     * @param orders
     */
    public void submit(Orders orders);
    
    /**
     * 再来一单
     * @param orders
     */
    public void again(Orders orders);
}
