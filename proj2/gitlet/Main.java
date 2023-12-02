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
                    System.out.println("Please enter a commit message.");
                    System.exit(0);
                }
                Repository.commit(args[1]);
                break;
            case "rm":
                checkArgsNum(args, 2);
                Repository.remove(args[1]);
                break;
            case "log":
                checkArgsNum(args, 1);
                Repository.log();
                break;
            case "global-log":
                checkArgsNum(args, 1);
                Repository.global_log();
                break;
            case "find":
                checkArgsNum(args, 2);
                Repository.find(args[1]);
                break;
            case "status":
                checkArgsNum(args, 1);
                Repository.status();
                break;
            case "checkout":
                if (args.length == 3) {
                    if (!args[1].equals("--")) {
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                    }
                    Repository.checkoutFile(args[2]);
                } else if (args.length == 4) {
                    if (!args[2].equals("--")) {
                        System.out.println("Incorrect operands.");
                        System.exit(0);
                    }
                    Repository.checkoutCommitFile(args[1], args[3]);
                } else if (args.length == 2) {
                    Repository.checkoutBranch(args[1]);
                } else {
                    throw error("Incorrect operands.", (Object []) args);
                }
                break;
            case "branch":
                checkArgsNum(args, 2);
                Repository.branch(args[1]);
                break;
            case "rm-branch":
                checkArgsNum(args, 2);
                Repository.rmBranch(args[1]);
                break;
            case "reset":
                checkArgsNum(args, 2);
                Repository.reset(args[1]);
                break;
            case "merge":
                checkArgsNum(args, 2);
                Repository.merge(args[1]);
                break;
        }
    }
    public static void checkArgsNum(String[] args, int n) {
        if (args.length != n) {
            throw error("Incorrect operands.", (Object [])args);
        }
    }
}
