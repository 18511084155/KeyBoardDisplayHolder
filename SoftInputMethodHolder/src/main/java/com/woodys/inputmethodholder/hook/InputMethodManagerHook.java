package com.woodys.inputmethodholder.hook;

import android.content.Context;
import android.os.IBinder;
import android.util.Log;
import android.util.Pair;
import android.view.inputmethod.InputMethodManager;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import com.woodys.inputmethodholder.OnInterceptMethodListener;
import com.woodys.inputmethodholder.compat.IInputMethodManagerCompat;
import com.woodys.inputmethodholder.compat.SystemServiceRegistryCompat;
import com.woodys.inputmethodholder.util.ReflectUtil;

/**
 * Created by woodys on 2018/3/7.
 */
public class InputMethodManagerHook extends Hook implements InvocationHandler {

    private static final String TAG = "InputMethodManagerHook";

    private MethodInvokeListener methodInvokeListener;
    private OnInterceptMethodListener onInterceptMethodListener;

    public InputMethodManagerHook(Context context) {
        super(context);
    }

    public void setMethodInvokeListener(MethodInvokeListener methodInvokeListener) {
        this.methodInvokeListener = methodInvokeListener;
    }

    public void setOnInterceptMethodListener(OnInterceptMethodListener onInterceptMethodListener) {
        this.onInterceptMethodListener = onInterceptMethodListener;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object invoke = null;
        try {
            Pair<Boolean, Object> objectPair = (null != onInterceptMethodListener) ? onInterceptMethodListener.onIntercept(proxy, method, args) : null;
            if (null != objectPair && objectPair.first) {
                Log.w(TAG, "method intercept success!  Method:" + method);
                invoke = objectPair.second;
            } else {
                invoke = method.invoke(mOriginObj, args);
            }

        } catch (Throwable e) {
            Log.w(TAG, "invoke failed!  " + Log.getStackTraceString(e));
        }

        if (methodInvokeListener != null) {
            methodInvokeListener.onMethod(mOriginObj, method, invoke);
        }
        return invoke;
    }

    @Override
    public void onHook(ClassLoader classLoader) throws Throwable {
        //其实有其他的hook点，比如InputMethodManager的sInstance，初始化的时候可以将代理的IInputMethodManager传进构造函数
        //现在的这种方式是从获取Binder代理对象的唯一入口ServiceManager开始hook，方便以后hook其他服务
        ServiceManagerHook serviceManagerHook = new ServiceManagerHook(mContext, Context.INPUT_METHOD_SERVICE);
        serviceManagerHook.onHook(classLoader);
        Object originBinder = serviceManagerHook.getOriginObj();
        if (originBinder instanceof IBinder) {
            mOriginObj = IInputMethodManagerCompat.asInterface((IBinder) originBinder);
            Object proxyInputMethodInterface = ReflectUtil.makeProxy(classLoader, mOriginObj.getClass(), this);
            serviceManagerHook.setProxyIInterface(proxyInputMethodInterface);
            //若hook之前调用过 mContext.getSystemService(Context.INPUT_METHOD_SERVICE)
            // 则在SystemServiceRegistry中会有缓存，清理缓存后重建才会拿到我们hook的代理
            clearCachedService();
            //rebuild cache
            mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        }
    }

    private void clearCachedService() throws Throwable {
        Object sInstance = ReflectUtil.getStaticFiled(InputMethodManager.class, "sInstance");
        if (sInstance != null) {
            ReflectUtil.setStaticFiled(InputMethodManager.class, "sInstance", null);
            Object systemFetcher = SystemServiceRegistryCompat.getSystemFetcher(Context.INPUT_METHOD_SERVICE);
            if (systemFetcher != null) {
                ReflectUtil.setFiled(systemFetcher.getClass().getSuperclass(), "mCachedInstance", systemFetcher, null);
            }
        }
    }

    public interface MethodInvokeListener {
        void onMethod(Object obj, Method method, Object result);
    }

}
