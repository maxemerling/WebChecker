package duplicateChecker;

public class Product extends Item {
	private String number;

	public Product(String url, String number, String name, int page) {
		super(url, name);
		this.number = number;
		this.page = page;
	}

	public String getNumber() {
		return number;
	}

	@Override
	public String toString() {
		return number + " | " + name + " | " + url + " | " + page;
	}
}
