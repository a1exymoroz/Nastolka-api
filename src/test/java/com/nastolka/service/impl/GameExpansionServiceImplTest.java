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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameExpansionServiceImplTest {

    private static final Long ID = 1L;

    @Mock
    private GameRepository gameRepository;
    @Mock
    private GameExpansionRepository expansionRepository;
    @Mock
    private BggClient bggClient;

    private GameExpansionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new GameExpansionServiceImpl(gameRepository, expansionRepository, bggClient);
    }

    @Test
    void getExpansions_returnsAssignedExpansions_whenAGameExistsWithThatId() {
        Game game = new Game();
        game.setId(ID);
        GameExpansion expansion = new GameExpansion();
        expansion.setId(2L);
        expansion.setGame(game);
        expansion.setName("Seafarers");
        when(gameRepository.existsById(ID)).thenReturn(true);
        when(expansionRepository.findByGameId(ID)).thenReturn(List.of(expansion));

        List<ExpansionResponse> expansions = service.getExpansions(ID);

        assertThat(expansions).extracting("name").containsExactly("Seafarers");
    }

    @Test
    void getExpansions_returnsEmptyList_whenIdBelongsToAnExpansionNotAGame() {
        // An assigned expansion links to its own game-detail page using its
        // own id, which then asks for "its" expansions — an expansion never
        // has expansions of its own, so this must be an empty list, not 404.
        when(gameRepository.existsById(ID)).thenReturn(false);
        when(expansionRepository.existsById(ID)).thenReturn(true);

        List<ExpansionResponse> expansions = service.getExpansions(ID);

        assertThat(expansions).isEmpty();
    }

    @Test
    void getExpansions_throwsNotFound_whenNeitherAGameNorAnExpansionExists() {
        when(gameRepository.existsById(ID)).thenReturn(false);
        when(expansionRepository.existsById(ID)).thenReturn(false);

        assertThatThrownBy(() -> service.getExpansions(ID))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}
