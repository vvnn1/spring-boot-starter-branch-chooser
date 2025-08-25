package indi.melon.branch.test;

import indi.melon.branch.chooser.annotation.Branch;
import indi.melon.branch.chooser.annotation.Guide;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author wangmenglong
 * @since 2024/8/28 15:46
 */
@Branch(mainName = "testA")
@Component
public class YunnanA implements A {
    private List<C> cList;
    @Override
    @Guide("#p0 == 'yunnan'")
    public void sayHi(String name) {
        System.out.println("yun nan aa");
    }

    @Autowired
    public void setcList(List<C> cList) {
        this.cList = cList;
    }
}
