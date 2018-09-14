package cn.edu.swpu.service.impl;

import cn.edu.swpu.annotation.RpcService;
import cn.edu.swpu.model.Person;
import cn.edu.swpu.service.HelloService;

@RpcService(HelloService.class)
public class HelloServiceImpl implements HelloService {

    @Override
    public String hello(String name) {
        return "Hello!" + name;
    }

    @Override
    public String hello(Person person) {
        return "Hello!" + person.getFirstName() + person.getLastName();
    }
}
