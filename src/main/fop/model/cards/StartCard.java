package fop.model.cards;

import static fop.model.cards.CardAnchor.bottom;
import static fop.model.cards.CardAnchor.left;
import static fop.model.cards.CardAnchor.right;
import static fop.model.cards.CardAnchor.top;
import static fop.model.TeamColor.*;

import java.util.List;

import fop.model.TeamColor;
import fop.model.graph.Graph;

/**
 * 
 * Stellt die Start-Wegekarte dar.
 *
 */
public final class StartCard extends PathCard {
	
	private final TeamColor color;

	public StartCard(Graph<CardAnchor> graph, TeamColor color) {
		super(color == RED ? "start_red" : "start_blue", graph);
		this.color = color;
	}
	
	public StartCard(TeamColor color) {
		super(color == RED ? "start_red" : "start_blue", List.of(List.of(left, bottom, right, top)));
		this.color = color;
	}

	public TeamColor getColor() {
		return color;
	}
	
	/**
	 * {@inheritDoc}
	 * @see fop.model.cards.Card#isStartCard()
	 */
	@Override
	public boolean isStartCard() {
		return true;
	}
	
	@Override
	public String toString() {
		return "StartCard";
	}
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		if (getClass() != obj.getClass()) return false;
		return true;
	}
	
}
