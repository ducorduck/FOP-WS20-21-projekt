package fop.model.cards;

import static fop.model.cards.CardAnchor.bottom;
import static fop.model.cards.CardAnchor.left;
import static fop.model.cards.CardAnchor.right;
import static fop.model.cards.CardAnchor.top;

import java.util.List;

import fop.model.graph.Graph;

/**
 * 
 * Stellt die Start-Wegekarte dar.
 *
 */
public final class StartCard extends PathCard {
	
	private final boolean isRed;

	public StartCard(Graph<CardAnchor> graph, boolean isRed) {
		super(isRed? "start" : "start2", graph);
		this.isRed = isRed;
	}
	
	public StartCard(boolean isRed) {
		super(isRed? "start" : "start2", List.of(List.of(left, bottom, right, top)));
		this.isRed = isRed;
	}

	public boolean isRed() {
		return isRed;
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
