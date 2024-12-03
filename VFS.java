public class VFS implements Device{
    // VFSList stores all the coordinates of VirtualFileMember.
    private VirtualFileMember[] VFSList = new VirtualFileMember[10];
    // fakeFileSystem initialized as a device.
    private FakeFileSystem fakeFileSystem = new FakeFileSystem();
    // RandomDevice initialized as the second device.
    private RandomDevice randomDevice = new RandomDevice();
    // Open checks to see if s is null, if so then split into an array and throw an exception if not in the correct format.
    // Find if the string depends on the fakeFileSystem or the randomDevice, take the second member of the string list and pass it on
    // to the selected device, and create a new member of VFSList with device stored as well as the return value of open within that device.
    @Override
    public int Open(String s) {
        if(s == null || s.isEmpty())
            return -1;
        try {
            String[] strs = s.split(" ");
            if(strs.length != 2)
                throw new Exception("VFS Error. VFS.OPEN requires a two worded string input. EXAMPLE: \"random 100\"");
            s = strs[1];
            int vfsId = -1;
            Device device = null;
            if (strs[0].equals("random")) {
                vfsId = randomDevice.Open(strs[1]);
                device = randomDevice;
            } else if (strs[0].equals("file")) {
                vfsId = fakeFileSystem.Open(s);
                device = fakeFileSystem;
            }
            int i = 0;
            while (i < VFSList.length) {
                if (VFSList[i] == null) {
                    VirtualFileMember vfs = new VirtualFileMember(vfsId, device);
                    VFSList[i] = vfs;
                    return i;
                }
                i++;
            }

        } catch(Exception e){
            e.printStackTrace();
        }
        return -1;
    }

    /*
    * Close gets the id of the VFSList and closes the selected device within the VirtualFileMember at index VirtualFileMember.id
    * */
    @Override
    public void Close(int id) {
       try{
           if(VFSList[id] != null){
               VFSList[id].getDevice().Close(VFSList[id].getId());
               VFSList[id] = null;
           }
       }
       catch(Exception e){
           e.printStackTrace();
       }
    }

    // Read returns VFSList[id].read of a certain size.
    @Override
    public byte[] Read(int id, int size) {
        if(VFSList[id] != null){
            return VFSList[id].getDevice().Read(VFSList[id].getId(), size);
        }
        return new byte[0];
    }

    // Seek routes the coordinate and calls VirtualFileMember.device to seek of VirtualFileMember.id
    @Override
    public void Seek(int id, int to) {
        if(VFSList[id] != null){
            VFSList[id].getDevice().Seek(VFSList[id].getId(), to);
        }
    }

    // Write routes the coordinate and calls VirtualFileMember.device to write in VirtualFileMember.id. It then returns the size of the content written.
    @Override
    public int Write(int id, byte[] data) {
        if(VFSList[id] != null){
            return VFSList[id].getDevice().Write(VFSList[id].getId(), data);
        }
        return -1;
    }

    // Clear closes all of the device members and nullifies the members as well.
    public void clear() {
        fakeFileSystem.clear();
        for(int i = 0; i < VFSList.length; i++){
            VFSList[i] = null;
        }
    }

    // Used for debugging.
    public VirtualFileMember[] getMembers(){return VFSList;}
}
