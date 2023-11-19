package gitlet;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
import static gitlet.Utils.*;
import static gitlet.Repository.*;
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            throw error("Incorrect operands.");
        }
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                checkArgsNum(args, 1);
                Repository.init();
                break;
            case "add":
                checkArgsNum(args, 2);
                Repository.add(args[1]);
                break;
            case "commit":
                checkArgsNum(args, 2);
                if (args[1].length() == 0) {
                    throw error("Please enter a commit message.");
                }
                Repository.commit(args[1]);
                break;
        }
    }
    public static void checkArgsNum(String[] args, int n) {
        if (args.length != n) {
            throw error("Incorrect operands.", (Object [])args);
        }
    }
}
