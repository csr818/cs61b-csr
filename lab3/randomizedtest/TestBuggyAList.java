package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {
  // YOUR TESTS HERE
    @Test
   public void randomizedTest() {
       AListNoResizing<Integer> L = new AListNoResizing<>();
       BuggyAList<Integer> BL = new BuggyAList<>();
       int N = 5000;
       for (int i = 0; i < N; i += 1) {
           int operationNumber = StdRandom.uniform(0, 4);
           if (operationNumber == 0) {
               // addLast
               int randVal = StdRandom.uniform(0, 100);
               L.addLast(randVal);
               BL.addLast(randVal);
           } else if (operationNumber == 1) {
               // size
               int size = L.size();
           } else if (operationNumber == 2) {
                if (L.size() > 0) {
                   int res_noResize = L.getLast();
                   int res_bug = BL.getLast();
                    assertEquals(res_noResize, res_bug);
                }
           } else if (operationNumber == 3) {
                if (L.size() > 0) {
                    int res_noResize = L.removeLast();
                    int res_bug = BL.removeLast();
                    assertEquals(res_noResize, res_bug);
                }
           }
       }
   }
}
