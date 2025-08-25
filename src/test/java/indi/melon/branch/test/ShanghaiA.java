package indi.melon.branch.test;

import indi.melon.branch.chooser.annotation.Branch;
import indi.melon.branch.chooser.annotation.Guide;
import org.springframework.stereotype.Component;

/**
 * @author wangmenglong
 * @since 2024/8/28 15:46
 */
@Branch(mainName = "testA")
@Component
public class ShanghaiA implements A {
    @Override
    @Guide("#ctx.cloud.region == 'shanghai'")
    public void sayHi(String name) {
        System.out.println("shang hai aaa");
    }
}
