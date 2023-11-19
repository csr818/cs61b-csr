package gitlet;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static gitlet.Utils.*;

public class Stage implements Serializable {
    // filename to blobId
    private HashMap<String, String> addStage = new HashMap<> ();

    // blobId
    private HashSet<String> removalStage = new HashSet<> ();

    public void saveStage() {
        writeObject(Repository.STAGE_FILE, this);
    }
    public static Stage readStage() {
        return readObject(Repository.STAGE_FILE, Stage.class);
    }

    // return whether addStage has the file
    public boolean hasFile(String name) {
        return addStage.containsKey(name);
    }

    // return whether pair<name, id> has existed in addStage
    public boolean hasExist(String name, String id) {
        if (hasFile(name)) {
            return addStage.get(name) == id;
        }
        return false;
    }

    public void put(String name, String id) { addStage.put(name, id); }

    public void remove(String name) {
        addStage.remove(name);
    }

    public HashMap<String, String> getAddStage() { return this.addStage; }

    public HashSet<String> getRemovalStage() { return this.removalStage; }
}
