package fop.model.board;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import fop.model.cards.CardAnchor;
import fop.model.cards.GoalCard;
import fop.model.cards.PathCard;
import fop.model.graph.Graph;

/**
 * 
 * Stellt das Wegelabyrinth als Liste von Karten und als Graph dar.
 *
 */
public class Gameboard {
	
	protected final Map<Position,PathCard> board = new HashMap<>();
	protected final Graph<BoardAnchor> graph = new Graph<>();
	
	/**
	 * Erstellt ein leeres Wegelabyrinth und platziert Start- sowie Zielkarten.
	 */
	public Gameboard() {
		clear();
	}
	
	/**
	 * Zum Debuggen kann hiermit der Graph ausgegeben werden.<br>
	 * Auf {@code http://webgraphviz.com/} kann der Code dargestellt werden.
	 */
	public void printGraph() {
		graph.toDotCode().forEach(System.out::println);
	}
	
	/**
	 * Leert das Wegelabyrinth.
	 */
	public void clear() {
		board.clear();
		graph.clear();
	}
	
	// add, remove //
	
	/**
	 * Setzt eine neue Wegekarte in das Wegelabyrinth.<br>
	 * Verbindet dabei alle Kanten des Graphen zu benachbarten Karten,
	 * sofern diese einen Knoten an der benachbarten Stelle besitzen.
	 * @param x x-Position im Wegelabyrinth
	 * @param y y-Position im Wegelabyrinth
	 * @param card die zu platzierende Wegekarte
	 */
	public void placeCard(int x, int y, PathCard card) {
		// TODO Aufgabe 4.1.4
		Position pos = Position.of(x, y);
		//check goal card
		if(card.isGoalCard()) {
			board.put(pos, card);
			GoalCard goal = (GoalCard) card;
			if(!goal.isCovered()) {
				Set<CardAnchor> goalAnchors = new HashSet<>();
				for(CardAnchor anchor : card.getGraph().vertices()) {
					goalAnchors.add(anchor);
				}
				for(CardAnchor anchor : goalAnchors) {
					for(CardAnchor anchor2 : goalAnchors) {
						if(anchor2.equals(anchor)) continue;
						if(card.getGraph().hasEdge(anchor, anchor2)) {
							graph.addEdge(BoardAnchor.of(pos, anchor), BoardAnchor.of(pos, anchor2));
						}
					}
				}
			}
		}
		//add all the card's vertices to the board's graph
		Set<CardAnchor> cardAnchors = new HashSet<>();
		for(CardAnchor anchor : card.getGraph().vertices()) {
			cardAnchors.add(anchor);
		}
		//check start card
		if(card.isStartCard()) {
			board.put(pos,card );
			for(CardAnchor anchor : cardAnchors) {
				for(CardAnchor anchor2 : cardAnchors) {
					if(anchor2.equals(anchor)) continue;
					if(card.getGraph().hasEdge(anchor, anchor2)) {
						graph.addEdge(BoardAnchor.of(pos, anchor), BoardAnchor.of(pos, anchor2));
					}
				}
			}
			return;
		}
		//check if the card can be placed at the given position
		else if(canCardBePlacedAt(x,y,card)) {
			//put the card on the board
			board.put(pos, card);
			for(CardAnchor anchor : cardAnchors) {
				BoardAnchor boardAnchor = BoardAnchor.of(pos,anchor);
				graph.addVertex(boardAnchor);
				//connect the card's vertices to the existing board's vertices  
				BoardAnchor next = BoardAnchor.of(anchor.getAdjacentPosition(pos), anchor.getOppositeAnchor());
				if(graph.hasVertex(next)) {
					graph.addEdge(next, boardAnchor);
					}
				}
			//connect all the cards internal vertices according to the card's graph
			for(CardAnchor anchor : cardAnchors) {
				for(CardAnchor anchor2 : cardAnchors) {
					if(anchor2.equals(anchor)) continue;
					if(card.getGraph().hasEdge(anchor, anchor2)) {
						graph.addEdge(BoardAnchor.of(pos, anchor), BoardAnchor.of(pos, anchor2));
					}
				}
			}
		}
		// stehen lassen
		// check for goal cards
		checkGoalCards();
	}
	
