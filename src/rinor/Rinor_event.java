package rinor;

public class Rinor_event {
	public int ticket_id = 0;
	public int device_id;
	public String key;
	public String Value;
	
	public Rinor_event ( int ticket, int dev_id, String key, String value) {
		this.ticket_id = ticket;
		this.device_id = dev_id;
		this.key = key;
		this.Value = value;
	}
	
}
