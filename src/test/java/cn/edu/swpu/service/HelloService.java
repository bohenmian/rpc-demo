package cn.edu.swpu.service;

import cn.edu.swpu.model.Person;

public interface HelloService {

    String hello(String name);

    String hello(Person person);

}
