package fop.model;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import fop.controller.GameController;
import fop.model.board.BoardAnchor;
import fop.model.board.Position;
import fop.model.cards.BrokenToolCard;
import fop.model.cards.Card;
import fop.model.cards.CardAnchor;
import fop.model.cards.FixedToolCard;
import fop.model.cards.PathCard;
import fop.model.cards.RockfallCard;
import fop.model.cards.GoalCard.Type;

import javax.swing.SwingWorker;


/***
 * 
 * Stellt einen Computerspieler dar.
 *
 */
public class ComputerPlayer extends Player {

	class PositionComparator implements Comparator<Position>{

		@Override
		public int compare(Position o1, Position o2) {
			int selfEvaluation = Math.abs(o1.x()-targetGoalPosition.x()) + Math.abs(o1.y() - targetGoalPosition.y());
			int otherPositionEvaluation = Math.abs(o2.x()-targetGoalPosition.x()) + Math.abs(o2.y() - targetGoalPosition.y());
			return selfEvaluation - otherPositionEvaluation;
		}
		
	}



	//////////////
	/////INIT/////
	//////////////


	private LinkedList<Integer> possibleGoldCardPositions = new LinkedList <Integer>();
	private boolean goldCardDetected = false;
	private Position targetGoalPosition = GameController.getCentrerPostion();


	List <Card> deadPathCardOnHand = new LinkedList <Card> ();				//all path card on hand that has at least 1 node
	List <Card> normalCardOnHand = new LinkedList <Card> (); 				//all path card on hand that has no node
	List <Card> mapOnHand = new LinkedList <Card> (); 						//all map card on hand
	List <Card> rockFallOnHand = new LinkedList <Card> (); 					//all rockfall card on hand
	List <Card> allBreakCardOnHand = new LinkedList <Card> ();				//all broken tool card on hand
	List <Card> breakableCardOnHand = new LinkedList <Card> ();				//break card on hand that can be used on other player(s)
	List <Card> simpleFixCardOnHand = new LinkedList <Card> ();				//onefold fixed tool card
	List <Card> useableSimpleFixCardOnHand = new LinkedList <Card> ();		//onefold fixed tool card that can be used on other players
	List <Card> fixCardOnHandForBrokenCard = new LinkedList <Card> ();		//fixed tool card that can be used on itself
	List <Card> complexFixCardOnHand = new LinkedList <Card> ();			//all complex fixed tool card

