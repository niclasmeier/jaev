package net.nicl.jaev.integration;

public class ExternalResult {

	private ExternalResultCode code;

	private String translatedCode;

	private String validity;

	private Object[] objects;

	private Boolean accepted;

	public ExternalResultCode getCode() {
		return this.code;
	}

	public void setCode(ExternalResultCode code) {
		this.code = code;
	}

	public String getValidity() {
		return this.validity;
	}

	public void setValidity(String validity) {
		this.validity = validity;
	}

	public Object[] getObjects() {
		return this.objects;
	}

	public void setObjects(Object[] objects) {
		this.objects = objects;
	}

	public String getTranslatedCode() {
		return this.translatedCode;
	}

	public void setTranslatedCode(String translatedCode) {
		this.translatedCode = translatedCode;
	}

	public Boolean isAccepted() {
		return this.accepted;
	}

	public void setAccepted(Boolean accepted) {
		this.accepted = accepted;
	}

}
