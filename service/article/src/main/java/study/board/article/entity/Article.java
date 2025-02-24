package study.board.article.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Table(name = "article")
@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Article {

    @Id
    private Long articleId;
    private String title;
    private String content;
    private Long boardId; // 나중에 샤딩할때 key로 사용
    private Long writerId;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public static Article create(Long articleId, String title, String content, Long boardId, Long writerId) {
        Article article = new Article();
        article.articleId = articleId;
        article.title = title;
        article.content = content;
        article.boardId = boardId;
        article.writerId = writerId;
        article.createdAt = LocalDateTime.now();
        article.modifiedAt = article.createdAt;

        return article;
        // 빌드 패턴, dto로 전달하하는게 아닌, 필드값을 일일이 전달하면서 만드는 정적 팩토리 메서드가 좋은건가?
        // 엔티티니까 dto에 대해 알면 안되니까 보통 dto에서 toEntity()같은걸로 하던데 이런 정적 팩토리 메서드가 어떤 용도로 쓰이지?
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
        modifiedAt = LocalDateTime.now();
    }
}
