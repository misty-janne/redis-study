package com.example.stock.facade;

import com.example.stock.repository.RedisLockRepository;
import com.example.stock.service.StockService;
import org.springframework.stereotype.Component;

@Component
public class LettuceLockStockFacade {

    //redis로 락/언락 수행
    private final RedisLockRepository redisLockRepository;
    private final StockService stockService;

    public LettuceLockStockFacade(RedisLockRepository redisLockRepository, StockService stockService) {
        this.redisLockRepository = redisLockRepository;
        this.stockService = stockService;
    }

    public void decrease(Long id, Long quantity) throws InterruptedException {
        //락 획득 시도, 실패시 100m 스레드 슬립후 획득 재시도(부하 줄이기)
        while (!redisLockRepository.lock(id)) {
            Thread.sleep(100);
        }

        //락 획득 성공시 재고 감소 수행
        try {
            stockService.decrease(id, quantity);
        } finally {
            //최종적으로 락 해제
            redisLockRepository.unlock(id);
        }
    }
}
