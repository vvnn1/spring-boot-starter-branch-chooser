package indi.melon.branch.chooser.branch;

import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.lang.reflect.Method;

/**
 * @author vvnn1
 * @since 2024/7/20 12:15
 */
public class BranchDefinition<T> implements Comparable<BranchDefinition<T>>{
    private final String guideBoard;
    private final Integer order;
    private final T branch;
    private final Method method;

    public BranchDefinition(String guideBoard, Integer order, T branch, Method method) {
        this.guideBoard = guideBoard;
        this.order = order;
        this.branch = branch;
        this.method = method;
    }

    public static <T> BranchDefinition<T> neverChoose(T branch, Method method){
        return new BranchDefinition<>(
                "false",
                Integer.MIN_VALUE,
                branch,
                method
        );
    }

    @Override
    public int compareTo(BranchDefinition<T> o) {
        return order.compareTo(o.order);
    }


    public String getGuideBoard() {
        return guideBoard;
    }

    public Integer getOrder() {
        return order;
    }

    public T getBranch() {
        return branch;
    }

    public Method getMethod() {
        return method;
    }
}
