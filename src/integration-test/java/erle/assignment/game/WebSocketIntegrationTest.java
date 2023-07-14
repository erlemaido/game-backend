package erle.assignment.game;

import erle.assignment.game.exception.ErrorResult;
import erle.assignment.game.usecase.PlayGameUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebSocketIntegrationTest {

    private static final BigDecimal VALID_BET = BigDecimal.valueOf(40.5);
    private static final int VALID_NUMBER = 50;
    private static final BigDecimal INVALID_BET = BigDecimal.ZERO;
    private static final int INVALID_NUMBER = 150;
    private static final BigDecimal WINNING_AMOUNT = BigDecimal.valueOf(80.19);
    private static final BigDecimal LOSING_AMOUNT = BigDecimal.ZERO;
    private static final String WEBSOCKET_TOPIC = "/topic/result";
    private static final String WEBSOCKET_ERROR_QUEUE = "/queue/errors";
    private static final String URL = "ws://localhost:{port}/game";
    private static final String MESSAGE_DESTINATION = "/app/play";

    @LocalServerPort
    private Integer port;
    private WebSocketStompClient stompClient;
    private StompSession session;

    @BeforeEach
    void setup() throws ExecutionException, InterruptedException, TimeoutException {
        this.stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
        session = createSession();
    }

    @Test
    void givenValidRequest_whenPlayingGame_thenAmountIsReturned() throws InterruptedException {
        // given
        var resultQueue = new ArrayBlockingQueue<PlayGameUseCase.Response>(1);
        subscribeToDestination(session, WEBSOCKET_TOPIC, resultQueue, PlayGameUseCase.Response.class);

        // when
        session.send(MESSAGE_DESTINATION, new PlayGameUseCase.Request(VALID_BET, VALID_NUMBER));

        // then
        var response = resultQueue.poll(5, TimeUnit.SECONDS);
        var expectedValues = List.of(LOSING_AMOUNT, WINNING_AMOUNT);

        assertThat(response).isNotNull();
        assertThat(response.winningAmount()).isIn(expectedValues);
    }

    @Test
    void givenInvalidRequest_whenPlayingGame_thenErrorIsReceived() throws InterruptedException {
        // given
        var errorQueue = new ArrayBlockingQueue<ErrorResult>(1);
        subscribeToDestination(session, WEBSOCKET_ERROR_QUEUE, errorQueue, ErrorResult.class);

        // when
        session.send(MESSAGE_DESTINATION, new PlayGameUseCase.Request(INVALID_BET, INVALID_NUMBER));

        // then
        var errorMessage = errorQueue.poll(5, TimeUnit.SECONDS);

        assertThat(errorMessage).isNotNull();
        assertThat(errorMessage.errors())
                .hasSize(2)
                .anySatisfy(
                        error -> {
                            assertThat(error.field()).isEqualTo("bet");
                            assertThat(error.message()).isEqualTo("Bet must be bigger than 0");
                        }
                )
                .anySatisfy(
                        error -> {
                            assertThat(error.field()).isEqualTo("number");
                            assertThat(error.message()).isEqualTo("Number should not be greater than 100");
                        }
                );
    }

    private StompSession createSession() throws InterruptedException, ExecutionException, TimeoutException {
        return stompClient
                .connect(getWebSocketUrl(), new StompSessionHandlerAdapter() {})
                .get(1, SECONDS);
    }

    private String getWebSocketUrl() {
        return URL.replace("{port}", String.valueOf(port));
    }

    private <T> void subscribeToDestination(StompSession session, String destination,
                                            ArrayBlockingQueue<T> queue, Class<T> payloadType) {
        session.subscribe(destination, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return payloadType;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                queue.offer(payloadType.cast(payload));
            }
        });
    }

}
