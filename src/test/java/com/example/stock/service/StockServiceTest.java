package com.example.stock.service;

import static org.junit.jupiter.api.Assertions.*;
import com.example.stock.domain.Stock;
import com.example.stock.repository.StockRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
public class StockServiceTest {
    @Autowired
    private PessimisticLockStockService stockService;
    @Autowired
    private StockRepository stockRepository;

    @BeforeEach
    public void before() {
        stockRepository.saveAndFlush(new Stock(1L, 100L));
    }

    @AfterEach
    public void after() {
        stockRepository.deleteAll();
    }

    @Test
    public void gamso() {
        stockService.decrease(1L, 1L);

        //100 -1 = 99
        Stock stock = stockRepository.findById(1L).orElseThrow();

        assertEquals(99, stock.getQuantity());
    }

    @Test
    public void simultaneousDecrease() throws InterruptedException {
        int threadCount = 100;

        //멀티스레드
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        //스레드 수행 끝날때까지 기다리기
        CountDownLatch latch = new CountDownLatch(threadCount);

        //100개의 수량에 대해서 한개씩 100번 감소시키기
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    stockService.decrease(1L, 1L);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        Stock stock = stockRepository.findById(1L).orElseThrow();
        //100 - (1*100) = 0
        assertEquals(0, stock.getQuantity());

        //레이스 컨디션 발생 -->> synchronized 적용 -->> 트랜잭션으로 인한 교착상태 발생
        // 해결방법1) 트랜잭션 어노테이션이 적용된 메서드 내에서 동기화가 필요한 블록만 별도로 synchronized 블록으로 감싸기
        //  => 트랜잭션 관리와 스레드 안정성을 동시에 확보 -> 하지만 결국 레이스 컨디션을 해결할수 없었음
        // 해결방법2) 비관적 잠금 구현
        //  => 교착상태 발생우려가 있음 -> 타임아웃 설정(3초)으로 교착상태 방지
    }
}
