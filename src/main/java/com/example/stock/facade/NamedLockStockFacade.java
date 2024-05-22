package com.example.stock.facade;

import com.example.stock.repository.LockRepository;
import com.example.stock.service.StockService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

@Component
public class NamedLockStockFacade {

    private final LockRepository lockRepository;

    private final StockService stockService;

    public NamedLockStockFacade(LockRepository lockRepository, StockService stockService) {
        this.lockRepository = lockRepository;
        this.stockService = stockService;
    }

    @Transactional
    public void decrease(Long id, Long quantity) {
        try {
            //락 획득
            lockRepository.getLock(id.toString());
            //재고 감소
            stockService.decrease(id, quantity);
        } finally {
            //종료후 락 해제
            lockRepository.releaseLock(id.toString());
        }
    }
}
