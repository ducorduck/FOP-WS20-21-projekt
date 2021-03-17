package fop.model;

import java.util.concurrent.TimeUnit;

import fop.controller.GameController;
import fop.model.board.Position;
import fop.model.cards.BrokenToolCard;
import fop.model.cards.Card;
import fop.model.cards.FixedToolCard;
import fop.model.cards.GoalCard;
import fop.model.cards.PathCard;

import javax.swing.SwingWorker;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.Map.Entry;

/***
 * 
 * Stellt einen Computerspieler dar.
 */
public class ComputerPlayer extends Player {

	private final HashSet<GoalCard> seenGoalCards = new HashSet <GoalCard>();
	
	public ComputerPlayer(String name) {
		super(name);
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
		sleep(800);
	}
	
	private Player getTargetToBreak (LinkedList<Player> players) {
		if (players == null || players.isEmpty()) return null;
		Collections.shuffle(players);
		for (Player player : players) {
			if (player != this) {
				return player;
			}
		}
		return null;
	}

	private Player getTargetToFix (LinkedList<Player> players) {
		if (players == null || players.isEmpty()) return null;
		if (players.contains(this)) return this;
		Collections.shuffle(players);
		return players.getFirst();
	}

	private BrokenToolCard getTargetBrokenToolCard (Player player, FixedToolCard fixCard) {
		LinkedList<BrokenToolCard> fixable = player.getAllBrokenToolCards(fixCard);
		Collections.shuffle(fixable);
		return fixable.getFirst();
	}

	private PathCard getTargetPathCard (LinkedList<PathCard> pathcards) {
		if (pathcards.isEmpty()) return null;
		Collections.shuffle(pathcards);
		return pathcards.getFirst();
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
		Card card = handCards.get((int) (Math.random() * handCards.size()));
		
		// wähle Karte aus
		selectCard(card);


		if (card.isBrokenTool()) {
			LinkedList <Player> targetPlayers = new LinkedList <Player>();
			for (Player player : GameController.getPlayers()) {
				if (player.canToolBeBroken((BrokenToolCard)card)) {
					targetPlayers.add(player);
				}
			}
			Player targetPlayer = getTargetToBreak(targetPlayers);
			if (targetPlayer == null) {
				GameController.discardSelectedCard();
				return;
			} else {
			GameController.breakToolWithSelectedCard(targetPlayer);
			return;
			}
		}

		if (card.isFixedTool()) {
			LinkedList <Player> targetPlayers = new LinkedList <Player>();
			for (Player player : GameController.getPlayers()) {
				if (player.canBeFixed((FixedToolCard)card)){
					targetPlayers.add(player);
				}
			}
			Player targetPlayer = getTargetToFix(targetPlayers);
			if (targetPlayer == null) {
				GameController.discardSelectedCard();
				return;
			} else {
				BrokenToolCard targetBrokenCard = getTargetBrokenToolCard(targetPlayer, (FixedToolCard)card);
				GameController.fixBrokenToolCardWithSelectedCard(targetPlayer, targetBrokenCard);
				return;
			}
		}

		
		if (card.isMap()) {
			Random rand = new Random();
			int i = rand.nextInt(GameController.getAllGoalCards().length);
			seenGoalCards.add(GameController.getGoalCard(i));
			GameController.lookAtGoalCardWithSelectedCard(GameController.getGoalCard(i));
			GameController.placeSelectedCardAt(GameController.getGameboard().getGoalCardPosition(i));
			return;
		}


		 /*
		if (card.isRockfall()) {
			LinkedList <PathCard> pathCards = new LinkedList <PathCard> ();
			pathCards.addAll(GameController.getAllOnBoardPathCards());
			PathCard targetPathCard = getTargetPathCard(pathCards);
			if (targetPathCard == null) {
				GameController.discardSelectedCard();
				return;
			}
			Position targetPosition = null;
			for (Entry <Position, PathCard> e : GameController.getGameboard().getBoard().entrySet()) {
				if (e.getValue().equals(targetPathCard) && e.getKey() != null ) {
					targetPosition = e.getKey();
					break;
				}
			}
			if (targetPosition == null) {
				GameController.discardSelectedCard();
				return;
			}
			//GameController.destroyCardWithSelectedCardAt(targetPosition.x(), targetPosition.y());
			GameController.getGameboard().placeCard(targetPosition, targetPathCard);
			GameController.getGameboard().removeCard(targetPosition.x(), targetPosition.y());
			
		}*/

		// werfe Karte ab
		GameController.discardSelectedCard();
	}
	
}
