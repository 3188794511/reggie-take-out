package com.lj.dto;

import com.lj.entity.OrderDetail;
import com.lj.entity.Orders;
import lombok.Data;

import java.util.List;

@Data
public class OrdersDto extends Orders {

    private String userName;

    private String phone;

    private String address;

    private List<OrderDetail> orderDetails;
	
}
