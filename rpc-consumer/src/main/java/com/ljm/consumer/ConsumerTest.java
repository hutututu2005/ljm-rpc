package com.ljm.consumer;


import com.ljm.client.proxy.ClientProxy;
import com.ljm.pojo.User;
import com.ljm.service.UserService;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @ClassName ConsumerExample
 * @Description 客户端测试
 * @Author ljm
 */
@Slf4j
public class ConsumerTest {

    private static final int THREAD_POOL_SIZE = 16;
    private static final ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

    public static void main(String[] args) throws InterruptedException {
        ClientProxy clientProxy = new ClientProxy();
        UserService proxy = clientProxy.getProxy(UserService.class);
        for (int i = 0; i < 120; i++) {
            final Integer i1 = i;
            if (i % 30 == 0) {
                // Simulate delay for every 30 requests
                Thread.sleep(10000);
            }

            // Submit tasks to executor service (thread pool)
            executorService.submit(() -> {
                try {
                    User user = proxy.getUserByUserId(i1);
                    if (user != null) {
                        log.info("从服务端得到的user={}", user);
                    } else {
                        log.warn("获取的 user 为 null, userId={}", i1);
                    }

                    Integer id = proxy.insertUserId(User.builder()
                            .id(i1)
                            .userName("User" + i1)
                            .gender(true)
                            .build());

                    if (id != null) {
                        log.info("向服务端插入user的id={}", id);
                    } else {
                        log.warn("插入失败，返回的id为null, userId={}", i1);
                    }
                } catch (Exception e) {
                    log.error("调用服务时发生异常，userId={}", i1, e);
                }
            });
        }

       /* User user = proxy.getUserByUserId(1);
        System.out.println("从服务端得到的user="+user.toString());

        User u= User.builder().id(100).userName("wxx").gender(true).build();
        Integer id = proxy.insertUserId(u);
        System.out.println("向服务端插入user的id"+id);*/


        // Gracefully shutdown the executor service
        executorService.shutdown();
        //优雅关闭，释放资源
        clientProxy.close();
    }

}
