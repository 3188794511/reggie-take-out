package com.lj;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ReigieApplicationTest {
    @Test
    public void test1(){
        Long[] ids = {1231313L,34141414L};
        for (Long id : ids) {
            System.out.println(id);
        }
    }
    
}
