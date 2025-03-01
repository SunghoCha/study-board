package study.board.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import study.board.comment.entity.Comment;
import study.board.comment.repository.CommentRepository;
import study.board.comment.service.request.CommentCreateRequest;
import study.board.comment.service.response.CommentPageResponse;
import study.board.comment.service.response.CommentResponse;
import study.board.common.snowflake.Snowflake;

import java.util.List;

import static java.util.function.Predicate.not;

@Service
@RequiredArgsConstructor
public class CommentService {
    private static Long MOVABLE_PAGE_SIZE = 10L;
    private static Long DEPTH = 2L;
    private final CommentRepository commentRepository;
    private final Snowflake snowflake = new Snowflake();

    @Transactional
    public CommentResponse create(CommentCreateRequest request) {
        Comment parent = findParent(request);
        Comment comment = commentRepository.save(
                Comment.create(
                        snowflake.nextId(),
                        request.getContent(),
                        parent == null ? null : parent.getCommentId(),
                        request.getArticleId(),
                        request.getWriterId()
                )
        );
        return CommentResponse.from(comment);
    }

    public CommentResponse read(Long commentId) {
        return CommentResponse.from(
                commentRepository.findById(commentId).orElseThrow()
        );
    }

    @Transactional
    public void delete(Long commentId) {
        commentRepository.findById(commentId)
                .filter(not(Comment::getDeleted))
                .ifPresent(comment -> {
                    if (hasChildren(comment)) {
                        comment.delete();
                    } else {
                        delete(comment);
                    }
                });
    }

    private boolean hasChildren(Comment comment) { // depth 2이고
        return commentRepository.countBy(comment.getArticleId(), comment.getCommentId(), DEPTH).equals(DEPTH);
    }

    private void delete(Comment comment) {
        commentRepository.delete(comment);
        if (!comment.isRoot()) { // 상위 댓글이 삭제처리되었지만 하위 댓글로 인해 지워지지 못하고 있었던 경우 삭제처리
            commentRepository.findById(comment.getParentCommentId())
                    .filter(Comment::getDeleted) // 삭제처리된 케이스
                    .filter(not(this::hasChildren)) // 더이상 하위 댓글을 가지고 있지 않은 케이스
                    .ifPresent(this::delete); // 재귀적 호출
        }
    }

    private Comment findParent(CommentCreateRequest request) {
        Long parentCommentId = request.getParentCommentId();
        if (parentCommentId == null) {
            return null;
        }
        return commentRepository.findById(parentCommentId)
                .filter(not(Comment::getDeleted))
                .filter(Comment::isRoot)
                .orElseThrow();
    }

    public CommentPageResponse readAll(Long articleId, Long page, Long pageSize) {
        return CommentPageResponse.of(
                commentRepository.findAll(articleId, (page - 1) * pageSize, pageSize).stream()
                        .map(CommentResponse::from)
                        .toList(),
                commentRepository.count(articleId, PageLimitCalculator.calculatePageLimit(page, pageSize, MOVABLE_PAGE_SIZE))
        );
    }

    public List<CommentResponse> readAll(Long articleId, Long lastParentCommentId, Long lastCommentId, Long limit) {
        List<Comment> comments = (lastParentCommentId == null || lastCommentId == null) ?
                commentRepository.findAllInfiniteScroll(articleId, limit) :
                commentRepository.findAllInfiniteScroll(articleId, lastParentCommentId, lastCommentId, limit);

        return comments.stream()
                .map(CommentResponse::from)
                .toList();
    }
}
