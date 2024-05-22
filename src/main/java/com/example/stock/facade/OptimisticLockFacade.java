package com.example.stock.facade;

import com.example.stock.service.OptimisticLockStockService;
import org.springframework.stereotype.Component;

@Component
public class OptimisticLockFacade {

    private final OptimisticLockStockService optimisticLockStockService;

    public OptimisticLockFacade(OptimisticLockStockService optimisticLockStockService) {
        this.optimisticLockStockService = optimisticLockStockService;
    }

    public void decrease(Long id, Long quantity) throws InterruptedException {
        //버전 확인후 업데이트 실패시에 재시도 해야하므로
        while (true) {
            try {
                optimisticLockStockService.decrease(id, quantity);

                break; //정상업데이트 된다면 빠져나오기
            } catch (Exception e) {
                Thread.sleep(50); //수량감소에 실패시 50ms 후 재시도
            }
        }
    }
}
