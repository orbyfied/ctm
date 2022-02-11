package com.github.orbyfied.argument;

import com.github.orbyfied.util.ErrorUtil;

import java.lang.reflect.Method;
import java.util.*;

public class ArgFunction {

    private final Object obj;
    private final Method methodToCall;

    public ArgFunction(Object function) {
        Method methodToCall1 = null;
        Objects.requireNonNull(function, "invalid function: null");
        this.obj = function;

        try {
            for (Method method : obj.getClass().getDeclaredMethods()) {
                if (method.getName().equals("invoke")) {
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    if (parameterTypes.length == 0) continue;
                    Class<?> lastParam = parameterTypes[parameterTypes.length - 1];
                    if (!lastParam.isArray() || lastParam.getComponentType() != Object.class) continue;
                    method.setAccessible(true);
                    methodToCall1 = method;
                    break;
                }
            }

        } catch (Exception e) {
            throw new IllegalStateException("invalid function " + obj, e);
        }

        methodToCall = methodToCall1;
    }

    public Object getObject() {
        return obj;
    }

    public Method getMethodl() {
        return methodToCall;
    }

    public Object invoke(ArgContext ctx, Object... args) {
        try {
            Class<?>[] params = methodToCall.getParameterTypes();
            int pc = methodToCall.getParameterCount() - 1;
            if (args.length < pc - 1)
                return new Err("not enough arguments; expected " + pc + ", got " + args.length);
            List<Object> newArgs = new ArrayList<>(pc + 2);
            newArgs.add(ctx);
            int i;
            for (i = 0; i < pc - 1; i++)
                newArgs.add(cast(args[i], params[i + 1]));
            Object[] other = new Object[args.length - i];
            for (int j = 0; i < args.length; i++, j++)
                other[j] = args[i];
            newArgs.add(other);

            Object o = methodToCall.invoke(obj, newArgs.toArray());
            if (methodToCall.getReturnType() == Void.TYPE)
                return VOID;
            return o;
        } catch (Exception e) {
            return new Err("invocation exception: " + e, e);
        }
    }

    private static Object cast(Object o, Class<?> to) {
        if (o == null) return null;
        if (to == Integer.TYPE) return ((Number)o).intValue();
        else if (to == Long.TYPE) return ((Number)o).longValue();
        else if (to == Double.TYPE) return ((Number)o).doubleValue();
        else if (to == Float.TYPE) return ((Number)o).floatValue();
        if (o.getClass() == Raw.class) return ((Raw)o).getString();
        return to.cast(o);
    }

    public static final Object VOID = new Object();

    public static class Err {
        private String str;
        private Throwable t;

        public Err(String str) {
            this.str = str;
        }

        public Err(String str, Throwable t) {
            this.str = str;
            this.t = t;
        }

        public String getStr() {
            return str;
        }

        public Throwable getT() {
            return t;
        }

        @Override
        public String toString() {
            return new StringJoiner(", ", Err.class.getSimpleName() + "[", "]")
                    .add("str='" + str + "'")
                    .add("t=" + ErrorUtil.getStackTrace(t))
                    .toString();
        }

    }

}
