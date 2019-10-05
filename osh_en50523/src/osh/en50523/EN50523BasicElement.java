package osh.en50523;

/**
 * Source: DIN EN 50523-1:2010-05, p.26
 * @author Ingo Mauser
 *
 */
public enum EN50523BasicElement {
	
	CHANGE 		(1, "�nderung"),
	SEND 		(2, "Senden"),
	REQUEST 	(3, "Abfrage"),
	RESPONSE	(4, "R�ckgabe");
	
	private byte elementID;
	private String descriptionDE;
	
	
	EN50523BasicElement(int elementID, String descriptionDE) {
		this.elementID = (byte) elementID;
		this.descriptionDE = descriptionDE;
	}


	public byte getElementID() {
		return elementID;
	}

	public String getDescriptionDE() {
		return descriptionDE;
	}

}
