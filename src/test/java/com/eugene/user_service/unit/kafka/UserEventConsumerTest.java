package com.eugene.user_service.unit.kafka;

import com.eugene.user_service.dto.event.BookDtoEvent;
import com.eugene.user_service.dto.event.ReviewDtoEvent;
import com.eugene.user_service.kafka.KafkaEventType;
import com.eugene.user_service.kafka.UserEventConsumer;
import com.eugene.user_service.model.Role;
import com.eugene.user_service.model.User;
import com.eugene.user_service.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class UserEventConsumerTest
{

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserEventConsumer userEventConsumer;

    @Test
    void handleBookDeletedEvent() throws JsonProcessingException {

        Set<Long> reviewIds = Set.of(1L, 2L, 3L);
        Set<Long> reviewIdsToDelete = Set.of(3L);
        Set<Long> reviewIdsAfterDelete = Set.of(1L, 2L);

        User user1 = new User("user1", "user1@email.com", "password", Role.USER);
        user1.setReviewsIds(reviewIds);
        User user2 = new User("user2", "user2@email.com", "password", Role.ADMIN);
        user2.setReviewsIds(reviewIdsAfterDelete);

        BookDtoEvent bookDtoEvent = new BookDtoEvent(KafkaEventType.BOOK_DELETED,
                                                     reviewIdsToDelete);
        String json = this.objectMapper.writeValueAsString(bookDtoEvent);

        given(this.userRepository.findAll()).willReturn(List.of(user1, user2));

        this.userEventConsumer.handleBookDeletedEvent(json);

        user1.setReviewsIds(reviewIdsAfterDelete);
        verify(this.userRepository, times(1)).save(user1);
        verify(this.userRepository, times(1)).save(user2);
    }

    @Test
    void handleReviewsCreatedEvent() throws JsonProcessingException {

        Set<Long> reviewsIds = Set.of(1L);
        User user1 = new User("user1", "user1@email.com", "password", Role.USER);

        ReviewDtoEvent reviewDtoEvent = new ReviewDtoEvent(KafkaEventType.REVIEWS_CREATED, "user1",
                                                           "isbn1", reviewsIds);
        String json = this.objectMapper.writeValueAsString(reviewDtoEvent);

        given(this.userRepository.findById(reviewDtoEvent.getUsername())).willReturn(
                Optional.of(user1));

        this.userEventConsumer.handleReviewsCreatedEvent(json);

        user1.setReviewsIds(reviewsIds);
        verify(this.userRepository, times(1)).save(user1);
    }

    @Test
    void handleReviewsDeletedEvent() throws JsonProcessingException {

        Set<Long> reviewIds = Set.of(1L, 2L, 3L);
        Set<Long> reviewIdsToDelete = Set.of(3L);
        Set<Long> reviewIdsAfterDelete = Set.of(1L, 2L);

        User user1 = new User("user1", "user1@email.com", "password", Role.USER);
        user1.setReviewsIds(reviewIds);
        User user2 = new User("user2", "user2@email.com", "password", Role.ADMIN);
        user2.setReviewsIds(reviewIdsAfterDelete);

        ReviewDtoEvent reviewDtoEvent = new ReviewDtoEvent(KafkaEventType.REVIEWS_DELETED, "user1",
                                                           "isbn1", reviewIdsToDelete);
        String json = this.objectMapper.writeValueAsString(reviewDtoEvent);

        given(this.userRepository.findAll()).willReturn(List.of(user1, user2));

        this.userEventConsumer.handleReviewsDeletedEvent(json);

        user1.setReviewsIds(reviewIdsAfterDelete);
        verify(this.userRepository, times(1)).save(user1);
        verify(this.userRepository, times(1)).save(user2);
        assertThat(user1.getReviewsIds()).isEqualTo(reviewIdsAfterDelete);
    }
}