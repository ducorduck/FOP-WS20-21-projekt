package fop.controller;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import fop.io.ActionCardReader;
import fop.io.PathCardReader;
import fop.io.ScoreEntryIO;
import fop.model.ComputerPlayer;
import fop.model.Player;
import fop.model.Player.Role;
import fop.model.ScoreEntry;
import fop.model.TeamColor;
import fop.model.board.Gameboard;
import fop.model.board.Position;
import fop.model.cards.*;
import fop.model.cards.GoalCard.Type;
import javax.swing.SwingWorker;
import static fop.model.TeamColor.*;

/**
 * 
 * Verwaltet das Gameplay.
 *
 */
public final class GameController {
	
	private GameController() {}
	
	// Konstanten
	private static final String ACTION_CARDS = "/actioncards.xml";
	private static final String PATH_CARDS = "/pathcards.xml";
	
	// Spielvariablen
	private static final List<Player> players = new ArrayList<>();
	private static final Stack<Card> drawDeck = new Stack<>();
	private static final Stack<Card> discardPile = new Stack<>();
	private static final Gameboard gameboard = new Gameboard();
	
	private static int activePlayer = -1;
	private static Card selectedCard = null;

	private static final int numberOfGoalCard = 3;
	private static final int cardsBetweenStartAndGoal = 6;
	private static final int numberOfGoldCard = 1;
	private static final GoalCard[] allGoalCards = new GoalCard[numberOfGoalCard];
	
	
	//////////
	// INIT //
	//////////
	
	/**
	 * Setzt das gesamte Spiel zurück.<br>
	 * Wichtig: Alle Property Change Listener müssen nach
	 * dem Aufruf dieser Methode gesetzt werden.
	 */
	public static void reset() {
		players.clear();
		drawDeck.clear();
		discardPile.clear();
		gameboard.clear();
		activePlayer = -1;
		selectedCard = null;
		for (PropertyChangeListener pcl : pcs.getPropertyChangeListeners())
			pcs.removePropertyChangeListener(pcl);
	}
	
	/**
	 * Fügt einen neuen Spieler hinzu.
	 * @param name der Name des Spielers
	 * @param isComputer gibt an, ob der Spieler ein Computergegner ist
	 * @see fop.model.Player.Role
	 */
	public static void addPlayer(String name, boolean isComputer) {
		if (isComputer)
			players.add(new ComputerPlayer(name));
		else players.add(new Player(name));
	}
	
	/**
	 * Startet das Spiel.<br>
	 * Dabei werden die Rollen verteilt, der Kartenstapel erstellt und gemischt und das Wegelabyrinth initialisiert.
	 */
	public static void startGame() {
		assignRoles();
		initCards();
		dealStartCards();
		firePropertyChange(SELECT_CARD, null);
		initMaze();
		new SwingWorker<Object, Void>() {
			
			@Override
			protected Object doInBackground() throws Exception {
				TimeUnit.SECONDS.sleep(1);
				nextPlayer();
				return null;
			}
		}.execute();
	}
	
	public static int getMaxRoleNumber (Role role) {
		int playerCount = players.size();
		if (role == Role.SABOTEUR) {
			return playerCount <= 10 ? List.of(0, 0, 1, 1, 1, 1, 2, 2, 2, 3, 3).get(playerCount) : playerCount - 2 * (1 + playerCount / 3);
		} 
		else if (role == Role.BLUE_GOLD_MINER) {
			return playerCount <= 10 ? List.of(0, 0, 0, 1, 2, 2, 2, 3, 3, 3, 4).get(playerCount) : 1 + playerCount / 3;
		} 
		else { //role == Role.RED_GOLD_MINER
			return playerCount <= 10 ? List.of(0, 1, 1, 1, 2, 2, 2, 3, 3, 3, 4).get(playerCount) : 1 + playerCount / 3;
		}
	}


	/**
	 * Setzt die Rollen der Spieler.
	 */
	public static void assignRoles() {
		if (players.isEmpty()) return;
		
		List<Role> roles = new LinkedList<>();
		for (int i = 0; i < getMaxRoleNumber(Role.SABOTEUR); i++)
			roles.add(Role.SABOTEUR);
		for (int i = 0; i < getMaxRoleNumber(Role.RED_GOLD_MINER); i++)
			roles.add(Role.RED_GOLD_MINER);
		for (int i = 0; i < getMaxRoleNumber(Role.BLUE_GOLD_MINER); i++) {
			roles.add(Role.BLUE_GOLD_MINER);
		}
		
		Collections.shuffle(roles);
		for (Player player : players)
			player.assignRole(roles.remove(0));
	}
	
