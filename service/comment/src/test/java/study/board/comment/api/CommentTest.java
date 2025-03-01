package study.board.comment.api;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import study.board.comment.entity.Comment;
import study.board.comment.repository.CommentRepository;
import study.board.comment.service.CommentService;
import study.board.common.snowflake.Snowflake;

// 영속성 컨텍스트 체크용 임시 테스트
@ActiveProfiles("test")
@SpringBootTest
public class CommentTest {
    
    Snowflake snowflake = new Snowflake();

    @Autowired
    CommentRepository commentRepository;
    
    @Autowired
    CommentService commentService;

    @Test
    void test() {
        Comment parentComment = Comment.create(
                snowflake.nextId(), 
                "content1",
                null,
                1L,
                1L
        );
        Comment comment = Comment.create(
                snowflake.nextId(),
                "content2",
                parentComment.getCommentId(),
                1L,
                1L
        );

        commentRepository.save(parentComment);
        commentRepository.save(comment);
        
        // when
        commentService.delete(parentComment.getCommentId());
        commentService.delete(comment.getCommentId());
        
        // then
        Assertions.assertThat(commentRepository.findById(parentComment.getCommentId())).isEmpty();
    }
}
