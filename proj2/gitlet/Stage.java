package gitlet;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import static gitlet.Utils.*;

public class Stage implements Serializable {
    // filename to blobId
    private Map<String, String> addStage = new HashMap<> ();

    public void saveStage() {
        writeObject(Repository.STAGE_FILE, this);
    }
    public static Stage readStage() {
        return readObject(Repository.STAGE_FILE, Stage.class);
    }
    public boolean hasFile(String name) {
        return addStage.containsKey(name);
    }
    public boolean hasExist(String name, String id) {
        if (hasFile(name)) {
            return addStage.get(name) == id;
        }
        return false;
    }

    public void put(String name, String id) {
        addStage.put(name, id);
    }

    public void remove(String name) {
        addStage.remove(name);
    }
}
