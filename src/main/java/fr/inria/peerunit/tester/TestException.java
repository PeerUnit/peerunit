package fr.inria.peerunit.tester;


public abstract class TestException extends AssertionError {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public TestException() {
		super();
	}
	
	/**
	 * @param message
	 */
	public TestException(String message) {
		super(message);
	}
}
