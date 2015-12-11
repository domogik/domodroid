package mq;

/**
 * Created by mpunie on 12/05/2015.
 */
import android.os.Parcelable;

public abstract class MQMessage implements Parcelable {
	protected String message;
	protected String id;

	public MQMessage(){}

	public String getMessage() {
		return message;
	}
	public String getId() {
		return id;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String toString() {
		return "Key: " + getId() + ", Value: " + getMessage();
	}
}