	/**
	 * Initialisiert und mischt den Nachziehstapel.
	 */
	private static void initCards() {
		drawDeck.clear();
		initActionCards(); // Aktionskarten
		initPathCards();   // Wegekarten
		Collections.shuffle(drawDeck); // Karten mischen
	}
	
	/**
	 * Fügt dem {@link #drawDeck} alle Aktionskarten hinzu.
	 */
	private static void initActionCards() {
		// read action cards
		List<ActionCard> actionCards = ActionCardReader.readFromResource(ACTION_CARDS);
		
		// add cards to deck
		drawDeck.addAll(actionCards);
	}
	
	/**
	 * Fügt dem {@link #drawDeck} alle Wegekarten hinzu.
	 */
	private static void initPathCards() {
		// read path cards
		List<PathCard> pathCards = PathCardReader.readFromResource(PATH_CARDS);
		
		// rotate 50% of cards randomly
		for (PathCard card : pathCards)
			if (Math.random() <= 0.5) card.rotate();
		
		// add cards to deck
		drawDeck.addAll(pathCards);
	}

	/**
	 * Verteilt die Startkarten an alle Spieler.
	 */
	private static void dealStartCards() {
		int playerCount = players.size();
		int cardCount = playerCount <= 7 ? playerCount <= 5 ? 6 : 5 : 4;
		for (int i = 0; i < cardCount; i++)
			for (Player player : players)
				drawCard(player);
	}
	
	/**
	 * Initialisiert das Wegelabyrinth.
	 */
	private static void initMaze() {

		boolean hasBlue = false;
		for (Player player :players) {
			if (player.getRole() == Role.BLUE_GOLD_MINER)
				hasBlue = true;
		}
		//Place Start card(s)
		gameboard.placeCard(0, 0, new StartCard(RED));
		if (hasBlue) 
			gameboard.placeCard(2 + 2*cardsBetweenStartAndGoal,0 , new StartCard(BLUE));
		
		//Place Goal cards
		List<GoalCard> goalCards = new LinkedList<>();
		for (int i = 0; i < numberOfGoldCard; i++) {
			goalCards.add(new GoalCard(Type.Gold));
		}
		for (int i = 0; i < getNumberOfStoneCard(); i++) {
			goalCards.add(new GoalCard(Type.Stone));
		}
		Collections.shuffle(goalCards);
		for (int i = 0; i < numberOfGoalCard; i++) {
			gameboard.placeCard(getGoalCardPosition(i), goalCards.get(0));
			allGoalCards[i] = goalCards.remove(0);
		}
	}
	
	
	////////////
	// GETTER //
	////////////
	
	public static Player[] getPlayers() {
		return players.toArray(Player[]::new);
	}
	
	public static Player getActivePlayer() {
		if (activePlayer == -1) return null;
		return players.get(activePlayer);
	}
	
	public static Position getStartCard (TeamColor color) {
		return color == RED? Position.of(0,0) : Position.of(2 + 2*cardsBetweenStartAndGoal,0);
	}

	public static Position getGoalCardPosition (int i) {
		int x = 1 + cardsBetweenStartAndGoal;
		int y = numberOfGoalCard - 1 - 2*i;
		return Position.of(x,y);
	}

	public static GoalCard getGoalCardByNo(int i) {
		return allGoalCards[i];
	}

	public static Card getSelectedCard() {
		return selectedCard;
	}
	
	public static Gameboard getGameboard() {
		return gameboard;
	}
	
	public static Set<Position> getCardPositions() {
		return gameboard.getBoard().keySet();
	}
	
	public static LinkedList<Position> getPathCardPositions() {
		LinkedList<Position> pathCardPositions = new LinkedList<Position>();
		for (Position pos : gameboard.getBoard().keySet()) {
			boolean pass = false;
			if (pos.equals(getStartCard(RED)) || pos.equals(getStartCard(BLUE))) continue;
			for (int i = 0; i < numberOfGoalCard; i++) {
				if (pos.equals(getGoalCardPosition(i))) {
					pass = true;
					break;
				}
			}
			if (pass) {
				continue;
			}
			pathCardPositions.add(pos);
		}
		return pathCardPositions;
	}

	public static PathCard getCardAt(Position pos) {
		return gameboard.getBoard().getOrDefault(pos, null);
	}
	
