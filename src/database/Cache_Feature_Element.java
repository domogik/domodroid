package database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import widgets.Entity_client;


public class Cache_Feature_Element {
	public int DevId  = -1;
	public String skey = null;
	public String Value = null;
	public ArrayList<Entity_client> clients_list ;
	
	public Cache_Feature_Element(int devid, String skey,String value) {
		super();
		this.DevId = devid;
		this.skey = skey;
		this.Value = value;
		clients_list = new ArrayList<Entity_client>() ;
	}
	public int add_client(Entity_client new_client) {
		// Check if it's for the good feature
		if( (new_client.getDevId() == DevId) && (new_client.getskey().equals(skey))) {
			// Ok, the good one !
			for(int i = 0; i < clients_list.size(); i++) {
				if(clients_list.get(i) == new_client) {
					//already exist in List
					new_client.setValue(Value);
					return -1;
				}
			}
			//not yet exists in list
			clients_list.add(new_client);					// add this client at end of list
			new_client.setValue(Value);
			new_client.setClientId(clients_list.size()-1);	//set index into caller structure
		}
		return -1;
	}
	public int remove_client(Entity_client client) {
		// Check if it's for the good feature
		if( (client.getDevId() == DevId) && client.getskey().equals(skey)) {
			// Ok, the good device !
			if( (client.getClientId() >= 0) && (client.getClientId() < clients_list.size()) ) {
				//index into list is coherent
				if(clients_list.get(client.getClientId()) == client) {
					//index is the good one : remove this client from list...
					clients_list.remove(client.getClientId());
					client.setClientId(-1);	//Not anymore connected to this device
					return -1;
				}
			}
		}
		return -1;
	}
}
