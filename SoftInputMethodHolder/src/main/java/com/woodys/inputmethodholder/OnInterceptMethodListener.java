package com.woodys.inputmethodholder;

import android.util.Pair;

import java.lang.reflect.Method;

/**
 * Created by woodys on 2018/5/3.
 */
public interface OnInterceptMethodListener {

    /**
     * 全局控制键盘的方法，制定是否需要拦截键盘相关事件，如：弹出键盘，隐藏键盘
     * 因为键盘在一个独立的进程中，我们hook的只是本地进程的一个binder代理
     * @param obj
     * @param method
     * @param result
     * @return
     */
    Pair<Boolean, Object> onIntercept(Object obj, Method method, Object result);
}
