package ostinato.chess.ai

import ostinato.chess.core._
import ostinato.core.RandomAi

case class ChessRandomAi(player: ChessPlayer, seed: Option[Long] = None)
  extends RandomAi[ChessBoard, ChessPlayer, ChessGame, ChessMovement, ChessRules](player, seed) {

  // N.B. the only reason this is here is to have a default implicit value for rules
  override def move(game: ChessGame)(implicit rules: ChessRules = ChessRules.default): ChessMovement = super.move(game)
}