package indi.melon.branch.chooser.processor;

import indi.melon.branch.chooser.annotation.Branch;
import indi.melon.branch.chooser.configuration.BranchChooserContext;
import indi.melon.branch.chooser.main.MainFactoryBean;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.*;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author vvnn1
 * @since 2024/7/20 11:17
 */
@Component
public class MainBeanDefinitionRegistryProcessor implements BeanDefinitionRegistryPostProcessor, BeanFactoryAware {

    private BeanFactory beanFactory;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        Map<String, List<BeanDefinitionHolder>> mainName2BeanDefinitonHolderMap = new LinkedHashMap<>();

        for (String beanDefinitionName : registry.getBeanDefinitionNames()) {
            BeanDefinition beanDefinition = registry.getBeanDefinition(beanDefinitionName);
            Class<?> beanClass = getBeanClass(beanDefinitionName);

            Branch branch = AnnotationUtils.findAnnotation(beanClass, Branch.class);
            if (branch == null) {
                continue;
            }

            List<BeanDefinitionHolder> beanDefinitionList = mainName2BeanDefinitonHolderMap.computeIfAbsent(
                    branch.mainName(),
                    ignore -> new ArrayList<>()
            );
            beanDefinitionList.add(
                    new BeanDefinitionHolder(beanDefinition, beanDefinitionName)
            );
        }

        BeanDefinitionHolder contextDefinitionHolder = null;

        for (String beanDefinitionName : registry.getBeanDefinitionNames()) {
            BeanDefinition beanDefinition = registry.getBeanDefinition(beanDefinitionName);
            Class<?> beanClass = getBeanClass(beanDefinitionName);
            if (BranchChooserContext.class.isAssignableFrom(beanClass)) {
                if (contextDefinitionHolder != null){
                    throw new RuntimeException("duplicate branch chooser context");
                }
                contextDefinitionHolder = new BeanDefinitionHolder(beanDefinition, beanDefinitionName);
            }
        }

        for (Map.Entry<String, List<BeanDefinitionHolder>> entry : mainName2BeanDefinitonHolderMap.entrySet()) {
            String mainName = entry.getKey();
            List<BeanDefinitionHolder> branchDefinitionList = entry.getValue();
            BeanDefinition mainBeanDefinition = buildBeanDefinition(mainName, branchDefinitionList, contextDefinitionHolder);
            registry.registerBeanDefinition(mainName, mainBeanDefinition);
        }
    }

    private BeanDefinition buildBeanDefinition(String mainName, List<BeanDefinitionHolder> beanDefinitionHolderList, BeanDefinitionHolder contextDefinitionHolder) {
        List<Class<?>> interfaceList = beanDefinitionHolderList.stream()
                .map(this::convertClassName2Class)
                .map(ClassUtils::getAllInterfacesForClass)
                .flatMap(Arrays::stream)
                .distinct()
                .collect(Collectors.toList());


        RootBeanDefinition rootBeanDefinition = new RootBeanDefinition();
        rootBeanDefinition.setBeanClass(MainFactoryBean.class);
        rootBeanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
        rootBeanDefinition.setLazyInit(false);
        rootBeanDefinition.getPropertyValues()
                .add("branchChooserContext", contextDefinitionHolder);



        ConstructorArgumentValues constructorArgumentValues = new ConstructorArgumentValues();
        addConstructorArgumentValues(
                constructorArgumentValues,
                mainName,
                interfaceList,
                ManagedList.of(beanDefinitionHolderList.toArray())
        );


        rootBeanDefinition.setConstructorArgumentValues(
                constructorArgumentValues
        );
        rootBeanDefinition.setAutowireCandidate(true);
        rootBeanDefinition.setPrimary(true);

        return rootBeanDefinition;
    }

    private void addConstructorArgumentValues(ConstructorArgumentValues constructorArgumentValues, Object ... values){
        for (Object value : values) {
            constructorArgumentValues.addGenericArgumentValue(
                    new ConstructorArgumentValues.ValueHolder(value)
            );
        }
    }

    private Class<?> convertClassName2Class(BeanDefinitionHolder holder) {
        return getBeanClass(holder.getBeanName());
    }

    private Class<?> getBeanClass(String beanName){
        return beanFactory.getType(beanName);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