	/**
	 * Prüft, ob eine Zielkarte erreichbar ist und dreht diese gegebenenfalls um.
	 */
	private void checkGoalCards() {
		for (Entry<Position, PathCard> goal : board.entrySet().stream().filter(e -> e.getValue().isGoalCard()).collect(Collectors.toList())) {
			int x = goal.getKey().x();
			int y = goal.getKey().y();
			if (existsPathFromStartCard(x, y)) {
				GoalCard goalCard = (GoalCard) goal.getValue();
				if (goalCard.isCovered()) {
					// turn card
					goalCard.showFront();
					// generate graph to match all neighbor cards
					goalCard.generateGraph(card -> doesCardMatchItsNeighbors(x, y, card));
					// connect graph of card
					placeCard(x, y, goalCard);
				}
			}
		}
	}
	
	/**
	 * Entfernt die Wegekarte an der übergebenen Position.
	 * @param x x-Position im Wegelabyrinth
	 * @param y y-Position im Wegelabyrinth
	 * @return die Karte, die an der Position lag
	 */
	public PathCard removeCard(int x, int y) {
		// TODO Aufgabe 4.1.5
		Position pos = new Position(x,y);
		if(!isPositionEmpty(x,y)) {
		//the removed card
		PathCard result = board.get(pos);
		//remove the card from the board
			board.remove(pos);
		//the the card's vertices from the board's graph
		for(CardAnchor anchor : result.getGraph().vertices()) {
			BoardAnchor boardAnchor = BoardAnchor.of(pos, anchor);
			graph.removeVertex(boardAnchor);
		}
		return result;
		}
		return null;
	}
	
	
	// can //
	
	/**
	 * Gibt genau dann {@code true} zurück, wenn die übergebene Karte an der übergebene Position platziert werden kann.
	 * @param x x-Position im Wegelabyrinth
	 * @param y y-Position im Wegelabyrinth
	 * @param card die zu testende Karte
	 * @return {@code true}, wenn die Karte dort platziert werden kann; sonst {@code false}
	 */
	public boolean canCardBePlacedAt(int x, int y, PathCard card) {
		return isPositionEmpty(x, y) && existsPathFromStartCard(x, y) && doesCardMatchItsNeighbors(x, y, card);
	}
	
	/**
	 * Gibt genau dann {@code true} zurück, wenn auf der übergebenen Position keine Karte liegt.
	 * @param x x-Position im Wegelabyrinth
	 * @param y y-Position im Wegelabyrinth
	 * @return {@code true}, wenn der Platz frei ist; sonst {@code false}
	 */
	private boolean isPositionEmpty(int x, int y) {
		// TODO Aufgabe 4.1.6
		if(!board.containsKey(Position.of(x, y))) {
			return true;
		}
		return  false;
	}
	
	/**
	 * Gibt genau dann {@code true} zurück, wenn die übergebene Position von einer Startkarte aus erreicht werden kann.
	 * @param x x-Position im Wegelabyrinth
	 * @param y y-Position im Wegelabyrinth
	 * @return {@code true}, wenn die Position erreichbar ist; sonst {@code false}
	 */
	private boolean existsPathFromStartCard(int x, int y) {
		// TODO Aufgabe 4.1.7
		Position thisPos = Position.of(x, y);
		//the set of all the anchors adjacent to the given position
		Set<BoardAnchor> adjAnchors = new HashSet<>();
		for(CardAnchor anchor : CardAnchor.values()){
			BoardAnchor boardanchor = BoardAnchor.of(anchor.getAdjacentPosition(thisPos), anchor.getOppositeAnchor());
			adjAnchors.add(boardanchor);
		}
		for(Position pos : board.keySet()) {
			//get the startcards
			if(board.get(pos).isStartCard()) {
				//get the start card's anchors
				Set<BoardAnchor> startAnchors = new HashSet<>();	
				for(BoardAnchor anchor : graph.vertices()) {
					if (anchor.x() == pos.x() && anchor.y() == pos.y()) {
						startAnchors.add(anchor);
					}
				}
				//check if there exists a path to any of the anchors adjacent to the given position from any start card
				for(BoardAnchor start : startAnchors) {
					for(BoardAnchor end : adjAnchors) {
						if(graph.hasPath(start, end)) return true;
					}
				}
			}
		}
		// die folgende Zeile entfernen und durch den korrekten Wert ersetzen
		return false;
	}
	
