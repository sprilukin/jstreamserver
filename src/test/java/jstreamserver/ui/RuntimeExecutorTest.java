package jstreamserver.ui;

import jstreamserver.utils.RuntimeExecutor;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Tests for {@link jstreamserver.utils.RuntimeExecutor}
 *
 * @author Sergey Prilukin
 */
public class RuntimeExecutorTest {

    @Test
    public void testExecution() throws Exception {
        RuntimeExecutor runtimeExecutor = new RuntimeExecutor();

        if (System.getenv("OS").contains("Windows")) {
            //Windows
            runtimeExecutor.execute("cmd", new String[] {"/c", "dir"});
        } else {
            //*nix
            runtimeExecutor.execute("ls", new String[] {"-lF"});
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(runtimeExecutor.getInputStream(), "ibm866"));

        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
    }
}
