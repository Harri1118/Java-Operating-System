import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class FakeFileSystem implements Device{
    private RandomAccessFile[] files = new RandomAccessFile[10];
    // Open goes through files until the next null is found, initializes it with string s and "rw", and then returns the
    // index of which it belongs to.
    @Override
    public int Open(String s){
        try{
        for(int i = 0; i < files.length; i++){
            if(files[i] == null)
                files[i] = new RandomAccessFile(s,"rw");
                return i;
        }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public void Close(int id) {
        try {
            if(files[id] != null){
                files[id].close();
                files[id] = null;
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public byte[] Read(int id, int size) {
        try {
            byte[] finList = new byte[size];
            files[id].read(finList,0,size);
            return finList;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return new byte[0];
    }

    @Override
    public void Seek(int id, int to) {
        try{
        files[id].seek(to);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public int Write(int id, byte[] data) {
        try{
        files[id].write(data);
        return data.length;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return 0;
    }

    public void clear() {
        for(int i = 0; i < files.length; i++){
            if(files[i] != null){
                try{
                files[i].close();
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
}