	/**
	 * Constructor
	 * @param name
	 */
	public ComputerPlayer(String name) {
		super(name);

		//init-possible gold card positions = {0,1,2,...,number of goal card};
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

	/**
	 * update the list of each card type on each hand
	 * call before each time select a card
	 */
	private void updateHandCards() {

		deadPathCardOnHand = new LinkedList <Card> ();
		normalCardOnHand = new LinkedList <Card> (); 
		mapOnHand = new LinkedList <Card> (); 
		rockFallOnHand = new LinkedList <Card> (); 
		allBreakCardOnHand = new LinkedList <Card> ();
		breakableCardOnHand = new LinkedList <Card> ();
		fixCardOnHandForBrokenCard = new LinkedList <Card> ();
		simpleFixCardOnHand = new LinkedList <Card> ();
		useableSimpleFixCardOnHand = new LinkedList <Card> ();
		complexFixCardOnHand = new LinkedList <Card> ();

		for (Card card : handCards) {
			if (card.isPathCard()) {
				if (((PathCard)card).isBlockPathCard()) 
					deadPathCardOnHand.add(card);
				else
					normalCardOnHand.add(card);
			}
			else if (card.isMap()) {
				mapOnHand.add(card);
			}
			else if (card.isRockfall()) {
				rockFallOnHand.add(card);
			}
			else if (card.isBrokenTool()) {
				allBreakCardOnHand.add(card);
				for (Player player : GameController.getPlayers()) {
					if (player != this && player.canToolBeBroken((BrokenToolCard)card)) {
						breakableCardOnHand.add(card);
						break;
					}
				}
			}
			else if (card.isFixedTool()) {
				if (canBeFixedBy((FixedToolCard)card)){
					fixCardOnHandForBrokenCard.add(card);
				}
				if (((FixedToolCard)card).getToolTypes().size() == 1) {
					simpleFixCardOnHand.add(card);
					for (Player player : GameController.getPlayers()) {
						if (player.canBeFixedBy((FixedToolCard)card)) {
							useableSimpleFixCardOnHand.add(card);
							break;
						}
					}
				} else {
					complexFixCardOnHand.add(card);
				}
			} 
		}
		fixCardOnHandForBrokenCard = fixCardOnHandForBrokenCard.stream().
			sorted((fix1, fix2) -> ((FixedToolCard)fix1).getToolTypes().size() - ((FixedToolCard)fix2).getToolTypes().size()).
			collect(Collectors.toList());
	}

	private double isSomeOneOnSameTeam() {
		return ((double)GameController.getMaxRoleNumber(role) - 1)/((double)GameController.getPlayers().length - 1);
	}

	private double isSomeOneOnOtherTeam() {
		double possibility = 0;
		for (Role r : Role.values()) {
			if (r != role) possibility += GameController.getMaxRoleNumber(r);
		}
		return (possibility)/((double)GameController.getPlayers().length - 1);
	}





	/////////////////
	///SELECT CARD///
	/////////////////


	private Entry<Card, Position> getOptimalPathCardWithPosition() {
		if (role == Role.SABOTEUR)
			return getOptimalPathCardWithPositionForSaboteur();
		else if (role == Role.RED_GOLD_MINER)
			return getOptimalPathCardWithPositionForGoldMiner(TeamColor.RED);
		else
			return getOptimalPathCardWithPositionForGoldMiner(TeamColor.BLUE);
	}

	private Entry<Card, Position> getOptimalPathCardWithPositionForGoldMiner(TeamColor color) {
		List<Card> handPathCards = normalCardOnHand;
		Set<Position> posSet = new HashSet<>();

		if (handPathCards.isEmpty()) {
			return new AbstractMap.SimpleEntry<Card, Position>(null, null);
		}
		
		for(Card card: handPathCards) {
			posSet.addAll(GameController.getAllPlaceablePosition((PathCard)card));
		}
		
		Position pos = posSet.stream().min(new PositionComparator()).orElse(null);
		
		while(pos != null) {
			//search through all Hand Path Card
			for(Card card: handPathCards) {
				//if this card can play at the given position
				if(GameController.canCardBePlacedAt(pos.x(), pos.y(), (PathCard)card)) {
					//traverse through all its anchor to find the matching neighbor
					for(CardAnchor anchor: ((PathCard)card).getGraph().vertices()) {
						BoardAnchor neighbor = BoardAnchor.of(anchor.getAdjacentPosition(pos), anchor.getOppositeAnchor());
						//ini Start BoardAnchor to ask if there is a path
						BoardAnchor startAnchor = BoardAnchor.of(GameController.getStartCard(color), CardAnchor.right);
						if (GameController.getGameboard().getGraph().hasPath(startAnchor, neighbor)){
							//check its connecting node and its next position
							Set<CardAnchor> vertices = ((PathCard)card).getGraph().getAdjacentVertices(anchor);
							for (CardAnchor nextAnchor : vertices) {
								if (new PositionComparator().compare(pos, nextAnchor.getAdjacentPosition(pos)) > 0) {
									return new AbstractMap.SimpleEntry<Card, Position>(card, pos);
								}
							}
						}	
					} 
				}
			}
			posSet.remove(pos);
			pos = posSet.stream().min(new PositionComparator()).orElse(null);
		}
		Card card = getRandomPlaceablePathCard ();
		if (card == null) return new AbstractMap.SimpleEntry<Card, Position>(null, null);
		List<Position> positions = GameController.getAllPlaceablePosition((PathCard)card).stream().collect(Collectors.toList());
		return new AbstractMap.SimpleEntry<Card, Position>(card, positions.get(0));
	}

	private Entry<Card, Position> getOptimalPathCardWithPositionForSaboteur(){

        //get all placeable positions from all dead Path Card on Hand
        Set<Position> allPlaceablePositions = new HashSet<>();
        for (Card deadCard : deadPathCardOnHand)
        {
            allPlaceablePositions.addAll(GameController.getAllPlaceablePosition((PathCard) deadCard));
        }
        Position pos = allPlaceablePositions.stream().min(new PositionComparator()).orElse(null);
        if (pos != null)
        {
            Card card = findPathCardFromPosition(pos, deadPathCardOnHand);
            return new AbstractMap.SimpleEntry<Card, Position>(card, pos);
        }
        return new AbstractMap.SimpleEntry<Card, Position>(null, null);
    }

	/** 
	 * @return a random card on hand
	 * the return card can never be {@code null}
	 */
	private Card getRandomCard() {
		return handCards.get((int) (Math.random() * handCards.size()));
	}

	private Card getRandomPlaceablePathCard () {
		LinkedList<Card> placeableNormalPathCard = new LinkedList<>();
		for (Card card : normalCardOnHand) {
			if (!GameController.getAllPlaceablePosition((PathCard)card).isEmpty()) {
				placeableNormalPathCard.add(card);
			}
		}
		Collections.shuffle(placeableNormalPathCard);
		return placeableNormalPathCard.isEmpty() ? null : placeableNormalPathCard.getFirst();
	}

	/**
	 * applied on Saboteur which was locked because of a broken tool
	 * @return an entry from card to position
	 * the return card must then not be discarded
	 * if the card is a {@link RockfallCard}, the return position is the position where it should be used
	 * otherwise the position is {@code null}
	 * if no card chosen (and hence no position chosen) then return an entry from null to null
	 */
	private Entry <Card,Position> getTargetCardForLOCKEDSaboteurToPlay() {
		if (!breakableCardOnHand.isEmpty()) {
			return new AbstractMap.SimpleEntry<Card,Position>(breakableCardOnHand.get(0), null);
		}
		if (!fixCardOnHandForBrokenCard.isEmpty()) {
			return new  AbstractMap.SimpleEntry<Card,Position>(fixCardOnHandForBrokenCard.get(0), null);
		}
		if ((!mapOnHand.isEmpty()) && (!goldCardDetected)) {
			return new  AbstractMap.SimpleEntry<Card,Position>(mapOnHand.get(0), null);
		}
		if (!rockFallOnHand.isEmpty()) {
			Position pos = getRockFallPositionForSaboteur();
				if (pos != null) 
					return new AbstractMap.SimpleEntry<Card,Position>(rockFallOnHand.get(0), pos);
		}
		return new AbstractMap.SimpleEntry<Card,Position>(null, null);
	}

	/**
	 * applied on unlocked Saboteur
	 * @return an entry from card to position
	 * the return card must then not be discarded
	 * if the card is a {@link RockfallCard} or {@link PathCard}, the return position is the position where it should be used
	 * otherwise the position is {@code null}
	 * if no card chosen (and hence no position chosen) then return an entry from null to null
	 */
	private Entry<Card, Position> getTargetCardForUNLOCKEDSaboteurToPlay() {
		if ((!mapOnHand.isEmpty()) && (!goldCardDetected)) {
			return new AbstractMap.SimpleEntry<Card,Position>(mapOnHand.get(0), null);
		}
		if (!breakableCardOnHand.isEmpty()) {
			return new AbstractMap.SimpleEntry<Card,Position>(breakableCardOnHand.get(0), null);
		}
		if (!deadPathCardOnHand.isEmpty()) {
			return getOptimalPathCardWithPosition();
		}
		if (!rockFallOnHand.isEmpty()) {
			Position pos = getRockFallPositionForSaboteur();
			if (pos != null) 
				return new AbstractMap.SimpleEntry<Card,Position>(rockFallOnHand.get(0), pos);
		}
		return new AbstractMap.SimpleEntry<Card,Position>(null, null);
	}

	/**
	 * 
	 * @return
	 */
	private Card getTargetCardForSaboteurToDisCard() {
		if (!normalCardOnHand.isEmpty()) {
			return normalCardOnHand.get(0);
		}
		if (!mapOnHand.isEmpty()) {
			return mapOnHand.get(0);
		}
		if (!simpleFixCardOnHand.isEmpty()) {
			return simpleFixCardOnHand.get(0);
		}
		if (!complexFixCardOnHand.isEmpty()) {
			return complexFixCardOnHand.get(0);
		}
		if (!rockFallOnHand.isEmpty()) {
			return rockFallOnHand.get(0);
		}
		if (!deadPathCardOnHand.isEmpty()) {
			return deadPathCardOnHand.get(0);
		}
		return getRandomCard();
	}

	
	/**
	 * applied on RED/BLUE Goldminer which was locked because of a broken tool
	 * @return an entry from card to position
	 * the return card must then not be discarded
	 * if the card is a {@link RockfallCard}, the return position is the position where it should be used
	 * otherwise the position is {@code null}
	 * if no card chosen (and hence no position chosen) then return an entry from null to null
	 */
	private Entry <Card,Position> getTargetCardForLOCKEDGoldMinerToPlay(){
		if (!fixCardOnHandForBrokenCard.isEmpty()) {
			return new AbstractMap.SimpleEntry<Card,Position>(fixCardOnHandForBrokenCard.get(0), null);
		}
		if ((!mapOnHand.isEmpty()) && (!goldCardDetected)) {
			return new AbstractMap.SimpleEntry<Card,Position>(mapOnHand.get(0),null);
		} 
		//only use these following card type if there is no path card on hand with no node
		if (deadPathCardOnHand.isEmpty()) {
			if (!rockFallOnHand.isEmpty()) {
				Position pos = getRockFallPositionForGoldMiner();
				if (pos != null) 
					return new AbstractMap.SimpleEntry<Card,Position>(rockFallOnHand.get(0), pos);
			}
			//TODO: Change 0.3 & 0.7 to the possibility of a player in the same team
			if ((!useableSimpleFixCardOnHand.isEmpty()) && (Math.random() < isSomeOneOnSameTeam())) {
				return new AbstractMap.SimpleEntry<Card,Position>(useableSimpleFixCardOnHand.get(0), null);
			}
			if ((!breakableCardOnHand.isEmpty()) && (Math.random() <  isSomeOneOnOtherTeam())) {
				return new AbstractMap.SimpleEntry<Card,Position>(breakableCardOnHand.get(0), null);
			}
		}
		return new AbstractMap.SimpleEntry<Card,Position>(null, null);
	}


	/**
	 * applied on unlocked RED/BLUE Goldminer
	 * @return an entry from card to position
	 * the return card must then not be discarded
	 * if the card is a {@link RockfallCard} or {@link PathCard}, the return position is the position where it should be used
	 * otherwise the position is {@code null}
	 * if no card chosen (and hence no position chosen) then return an entry from null to null
	 */
	private Entry<Card, Position> getTargetCardForUNLOCKEDGoldMinerToPlay() {
		if (!mapOnHand.isEmpty() && (!goldCardDetected)) {
			return new AbstractMap.SimpleEntry<Card,Position>(mapOnHand.get(0),null);
		}
		if (!normalCardOnHand.isEmpty()) {
			return getOptimalPathCardWithPosition();
		}
		if (!rockFallOnHand.isEmpty()) {
			Position pos = getRockFallPositionForGoldMiner();
			if (pos != null) {
				return new AbstractMap.SimpleEntry<Card,Position>(rockFallOnHand.get(0),pos);
			}
		}
		if (deadPathCardOnHand.isEmpty()) {
			//TODO: Change 0.3 & 0.7 to the possibility of a player in the same team
			if ((!useableSimpleFixCardOnHand.isEmpty()) && (Math.random() < isSomeOneOnSameTeam())) {
				return new AbstractMap.SimpleEntry<Card,Position>(useableSimpleFixCardOnHand.get(0), null);
			}
			if ((!breakableCardOnHand.isEmpty()) && (Math.random() < isSomeOneOnOtherTeam())) {
				return new AbstractMap.SimpleEntry<Card,Position>(breakableCardOnHand.get(0), null);
			}
		}
		return new AbstractMap.SimpleEntry<Card,Position>(null, null);
	}


	/**
	 * applied on RED/BLUE Goldminer, when the player has no card to be used
	 * (if getTargetCardForLOCKEDGoldMinerToPlay or getTargetCardForUNLOCKEDGoldMinerToPlay 
	 * it is then independent on whether the player is locked or not
	 * @return
	 */
	private Card getTargetCardForGoldMinerToDisCard () {
		if (!deadPathCardOnHand.isEmpty()) {
			return deadPathCardOnHand.get(0);
		}
		if (!mapOnHand.isEmpty()) {
			return mapOnHand.get(0);
		}
		if (!allBreakCardOnHand.isEmpty()) {
			return allBreakCardOnHand.get(0);
		}
		if (!normalCardOnHand.isEmpty()) {
			return normalCardOnHand.get(0);
		} 
		if (!rockFallOnHand.isEmpty()) {
			return rockFallOnHand.get(0);
		} 
		if (!simpleFixCardOnHand.isEmpty()) {
			return simpleFixCardOnHand.get(0);
		}
		return getRandomCard();
	}





	//////////////////
	////GET TARGET////
	//////////////////

	/**
	 * @param color {@link TeamColor} value
	 * @return a position that is need to be break to help the team {@code color}
	 */
	private Position getRockFallHelpingPosition(TeamColor color) {
		Map<Position, PathCard> blockBoard = new HashMap<>();
			
		Map<Position, PathCard> board = GameController.getGameboard().getBoard();
		for ( Position pos : board.keySet()) {
			PathCard card = board.get(pos);
			if (card.isGoalCard() || card.isStartCard())
				continue;
			if (card.isBlockPathCard())
			{
				if (GameController.getGameboard().existsPathFromStartCard(pos.x(), pos.y(), color))
					blockBoard.put(pos, card);
			}
		}
		return blockBoard.keySet().stream().min(new PositionComparator()).orElse(null);
	}
	
	/**
	 * @param color {@link TeamColor} value
	 * @return a position that is need to be break to sabotage the team {@code color}
	 */
	private Position getRockFallSabotagingPosition(TeamColor color) {
		Map<Position, PathCard> pathBoard = new HashMap<>();
		Map<Position, PathCard> board = GameController.getGameboard().getBoard();
		for ( Position pos : board.keySet()) {
			PathCard card = board.get(pos);
			if (card.isGoalCard() || card.isStartCard())
				continue;
			if (!card.isBlockPathCard() && 
					(GameController.getGameboard().existsPathFromStartCard(pos.x(), pos.y(), color)))
			{
				pathBoard.put(pos, card);
			}
		}
		return pathBoard.keySet().stream().min(new PositionComparator()).orElse(null);
	}

	/**
	 * @return a Position to break if the player is a RED or BLUE gold miner
	 */
	private Position getRockFallPositionForGoldMiner(){
		if (this.role == Role.RED_GOLD_MINER) {
			Position pos = getRockFallHelpingPosition(TeamColor.RED);
			if (pos == null) {
				return getRockFallSabotagingPosition(TeamColor.BLUE);
			}
			else
				return pos;
		}
		else if (this.role == Role.BLUE_GOLD_MINER) {
			Position pos = getRockFallHelpingPosition(TeamColor.BLUE);
			if (pos == null) {
				return getRockFallSabotagingPosition(TeamColor.RED);
			}
			else
				return pos;
		}
		return null;
	}

	/**
	 * @return a Position to break if the player is a Saboteur
	 */
	private Position getRockFallPositionForSaboteur() {
		Set<Position> rockFallPositions = new HashSet<>();
		Position redPos = getRockFallSabotagingPosition(TeamColor.RED);
		Position bluePos = getRockFallSabotagingPosition(TeamColor.BLUE);
		if (redPos != null)
			rockFallPositions.add(redPos);
		if (bluePos != null)
			rockFallPositions.add(bluePos);
		return rockFallPositions.stream().min(new PositionComparator()).orElse(null);		
	}

	/**
	 * @param players list of player whose tool can be broken by selected fixed tool card
	 * @return randomly a player to fix his/her/its tool
	 */
	private Player getTargetToBreak (LinkedList<Player> players) {
		if (players == null || players.isEmpty()) return null;
		Collections.shuffle(players);
		return players.isEmpty() ? null : players.getFirst();
	}
	
	/**
	 * @param players: list of player whose tool can be fixed by selected fixed tool card
	 * @return a player to fix his/her/its tool
	 */
	private Player getTargetToFix (LinkedList<Player> players) {
		if (players == null || players.isEmpty()) return null;
		if (players.contains(this)) return this;
		Collections.shuffle(players);
		return players.getFirst();
	}

	/**
	 * @param player: a target player whose tool need to be fixed the input {@link FixedToolCard}
	 * @param fixedToolCard to fix
	 * @return randomly a {@link BrokenToolCard} of the input @param player to fix
	 */
	private BrokenToolCard getTargetBrokenToolCard (Player player, FixedToolCard fixedToolCard) {
		LinkedList<BrokenToolCard> fixable = player.getAllBrokenToolCards(fixedToolCard);
		Collections.shuffle(fixable);
		return fixable.getFirst();
	}

	/**
	 * @param destroyableCards List of the position of destroyable pathcards
	 * @return randomly a position from the list
	 */
	private Position getRandomTargetRockfall(LinkedList<Position> destroyableCards) {
		if (destroyableCards.isEmpty()) return null;
		Collections.shuffle(destroyableCards);
		return destroyableCards.getFirst();
	}

	/**
	 * @param placeablePosition a list of position
	 * @return randomly a position in the list
	 */
	private Position getRandomPlacedPosition(Set<Position> placeablePosition) {
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

	private Card findPathCardFromPosition(Position pos, List<Card>pathCards) {
        for (Card pathCard : pathCards) {
            if (pathCard.isPathCard()) {
                if (GameController.getAllPlaceablePosition((PathCard) pathCard).contains(pos)) {
                    return pathCard;
                }
            }
        }
        return null;
    }





	////////////////////////////////
	//ESTIMATE GOLD CARD POSITIONS//
	////////////////////////////////

	
	/**
	 * @return a optimised ordinal number of a detected gold card
	 * if no gold card is yet detected then @return -1
	 */
	private int getGoldCardNo() {
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

	/**
	 * this methode updates the list of possible gold cards {@code possibleGoldCardPositions}
	 * is called when a map card is about to be used
	 * @param i (non-negative) ordinal number of a gold card
	 */
	private void goalCardUpdate(int i) {
		if (GameController.getGoalCardByNo(i).getType() == Type.Stone) {
			possibleGoldCardPositions.remove(Integer.valueOf(i));
		} else {
			goldCardDetected = true;
			possibleGoldCardPositions = new LinkedList<>(Arrays.asList(i));
		}
	}

	/**
	 * this methods update the ordinal numbers of possible gold card and analyse this list
	 * it is call when the player is about to select a card
	 */
	public void goalCardAnalyse() {
		if (!goldCardDetected) {
			for (int i = 0; i < GameController.getNumberOfGoalCard(); i++) {
				if ((!GameController.getGoalCardByNo(i).isCovered()) && GameController.getGoalCardByNo(i).getType() == Type.Stone) {
					possibleGoldCardPositions.remove(Integer.valueOf(i));
				} 
			}
			if (possibleGoldCardPositions.size() <= GameController.getNumberOfGoldCard()) {
				goldCardDetected = true;
			}
		}
		targetGoalPosition = getPredictedGoldCardPosition();
	}

	/**
	 * @return an optimised position that the player need to head to
	 */
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


	////////////////////
	////PLAYING CARD////
	////////////////////

	/**
	 * Führt einen Zug des Computerspielers aus.<br>
	 * Benutzt {@link #selectCard(Card)}, um eine Karte auszuwählen.<br>
	 * Benutzt Methoden in {@link GameController}, um Aktionen auszuführen.
	 */
	protected void doAction() {
		// TODO Aufgabe 4.3.3
		// Sie dürfen diese Methode vollständig umschreiben und den vorhandenen Code entfernen.

		// Analyse the game
		goalCardAnalyse();
		updateHandCards();

		//Chosing card
		Card card = getRandomCard();

		//1st Case: if the player is Gold Miner
		if (role != Role.SABOTEUR) {
			//if the player is locked
			if (hasBrokenTool()) {
				Entry <Card, Position> cardWithTargetPosition = getTargetCardForLOCKEDGoldMinerToPlay();
				card = cardWithTargetPosition.getKey();
				//if there is a card to use
				if (card != null) {
					playCard(card,cardWithTargetPosition.getValue());
				}
				//if there is no card to use (card == null), then pick a card to discard
				else {
					card = getTargetCardForGoldMinerToDisCard();
					if (card != null) {
						selectCard(card);
						GameController.discardSelectedCard();
					}
				}

			}
			//if the player is not locked
			else {
				Entry <Card, Position> cardWithTargetPosition = getTargetCardForUNLOCKEDGoldMinerToPlay();
				card = cardWithTargetPosition.getKey();
				//if there is a card to play
				if (card != null) {
					playCard(card, cardWithTargetPosition.getValue());
				}
				//if there is no card to play (card == null) and a card must be discard;
				else {
					card = getTargetCardForGoldMinerToDisCard();
					if (card != null) {
						selectCard(card);
						GameController.discardSelectedCard();
					}
				}
			}
		}
		//2nd Case: if the player is Saboteur
		else {
			//if the player is locked
			if (hasBrokenTool()) {
				Entry <Card, Position> cardWithTargetPosition = getTargetCardForLOCKEDSaboteurToPlay();
				card = cardWithTargetPosition.getKey();
				//if there is a card to use
				if (card != null) {
					playCard(card,cardWithTargetPosition.getValue());
				}
				//if there is no card to use
				else {
					card = getTargetCardForSaboteurToDisCard();
					if (card != null) {
						selectCard(card);
						GameController.discardSelectedCard();
					}
				}
			}
			//if the player is not locked
			else {
				Entry <Card, Position> cardWithTargetPosition = getTargetCardForUNLOCKEDSaboteurToPlay();
				card = cardWithTargetPosition.getKey();
				//if there is a card to play
				if (card != null) {
					playCard(card, cardWithTargetPosition.getValue());
				}
				//if there is no card to play (card == null) and a card must be discard;
				else {
					card = getTargetCardForSaboteurToDisCard();
					if (card != null) {
						selectCard(card);
						GameController.discardSelectedCard();
					}
				}
			}
		}
		
		//For Debugging
		//if the selected card stay at null
		//then randomly select a new card by getRandomCard()
		if (card == null) {
			card = getRandomCard();
			selectCard(card);
			if (card.isBrokenTool()) {
				playBrokenTool();
				return;
			}
	
			if (card.isFixedTool()) {
				playFixCard();
				return;
			}
	
			if (card.isMap()) {
				playMapCard();
				return;
			}
	
			if (card.isRockfall()) {
				playRockFall(getRandomTargetRockfall(GameController.getPathCardPositions()));
				return;
			}
	
			if (card.isPathCard()) {
				if (hasBrokenTool()) {
					GameController.discardSelectedCard();
				} else {
					playPathCard(getRandomPlacedPosition(GameController.getAllPlaceablePosition((PathCard)card)));	
				}
				return;
			}
		}
		
		// werfe Karte ab
		//GameController.discardSelectedCard();
	}

	private void playCard (Card card, Position pos) {
		selectCard(card);
		if (card.isMap()) {
			playMapCard();
		}
		else if (card.isRockfall()) {
			playRockFall(pos);
		}
		else if (card.isFixedTool()) {
			playFixCard();
		}
		else if (card.isBrokenTool()) {
			playBrokenTool();
		}
		else if (card.isPathCard() && (!hasBrokenTool())) {
			playPathCard(pos);
		}
		else {
			GameController.discardSelectedCard();	//in case of bugging
		}
	}

	/**
	 * play map card and call {@code goalCardUpdate(int)} to update the possible gold cards possition
	 */
	private void playMapCard() {
		int i = getGoldCardNo();
		goalCardUpdate(i);
		GameController.lookAtGoalCardWithSelectedCard(GameController.getGoalCardByNo(i));
	}

	/**
	 * place the selected path card on a position on the board
	 * @param pos the position that need to be placed on
	 */
	private void playPathCard(Position pos) {
		if (pos == null) {
			GameController.discardSelectedCard();
		} else {
			GameController.placeSelectedCardAt(pos);
		}
	}

	/**
	 * use a selected broken tool card to break tool of others player
	 * the player whose tool will be broken is chosen by {@code getTargetToBreak}
	 */
	private void playBrokenTool() {
		LinkedList <Player> targetPlayers = new LinkedList <Player>();
		Card card = GameController.getSelectedCard();
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
	}

	/**
	 * use a selected fixed tool card to fix a player
	 * the player whose tool need to be fixed is chosen by {@code getTargetToFix(targetPlayers)} 
	 */
	private void playFixCard() {
		Card card = GameController.getSelectedCard();
		LinkedList <Player> targetPlayers = new LinkedList <Player>();
		for (Player player : GameController.getPlayers()) {
			if (player.canBeFixedBy((FixedToolCard)card)){
				targetPlayers.add(player);
				}
		}
		Player targetPlayer = getTargetToFix(targetPlayers);
		if (targetPlayer == null) {
			GameController.discardSelectedCard();
		} 
		else {
			BrokenToolCard targetBrokenCard = getTargetBrokenToolCard(targetPlayer, (FixedToolCard)card);
			GameController.fixBrokenToolCardWithSelectedCard(targetPlayer, targetBrokenCard);
		}
	}

	/**
	 * use the selected Rock Fall card to destroy a PathCard
	 * @param pos the position of the Path card that need to be destroyed
	 */
	private void playRockFall(Position pos) {
		if (pos != null)
			GameController.destroyCardWithSelectedCardAt(pos.x(), pos.y());
		else
			GameController.discardSelectedCard();
	}
	

}