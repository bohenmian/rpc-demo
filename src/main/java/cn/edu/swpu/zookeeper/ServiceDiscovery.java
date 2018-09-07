package cn.edu.swpu.zookeeper;

import cn.edu.swpu.constant.ZookeeperConstant;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

// 从ZK上获取服务的地址
public class ServiceDiscovery {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceDiscovery.class);

    private CountDownLatch countDownLatch = new CountDownLatch(1);

    private volatile List<String> dataList = new ArrayList<>();

    private String registryAddress;

    public ServiceDiscovery(String registryAddress) {
        this.registryAddress = registryAddress;
        ZooKeeper zk = connectServer();
        if (zk == null) {
            watchNode(zk);
        }
    }

    private ZooKeeper connectServer() {
        ZooKeeper zk = null;
        try {
            zk = new ZooKeeper(registryAddress, ZookeeperConstant.ZK_SESSION_TIMEOUT, event -> {
                if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    countDownLatch.countDown();
                }
            });
            countDownLatch.await();
        } catch (InterruptedException | IOException e) {
            LOGGER.error(e.toString());
        }
        return zk;
    }

    private void watchNode(ZooKeeper zk) {
        try {
            // 如果node的状态发生变化,则重新获取服务的地址列表
            List<String> nodeList = zk.getChildren(ZookeeperConstant.ZK_REGISTRY_PATH, event -> {
                if (event.getType() == Watcher.Event.EventType.NodeChildrenChanged) {
                    watchNode(zk);
                }
            });
            List<String> dataList = new ArrayList<>();

            nodeList.forEach(node -> {
                try {
                    dataList.add(new String(zk.getData(ZookeeperConstant.ZK_REGISTRY_PATH + "/" + node, false, null)));
                } catch (KeeperException | InterruptedException e) {
                    LOGGER.error(e.toString());
                }
            });
            LOGGER.debug("node data: {}", dataList);
            this.dataList = dataList;
        } catch (InterruptedException | KeeperException e) {
            LOGGER.error(e.toString());
        }
    }
}
