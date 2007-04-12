package com.arcaner.warlock.client.internal;

import java.util.ArrayList;

import com.arcaner.warlock.client.IProperty;
import com.arcaner.warlock.client.IPropertyListener;

public class Property<T> implements IProperty<T> {

	protected String name;
	protected T data;
	protected ArrayList<IPropertyListener<T>> listeners = new ArrayList<IPropertyListener<T>>();
	
	public Property(String name) {
		this.name = name;
	}
	
	public void set(T data) {
		T oldData = this.data;
		
		this.data = data;
		for(IPropertyListener<T> listener : listeners) {
			listener.propertyChanged(this, oldData);
		}
	}
	
	public String getName() {
		return name;
	}
	
	public T get() {
		return data;
	}
	
	public void clear() {
		T oldData = this.data;
		data = null;
		
		for(IPropertyListener<T> listener : listeners) {
			listener.propertyCleared(this, oldData);
		}
	}
	
	public void activate() {
		for (IPropertyListener<T> listener : listeners) {
			listener.propertyActivated(this);
		}
	}
	
	public void addListener(IPropertyListener<T> listener) {
		listeners.add(listener);
	}
	
	public boolean removeListener(IPropertyListener<T> listener) {
		return listeners.remove(listener);
	}
	
	@Override
	public String toString() {
		return getName() + "=" + get();
	}
}
