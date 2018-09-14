package cn.edu.swpu.client;

import cn.edu.swpu.protocol.RpcRequest;
import cn.edu.swpu.protocol.RpcResponse;
import cn.edu.swpu.zookeeper.ServiceDiscovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Proxy;
import java.util.UUID;

public class RpcProxy {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcProxy.class);

    private String serverAddress;
    private ServiceDiscovery serviceDiscovery;

    public RpcProxy(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    @SuppressWarnings("unchecked")
    public <T> T create(Class<?> interfaceClass) {
        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                (proxy, method, args) -> {
                    RpcRequest request = new RpcRequest();
                    request.setRequestId(UUID.randomUUID().toString());
                    request.setClassName(method.getDeclaringClass().getName());
                    request.setMethodName(method.getName());
                    request.setParameters(args);
                    request.setParameterTypes(method.getParameterTypes());
                    if (serviceDiscovery != null) {
                        serverAddress = serviceDiscovery.discover();
                    }
                    LOGGER.info("server address {}", serverAddress);
                    String host = serverAddress.split(":")[0];
                    int port = Integer.parseInt(serverAddress.split(":")[1]);
                    RpcClient client = new RpcClient(host, port);
                    RpcResponse response = client.send(request);
                    if (response == null) {
                        throw new RuntimeException("response is null");
                    }
                    if (response.hasThrowable()) {
                        throw response.getError();
                    } else {
                        return response.getResult();
                    }
                }
        );
    }
}
