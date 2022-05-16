package com.lj.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lj.common.BaseContext;
import com.lj.common.R;
import com.lj.entity.ShoppingCart;
import com.lj.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 购物车操作
 */
@RestController
@RequestMapping("/shoppingCart")
@Slf4j
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;
    
    /**
     * 往购物车中添加一个套餐或菜品
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
        //为购物车添加所属用户
        shoppingCart.setUserId(BaseContext.getCurrentId());
        //判断是操作套餐还是菜品
        Long dishId = shoppingCart.getDishId();
        if (dishId != null){
            //操作的是菜品,判断购物车中是否已经添加过该菜品
            LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
            //通过userId和dishId可以锁定添加过的菜品
            queryWrapper.eq(ShoppingCart::getUserId,shoppingCart.getUserId()).eq(ShoppingCart::getDishId,dishId);
            ShoppingCart cart = shoppingCartService.getOne(queryWrapper);
            if (cart == null){
                //未添加过该菜品
                shoppingCart.setNumber(1);
                shoppingCartService.save(shoppingCart);
            }else{
                //添加过该菜品,该菜品在购物车中的数量+1
                cart.setNumber(cart.getNumber() + 1);
                shoppingCartService.update(cart,queryWrapper);
            }
        }
        else{
            //操作的是套餐,判断购物车中是否已经添加过该套餐
            LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
            //通过userId和setmealId可以锁定添加过的套餐
            queryWrapper.eq(ShoppingCart::getUserId,shoppingCart.getUserId()).eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
            ShoppingCart cart = shoppingCartService.getOne(queryWrapper);
            if (cart == null){
                //未添加过该套餐
                shoppingCart.setNumber(1);
                shoppingCartService.save(shoppingCart);
            }else{
                //添加过该菜品,该套餐在购物车中的数量+1
                cart.setNumber(cart.getNumber() + 1);
                shoppingCartService.update(cart,queryWrapper);
            }
        }
        return R.success(shoppingCart);
    }
    
    /**
     * 购物车中套餐或菜品的数量-1
     * @return
     */
    @PostMapping("/sub")
    public R<String> sub(@RequestBody Map<String,Long> map){
        Long dishId = map.get("dishId");
        Long userId = BaseContext.getCurrentId();
        if (dishId != null){
            //菜品
            LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ShoppingCart::getDishId,dishId).eq(ShoppingCart::getUserId,userId);
            //查询要修改的菜品
            ShoppingCart cart = shoppingCartService.getOne(queryWrapper);
            //判断要修改的菜品数量是否小于等于1
            if (cart.getNumber() <= 1){
                //直接删除
                shoppingCartService.removeById(cart.getId());
            }
            else{
                //菜品数量-1
                cart.setNumber(cart.getNumber() - 1);
                shoppingCartService.update(cart,queryWrapper);
            }
        }
        else{
            //套餐
            Long setmealId = map.get("setmealId");
            LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ShoppingCart::getSetmealId,setmealId).eq(ShoppingCart::getUserId,userId);
            //查询要修改的套餐
            ShoppingCart cart = shoppingCartService.getOne(queryWrapper);
            //判断要修改的套餐数量是否小于等于1
            if (cart.getNumber() <= 1){
                //直接删除
                shoppingCartService.removeById(cart.getId());
            }
            else{
                //套餐数量-1
                cart.setNumber(cart.getNumber() - 1);
                shoppingCartService.update(cart,queryWrapper);
            }
        }
        return R.success("");
    }
    
    /**
     * 清空当前用户的所有购物车数据
     * @return
     */
    @DeleteMapping("/clean")
    public R<String> clean(){
        Long userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,userId);
        shoppingCartService.remove(queryWrapper);
        return R.success("");
    }
    
    /**
     * 获取当前用户的所有购物车数据
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){
        Long userId = BaseContext.getCurrentId();//当前用户id
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        //查询单前用户的所有购物车数据,并按金额降序排列
        queryWrapper.eq(ShoppingCart::getUserId,userId).orderByDesc(ShoppingCart::getAmount);
        List<ShoppingCart> shoppingCartList = shoppingCartService.list(queryWrapper);
        return R.success(shoppingCartList);
    }
}
