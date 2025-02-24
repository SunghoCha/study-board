package study.board.article.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import study.board.article.service.request.ArticleCreateRequest;
import study.board.article.service.response.ArticleResponse;

public class ArticleApiTest {
    RestClient restClient = RestClient.create("http://localhost:9000");

    @Test
    @DisplayName("")
    void createTest() {
        ArticleResponse response = create(new ArticleCreateRequest("my title", "my content", 1L, 1L));
        System.out.println("response = " + response);
    }

    ArticleResponse create(ArticleCreateRequest request) {
        return restClient.post()
                .uri("/v1/articles")
                .body(request)
                .retrieve()
                .body(ArticleResponse.class);
    }

    @Test
    void readTest() {
        ArticleResponse response = read(152493632004263936L);
        System.out.println("response = " + response);
    }

    ArticleResponse read(Long articleId) {
        return restClient.get()
                .uri("/v1/articles/{articleId}", articleId)
                .retrieve()
                .body(ArticleResponse.class);
    }

    @Test
    void updateTest() {
        update(152493632004263936L);
        ArticleResponse response = read(152493632004263936L);
        System.out.println("response = " + response);
    }

    void update(Long articleId) {
        restClient.patch()
                .uri("/v1/articles/{articleId}", articleId)
                .body(new ArticleUpdateRequest("my title 2", "my content 2"))
                .retrieve();
    }

    @Test
    void deleteTest() {
        restClient.delete()
                .uri("/v1/articles/{articleId}", 152493632004263936L)
                .retrieve();
    }

    @Getter
    @AllArgsConstructor
    static class ArticleCreateRequest {
        private String title;
        private String content;
        private Long writerId;
        private Long boardId;
    }

    @Getter
    @AllArgsConstructor
     static class ArticleUpdateRequest {
        private String title;
        private String content;
    }

}
