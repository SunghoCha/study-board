package study.board.comment.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestClient;
import study.board.comment.service.response.CommentPageResponseV2;
import study.board.comment.service.response.CommentResponse;
import study.board.comment.service.response.CommentResponseV2;

import java.util.List;

public class CommentApiV2Test {
    RestClient restClient = RestClient.create("http://localhost:9001");

    @Test
    void create() {
        CommentResponseV2 response1 = create(new CommentCreateRequestV2(1L, "my comment1", null, 1L));
        CommentResponseV2 response2 = create(new CommentCreateRequestV2(1L, "my comment2", response1.getPath(), 1L));
        CommentResponseV2 response3 = create(new CommentCreateRequestV2(1L, "my comment3", response2.getPath(), 1L));

        System.out.println("response1.getPath() = " + response1.getPath());
        System.out.println("response1.getCommentId() = " + response1.getCommentId());
        System.out.println("\tresponse2.getPath() = " + response2.getPath());
        System.out.println("\tresponse2.getCommentId() = " + response2.getCommentId());
        System.out.println("\t\tresponse3.getPath() = " + response3.getPath());
        System.out.println("\t\tresponse3.getCommentId() = " + response3.getCommentId());

        /*
            response1.getPath() = 00000
            response1.getCommentId() = 153838263422504960
                response2.getPath() = 0000000000
                response2.getCommentId() = 153838263682551808
                    response3.getPath() = 000000000000000
                    response3.getCommentId() = 153838263770632192
         */
    }

    @Test
    void read() {
        CommentResponseV2 response = restClient.get()
                .uri("/v2/comments/{commentId}", 153838263422504960L)
                .retrieve()
                .body(CommentResponseV2.class);
        System.out.println("response = " + response);
    }

    @Test
    void delete() {
        restClient.delete()
                .uri("/v2/comments/{commentId}", 153838263422504960L)
                .retrieve();
    }

    @Test
    void readAll() {
        CommentPageResponseV2 response = restClient.get()
                .uri("/v2/comments?articleId=1&pageSize=10&page=50000")
                .retrieve()
                .body(CommentPageResponseV2.class);

        System.out.println("response.getCommentCount() = " + response.getCommentCount());
        for (CommentResponseV2 comment : response.getComments()) {
            System.out.println("comment.getCommentId() = " + comment.getCommentId());
        }
    }

    @Test
    void readAllInfiniteScroll() {
        List<CommentResponseV2> responses1 = restClient.get()
                .uri("/v2/comments/infinite-scroll?articleId=1&pageSize=5")
                .retrieve()
                .body(new ParameterizedTypeReference<List<CommentResponseV2>>() {
                });

        System.out.println("firstPage");
        for (CommentResponseV2 response : responses1) {
            System.out.println("response.getCommentId() = " + response.getCommentId());
        }

        String lastPath = responses1.get(responses1.size() - 1).getPath();
        List<CommentResponseV2> responses2 = restClient.get()
                .uri("/v2/comments/infinite-scroll?articleId=1&pageSize=5&lastPath=%s".formatted(lastPath))
                .retrieve()
                .body(new ParameterizedTypeReference<List<CommentResponseV2>>() {
                });

        System.out.println("secondPage");
        for (CommentResponseV2 response : responses2) {
            System.out.println("response.getCommentId() = " + response.getCommentId());
        }
    }


    CommentResponseV2 create(CommentCreateRequestV2 request) {
        return restClient.post()
                .uri("/v2/comments")
                .body(request)
                .retrieve()
                .body(CommentResponseV2.class);
    }

    @Getter
    @AllArgsConstructor
    public static class CommentCreateRequestV2 {
        private Long articleId;
        private String content;
        private String parentPath;
        private Long writerId;
    }
}
