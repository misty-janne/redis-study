package com.example.stock.facade;

import com.example.stock.service.StockService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedissonLockStockFacade {

    //락 획득에 사용할 레디슨 클라이언트
    private RedissonClient redissonClient;

    private StockService stockService;

    public RedissonLockStockFacade(RedissonClient redissonClient, StockService stockService) {
        this.redissonClient = redissonClient;
        this.stockService = stockService;
    }

    public void decrease(Long id, Long quantity) {
        RLock lock = redissonClient.getLock(id.toString());

        try {
            //10초 간 락획득을 시도, 1초 간 락을 점유
            boolean available = lock.tryLock(10, 1, TimeUnit.SECONDS);

            //락획득 실패시 로그 남긴후 리턴
            if (!available) {
                System.out.println("lock 획득 실패");
                return;
            }

            //정상 락 획득시 재고 감소 수행
            stockService.decrease(id, quantity);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            //로직 정상종료 후 락 해제
            lock.unlock();
        }
    }
}
