package com.lj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lj.common.CustomerException;
import com.lj.entity.Category;
import com.lj.entity.Dish;
import com.lj.entity.Setmeal;
import com.lj.mapper.CategoryMapper;
import com.lj.service.CategoryService;
import com.lj.service.DishService;
import com.lj.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 分类
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private DishService dishService;
    /**
     * 根基id删除分类
     * @param id
     */
    public void deleteById(Long id){
        //判断当前要删除的分类是否被关联到套餐
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId,id);
        int count1 = setmealService.count(setmealLambdaQueryWrapper);
        if(count1 > 0){
            //抛出业务异常
            throw new CustomerException("当前分类已关联套餐,不能删除");
        }
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.eq(Dish::getCategoryId,id);
        int count2 = dishService.count(dishLambdaQueryWrapper);
        if (count2 > 0){
            //抛出业务异常
            throw new CustomerException("当前分类已关联菜品,不能删除");
        }
        //删除分类
        super.removeById(id);
    }
}
