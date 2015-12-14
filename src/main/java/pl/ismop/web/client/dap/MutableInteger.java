package pl.ismop.web.client.dap;

public class MutableInteger {
	private int value;
	
	public MutableInteger(int initialValue) {
		value = initialValue;
	}
	
	public int get() {
		return value;
	}
	
	public void increment() {
		value++;
	}
	
	public void decrement() {
		value--;
	}
}