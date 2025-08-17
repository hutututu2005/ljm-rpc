package com.ljm.client.servicecenter.balacne.impl;


/**
 * @ClassName ConsistencyHashBalance
 * @Description 一致性哈希算法
 * @Author ljm
 */

import com.ljm.client.servicecenter.balacne.LoadBalance;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * 一致性哈希算法本质上也是一种取模算法。只不过普通取模算法是按服务器数量取模，而一致性哈希算法是对固定值2^32取模，
 * 这就使得一致性算法具备良好的单调性：不管集群中有多少个节点，只要key值固定，那所请求的服务器节点也同样是固定的。其算法的工作原理如下：
 * 1. 一致性哈希算法将整个哈希值空间映射成一个虚拟的圆环，整个哈希空间的取值范围为0~2^32-1；
 * 2. 计算各服务器节点的哈希值，并映射到哈希环上；
 * 3. 将服务发来的数据请求使用哈希算法算出对应的哈希值；
 * 4. 将计算的哈希值映射到哈希环上，同时沿圆环顺时针方向查找，遇到的第一台服务器就是所对应的处理请求服务器。
 * 5. 当增加或者删除一台服务器时，受影响的数据仅仅是新添加或删除的服务器到其环空间中前一台的服务器（也就是顺着逆时针方向遇到的第一台服务器）之间的数据，其他都不会受到影响。
 */
@Slf4j
public class ConsistencyHashBalance implements LoadBalance {

    // 虚拟节点的个数
    private static final int VIRTUAL_NUM = 5;

    // 虚拟节点分配，key是hash值，value是虚拟节点服务器名称
    private SortedMap<Integer, String> shards = new TreeMap<Integer,String>();

    // 真实节点列表
    private List<String> realNodes = new LinkedList<>();

    // 获取虚拟节点的个数
    public static int getVirtualNum() {
        return VIRTUAL_NUM;
    }

    // 初始化虚拟节点
    public void init(List<String> serviceList) {
        for (String server : serviceList) {
            realNodes.add(server);
            log.info("真实节点[{}] 被添加", server);
            for (int i = 0; i < VIRTUAL_NUM; i++) {
                String virtualNode = server + "&&VN" + i;
                int hash = getHash(virtualNode);
                shards.put(hash, virtualNode);
                log.info("虚拟节点[{}] hash:{}，被添加", virtualNode, hash);
            }
        }
    }

    /**
     * 获取被分配的节点名
     *
     * @param node 请求的节点（通常是请求的唯一标识符）
     * @return 负责该请求的真实节点名称
     */
    public synchronized String getServer(String node, List<String> serviceList) {
        if (shards.isEmpty()) {
            init(serviceList);  // 初始化，如果shards为空
        }

        int hash = getHash(node);
        Integer key = null;

        SortedMap<Integer, String> subMap = shards.tailMap(hash);
        if (subMap.isEmpty()) {
            key = shards.firstKey();  // 如果没有大于该hash的节点，则返回最小的hash值
        } else {
            key = subMap.firstKey();
        }

        String virtualNode = shards.get(key);
        return virtualNode.substring(0, virtualNode.indexOf("&&"));
    }

    /**
     * 添加节点
     *
     * @param node 新加入的节点
     */
    public void addNode(String node) {
        if (!realNodes.contains(node)) {
            realNodes.add(node);
            log.info("真实节点[{}] 上线添加", node);
            for (int i = 0; i < VIRTUAL_NUM; i++) {
                String virtualNode = node + "&&VN" + i;
                int hash = getHash(virtualNode);
                shards.put(hash, virtualNode);
                log.info("虚拟节点[{}] hash:{}，被添加", virtualNode, hash);
            }
        }
    }

    /**
     * 删除节点
     *
     * @param node 被移除的节点
     */
    public void delNode(String node) {
        if (realNodes.contains(node)) {
            realNodes.remove(node);
            log.info("真实节点[{}] 下线移除", node);
            for (int i = 0; i < VIRTUAL_NUM; i++) {
                String virtualNode = node + "&&VN" + i;
                int hash = getHash(virtualNode);
                shards.remove(hash);
                log.info("虚拟节点[{}] hash:{}，被移除", virtualNode, hash);
            }
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

        // 使用UUID作为请求的唯一标识符来进行一致性哈希
        String random = UUID.randomUUID().toString();
        return getServer(random, addressList);
    }
    public SortedMap<Integer, String> getShards() {
        return shards;
    }

    public List<String> getRealNodes() {
        return realNodes;
    }
    @Override
    public String toString() {
        return "ConsistencyHash";
    }
}





//todo 以下代码并发时存在空指针异常，待优化
/*import com.ljm.client.servicecenter.balacne.LoadBalance;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
public class ConsistencyHashBalance implements LoadBalance {

    // 虚拟节点的目的是提高负载均衡的均匀性
    private static final int VIRTUAL_NUM = 5;

    // 虚拟节点分配，key是hash值，value是虚拟节点服务器名称,使用线程安全的map   跳表支持有序，且范围查询方便
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
     *//**
 * 获取被分配的节点名
 *
 * @param node 请求的节点（通常是请求的唯一标识符）
 * @return 负责该请求的真实节点名称
 *//*

    public String getServer(String node, List<String> serviceList) {
        if (virtualnodes.isEmpty()) {
            log.warn("虚拟节点列表为空，请先初始化");
            return null;
        }

        int hash = getHash(node);
        // 找到大于等于给定hash的最大键值对，如果没有则返回null
        Map.Entry<Integer, String> entry = virtualnodes.ceilingEntry(hash);
        if(entry == null) {
            // 如果没有找到，返回第一个节点（哈希闭环）
            entry = virtualnodes.firstEntry();
        }
        String virtualnode = entry.getValue();
        return virtualnode.substring(0, virtualnode.indexOf("&&"));
    }

    *//**
 * 添加节点
 *
 * @param node 新加入的节点
 *//*

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
     *//**
 * 删除节点
 *
 * @param node 被移除的节点
 *//*

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
    *//*
 * FNV1_32_HASH算法
 *//*

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
        // 先使用读锁检查是否需要刷新
        lock.readLock().lock();
        boolean needRefresh;
        try {
            // 优化：通过比较大小和包含关系来判断，避免创建HashSet
            needRefresh = realNodes.size() != addressList.size() ||
                    !addressList.containsAll(realNodes) ||
                    !realNodes.containsAll(addressList);
        } finally {
            lock.readLock().unlock();
        }


        if (needRefresh) {
            init(addressList);
        }
        // 使用UUID作为请求的唯一标识符来进行一致性哈希
        // 真实生产环境需要用客户端ip进行映射
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
}*/
