
public class InvalidAssemblyException extends RuntimeException{
	private static final long serialVersionUID = 1L;

	public InvalidAssemblyException() {
		super();
	}

	public InvalidAssemblyException(String message, int lineNumber) {
		super(message + " on line " + lineNumber);
	}
}
