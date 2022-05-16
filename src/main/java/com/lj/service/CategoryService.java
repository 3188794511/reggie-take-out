package com.lj.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lj.entity.Category;

public interface CategoryService extends IService<Category> {
    /**
     * 根据id删除分类
     * @param id
     */
    public void deleteById(Long id);
}
