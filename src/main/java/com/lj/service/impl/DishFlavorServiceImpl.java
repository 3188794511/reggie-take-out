package com.lj.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lj.entity.DishFlavor;
import com.lj.mapper.DishFlavorMapper;
import com.lj.service.DishFlavorService;
import org.springframework.stereotype.Service;

@Service
public class DishFlavorServiceImpl extends ServiceImpl<DishFlavorMapper, DishFlavor> implements DishFlavorService {
}
