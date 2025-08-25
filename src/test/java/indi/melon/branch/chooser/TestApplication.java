package indi.melon.branch.chooser;

import indi.melon.branch.TestConfiguration;
import indi.melon.branch.test.A;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * @author vvnn1
 * @since 2024/7/20 10:18
 */
@SpringBootTest(classes = TestConfiguration.class)
@ExtendWith(SpringExtension.class)
public class TestApplication{
    @Autowired
    private A testA;
    @Test
    public void methodLogTest(){
        testA.sayHi("yunnan");
    }
}