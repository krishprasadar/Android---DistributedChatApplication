package ds.chat;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.widget.Toast;

/**
 * Device information that needs to be stored and sent across messages.
 * Enabling Wi-Fi on device check is also done.
 */
public class DeviceInfo {
    public static String MacAddress;
    public static String IpAddress;
    private final Context context;

    public DeviceInfo(Context context) {
        this.context = context;

    }

    public boolean isWiFiEnabled()
    {
        WifiManager wManager = (WifiManager)context.getSystemService(context.WIFI_SERVICE);
        if(!wManager.isWifiEnabled()) {
            Toast.makeText(context, "Enable Wi-fi from Setting Menu", Toast.LENGTH_SHORT).show();
            return false;
        }
        else
        {
            return true;
        }
    }

    public void setDeviceInfo()
    {

        WifiInfo wifiInfo = ((WifiManager)context.getSystemService(context.WIFI_SERVICE)).getConnectionInfo();
        int ip = wifiInfo.getIpAddress();
        DeviceInfo.IpAddress = String.format("%d.%d.%d.%d",
                (ip & 0xff),
                (ip >> 8 & 0xff),
                (ip >> 16 & 0xff),
                (ip >> 24 & 0xff));
        DeviceInfo.MacAddress = wifiInfo.getMacAddress();
    }

}
