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
    private final Set<Long> reviewIds;
    private final Set<Long> reviewIdsToDelete;
    private final Set<Long> reviewIdsAfterDelete;
    private final User user1;
    private final User user2;
    
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserEventConsumer userEventConsumer;
    
    public UserEventConsumerTest() {
        this.reviewIds = Set.of(1L,
                                2L,
                                3L);
        this.reviewIdsToDelete = Set.of(3L);
        this.reviewIdsAfterDelete = Set.of(1L,
                                           2L);
        
        this.user1 = new User("keycloakID",
                              "user1",
                              Set.of(Role.USER));
        this.user1.setReviewsIds(this.reviewIds);
        this.user2 = new User("keycloakID",
                              "user2",
                              Set.of(Role.ADMIN));
        this.user2.setReviewsIds(this.reviewIdsAfterDelete);
    }
    
    @Test
    void handleUserEvent_bookDeleted() throws JsonProcessingException {
        
        BookDtoEvent bookDtoEvent = new BookDtoEvent(KafkaEventType.BOOK_DELETED,
                                                     this.reviewIdsToDelete);
        String json = this.objectMapper.writeValueAsString(bookDtoEvent);
        
        given(this.userRepository.findAll()).willReturn(List.of(this.user1,
                                                                this.user2));
        this.userEventConsumer.handleBookEvents(json);
        
        this.user1.setReviewsIds(reviewIdsAfterDelete);
        verify(this.userRepository,
               times(1)).save(this.user1);
        verify(this.userRepository,
               times(1)).save(this.user2);
    }
    
    @Test
    void handleReviewsEvent_reviewCreated() throws JsonProcessingException {
        
        ReviewDtoEvent reviewDtoEvent = new ReviewDtoEvent(KafkaEventType.REVIEWS_CREATED,
                                                           "user1",
                                                           "isbn1",
                                                           this.reviewIds);
        String json = this.objectMapper.writeValueAsString(reviewDtoEvent);
        
        given(this.userRepository.findById(reviewDtoEvent.getUserId())).willReturn(Optional.of(this.user1));
        
        this.userEventConsumer.handleReviewsEvents(json);
        
        this.user1.setReviewsIds(this.reviewIds);
        verify(this.userRepository,
               times(1)).save(this.user1);
    }
    
    @Test
    void handleReviewsEvent_reviewDeleted() throws JsonProcessingException {
        
        ReviewDtoEvent reviewDtoEvent = new ReviewDtoEvent(KafkaEventType.REVIEWS_DELETED,
                                                           "user1",
                                                           "isbn1",
                                                           reviewIdsToDelete);
        String json = this.objectMapper.writeValueAsString(reviewDtoEvent);
        
        given(this.userRepository.findAll()).willReturn(List.of(this.user1,
                                                                this.user2));
        
        this.userEventConsumer.handleReviewsEvents(json);
        
        this.user1.setReviewsIds(this.reviewIdsAfterDelete);
        verify(this.userRepository,
               times(1)).save(this.user1);
        verify(this.userRepository,
               times(1)).save(this.user2);
        assertThat(this.user1.getReviewsIds()).isEqualTo(this.reviewIdsAfterDelete);
    }
}