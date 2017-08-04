package com.incarcloud.rooster.gather.remotecmd.device;

import java.util.Collection;

/**
 * 设备连接容器
 *
 * @author fanbeibei
 */
public interface DeviceConnectionContainer {
    /**
     * 添加设备连接
     *
     * @param conn
     * @return
     */
    public void addDeviceConnection(DeviceConnection conn);

    /**
     * 移除设备连接
     *
     * @param vin 车辆vin码
     * @return
     */
    public void removeDeviceConnection(String vin);

    /**
     * 获取设备连接
     *
     * @param vin 车辆vin码
     * @return
     * @throws Exception
     */
    public DeviceConnection getDeviceConnection(String vin);

    /**
     * 获取所有的设备连接
     *
     * @return
     */
    public Collection<DeviceConnection> getAllDeviceConnection();


    /**
     * 移除所有的设备连接
     */
    public void removeAllDeviceConnection();
}
