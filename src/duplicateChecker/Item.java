package duplicateChecker;

public class Item {
	protected String url, name;
	protected int page;

	public Item(String url, String name) {
		this.url = url;
		this.name = name;
		page = 1;
	}

	@Override
	public String toString() {
		return name + " | " + url;
	}

	public String getName() {
		return name;
	}

	public String getURL() {
		return url;
	}

	public int getPage() {
		return page;
	}
}
