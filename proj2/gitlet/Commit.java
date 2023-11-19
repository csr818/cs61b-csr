package gitlet;

// TODO: any imports you need here

import org.w3c.dom.UserDataHandler;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static gitlet.Utils.*;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;
    private String date;
    private String parent;
    private HashMap<String, String> nameToBlobID = new HashMap<>();


    /* TODO: fill in the rest of this class. */

    public static String dateToTimeStamp(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.US);
        return dateFormat.format(date);
    }
    public Commit(String message, Date date, String parent) {
        this.message = message;
        this.date = dateToTimeStamp(date);
        this.parent = parent;
    }

    // initialize the initial commit
    public Commit() {
        this.message = "initial commit";
        this.date = dateToTimeStamp(new Date(0));
        this.parent = null;
    }

    /** calculate ID and create a file named ID store*/
    public void saveCommit(String id){
        File f = join(Repository.OBJECTS_DIR, id);
        try {
            f.createNewFile();
        } catch (IOException e) {
            throw error(e.toString());
        }
        writeObject(f, this);
    }
    public String generateID() {
        return sha1(this.message, this.date, this.parent, this.nameToBlobID);
    }

    // whether commit points from name to the id, if just diff id --> contents changed
    public boolean sameBlob(String name, String id) {
        String storeId = nameToBlobID.getOrDefault(name, "");
        return storeId.equals(id);
    }

    public HashMap<String, String> getNameToBlobID() {
        return this.nameToBlobID;
    }

    public void put(String name, String id) { nameToBlobID.put(name, id); }

    public void remove(String name) { nameToBlobID.remove(name); }

    public void updateMessage(String message) { this.message = message; }

    public void updateTime(Date date) { this.date = dateToTimeStamp(date); }

    public void updateParent(String p) { this.parent = p; }
}