	public static boolean canCardBePlacedAt(int x, int y, PathCard card) {
		return gameboard.canCardBePlacedAt(x, y, card);
	}
	
	public static int getDrawDeckSize() {
		return drawDeck.size();
	}
	
	public static List<Card> getDiscardPile() {
		return new ArrayList<>(discardPile);
	}

	public static int getNumberOfGoalCard() {
		return numberOfGoalCard;
	}

	public static int getNumberOfGoldCard() {
		return numberOfGoldCard;
	}
	
	public static int getNumberOfStoneCard() {
		return numberOfGoalCard - numberOfGoldCard;
	}
	
	public static Position getCentrerPostion() {
		return Position.of(1+cardsBetweenStartAndGoal,0);
	}
	
	public static Set<Position> getAllPlaceablePosition(PathCard card) {
        //init result var
        Set<Position> result = new HashSet<>();
        //get map of board in game
        Map<Position, PathCard> board = getGameboard().getBoard();
        for (Position pos : board.keySet()) {
            PathCard pathCard = board.get(pos);
            //ignor all the covered goal card
            if (pathCard.isGoalCard()) {
                GoalCard goldCard = (GoalCard) pathCard;
                if (goldCard.isCovered())
                    continue;
            }
            //add all card anchor of this path card to a list
            List<CardAnchor> anchors = new ArrayList<>();
            for (CardAnchor anchor : pathCard.getGraph().vertices()) {
                anchors.add(anchor);
            }
            for (CardAnchor anchor : anchors) {
                Position adjacentPosition = anchor.getAdjacentPosition(pos);
                if (canCardBePlacedAt(adjacentPosition.x(), adjacentPosition.y(), card))
                    result.add(adjacentPosition);
            }
        }
        return result;
    }

    public static Set<Position> getAllPlaceablePosition(PathCard... pathCards){
        Set<Position> result = new HashSet<>();
        for (PathCard pathCard : pathCards) {
            result.addAll(getAllPlaceablePosition(pathCard));
        }
        return result;
    }




	//////////////
	// GAMEPLAY //
	//////////////
	
	public static List<Player> getWinners() {
        // TODO Aufgabe 4.3.2
        // Sie dürfen diese Methode vollständig umschreiben und den vorhandenen Code entfernen.

        // Goldkarte wurde aufgedeckt -> Goldsucher gewinnen
        if (gameboard.isGoldCardVisible()) {
			List<Position> positions = new LinkedList<>();
			for (int i = 0; i < numberOfGoalCard; i++) {
				positions.add(getGoalCardPosition(i));
			}
            int x = 0;
            int y = 0;
            for (Position pos : positions) {
                GoalCard goldcard = (GoalCard) gameboard.getBoard().get(pos);
                if (!goldcard.isCovered()) {
                    x = pos.x();
                    y = pos.y();
                }
            }
			List <Player> goldMinerWinners = new ArrayList<Player>();
            if (gameboard.existsPathFromStartCard(x, y, RED))
                goldMinerWinners.addAll(players.stream().filter(p -> p.getRole() == Player.Role.RED_GOLD_MINER).collect(Collectors.toList()));
            if (gameboard.existsPathFromStartCard(x, y, BLUE))
				goldMinerWinners.addAll(players.stream().filter(p -> p.getRole() == Player.Role.BLUE_GOLD_MINER).collect(Collectors.toList()));
			return goldMinerWinners;
        }

        // keine Karten mehr übrig -> Saboteure gewinnen
        if (drawDeck.isEmpty() && players.stream().allMatch(p -> p.getAllHandCards().isEmpty()))
            return players.stream().filter(p -> p.getRole() == Player.Role.SABOTEUR).collect(Collectors.toList());

        // noch kein Gewinner
        return null;
    }

