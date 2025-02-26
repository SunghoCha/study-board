package study.board.article.repository;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import study.board.article.entity.Article;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class ArticleRepositoryTest {
    @Autowired
    ArticleRepository articleRepository;

    @Test
    @DisplayName("findAll 쿼리 테스트")
    void findAllTest() {
        // given
        List<Article> articles = articleRepository.findAll(1L, 1499970L, 30L);
        log.info("articles.size: {}", articles.size());
        articles.forEach(article -> log.info(article.toString()));
    }

    @Test
    @DisplayName("")
    void countTest() {
        Long count = articleRepository.count(1L, 10000L);
        log.info("count: {}", count);
    }

    @Test
    void findInfiniteScrollTest() {
        List<Article> articles = articleRepository.findAllInfiniteScroll(1L, 30L);
        articles.forEach(article -> log.info("articleId = {}", article.getArticleId()));

        Long lastArticleId = articles.get(articles.size() - 1).getArticleId();
        List<Article> newArticles = articleRepository.findAllInfiniteScroll(1L, 30L, lastArticleId);
        newArticles.forEach(article -> log.info("articleId = {}", article.getArticleId()));
    }


}