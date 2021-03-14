package fop.model.graph;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 
 * Modelliert einen ungerichteten Graph mit Knoten des Typs {@code V} durch eine Adjazenzliste.
 *
 * @param <V> Typ der Knoten des Graphen
 */
public class Graph<V> {
	
	/** Die Adjazenzliste des Graphen. */
	protected Map<V, Set<V>> G = new HashMap<>();
	
	/**
	 * Erstellt einen leeren ungerichteten Graphen.
	 */
	public Graph() {}
	
	/**
	 * Entfernt alle Knoten und Kanten des Graphen.
	 */
	public void clear() {
		G.clear();
	}
	
	
	// add //
	
	/**
	 * Fügt den übergebenen Knoten hinzu.
	 * @param v der Knoten, der hinzugefügt werden soll
	 */
	public void addVertex(V v) {
		// TODO Aufgabe 4.1.1
		//create an empty adjacent list
		Set<V> set = new HashSet<>();
		G.putIfAbsent(v, set);
	}
	
	/**
	 * Fügt eine Kante von Knoten {@code x} nach Knoten {@code y} hinzu.
	 * @param x der Startknoten der Kante
	 * @param y der Endknoten der Kante
	 * @return {@code true} wenn die Kante hinzugefügt wurde;
	 *         {@code false} wenn sie bereits existiert hat
	 */
	public boolean addEdge(V x, V y) {
		// TODO Aufgabe 4.1.1
		//check if the graph already has x and y and add any missing node
		if (!G.containsKey(x)) 
            addVertex(x);        
		
		if (!G.containsKey(y)) 
            addVertex(y);        
		//check if the edge already exists
		if(G.get(x).contains(y) && G.get(y).contains(x))
			return false;
		
		//add y to x's adjacent list
		Set<V> setX = G.get(x);
		setX.add(y);
		G.put(x, setX);
		//add x to y's adjacent list
		Set<V> setY = G.get(y);
		setY.add(x);
	    G.put(y, setY);
		return true;
	}
	
	
	// remove //
	
	/**
	 * Entfernt den Knoten {@code v} und alle mit ihm verbundenen Kanten.
	 * @param v der Knoten, der entfernt werden soll
	 * @return {@code true} wenn der Knoten entfernt wurde;
	 *         {@code false} wenn er nicht existiert hat
	 */
	public boolean removeVertex(V v) {
		// TODO Aufgabe 4.1.1
		//check if the graph has v
		if(!G.containsKey(v)) {
			return false;
		}
		//remove the node from the graph
		G.remove(v);
		//remove the node from all other nodes' adjacent list
		for(V vertex: G.keySet()) {
			//get set of edges connected to v
			Set<V> set = G.get(vertex);
			//remove edges connected to vertex v
           		if(set.contains(v))
               			set.remove(v);
            
		}
		return true;
	}
	
	/**
	 * Entfernt die Kante von Knoten {@code x} nach Knoten {@code y}.
	 * @param x der Startknoten der Kante
	 * @param y der Endknoten der Kante
	 * @return {@code true} wenn die Kante entfernt wurde;
	 *         {@code false} wenn die Kante nicht existiert hat
	 */
	public boolean removeEdge(V x, V y) {
		// TODO Aufgabe 4.1.1
		//check if the path already exists
		if(!G.get(x).contains(y) || !G.get(y).contains(x)) {
			return false;
		}
		//check if the graph has x and y
		if(!G.containsKey(x) || !G.containsKey(y)) {
			return false;
		}
		//remove y from  x's adjacent list
		Set<V> setX = G.get(x);
		setX.remove(y);
		G.put(x, setX);
		
		//remove x from  y's adjacent list
		Set<V> setY = G.get(y);
		setY.remove(x);
		G.put(y, setY);
		return true;
	}
	
	
	// has //
	
	/**
	 * Prüft, ob der Graph den Knoten {@code v} besitzt.
	 * @param v der zu überprüfende Knoten
	 * @return {@code true} wenn der Knoten existiert; sonst {@code false}
	 */
	public boolean hasVertex(V v) {
		return G.containsKey(v);
	}
	
	/**
	 * Prüft, ob der Graph eine Kante vom Knoten {@code x} zum Knoten {@code y} besitzt.
	 * @param x der Startknoten der Kante
	 * @param y der Endknoten der Kante
	 * @return {@code true} wenn die Kante existiert; sonst {@code false}
	 */
	public boolean hasEdge(V x, V y) {
		return G.containsKey(x) && G.get(x).contains(y);
	}
	
