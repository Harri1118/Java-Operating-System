public class VirtualFileMember {
    // id to be used for mapping purposes in VFS
    private int id;

    // Device to be stored as the device to be stored.
    private Device device;

    // Constructor initializes class.
    public VirtualFileMember(int id, Device device){
        this.id = id;
        this.device = device;
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }
}
