package com.eugene.user_service.kafka;

import com.eugene.user_service.dto.event.BookEvent;
import com.eugene.user_service.dto.event.ReviewEvent;
import com.eugene.user_service.model.User;
import com.eugene.user_service.repository.UserRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserEventConsumer {

    private final UserRepository userRepository;

    public UserEventConsumer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @KafkaListener(topics = "book.events", groupId = "user-service-group")
    @Transactional
    public void handleBookDeletedEvent(BookEvent event) {
        if ("BOOK_DELETED".equals(event.eventType())) {
            deleteUserReviewsByIds(event.reviewsIds());
        }
    }

    @KafkaListener(topics = "review.events", groupId = "user-service-group")
    @Transactional
    public void handleReviewsCreatedEvent(ReviewEvent event) {
        if ("REVIEWS_CREATED".equals(event.eventType())) {
            userRepository
                    .findById(event.username())
                    .ifPresent(user -> {
                        user
                                .getReviewsIds()
                                .addAll(event.reviewsIds());
                        userRepository.save(user);
                    });
        }
    }

    @KafkaListener(topics = "review.events", groupId = "user-service-group")
    @Transactional
    public void handleReviewsDeletedEvent(ReviewEvent event) {
        if ("REVIEWS_DELETED".equals(event.eventType())) {
            deleteUserReviewsByIds(event.reviewsIds());
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
    }
}