package com.simatelex.admin.config.datasource;

import org.apache.commons.lang3.RandomUtils;

/**
 * @Author Nancy Zhao
 * @Date 2023/10/21 9:49
 * @Version 1.0
 **/

public class DynamicDataSourceContextHolder {
    private static final ThreadLocal<DataSourceKey> currentDateSource = new ThreadLocal<>();

    /**
     * 清除当前数据源
     */
    public static void clear() {
        currentDateSource.remove();
    }

    /**
     * 获取当前使用的数据源
     *
     * @return 当前使用数据源的ID
     */
    public static DataSourceKey get() {
        return currentDateSource.get();
    }

    /**
     * 设置当前使用的数据源
     *
     * @param value 需要设置的数据源ID
     */
    public static void set(DataSourceKey value) {
        currentDateSource.set(value);
    }

    /**
     * 设置从从库读取数据
     * 采用简单生成随机数的方式切换不同的从库
     */
    public static void setSlave() {
        if (RandomUtils.nextInt(0, 2) > 0) {
            DynamicDataSourceContextHolder.set(DataSourceKey.DB_XFBPS01);
        } else {
            DynamicDataSourceContextHolder.set(DataSourceKey.DB_XFSTD01);
        }
    }
}
