package com.erinicv1.apiImpl;

import com.erinicv1.annotation.RpcService;
import com.erinicv1.service.FabResult;

@RpcService(FabResult.class)
public class FabResultImpl  {

    public Integer get(Integer num) {
        if (num < 2){
            return num;
        }else {
            return get(num - 1) + get(num - 2);
        }
    }
}
