package htmlclient;

public enum ContentType {
	HTML("html"),IMAGEJPG("jpg"), IMAGEPNG("png"), IMAGEGIF("gif"), UNKNOWN("");
	
	private ContentType(String extension) {
		this.extension = extension;
	}
	
	private String extension;
	
	public String getExtension() {
		return extension;
	}
}
