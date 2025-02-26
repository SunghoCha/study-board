package study.board.article.data;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;
import study.board.article.entity.Article;
import study.board.common.snowflake.Snowflake;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

@Slf4j
@SpringBootTest
public class DataInitializer {
    @PersistenceContext
    EntityManager entityManager;
    @Autowired
    TransactionTemplate transactionTemplate;
    Snowflake snowflake = new Snowflake();

    static final int BULK_INSERT_SIZE = 2000;
    static final int EXECUTE_COUNT = 6000;

    @Test
    void initialize() {

        ExecutorService executorService = Executors.newFixedThreadPool(3);
        List<CompletableFuture<Void>> futures = IntStream.range(0, EXECUTE_COUNT)
                .mapToObj(i -> {
                    System.out.println("i = " + i);
                    return CompletableFuture.runAsync(this::insert, executorService);
                })
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executorService.shutdown();
    }

    private void insert() {
        transactionTemplate.executeWithoutResult(status -> {
            for(int i = 1; i <= BULK_INSERT_SIZE; i++) {
                Article article = Article.create(
                        snowflake.nextId(),
                        "title" + i,
                        "content" + i,
                        1L,
                        1L
                );
                entityManager.persist(article);

                if (i % 100 == 0) {
                    entityManager.flush();
                    entityManager.clear();
                }
            }
        });
    }

}
