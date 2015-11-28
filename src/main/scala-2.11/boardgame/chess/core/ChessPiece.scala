package boardgame.chess.core

import boardgame.core.{ BoardSize, XY, Piece }

abstract class ChessPiece(pos: XY, owner: ChessPlayer) extends Piece[ChessPlayer, ChessMovement, ChessBoard, ChessRules](pos, owner) {
  val isKing = false
  val isPawn = false
  val isRook = false
  def isThreatened(board: ChessBoard)(implicit rules: ChessRules): Boolean = threatenedBy(board).nonEmpty
  def isDefended(board: ChessBoard)(implicit rules: ChessRules): Boolean = defendedBy(board).nonEmpty

  def threatenedBy(board: ChessBoard)(implicit rules: ChessRules): Set[ChessPiece] =
    otherPlayer.pieces(board).filter(_.canMoveTo(pos, board)(rules.copy(kingIsTakeable = true)))

  def defendedBy(board: ChessBoard)(implicit rules: ChessRules): Set[ChessPiece] =
    owner.pieces(board).filter(_.canMoveTo(pos, board.move(new ChessMovement(withOwner(otherPlayer), XY(0, 0)))))

  def canMoveTo(to: XY, board: ChessBoard)(implicit rules: ChessRules) = movements(board).exists {
    m ⇒ (pos + m.delta) == to
  }

  def otherPlayer: ChessPlayer = this.owner.enemy
  def withOwner(newOwner: ChessPlayer): ChessPiece
  def equals(that: ChessPiece) = pos == that.pos && owner == that.owner
  override def toString = s"${owner.name}'s $pieceName on (${pos.x}, ${pos.y})"
  def movedTo(pos: XY): ChessPiece // N.B. unsafe (doesn't check bounds)
  def movements(board: ChessBoard)(implicit rules: ChessRules): Set[ChessMovement]
  val toChar: Char
  val pieceName: String
}

object ♜ {
  val deltas = Piece.toXYs(Set((-1, 0), (1, 0), (0, -1), (0, 1)))
  def char(owner: ChessPlayer) = owner match {
    case WhiteChessPlayer ⇒ '♜'
    case BlackChessPlayer ⇒ '♖'
  }
}
object ♝ {
  val deltas = Piece.toXYs(Set((-1, -1), (1, 1), (-1, 1), (1, -1)))
  def char(owner: ChessPlayer) = owner match {
    case WhiteChessPlayer ⇒ '♝'
    case BlackChessPlayer ⇒ '♗'
  }
}
object ♞ {
  val deltas = Piece.toXYs(Set((-1, -2), (1, -2), (-1, 2), (1, 2), (-2, -1), (-2, 1), (2, -1), (2, 1)))
  def char(owner: ChessPlayer) = owner match {
    case WhiteChessPlayer ⇒ '♞'
    case BlackChessPlayer ⇒ '♘'
  }
}
object ♚ {
  def deltas(addCastlingDeltas: Boolean) = normalDeltas ++ (if (addCastlingDeltas) Piece.toXYs(Set((-2, 0), (2, 0))) else Set())
  def normalDeltas = ♜.deltas ++ ♝.deltas
  def rookDeltaFor(kingDelta: XY) = XY(if (kingDelta.x < 0) 3 else -2, 0)
  def char(owner: ChessPlayer) = owner match {
    case WhiteChessPlayer ⇒ '♚'
    case BlackChessPlayer ⇒ '♔'
  }
}
object ♛ {
  val deltas = ♚.normalDeltas
  def char(owner: ChessPlayer) = owner match {
    case WhiteChessPlayer ⇒ '♛'
    case BlackChessPlayer ⇒ '♕'
  }
}
object ♟ {
  def deltas(dy: Int, isInInitialPosition: Boolean) =
    Piece.toXYs(Set((-1, dy), (0, dy), (1, dy)) ++ (if (isInInitialPosition) Set((0, 2 * dy)) else Set()))

  def char(owner: ChessPlayer) = owner match {
    case WhiteChessPlayer ⇒ '♟'
    case BlackChessPlayer ⇒ '♙'
  }
  def promotingPosition(dy: Int)(implicit boardSize: BoardSize) = Map(-1 -> 0, 1 -> (boardSize.x - 1))(dy)
}

