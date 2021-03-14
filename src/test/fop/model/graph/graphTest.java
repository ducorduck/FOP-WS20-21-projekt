package fop.model.graph;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class graphTest {
	
	private Graph<String> graph;
	
	public graphTest() {
		
	}
	
	@BeforeEach
	public void setup() {
		graph = new Graph<>();
		graph.addVertex("a");
		graph.addVertex("b");
		graph.addVertex("c");
		graph.addEdge("a", "b");
		graph.addEdge("c", "a");
		
	}
	
	@Test
	public void testAddVertex() {
		assertEquals(true, graph.hasVertex("a"));
		assertEquals(true, graph.hasVertex("b"));
		assertEquals(true, graph.hasVertex("c"));
		
		assertEquals(false, graph.hasVertex("g"));
		assertEquals(false, graph.hasVertex("f"));
		assertEquals(false, graph.hasVertex("h"));
	}
	
	@Test
	public void testAddEdge() {
		assertEquals(true, graph.addEdge("d", "a"));
		assertEquals(false, graph.addEdge("a", "d"));
		assertEquals(true, graph.addEdge("g", "h"));
		
		assertEquals(true, graph.hasVertex("d"));
		assertEquals(true, graph.hasVertex("g"));
		assertEquals(true, graph.hasVertex("h"));
		
		assertEquals(true, graph.hasEdge("a", "b"));
		
		assertEquals(true, graph.hasEdge("a", "c"));
		
		assertEquals(true, graph.hasEdge("a", "d"));
		
		assertEquals(true, graph.hasEdge("g", "h"));
		
		assertEquals(false, graph.hasEdge("c", "b"));
		
		assertEquals(false, graph.hasEdge("d", "c"));
		
		
	}
	
	@Test
	public void testRemoveVertex() {
		graph.addEdge("d", "a");
		graph.addEdge("d", "c");
		assertEquals(true, graph.removeVertex("d"));
		
		assertEquals(false, graph.removeVertex("f"));
		
		assertEquals(false, graph.vertices().contains("d"));
		assertEquals(false, graph.hasEdge("a", "d"));
	}
	
	@Test
	public void testRemoveEdge() {
		assertEquals(true, graph.removeEdge("a", "b"));
		assertEquals(false, graph.removeEdge("c", "b"));
		assertEquals(false, graph.removeEdge("c", "f"));
		
		assertEquals(false, graph.removeEdge("g", "f"));
		
		assertEquals(false, graph.hasEdge("a", "b"));
	}
	
	@Test
	public void testHasPath() {
		graph.removeEdge("a", "c");
		graph.addEdge("b", "c");
		graph.addEdge("b", "d");
		graph.addEdge("c", "e");
		graph.addEdge("c", "f");
		graph.addEdge("d", "g");
		graph.addEdge("d", "h");
		
		assertEquals(true, graph.hasPath("a", "h"));
		assertEquals(true, graph.hasPath("a", "b"));
		assertEquals(true, graph.hasPath("a", "c"));
		assertEquals(true, graph.hasPath("a", "d"));
		assertEquals(true, graph.hasPath("a", "e"));
		assertEquals(true, graph.hasPath("a", "f"));
		assertEquals(true, graph.hasPath("a", "g"));
		
		graph.addEdge("aa", "bb");
		graph.addEdge("cc", "bb");
		graph.addEdge("dd", "bb");
		graph.addEdge("cc", "ee");
		graph.addEdge("cc", "ff");
		graph.addEdge("dd", "gg");
		graph.addEdge("dd", "hh");
		
		assertEquals(false, graph.hasPath("a", "aa"));
		assertEquals(false, graph.hasPath("c", "bb"));
		assertEquals(false, graph.hasPath("g", "cc"));
		assertEquals(false, graph.hasPath("b", "dd"));
		assertEquals(false, graph.hasPath("f", "ff"));
		assertEquals(false, graph.hasPath("h", "gg"));
		assertEquals(false, graph.hasPath("d", "ee"));
		assertEquals(false, graph.hasPath("a", "hh"));
		
		graph.addEdge("h", "ee");
		
		assertEquals(true, graph.hasPath("f", "gg"));
		
		assertEquals(false, graph.hasPath("d", "zz"));
		assertEquals(false, graph.hasPath("z", "zz"));
		
	}
	
	
}
