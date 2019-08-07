package linkCheckerFailedDrafts.linkCheckerOLD;

import tools.WebTools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static linkCheckerFailedDrafts.linkCheckerOLD.Trace.tab;

public class Tree {
    private Node root;

	private Map<String, Node> nodeMap;
	private List<Node> invalidNodes;
	private Map<Node, Exception> nonAccessibleNodes;

    public Tree(String rootUrl) {
    	nodeMap = new HashMap<>();
    	invalidNodes = new ArrayList<>();
    	nonAccessibleNodes = new HashMap<>();


        root = new Node(new Link(rootUrl, "**ROOT**"), 0);
        nodeMap.put(rootUrl, root);
    }

    public Node getRoot() {
    	return root;
	}

	public boolean contains(String url) {
    	return nodeMap.containsKey(url);
	}

	public List<Node> getInvalidNodes() {
    	return invalidNodes;
	}

	@Override
	public String toString() {
    	return output(root, "");
	}

	private static String output(Node parent, String runningOutput) {
		runningOutput += "\n" + tab(parent.getLevel()) + parent.getLink();
    	for (Node child : parent.getChildren()) {
    		output(child, runningOutput);
		}

    	return runningOutput;
	}
    
    class Node {

    	private Node parent;
    	private List<Node> children;
    	
    	private Link link;
    	private String url;
    	private boolean valid, heroarts;
    	private int level;
    	
    	public Node(Link link, int level) {
    		this.link = link;
    		url = link.getAddress();

			heroarts = link.isHeroArts();
    		
    		children = new ArrayList<Node>();

    		nodeMap.put(url, this);

			try {
				valid = WebTools.isValid(url);
				if (!valid) {
					invalidNodes.add(this);
					System.out.println("INVALID:\n" + this.getTrace() + "\n\n");
				}
			} catch (Exception e) {
				valid = false;
				nonAccessibleNodes.put(this, e);
				System.out.println("CHECK:\n" + this.getTrace() + "\n\n");
			}
    	}

    	public String getUrl() {
    		return url;
		}

		public Link getLink() {
    		return link;
		}

		public boolean isHeroArts() {
    		return heroarts;
		}

		public boolean isValid() {
    		return valid;
		}

    	private void setParent(Node parent) {
    		this.parent = parent;
    	}
    	
    	private Node getParent() {
    		return parent;
    	}
    	
    	private void addChild(Node child) {
    		children.add(child);
    		child.setParent(this);
    	}

    	public void addChild(Link link) {
    		addChild(new Node(link, level + 1));
		}
    	
    	public List<Node> getChildren() {
    		return children;
    	}

    	public int getLevel() {
    		return level;
		}

		public Trace getTrace() {
			Trace trace = new Trace();

			trace.add(this);
			Node parent;
			while ((parent = trace.get(0).getParent()) != null) {
				trace.add(0, parent);
			}

			return trace;
		}
    	
    	public boolean matches(String url) {
    		return url.equals(this.url);
    	}
    }
}
