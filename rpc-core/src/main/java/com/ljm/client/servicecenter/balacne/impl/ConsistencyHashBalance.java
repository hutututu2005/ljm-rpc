package com.ljm.client.servicecenter.balacne.impl;


import com.ljm.client.servicecenter.balacne.LoadBalance;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @ClassName ConsistencyHashBalance
 * @Description 一致性哈希算法
 * @Author ljm
 */
@Slf4j
public class ConsistencyHashBalance implements LoadBalance {

    // 虚拟节点的个数
    private static final int VIRTUAL_NUM = 5;

    // 虚拟节点分配，key是hash值，value是虚拟节点服务器名称,使用线程安全的map
    private final ConcurrentSkipListMap<Integer, String> virtualnodes = new ConcurrentSkipListMap<>();

    // 真实节点列表
    private final List<String> realNodes = new CopyOnWriteArrayList<>();
    //读写锁
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    // 获取虚拟节点的个数
    public static int getVirtualNum() {
        return VIRTUAL_NUM;
    }

    // 初始化虚拟节点
    public void init(List<String> serviceList) {
        if (serviceList == null || serviceList.isEmpty()) {
         return;
        }
        //上锁
        lock.writeLock().lock();
        try {
            //清空现有节点
            virtualnodes.clear();
            realNodes.clear();

            for (String server : serviceList) {
                realNodes.add(server);
                log.info("真实节点[{}] 被添加", server);
                for (int i = 0; i < VIRTUAL_NUM; i++) {
                    String virtualNode = server + "&&VN" + i;
                    int hash = getHash(virtualNode);
                    virtualnodes.put(hash, virtualNode);
                    log.info("虚拟节点[{}] hash:{}，被添加", virtualNode, hash);
                }
            }
        } finally {
            //释放锁
           lock.writeLock().unlock();
        }
    }

    /**
     * 获取被分配的节点名
     *
     * @param node 请求的节点（通常是请求的唯一标识符）
     * @return 负责该请求的真实节点名称
     */
    public String getServer(String node, List<String> serviceList) {
        if (virtualnodes.isEmpty()) {
            log.warn("虚拟节点列表为空，请先初始化");
            return null;
        }

        int hash = getHash(node);
        // 使用floorEntry方法找到小于等于给定hash的最大键值对，如果没有则返回null
        Map.Entry<Integer, String> entry = virtualnodes.ceilingEntry(hash);
        if(entry == null) {
            // 如果没有找到，返回第一个节点（哈希闭环）
            entry = virtualnodes.firstEntry();
        }
        String virtualnode = entry.getValue();
        return virtualnode.substring(0, virtualnode.indexOf("&&"));
    }

    /**
     * 添加节点
     *
     * @param node 新加入的节点
     */
    public void addNode(String node) {
        if (node == null || realNodes.contains(node)) {
            return;
        }

        lock.writeLock().lock();
        try {
            realNodes.add(node);
            log.info("真实节点[{}] 上线添加", node);
            for (int i = 0; i < VIRTUAL_NUM; i++) {
                String virtualNode = node + "&&VN" + i;
                int hash = getHash(virtualNode);
                virtualnodes.put(hash, virtualNode);
                log.info("虚拟节点[{}] hash:{}，被添加", virtualNode, hash);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 删除节点
     *
     * @param node 被移除的节点
     */
    public void delNode(String node) {
        if (node == null || !realNodes.contains(node)) {
            return;
        }

        lock.writeLock().lock();
        try {
            realNodes.remove(node);
            log.info("真实节点[{}] 下线移除", node);
            for (int i = 0; i < VIRTUAL_NUM; i++) {
                String virtualNode = node + "&&VN" + i;
                int hash = getHash(virtualNode);
                virtualnodes.remove(hash);
                log.info("虚拟节点[{}] hash:{}，被移除", virtualNode, hash);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }


    /**
     * FNV1_32_HASH算法
     */
    private static int getHash(String str) {
        final int p = 16777619;
        int hash = (int) 2166136261L;
        for (int i = 0; i < str.length(); i++)
            hash = (hash ^ str.charAt(i)) * p;
        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;
        // 如果算出来的值为负数则取其绝对值
        if (hash < 0)
            hash = Math.abs(hash);
        return hash;
    }

    @Override
    public String balance(List<String> addressList) {
        // 如果 addressList 为空或 null，抛出 IllegalArgumentException
        if (addressList == null || addressList.isEmpty()) {
            throw new IllegalArgumentException("Address list cannot be null or empty");
        }
        // 检查当前节点列表是否与传入的服务列表一致
        boolean needRefresh = !new HashSet<>(realNodes).equals(new HashSet<>(addressList));

        if (needRefresh) {
            init(addressList);
        }
        // 使用UUID作为请求的唯一标识符来进行一致性哈希
        String random = UUID.randomUUID().toString();
        return getServer(random, addressList);
    }
    public SortedMap<Integer, String> getVirtualnodes() {
        return virtualnodes;
    }

    public List<String> getRealNodes() {
        return realNodes;
    }
    @Override
    public String toString() {
        return "ConsistencyHash";
    }
}

