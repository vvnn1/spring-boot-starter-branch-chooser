package indi.melon.branch.chooser.main;

import indi.melon.branch.chooser.configuration.BranchChooserContext;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Proxy;
import java.util.List;

/**
 * @author wangmenglong
 * @since 2024/8/27 17:02
 */
public class MainFactoryBean extends AbstractFactoryBean<Object> {

    private final String beanName;
    private final List<Class<?>> interfaceList;
    private final List<Object> branchBeanList;

    private BranchChooserContext branchChooserContext;
    private Object mainBean;


    public MainFactoryBean(String beanName, List<Class<?>> interfaceList, List<Object> branchBeanList) {
        if (interfaceList == null || interfaceList.isEmpty()) {
            throw new RuntimeException("branch bean need to implement interface");
        }

        if (branchBeanList == null || branchBeanList.isEmpty()) {
            throw new RuntimeException("no match branch bean found.");
        }

        this.beanName = beanName;
        this.interfaceList = interfaceList;
        this.branchBeanList = branchBeanList;
    }

    @Override
    protected  Object createInstance() {
        if (mainBean == null){
            mainBean = Proxy.newProxyInstance(
                    ClassUtils.getDefaultClassLoader(),
                    ClassUtils.toClassArray(interfaceList),
                    new MainInvocationHandler<>(beanName, branchBeanList, branchChooserContext)
            );
        }

        return mainBean;
    }

    @Override
    public Class<?> getObjectType() {
        if (interfaceList == null || interfaceList.isEmpty()) {
            throw new RuntimeException("interfaceList is empty");
        }

        return Proxy.getProxyClass(ClassUtils.getDefaultClassLoader(), ClassUtils.toClassArray(interfaceList));
    }

    public void setBranchChooserContext(BranchChooserContext branchChooserContext) {
        this.branchChooserContext = branchChooserContext;
    }
}
