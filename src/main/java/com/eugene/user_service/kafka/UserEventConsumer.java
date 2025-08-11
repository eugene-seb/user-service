package com.eugene.user_service.kafka;

import com.eugene.user_service.dto.event.BookDtoEvent;
import com.eugene.user_service.dto.event.ReviewDtoEvent;
import com.eugene.user_service.model.User;
import com.eugene.user_service.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserEventConsumer
{
    private final Logger log = LoggerFactory.getLogger(UserEventConsumer.class);
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UserRepository userRepository;
    
    @KafkaListener(topics = "book.events", groupId = "user-service-group")
    @Transactional
    public void handleBookEvents(String json) throws JsonProcessingException {
        BookDtoEvent bookDtoEvent = this.objectMapper.readValue(json,
                                                                BookDtoEvent.class);
        if (Objects.equals(bookDtoEvent.getEventType(),
                           KafkaEventType.BOOK_DELETED)) {
            deleteUserReviewsByIds(bookDtoEvent.getReviewsIds());
        }
    }
    
    @KafkaListener(topics = "review.events", groupId = "user-service-group")
    @Transactional
    public void handleReviewsEvents(String json) throws JsonProcessingException {
        ReviewDtoEvent reviewDtoEvent = this.objectMapper.readValue(json,
                                                                    ReviewDtoEvent.class);
        switch (reviewDtoEvent.getEventType()) {
            case REVIEWS_CREATED -> addNewReviewsToUser(reviewDtoEvent);
            case REVIEWS_DELETED -> deleteUserReviewsByIds(reviewDtoEvent.getReviewsIds());
            case null, default -> {
                // No need to treat the other enum values since user-service don't listen those events in review topic
            }
        }
    }
    
    private void addNewReviewsToUser(ReviewDtoEvent reviewDtoEvent) {
        this.userRepository
                .findById(reviewDtoEvent.getUserId())
                .ifPresent(user -> {
                    Set<Long> reviewIds = new HashSet<>(user.getReviewsIds());
                    reviewIds.addAll(reviewDtoEvent.getReviewsIds());
                    user.setReviewsIds(reviewIds);
                    this.userRepository.save(user);
                    this.log.info("New review added to the user.");
                });
    }
    
    private void deleteUserReviewsByIds(Set<Long> reviewsIds) {
        List<User> users = this.userRepository.findAll();
        for (User u : users) {
            Set<Long> reviewsIdsUpdated = u
                    .getReviewsIds()
                    .stream()
                    .filter(r -> !reviewsIds.contains(r))
                    .collect(Collectors.toSet());
            u.setReviewsIds(reviewsIdsUpdated);
            this.userRepository.save(u);
        }
        this.log.info("Reviews deleted in Users");
    }
}