package com.nastolka.service.impl;

import com.nastolka.dto.ExpansionResponse;
import com.nastolka.entity.Game;
import com.nastolka.entity.GameExpansion;
import com.nastolka.integration.bgg.BggClient;
import com.nastolka.repository.GameExpansionRepository;
import com.nastolka.repository.GameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameExpansionServiceImplTest {

    private static final Long GAME_ID = 1L;
    private static final Long EXPANSION_ID = 2L;

    @Mock
    private GameRepository gameRepository;
    @Mock
    private GameExpansionRepository expansionRepository;
    @Mock
    private BggClient bggClient;

    private GameExpansionServiceImpl service;
    private Game game;

    @BeforeEach
    void setUp() {
        service = new GameExpansionServiceImpl(gameRepository, expansionRepository, bggClient);

        game = new Game();
        game.setId(GAME_ID);
        lenient().when(gameRepository.findById(GAME_ID)).thenReturn(Optional.of(game));
    }

    @Test
    void getExpansions_returnsAssignedExpansions() {
        GameExpansion expansion = new GameExpansion();
        expansion.setId(EXPANSION_ID);
        expansion.setGame(game);
        expansion.setName("Seafarers");
        when(expansionRepository.findByGameId(GAME_ID)).thenReturn(List.of(expansion));

        List<ExpansionResponse> expansions = service.getExpansions(GAME_ID);

        assertThat(expansions).extracting("name").containsExactly("Seafarers");
    }

    @Test
    void getExpansions_throwsNotFound_whenGameDoesNotExist() {
        when(gameRepository.findById(GAME_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getExpansions(GAME_ID))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void getExpansion_returnsTheExpansion_whenItBelongsToTheGivenGame() {
        GameExpansion expansion = new GameExpansion();
        expansion.setId(EXPANSION_ID);
        expansion.setGame(game);
        expansion.setName("Seafarers");
        when(expansionRepository.findByIdAndGameId(EXPANSION_ID, GAME_ID)).thenReturn(Optional.of(expansion));

        ExpansionResponse response = service.getExpansion(GAME_ID, EXPANSION_ID);

        assertThat(response.getId()).isEqualTo(EXPANSION_ID);
        assertThat(response.getName()).isEqualTo("Seafarers");
    }

    @Test
    void getExpansion_throwsNotFound_whenExpansionDoesNotBelongToThatGame() {
        when(expansionRepository.findByIdAndGameId(EXPANSION_ID, GAME_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getExpansion(GAME_ID, EXPANSION_ID))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }

    @Test
    void getExpansion_throwsNotFound_whenGameDoesNotExist() {
        when(gameRepository.findById(GAME_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getExpansion(GAME_ID, EXPANSION_ID))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}
