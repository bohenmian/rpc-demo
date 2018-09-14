package cn.edu.swpu.zookeeper;

import cn.edu.swpu.constant.ZookeeperConstant;
import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

// 使用ZooKeeper实现服务的注册(将服务注册到ZK)
public class ServiceRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceRegistry.class);

    private CountDownLatch countDownLatch = new CountDownLatch(1);

    private String registryAddress;

    public ServiceRegistry(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public void registry(String data) {
        if (data != null) {
            ZooKeeper zk = connectServer();
            if (zk != null) {
//                AddRootNode(zk);
                createNode(zk, data);
            }
        }
    }

    private ZooKeeper connectServer() {
        LOGGER.info("start connect to zookeeper");
        ZooKeeper zk = null;
        try {
            zk = new ZooKeeper(registryAddress, ZookeeperConstant.ZK_SESSION_TIMEOUT, event -> {
                if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    countDownLatch.countDown();
                }
            });
            countDownLatch.await();
        } catch (InterruptedException | IOException e) {
            LOGGER.error("", e);
        }
        return zk;
    }

//    private void AddRootNode(ZooKeeper zk) {
//        try {
//            // 服务对应path这个节点不存在
//            Stat stat = zk.exists(ZookeeperConstant.ZK_REGISTRY_PATH, false);
//            if (stat == null) {
//                // 添加这个服务的节点
//                zk.create(ZookeeperConstant.ZK_REGISTRY_PATH, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
//            }
//        } catch (InterruptedException | KeeperException e) {
//            LOGGER.error("", e.toString());
//        }
//    }

    private void createNode(ZooKeeper zk, String data) {
        try {
            byte[] bytes = data.getBytes();
            String path = zk.create(ZookeeperConstant.ZK_DATA_PATH, bytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            LOGGER.debug("create zookeeper node ({} => {})", path, data);
        } catch (KeeperException | InterruptedException e) {
            LOGGER.error("", e);
        }
    }
}