	/**
	 * Gibt genau dann {@code true} zurück, wenn die übergebene Karte an der übergebene Position zu ihren Nachbarn passt.
	 * @param x x-Position im Wegelabyrinth
	 * @param y y-Position im Wegelabyrinth
	 * @param card die zu testende Karte
	 * @return {@code true}, wenn die Karte dort zu ihren Nachbarn passt; sonst {@code false}
	 */
	private boolean doesCardMatchItsNeighbors(int x, int y, PathCard card) {
		// TODO Aufgabe 4.1.8
		Position pos = Position.of(x, y);
		//set of this card's anchors
		Set<CardAnchor> anchors = new HashSet<>();
		for(CardAnchor 	anchor : card.getGraph().vertices()) {
			anchors.add(anchor);
		}
		
		//map of this card's neighbors and their direction
		Map<CardAnchor,PathCard> neighbors = new HashMap<>();
		for(CardAnchor anchor : CardAnchor.values()) {
			if(!isPositionEmpty(anchor.getAdjacentPosition(pos).x(),anchor.getAdjacentPosition(pos).y())) {
				neighbors.put(anchor,board.get(anchor.getAdjacentPosition(pos)));
			}
		}
		//remove any covered goal card from the map of neighbors
		Set<CardAnchor> removeAnchorNeighbor = new HashSet<>();
		for(CardAnchor anchor : neighbors.keySet()) {
			if(neighbors.get(anchor).isGoalCard()) {
				GoalCard goalCard = (GoalCard) neighbors.get(anchor);
				if(goalCard.isCovered()) 
					removeAnchorNeighbor.add(anchor);					
			}
			
		}
		for(CardAnchor anchor: removeAnchorNeighbor) {
			neighbors.remove(anchor);
		}
		
		//array of the passed status of the neighboring cards
		boolean[] passed = new boolean[neighbors.size()];
		for(int a = 0; a < passed.length; a++) {
			passed[a] = false;
		}
		int i = 0;
		//check if the cards' anchors match with each other
		for(CardAnchor anchor : neighbors.keySet()) { 
			Set<CardAnchor> neighborsAnchors = neighbors.get(anchor).getGraph().vertices();
			if(anchors.contains(anchor)) { //anchors.contains(anchor)
				if(neighborsAnchors.contains(anchor.getOppositeAnchor()))
					passed[i] = true; 
			}
			else {
				passed[i] = true;
				if(neighborsAnchors.contains(anchor.getOppositeAnchor()))
					passed[i] = false;

			}
			i++;
		}
		boolean result = true;
		for(int a = 0; a < passed.length; a++) {
			result = result & passed[a];
		}
		return result;
	}
	
	
	/**
	 * Gibt genau dann {@code true} zurück, wenn eine aufgedeckte Goldkarte im Wegelabyrinth liegt.
	 * @return {@code true} wenn eine Goldkarte aufgedeckt ist; sonst {@code false}
	 */
	public boolean isGoldCardVisible() {
		return board.values().stream().anyMatch(c -> c.isGoalCard() && ((GoalCard) c).getType() == GoalCard.Type.Gold && !((GoalCard) c).isCovered());
	}
	
	
	// get //
	
	public Map<Position, PathCard> getBoard() {
		return board;
	}
	
	public int getNumberOfAdjacentCards(int x, int y) {
		Set<Position> neighborPositions = Set.of(Position.of(x - 1, y), Position.of(x + 1, y), Position.of(x, y - 1), Position.of(x, y + 1));
		return (int) board.keySet().stream().filter(pos -> neighborPositions.contains(pos)).count();
	}
	
}
