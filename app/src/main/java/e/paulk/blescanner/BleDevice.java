package e.paulk.blescanner;

public class BleDevice {

    private String device_name;

    private String device_UUID;

    private int device_RSSI;

    private int device_TXpower;

    private int device_adID;

    private byte[] device_rawData;

    private String device_identifier;

    private String lastUpdateTime;

    private int estimated_distance;

    public BleDevice() {
    }

    public BleDevice(byte[] rawData) {
        this.device_rawData = rawData;
    }

    public BleDevice(byte[] rawData, String name, int rssi) {
        this.device_rawData = rawData;
        this.device_name = name;
        this.device_RSSI = rssi;
    }

    public BleDevice(byte[] rawData, String name, int rssi, String identifier) {
        this.device_rawData = rawData;
        this.device_name = name;
        this.device_RSSI = rssi;
        this.device_identifier = identifier;
    }

    public void setDevice_name(String device_name) {
        this.device_name = device_name;
    }

    public void setDevice_UUID(String device_UUID) {
        this.device_UUID = formatStringToUUID(device_UUID);
    }

    public void setDevice_RSSI(int device_RSSI) {
        this.device_RSSI = device_RSSI;
    }

    public void setDevice_TXpower() {
        String data = getDevice_convertedRawData();
        String tx_byte = data.substring(58, 60);
        this.device_TXpower = Integer.valueOf(tx_byte, 16).byteValue();
    }

    public void setDevice_adID(int device_adID) {
        this.device_adID = device_adID;
    }

    public void setDevice_rawData(byte[] device_rawData) {
        this.device_rawData = device_rawData;
    }

    public void setDevice_identifier(String device_identifier) {
        this.device_identifier = device_identifier;
    }

    public void setLastUpdateTime(String lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public void setEstimated_distance(int estimated_distance) {
        this.estimated_distance = estimated_distance;
    }

    public String getLastUpdateTime() {
        return lastUpdateTime;
    }

    public String getDevice_name() {
        return device_name;
    }

    public String getDevice_UUID() {
        return device_UUID;
    }

    public int getDevice_TXpower() {
        return device_TXpower;
    }

    public int getDevice_RSSI() {
        return device_RSSI;
    }

    public int getDevice_adID() {
        return device_adID;
    }

    public String getDevice_convertedRawData() {
        return getDataFromByteArray(this.device_rawData);
    }

    public String getDevice_identifier() {
        return device_identifier;
    }

    public int getEstimated_distance() {
        //calculate the distance by RSSI
        //80/90/100 for 1/2/3 meters
        int rssi = getDevice_RSSI() * -1;

        if (rssi > 40 && rssi < 65) {
            //1 meter
            estimated_distance = 1;
        } else if (rssi > 65 && rssi < 76) {
            //2 meter
            estimated_distance = 2;
        } else if (rssi > 76 && rssi < 80){
            //3 meter
            estimated_distance = 3;
        } else if (rssi > 80 && rssi < 90) {
            //4 meter
            estimated_distance = 4;
        } else {
            estimated_distance = 0;
        }
        return estimated_distance;
    }

    public static String getDataFromByteArray(byte[] byteArray) {
        if (byteArray == null) {
            return null;
        }
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[byteArray.length * 2];
        for (int j = 0; j < byteArray.length; j++) {
            int v = byteArray[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        String str = new String(hexChars);
        return str;
    }

    public static String formatStringToUUID(String str) {
        String temp = str.substring(0, 8);
        temp = temp.concat("-");
        temp = temp.concat(str.substring(8, 12));
        temp = temp.concat("-");
        temp = temp.concat(str.substring(12, 16));
        temp = temp.concat("-");
        temp = temp.concat(str.substring(16, 20));
        temp = temp.concat("-");
        temp = temp.concat(str.substring(20));
        return temp;
    }
}
