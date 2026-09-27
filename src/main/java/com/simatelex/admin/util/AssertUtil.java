package com.simatelex.admin.util;


import com.simatelex.admin.exceptions.ParamsException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AssertUtil {
    public  static void isTrue(Boolean flag,String msg){
        if(Boolean.TRUE.equals(flag)){
            log.error(msg);
            throw  new ParamsException(msg);
        }
    }

}
