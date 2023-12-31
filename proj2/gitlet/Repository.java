package gitlet;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;





import static gitlet.Utils.*;
import static gitlet.Utils.restrictedDelete;

//

/** Represents a gitlet repository.
 *
 *  does at a high level.
 *
 *  @author TODO
 */
public class Repository {
    /**
     *
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

    // current work branch  HEAD
    public static final File HEAD_FILE = join(GITLET_DIR, "HEAD");
    // store the stage area objects
    public static final File STAGE_FILE = join(GITLET_DIR, "stage");

    //create the folders we need, and create an init commit and write it into a file
    public static void init() {
        // create necessary file
        if (GITLET_DIR.exists() && GITLET_DIR.isDirectory()) {
            System.out.println("A Gitlet version-control system " +
                    "already exists in the current directory.");
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
        writeContents(HEAD_FILE, "master");
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
        File workBranch = join(REFS_DIR, readContentsAsString(Repository.HEAD_FILE));
        String workCommit = readContentsAsString(workBranch);
        File headFile = join(Repository.COMMITS_DIR, workCommit);
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
        File workBranch = join(REFS_DIR, readContentsAsString(Repository.HEAD_FILE));
        String workCommit = readContentsAsString(workBranch);
        File headFile = join(Repository.COMMITS_DIR, workCommit);
        return readObject(headFile, Commit.class);
    }

    private static void writeHead(String branchName) {
        writeContents(HEAD_FILE, branchName);
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
        for (String fileId : removalStage) { // this is a bug, removalStage stores the blobId
            Iterator<Map.Entry<String, String>> iterator = n2b.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                if (entry.getValue().equals(fileId)) {
                    iterator.remove();
                    break;
                }
            }
            //System.out.println("the remove file is " + fileId);
        }
        for (Map.Entry<String, String> entry : addStage.entrySet()) {
            String fileName = entry.getKey();
            String id = entry.getValue();
            n2b.put(fileName, id);
        }
        // change commit
        currentCommit.updateMessage(message);
        currentCommit.updateTime(new Date());
        Commit headCommit = headRead();
        currentCommit.updateParent(headCommit.getId());
        // save commit
        String commitId = currentCommit.generateID();
        currentCommit.saveId(commitId);
        currentCommit.saveCommit(commitId);
        Stage emptyStage = new Stage();
        // clear stage
        emptyStage.saveStage();

        //change current branch
        String branchName = readContentsAsString(HEAD_FILE);
        File branch = join(REFS_DIR, branchName);
        writeContents(branch, commitId);
        //printCommitFiles(currentCommit, currentCommit.getMessage());
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
            if (c.getParent().size() == 0) {
                break;
            }
            // deliberately put the parent to display at the first index
            c = readObject(join(COMMITS_DIR, c.getParent().get(0)), Commit.class);
        }
    }

    public static void globalLog() {
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

    private static String getHeadId() {
        return readContentsAsString(join(REFS_DIR, readContentsAsString(HEAD_FILE)));
    }
    public static void status() {
        List<String> branches = plainFilenamesIn(REFS_DIR);
        // write as a function
        // String HEADId = getHeadId();
        System.out.println("=== Branches ===");
        for (String b : branches) {
            String workBranch = readContentsAsString(HEAD_FILE);
            if (b.equals(workBranch)) {
                System.out.println("*" + b);
            } else {
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
        Blob b = readObject(join(OBJECTS_DIR, n2b.get(name)), Blob.class);
        writeContents(join(CWD, name), new String(b.getContents(), StandardCharsets.UTF_8));
    }

    public static void checkoutCommitFile(String commitId, String name) {
        List<String> commits =  plainFilenamesIn(COMMITS_DIR);
        if (commitId.length() <= 8) {
            commitId = checkShortId(commits, commitId);
        }
        if (commitId.equals("") || !commits.contains(commitId)) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        Commit c = readObject(join(COMMITS_DIR, commitId), Commit.class);
        HashMap<String, String> n2b = c.getNameToBlobID();
        if (!n2b.containsKey(name)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        Blob b = readObject(join(OBJECTS_DIR, n2b.get(name)), Blob.class);
        writeContents(join(CWD, name), new String(b.getContents(), StandardCharsets.UTF_8));
    }

    private static String checkShortId(List<String> commits, String id) {
        for (String c : commits) {
            if (c.substring(0, id.length()).equals(id)) {
                return c;
            }
        }
        return "";
    }
    public static void checkoutBranch(String branch) {
        List<String> branches = plainFilenamesIn(REFS_DIR);
        if (!branches.contains(branch)) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        String workBranch = readContentsAsString(HEAD_FILE);
        if (workBranch.equals(branch)) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        String branchId = readContentsAsString(join(REFS_DIR, branch));
        Commit newCommit = readObject(join(COMMITS_DIR, branchId), Commit.class);
        Commit curCommit = headRead();
        // find the only files and common files
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

        writeContents(HEAD_FILE, branch);
    }

    private static void deleteFiles(List<String> ls) {
        for (String name : ls) {
            restrictedDelete(join(CWD, name));
        }
    }
    private static void overWriteFiles(List<String> ls, Commit c) {
        for (String name : ls) {
            Blob b = readObject(join(OBJECTS_DIR, c.getNameToBlobID().get(name)), Blob.class);
            writeContents(join(CWD, name), new String(b.getContents(), StandardCharsets.UTF_8));
        }
    }
    private static void writeFiles(List<String> ls, Commit c) {
        for (String name : ls) {
            File f = join(CWD, name);
            if (f.exists()) {
                System.out.println("There is an untracked file in the way;" +
                        " delete it, or add and commit it first.");
                System.exit(0);
            }
        }
        overWriteFiles(ls, c);
    }

    public static void branch(String branch) {
        List<String> branches = plainFilenamesIn(REFS_DIR);
        if (branches.contains(branch)) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        File f = join(REFS_DIR, branch);
        try {
            f.createNewFile();
        } catch (Exception e) {
            throw error(e.toString());
        }
        String id = getHeadId();
        writeContents(f, id);
    }

    public static void rmBranch(String branch) {
        List<String> branches = plainFilenamesIn(REFS_DIR);
        if (!branches.contains(branch)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        String workBranch = readContentsAsString(HEAD_FILE);
        if (workBranch.equals(branch)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        File b = join(REFS_DIR, branch);
        b.delete();
    }

    public static void reset(String id) {
        List<String> lsCommit = plainFilenamesIn(COMMITS_DIR);
        if (!lsCommit.contains(id)) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        String branch = readContentsAsString(HEAD_FILE);
        File f = join(REFS_DIR, ".temp");
        try {
            f.createNewFile();
        } catch (Exception e) {
            throw error(e.toString());
        }
        writeContents(f, id);
        checkoutBranch(".temp");
        writeContents(join(REFS_DIR, branch), id);
        writeContents(HEAD_FILE, branch);
        f.delete();
    }

    private static Commit findCommonParent(Commit c1, Commit c2) {
        HashMap<String, Integer> commit1ToDepth = new HashMap<> ();
        Deque<Commit> deque = new LinkedList<>();
        // store all commits from c1
        deque.addLast(c1);
        int depth = 0;
        while (deque.size() > 0) {
            int dequeSize = deque.size();
            for (int i = 0; i < dequeSize; ++i) {
                Commit curCommit = deque.removeFirst();
                List<String> parents = curCommit.getParent();
                for (String p : parents) {
                    deque.addLast(readObject(join(COMMITS_DIR, p), Commit.class));
                }
                commit1ToDepth.put(curCommit.getId(), depth);
            }
            depth += 1;
        }

        deque.addLast(c2);
        while (deque.size() > 0) {
            Commit curCommit = deque.removeFirst();
            if (commit1ToDepth.containsKey(curCommit.getId())) {
                return curCommit;
            }
            List<String> parents = curCommit.getParent();
            for (String p : parents) {
                deque.addLast(readObject(join(COMMITS_DIR, p), Commit.class));
            }
        }
        return null;
    }

    private static String dealConflict(Blob branchConflictBlob, Blob currentConflictBlob) {
        File conflictFile = join(CWD, currentConflictBlob.getFileName());
        /**System.out.println("------------" + conflictFile.getPath());
        // System.out.println(currentConflictBlob.getFileName()
         + " " + branchConflictBlob.getFileName());
        // System.out.println(currentConflictBlob.getFileName().
         equals(branchConflictBlob.getFileName()));
        */
        try {
            conflictFile.createNewFile();
        } catch (Exception e) {
            throw error(e.toString());
        }
        if (!conflictFile.exists()) {
            System.out.println("not exists");
        }
        String conflictContent = "<<<<<<< HEAD\n" +  new String(currentConflictBlob.getContents(), StandardCharsets.UTF_8) + "=======\n" + new String(branchConflictBlob.getContents(), StandardCharsets.UTF_8) + ">>>>>>>\n";
        writeContents(conflictFile, conflictContent);
        /**System.out.println("------------" + conflictFile.getPath());
        // System.out.print(readContentsAsString(conflictFile));
        //File FileOut = new File("D:\\courses\\cs61b\\
         skeleton-sp21\\proj2\\testing\\src\\out.txt");
        //writeContents(FileOut, conflictContent);
         */
        return currentConflictBlob.getFileName();
    }

    private static String dealConflict(Blob b) {
        File conflictFile = join(CWD, b.getFileName());
        try {
            conflictFile.createNewFile();
        } catch (Exception e) {
            throw error(e.toString());
        }
        if (!conflictFile.exists()) {
            System.out.println("not exists");
        }
        String conflictContent = "<<<<<<< HEAD\n" +  new String(b.getContents(), StandardCharsets.UTF_8) + "=======\n"  + ">>>>>>>\n";
        writeContents(conflictFile, conflictContent);
        return b.getFileName();
    }

    public static void merge(String branch) {
        List<String> allBranches = plainFilenamesIn(REFS_DIR);
        if (!allBranches.contains(branch)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        Stage s = Stage.readStage();
        if (!s.getAddStage().isEmpty() || !s.getRemovalStage().isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        Commit branchCommit = readObject(join(COMMITS_DIR, readContentsAsString(join(REFS_DIR, branch))), Commit.class);
        Commit currentCommit = headRead();
        Commit splitCommit = findCommonParent(branchCommit, currentCommit);
        String conflictFile = null;
        // printCommitFiles(splitCommit, "splitCommit");
        // printCommitFiles(currentCommit, "currentCommit");
        // printCommitFiles(branchCommit, "givenCommit");
        if (branchCommit.getId().equals(currentCommit.getId())) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
        if (splitCommit.getId().equals(branchCommit.getId())) {
            System.out.println("Given branch is an ancestor of the current branch.");
            System.exit(0);
        }
        if (splitCommit.getId().equals(currentCommit.getId())) {
            checkoutBranch(branch);
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }
        HashMap<String, String> splitCommitN2b = splitCommit.getNameToBlobID();
        HashMap<String, String> branchCommitN2b = branchCommit.getNameToBlobID();
        HashMap<String, String> currentCommitN2b = currentCommit.getNameToBlobID();
        Commit mergeCommit = new Commit();
        List<String> conflictFiles = new ArrayList<>();
        for (Map.Entry<String, String> entry : splitCommitN2b.entrySet()) {
            String fileName = entry.getKey();
            String blobId = entry.getValue();
            if (branchCommitN2b.containsKey(fileName) && currentCommitN2b.containsKey(fileName)) {
                String branchBlobId = branchCommitN2b.get(fileName);
                String currentBlobId = currentCommitN2b.get(fileName);
                if (blobId.equals(branchBlobId) && blobId.equals(currentBlobId)) {
                    mergeCommit.getNameToBlobID().put(fileName, blobId);
                } else if (blobId.equals(branchBlobId)) {
                    mergeCommit.getNameToBlobID().put(fileName, currentBlobId);
                } else if (blobId.equals(currentBlobId)) {
                    mergeCommit.getNameToBlobID().put(fileName, branchBlobId);
                } else {
                    System.out.println("Encountered a merge conflict.");
                    // conflict
                    Blob branchBlob = readObject(join(OBJECTS_DIR, branchBlobId), Blob.class);
                    Blob currentBlob = readObject(join(OBJECTS_DIR, currentBlobId), Blob.class);
                    conflictFile = dealConflict(branchBlob, currentBlob);
                    conflictFiles.add(conflictFile);
                }
            } else if (branchCommitN2b.containsKey(fileName)
                    && !currentCommitN2b.containsKey(fileName)
                    && !branchCommitN2b.get(fileName).equals(splitCommitN2b.get(fileName))) {
                System.out.println("Encountered a merge conflict.");
                String branchBlobId = branchCommitN2b.get(fileName);
                Blob branchBlob = readObject(join(OBJECTS_DIR, branchBlobId), Blob.class);
                conflictFile = dealConflict(branchBlob);
                conflictFiles.add(conflictFile);
            } else if (currentCommitN2b.containsKey(fileName) &&
                    !branchCommitN2b.containsKey(fileName) &&
                    !currentCommitN2b.get(fileName).equals(splitCommitN2b.get(fileName))) {
                System.out.println("Encountered a merge conflict.");
                String currentBlobId = currentCommitN2b.get(fileName);
                Blob currentBlob = readObject(join(OBJECTS_DIR, currentBlobId), Blob.class);
                conflictFile = dealConflict(currentBlob);
                conflictFiles.add(conflictFile);
            }
        }
        // find the files only exist in the current branch or given branch
        Set<String> filesInSplitCommit = splitCommitN2b.keySet();
        Set<String> filesInBranchCommit = branchCommitN2b.keySet();
        Set<String> filesInCurrentCommit = currentCommitN2b.keySet();
        Set<String> filesNotInSplit = new HashSet<> (filesInCurrentCommit);
        filesNotInSplit.addAll(filesInBranchCommit);
        filesNotInSplit.removeAll(filesInSplitCommit);
        for (String file : filesNotInSplit) {
            if (branchCommitN2b.containsKey(file) && currentCommitN2b.containsKey(file)) {
                // if the branchCommit and currentCommit have the same content don't happen conflict
                if (branchCommitN2b.get(file).equals(currentCommitN2b.get(file))) {
                    mergeCommit.getNameToBlobID().put(file, branchCommitN2b.get(file));
                } else {
                    System.out.println("Encountered a merge conflict.");
                    String branchConflictId = branchCommitN2b.get(file);
                    String currentConflictId = currentCommitN2b.get(file);
                    Blob branchConflictBlob = readObject(join(OBJECTS_DIR, branchConflictId), Blob.class);
                    Blob currentConflictBlob = readObject(join(OBJECTS_DIR, currentConflictId), Blob.class);
                    conflictFile =  dealConflict(branchConflictBlob, currentConflictBlob);
                    conflictFiles.add(conflictFile);
                }
                // conflict
            } else if (branchCommitN2b.containsKey(file)) {
                mergeCommit.getNameToBlobID().put(file, branchCommitN2b.get(file));
            } else {
                mergeCommit.getNameToBlobID().put(file, currentCommitN2b.get(file));
            }
        }
//        if (conflictFile != null) {
//
//        }
        // update the part of the commit
        mergeCommit.getParent().add(currentCommit.getId());
        mergeCommit.getParent().add(branchCommit.getId());
        mergeCommit.updateMessage("Merged " + branch + " into " + readContentsAsString(HEAD_FILE) + ".");
        mergeCommit.updateId();
        mergeCommit.saveCommit(mergeCommit.getId());
        // work directory files change

        List<String> currentFiles = plainFilenamesIn(CWD);
        // change the file with different contents and add the new files
        for (Map.Entry<String, String> entry : mergeCommit.getNameToBlobID().entrySet()) {
            String fileName = entry.getKey();
            String blobId = entry.getValue();
            if (!currentCommitN2b.containsKey(fileName)) {
                File f = join(CWD, fileName);
                try {
                    f.createNewFile();
                } catch (Exception e) {
                    throw error(e.toString());
                }
            }
            if (!blobId.equals(currentCommitN2b.getOrDefault(fileName, ""))) {
                File f = join(CWD, fileName);
                // overwrite------
                if (currentFiles.contains(fileName) && !currentCommitN2b.containsKey(fileName)) {
                    System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                    System.exit(0);
                }
                writeContents(f, new String(readObject(join(OBJECTS_DIR, blobId), Blob.class).getContents(), StandardCharsets.UTF_8));
            }
        }
        // delete the file which is not appeared in mergeCommit
        for (Map.Entry<String, String> entry : currentCommitN2b.entrySet()) {
            String fileName = entry.getKey();
            String blobId = entry.getValue();
            if (!mergeCommit.getNameToBlobID().containsKey(fileName) && !conflictFiles.contains(fileName)) {
                // System.out.println("---------- in the work directory but not in mergeCommit " + fileName);
                restrictedDelete(join(CWD, fileName));
            }
        }
        // printCommitFiles(mergeCommit, "merged");

        for (String fileName : currentFiles) {
            // System.out.println("---------- in the work directory files " + fileName);
            if (!mergeCommit.getNameToBlobID().containsKey(fileName) && !conflictFiles.contains(fileName)) {
               // System.out.println("---------- in the work directory but not in mergeCommit " + fileName);
                // files don't exist in the currentCommit but in the work directory and is not the conflictFile should trigger an exit
                if (!currentCommitN2b.containsKey(fileName)) {
                    System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                    System.exit(0);
                }
                File f = join(CWD, fileName);
                restrictedDelete(f);
            }
        }
        // change HEAD branch points to the merged commit
        writeContents(join(REFS_DIR, readContentsAsString(HEAD_FILE)), mergeCommit.getId());
        // initial stage should be empty
        // Stage s = new Stage();
        // s.saveStage();
    }

    private static  void printCommitFiles(Commit c, String name) {
        System.out.println("--------------the files contained are as below : " + name);
        for (String file : c.getNameToBlobID().keySet()) {
            System.out.println(file);
        }
        System.out.println("--------------");
    }

    public static void checkIfInitialize() {
        if (!GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }
}