	/**
	 * Beendet den Zug des aktuellen aktiven Spielers und startet den Zug des nächsten Spielers.<br>
	 * Dabei zieht der alte aktive Spieler eine Karte nach.
	 */
	private static void nextPlayer() {
        // Spielende prüfen
        List<Player> winners = getWinners();
        if (winners != null) {
            if (winners.get(0).getRole() != Role.SABOTEUR) {
                getActivePlayer().scorePoints(5);
            }
            // Siegpunkte verteilen
            for (Player player : winners) {
                final int winPoints = 20;
                if (player.getRole() == Role.SABOTEUR)
                    player.scorePoints(winPoints / 2);
                player.scorePoints(winPoints);
            }
            // Highscores speichern
            LocalDateTime now = LocalDateTime.now();
            for (Player player : players) {
                ScoreEntry scoreEntry = new ScoreEntry(player.getName(), now, player.getScore());
                ScoreEntryIO.addScoreEntry(scoreEntry);
            }
            // Spielende signalisieren
            selectCard(null);
            activePlayer = -1;
            firePropertyChange(NEXT_PLAYER);
            firePropertyChange(GAME_OVER, winners);
            return;
        }

        // Karte nachziehen
        drawCard();

        // der nächste Spieler ist am Zug
        int nextActivePlayer = activePlayer + 1;
        if (nextActivePlayer == players.size()) nextActivePlayer = 0;

        // alle Karten verstecken
        selectCard(null);
        activePlayer = -1;
        firePropertyChange(NEXT_PLAYER);

        // nächsten Spieler informieren
        firePropertyChange(INFORM_NEXT_PLAYER, players.get(nextActivePlayer));

        // Karten des aktiven Spielers zeigen
        activePlayer = nextActivePlayer;
        firePropertyChange(NEXT_PLAYER);
        if (getActivePlayer().getAllHandCards().isEmpty())
            firePropertyChange(ACTIVE_PLAYER_NO_HAND_CARDS);
    }

	
	
	/**
	 * Der übergebene Spieler zieht eine Karte vom Nachziehstapel.
	 * @param player der Spieler, der eine Karten zieht
	 * @return die gezogene Karte; oder null
	 */
	private static Card drawCard(Player player) {
		// wenn das Deck leer ist geht das Spiel weiter bis kein Spieler mehr Handkarten hat
		if (drawDeck.isEmpty()) return null;
		if (player == null) return null;
		Card card = drawDeck.pop();
		player.drawCard(card);
		return card;
	}
	
	private static void drawCard() {
		Card card = drawCard(getActivePlayer());
		firePropertyChange(DRAW_CARD, card);
	}
	
	/**
	 * Benutzt die ausgewählte Karte des aktiven Spielers.
	 */
	private static void playSelectedCard() {
		getActivePlayer().playCard(selectedCard);
		selectCard(null);
	}
	
	/**
	 * Wählt eine Karte aus. Dementsprechend wird die Benutzeroberfläche aktualisiert.
	 * @param card die auszuwählende Karte
	 */
	public static void selectCard(Card card) {
		selectedCard = card;
		firePropertyChange(SELECT_CARD);
	}
	
	/**
	 * Gibt dem aktiven Spieler die übergebene Anzahl Punkte.
	 * @param points die zu vergebenden Punkte
	 */
	private static void scorePoints(int points) {
		getActivePlayer().scorePoints(points);
	}
	
	// POSSIBLE ACTIONS //
	
	/**
	 * Beendet den Zug des aktiven Spielers.<br>
	 * Danach ist der nächste Spieler an der Reihe.
	 */
	public static void doNothing() {
		nextPlayer();
	}
	
	/**
	 * Setzt die ausgewählte Karte in das Wegelabyrinth.<br>
	 * Danach ist der nächste Spieler an der Reihe.
	 * @param x x-Position im Wegelabyrinth
	 * @param y y-Position im Wegelabyrinth
	 * @see Gameboard#placeCard(int, int, PathCard)
	 */
	public static void placeSelectedCardAt(int x, int y) {
		gameboard.placeCard(x, y, (PathCard) selectedCard);
		playSelectedCard();
		scorePoints(gameboard.getNumberOfAdjacentCards(x, y) + 1);
		nextPlayer();
	}
	
	
	public static void placeSelectedCardAt(Position pos) {
		placeSelectedCardAt(pos.x(), pos.y());
	}

	/**
	 * Zerstört die Wegekarte an der übergebenen Position mit der ausgewählten Karte.<br>
	 * Legt die zerstörte und die ausgewählte Karte auf den Ablagestapel.<br>
	 * Danach ist der nächste Spieler an der Reihe.
	 * @param x x-Position im Wegelabyrinth
	 * @param y y-Position im Wegelabyrinth
	 * @see Gameboard#removeCard(int, int)
	 */
	public static void destroyCardWithSelectedCardAt(int x, int y) {
        PathCard oldCard = gameboard.removeCard(x, y);
        discardPile.add(oldCard);
        discardPile.add(selectedCard);
        playSelectedCard();
        if (getActivePlayer().getRole() == Role.SABOTEUR && players.size() > 2)
            scorePoints(3);
        else
            scorePoints(2);
        nextPlayer();
    }


