package com.woodys.keyboard.hook;

import android.content.Context;
import android.os.IBinder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

import com.woodys.keyboard.compat.ServiceManagerCompat;
import com.woodys.keyboard.util.ReflectUtil;

/**
 * Created by woodys on 2018/3/7.
 */
public class ServiceManagerHook extends Hook implements InvocationHandler {

    private String mServiceName;
    private Object mProxyIInterface;

    public ServiceManagerHook(Context context, String serviceName) {
        super(context);
        mServiceName = serviceName;
    }

    public void setProxyIInterface(Object proxyIInterface) {
        this.mProxyIInterface = proxyIInterface;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals(ServiceManagerCompat.METHOD_QUERY_LOCAL_INTERFACE)) {
            if (mProxyIInterface != null) {
                return mProxyIInterface;
            }
        }
        return method.invoke(mOriginObj, args);
    }

    @Override
    public void onHook(ClassLoader classLoader) throws Throwable {
        Map sCache = ServiceManagerCompat.sCache();
        Object cachedObj = sCache.get(mServiceName);
        sCache.remove(mServiceName);
        mOriginObj = ServiceManagerCompat.getService(mServiceName);
        if (mOriginObj == null) {
            if (cachedObj != null && cachedObj instanceof IBinder && !Proxy.isProxyClass(cachedObj.getClass())) {
                mOriginObj = cachedObj;
            }
        }
        if (mOriginObj instanceof IBinder) {
            Object proxyBinder = ReflectUtil.makeProxy(classLoader, mOriginObj.getClass(), this);
            sCache.put(mServiceName, proxyBinder);
        }
    }
}
