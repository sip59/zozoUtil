package com.zozo.util;

import android.test.AndroidTestCase;

/**
 * Created by Administrator on 2016/1/20.
 */
public class NetworkUtilTest extends AndroidTestCase {

    public void testIsNetworkAvailable() throws Exception {
        assertEquals(true, NetworkUtil.isNetworkAvailable(getContext()));
    }
}