	/**
	 * Repariert die Karte mit dem zerbrochenen Werkzeug mit der ausgewählten Karte.<br>
	 * Legt die Karte mit dem zerbrochenen Werkzeug und die ausgewählte Karte auf den Ablagestapel.<br>
	 * Danach ist der nächste Spieler an der Reihe.
	 * @param player der Spieler, dessen Werkzeug repariert wird
	 * @param brokenToolCard die Karte, die repariert wird
	 * @see Player#fixBrokenTool(BrokenToolCard, FixedToolCard)
	 */
	public static void fixBrokenToolCardWithSelectedCard(Player player, BrokenToolCard brokenToolCard) {
		player.fixBrokenTool(brokenToolCard, (FixedToolCard) selectedCard);
		discardPile.add(brokenToolCard);
		discardPile.add(selectedCard);
		playSelectedCard();
		if (getActivePlayer().getRole() != Role.SABOTEUR) scorePoints(3); 
		else scorePoints(2);
		nextPlayer();
	}
	
	/**
	 * Zerstört das Werkzeug eines Spielers mit der ausgewählten Karte.<br>
	 * Danach ist der nächste Spieler an der Reihe.
	 * @param player der Spieler, dessen Werkzeug zerstört wird
	 */
	public static void breakToolWithSelectedCard(Player player) {
        player.breakTool((BrokenToolCard) selectedCard);
        playSelectedCard();
        if (getActivePlayer().getRole() == Role.SABOTEUR && players.size() > 2)
            scorePoints(3);
        else
            scorePoints(2);
        nextPlayer();
    }
	
	/**
	 * Schaut die übergebene Zielkarte an.<br>
	 * Legt die ausgewählte Karte auf den Ablagestapel.<br>
	 * Danach ist der nächste Spieler an der Reihe.
	 * @param goalCard die anzuschauende Zielkarte
	 */
	public static void lookAtGoalCardWithSelectedCard(GoalCard goalCard) {
		firePropertyChange(LOOK_AT_GOAL_CARD, goalCard);
		discardPile.add(selectedCard);
		playSelectedCard();
		nextPlayer();
	}
	
	/**
	 * Legt die ausgewählte Karte auf den Ablagestapel.<br>
	 * Danach ist der nächste Spieler an der Reihe.
	 */
	public static void discardSelectedCard() {
		discardPile.add(selectedCard);
		playSelectedCard();
		nextPlayer();
	}
	
	
	//////////////
	// LISTENER //
	//////////////
	
	private static PropertyChangeSupport pcs = new PropertyChangeSupport(GameController.class);
	
	// CONSTANTS //
	
	/**
	 * Wird aktiviert, wenn das Spiel beendet ist.<br>
	 * newValue (List&lt;Player&gt;): die Gewinner des Spiels
	 */
	public static final String GAME_OVER = "game_over";
	
	/**
	 * Wird aktiviert, wenn ein neuer Spieler am Zug ist.
	 */
	public static final String NEXT_PLAYER = "next_player";
	
	/**
	 * Wird aktiviert, wenn der nächste aktive Spieler über seinen Zug informiert werden soll.<br>
	 * newValue (Player): der nächste aktive Spieler
	 */
	public static final String INFORM_NEXT_PLAYER = "prepare_next_player";
	
	/**
	 * Wird aktiviert, wenn der aktive Spieler am Zug ist, aber keine Handkarten mehr zur Verfügung hat.
	 */
	public static final String ACTIVE_PLAYER_NO_HAND_CARDS = "active_player_no_hand_cards";
	
	/**
	 * Wird aktiviert, wenn eine Karte ausgewählt wird.
	 */
	public static final String SELECT_CARD = "select_card";
	
	/**
	 * Wird aktiviert, wenn eine Karte nachgezogen wird.<br>
	 * newValue (Card): die neue Karte; oder null
	 */
	public static final String DRAW_CARD = "draw_card";
	
	/**
	 * Wird aktiviert, wenn eine Zielkarte angeschaut wird.<br>
	 * newValue (GoalCard): die angeschaute Zielkarte
	 */
	public static final String LOOK_AT_GOAL_CARD = "look_at_goal_card";
	
	// METHODS //
	
	public static void addPropertyChangeListener(String name, PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(name, listener);
	}
	
	private static void firePropertyChange(String name, Object value) {
		pcs.firePropertyChange(name, null, value);
	}
	
	private static void firePropertyChange(String name) {
		pcs.firePropertyChange(name, null, null);
	}
	
}
