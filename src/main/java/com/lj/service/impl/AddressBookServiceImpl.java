package com.lj.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lj.entity.AddressBook;
import com.lj.mapper.AddressBookMapper;
import com.lj.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@Slf4j
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements AddressBookService {
}
