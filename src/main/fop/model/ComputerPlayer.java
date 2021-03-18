package fop.model;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import fop.controller.GameController;
import fop.model.board.Position;
import fop.model.cards.BrokenToolCard;
import fop.model.cards.Card;
import fop.model.cards.FixedToolCard;
import fop.model.cards.GoalCard;
import fop.model.cards.PathCard;
import fop.model.cards.GoalCard.Type;

import javax.swing.SwingWorker;

/***
 * 
 * Stellt einen Computerspieler dar.
 *
 */
public class ComputerPlayer extends Player {

	private LinkedList<Integer> possibleGoldCardPositions = new LinkedList<>();
	private int goldCardPosition = -1;
	
	public ComputerPlayer(String name) {
		super(name);
		for (int i = 0; i < GameController.getNumberOfGoalCard(); i++) {
			possibleGoldCardPositions.add(i);
		}
		Collections.shuffle(possibleGoldCardPositions);
		GameController.addPropertyChangeListener(GameController.NEXT_PLAYER, evt -> {
			// skip if it is not the players turn
			if (GameController.getActivePlayer() != this) return;
			
			// do action in background worker
			new SwingWorker<Object, Void>() {
				
				@Override
				protected Object doInBackground() throws Exception {
					sleep(2000);
					doAction();
					sleep(2000);
					return null;
				}
			}.execute();
		});
	}
	
	@Override
	public boolean isComputer() {
		return true;
	}
	
	/**
	 * Pausiert das Programm, damit die Änderungen auf der Benutzeroberfläche sichtbar werden.
	 * @param timeMillis zu wartende Zeit in Millisekunden
	 */
	protected void sleep(int timeMillis) {
		try {
			TimeUnit.MILLISECONDS.sleep(timeMillis);
		} catch (InterruptedException ignored) {}
	}
	
	protected void selectCard(Card card) {
		GameController.selectCard(card);
		sleep(2000);
	}

	
	private Card getTargetCard() {
		Card targetCard = handCards.get((int) (Math.random() * handCards.size()));
		return targetCard;
	}

	private Player getTargetToBreak (LinkedList<Player> players) {
		if (players == null || players.isEmpty()) return null;
		Collections.shuffle(players);
		return players.isEmpty() ? null : players.getFirst();
	}
	
	private Player getTargetToFix (LinkedList<Player> players) {
		if (players == null || players.isEmpty()) return null;
		if (players.contains(this)) return this;
		Collections.shuffle(players);
		return players.getFirst();
	}

	private BrokenToolCard getTargetBrokenToolCard (Player player, FixedToolCard fixedToolCard) {
		LinkedList<BrokenToolCard> fixable = player.getAllBrokenToolCards(fixedToolCard);
		Collections.shuffle(fixable);
		return fixable.getFirst();
	}

	private int getGoalCardNo() {
		if (possibleGoldCardPositions.isEmpty()) return -1;
		return possibleGoldCardPositions.getFirst();
	}

	public void estimateGoldCardPosition(int i, GoalCard seenGoalCard) {
		possibleGoldCardPositions.remove(Integer.valueOf(i));
		if (seenGoalCard.getType() == Type.Gold) {
			goldCardPosition = i;
			possibleGoldCardPositions = new LinkedList<Integer>();
		} else {
			if (possibleGoldCardPositions.size() == GameController.getNumberOfGoldCard()) {
				goldCardPosition = possibleGoldCardPositions.getFirst();
			}
		}
		if (goldCardPosition != -1) {
			System.out.println("One possible gold card is " + goldCardPosition);
		}
	}

	private Position getTargetRockfall(LinkedList<Position> destroyableCards) {
		if (destroyableCards.isEmpty()) return null;
		Collections.shuffle(destroyableCards);
		return destroyableCards.getFirst();
	}

	private Position getPredictedGoldCardPosition() {
		return goldCardPosition != -1 ? GameController.getGoalCardPosition(goldCardPosition) : GameController.getCentrerPostion();
	}

	/**
	 * Führt einen Zug des Computerspielers aus.<br>
	 * Benutzt {@link #selectCard(Card)}, um eine Karte auszuwählen.<br>
	 * Benutzt Methoden in {@link GameController}, um Aktionen auszuführen.
	 */
	protected void doAction() {
		// TODO Aufgabe 4.3.3
		// Sie dürfen diese Methode vollständig umschreiben und den vorhandenen Code entfernen.
		


		// erhalte zufällige Handkarte
		Card card = getTargetCard();
		
		// wähle Karte aus
		selectCard(card);

		if (card.isBrokenTool()) {
			LinkedList <Player> targetPlayers = new LinkedList <Player>();
			for (Player player : GameController.getPlayers()) {
				if (player != this && player.canToolBeBroken((BrokenToolCard)card)) {
					targetPlayers.add(player);
				}
			}
			Player targetPlayer = getTargetToBreak(targetPlayers);
			if (targetPlayer == null) {
				GameController.discardSelectedCard();
			} else {
				GameController.breakToolWithSelectedCard(targetPlayer);
			}
			return;
		}

		if (card.isFixedTool()) {
			LinkedList <Player> targetPlayers = new LinkedList <Player>();
			for (Player player : GameController.getPlayers()) {
				if (player.canBeFixedBy((FixedToolCard)card)){
					targetPlayers.add(player);
				}
			}
			Player targetPlayer = getTargetToFix(targetPlayers);
			if (targetPlayer == null) {
				GameController.discardSelectedCard();
			} else {
				BrokenToolCard targetBrokenCard = getTargetBrokenToolCard(targetPlayer, (FixedToolCard)card);
				GameController.fixBrokenToolCardWithSelectedCard(targetPlayer, targetBrokenCard);
			}
			return;
		}

		if (card.isMap()) {
			int i = getGoalCardNo();
			if (i == -1) {
				GameController.discardSelectedCard();
			} else {
			estimateGoldCardPosition(i, GameController.getGoalCardByNo(i));
			GameController.lookAtGoalCardWithSelectedCard(GameController.getGoalCardByNo(i));
			//GameController.placeSelectedCardAt(GameController.getGoalCardPosition(i));    //WRONG CODE
			}
			return;
		}


		if (card.isRockfall()) {
			Position targetRockFall = getTargetRockfall(GameController.getPathCardPositions());
			if (targetRockFall == null ){
				GameController.discardSelectedCard();
			} else {
			GameController.destroyCardWithSelectedCardAt(targetRockFall.x(), targetRockFall.y());
			}
			return;
		}

		if (card.isPathCard()) {
			if (hasBrokenTool()) {
				GameController.discardSelectedCard();
			} else {
				Set<Position> placeablePosition = GameController.getAllPlaceablePosition((PathCard)card);
			}
		}
		
		// werfe Karte ab
		GameController.discardSelectedCard();
	}
	
}
