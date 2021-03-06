package com.incarcloud.rooster.gather;

import com.incarcloud.rooster.cache.ICacheManager;
import com.incarcloud.rooster.mq.IBigMQ;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * 采集槽所在主机
 *
 * @author 熊广化
 */
public class GatherHost {

    /**
     * Logger
     */
    private static Logger s_logger = LoggerFactory.getLogger(GatherHost.class);

    /**
     * 主机名
     */
    private String name;

    private EventLoopGroup _bossGroup;
    private EventLoopGroup _workerGroup;

    /**
     * 采集槽列表
     */
    private ArrayList<GatherSlot> _slots = new ArrayList<>();
    /**
     * 数据包发送管理器
     */
    private DataPackPostManager dataPackPostManager;

    /**
     * 操作消息队列接口
     */
    private IBigMQ bigMQ;

    /**
     * 缓存管理器接口
     */
    private ICacheManager cacheManager;

    /**
     * 是否已启动
     */
    private Boolean _bRunning = false;

    /**
     * bigMQ topic
     * 上行-- DataPack TBOX消息
     */
    private String dataPackTopic ;

    /**
     * bigMQ topic
     * 下行-- remoteTopic 下发TBOX
     */
    private String remoteTopic ;

    /**
     * bigMQ topic
     * 上行-- feedBackTopic 回馈
     */
    private String feedBackTopic ;

    public GatherHost() {
        this("host" + Calendar.getInstance().getTimeInMillis());
    }

    /**
     * @param name 主机名
     */
    public GatherHost(String name) {
        this.name = name;
        _bossGroup = new NioEventLoopGroup();
        _workerGroup = new NioEventLoopGroup();

        this.dataPackPostManager = new DataPackPostManager(this);
    }

    /**
     * 启动
     *
     * @throws Exception
     */
    public synchronized void start() throws Exception {
        if (_bRunning) {
            return;
        }

        if(null != dataPackPostManager) {
            dataPackPostManager.start();
        }

        if (null == _slots || 0 == _slots.size()) {
            throw new RuntimeException("No slot!!!");
        }

        //启动所有采集槽
        for (GatherSlot slot : _slots) {
            slot.start();
        }

        _bRunning = true;

        s_logger.info(name + " start success!!");
    }

    /**
     * 停止
     *
     * @throws Exception
     */
    public synchronized void stop() throws Exception {
        _bossGroup.shutdownGracefully();
        _workerGroup.shutdownGracefully();

        for (GatherSlot slot : _slots) {
            slot.stop();
        }

        if(null != dataPackPostManager) {
            dataPackPostManager.stop();
        }

        if (null != bigMQ) {
            bigMQ.close();
        }
        _bRunning = false;
    }

    /**
     * 增加输入端口
     *
     * @param portType 端口类型
     * @param portArgs 端口参数
     * @return
     */
    @Deprecated
    public GatherSlot addSlot(GatherPortType portType, String portArgs) {
        GatherSlot slot = null;

        switch (portType) {
            case TCP:
                slot = new GatherSlot4TCP(Integer.parseInt(portArgs), this);
                break;
            case UDP:
                slot = new GatherSlot4UDP(Integer.parseInt(portArgs), this);
                break;
            case MQTT:
            default:
                throw new UnsupportedOperationException(portType.name());
        }

        _slots.add(slot);
        return slot;
    }


    /**
     * 添加采集槽
     * @param slotsConf 采集槽配置,格式:   解析器名:通讯协议:监听端口,解析器名:通讯协议:监听端口,......,解析器名:通讯协议:监听端口
     */
    public void addSlot(String slotsConf) throws Exception{
        if(StringUtils.isBlank(slotsConf)){
            throw new IllegalArgumentException();
        }

        String[] cfgs = slotsConf.split(",");

        for (String s : cfgs) {
            String parse = s.split(":")[0].trim();
            String protocol = s.split(":")[1].trim();
            String port = s.split(":")[2].trim();

            if("tcp".equals(protocol)){
                GatherSlot slot = new GatherSlot4TCP(Integer.parseInt(port),this);
                slot.setDataParser(parse);
                slot.setCacheManager(this.getCacheManager());
                _slots.add(slot);
            }

            if("udp".equals(protocol)){
                GatherSlot slot = new GatherSlot4UDP(Integer.parseInt(port),this);
                slot.setDataParser(parse);
                slot.setCacheManager(this.getCacheManager());
                _slots.add(slot);
            }

            if("mqtt".equals(protocol)){
                // TODO
            }
        }
    }

    EventLoopGroup getBossGroup() {
        return _bossGroup;
    }

    EventLoopGroup getWorkerGroup() {
        return _workerGroup;
    }

    /**
     * 获取 主机名
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    public IBigMQ getBigMQ() {
        return bigMQ;
    }

    public void setBigMQ(IBigMQ bigMQ) {
        this.bigMQ = bigMQ;
    }

    public ICacheManager getCacheManager() {
        return cacheManager;
    }

    public void setCacheManager(ICacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /**
     * 将数据包处理任务扔到队列中
     *
     * @param packWrap
     */
    public void putToCacheQueue(DataPackWrap packWrap) {
        if (null == packWrap) {
            return;
        }

        s_logger.debug("PutToCacheQueue: {}", packWrap);

        dataPackPostManager.add(packWrap);
    }

    public String getDataPackTopic() {
        return dataPackTopic;
    }

    public void setDataPackTopic(String dataPackTopic) {
        this.dataPackTopic = dataPackTopic;
    }

    public String getRemoteTopic() {
        return remoteTopic;
    }

    public void setRemoteTopic(String remoteTopic) {
        this.remoteTopic = remoteTopic;
    }

    public String getFeedBackTopic() {
        return feedBackTopic;
    }

    public void setFeedBackTopic(String feedBackTopic) {
        this.feedBackTopic = feedBackTopic;
    }
}
