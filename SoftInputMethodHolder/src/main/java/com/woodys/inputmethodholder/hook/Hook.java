package com.woodys.inputmethodholder.hook;

import android.content.Context;

/**
 * Created by woodys on 2018/3/7.
 */
abstract class Hook {

    Context mContext;
    Object mOriginObj;

    Hook(Context context) {
        mContext = context;
    }

    Object getOriginObj() {
        return mOriginObj;
    }

    public abstract void onHook(ClassLoader classLoader) throws Throwable;

}
