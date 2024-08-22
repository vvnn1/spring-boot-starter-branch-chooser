package indi.melon.branch.chooser.main;

import indi.melon.branch.chooser.annotation.Branch;
import indi.melon.branch.chooser.annotation.Guide;
import indi.melon.branch.chooser.branch.BranchDefinition;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author vvnn1
 * @since 2024/7/19 21:18
 */
public class MainInvocationHandler<T> implements InvocationHandler {
    private final Map<Method, Collection<BranchDefinition<T>>> branchMap = new HashMap<>();
    private final SpelExpressionParser spelExpressionParser = new SpelExpressionParser();
    private final Map<String, Object> contextVariable = Collections.emptyMap();
    private final List<T> branchBeanList;

    public MainInvocationHandler(List<T> branchBeanList) {
        this.branchBeanList = branchBeanList;
    }

    private boolean check(T branchBean) {
        Class<?> aClass = branchBean.getClass();
        return AnnotationUtils.isAnnotationDeclaredLocally(Branch.class, aClass);
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Collection<BranchDefinition<T>> definitionCollection = branchMap.computeIfAbsent(method, ignore -> new TreeSet<>());
        if (definitionCollection.isEmpty()){
            for (T branchBean : branchBeanList) {
                if (check(branchBean)){
                    BranchDefinition<T> definition = analyzeBranch(method, branchBean);
                    definitionCollection.add(definition);
                }
            }
        }

        for (BranchDefinition<T> definition : definitionCollection) {
            if (isRightBranch(definition, method, args)){
                return method.invoke(definition.getBranch(), args);
            }
        }

        return null;
    }


    private boolean isRightBranch(BranchDefinition<T> definition, Method method, Object[] args){
        String guideBoard = definition.getGuideBoard();

        if (guideBoard == null || guideBoard.isEmpty()) {
            return false;
        }
        if ("true".equalsIgnoreCase(guideBoard) || "false".equalsIgnoreCase(guideBoard)){
            return Boolean.parseBoolean(guideBoard);
        }

        MethodBasedEvaluationContext context = createMethodBasedEvaluationContext(
                definition.getBranch(),
                method,
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
        evaluationContext.setVariables(contextVariable);
        return evaluationContext;
    }

    private BranchDefinition<T> analyzeBranch(Method method, T branchBean){
        Guide guide = AnnotationUtils.findAnnotation(method, Guide.class);
        if (guide == null){
            Class<?> beanClass = branchBean.getClass();
            guide = AnnotationUtils.findAnnotation(beanClass, Guide.class);
        }

        if (guide == null){
            return BranchDefinition.neverChoose(branchBean, method);
        }

        String guideBoard = guide.value();
        int order = guide.order();

        return new BranchDefinition<>(
                guideBoard,
                order,
                branchBean,
                method
        );
    }
}
