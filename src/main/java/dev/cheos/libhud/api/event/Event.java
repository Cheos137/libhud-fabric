package dev.cheos.libhud.api.event;

public class Event {
	private final EventPhase phase;
	private boolean cancelled;
	boolean cancellable;
	
	protected Event(EventPhase phase) {
		this(true, phase);
	}
	
	protected Event(boolean cancellable, EventPhase phase) {
		this.cancellable = cancellable && phase == EventPhase.PRE;
		this.phase = phase;
	}
	
	public boolean isCancellable() {
		return this.cancellable;
	}
	
	public boolean isCancelled() {
		return isCancellable() && this.cancelled;
	}
	
	public void cancel() {
		if (!isCancellable()) throw new UnsupportedOperationException(getClass().getSimpleName() + " is not cancellable");
		this.cancelled = true;
	}
	
	public EventPhase getPhase() {
		return this.phase;
	}
	
	
	public static enum EventPhase {
		NONE, PRE, POST;
	}
}
