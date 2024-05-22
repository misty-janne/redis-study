package com.example.stock.service;

import com.example.stock.domain.Stock;
import com.example.stock.repository.StockRepository;
//import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
public class StockService {

    private final StockRepository stockRepository;
    private final Object lock = new Object(); // 잠금 객체

    public StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    /* 해결방법 (1) 잠금 객체 활용
    @Transactional
    public void decrease(Long id, Long quantity) {
        synchronized (lock) { // 잠금 객체를 사용하여 동기화 블록 설정
            Stock stock = stockRepository.findById(id).orElseThrow();
            stock.decrease(quantity);

            stockRepository.saveAndFlush(stock);
        }
    }
     */

    /* 해결방법 (2) 비관적 잠금 +타임아웃으로 교착상태 방지
    @Transactional
    public void decrease(Long id, Long quantity) {
        Stock stock = stockRepository.findByIdWithPessimisticLock(id);
        stock.decrease(quantity);

        stockRepository.saveAndFlush(stock);
    }
    */


    /* 해결방법 (3) 네임드락 --부모의 트랜잭션과 별도로 실행되어야 하므로 propagation 변경 */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void decrease(Long id, Long quantity) {
        Stock stock = stockRepository.findById(id).orElseThrow();
        stock.decrease(quantity);

        stockRepository.saveAndFlush(stock);
    }

}
