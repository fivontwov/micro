package com.fivontwov.service;

import com.fivontwov.dto.*;
import com.fivontwov.grpc.UserGrpcClient;
import com.fivontwov.model.Comment;
import com.fivontwov.model.Topic;
import com.fivontwov.model.TopicVote;
import com.fivontwov.repo.CommentRepository;
import com.fivontwov.repo.TopicRepository;
import com.fivontwov.repo.TopicVoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fivontwov.user.proto.UserResponse;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TopicService {

    private final TopicRepository topicRepository;
    private final CommentRepository commentRepository;
    private final TopicVoteRepository voteRepository;
    private final UserGrpcClient userClient;

    public Topic createTopic(Long userId, String title, String body) {
        Optional<UserResponse> userOpt = userClient.getUserById(userId);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found with id: " + userId);
        }

        Topic t = new Topic();
        t.setUserId(userId);
        t.setTitle(title);
        t.setBody(body);
        return topicRepository.save(t);
    }

    public Comment addComment(Long topicId, AddCommentRequest req) {
        Optional<Topic> topic = topicRepository.findById(topicId);
        if (topic.isEmpty()) throw new IllegalArgumentException("Topic not found");

        // Verify user exists via gRPC
        Optional<UserResponse> userOpt = userClient.getUserById(req.getUserId());
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found with id: " + req.getUserId());
        }

        Comment c = new Comment();
        c.setTopicId(topicId);
        c.setParentCommentId(req.getParentCommentId());
        c.setUserId(req.getUserId());
        c.setBody(req.getBody());
        return commentRepository.save(c);
    }

    @Transactional
    public void vote(Long topicId, VoteRequest req) {
        // Verify user exists via gRPC
        Optional<UserResponse> userOpt = userClient.getUserById(req.getUserId());
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found with id: " + req.getUserId());
        }

        if (req.getValue() == null || (req.getValue() != 1 && req.getValue() != -1)) throw new IllegalArgumentException("Invalid vote value");
        Optional<Topic> t = topicRepository.findById(topicId);
        if (t.isEmpty()) throw new IllegalArgumentException("Topic not found");

        Optional<TopicVote> existing = voteRepository.findByUserIdAndTopicId(req.getUserId(), topicId);
        if (existing.isPresent()) {
            TopicVote ev = existing.get();
            if (ev.getValue().equals(req.getValue())) {
                // same vote, no-op (or we could remove)
                return;
            }
            ev.setValue(req.getValue());
            voteRepository.save(ev);
        } else {
            TopicVote v = new TopicVote();
            v.setTopicId(topicId);
            v.setUserId(req.getUserId());
            v.setValue(req.getValue());
            voteRepository.save(v);
        }
    }

    // Get topic with user information
    public Optional<TopicWithUserDTO> getTopicWithUser(Long topicId) {
        Optional<Topic> topicOpt = topicRepository.findById(topicId);
        if (topicOpt.isEmpty()) {
            return Optional.empty();
        }

        Topic topic = topicOpt.get();
        UserDTO creator = null;
        try {
            Optional<UserResponse> userOpt = userClient.getUserById(topic.getUserId());
            creator = userOpt.map(UserDTO::fromGrpcResponse).orElse(null);
        } catch (Exception e) {
            System.err.println("Failed to fetch user " + topic.getUserId() + ": " + e.getMessage());
        }

        return Optional.of(TopicWithUserDTO.fromTopic(topic, creator));
    }

    public List<TopicWithUserDTO> getAllTopicsWithUser() {
        List<Topic> topics = topicRepository.findAll();
        return topics.stream()
                .map(topic -> {
                    UserDTO creator = null;
                    try {
                        Optional<UserResponse> userOpt = userClient.getUserById(topic.getUserId());
                        creator = userOpt.map(UserDTO::fromGrpcResponse).orElse(null);
                    } catch (Exception e) {
                        // Log error but continue - return topic without user info
                        System.err.println("Failed to fetch user " + topic.getUserId() + ": " + e.getMessage());
                    }
                    return TopicWithUserDTO.fromTopic(topic, creator);
                })
                .collect(Collectors.toList());
    }

    public Optional<CommentWithUserDTO> getCommentWithUser(Long commentId) {
        Optional<Comment> commentOpt = commentRepository.findById(commentId);
        if (commentOpt.isEmpty()) {
            return Optional.empty();
        }

        Comment comment = commentOpt.get();
        UserDTO creator = null;
        try {
            Optional<UserResponse> userOpt = userClient.getUserById(comment.getUserId());
            creator = userOpt.map(UserDTO::fromGrpcResponse).orElse(null);
        } catch (Exception e) {
            System.err.println("Failed to fetch user " + comment.getUserId() + ": " + e.getMessage());
        }

        return Optional.of(CommentWithUserDTO.fromComment(comment, creator));
    }

    public List<CommentWithUserDTO> getCommentsWithUser(Long topicId) {
        List<Comment> comments = commentRepository.findByTopicId(topicId);
        return comments.stream()
                .map(comment -> {
                    UserDTO creator = null;
                    try {
                        Optional<UserResponse> userOpt = userClient.getUserById(comment.getUserId());
                        creator = userOpt.map(UserDTO::fromGrpcResponse).orElse(null);
                    } catch (Exception e) {
                        System.err.println("Failed to fetch user " + comment.getUserId() + ": " + e.getMessage());
                    }
                    return CommentWithUserDTO.fromComment(comment, creator);
                })
                .collect(Collectors.toList());
    }
}
