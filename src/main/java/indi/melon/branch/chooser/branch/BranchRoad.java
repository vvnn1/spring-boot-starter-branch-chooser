package indi.melon.branch.chooser.branch;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author vvnn1
 * @since 2024/7/20 12:15
 */
public class BranchRoad<T> implements Comparable<BranchRoad<T>>{
    private final String guideBoard;
    private final Integer order;
    private final T branch;
    private final Method method;

    public BranchRoad(String guideBoard, Integer order, T branch, Method method) {
        this.guideBoard = guideBoard;
        this.order = order;
        this.branch = branch;
        this.method = method;
    }

    public static <T> BranchRoad<T> neverChoose(T branch, Method method){
        return new BranchRoad<>(
                "false",
                Integer.MIN_VALUE,
                branch,
                method
        );
    }

    @Override
    public int compareTo(BranchRoad<T> o) {
        int compareResult = order.compareTo(o.order);
        if (compareResult == 0){
            return Integer.compare(method.hashCode(), o.method.hashCode());
        }
        return compareResult;
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


    public Object invoke(Object ... args) throws InvocationTargetException, IllegalAccessException {
        return method.invoke(branch, args);
    }
}
