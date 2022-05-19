package com.lj.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lj.common.CustomerException;
import com.lj.dto.DishDto;
import com.lj.entity.Dish;
import com.lj.entity.DishFlavor;
import com.lj.mapper.DishMapper;
import com.lj.service.DishFlavorService;
import com.lj.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@Transactional
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishFlavorService dishFlavorService;
    /**
     * 新增菜品(需要添加对应的菜品口味)
     * @param dishDto
     */
    public void add(DishDto dishDto) {
        //将菜品的基本信息保存dish表
        this.save(dishDto);
        //获取菜品的id
        Long dishId = dishDto.getId();
        //获取dishDto中的菜品口味信息
        List<DishFlavor> flavors = dishDto.getFlavors();
        //对flavors中的每一种口味的数据进行处理,关联对应的菜品id
        flavors.forEach((item) -> item.setDishId(dishId));
        //将菜品口味信息保存到dish_flavor表
        flavors.forEach((item) -> dishFlavorService.save(item));
    }
    
    /**
     * 根据id查询菜品详细信息
     * @param id
     * @return
     */
    public DishDto findByIdWithFlavor(Long id) {
        //先查询菜品的基本信息
        Dish dish = this.getById(id);
        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish,dishDto);
        //查询菜品的口味信息,并封装
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(id != null,DishFlavor::getDishId,id);
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);
        dishDto.setFlavors(flavors);
        return dishDto;
    }
    
    /**
     * 批量起售/停售
     * @param updateStatus
     * @param ids
     */
    public void updateStatusByIds(Integer updateStatus, Long[] ids) {
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        Dish dish = new Dish();
        dish.setStatus(updateStatus);
        queryWrapper.in(ids.length > 0 && ids != null,Dish::getId,ids);
        this.update(dish,queryWrapper);
    }
    
    /**
     * 批量删除菜品(需要删除菜品的基本信息和菜品关联的口味)
     * @param ids
     */
    public void deleteByIds(Long[] ids) {
        //先判断要删除的菜品中是否含有正在售卖的菜品
        List<Dish> dishList = this.listByIds(Arrays.asList(ids));
        for (Dish dish : dishList) {
            if (dish.getStatus() == 1){
                throw new CustomerException("菜品正在售卖,无法删除");
            }
        }
        //先删除口味,再删除菜品基本信息(先删除从表,再删除主表)
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ids.length > 0 && ids != null,DishFlavor::getDishId,ids);
        dishFlavorService.remove(queryWrapper);
        this.removeByIds(Arrays.asList(ids));
    }
}
