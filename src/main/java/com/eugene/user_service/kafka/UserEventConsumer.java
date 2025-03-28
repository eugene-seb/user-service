package com.eugene.user_service.kafka;

import com.eugene.user_service.dto.event.BookDtoEvent;
import com.eugene.user_service.dto.event.ReviewDtoEvent;
import com.eugene.user_service.model.User;
import com.eugene.user_service.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserEventConsumer {

    private final Logger log = LoggerFactory.getLogger(UserEventConsumer.class);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UserRepository userRepository;

    public UserEventConsumer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @KafkaListener(topics = "book.events", groupId = "user-service-group")
    @Transactional
    public void handleBookDeletedEvent(String json) throws JsonProcessingException {
        BookDtoEvent bookDtoEvent = objectMapper.readValue(json, BookDtoEvent.class);
        if ("BOOK_DELETED".equals(bookDtoEvent.getEventType())) {
            deleteUserReviewsByIds(bookDtoEvent.getReviewsIds());
        }
    }

    @KafkaListener(topics = "review.events", groupId = "user-service-group")
    @Transactional
    public void handleReviewsCreatedEvent(String json) throws JsonProcessingException {
        ReviewDtoEvent reviewDtoEvent = objectMapper.readValue(json, ReviewDtoEvent.class);
        if ("REVIEWS_CREATED".equals(reviewDtoEvent.getEventType())) {
            userRepository
                    .findById(reviewDtoEvent.getUsername())
                    .ifPresent(user -> {
                        user
                                .getReviewsIds()
                                .addAll(reviewDtoEvent.getReviewsIds());
                        userRepository.save(user);
                        log.info("ID of the review saved in User");
                    });
        }
    }

    @KafkaListener(topics = "review.events", groupId = "user-service-group")
    @Transactional
    public void handleReviewsDeletedEvent(String json) throws JsonProcessingException {
        ReviewDtoEvent reviewDtoEvent = objectMapper.readValue(json, ReviewDtoEvent.class);
        if ("REVIEWS_DELETED".equals(reviewDtoEvent.getEventType())) {
            deleteUserReviewsByIds(reviewDtoEvent.getReviewsIds());
        }
    }

    private void deleteUserReviewsByIds(Set<Long> reviewsIds) {
        List<User> users = userRepository.findAll();
        for (User u : users) {
            Set<Long> reviewsIdsUpdated = u
                    .getReviewsIds()
                    .stream()
                    .filter(r -> !reviewsIds.contains(r))
                    .collect(Collectors.toSet());
            u.setReviewsIds(reviewsIdsUpdated);
            userRepository.save(u);
        }
        log.info("Reviews deleted in Users");
    }
}