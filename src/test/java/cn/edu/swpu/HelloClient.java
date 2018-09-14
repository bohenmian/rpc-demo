package cn.edu.swpu;

import cn.edu.swpu.client.RpcProxy;
import cn.edu.swpu.model.Person;
import cn.edu.swpu.service.HelloService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class HelloClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(HelloClient.class);

    public static void main(String[] args) throws InterruptedException {
        ApplicationContext context = new ClassPathXmlApplicationContext("client.xml");

        RpcProxy proxy = context.getBean(RpcProxy.class);

        HelloService helloService = proxy.create(HelloService.class);

        String result = helloService.hello("World");

        LOGGER.info(result);

        String helloPerson = helloService.hello(new Person("Jack", "Melo"));

        LOGGER.info(helloPerson);
    }
}
