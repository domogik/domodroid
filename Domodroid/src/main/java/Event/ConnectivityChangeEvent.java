package Event;

/**
 * Created by tiki on 12/03/2017.
 */

public class ConnectivityChangeEvent {
    private boolean isConnected;
    private boolean isWifiConnected;
    private int networkType;
    public Boolean on_preferred_Wifi;

    public ConnectivityChangeEvent() {
    }

    public ConnectivityChangeEvent(int networkType, boolean isConnected, boolean isWifiConnected, boolean on_preferred_Wifi) {
        this.networkType = networkType;
        this.isConnected = isConnected;
        this.isWifiConnected = isWifiConnected;
        this.on_preferred_Wifi = on_preferred_Wifi;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setIsConnected(boolean isConnected) {
        this.isConnected = isConnected;
    }

    public boolean isWifiConnected() {
        return isWifiConnected;
    }

    public void setIsWifiConnected(boolean isWifiConnected) {
        this.isWifiConnected = isWifiConnected;
    }

    public int getNetworkType() {
        return networkType;
    }

    public void setNetworkType(int networkType) {
        this.networkType = networkType;
    }

    public boolean getOn_preferred_Wifi() {
        return on_preferred_Wifi;
    }

    public void setOn_preferred_Wifi(boolean on_preferred_Wifi) {
        this.on_preferred_Wifi = on_preferred_Wifi;
    }
}