	/**
	 * Prüft, ob ein Pfad vom Knoten {@code x} zum Knoten {@code y} existiert.
	 * @param x der Startknoten des Pfads
	 * @param y der Endknoten des Pfads
	 * @return {@code true} wenn ein Pfad existiert; sonst {@code false}
	 */
	public boolean hasPath(V x, V y) {
		// TODO Aufgabe 4.1.2
		//check if the graph has x and y
		if (this.hasVertex(x) && this.hasVertex(y)) {
			//check if x and y are the same
			if (x.equals(y)) return true;
			//set of already visited nodes
			Set<V> visited = new HashSet<V>();
			//first visit x
			visited.add(x);
			//set of not yet visited nodes
			Set<V> unvisited = new HashSet<V>();
			//first add the adjacent list of x to the list
			for(V item : G.get(x)) {
				unvisited.add(item);
			}
			//stop if we've visited all possible nodes from x
			while(unvisited.size() != 0 ) {
				V tempItem = null;
				//get the item to be assessed
				for(V item : unvisited) {
					if(item.equals(y))return true;
					tempItem = item;
					break;
				}
				
				if(tempItem != null) {
					//get the adjacent list of the item
					Set<V> tempSet = G.get(tempItem); 
					//add all the items that are not already visited or are not already in the set of unvisited nodes in the unvisited nodes
					for(V item : tempSet) {
						if(item.equals(y))return true;
						if(!visited.contains(item) && !unvisited.contains(item)) {
							unvisited.add(item);
						}
					}
					//mark the item as visited
					unvisited.remove(tempItem);
					visited.add(tempItem);
				}
			}
			//check if we've already visited the wanted node
			for(V item : visited) {
				if(item.equals(y)) return true;
			}
		}
		return false;
	}
	
	
	// Collections //
	
	/**
	 * Gibt die Menge aller Knoten zurück.
	 * @return die Menge aller Knoten
	 */
	public Set<V> vertices() {
		return G.keySet();
	}
	
	/**
	 * Gibt die Menge aller benachbarter Knoten des Knoten {@code v} zurück.
	 * @param v der Knoten, dessen Nachbarn zurückgegeben werden sollen
	 * @return die Menge aller benachbarter Knoten
	 */
	public Set<V> getAdjacentVertices(V v) {
		return G.get(v);
	}
	
	/**
	 * Gibt die Menge aller Kanten zurück.
	 * @return die Menge aller Kanten
	 */
	public Set<Edge<V>> edges() {
		Set<Edge<V>> edges = new HashSet<>();
		G.forEach((x, m) -> m.forEach(y -> {
			edges.add(Edge.of(x, y));
		}));
		return edges;
	}
	
	/**
	 * Gibt die Menge aller Kanten zurück, wobei immer nur die Kante in eine Richtung gezählt wird.<br>
	 * <i>Beispiel:</i><br>
	 * Statt {@code [(x, y), (y, x)]} wird {@code [(x, y)]} ausgegeben.
	 * @return die Menge aller Kanten
	 */
	private Set<Edge<V>> singleEdges() {
		Set<Edge<V>> edges = new HashSet<>();
		G.forEach((x, m) -> m.forEach(y -> {
			if (!edges.contains(Edge.of(x, y)) && !edges.contains(Edge.of(y, x))) edges.add(Edge.of(x, y));
		}));
		return edges;
	}
	
	
	// Visualization //
	
	/**
	 * Liefert eine Darstellung des Graphen in {@code dot}-Sprache.<br>
	 * Mittels {@code graph.toDotCode.forEach(System.out::println)} kann dieser Code auf der Konsole ausgegeben werden.<br>
	 * Auf {@code http://webgraphviz.com/} kann der Code dargestellt werden.
	 * @return die einzelnen Zeilen des {@code dot}-Codes
	 */
	public List<String> toDotCode() {
		List<String> l = new ArrayList<>();
		l.add("graph {");
		Comparator<Object> byString = (a, b) -> a.toString().compareTo(b.toString());
		vertices().stream().sorted(byString).forEach(v -> l.add(String.format("\t\"%s\" [label=\"%s\"];", v, v)));
		singleEdges().stream().sorted(byString).forEach(key -> {
			l.add(String.format("\t\"%s\" -- \"%s\";", key.x(), key.y()));
		});
		l.add("}");
		return l;
	}
	
	/**
	 * Zum Debuggen kann hiermit der Graph ausgegeben werden.<br>
	 * Auf {@code http://webgraphviz.com/} kann der Code dargestellt werden.
	 */
	public void printGraph() {
		toDotCode().forEach(System.out::println);
	}
	
	
	// Object //
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		sb.append(vertices().stream().sorted().collect(Collectors.toList()).toString());
		sb.append(", ");
		sb.append("{");
		if (singleEdges().isEmpty())
			sb.append("  ");
		else {
			Comparator<Object> byString = (a, b) -> a.toString().compareTo(b.toString());
			singleEdges().stream().sorted(byString).forEach(key -> {
				sb.append(String.format("%s<->%s, ", key.x(), key.y()));
			});
		}
		sb.replace(sb.length() - 2, sb.length(), "");
		sb.append("}");
		sb.append(")");
		return sb.toString();
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (G == null ? 0 : G.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Graph<?> other = (Graph<?>) obj;
		if (G == null) {
			if (other.G != null) return false;
		} else if (!G.equals(other.G)) return false;
		return true;
	}
	
}
