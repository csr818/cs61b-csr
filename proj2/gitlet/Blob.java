package gitlet;

import static gitlet.Utils.*;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;

/** to store the*/
public class Blob implements Serializable {
    private String filename;
    private byte[] contents;
    private String blobId;

    String getFileName() {
        return filename;
    }

    String getId() {
        return blobId;
    }
    public Blob(String name) {
        this.filename = name;
        File f = join(Repository.CWD, name);
        if (f.exists()) {
            this.contents = readContents(f);
            this.blobId = sha1(name, contents);
        } else {
            this.contents = null;
            this.blobId = sha1(name);
        }
    }

    public Blob(String name, String id, byte[] contents) {
        this.filename = name;
        this.blobId = id;
        this.contents = contents;
    }

    /** save blob to the file */
    public void saveBlob() {
        File f = join(Repository.OBJECTS_DIR, blobId);
        try {
            f.createNewFile();
        } catch (IOException e) {
            throw error(e.toString());
        }
        writeObject(f, this);
    }

    public byte[] getContents() {
        return this.contents;
    }
}
