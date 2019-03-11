package httpclient;

/**
 * A class for defining a MIME resource request.
 * 
 * @author Dennis Debree
 * @author Jonas Bertels
 *
 */
public class ResourceRequest {
	
	/**
	 * Constructs a new resource request.
	 * 
	 * @param path the path of this request.
	 * @param contentType the type of the resource that is being requested.
	 */
	public ResourceRequest(String path, ContentType contentType) {
		if(path == null || path == "") {
			throw new IllegalArgumentException("The given path is not valid");
		}
		if(contentType == null || contentType == ContentType.UNKNOWN) {
			throw new IllegalArgumentException("Given content type is not valid or not implemented");
		}
		
		this.path = path;
		this.contentType = contentType;
	}

	/**
	 * The path for this request.
	 */
	private String path;
	
	/**
	 * The content type for this request.
	 */
	private ContentType contentType;
	
	/**
	 * Returns the path for this request.
	 * @return the path
	 */
	public String getPath() {
		return path;
	}
	
	/**
	 * Returns the type of the requested resource.
	 * @return the type of the resource.
	 */
	public ContentType getContentType() {
		return contentType;
	}
}
