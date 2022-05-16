package com.lj.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lj.entity.ShoppingCart;
import com.lj.mapper.ShoppingCartMapper;
import com.lj.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Transactional
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {
}