class ♜(pos: XY, owner: ChessPlayer) extends ChessPiece(pos, owner) {
  def movements(board: ChessBoard)(implicit rules: ChessRules): Set[ChessMovement] = {
    ♜.deltas.flatMap { case delta ⇒ allMovementsOfDelta(pos, delta, board) }
  }
  val toChar = ♜.char(owner)
  val pieceName = "Rook"
  override val isRook = true
  def withOwner(newOwner: ChessPlayer) = new ♜(pos, newOwner)
  def movedTo(newXY: XY) = new ♜(newXY, owner)
}

class ♝(pos: XY, owner: ChessPlayer) extends ChessPiece(pos, owner) {
  def movements(board: ChessBoard)(implicit rules: ChessRules): Set[ChessMovement] = {
    ♝.deltas.flatMap { case delta ⇒ allMovementsOfDelta(pos, delta, board) }
  }
  val toChar = ♝.char(owner)
  val pieceName = "Bishop"
  def withOwner(newOwner: ChessPlayer) = new ♝(pos, newOwner)
  def movedTo(newXY: XY) = new ♝(newXY, owner)
}

class ♞(pos: XY, owner: ChessPlayer) extends ChessPiece(pos, owner) {
  def movements(board: ChessBoard)(implicit rules: ChessRules): Set[ChessMovement] = {
    ♞.deltas.flatMap { case delta ⇒ movementOfDelta(pos, delta, board) }
  }
  val toChar = ♞.char(owner)
  val pieceName = "Knight"
  def withOwner(newOwner: ChessPlayer) = new ♞(pos, newOwner)
  def movedTo(newXY: XY) = new ♞(newXY, owner)
}

class ♛(pos: XY, owner: ChessPlayer) extends ChessPiece(pos, owner) {
  def movements(board: ChessBoard)(implicit rules: ChessRules): Set[ChessMovement] = {
    ♛.deltas.flatMap { case delta ⇒ allMovementsOfDelta(pos, delta, board) }
  }
  val toChar = ♛.char(owner)
  val pieceName = "Queen"
  def withOwner(newOwner: ChessPlayer) = new ♛(pos, newOwner)
  def movedTo(newXY: XY) = new ♛(newXY, owner)
}

class ♚(pos: XY, owner: ChessPlayer) extends ChessPiece(pos, owner) {
  def movements(board: ChessBoard)(implicit rules: ChessRules): Set[ChessMovement] = {
    ♚.deltas(isInInitialPosition).flatMap { case delta ⇒ movementOfDelta(pos, delta, board) }
  }

  def initialY(implicit rules: ChessRules, chessBoardSize: BoardSize) =
    if (owner == WhiteChessPlayer && rules.whitePawnDirection == 1 ||
      owner == BlackChessPlayer && rules.whitePawnDirection == -1)
      0
    else
      chessBoardSize.y - 1

  def targetRookPosition(dx: Int)(implicit rules: ChessRules) = XY(if (dx < 0) 0 else chessBoardSize.x - 1, initialY)

  def isInInitialPosition(implicit rules: ChessRules) = pos.x == 4 && pos.y == initialY
  val toChar = ♚.char(owner)
  val pieceName = "King"
  override val isKing = true
  def withOwner(newOwner: ChessPlayer) = new ♚(pos, newOwner)
  def movedTo(newXY: XY) = new ♚(newXY, owner)
}
class ♟(pos: XY, owner: ChessPlayer, dy: Int) extends ChessPiece(pos, owner) {
  def movements(board: ChessBoard)(implicit rules: ChessRules): Set[ChessMovement] = {
    ♟.deltas(dy, isInInitialPosition).flatMap { case delta ⇒ movementOfDelta(pos, delta, board) }
  }
  val isInInitialPosition = dy == 1 && pos.y == 1 || dy == -1 && pos.y == chessBoardSize.y - 2
  val isPromoting = pos.y == ♟.promotingPosition(dy)
  val toChar = ♟.char(owner)
  val pieceName = "Pawn"
  override val isPawn = true
  def withOwner(newOwner: ChessPlayer) = new ♟(pos, newOwner, dy)
  def movedTo(newXY: XY) = new ♟(newXY, owner, dy)
}

object EnPassantPawn {
  def fromXYD(pos: XY, delta: XY, grid: Vector[Option[ChessPiece]]): Option[EnPassantPawn] = {
    if (pos.exists && (pos + delta).exists) {
      grid((pos + delta).toI) map {
        case p: ♟ ⇒ EnPassantPawn(pos, p)
      }
    } else None
  }
}
case class EnPassantPawn(from: XY, pawn: ♟)