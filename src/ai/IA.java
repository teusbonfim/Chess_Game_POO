package ai;

import controller.Game;
import model.board.Move;

public interface IA {
    /**
     * Decide o próximo movimento com base no estado atual do jogo.
     *
     * @param game O estado atual do jogo.
     * @return O movimento escolhido pela IA.
     */
    Move makeMove(Game game);
}