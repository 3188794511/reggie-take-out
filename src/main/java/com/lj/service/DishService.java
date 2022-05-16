package com.lj.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lj.dto.DishDto;
import com.lj.entity.Dish;

public interface DishService extends IService<Dish> {
    /**
     * 新增菜品(需要添加对应的菜品口味)
     * @param dishDto
     */
    public void add(DishDto dishDto);
    
    /**
     * 根据id查询菜品详细信息
     * @param id
     * @return
     */
    public DishDto findByIdWithFlavor(Long id);
    
    /**
     * 批量起售/停售
     * @param updateStatus
     * @param ids
     */
    public void updateStatusByIds(Integer updateStatus, Long[] ids);
    
    /**
     * 批量删除菜品(需要删除菜品的基本信息和菜品关联的口味)
     * @param ids
     */
    public void deleteByIds(Long[] ids);
}
