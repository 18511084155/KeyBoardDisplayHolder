package com.woodys.inputmethodholder;

import android.content.Context;
import android.util.Log;

import java.lang.reflect.Method;

import com.woodys.inputmethodholder.hook.InputMethodManagerHook;

/**
 * Created by woodys on 2018/3/6.
 */
public class InputMethodHolder {

    private static final String TAG = "InputMethodHolder";

    private static InputMethodManagerHook inputMethodManagerHook;

    private static InputMethodListener mInputMethodListener;

    public static void registerListener(OnInputMethodListener listener) {
        if (mInputMethodListener != null) {
            mInputMethodListener.registerListener(listener);
        }
    }

    public static void unregisterListener(OnInputMethodListener listener) {
        if (mInputMethodListener != null) {
            mInputMethodListener.unregisterListener(listener);
        }
    }

    public static void init(final Context context) {
        if (inputMethodManagerHook != null) {
            return;
        }
        try {
            mInputMethodListener = new InputMethodListener();
            inputMethodManagerHook = new InputMethodManagerHook(context);
            inputMethodManagerHook.onHook(context.getClassLoader());
            inputMethodManagerHook.setMethodInvokeListener(new InputMethodManagerHook.MethodInvokeListener() {
                @Override
                public void onMethod(Object obj, Method method, Object result) {
                    if (mInputMethodListener != null) {
                        mInputMethodListener.onMethod(obj, method, result);
                    }
                }
            });
        } catch (Throwable throwable) {
            Log.w(TAG, "hook failed! detail:" + Log.getStackTraceString(throwable));
        }
    }


    /**
     * 拦截键盘事件
     * 注意：设置完成拦截器以后，假如不在使用请调用clearOnInterceptMethodListener进行清空拦截监听
     * @param onInterceptMethodListener
     */
    public static void setOnInterceptMethodListener(final OnInterceptMethodListener onInterceptMethodListener) {
        if (null != inputMethodManagerHook) {
            inputMethodManagerHook.setOnInterceptMethodListener(onInterceptMethodListener);
        }
    }

    /**
     * 清空拦截监听
     */
    public static void clearOnInterceptMethodListener() {
        if (null != inputMethodManagerHook) {
            inputMethodManagerHook.setOnInterceptMethodListener(null);
        }
    }


    public static void release() {
        if (null != mInputMethodListener) mInputMethodListener.clear();
        if (null != inputMethodManagerHook) {
            inputMethodManagerHook.setMethodInvokeListener(null);
            inputMethodManagerHook.setOnInterceptMethodListener(null);
        }
        inputMethodManagerHook = null;
        mInputMethodListener = null;
    }

}
