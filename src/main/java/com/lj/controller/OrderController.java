package com.lj.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lj.common.BaseContext;
import com.lj.common.R;
import com.lj.dto.OrdersDto;
import com.lj.entity.OrderDetail;
import com.lj.entity.Orders;
import com.lj.entity.User;
import com.lj.service.OrderDetailService;
import com.lj.service.OrderService;
import com.lj.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单管理
 */
@RestController
@Slf4j
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private UserService userService;
    @Autowired
    private OrderDetailService orderDetailService;
    
    /**
     * 提交订单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        orderService.submit(orders);
        return R.success("支付成功");
    }
    
    @GetMapping("/userPage")
    public R<Page<OrdersDto>> userPage(Integer page, Integer pageSize){
        //当前用户
        Long userId = BaseContext.getCurrentId();
        User user = userService.getById(userId);
        //分页查询订单基本信息
        Page<Orders> ordersPage = new Page<>(page,pageSize);
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getUserId,userId).orderByDesc(Orders::getCheckoutTime);
        orderService.page(ordersPage, queryWrapper);
        //订单详情分页查询
        Page<OrdersDto> ordersDtoPage = new Page<>(page,pageSize);
        BeanUtils.copyProperties(ordersPage,ordersDtoPage,"records");
        List<OrdersDto> ordersDtoList = ordersPage.getRecords().stream().map((item) -> {
            OrdersDto ordersDto = new OrdersDto();
            BeanUtils.copyProperties(item, ordersDto);
            ordersDto.setUserName(user.getName());
            ordersDto.setPhone(user.getPhone());
            LambdaQueryWrapper<Orders> ordersLambdaQueryWrapper = new LambdaQueryWrapper<>();
            ordersLambdaQueryWrapper.eq(Orders::getUserId,userId).eq(Orders::getId,item.getId());
            ordersDto.setAddress((orderService.getOne(ordersLambdaQueryWrapper)).getAddress());
            //订单的具体菜品信息或套餐信息
            LambdaQueryWrapper<OrderDetail> orderDetailLambdaQueryWrapper = new LambdaQueryWrapper<>();
            orderDetailLambdaQueryWrapper.eq(OrderDetail::getOrderId, item.getId());
            List<OrderDetail> orderDetailList = orderDetailService.list(orderDetailLambdaQueryWrapper);
            ordersDto.setOrderDetails(orderDetailList);
            return ordersDto;
        }).collect(Collectors.toList());
        ordersDtoPage.setRecords(ordersDtoList);//给订单详情的分页数据属性赋值
        return R.success(ordersDtoPage);
    }
    
    /**
     * 分页查询所有用户订单
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/page")
    public R<Page<Orders>> page(Integer page,Integer pageSize){
        Page<Orders> ordersPage = new Page<>(page,pageSize);
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Orders::getOrderTime);
        orderService.page(ordersPage,queryWrapper);
        return R.success(ordersPage);
    }
    
    /**
     * 修改订单状态
     * @param orders
     * @return
     */
    @PutMapping
    public R<String> editOrderDetail(@RequestBody Orders orders){
        orderService.updateById(orders);
        return R.success("订单状态修改成功");
    }
    
    @PostMapping("/again")
    public R<String> again(@RequestBody Orders orders){
        orderService.again(orders);
        return R.success("再来一单成功");
    }
    
}
