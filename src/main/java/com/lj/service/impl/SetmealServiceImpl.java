package com.lj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lj.common.CustomerException;
import com.lj.dto.SetmealDto;
import com.lj.entity.Setmeal;
import com.lj.entity.SetmealDish;
import com.lj.mapper.SetmealMapper;
import com.lj.service.SetmealDishService;
import com.lj.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    @Autowired
    private SetmealDishService setmealDishService;
    /**
     * 添加套餐和套餐包含的菜品
     * @param setmealDto
     */
    public void saveWithDish(SetmealDto setmealDto) {
        //添加套餐基本信息
        this.save(setmealDto);
        //添加套餐包含的菜品
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        //将每个菜品都添加到setmeal_dish表中
        setmealDishes.stream().map((item) -> {
            //设置关联的套餐id
            item.setSetmealId(setmealDto.getId());
            setmealDishService.save(item);
            return item;
        }).collect(Collectors.toList());
    }
    
    /**
     * 修改套餐和套餐包含的菜品
     * @param setmealDto
     */
    public void updateWithDish(SetmealDto setmealDto) {
        Setmeal setmeal = new Setmeal();
        //将套餐基本信息拷贝到setmeal
        BeanUtils.copyProperties(setmealDto,setmeal,"setmealDishes,categoryName");
        //修改基本信息
        this.updateById(setmeal);
        //先将原来关联的菜品删除,再将setmealDto中的菜品新增到setmeal_dish表
        //删除原先关联的菜品信息
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,setmealDto.getId());
        setmealDishService.remove(queryWrapper);
        //关联新的菜品
        for (SetmealDish setmealDish : setmealDto.getSetmealDishes()) {
            setmealDish.setSetmealId(setmealDto.getId());
            setmealDishService.save(setmealDish);
        }
    }
    
    /**
     * 批量删除套餐
     * @param ids
     */
    public void deleteWithDishByIds(Long[] ids) {
        //判断套餐是否正在售卖
        List<Setmeal> setmealList = this.listByIds(Arrays.asList(ids));
        for (Setmeal setmeal : setmealList) {
            if (setmeal.getStatus() == 1){
                throw new CustomerException("套餐正在售卖中,无法删除");
            }
        }
        for (Long id : ids) {
            //先删除从表setmeal_dish数据
            LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SetmealDish::getSetmealId,id);
            setmealDishService.remove(queryWrapper);
            //再删除主表setmeal数据
            this.removeById(id);
        }
    }
    
}
