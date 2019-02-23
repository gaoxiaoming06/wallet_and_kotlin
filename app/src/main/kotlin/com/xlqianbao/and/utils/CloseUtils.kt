package com.xlqianbao.and.utils

import java.io.Closeable
import java.io.IOException

/**
 * Created by Makise on 2017/4/17.
 */

object CloseUtils {
    /**
     * 关闭IO
     *
     * @param closeables closeables
     */
    fun closeIO(vararg closeables: Closeable) {
        if (closeables == null) return
        for (closeable in closeables) {
            if (closeable != null) {
                try {
                    closeable.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }
    }

    /**
     * 安静关闭IO
     *
     * @param closeables closeables
     */
    fun closeIOQuietly(vararg closeables: Closeable) {
        if (closeables == null) return
        for (closeable in closeables) {
            if (closeable != null) {
                try {
                    closeable.close()
                } catch (ignored: IOException) {
                }

            }
        }
    }
}
