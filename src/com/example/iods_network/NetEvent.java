package com.example.iods_network;

//��������״̬��ʶ�¼���
public class NetEvent {
	public boolean isConnect;//����״̬��ʶ
	
	/**
	 * ���캯�������������ʵ��ʱ����
	 * @param isConnect��boolean��true���������ӣ�false������������
	 */
	public NetEvent(boolean isConnect) {
		// TODO �Զ����ɵĹ��캯�����
		this.isConnect = isConnect;
	}
	
	/**
	 * ��ȡ����״̬
	 * @return boolean��true���������ӣ�false������������
	 */
	public boolean netCheck() {
		return isConnect;
	}

}
