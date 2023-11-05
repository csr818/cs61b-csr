package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import gitlet.Commit;
import gitlet.Blob;


import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    // store blob and commit objects
    public static final File OBJECTS_DIR = join(GITLET_DIR, "objects");

    // store the latest commit of the branch
    public static final File REFS_DIR = join(GITLET_DIR, "refs");
    // current work branch commit HEAD
    public static final File HEAD_FILE = join(GITLET_DIR, "HEAD");
    // store the stage area objects
    public static final File STAGE_FILE = join(GITLET_DIR, "stage");

    //create the folders we need, and create an init commit and write it into a file
    public static void init() {
        // create necessary file
        if (GITLET_DIR.exists() && GITLET_DIR.isDirectory()) {
            throw error("A Gitlet version-control system already exists in the current directory.");
        }
        GITLET_DIR.mkdir();
        OBJECTS_DIR.mkdir();
        REFS_DIR.mkdir();
        try {
            HEAD_FILE.createNewFile();
        } catch (IOException e) {
            throw error(e.toString());
        }
        try {
            STAGE_FILE.createNewFile();
        } catch (IOException e) {
            throw error(e.toString());
        }
        // create initial commit and save
        Commit initCommit = new Commit();
        String id = initCommit.generateID();
        initCommit.saveCommit(id);
        // create master branch and set the initial commit as its head
        File master = join(REFS_DIR, "master");
        try {
            master.createNewFile();
        } catch (IOException e) {
            throw error(e.toString());
        }
        writeContents(master, id);
        writeContents(HEAD_FILE, id);
    }

    // the removed blob file has not been deleted !!!
    public static void add(String filename) {
        // calculate the blobId if has been staged area do nothing
        File f = join(CWD, filename);
        // if file does not exist
        if (!f.exists()) {
            throw error("file doesn't exist");
        }
        byte[] contents = readContents(f);
        String id = sha1(filename, contents);
        Stage s = Stage.readStage();
        File headFile = join(Repository.OBJECTS_DIR, readContentsAsString(Repository.HEAD_FILE));
        Commit head = readObject(headFile, Commit.class);
        //if the file in current commit and not changed(has same blobId), remove from the stage
        if (head.sameBlob(filename, id)) {
            if (s.hasFile(filename)) {
                s.remove(filename);
            }
        } else {
            // if the file not exist or the content has changed
            if (!s.hasExist(filename, id)) {
                Blob b = new Blob(filename, id, contents);
                b.saveBlob();
                if (s.hasFile(filename)) {
                    s.remove(filename);
                }
                s.put(filename, id);
            }
        }
        s.saveStage();
    }

    /* TODO: fill in the rest of this class. */

    /** check whether the .gitlet has existed, or not create .gitlet ,gets a init commit and write in the file */

    //create a blob try to store in stage
    public static void storeToStage(String filename) {
        Blob b = new Blob(filename);
        b.saveBlob();
    }
}
