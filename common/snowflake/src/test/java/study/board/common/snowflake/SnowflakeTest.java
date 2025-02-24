package study.board.common.snowflake;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class SnowflakeTest {
    Snowflake snowflake = new Snowflake();

    @Test
    @DisplayName("Snowflake ID는 멀티스레드 환경에서도 고유하고 순차적으로 생성되어야 한다.")
    void nextIdTest() {
        // given
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        int repeatCount = 1000;
        int idCount = 1000;

        // when
        List<CompletableFuture<List<Long>>> futures = IntStream.range(0, repeatCount)
                .mapToObj(i -> CompletableFuture.supplyAsync(() -> generateIdList(snowflake, idCount), executorService))
                .toList();

        // then
        List<Long> result = futures.stream()
                .map(CompletableFuture::join)
                .peek(idList -> IntStream.range(1, idList.size())
                        .forEach(i -> assertThat(idList.get(i)).isGreaterThan(idList.get(i - 1)))
                )
                .flatMap(List::stream)
                .toList();

        assertThat(result.stream().distinct().count()).isEqualTo((long) repeatCount * idCount);

    }

    private List<Long> generateIdList(Snowflake snowflake, int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> snowflake.nextId())
                .toList();
    }

    @Test
    @DisplayName("비동기 ID 생성 성능 테스트")
    void nextIdPerformanceTest() {
        // given
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        int repeatCount = 1000;
        int idCount = 1000;

        // when
        long start = System.nanoTime();
        List<CompletableFuture<Void>> futures = IntStream.range(0, repeatCount)
                .mapToObj(i -> CompletableFuture.runAsync(() -> {
                    generateIdList(snowflake, idCount);
                }, executorService))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        long end = System.nanoTime();
        System.out.printf("times = %s ms%n", (end - start) / 1_000_000);

        executorService.shutdown();

        // 굳이 이 로직도 비동기로 처리해야하나? 성능차이는 없으면서 코드만 이해하기 힘든 느낌
//        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
//                .thenRun(() -> {
//                    long end = System.nanoTime();
//                    System.out.printf("times = %s ms%n", (end - start) / 1_000_000);
//
//                    executorService.shutdown();
//                }).join();

    }
}