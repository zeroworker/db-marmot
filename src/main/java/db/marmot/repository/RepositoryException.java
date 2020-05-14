package db.marmot.repository;

/**
 * @author shaokang
 */
public class RepositoryException extends RuntimeException {
	
	public RepositoryException() {
	}
	
	public RepositoryException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}
	
	public RepositoryException(String arg0) {
		super(arg0);
	}
	
	public RepositoryException(Throwable arg0) {
		super(arg0);
	}
}
