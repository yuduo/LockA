package com.example.iods_network;

//网络连接状态标识事件类
public class NetEvent {
	public boolean isConnect;//连接状态标识
	
	/**
	 * 构造函数，创建类对象实例时调用
	 * @param isConnect：boolean，true：网络连接；false：无网络连接
	 */
	public NetEvent(boolean isConnect) {
		// TODO 自动生成的构造函数存根
		this.isConnect = isConnect;
	}
	
	/**
	 * 获取连接状态
	 * @return boolean，true：网络连接；false：无网络连接
	 */
	public boolean netCheck() {
		return isConnect;
	}

}
