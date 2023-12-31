package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static gitlet.Utils.*;

/** Represents a gitlet commit object.
 *
 *  does at a high level.
 *
 *  @author TODO
 */
public class Commit implements Serializable {
    /**
     *
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;
    private String date;
    private List<String> parent = new ArrayList<>();
    private HashMap<String, String> nameToBlobID = new HashMap<>();
    private String commitId;



    public static String dateToTimeStamp(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.US);
        return dateFormat.format(date);
    }
    public Commit(String message, Date date, String parent) {
        this.message = message;
        this.date = dateToTimeStamp(date);
        this.parent.add(parent);
        this.commitId = generateID();
    }

    // initialize the initial commit
    public Commit() {
        this.message = "initial commit";
        this.date = dateToTimeStamp(new Date(0));
        this.commitId = generateID();
    }

    /** calculate ID and create a file named ID store*/
    public void saveCommit(String id) {
        File f = join(Repository.COMMITS_DIR, id);
        try {
            f.createNewFile();
        } catch (IOException e) {
            throw error(e.toString());
        }
        writeObject(f, this);
    }
    public String generateID() {
        return sha1(this.message, this.date,
                serialize((Serializable) this.parent),
                serialize(this.nameToBlobID));
    }

    // whether commit points from name to the id, if just diff id --> contents changed
    public boolean sameBlob(String name, String id) {
        String storeId = nameToBlobID.getOrDefault(name, "");
        return storeId.equals(id);
    }

    public HashMap<String, String> getNameToBlobID() {
        return this.nameToBlobID;
    }

    public void put(String name, String id) {
        nameToBlobID.put(name, id);
    }

    public void remove(String name) {
        nameToBlobID.remove(name);
    }

    public void updateMessage(String message) {
        this.message = message;
    }

    public void updateTime(Date date) {
        this.date = dateToTimeStamp(date);
    }

    public void updateParent(String p) {
        ArrayList<String> newParent = new ArrayList<>();
        newParent.add(p);
        this.parent = newParent;
    }

    public List<String> getParent() {
        return this.parent;
    }

    public String getId() {
        return this.commitId;
    }

    public String getDate() {
        return this.date;
    }

    public String getMessage() {
        return this.message;
    }

    public void saveId(String id) {
        this.commitId = id;
    }

    public String toString() {
        return this.message + " " + this.date + " " + this.commitId;
    }

    public List<String> getFilename() {
        List<String> names = new ArrayList<>();
        for (String name : nameToBlobID.keySet()) {
            names.add(name);
        }
        return names;
    }
    public void updateId() {
        this.commitId = generateID();
    }
}
