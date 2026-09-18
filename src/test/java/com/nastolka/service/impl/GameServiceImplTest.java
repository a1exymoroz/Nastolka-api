package com.nastolka.service.impl;

import com.nastolka.dto.GameResponse;
import com.nastolka.entity.Game;
import com.nastolka.integration.bgg.BggClient;
import com.nastolka.repository.GameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameServiceImplTest {

    private static final Long ID = 1L;

    @Mock
    private GameRepository gameRepository;
    @Mock
    private BggClient bggClient;

    private GameServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new GameServiceImpl(gameRepository, bggClient);
    }

    @Test
    void getGameById_returnsGame_whenAGameExistsWithThatId() {
        Game game = new Game();
        game.setId(ID);
        game.setName("Catan");
        when(gameRepository.findById(ID)).thenReturn(Optional.of(game));

        GameResponse response = service.getGameById(ID);

        assertThat(response.getId()).isEqualTo(ID);
        assertThat(response.getName()).isEqualTo("Catan");
    }

    @Test
    void getGameById_throwsNotFound_whenNoGameExists() {
        when(gameRepository.findById(ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getGameById(ID))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}
