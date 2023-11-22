package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.*;

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

    public static final File COMMITS_DIR = join(OBJECTS_DIR, "commits");
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
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
        GITLET_DIR.mkdir();
        OBJECTS_DIR.mkdir();
        COMMITS_DIR.mkdir();
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
        Stage s = new Stage();
        s.saveStage();
    }

    // the removed blob file has not been deleted !!!
    public static void add(String filename) {
        // calculate the blobId if has been staged area do nothing
        File f = join(CWD, filename);
        // if file does not exist
        if (!f.exists()) {
            System.out.println("File doesn't exist");
            System.exit(0);
        }
        byte[] contents = readContents(f);
        String id = sha1(filename, contents);
        Stage s = Stage.readStage();
        File headFile = join(Repository.COMMITS_DIR, readContentsAsString(Repository.HEAD_FILE));
        Commit head = readObject(headFile, Commit.class);
        Blob b = new Blob(filename, id, contents);
        //if the file in current commit and not changed(has same blobId), remove from the stage
        if (!head.sameBlob(filename, id) || s.getRemovalStage().contains(id)) {
            if (!s.hasExist(filename, id)) {
                if (!s.getRemovalStage().contains((id))) {
                    b.saveBlob();
                    if (s.hasFile(filename)) {
                        s.remove(filename);
                    }
                    s.put(filename, id);
                    s.saveStage();
                } else {
                    s.getRemovalStage().remove(id);
                    s.saveStage();
                }
            }
        }
        /*
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
        */
    }

    private static Commit headRead() {
        String headId = readContentsAsString(HEAD_FILE);
        File f = join(COMMITS_DIR, headId);
        return readObject(f, Commit.class);
    }

    private static void writeHead(String id) {
        writeContents(HEAD_FILE, id);
    }
    public static void commit(String message) {
        // copy head commit -- try to update new commit from addStage
        Commit currentCommit = headRead();
        HashMap<String, String> n2b = currentCommit.getNameToBlobID();
        Stage s = Stage.readStage();
        // try to update
        HashMap<String, String> addStage = s.getAddStage();
        HashSet<String> removalStage = s.getRemovalStage();
        if (addStage.isEmpty() && removalStage.isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        for (String fileName : removalStage) {
            n2b.remove(fileName);
        }
        for (Map.Entry<String, String> entry : addStage.entrySet()) {
            String fileName = entry.getKey();
            String id = entry.getValue();
            n2b.put(fileName, id);
        }
        // change commit
        currentCommit.updateMessage(message);
        currentCommit.updateTime(new Date());
        currentCommit.updateParent(readContentsAsString(HEAD_FILE));
        // save commit
        String commitId = currentCommit.generateID();
        currentCommit.saveId(commitId);
        currentCommit.saveCommit(commitId);
        writeHead(commitId);
        Stage emptyStage = new Stage();
        // clear stage
        emptyStage.saveStage();

        //change master
        File master = join(REFS_DIR, "master");
        writeContents(master, commitId);
    }

    // cannot change the commit
    public static void remove(String fileName) {
        Stage s = Stage.readStage();
        Commit head = headRead();
        HashMap<String, String> n2b = head.getNameToBlobID();
        if (!s.hasFile(fileName) && !n2b.containsKey(fileName)) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
        if (s.hasFile(fileName)) {
            s.remove(fileName);
        }
        if (n2b.containsKey(fileName)) {
            restrictedDelete(join(CWD, fileName));
            String id = n2b.get(fileName);
            s.getRemovalStage().add(id);
        }
        s.saveStage();
    }

    private static void printCommit(Commit c) {
        System.out.println("===");
        System.out.print("commit ");
        System.out.println(c.getId());
        System.out.println("Date: " + c.getDate());
        System.out.println(c.getMessage());
        System.out.println();
    }
    public static void log() {
        Commit c = headRead();
        while (true) {
            printCommit(c);
            if (c.getParent().length() == 0) {
                break;
            }
            c = readObject(join(COMMITS_DIR, c.getParent()), Commit.class);
        }
    }

    public static void global_log() {
        List<String> commits = plainFilenamesIn(COMMITS_DIR);
        for (String name : commits) {
            Commit c = readObject(join(COMMITS_DIR, name), Commit.class);
            printCommit(c);
        }
    }

    public static void find(String message) {
        List<String> commits = plainFilenamesIn(COMMITS_DIR);
        boolean befound = false;
        /**
        System.out.println("-------print all");
        for (String name : commits) {
            Commit c = readObject(join(COMMITS_DIR, name), Commit.class);
            System.out.println("******");
            System.out.println(c.toString());
            System.out.println("******");
        }
        System.out.println("-------");
        */
        for (String name : commits) {
            Commit c = readObject(join(COMMITS_DIR, name), Commit.class);
            if (c.getMessage().equals(message)) {
                befound = true;
                System.out.println(c.getId());
            }
        }
        if (!befound) {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
    }

    public static void status() {
        List<String> branches = plainFilenamesIn(REFS_DIR);
        String HEADid = readContentsAsString(HEAD_FILE);
        System.out.println("=== Branches ===");
        for (String b : branches) {
            String id = readContentsAsString(join(REFS_DIR, b));
            if (id.equals(HEADid)) {
                System.out.println("*" + b);
            }
            else {
                System.out.println(b);
            }
        }
        System.out.println();
        //System.out.println();
        System.out.println("=== Staged Files ===");
        Stage s = Stage.readStage();
        Set<String> addFiles = s.getAddStage().keySet();
        for (String f : addFiles) {
            System.out.println(f);
        }
        System.out.println();
        //System.out.println();
        System.out.println("=== Removed Files ===");
        Set<String> removeFiles = s.getRemovalStage();
        for (String f : removeFiles) {
            Blob b = readObject(join(OBJECTS_DIR, f), Blob.class);
            System.out.println(b.getFileName());
        }
        System.out.println();
        //System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();
        //System.out.println();
        System.out.println("=== Untracked Files ===");
        System.out.println();
        //System.out.println();
    }
    public static void checkoutFile(String name) {
        Commit h = headRead();
        HashMap<String, String> n2b = h.getNameToBlobID();
        if (!n2b.containsKey(name)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        writeContents(join(CWD, name), n2b.get(name));
    }

    public static void checkoutCommitFile(String commitId, String name) {
        List<String> commits =  plainFilenamesIn(COMMITS_DIR);
        if (!commits.contains(commitId)) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        Commit c = readObject(join(COMMITS_DIR, commitId), Commit.class);
        HashMap<String, String> n2b = c.getNameToBlobID();
        if (!n2b.containsKey(name)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        writeContents(join(CWD, name), n2b.get(name));
    }

    public static void checkoutBranch(String branch) {
        List<String> branches = plainFilenamesIn(REFS_DIR);
        if (!branches.contains(branch)) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        String branchId = readContentsAsString(join(REFS_DIR, branch));
        String headId = readContentsAsString(HEAD_FILE);
        if (headId.equals(branchId)) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        Commit newCommit = readObject(join(COMMITS_DIR, branchId), Commit.class);
        Commit curCommit = headRead();
        // file in work dir but not in the curCommit,
        List<String> fileOnlyInNewCommit = newCommit.getFilename();
        List<String> fileBothIn = new ArrayList<>();
        List<String> fileOnlyInCurCommit = curCommit.getFilename();

        for (String name : curCommit.getFilename()) {
            fileOnlyInNewCommit.remove(name);
        }
        for (String name : newCommit.getFilename()) {
            fileOnlyInCurCommit.remove(name);
        }
        for (String name : curCommit.getFilename()) {
            if (newCommit.getFilename().contains(name)) {
                fileBothIn.add(name);
            }
        }
        deleteFiles(fileOnlyInCurCommit);
        overWriteFiles(fileBothIn, newCommit);
        writeFiles(fileOnlyInNewCommit, newCommit);

        Stage s = new Stage();
        s.saveStage();

        writeContents(HEAD_FILE, branchId);
    }

    private static void deleteFiles(List<String> ls) {
        for (String name : ls) {
            restrictedDelete(join(CWD, name));
        }
    }
    private static void overWriteFiles(List<String> ls, Commit c) {
        for (String name : ls) {
            Blob b = readObject(join(OBJECTS_DIR, c.getNameToBlobID().get(name)), Blob.class);
            writeContents(join(CWD, name), b.getContents());
        }
    }
    private static void writeFiles(List<String> ls, Commit c) {
        for (String name : ls) {
            File f = join(CWD, name);
            if (f.exists()) {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                System.exit(0);
            }
        }
        overWriteFiles(ls, c);
    }
}
