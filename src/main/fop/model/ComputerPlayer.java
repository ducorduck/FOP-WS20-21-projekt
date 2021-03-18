package fop.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import fop.controller.GameController;
import fop.model.board.Position;
import fop.model.cards.BrokenToolCard;
import fop.model.cards.Card;
import fop.model.cards.FixedToolCard;
import fop.model.cards.PathCard;
import fop.model.cards.GoalCard.Type;

import javax.swing.SwingWorker;


/***
 * 
 * Stellt einen Computerspieler dar.
 *
 */
public class ComputerPlayer extends Player {

	private LinkedList<Integer> possibleGoldCardPositions = new LinkedList <Integer>();
	private boolean goldCardDetected = false;
	private Position targetPosition = GameController.getCentrerPostion();
	
	
	public ComputerPlayer(String name) {
		super(name);
		for (int i = 0; i < GameController.getNumberOfGoalCard(); i++) {
			possibleGoldCardPositions.addFirst(i);
		}
		GameController.addPropertyChangeListener(GameController.NEXT_PLAYER, evt -> {
			// skip if it is not the players turn
			if (GameController.getActivePlayer() != this) return;
			
			// do action in background worker
			new SwingWorker<Object, Void>() {
				
				@Override
				protected Object doInBackground() throws Exception {
					sleep(1000);
					doAction();
					sleep(1000);
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
		sleep(1000);
	}

	
	private Card getTargetCard() {
		goalCardAnalyse();
		targetPosition = getPredictedGoldCardPosition();
		System.out.println(name + " should go to " + targetPosition.toString());
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
		if (goldCardDetected) return -1;
		int chosenNo = possibleGoldCardPositions.getFirst();
		double optimise = (GameController.getNumberOfGoalCard()-1)/2;
		for (int i : possibleGoldCardPositions) {
			if (Math.abs(i-optimise) < Math.abs(chosenNo-optimise)) {
				chosenNo = i;
			}
		}
		return chosenNo;
	}

	private void goalCardUpdate(int i) {
		if (GameController.getGoalCardByNo(i).getType() == Type.Stone) {
			possibleGoldCardPositions.remove(Integer.valueOf(i));
		} else {
			goldCardDetected = true;
			possibleGoldCardPositions = new LinkedList<>(Arrays.asList(i));
			System.out.println(name + " knew card " + i + " is a gold card");
		}
	}

	public void goalCardAnalyse() {
		if (!goldCardDetected) {
			for (int i = 0; i < GameController.getNumberOfGoalCard(); i++) {
				if ((!GameController.getGoalCardByNo(i).isCovered()) && GameController.getGoalCardByNo(i).getType() == Type.Stone) {
					possibleGoldCardPositions.remove(Integer.valueOf(i));
				} 
			}
			if (possibleGoldCardPositions.size() <= GameController.getNumberOfGoldCard()) {
				goldCardDetected = true;
				//int i = GameController.getPositionClosestToCenter(possibleGoldCardPositions);
				//System.out.println(name + " knew card " + i + " is a gold card");
				System.out.print(name + "knew ");
				for (int i = 0; i < possibleGoldCardPositions.size() - 1; i++) {
					System.out.print(possibleGoldCardPositions.get(i) + ", ");
				}
				System.out.print("and " + possibleGoldCardPositions.getLast() + "is/are gold");
			}
		}
	}

	private Position getPredictedGoldCardPosition() {
		if (goldCardDetected) {
			int chosenNo = possibleGoldCardPositions.getFirst();
			double optimise = (GameController.getNumberOfGoalCard()-1)/2;
			for (int i : possibleGoldCardPositions) {
				if (Math.abs(i-optimise) < Math.abs(chosenNo-optimise)) {
					chosenNo = i;
				}
			}
			return GameController.getGoalCardPosition(chosenNo);
		} else {
			int size = possibleGoldCardPositions.size();
			if (size % 2 != 0) {
				int chosenNo = possibleGoldCardPositions.get((size-1)/2);
				return GameController.getGoalCardPosition(chosenNo);
			} else {
				int chosenNo1 = possibleGoldCardPositions.get(size/2);
				int chosenNo2 = possibleGoldCardPositions.get(size/2-1);
				Position pos1 = GameController.getGoalCardPosition(chosenNo1);
				Position pos2 = GameController.getGoalCardPosition(chosenNo2);
				int x = pos1.x();
				int y = (pos1.y() + pos2.y())/2;
				return Position.of(x,y);
			}
		}
	}


	private Position getTargetRockfall(LinkedList<Position> destroyableCards) {
		if (destroyableCards.isEmpty()) return null;
		Collections.shuffle(destroyableCards);
		return destroyableCards.getFirst();
	}

	private Position getTargetPosition(Set<Position> placeablePosition) {
		if (placeablePosition.size() == 0) return null;
		int item = new Random().nextInt(placeablePosition.size());
		int i = 0;
        Position pos = null;
		for (Position posi : placeablePosition) {
			if (i == item) {
				pos = posi;
				break;
			}
			i++;
		}
		return pos;
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
				System.out.println(name + " has seen card " + i + ": " + GameController.getGoalCardByNo(i).getType().toString());
				goalCardUpdate(i);
				GameController.lookAtGoalCardWithSelectedCard(GameController.getGoalCardByNo(i));
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
				Position targetPosition = getTargetPosition(GameController.getAllPlaceablePosition((PathCard)card));
				if (targetPosition == null) {
					GameController.discardSelectedCard();
				}
				else {
					GameController.placeSelectedCardAt(targetPosition);
				}
			}
			return;
		}
		
		// werfe Karte ab
		GameController.discardSelectedCard();
	}
	
}
