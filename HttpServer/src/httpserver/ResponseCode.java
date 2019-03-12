package httpserver;

public enum ResponseCode {
	SUCCESS(200, "OK"), CREATED(201, "Created"), NOT_FOUND(404, "Not Found"), BAD_REQUEST(400, "Bad Request"),
	SERVER_ERROR(500, "Server Error"), NOT_MODIFIED(304, "Not Modified");
	
	
	private ResponseCode(int code, String message) {
		this.code = code;
		this.message = message;
	}
	
	private int code;
	
	private String message;
	
	public int getCode() {
		return code;
	}
	
	public String getMessage() {
		return message;
	}
}
