package indi.melon.branch.chooser.main;

import indi.melon.branch.chooser.annotation.Branch;
import indi.melon.branch.chooser.annotation.Guide;
import indi.melon.branch.chooser.branch.BranchRoad;
import indi.melon.branch.chooser.configuration.BranchChooserContext;
import org.springframework.context.expression.MapAccessor;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.ClassUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author vvnn1
 * @since 2024/7/19 21:18
 */
public class MainInvocationHandler<T> implements InvocationHandler {
    private final Map<Method, Collection<BranchRoad<T>>> branchRoadMap = new HashMap<>();
    private static final SpelExpressionParser spelExpressionParser = new SpelExpressionParser();
    private final List<T> branchBeanList;
    private final String beanName;
    private final BranchChooserContext branchChooserContext;

    public MainInvocationHandler(String beanName, List<T> branchBeanList, BranchChooserContext branchChooserContext) {
        this.branchBeanList = branchBeanList;
        this.beanName = beanName;
        this.branchChooserContext = branchChooserContext;
    }

    private boolean check(T branchBean) {
        Class<?> aClass = branchBean.getClass();
        return AnnotationUtils.isAnnotationDeclaredLocally(Branch.class, aClass);
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        for (Method objMethod : Object.class.getMethods()) {
            if (method.equals(objMethod)){
                return method.invoke(this, args);
            }
        }


        Collection<BranchRoad<T>> roadCollection = branchRoadMap.computeIfAbsent(method, ignore -> new TreeSet<>());
        if (roadCollection.isEmpty()){
            for (T branchBean : branchBeanList) {
                if (check(branchBean)){
                    BranchRoad<T> road = analyzeBranch(method, branchBean);
                    roadCollection.add(road);
                }
            }
        }

        for (BranchRoad<T> road : roadCollection) {
            if (isRightRoad(road, args)){
                return road.invoke(args);
            }
        }

        return null;
    }


    private boolean isRightRoad(BranchRoad<T> definition, Object[] args){
        String guideBoard = definition.getGuideBoard();

        if (guideBoard == null || guideBoard.isEmpty()) {
            return false;
        }
        if ("true".equalsIgnoreCase(guideBoard) || "false".equalsIgnoreCase(guideBoard)){
            return Boolean.parseBoolean(guideBoard);
        }

        MethodBasedEvaluationContext context = createMethodBasedEvaluationContext(
                definition.getBranch(),
                definition.getMethod(),
                args
        );

        Boolean right = spelExpressionParser.parseExpression(guideBoard)
                .getValue(context, Boolean.class);
        return right != null && right;
    }

    private MethodBasedEvaluationContext createMethodBasedEvaluationContext(Object rootObject, Method method, Object[] arguments){
        MethodBasedEvaluationContext evaluationContext = new MethodBasedEvaluationContext(
                rootObject,
                method,
                arguments,
                new DefaultParameterNameDiscoverer()
        );

        evaluationContext.addPropertyAccessor(new MapAccessor());

        if (branchChooserContext != null){
            evaluationContext.setVariables(Collections.singletonMap("ctx", branchChooserContext.getContext()));
        }

        return evaluationContext;
    }

    private BranchRoad<T> analyzeBranch(Method method, T branchBean){
        Class<?> beanClass = branchBean.getClass();
        method = ClassUtils.getMostSpecificMethod(method, beanClass);

        Guide guide = AnnotationUtils.findAnnotation(method, Guide.class);
        if (guide == null){
            guide = AnnotationUtils.findAnnotation(beanClass, Guide.class);
        }

        if (guide == null){
            return BranchRoad.neverChoose(branchBean, method);
        }

        String guideBoard = guide.value();
        int order = guide.order();

        return new BranchRoad<>(
                guideBoard,
                order,
                branchBean,
                method
        );
    }

    @Override
    public String toString() {
        return beanName;
    }
}
