package de.chrisnew.zerk.console;

public class ConsoleVariable<T> {
	private final T defaultValue;
	private final T currentValue;
	private final T latchValue;

	public ConsoleVariable(String name, T defaultValue) {
		this.defaultValue = defaultValue;
		currentValue = defaultValue;
		latchValue = defaultValue;
	}

	public T getValue() {
		return currentValue;
	}
}
