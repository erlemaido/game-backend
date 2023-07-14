package erle.assignment.game.controller;

import erle.assignment.game.usecase.PlayGameUseCase;


import lombok.AllArgsConstructor;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import javax.validation.Valid;

@Controller
@AllArgsConstructor
public class GameController {

    private PlayGameUseCase playGameUseCase;

    @MessageMapping("/play")
    @SendTo("/topic/result")
    public PlayGameUseCase.Response playGame(@Valid @Payload PlayGameUseCase.Request request) {
        return playGameUseCase.handle(request);
    }

}
