package com.practice.springbasic.controller.comment;

import com.practice.springbasic.config.jwt.JwtProperties;
import com.practice.springbasic.config.jwt.JwtUtils;
import com.practice.springbasic.controller.comment.dto.ReturnSingleCommentForm;
import com.practice.springbasic.controller.form.SuccessResult;
import com.practice.springbasic.controller.utils.CheckUtil;
import com.practice.springbasic.domain.board.Board;
import com.practice.springbasic.domain.comment.Comment;
import com.practice.springbasic.domain.comment.dto.CommentUpdateDto;
import com.practice.springbasic.domain.member.Member;
import com.practice.springbasic.service.board.BoardService;
import com.practice.springbasic.service.comment.CommentService;
import com.practice.springbasic.service.comment.dto.CommentDto;
import com.practice.springbasic.service.member.MemberService;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
public class CommentControllerImpl implements CommentController{
    private final BoardService boardService;
    private final MemberService memberService;
    private final CommentService commentService;

    public CommentControllerImpl(BoardService boardService, MemberService memberService, CommentService commentService) {
        this.boardService = boardService;
        this.memberService = memberService;
        this.commentService = commentService;
    }

    @Override
    @PostMapping("/boards/{boardId}/comments")
    public SuccessResult postComment(HttpServletRequest request, @PathVariable Long boardId, @RequestBody @Validated CommentDto commentDto, BindingResult bindingResult) {
        CheckUtil.bindingResultCheck(bindingResult.hasErrors());
        Long memberId = JwtUtils.verifyJwtToken(request, JwtProperties.ACCESS_HEADER_STRING);
        Member member = memberService.findMemberById(memberId).orElse(null);
        Board board = boardService.findBoard(boardId).orElse(null);
        CheckUtil.nullCheck(member);
        CheckUtil.nullCheck(board);

        Comment comment = commentService.postComment(member, board, commentDto);

        return new SuccessResult(new ReturnSingleCommentForm(comment));
    }

    @Override
    @GetMapping("/boards/{boardId}/comments/{commentId}")
    public SuccessResult getComment(@PathVariable Long boardId, @PathVariable Long commentId) {
        Comment comment = commentService.findComment(commentId).orElse(null);
        CheckUtil.nullCheck(comment);
        return new SuccessResult(new ReturnSingleCommentForm(comment));
    }

    @Override
    @PutMapping("/boards/{boardId}/comments/{commentId}")
    public SuccessResult updateComment(HttpServletRequest request,@PathVariable Long boardId, @RequestBody @Validated CommentUpdateDto commentUpdateDto, @PathVariable Long commentId, BindingResult bindingResult) {
        CheckUtil.bindingResultCheck(bindingResult.hasErrors());
        JwtUtils.verifyJwtToken(request, JwtProperties.ACCESS_HEADER_STRING);
        String email = JwtUtils.getTokenEmail(request, JwtProperties.ACCESS_HEADER_STRING);
        Comment preComment = commentService.findComment(commentId).orElse(null);
        CheckUtil.nullCheck(preComment);
        memberEmailAndCommentEmailSameCheck(email, preComment);
        Comment comment = commentService.updateComment(preComment, commentUpdateDto);
        return new SuccessResult(new ReturnSingleCommentForm(comment));
    }

    @Override
    @DeleteMapping("/boards/{boardId}/comments/{commentId}")
    public SuccessResult deleteComment(HttpServletRequest request,@PathVariable Long boardId, @PathVariable Long commentId) {
        JwtUtils.verifyJwtToken(request, JwtProperties.ACCESS_HEADER_STRING);
        String email = JwtUtils.getTokenEmail(request, JwtProperties.ACCESS_HEADER_STRING);
        Comment comment = commentService.findComment(commentId).orElse(null);
        CheckUtil.nullCheck(comment);
        memberEmailAndCommentEmailSameCheck(email, comment);
        commentService.deleteComment(comment);
        return new SuccessResult(null);
    }
    private static void memberEmailAndCommentEmailSameCheck(String email, Comment comment) {
        if(!email.equals(comment.getMember().getEmail())) {
            throw new RuntimeException("정상적이지 않은 접근");
        }
    }
}