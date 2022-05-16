package com.lj.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lj.dto.SetmealDto;
import com.lj.entity.Setmeal;

public interface SetmealService extends IService<Setmeal> {
    /**
     * 添加套餐和套餐包含的菜品
     * @param setmealDto
     */
    public void saveWithDish(SetmealDto setmealDto);
    
    /**
     * 修改套餐和套餐包含的菜品
     * @param setmealDto
     */
    public void updateWithDish(SetmealDto setmealDto);
    
    /**
     * 批量删除套餐
     * @param ids
     */
    public void deleteWithDishByIds(Long[] ids);
}
