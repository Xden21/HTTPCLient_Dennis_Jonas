package httpclient;

/**
 * Enum describing all supported content types.
 * 
 * @author Dennis Debree
 * @author Jonas Bertels
 */
public enum ContentType {
	HTML("html"),IMAGEJPG("jpg"), IMAGEPNG("png"), IMAGEGIF("gif"), UNKNOWN(""), TEXT(".txt");
	
	/**
	 * Creates a new content type
	 * 
	 * @param extension the extension of the content.
	 */
	private ContentType(String extension) {
		this.extension = extension;
	}
	
	/**
	 * The extension of the content.
	 */
	private String extension;
	
	/**
	 * Gets the extension of the content
	 * @return the extension.
	 */
	public String getExtension() {
		return extension;
	}
}
