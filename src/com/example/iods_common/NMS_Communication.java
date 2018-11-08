package com.example.iods_common;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;

public class NMS_Communication {

	private String Source_Inform;	//源消息
	private Handler handler;		//当前handler
	
	public static String NMS_Address;		//网管TCP服务地址
	public static int NMS_Port;				//网管TCP服务端口号
	public static int index_backgroup;		//背景图号
	
	public static Socket TCP_Socket ;
	public static DataInputStream TCP_In ;	 
	public static DataOutputStream TCP_Out ;
	
	static byte[] Tx_byteData=new byte[1200];
	
	int Frame_Len;
	static byte[] re_in_buff = new byte[1000000];			//至少可以一次性接收近千个 1024bit 长的 标签信息，或 近4000个 256bit 长的标签数据
	byte[] Recive_Frame = new byte[1000000];
	int time_Start, Frame_Numb, Order_Tpye = 0;
	String Str_Show_Result;
	
	int temp_Tag_1, temp_Tag_2;			//状态标记
	int Numb_Data = 0;					//信令中的数据数 N 值
	
	public NMS_Communication(Handler handler, String Source){
		//构造函数
		this.Source_Inform=Source;
		this.handler=handler;
	}
	
	private class Connect_Thread extends Thread {
		
		public void run() {
			//建立连接
			String Device_IP;
			int Device_Port;

			Device_IP = NMS_Communication.NMS_Address;
			
			Device_Port = NMS_Communication.NMS_Port;
			
			InetAddress addr = null;
			try {
				addr = InetAddress.getByName(Device_IP);
			} catch (UnknownHostException e1) {

				e1.printStackTrace();
			}
			
			
			try {
				
				TCP_Socket = new Socket(addr, Device_Port);
				TCP_Socket.setSoLinger(true, 1);									//设定关闭Socket后，延时1秒关闭底层连接
				
				TCP_Out = new DataOutputStream(TCP_Socket.getOutputStream());
				TCP_In = new  DataInputStream (TCP_Socket.getInputStream());
				
				handler.obtainMessage(2, Source_Inform).sendToTarget();		//传递完成TCP连接消息，并返回源信息
				
			} catch (IOException e) {
				//置子网以太网连接失败

				handler.obtainMessage(2, "联网失败").sendToTarget();		//传递完成TCP连接消息，并返回源信息
				e.printStackTrace();
				System.out.println("Socket 连接失败");
				
			}
		}
	}
	
	public void Make_Socket_Connect() {
		Connect_Thread Make_NMS_Connect = new Connect_Thread();
		Make_NMS_Connect.start();
		
	}
	
	public void Close_Socket_Connect() {
		//关闭连接
		try {
			
			if (TCP_Socket != null) {
				TCP_Socket.close();
			}
			
			if (TCP_Out != null) {
				TCP_Out.close();
			}
			
			if (TCP_In != null) {
				TCP_In.close();
			}

		} catch (IOException e) {

			e.printStackTrace();
		}
		
	}
	
	public void Wait_Recive_TCP_Reply() {
    	//启动监听TCP通信应答
        AcceptThread TCP_acceptThread = new AcceptThread();
        TCP_acceptThread.start(); 	

    }
	
	private class AcceptThread extends Thread {
		
		@Override
		public void run() {
			int i, j = 0, k, m, bye_Len, readDataLen;
			
			String Send_Frame_Str;
			byte[] Finishin_Frame = new byte[2000];
			
			// 接收设备应答的TCP协议信令
			//time_Start = Error_Beep_Thead.Counter_Time;			
			//记录起始时间，若当前进程运行时间超过2个告警周期计数（时长在1秒到2秒之间），则关闭当前连接，并结束此进程
			
	    	try {
	    		
	    		for (i = 0; i< 5300; i++) {
	    			Recive_Frame[i] = 0;
	    		}
	    		j = 0;

				k = 0;
				while (k == 0) {
					//将输入读入到 in_buff ，数据长度为 bye_Len
					bye_Len = TCP_In.read(re_in_buff);
						
					if (bye_Len < 1) {
						continue;
					}
					
					if (j > 0) {
						//有以前保存的接收帧前段数据，则将新接收到的数据拼接在以前接收到的数据后
						if (j + bye_Len < 1000000) {
							for (i = 0; i<= bye_Len; i++) {
								Recive_Frame[i + j] = re_in_buff[i];
							}
							j = j + bye_Len;
						}
						else {
							//数据太多无法保证接收
							k = 1;
							for (i = 0; i<= bye_Len; i++) {
								if (i + j >= 1000000) {
									break;
								}
								else {
									Recive_Frame[i + j] = re_in_buff[i];
								}
							}
							j = 1000000;
						}
					}
					else {
						//没有以前的累积数据，则直接将接收数据进行转移
						for (i = 0; i<= bye_Len; i++ ) {
							Recive_Frame[i] = re_in_buff[i];
						}
						j = bye_Len;
					}
					
					if (j >= 3) {
						m = 0;
						if (! (Recive_Frame[0] == 0x7E && Recive_Frame[1] == 0 && Recive_Frame[2] == 0x10)) {
							//起始数据不是帧头，则滚动丢弃直到找到帧头，或到数据串末尾	
							i = 0;
							while (m == 0) {
								i ++;
								if (Recive_Frame[i] == 0x7E) {
									//找到帧定界符
									if (j - i >= 3) {
										if (Recive_Frame[i + 1] == 0 && Recive_Frame[i + 2] == 0x10) {
											//找到帧头，则将帧头前的数据丢弃，重新整理数据串
											for (m = i; m <= j; m++) {
												Recive_Frame[m - i] = Recive_Frame[m];
											}
											j = j - i;
											//结束滚动查找
											m = 1;
										}
									}
									else {
										//帧定界符后的数据太短，则不做任何处理，退出滚动查找，以便继续接收数据
										m = 1;
									}
								}
								else {
									if (i == j) {
										//滚动到数据串末尾，仍未找到帧定界符，则不做任何处理，退出滚动查找，以便继续接收数据
										m = 1;
									}
								}
							}
						}
						
						while (m == 0 && j >= 20) {
							//接收到的数据起始点为帧头
							//查找帧尾
							for (i = 1; i <= j; i++) {
								if (Recive_Frame[i] == 0x7E) {
									//找到帧尾
									m = i;
									break;
								}
							}
							
							if (m > 0) {
								//找到帧尾
																
								for (i = 0; i<=m; i++) {
									Finishin_Frame[i] = Recive_Frame[i];
								}
								
								for (i = 0; i < j; i ++) {
									if (i < j - m - 1) {
										Recive_Frame[i] = Recive_Frame[i + m + 1];
									}
									else {
										Recive_Frame[i] = 0;
									}
								}
								j = j - m - 1;
								

								
								//先进行 7D 5E 向 7E的转译
								m = Trans_7D5E_To_7E(Finishin_Frame, m);		//返回值为转译后的数据长度
								   
								//再进行CRC校验分析
								i = NMS_Communication.Cala_CRC(Finishin_Frame, m - 3);
																					  
								//对单个指令的回复进行显示
								Str_Show_Result = "指令执行情况： ";

								if (Finishin_Frame[m - 2] == (byte)(i & 0xFF) && Finishin_Frame[m - 1] == (byte)(i >> 8)  && Finishin_Frame[m] == 0x7E) {
									//CRC正确，且收到帧结束符
									i = 0;
								}
								else {
									i = 1;		//CRC校验错误，或帧结束符错误

									Str_Show_Result = Str_Show_Result + "\n \n     信号 CRC 校验错误。";
								}
								
								Recive_Data_Analizy(Finishin_Frame, m, i);		//接收数据分析
								
								if (Recive_Frame[0] == 0x7E && Recive_Frame[1] == 0 && Recive_Frame[2] == 0x10) {
									m = 0;
								}
								
								if ((Finishin_Frame[3] == Finishin_Frame[5] && Finishin_Frame[4] == Finishin_Frame[6]) || (Finishin_Frame[5] == 1 && Finishin_Frame[6] == 0)) {
									//是单帧，或是最后一帧，则结束接收
									k = 1;
									//仅6202、6217和6209的收信应答不用立即关闭连接
									if (Finishin_Frame[18] == 0x62 && (Finishin_Frame[17] == 0x02 || Finishin_Frame[17] == 0x17 || Finishin_Frame[17] == 0x09)) {
										if (m == 23 && Finishin_Frame[20] == 0x01) {
											k = 0;		//这是网管的直接应答信号，暂不关闭连接
										}
									}
									
									if (k == 1) {
										Close_Socket_Connect();
									}
																		
									m = 0;
								}													
							}
						}
					}
				}
				//数据接收完成
	    	} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private int Trans_7D5E_To_7E(byte[] Data_in_buff, int bye_Len) {
		// 将接收数据序列中的 7D 5E 转译为 7E，将7D 5D转译成7D
		
		int i, j;
		i = 1;
		
		while (i < bye_Len) {
			if (Data_in_buff[i] == 0x7D && Data_in_buff[i+1] == 0x5E) {
				//找到 7D5E 则转译为 7E
				for (j = i; j < bye_Len; j++) {
					Data_in_buff[j] = Data_in_buff[j + 1];
				}
				Data_in_buff[i] = 0x7E;
				bye_Len --;
			}
			i++;
		}
		
		i = 1;
		while (i < bye_Len) {
			if (Data_in_buff[i] == 0x7D && Data_in_buff[i+1] == 0x5D) {
				//找到 7D5D 则转译为 7D
				for (j = i; j < bye_Len; j++) {
					Data_in_buff[j] = Data_in_buff[j + 1];
				}
				Data_in_buff[i] = 0x7D;
				bye_Len --;
			}
			i++;
		}
		
		return bye_Len;
	}
	

	private void Recive_Data_Analizy(byte[] finishin_frame, int Len_frame, int CRC_Tag) {
		//分析接收数据，CRC_Tag为1时表示CRC计算出错，仅只解析最重要数据；RC_Tag为1时表示CRC计算正确
		String Command_Code;
		String Str_Date, Str_Date_0;		//日期字符串
		int Numb_Frame, i, j, k;

		byte[] Byte_Lnglat = new byte[10];
		byte[] Byte_Name = new byte[50];
		byte[] Byte_Str = new byte[17];
		byte[] Byte_Date = new byte[1100];
		
		String Str_Lng, Str_Lat, Str_Name, Str_ID;
		
		int Posit_Frame_Byte;	//帧数据解析到的位置
					
		if (finishin_frame[3] < 0) {
			i = 256 + finishin_frame[3];
		}
		else {
			i = finishin_frame[3];
		}
		
		Numb_Frame = i;
		
		if (finishin_frame[4] < 0) {
			i = 256 + finishin_frame[4];
		}
		else {
			i = finishin_frame[4];
		}
		
		Numb_Frame = 256 * i + Numb_Frame;		//帧序号
		
		//命令码转换
		Command_Code = Integer.toHexString(finishin_frame[17]);
		
		if (Command_Code.length() < 2) {
			Command_Code = "0" + Command_Code;
		}
		Command_Code = Integer.toHexString(finishin_frame[18]) + Command_Code;
		
		if (Command_Code.length() < 4) {
			Command_Code = "0" + Command_Code;
		}
		Command_Code = Command_Code.toUpperCase();
		
		if (finishin_frame[10] == 0) {
			i = 1;
		}
		else {
			i = 2;
		}
		
		switch(Command_Code){
		case "6201":
			//用户登录返回数据
							
			if (Numb_Frame == 0 || Numb_Frame == 1) {
				//优先处理登录权限信息
				temp_Tag_1 = 0;

				switch(finishin_frame[20]){
				case 0:
					////登录失败
					handler.obtainMessage(0, "6201 U 0 0").sendToTarget();		//传递登录失败消息
					break;
					
				case 3:
					//终端管理用户登录
					temp_Tag_1 = 3;
					break;
					
				case 4:
					//终端施工用户登录
					temp_Tag_1 = 4;
					break;
					
				default: 
					//其他情况
					handler.obtainMessage(0, "6201 U 0 0").sendToTarget();		//传递登录失败消息
					break;
				}
									
				if (temp_Tag_1 > 0) {
					//登录成功，则处理用户辖区数据
					if (finishin_frame[21] < 0) {
						i = 256 + finishin_frame[21];
					}
					else {
						i = finishin_frame[21];
					}
					
					Numb_Data = i;
										
					if (finishin_frame[22] < 0) {
						i = 256 + finishin_frame[22];
					}
					else {
						i = finishin_frame[22];
					}
					
					Numb_Data = 256 * i + Numb_Data;		//用户所辖锁数量
					
					i = Numb_Data;
					if (CRC_Tag == 1) {
						i = -1 * Numb_Data;			//校验出错，则将辖区锁数量取负值
					}

					//传递成功登录用户类型和用户辖区锁数量信息
					handler.obtainMessage(0, "6201 U " + String.valueOf(temp_Tag_1) + " " + String.valueOf(i)).sendToTarget();		//传递施工用户登录成功消息
										
					if (Numb_Data > 0) {
						//校验正确再处理用户辖区范围数据
													
						//头帧
						for (i = 0; i < 8; i++) {
							Byte_Str[i] = finishin_frame[23 + i];
						}
						
						for (i = 8; i < 17; i++) {
							Byte_Str[i] = 0;
						}

						Str_Date = new String(Byte_Str);		//获得用户辖区数据更新时间
						Str_Date = Str_Date.substring(0, 8);
						 
						handler.obtainMessage(0, "6201 D " + Str_Date).sendToTarget();		//传递用户辖区数据更新时间
						
						Posit_Frame_Byte = 31;
						
						//解析用户辖区锁ID数据
						while (Len_frame - Posit_Frame_Byte > 17 && Numb_Data > 0) {
							for (i = 0; i<17; i++) {
								Byte_Str[i] = finishin_frame[i + Posit_Frame_Byte];
							}
							
							Posit_Frame_Byte = Posit_Frame_Byte + 17;

							Str_Date = DataAlgorithm.byteToHexString(Byte_Str, 17);
							
							if (CRC_Tag == 0) {
								handler.obtainMessage(0, "6201 L " + Str_Date).sendToTarget();		//传递用户辖区锁ID
							}
							else {
								handler.obtainMessage(0, "6201 R " + Str_Date).sendToTarget();		//传递校验错误的用户辖区锁ID
							}
							
						}
					}
				}
				
			}
			else {
				//后续帧
				if (temp_Tag_1 > 0) {
					Posit_Frame_Byte = 20;
					//解析用户辖区锁ID数据
					while (Len_frame - Posit_Frame_Byte > 17 && Numb_Data > 0) {
						for (i = 0; i<17; i++) {
							Byte_Str[i] = finishin_frame[i + Posit_Frame_Byte];
						}
						
						Posit_Frame_Byte = Posit_Frame_Byte + 17;

						Str_Date = DataAlgorithm.byteToHexString(Byte_Str, 17);
						
						if (CRC_Tag == 0) {
							handler.obtainMessage(0, "6201 L " + Str_Date).sendToTarget();		//传递用户辖区锁ID
						}
						else {
							handler.obtainMessage(0, "6201 R " + Str_Date).sendToTarget();		//传递校验错误的用户辖区锁ID
						}
						
					}
					
				}
			}
			
			if (temp_Tag_1 > 0) {
				
			}
						
			break;
			
		case "6202":

			i = finishin_frame[20];
			if (i < 0) {
				i = 256 + i;
			}
			Str_Date = Integer.toHexString(i);
			if (i < 16) {
				Str_Date = "0" + Str_Date;
			}
			Str_Date = "6202 " + Str_Date.toUpperCase();
			handler.obtainMessage(0, Str_Date).sendToTarget();		//传递NB开锁的应答
			
			break;
			
		case "6203":

			i = finishin_frame[20];
			if (i < 0) {
				i = 256 + i;
			}
			Str_Date = Integer.toHexString(i);
			if (i < 16) {
				Str_Date = "0" + Str_Date;
			}
			Str_Date = "6203 " + Str_Date.toUpperCase();
			handler.obtainMessage(0, Str_Date).sendToTarget();		//传递NB开锁的应答
			
			break;
			
		case "6206":				
			if (Numb_Frame == 0 || Numb_Frame == 1) {
				//首帧处理
				if (finishin_frame[20] < 0) {
					i = 256 + finishin_frame[20];
				}
				else {
					i = finishin_frame[20];
				}
				
				Numb_Data = i;
									
				if (finishin_frame[21] < 0) {
					i = 256 + finishin_frame[21];
				}
				else {
					i = finishin_frame[21];
				}
				
				Numb_Data = 256 * i + Numb_Data;		//传送的锁数量
				
				handler.obtainMessage(0, "6206 N " + String.valueOf(Numb_Data)).sendToTarget();		//节点GIS数据
				
				if (Numb_Data > 0) {
					//头帧
					Posit_Frame_Byte = 22;
					
					//解析GIS数据
					while (Len_frame - Posit_Frame_Byte > 88 && Numb_Data > 0) {
						for (i = 0; i<10; i++) {
							Byte_Lnglat[i] = finishin_frame[i + Posit_Frame_Byte];
						}
						Str_Lng = new String(Byte_Lnglat);		//经度
						
						Posit_Frame_Byte = Posit_Frame_Byte + 10;
						for (i = 0; i<10; i++) {
							Byte_Lnglat[i] = finishin_frame[i + Posit_Frame_Byte];
						}
						Str_Lat = new String(Byte_Lnglat);		//纬度
						
						Posit_Frame_Byte = Posit_Frame_Byte + 10;
						for (i = 0; i<17; i++) {
							Byte_Str[i] = finishin_frame[i + Posit_Frame_Byte];
						}
						Str_ID = DataAlgorithm.byteToHexString(Byte_Str, 17);;		//设备ID
						
						Posit_Frame_Byte = Posit_Frame_Byte + 17;
						j = 0;
						k = 0;
						for (i = 0; i<50; i++) {
							Byte_Name[i] = finishin_frame[i + Posit_Frame_Byte];
							if (Byte_Name[i] < 0) {
								k++;
							}
							if (j == 0 && Byte_Name[i] == 0) {
								j = i;		//字符串长度；
								break;
							}
						}
						Str_Name = new String(Byte_Name);
						j = j - k + k/3;
						Str_Name = Str_Name.substring(0, j);	//设备名

						Posit_Frame_Byte = Posit_Frame_Byte + 51;
						
						if (finishin_frame[50 + Posit_Frame_Byte] == 0) {
							Str_Date = "6206 F ";	//无告警
						}
						else {
							Str_Date = "6206 W ";	//有告警
						}
						
						Str_Date = Str_Date + Str_Lng + " " + Str_Lat + " " + Str_ID + " " + Str_Name;
						handler.obtainMessage(0, Str_Date).sendToTarget();		//节点GIS数据
					}
				}
			}
			else {
				//后续帧处理
				Posit_Frame_Byte = 20;
				
				//解析GIS数据
				while (Len_frame - Posit_Frame_Byte > 88 && Numb_Data > 0) {
					for (i = 0; i<10; i++) {
						Byte_Lnglat[i] = finishin_frame[i + Posit_Frame_Byte];
					}
					Str_Lng = new String(Byte_Lnglat);		//经度
					
					Posit_Frame_Byte = Posit_Frame_Byte + 10;
					for (i = 0; i<10; i++) {
						Byte_Lnglat[i] = finishin_frame[i + Posit_Frame_Byte];
					}
					Str_Lat = new String(Byte_Lnglat);		//纬度
					
					Posit_Frame_Byte = Posit_Frame_Byte + 10;
					for (i = 0; i<17; i++) {
						Byte_Str[i] = finishin_frame[i + Posit_Frame_Byte];
					}
					Str_ID = DataAlgorithm.byteToHexString(Byte_Str, 17);;		//设备ID
					
					Posit_Frame_Byte = Posit_Frame_Byte + 17;
					j = 0;
					k = 0;
					for (i = 0; i<50; i++) {
						Byte_Name[i] = finishin_frame[i + Posit_Frame_Byte];
						if (Byte_Name[i] < 0) {
							k++;
						}
						if (j == 0 && Byte_Name[i] == 0) {
							j = i;		//字符串长度；
							break;
						}
					}
					Str_Name = new String(Byte_Name);
					j = j - k + k/3;
					Str_Name = Str_Name.substring(0, j);	//设备名

					Posit_Frame_Byte = Posit_Frame_Byte + 51;
					
					if (finishin_frame[50 + Posit_Frame_Byte] == 0) {
						Str_Date = "6206 F ";	//无告警
					}
					else {
						Str_Date = "6206 W ";	//有告警
					}
					
					Str_Date = Str_Date + Str_Lng + " " + Str_Lat + " " + Str_ID + " " + Str_Name;
					handler.obtainMessage(0, Str_Date).sendToTarget();		//节点GIS数据
				}
			}
			
			
			break;
			
		case "6207":
			if (CRC_Tag == 0) {
				Str_Date = "6207 F ";	//无告警
				
				j = 0;
				k = 0;
				for (i = 0; i<16; i++) {
					Byte_Name[i] = finishin_frame[i + 20];
					if (Byte_Name[i] < 0) {
						k++;
					}
					if (j == 0 && Byte_Name[i] == 0) {
						j = i;		//字符串长度；
						break;
					}
				}
				Str_Name = new String(Byte_Name);
				j = j - k + k/3;
				Str_Name = Str_Name.substring(0, j);	//用户名
				Str_Date = Str_Date + Str_Name + " ";
				
				j = 0;
				k = 0;
				for (i = 0; i<16; i++) {
					Byte_Name[i] = finishin_frame[i + 36];
					if (Byte_Name[i] < 0) {
						k++;
					}
					if (j == 0 && Byte_Name[i] == 0) {
						j = i;		//字符串长度；
						break;
					}
				}
				Str_Name = new String(Byte_Name);
				j = j - k + k/3;
				Str_Name = Str_Name.substring(0, j);	//密码
				Str_Date = Str_Date + Str_Name + " ";
				
				j = 0;
				k = 0;
				for (i = 0; i<16; i++) {
					Byte_Name[i] = finishin_frame[i + 52];
					if (Byte_Name[i] < 0) {
						k++;
					}
					if (j == 0 && Byte_Name[i] == 0) {
						j = i;		//字符串长度；
						break;
					}
				}
				Str_Name = new String(Byte_Name);
				j = j - k + k/3;
				Str_Name = Str_Name.substring(0, j);	//电话
				Str_Date = Str_Date + Str_Name + " ";
				
				j = 0;
				k = 0;
				for (i = 0; i<16; i++) {
					Byte_Name[i] = finishin_frame[i + 64];
					if (Byte_Name[i] < 0) {
						k++;
					}
					if (j == 0 && Byte_Name[i] == 0) {
						j = i;		//字符串长度；
						break;
					}
				}
				Str_Name = new String(Byte_Name);
				j = j - k + k/3;
				Str_Name = Str_Name.substring(0, j);	//姓名
				Str_Date = Str_Date + Str_Name;
			}
			else {
				Str_Date = "6207 W ";	//有告警
			}
			handler.obtainMessage(0, Str_Date).sendToTarget();		//节点GIS数据
			
			break;
			
		case "620A":

			if (Numb_Frame == 0 || Numb_Frame == 1) {
				//首帧处理
				if (finishin_frame[20] < 0) {
					i = 256 + finishin_frame[20];
				}
				else {
					i = finishin_frame[20];
				}
				
				Numb_Data = i;			//工单数量
								
				if (Numb_Data > 0) {
					//头帧
					Posit_Frame_Byte = 21;
					
					//解析各工单ID
					while (Len_frame - Posit_Frame_Byte > 32 && Numb_Data > 0) {
						j = 0;
						for (i = 0; i<32; i++) {
							Byte_Name[i] = finishin_frame[i + Posit_Frame_Byte];

							if (Byte_Name[i] == 0) {
								j = i;		//字符串长度；
								break;
							}
						}
						Str_Name = new String(Byte_Name);
						Str_Name = Str_Name.substring(0, j);	//工单ID
						
						Posit_Frame_Byte = Posit_Frame_Byte + 32;
						
						Str_Date = "620A ";
												
						Str_Date = Str_Date + Str_Name;
						handler.obtainMessage(0, Str_Date).sendToTarget();		//传递当前工单ID
					}
				}
			}
			else {
				//后续帧处理
				Posit_Frame_Byte = 20;
				
				while (Len_frame - Posit_Frame_Byte > 32 && Numb_Data > 0) {
					j = 0;
					for (i = 0; i<32; i++) {
						Byte_Name[i] = finishin_frame[i + Posit_Frame_Byte];

						if (Byte_Name[i] == 0) {
							j = i;		//字符串长度；
							break;
						}
					}
					Str_Name = new String(Byte_Name);
					Str_Name = Str_Name.substring(0, j);	//工单ID
					
					Posit_Frame_Byte = Posit_Frame_Byte + 32;
					
					Str_Date = "620A ";
											
					Str_Date = Str_Date + Str_Name;
					handler.obtainMessage(0, Str_Date).sendToTarget();		//传递当前工单ID
				}
			}
			
			break;
			
		case "620B":				
			if (Numb_Frame == 0 || Numb_Frame == 1) {
				//首帧处理
				if (finishin_frame[68] < 0) {
					i = 256 + finishin_frame[68];
				}
				else {
					i = finishin_frame[68];
				}
				Str_Lng = String.valueOf(i);		//施工要求数量
				
				if (finishin_frame[69] < 0) {
					i = 256 + finishin_frame[69];
				}
				else {
					i = finishin_frame[69];
				}
				Numb_Data = i;			//实际完成数量		
									
				j = 0;
				for (i = 0; i<32; i++) {
					Byte_Name[i] = finishin_frame[i + 20];

					if (j == 0 && Byte_Name[i] == 0) {
						j = i;		//字符串长度；
						break;
					}
				}
				Str_Name = new String(Byte_Name);
				Str_Name = Str_Name.substring(0, j);	//工单ID
				
				j = 0;
				for (i = 0; i<16; i++) {
					Byte_Name[i] = finishin_frame[i + 52];

					if (j == 0 && Byte_Name[i] == 0) {
						j = i;		//字符串长度；
						break;
					}
				}
				Str_Lat = new String(Byte_Name);
				Str_Lat = Str_Lat.substring(0, j);	//施工人ID			
				
				Str_Date = "620B O " + Str_Name + " " + Str_Lat + " " + Str_Lng + " " + String.valueOf(Numb_Data);
				handler.obtainMessage(0, Str_Date).sendToTarget();		//广播工单时限和工单锁数量
								
				if (Numb_Data > 0) {
					//头帧
					Posit_Frame_Byte = 70;
					
					//解析工单锁数据
					while (Len_frame - Posit_Frame_Byte > 70 && Numb_Data > 0) {
						for (i = 0; i<10; i++) {
							Byte_Lnglat[i] = finishin_frame[i + Posit_Frame_Byte];
						}
						Str_Lng = new String(Byte_Lnglat);		//经度
						
						Posit_Frame_Byte = Posit_Frame_Byte + 10;
						for (i = 0; i<10; i++) {
							Byte_Lnglat[i] = finishin_frame[i + Posit_Frame_Byte];
						}
						Str_Lat = new String(Byte_Lnglat);		//纬度
						
						Posit_Frame_Byte = Posit_Frame_Byte + 10;

						j = 0;
						k = 0;
						for (i = 0; i<50; i++) {
							Byte_Name[i] = finishin_frame[i + Posit_Frame_Byte];
							if (Byte_Name[i] < 0) {
								k++;
							}
							if (j == 0 && Byte_Name[i] == 0) {
								j = i;		//字符串长度；
								break;
							}
						}
						Str_Name = new String(Byte_Name);
						j = j - k + k/3;
						Str_Name = Str_Name.substring(0, j);	//设备名

						Posit_Frame_Byte = Posit_Frame_Byte + 50;
						
						Str_Date = "620B L ";
						
						Str_Date = Str_Date + Str_Lng + " " + Str_Lat + " " + Str_Name;
						handler.obtainMessage(0, Str_Date).sendToTarget();		//广播工单锁数据数据
					}
				}
			}
			else {
				//后续帧处理
				Posit_Frame_Byte = 20;
				
				//解析工单锁数据
				while (Len_frame - Posit_Frame_Byte > 70 && Numb_Data > 0) {
					for (i = 0; i<10; i++) {
						Byte_Lnglat[i] = finishin_frame[i + Posit_Frame_Byte];
					}
					Str_Lng = new String(Byte_Lnglat);		//经度
					
					Posit_Frame_Byte = Posit_Frame_Byte + 10;
					for (i = 0; i<10; i++) {
						Byte_Lnglat[i] = finishin_frame[i + Posit_Frame_Byte];
					}
					Str_Lat = new String(Byte_Lnglat);		//纬度
					
					Posit_Frame_Byte = Posit_Frame_Byte + 10;

					j = 0;
					k = 0;
					for (i = 0; i<50; i++) {
						Byte_Name[i] = finishin_frame[i + Posit_Frame_Byte];
						if (Byte_Name[i] < 0) {
							k++;
						}
						if (j == 0 && Byte_Name[i] == 0) {
							j = i;		//字符串长度；
							break;
						}
					}
					Str_Name = new String(Byte_Name);
					j = j - k + k/3;
					Str_Name = Str_Name.substring(0, j);	//设备名

					Posit_Frame_Byte = Posit_Frame_Byte + 50;
					
					Str_Date = "620B L ";
					
					Str_Date = Str_Date + Str_Lng + " " + Str_Lat + " " + Str_Name;
					handler.obtainMessage(0, Str_Date).sendToTarget();		//广播工单锁数据数据
				}
			}
						
			break;
			
		case "620C":

			i = finishin_frame[20];
			if (i == 0 && CRC_Tag == 0) {
				Str_Date = "620C F";	//完成
			}
			else {
				Str_Date = "620C E";
			}
			
			handler.obtainMessage(0, Str_Date).sendToTarget();		//传递NB开锁的应答
			
			break;			

			
		case "620D":

			if (Numb_Frame == 0 || Numb_Frame == 1) {
				//首帧处理
				if (finishin_frame[20] < 0) {
					i = 256 + finishin_frame[20];
				}
				else {
					i = finishin_frame[20];
				}
				
				Numb_Data = i;			//工单数量
								
				if (Numb_Data > 0) {
					//头帧
					Posit_Frame_Byte = 21;
					
					//解析各工单ID
					while (Len_frame - Posit_Frame_Byte > 32 && Numb_Data > 0) {
						j = 0;
						for (i = 0; i<32; i++) {
							Byte_Name[i] = finishin_frame[i + Posit_Frame_Byte];

							if (Byte_Name[i] == 0) {
								j = i;		//字符串长度；
								break;
							}
						}
						Str_Name = new String(Byte_Name);
						Str_Name = Str_Name.substring(0, j);	//工单ID
						
						Posit_Frame_Byte = Posit_Frame_Byte + 32;
						
						Str_Date = "620D ";
												
						Str_Date = Str_Date + Str_Name;
						handler.obtainMessage(0, Str_Date).sendToTarget();		//传递当前工单ID
					}
				}
			}
			else {
				//后续帧处理
				Posit_Frame_Byte = 20;
				
				while (Len_frame - Posit_Frame_Byte > 32 && Numb_Data > 0) {
					j = 0;
					for (i = 0; i<32; i++) {
						Byte_Name[i] = finishin_frame[i + Posit_Frame_Byte];

						if (Byte_Name[i] == 0) {
							j = i;		//字符串长度；
							break;
						}
					}
					Str_Name = new String(Byte_Name);
					Str_Name = Str_Name.substring(0, j);	//工单ID
					
					Posit_Frame_Byte = Posit_Frame_Byte + 32;
					
					Str_Date = "620D ";
											
					Str_Date = Str_Date + Str_Name;
					handler.obtainMessage(0, Str_Date).sendToTarget();		//传递当前工单ID
				}
			}
			
			break;
			
		case "620E":				
			if (Numb_Frame == 0 || Numb_Frame == 1) {
				//首帧处理
				if (finishin_frame[60] < 0) {
					i = 256 + finishin_frame[60];
				}
				else {
					i = finishin_frame[60];
				}
				
				Numb_Data = i;
				
				for (i = 0; i<8; i++) {
					Byte_Lnglat[i] = finishin_frame[i + 52];
				}
				Byte_Lnglat[8] = 0;
				Byte_Lnglat[9] = 0;
						
				Str_Lng = new String(Byte_Lnglat);
				Str_Lng = Str_Lng.substring(0, 8);		//时限要求
									
				j = 0;
				for (i = 0; i<32; i++) {
					Byte_Name[i] = finishin_frame[i + 20];

					if (j == 0 && Byte_Name[i] == 0) {
						j = i;		//字符串长度；
						break;
					}
				}
				Str_Name = new String(Byte_Name);
				Str_Name = Str_Name.substring(0, j);	//工单ID
				
				Str_Date = "620E O " + Str_Lng + " " + String.valueOf(Numb_Data) + " " + Str_Name;
				handler.obtainMessage(0, Str_Date).sendToTarget();		//广播工单时限和工单锁数量
								
				if (Numb_Data > 0) {
					//头帧
					Posit_Frame_Byte = 61;
					
					//解析工单锁数据
					while (Len_frame - Posit_Frame_Byte > 88 && Numb_Data > 0) {
						for (i = 0; i<10; i++) {
							Byte_Lnglat[i] = finishin_frame[i + Posit_Frame_Byte];
						}
						Str_Lng = new String(Byte_Lnglat);		//经度
						
						Posit_Frame_Byte = Posit_Frame_Byte + 10;
						for (i = 0; i<10; i++) {
							Byte_Lnglat[i] = finishin_frame[i + Posit_Frame_Byte];
						}
						Str_Lat = new String(Byte_Lnglat);		//纬度
						
						Posit_Frame_Byte = Posit_Frame_Byte + 10;
						for (i = 0; i<17; i++) {
							Byte_Str[i] = finishin_frame[i + Posit_Frame_Byte];
						}
						Str_ID = DataAlgorithm.byteToHexString(Byte_Str, 17);		//设备ID
						
						Posit_Frame_Byte = Posit_Frame_Byte + 17;
						j = 0;
						k = 0;
						for (i = 0; i<50; i++) {
							Byte_Name[i] = finishin_frame[i + Posit_Frame_Byte];
							if (Byte_Name[i] < 0) {
								k++;
							}
							if (j == 0 && Byte_Name[i] == 0) {
								j = i;		//字符串长度；
								break;
							}
						}
						Str_Name = new String(Byte_Name);
						j = j - k + k/3;
						Str_Name = Str_Name.substring(0, j);	//设备名

						Posit_Frame_Byte = Posit_Frame_Byte + 51;
						
						Str_Date = "620E L ";
						
						Str_Date = Str_Date + Str_Lng + " " + Str_Lat + " " + Str_ID + " " + Str_Name;
						handler.obtainMessage(0, Str_Date).sendToTarget();		//广播工单锁数据数据
					}
				}
			}
			else {
				//后续帧处理
				Posit_Frame_Byte = 20;
				
				//解析工单锁数据
				while (Len_frame - Posit_Frame_Byte > 88 && Numb_Data > 0) {
					for (i = 0; i<10; i++) {
						Byte_Lnglat[i] = finishin_frame[i + Posit_Frame_Byte];
					}
					Str_Lng = new String(Byte_Lnglat);		//经度
					
					Posit_Frame_Byte = Posit_Frame_Byte + 10;
					for (i = 0; i<10; i++) {
						Byte_Lnglat[i] = finishin_frame[i + Posit_Frame_Byte];
					}
					Str_Lat = new String(Byte_Lnglat);		//纬度
					
					Posit_Frame_Byte = Posit_Frame_Byte + 10;
					for (i = 0; i<17; i++) {
						Byte_Str[i] = finishin_frame[i + Posit_Frame_Byte];
					}
					Str_ID = DataAlgorithm.byteToHexString(Byte_Str, 17);;		//设备ID
					
					Posit_Frame_Byte = Posit_Frame_Byte + 17;
					j = 0;
					k = 0;
					for (i = 0; i<50; i++) {
						Byte_Name[i] = finishin_frame[i + Posit_Frame_Byte];
						if (Byte_Name[i] < 0) {
							k++;
						}
						if (j == 0 && Byte_Name[i] == 0) {
							j = i;		//字符串长度；
							break;
						}
					}
					Str_Name = new String(Byte_Name);
					j = j - k + k/3;
					Str_Name = Str_Name.substring(0, j);	//设备名

					Posit_Frame_Byte = Posit_Frame_Byte + 51;

					Str_Date = "620E L ";	//锁数据
					
					Str_Date = Str_Date + Str_Lng + " " + Str_Lat + " " + Str_ID + " " + Str_Name;
					handler.obtainMessage(0, Str_Date).sendToTarget();		//广播工单锁数据数据
				}
			}
						
			break;
			
		case "620F":				
			if (Numb_Frame == 0 || Numb_Frame == 1) {
				//首帧处理
				if (finishin_frame[20] < 0) {
					i = 256 + finishin_frame[20];
				}
				else {
					i = finishin_frame[20];
				}
				
				Numb_Data = i;
								
				Str_Date = "620F N " + String.valueOf(Numb_Data);
				handler.obtainMessage(0, Str_Date).sendToTarget();		//广播查询到的锁数量锁数量
								
				if (Numb_Data > 0) {
					//头帧
					Posit_Frame_Byte = 21;
					
					//解析查询锁数据
					while (Len_frame - Posit_Frame_Byte > 70 && Numb_Data > 0) {
						for (i = 0; i<10; i++) {
							Byte_Lnglat[i] = finishin_frame[i + Posit_Frame_Byte];
						}
						Str_Lng = new String(Byte_Lnglat);		//经度
						
						Posit_Frame_Byte = Posit_Frame_Byte + 10;
						for (i = 0; i<10; i++) {
							Byte_Lnglat[i] = finishin_frame[i + Posit_Frame_Byte];
						}
						Str_Lat = new String(Byte_Lnglat);		//纬度
						
						Posit_Frame_Byte = Posit_Frame_Byte + 10;

						j = 0;
						k = 0;
						for (i = 0; i<50; i++) {
							Byte_Name[i] = finishin_frame[i + Posit_Frame_Byte];
							if (Byte_Name[i] < 0) {
								k++;
							}
							if (j == 0 && Byte_Name[i] == 0) {
								j = i;		//字符串长度；
								break;
							}
						}
						Str_Name = new String(Byte_Name);
						j = j - k + k/3;
						Str_Name = Str_Name.substring(0, j);	//设备名，或资源编码

						Posit_Frame_Byte = Posit_Frame_Byte + 50;
						
						Str_Date = "620F L ";
						
						Str_Date = Str_Date + Str_Lng + " " + Str_Lat + " " + Str_Name;
						handler.obtainMessage(0, Str_Date).sendToTarget();		//广播查询到的锁位置数据数据
					}
				}
			}
			else {
				//后续帧处理
				Posit_Frame_Byte = 20;
				
				//解析查询锁数据
				while (Len_frame - Posit_Frame_Byte > 70 && Numb_Data > 0) {
					for (i = 0; i<10; i++) {
						Byte_Lnglat[i] = finishin_frame[i + Posit_Frame_Byte];
					}
					Str_Lng = new String(Byte_Lnglat);		//经度
					
					Posit_Frame_Byte = Posit_Frame_Byte + 10;
					for (i = 0; i<10; i++) {
						Byte_Lnglat[i] = finishin_frame[i + Posit_Frame_Byte];
					}
					Str_Lat = new String(Byte_Lnglat);		//纬度
					
					Posit_Frame_Byte = Posit_Frame_Byte + 10;

					j = 0;
					k = 0;
					for (i = 0; i<50; i++) {
						Byte_Name[i] = finishin_frame[i + Posit_Frame_Byte];
						if (Byte_Name[i] < 0) {
							k++;
						}
						if (j == 0 && Byte_Name[i] == 0) {
							j = i;		//字符串长度；
							break;
						}
					}
					Str_Name = new String(Byte_Name);
					j = j - k + k/3;
					Str_Name = Str_Name.substring(0, j);	//设备名，或资源编码

					Posit_Frame_Byte = Posit_Frame_Byte + 50;
					
					Str_Date = "620F L ";
					
					Str_Date = Str_Date + Str_Lng + " " + Str_Lat + " " + Str_Name;
					handler.obtainMessage(0, Str_Date).sendToTarget();		//广播查询到的锁位置数据数据
				}
			}
						
			break;
			
		case "6210":
			
			break;
			
		case "6211":

			i = finishin_frame[20];
			if (i == 0 && CRC_Tag == 0) {
				Str_Date = "6211 F";	//完成
			}
			else if (i == 112 && CRC_Tag == 0) {
				//回复0x70,回单存在错误，但让步接受
				Str_Date = "6211 C";	//让步接受
			}
			else {
				Str_Date = "6211 E";
			}
			
			handler.obtainMessage(0, Str_Date).sendToTarget();		//传递NB开锁的应答
			
			break;
			
		case "6212":

			i = finishin_frame[20];
			if (i == 0 && CRC_Tag == 0) {
				Str_Date = "6212 F";	//完成
			}
			else {
				Str_Date = "6212 E";
			}
			
			handler.obtainMessage(0, Str_Date).sendToTarget();		//传递NB开锁的应答
			
			break;
			
		case "6213":
			if (CRC_Tag == 0) {
				Str_Date = "6213 F";	//完成
			}
			else {
				Str_Date = "6213 E";
			}
			
			if (finishin_frame[20] < 0) {
				i = 256 + finishin_frame[20];
			}
			else {
				i = finishin_frame[20];
			}
			
			Numb_Data = i;
			
			Str_Lat = Str_Date + " N " + String.valueOf(Numb_Data);	//广播求助开锁数量信息
			
			handler.obtainMessage(0, Str_Lat).sendToTarget();
			
			if (Numb_Data > 0) {
				Posit_Frame_Byte = 21;
				
				//解析各条求助开锁信息
				while (Len_frame - Posit_Frame_Byte > 87 && Numb_Data > 0) {

					j = 0;
					k = 0;
					for (i = 0; i<10; i++) {
						Byte_Lnglat[i] = finishin_frame[i + Posit_Frame_Byte];
					}
					Str_Lng = new String(Byte_Lnglat);		//经度
					
					for (i = 0; i<10; i++) {
						Byte_Lnglat[i] = finishin_frame[i + Posit_Frame_Byte + 10];
					}
					Str_Lat = new String(Byte_Lnglat);		//纬度
					
					for (i = 0; i<17; i++) {
						Byte_Str[i] = finishin_frame[i + Posit_Frame_Byte + 20];
					}
					Str_ID = DataAlgorithm.byteToHexString(Byte_Str, 17);		//设备ID
					
					j = 0;
					k = 0;
					for (i = 0; i<50; i++) {
						Byte_Name[i] = finishin_frame[i + Posit_Frame_Byte + 37];
						if (Byte_Name[i] < 0) {
							k++;
						}
						if (j == 0 && Byte_Name[i] == 0) {
							j = i;		//字符串长度；
							break;
						}
					}
					Str_Name = new String(Byte_Name);
					j = j - k + k/3;
					Str_Name = Str_Name.substring(0, j);	//设备名
					
					Str_Lat = Str_Date + " D " + Str_Lng + " " + Str_Lat + " " + Str_ID + " " + Str_Name;
					
					handler.obtainMessage(0, Str_Lat).sendToTarget();		//广播用求助开锁数据
					
					Posit_Frame_Byte = Posit_Frame_Byte + 87;
					
				}
			}
						
			break;
			
		case "6214":

			if (CRC_Tag == 0) {
				Str_Date = "6214 F";	//完成
			}
			else {
				Str_Date = "6214 E";
			}
			
			if (Numb_Frame == 0 || Numb_Frame == 1) {
				//首帧处理
				if (finishin_frame[20] < 0) {
					i = 256 + finishin_frame[20];
				}
				else {
					i = finishin_frame[20];
				}
				
				Numb_Data = i;
				
				Str_Lat = Str_Date + " N " + String.valueOf(Numb_Data);	//广播用户列表数量
				
				handler.obtainMessage(0, Str_Lat).sendToTarget();
								
				if (Numb_Data > 0) {
					//头帧
					Posit_Frame_Byte = 21;
					
					//解析用户数据
					while (Len_frame - Posit_Frame_Byte > 29 && Numb_Data > 0) {

						j = 0;
						k = 0;
						for (i = 0; i<16; i++) {
							Byte_Str[i] = finishin_frame[i + Posit_Frame_Byte];
							if (Byte_Str[i] < 0) {
								k++;
							}
							if (j == 0 && Byte_Str[i] == 0) {
								j = i;		//字符串长度；
								break;
							}
						}

						Str_Name = new String(Byte_Str);
						j = j - k + k/3;
						Str_Name = Str_Name.substring(0, j);	//用户ID

						Posit_Frame_Byte = Posit_Frame_Byte + 16;
						
						j = 0;
						k = 0;
						for (i = 0; i<12; i++) {
							Byte_Str[i] = finishin_frame[i + Posit_Frame_Byte];
							if (Byte_Str[i] < 0) {
								k++;
							}
							if (j == 0 && Byte_Str[i] == 0) {
								j = i;		//字符串长度；
								break;
							}
						}

						Str_Lng = new String(Byte_Str);
						j = j - k + k/3;
						Str_Lng = Str_Lng.substring(0, j);		//用户真实姓名

						i = finishin_frame[12 + Posit_Frame_Byte];
						Posit_Frame_Byte = Posit_Frame_Byte + 13;
						
						Str_Lat = Str_Date + " U " + Str_Name + " " + Str_Lng + " " + String.valueOf(i);
						handler.obtainMessage(0, Str_Lat).sendToTarget();		//广播用户数据
					}
				}
			}
			else {
				//后续帧处理
				Posit_Frame_Byte = 20;
				
				//解析工单锁数据
				while (Len_frame - Posit_Frame_Byte > 88 && Numb_Data > 0) {
					j = 0;
					k = 0;
					for (i = 0; i<16; i++) {
						Byte_Str[i] = finishin_frame[i + Posit_Frame_Byte];
						if (Byte_Str[i] < 0) {
							k++;
						}
						if (j == 0 && Byte_Str[i] == 0) {
							j = i;		//字符串长度；
							break;
						}
					}

					Str_Name = new String(Byte_Str);
					j = j - k + k/3;
					Str_Name = Str_Name.substring(0, j);	//用户ID

					Posit_Frame_Byte = Posit_Frame_Byte + 16;
					
					j = 0;
					k = 0;
					for (i = 0; i<12; i++) {
						Byte_Str[i] = finishin_frame[i + Posit_Frame_Byte];
						if (Byte_Str[i] < 0) {
							k++;
						}
						if (j == 0 && Byte_Str[i] == 0) {
							j = i;		//字符串长度；
							break;
						}
					}

					Str_Lng = new String(Byte_Str);
					j = j - k + k/3;
					Str_Lng = Str_Lng.substring(0, j);		//用户真实姓名

					i = finishin_frame[12 + Posit_Frame_Byte];
					Posit_Frame_Byte = Posit_Frame_Byte + 13;
					
					Str_Lat = Str_Date + " U " + Str_Name + " " + Str_Lng + " " + String.valueOf(i);
					handler.obtainMessage(0, Str_Lat).sendToTarget();		//广播用户数据数据
				}
			}
			
			break;
			
		case "6215":

			if (CRC_Tag == 0) {
				Str_Date_0 = "6215 F";	//完成
			}
			else {
				Str_Date_0 = "6215 E";
			}
			
			if (Numb_Frame == 0 || Numb_Frame == 1) {
				//首帧处理
				if (finishin_frame[20] < 0) {
					i = 256 + finishin_frame[20];
				}
				else {
					i = finishin_frame[20];
				}
				
				Numb_Data = i;
				
				Str_Lat = Str_Date_0 + " N " + String.valueOf(Numb_Data);
				
				handler.obtainMessage(0, Str_Lat).sendToTarget();	//广播辖区数量
								
				if (Numb_Data > 0) {
					//头帧
					Posit_Frame_Byte = 21;
					
					//解析用户数据
					while (Len_frame - Posit_Frame_Byte > 59 && Numb_Data > 0) {
						i = finishin_frame[Posit_Frame_Byte];
						Str_Date = Str_Date_0 + " P " + String.valueOf(i);	//添加范畴类型
						
						for (i = 0; i<8; i++) {
							Byte_Str[i] = finishin_frame[i + Posit_Frame_Byte + 1];
						}
						
						Str_Date = Str_Date + " " + DataAlgorithm.byteToHexString(Byte_Str, 8);	//添加范畴ID
						
						Posit_Frame_Byte = Posit_Frame_Byte + 9;
						j = 0;
						k = 0;
						for (i = 0; i<50; i++) {
							Byte_Name[i] = finishin_frame[i + Posit_Frame_Byte];
							if (Byte_Name[i] < 0) {
								k++;
							}
							if (j == 0 && Byte_Name[i] == 0) {
								j = i;		//字符串长度；
								break;
							}
						}

						Str_Name = new String(Byte_Name);
						j = j - k + k/3;
						Str_Name = Str_Name.substring(0, j);	//用户ID
						Str_Date = Str_Date + " " + Str_Name;	//添加范畴名;

						Posit_Frame_Byte = Posit_Frame_Byte + 50;
												
						handler.obtainMessage(0, Str_Date).sendToTarget();		//广播辖区范畴数据
					}
				}
			}
			else {
				//后续帧处理
				Posit_Frame_Byte = 20;
				
				//解析工单锁数据
				while (Len_frame - Posit_Frame_Byte > 88 && Numb_Data > 0) {
					i = finishin_frame[Posit_Frame_Byte];
					Str_Date = Str_Date_0 + " P " + String.valueOf(i);	//添加范畴类型

					for (i = 0; i<8; i++) {
						Byte_Str[i] = finishin_frame[i + Posit_Frame_Byte + 1];
					}
					
					Str_Date = Str_Date + " " + DataAlgorithm.byteToHexString(Byte_Str, 8);	//添加范畴ID
										
					Posit_Frame_Byte = Posit_Frame_Byte + 9;
					j = 0;
					k = 0;
					for (i = 0; i<50; i++) {
						Byte_Name[i] = finishin_frame[i + Posit_Frame_Byte];
						if (Byte_Name[i] < 0) {
							k++;
						}
						if (j == 0 && Byte_Name[i] == 0) {
							j = i;		//字符串长度；
							break;
						}
					}

					Str_Name = new String(Byte_Name);
					j = j - k + k/3;
					Str_Name = Str_Name.substring(0, j);	//用户ID
					Str_Date = Str_Date + " " + Str_Name;	//添加范畴名;

					Posit_Frame_Byte = Posit_Frame_Byte + 50;
											
					handler.obtainMessage(0, Str_Date).sendToTarget();		//广播辖区范畴数据
				}
			}
			
			break;
			
		case "6216":
			if (CRC_Tag == 0) {
				Str_Date = "6216 F ";
			}
			else {
				Str_Date = "6216 E ";
			}

			i = finishin_frame[20];
			if (i == 0) {
				Str_Date = Str_Date + "00";		//完成
			}
			else if (i == 17){
				Str_Date = Str_Date + "11";		//电话号码已被占用
			}
			else {
				Str_Date = Str_Date + "FF";
			}
			
			handler.obtainMessage(0, Str_Date).sendToTarget();		//传递NB开锁的应答
			
			break;
			
		case "6217":
			if (CRC_Tag == 0) {
				Str_Date = "6217 F ";
			}
			else {
				Str_Date = "6217 E ";
			}

			i = finishin_frame[20];
			if (i == 0) {
				Str_Date = Str_Date + "00";		//平台注册成功
			}
			else if (i == 1){
				Str_Date = Str_Date + "01";		//网管表示收到指令
			}
			else if (i == 0x44){
				Str_Date = Str_Date + "44";		//网管已经注册
			}
			else if (i == 0x0B){
				Str_Date = Str_Date + "0B";		//网管向平台注册失败
			}
			else if (i == -1){
				Str_Date = Str_Date + "FF";		//网管收信CRC出错
			}
			
			handler.obtainMessage(0, Str_Date).sendToTarget();		//传递NB开锁的应答
			
			break;
			
		case "6224":
			if (CRC_Tag == 0) {
				Str_Date_0 = "6224 F";	//完成
				
				if (Numb_Frame == 0 || Numb_Frame == 1) {
					//首帧处理
					for (i = 0; i<16; i++) {
						Byte_Str[i] = finishin_frame[i + 20];
					}
					
					Str_Date_0 = Str_Date_0 + " Ver" + DataAlgorithm.byteToHexString(Byte_Str, 16);	//添加软件版本
					
					Byte_Str[0] = finishin_frame[38];
					Byte_Str[1] = finishin_frame[39];
					Byte_Str[2] = 0;
					
					Str_Date_0 = Str_Date_0 + " " + DataAlgorithm.byteToHexString(Byte_Str, 2);	//添加软件数据的CRC校验值
					
					if (finishin_frame[36] < 0) {
						i = 256 + finishin_frame[36];
					}
					else {
						i = finishin_frame[36];
					}
					
					if (finishin_frame[37] < 0) {
						j = 256 + finishin_frame[37];
					}
					else {
						j = finishin_frame[37];
					}
					
					i = i + 256 * j;
					
					Str_Date_0 = Str_Date_0 + " " + String.valueOf(i);	//添加升级软件长度
					
					handler.obtainMessage(0, Str_Date_0).sendToTarget();		//传递升级软件版本、校验值和长度
					
					Str_Date = "6224 F 1/";		//第1帧
					
					if (finishin_frame[5] < 0) {
						i = 256 + finishin_frame[5];
					}
					else {
						i = finishin_frame[5];
					}
					
					if (finishin_frame[6] < 0) {
						j = 256 + finishin_frame[6];
					}
					else {
						j = finishin_frame[6];
					}
					
					i = i + 256 * j;	//帧总数
					
					Str_Date = Str_Date + String.valueOf(i) + " ";	//添加帧总数;
					
					
					for (i = 40; i < Len_frame - 2 ; i++) {
						Byte_Date[i - 40] = finishin_frame[i];
					}
					Str_Date = Str_Date + DataAlgorithm.byteToHexString(Byte_Date, Len_frame - 42);	//当前帧中传递的设备升级软件数据
					j = i;
				}
				else {
					//后续帧处理
					if (finishin_frame[3] < 0) {
						i = 256 + finishin_frame[3];
					}
					else {
						i = finishin_frame[3];
					}
					
					if (finishin_frame[4] < 0) {
						j = 256 + finishin_frame[4];
					}
					else {
						j = finishin_frame[4];
					}
					
					i = i + 256 * j;	//帧序号
					
					Str_Date = "6224 F " + String.valueOf(i) + "/";	//添加帧序号;
						
					if (finishin_frame[5] < 0) {
						i = 256 + finishin_frame[5];
					}
					else {
						i = finishin_frame[5];
					}
					
					if (finishin_frame[6] < 0) {
						j = 256 + finishin_frame[6];
					}
					else {
						j = finishin_frame[6];
					}
					
					i = i + 256 * j;	//帧总数
					
					Str_Date = Str_Date + String.valueOf(i) + " ";	//添加帧总数;
					
					for (i = 20; i < Len_frame - 2 ; i++) {
						Byte_Date[i - 20] = finishin_frame[i];
					}
					Str_Date = Str_Date + DataAlgorithm.byteToHexString(Byte_Date, Len_frame - 22);	//当前帧中传递的设备升级软件数据
				}
				
				handler.obtainMessage(0, Str_Date).sendToTarget();		//传递升级软件数据
			}
			else {
				//有误码
				Str_Date_0 = "6224 E";
				handler.obtainMessage(0, Str_Date_0).sendToTarget();		//传递校验出错信息
			}
			
			break;
			
		case "6230":
			if (CRC_Tag == 0) {
				Str_Date = "6230 F ";
			}
			else {
				Str_Date = "6230 E ";
			}

			Str_Date_0 = Integer.toHexString(finishin_frame[20]);
			if (Str_Date_0.length() == 1) {
				Str_Date_0 = "0" + Str_Date_0;
			}
			else if (Str_Date_0.length() > 2) {
				Str_Date_0 = Str_Date_0.substring(Str_Date_0.length() - 2);
			}
			Str_Date = Str_Date + Str_Date_0;
			
			Str_Date_0 = Integer.toHexString(finishin_frame[21]);
			if (Str_Date_0.length() == 1) {
				Str_Date_0 = "0" + Str_Date_0;
			}
			else if (Str_Date_0.length() > 2) {
				Str_Date_0 = Str_Date_0.substring(Str_Date_0.length() - 2);
			}
			Str_Date = Str_Date + Str_Date_0;
			
			Str_Date_0 = Integer.toHexString(finishin_frame[22]);
			if (Str_Date_0.length() == 1) {
				Str_Date_0 = "0" + Str_Date_0;
			}
			else if (Str_Date_0.length() > 2) {
				Str_Date_0 = Str_Date_0.substring(Str_Date_0.length() - 2);
			}
			Str_Date = Str_Date + Str_Date_0;
			Str_Date = Str_Date.toUpperCase();
			
			handler.obtainMessage(0, Str_Date).sendToTarget();		//传递信息
			break;
		
		}
	}
	
	public static void Send_Basic_Data() {
		//生成基本发送指令序列
		
		//报文帧头
		Tx_byteData[0] = 0x7E;
		
		//协议版本号
		Tx_byteData[1] = 0x00;
		Tx_byteData[2] = 0x10;
		
		//帧序号
		Tx_byteData[3] = 0x01;
		Tx_byteData[4] = 0x00;		//小端格式
		
		//帧总数
		Tx_byteData[5] = 0x01;
		Tx_byteData[6] = 0x00;
		
		//预留字段
		Tx_byteData[7] = 0x00;
		Tx_byteData[8] = 0x00;
		Tx_byteData[9] = 0x00;
		Tx_byteData[10] = 0x00;
		Tx_byteData[11] = 0x00;
		Tx_byteData[12] = 0x00;
		Tx_byteData[13] = 0x00;
		Tx_byteData[14] = 0x00;
		Tx_byteData[15] = 0x00;
		Tx_byteData[16] = 0x00;

		//命令码
		Tx_byteData[17] = 0x00;				//各指令的代码不同
		Tx_byteData[18] = 0x00;
		
		//状态码
		Tx_byteData[19] = (byte) 0xFF;
		
		//消息体
		Tx_byteData[20] = 0x01;
		Tx_byteData[21] = (byte) 0xAA;
		
		//CRC校验
		Tx_byteData[22] = 0x00;
		Tx_byteData[23] = 0x01;
		
		//报文帧尾
		Tx_byteData[24] = 0x7E;
		
	}
	
	
	public void User_Login_6201(String User_Name, String Login_PassWD, String Data_Date) {
		//用户登录上报
		int i, j=0;

		Send_Basic_Data();

		//命令码
		Tx_byteData[17] = 0x01;
		Tx_byteData[18] = 0x62;
		
		//消息体
		byte Byte_Name[] = User_Name.getBytes();
		byte Byte_PassWD[] = Login_PassWD.getBytes();
		
		if (Data_Date.length() != 8) {
			Data_Date = "00000000";
		}
		
		byte Byte_Date[] = Data_Date.getBytes();
		
		for (i = 0; i<16; i++) {
			if (i>=Byte_Name.length) {
				Tx_byteData[20 + i] = 0;
			}
			else {
				Tx_byteData[20 + i] = Byte_Name[i];
			}
		}
		
		for (i = 0; i<16; i++) {
			if (i>=Byte_PassWD.length) {
				Tx_byteData[36 + i] = 0;
			}
			else {
				Tx_byteData[36 + i] = Byte_PassWD[i];
			}
		}
		
		for (i = 0; i<8; i++) {
			if (i>Byte_Date.length) {
				Tx_byteData[52 + i] = 0;
			}
			else {
				Tx_byteData[52 + i] = Byte_Date[i];
			}
		}
				
		//CRC校验
		final int Tx_Length = CRC_And_Trans_7E(62);					//进行CRC校验和转义处理
		//长度参数是：19+3+消息体长度
		
		new Thread(new Runnable() {
	        public void run() {
	        	try {
					TCP_Out.write( Tx_byteData, 0, Tx_Length + 1);		//将 Tx_byteData 前面的有效数据传送出去
				} catch (IOException e) {

					e.printStackTrace();
				}
	        }
	    }).start();

	}
	
	public static void Open_NB_Lock_6202(String login_User_Name, String Str_ID) {
		//发送通过物联网开锁指令
		int i, j;
		byte Byte_Name[] = login_User_Name.getBytes();
		byte[] Byte_ID = DataAlgorithm.hexStringToBytes(Str_ID);
		
		Send_Basic_Data();
		
		//命令码
		Tx_byteData[17] = 0x02;
		Tx_byteData[18] = 0x62;
				
		for (i = 0; i<Byte_Name.length; i++) {
			Tx_byteData[20 + i] = Byte_Name[i];
		}
		j = i;
		
		for (i = j; i<16; i++) {
			Tx_byteData[20 + i] = 0;
		}
		
		for (i = 0; i<17; i++) {
			Tx_byteData[36 + i] = Byte_ID[i];
		}
		
		//CRC校验
		final int Tx_Length = CRC_And_Trans_7E(55);					//进行CRC校验和转义处理
		//长度参数是：19+3+消息体长度
				
		new Thread(new Runnable() {
	        public void run() {
	        	try {
					TCP_Out.write( Tx_byteData, 0, Tx_Length + 1);		//将 Tx_byteData 前面的有效数据传送出去
				} catch (IOException e) {

					e.printStackTrace();
				}
	        }
	    }).start();
				
	}
	
	
	public static void Call_Open_Lock_6203(String User_ID, String Lock_ID, String GPS_Lng, String GPS_Lat) {
		//远程开锁求助

		int i;
		
		Send_Basic_Data();
				
		byte Byte_lat[] = GPS_Lat.getBytes();
		byte Byte_lng[] = GPS_Lng.getBytes();
		byte Byte_ID[] = DataAlgorithm.hexStringToBytes(Lock_ID);
		byte Byte_User[] = User_ID.getBytes();
		
		//命令码
		Tx_byteData[17] = 0x03;
		Tx_byteData[18] = 0x62;
				
		for (i = 0; i<10; i++) {
			Tx_byteData[20+i] = Byte_lng[i];
		}
		
		for (i = 0; i<10; i++) {
			Tx_byteData[30+i] = Byte_lat[i];
		}
		
		for (i = 0; i<17; i++) {
			Tx_byteData[40 + i] = Byte_ID[i];
		}
		
		for (i = 0; i<Byte_User.length; i++) {
			Tx_byteData[57 + i] = Byte_User[i];
		}
		for (i = Byte_User.length; i<16; i++) {
			Tx_byteData[57 + i] = 0;
		}
		
		//CRC校验
		final int Tx_Length = CRC_And_Trans_7E(75);					//进行CRC校验和转义处理
		//长度参数是：19+3+消息体长度
				
		new Thread(new Runnable() {
	        public void run() {
	        	try {
					TCP_Out.write( Tx_byteData, 0, Tx_Length + 1);		//将 Tx_byteData 前面的有效数据传送出去
				} catch (IOException e) {

					e.printStackTrace();
				}
	        }
	    }).start();
	}
	
	
	public static void GIS_Query_6206(Double Db_Lat, Double Db_Lng, double Db_Err) {
		//发送GIS显示查询指令
		String Str_Lat, Str_Lng, Str_Err;
		int i;
		
		Send_Basic_Data();
		
		Str_Lat = String.valueOf(Db_Lat);
		if (Str_Lat.indexOf(".") > -1) {
			Str_Lat = Str_Lat + "0000000000";
		}
		else {
			Str_Lat = Str_Lat + ".0000000000";
		}
		
		Str_Lng = String.valueOf(Db_Lng);
		if (Str_Lng.indexOf(".") > -1) {
			Str_Lng = Str_Lng + "0000000000";
		}
		else {
			Str_Lng = Str_Lng + ".0000000000";
		}
		
		BigDecimal d1 = new BigDecimal(Double.toString(Db_Err));		//若是科学计数法，则转换成常规小数计数法

		Str_Err = String.valueOf(d1);
		if (Str_Err.indexOf(".") > -1) {
			Str_Err = Str_Err + "0000000000";
		}
		else {
			Str_Err = Str_Err + ".0000000000";
		}
								
		Str_Lat = Str_Lat.substring(0, 10);
		Str_Lng = Str_Lng.substring(0, 10);
		Str_Err = Str_Err.substring(0, 6);
		
		byte Byte_lat[] = Str_Lat.getBytes();
		byte Byte_lng[] = Str_Lng.getBytes();
		byte Byte_Err[] = Str_Err.getBytes();
		
		//命令码
		Tx_byteData[17] = 0x06;
		Tx_byteData[18] = 0x62;
				
		for (i = 0; i<10; i++) {
			Tx_byteData[20+i] = Byte_lng[i];
		}
		
		for (i = 0; i<10; i++) {
			Tx_byteData[30+i] = Byte_lat[i];
		}
		
		for (i = 0; i<6; i++) {
			Tx_byteData[40+i] = Byte_Err[i];
		}
		
		//CRC校验
		final int Tx_Length = CRC_And_Trans_7E(48);					//进行CRC校验和转义处理
		//长度参数是：19+3+消息体长度
				
		new Thread(new Runnable() {
	        public void run() {
	        	try {
					TCP_Out.write( Tx_byteData, 0, Tx_Length + 1);		//将 Tx_byteData 前面的有效数据传送出去
				} catch (IOException e) {

					e.printStackTrace();
				}
	        }
	    }).start();
		
	}
	
	public static void Read_User_Inform_6207(String login_User_Name) {
		//取用户信息
		int i, j;
		byte Byte_Name[] = login_User_Name.getBytes();
		
		Send_Basic_Data();
		
		//命令码
		Tx_byteData[17] = 0x07;
		Tx_byteData[18] = 0x62;
				
		for (i = 0; i<Byte_Name.length; i++) {
			Tx_byteData[20 + i] = Byte_Name[i];
		}
		j = i;
		
		for (i = j; i<16; i++) {
			Tx_byteData[20 + i] = 0;
		}
			
		i = Cala_CRC(Tx_byteData, 20, 35);
		
		Tx_byteData[36] = (byte) (i & 0xFF);				//低8位
		Tx_byteData[37] = (byte) (i >> 8);						//高8位
		
		//CRC校验
		final int Tx_Length = CRC_And_Trans_7E(40);					//进行CRC校验和转义处理
		//长度参数是：19+3+消息体长度
				
		new Thread(new Runnable() {
	        public void run() {
	        	try {
					TCP_Out.write( Tx_byteData, 0, Tx_Length + 1);		//将 Tx_byteData 前面的有效数据传送出去
				} catch (IOException e) {

					e.printStackTrace();
				}
	        }
	    }).start();
	}
	
	public static void DownLoad_CheckList_620A(String login_User_Name) {
		//待审查工单列表下载
		int i, j;
		byte Byte_Name[] = login_User_Name.getBytes();
		
		Send_Basic_Data();
		
		//命令码
		Tx_byteData[17] = 0x0A;
		Tx_byteData[18] = 0x62;
				
		for (i = 0; i<Byte_Name.length; i++) {
			Tx_byteData[20 + i] = Byte_Name[i];
		}
		j = i;
		
		for (i = j; i<16; i++) {
			Tx_byteData[20 + i] = 0;
		}
				
		//CRC校验
		final int Tx_Length = CRC_And_Trans_7E(38);					//进行CRC校验和转义处理
		//长度参数是：19+3+消息体长度
				
		new Thread(new Runnable() {
	        public void run() {
	        	try {
					TCP_Out.write( Tx_byteData, 0, Tx_Length + 1);		//将 Tx_byteData 前面的有效数据传送出去
				} catch (IOException e) {

					e.printStackTrace();
				}
	        }
	    }).start();
	}
	
	public static void DownLoad_CheckData_620B(String Order_ID) {
		//下载待审查工单数据
		int i, j;
		byte Byte_Name[] = Order_ID.getBytes();
		
		Send_Basic_Data();
		
		//命令码
		Tx_byteData[17] = 0x0B;
		Tx_byteData[18] = 0x62;
				
		for (i = 0; i<Byte_Name.length; i++) {
			Tx_byteData[20 + i] = Byte_Name[i];
		}
		j = i;
		
		for (i = j; i<32; i++) {
			Tx_byteData[20 + i] = 0;
		}
				
		//CRC校验
		final int Tx_Length = CRC_And_Trans_7E(54);					//进行CRC校验和转义处理
		//长度参数是：19+3+消息体长度
				
		new Thread(new Runnable() {
	        public void run() {
	        	try {
					TCP_Out.write( Tx_byteData, 0, Tx_Length + 1);		//将 Tx_byteData 前面的有效数据传送出去
				} catch (IOException e) {

					e.printStackTrace();
				}
	        }
	    }).start();
	}
	
	public static void Report_Checkup_620C(String Order_ID, boolean Review) {
		//上传审查结论， Review为 true是审查通过，false是审查驳回
		int i, j;
		byte Byte_Name[] = Order_ID.getBytes();
		
		Send_Basic_Data();
		
		//命令码
		Tx_byteData[17] = 0x0C;
		Tx_byteData[18] = 0x62;
				
		for (i = 0; i<Byte_Name.length; i++) {
			Tx_byteData[20 + i] = Byte_Name[i];
		}
		j = i;
		
		for (i = j; i<32; i++) {
			Tx_byteData[20 + i] = 0;
		}
			
		if (Review) {
			Tx_byteData[52] = 0;
		}
		else {
			Tx_byteData[52] = -1;
		}
		
		//CRC校验
		final int Tx_Length = CRC_And_Trans_7E(55);					//进行CRC校验和转义处理
		//长度参数是：19+3+消息体长度
				
		new Thread(new Runnable() {
	        public void run() {
	        	try {
					TCP_Out.write( Tx_byteData, 0, Tx_Length + 1);		//将 Tx_byteData 前面的有效数据传送出去
				} catch (IOException e) {

					e.printStackTrace();
				}
	        }
	    }).start();
	}
	
	public static void DownLoad_OrderList_620D(String login_User_Name) {
		//下载工单列表
		int i, j;
		byte Byte_Name[] = login_User_Name.getBytes();
		
		Send_Basic_Data();
		
		//命令码
		Tx_byteData[17] = 0x0D;
		Tx_byteData[18] = 0x62;
				
		for (i = 0; i<Byte_Name.length; i++) {
			Tx_byteData[20 + i] = Byte_Name[i];
		}
		j = i;
		
		for (i = j; i<16; i++) {
			Tx_byteData[20 + i] = 0;
		}
				
		//CRC校验
		final int Tx_Length = CRC_And_Trans_7E(38);					//进行CRC校验和转义处理
		//长度参数是：19+3+消息体长度
				
		new Thread(new Runnable() {
	        public void run() {
	        	try {
					TCP_Out.write( Tx_byteData, 0, Tx_Length + 1);		//将 Tx_byteData 前面的有效数据传送出去
				} catch (IOException e) {

					e.printStackTrace();
				}
	        }
	    }).start();

	}
	
	
	public static void DownLoad_OrderData_620E(String Order_ID) {
		//发送下载工单数据指令
		int i, j;
		byte Byte_Name[] = Order_ID.getBytes();
		
		Send_Basic_Data();
		
		//命令码
		Tx_byteData[17] = 0x0E;
		Tx_byteData[18] = 0x62;
				
		for (i = 0; i<Byte_Name.length; i++) {
			Tx_byteData[20 + i] = Byte_Name[i];
		}
		j = i;
		
		for (i = j; i<32; i++) {
			Tx_byteData[20 + i] = 0;
		}
				
		//CRC校验
		final int Tx_Length = CRC_And_Trans_7E(54);					//进行CRC校验和转义处理
		//长度参数是：19+3+消息体长度
				
		new Thread(new Runnable() {
	        public void run() {
	        	try {
					TCP_Out.write( Tx_byteData, 0, Tx_Length + 1);		//将 Tx_byteData 前面的有效数据传送出去
				} catch (IOException e) {

					e.printStackTrace();
				}
	        }
	    }).start();
	}
	
	public static void DownLoad_Lock_Query_620F(String Str_text, boolean Name_checked) {
		// TODO 发送锁具地理位置查询指令，Str_text是查询值，Name_checked 为 true表示查询设备名，而false表示查询资源编码
		int i, j;
		byte Byte_Name[] = Str_text.getBytes();
		
		Send_Basic_Data();
		
		//命令码
		Tx_byteData[17] = 0x0F;
		Tx_byteData[18] = 0x62;
		
		if (Name_checked) {
			//查锁名
			Tx_byteData[20] = 0x00;
		}
		else {
			//查资源编码
			Tx_byteData[20] = (byte) 0xFF;
		}
				
		for (i = 0; i<Byte_Name.length; i++) {
			Tx_byteData[21 + i] = Byte_Name[i];
		}
		j = i;
		
		for (i = j; i<50; i++) {
			Tx_byteData[21 + i] = 0;
		}
				
		//CRC校验
		final int Tx_Length = CRC_And_Trans_7E(73);					//进行CRC校验和转义处理
		//长度参数是：19+3+消息体长度
				
		new Thread(new Runnable() {
	        public void run() {
	        	try {
					TCP_Out.write( Tx_byteData, 0, Tx_Length + 1);		//将 Tx_byteData 前面的有效数据传送出去
				} catch (IOException e) {

					e.printStackTrace();
				}
	        }
	    }).start();
	}
	
	public static void Update_Log_Data_6210(String user_Log, String log_recode, String log_Obj) {
		//上传日志数据
		int i, j, Length_Tx;
		byte Byte_Name[] = user_Log.getBytes();
		
		Send_Basic_Data();
		
		//命令码
		Tx_byteData[17] = 0x10;
		Tx_byteData[18] = 0x62;
				
		for (i = 0; i<Byte_Name.length; i++) {
			Tx_byteData[20 + i] = Byte_Name[i];
		}
		j = i;
		
		for (i = j; i<16; i++) {
			Tx_byteData[20 + i] = 0;
		}
		Length_Tx = 36;
		
		byte Byte_recode[] = log_recode.getBytes();
		j = Byte_recode.length;
		
		if (j > 255) {
			j = 255;
		}
		Tx_byteData[36] = 0x1D;
		Tx_byteData[37] = (byte) j;
		
		for (i = 0; i<j; i++) {
			Tx_byteData[38 + i] = Byte_recode[i];
		}
		
		Length_Tx = 38 + j;
		
		byte Byte_Obj[] = log_Obj.getBytes();
		j = Byte_Obj.length;
		
		if (j > 255) {
			j = 255;
		}
		Tx_byteData[Length_Tx] = 0x1D;
		Length_Tx ++;
		Tx_byteData[Length_Tx] = (byte) j;
		Length_Tx ++;
		
		for (i = 0; i<j; i++) {
			Tx_byteData[Length_Tx + i] = Byte_Obj[i];
		}
			
		Length_Tx = Length_Tx + j;
		
		//CRC校验
		final int Tx_Length = CRC_And_Trans_7E(Length_Tx + 2);					//进行CRC校验和转义处理
		//长度参数是：19+3+消息体长度
				
		new Thread(new Runnable() {
	        public void run() {
	        	try {
					TCP_Out.write( Tx_byteData, 0, Tx_Length + 1);		//将 Tx_byteData 前面的有效数据传送出去
				} catch (IOException e) {

					e.printStackTrace();
				}
	        }
	    }).start();
		
	}
	
	public static int[] BackOrder_6211(String workID, DBHelper dbHelp, boolean Finish_Tag, int[] int_Frame_Numb) {
		//发送回单指令，Finish_Tag为 true 是回单， 为 false 是退单， int_Frame_Numb[0]为0是头帧，否则是后续帧
		int i, j;
		String temp_Str;
		byte Byte_lnglat[] = new byte[10];
		byte Byte_Lock[] = new byte[17];
		byte Byte_Name[] = new byte[50];
		byte Byte_ID[] = workID.getBytes();
		byte Byte_imsi[] = new byte[16];
		
		Send_Basic_Data();
		
		//命令码
		Tx_byteData[17] = 0x11;
		Tx_byteData[18] = 0x62;
				
		for (i = 0; i<Byte_ID.length; i++) {
			Tx_byteData[20 + i] = Byte_ID[i];
		}
		j = i;
		
		for (i = j; i<32; i++) {
			Tx_byteData[20 + i] = 0;
		}
		
		if (! Finish_Tag) {
			//是退单
			Tx_byteData[52] = 0;
			Tx_byteData[53] = 0;
			i = 53 + 3;		//退单长度，参数是：19+3+消息体长度
			
			int_Frame_Numb[0] = 1;
			int_Frame_Numb[1] = 1;
		}
		else {
			//完工回单
			SQLiteDatabase db=dbHelp.getWritableDatabase();
			Cursor cursor;
			
			if (int_Frame_Numb[0] == 0) {
				//回单头帧
				cursor=db.rawQuery("select count(*) from Order_Data_Table  where Order_ID = ? AND Status = '1'", new String[]{workID});    			    
				if (cursor.moveToNext()){
					i = cursor.getInt(0);
				}
				cursor.close();
				
				if (i == 0) {
					//没有回单数据，则直接退出，不回单
					int_Frame_Numb[0] = 0;
					int_Frame_Numb[1] = 0;
					return int_Frame_Numb;
				}
				
				int_Frame_Numb[1] = (int) (i/8.0);		//计算总帧数
				
				if (i % 8 > 0) {
					//有余数，则总帧数多一帧
					int_Frame_Numb[1] ++;	
				}
								
				Tx_byteData[52] = (byte) i;		//锁数据条数
				Tx_byteData[53] = (byte) i;

				int_Frame_Numb[0] = 1;
				Tx_byteData[3] = 1;				//帧号
				Tx_byteData[5] = (byte) int_Frame_Numb[1];				//帧总数
				
				cursor=db.rawQuery("select * from Order_Data_Table  where Order_ID = ? AND Status = '1'", new String[]{workID});    			    
				i = 0;
				while (cursor.moveToNext() && i<8){
					if (i < 8) {
						temp_Str = cursor.getString(7);		//取锁位置经度
						if (temp_Str == null) {
							for (j = 0; j < 10; j++) {
								Tx_byteData[54 + i * 104 + j] = 0;
							}
						}
						else {
							if (temp_Str.indexOf(".") > -1) {
								temp_Str = temp_Str + "0000000000";
							}
							else {
								temp_Str = temp_Str + ".0000000000";
							}
							temp_Str = temp_Str.substring(0, 10);
							Byte_lnglat = temp_Str.getBytes();
							for (j = 0; j < 10; j++) {
								Tx_byteData[54 + i * 104 + j] = Byte_lnglat[j];
							}
						}
												
						temp_Str = cursor.getString(8);		//取锁位置纬度
						if (temp_Str == null) {
							for (j = 0; j < 10; j++) {
								Tx_byteData[64 + i * 104 + j] = 0;
							}
						}
						else {
							if (temp_Str.indexOf(".") > -1) {
								temp_Str = temp_Str + "0000000000";
							}
							else {
								temp_Str = temp_Str + ".0000000000";
							}
							temp_Str = temp_Str.substring(0, 10);
							Byte_lnglat = temp_Str.getBytes();
							for (j = 0; j < 10; j++) {
								Tx_byteData[64 + i * 104 + j] = Byte_lnglat[j];
							}
						}
												
						temp_Str = cursor.getString(9);		//取新锁ID
						if (temp_Str == null) {
							temp_Str = cursor.getString(2);		//取老锁ID
						}
						Byte_Lock = DataAlgorithm.hexStringToBytes(temp_Str);
						for (j = 0; j < 17; j++) {
							Tx_byteData[74 + i * 104 + j] = Byte_Lock[j];
						}
						
						temp_Str = cursor.getString(6);		//取锁名
						if (temp_Str == null) {
							for (j = 0; j < 50; j++) {
								Tx_byteData[91 + i * 104 + j] = 0;
							}
						}
						else {
							Byte_Name = temp_Str.getBytes();
							for (j = 0; j < Byte_Name.length; j++) {
								Tx_byteData[91 + i * 104 + j] = Byte_Name[j];
							}
							for (j = Byte_Name.length; j < 50; j++) {
								Tx_byteData[91 + i * 104 + j] = 0;
							}
						}
						
						temp_Str = cursor.getString(11);		//取NB卡的imsi码Byte_imsi
						if (temp_Str == null) {
							for (j = 0; j < 16; j++) {
								Tx_byteData[141 + i * 104 + j] = 0;
							}
						}
						else {
							Byte_imsi = DataAlgorithm.hexStringToBytes(temp_Str);
							for (j = 0; j < Byte_imsi.length; j++) {
								Tx_byteData[141 + i * 104 + j] = Byte_imsi[j];
							}
							for (j = Byte_imsi.length; j < 16; j++) {
								Tx_byteData[141 + i * 104 + j] = 0;
							}
						}
						
						Tx_byteData[157 + i * 104] = 0;		//最后一个是告警状态字节，暂填0
						
					}

					i++;
				}
				cursor.close();
				i = 22 + 34 + 104 * i;		//帧长度

			}
			else {
				//回单后续帧，数据从[20]开始保存
				int k = 8 * int_Frame_Numb[0];
				int_Frame_Numb[0]++;		//帧号递增
				
				Tx_byteData[3] = (byte) int_Frame_Numb[0];				//帧号
				Tx_byteData[5] = (byte) int_Frame_Numb[1];				//帧总数
				
				cursor=db.rawQuery("select * from Order_Data_Table  where Order_ID = ? AND Status = '1'", new String[]{workID});    			    
				i = 0;
				while (cursor.moveToNext()){
					if (i >= k && i< 8 * int_Frame_Numb[0]) {
						temp_Str = cursor.getString(7);		//取锁位置经度
						
						if (temp_Str == null) {
							for (j = 0; j < 10; j++) {
								Tx_byteData[20 + (i-k) * 104 + j] = 0;
							}
						}
						else {
							if (temp_Str.indexOf(".") > -1) {
								temp_Str = temp_Str + "0000000000";
							}
							else {
								temp_Str = temp_Str + ".0000000000";
							}
							temp_Str = temp_Str.substring(0, 10);
							Byte_lnglat = temp_Str.getBytes();
							for (j = 0; j < 10; j++) {
								Tx_byteData[20 + (i-k) * 104 + j] = Byte_lnglat[j];
							}
						}					
						
						temp_Str = cursor.getString(8);		//取锁位置纬度
						if (temp_Str == null) {
							for (j = 0; j < 10; j++) {
								Tx_byteData[30 + (i-k) * 104 + j] = Byte_lnglat[j];
							}
						}
						else {
							if (temp_Str.indexOf(".") > -1) {
								temp_Str = temp_Str + "0000000000";
							}
							else {
								temp_Str = temp_Str + ".0000000000";
							}
							temp_Str = temp_Str.substring(0, 10);
							Byte_lnglat = temp_Str.getBytes();
							for (j = 0; j < 10; j++) {
								Tx_byteData[30 + (i-k) * 104 + j] = Byte_lnglat[j];
							}
						}
																		
						temp_Str = cursor.getString(9);		//取锁ID
						if (temp_Str == null) {
							temp_Str = cursor.getString(2);		//取老锁ID
						}
						Byte_Lock = DataAlgorithm.hexStringToBytes(temp_Str);

						for (j = 0; j < 17; j++) {
							Tx_byteData[40 + (i-k) * 104 + j] = Byte_Lock[j];
						}
						
						temp_Str = cursor.getString(6);		//取锁名
						if (temp_Str == null) {
							for (j = 0; j < 50; j++) {
								Tx_byteData[57 + (i-k) * 104 + j] = 0;
							}
						}
						else {
							Byte_Name = temp_Str.getBytes();
							for (j = 0; j < Byte_Name.length; j++) {
								Tx_byteData[57 + (i-k) * 104 + j] = Byte_Name[j];
							}
							for (j = Byte_Name.length; j < 50; j++) {
								Tx_byteData[57 + (i-k) * 104 + j] = 0;
							}
						}
						
						temp_Str = cursor.getString(11);		//取NB卡的imsi码Byte_imsi
						if (temp_Str == null) {
							for (j = 0; j < 16; j++) {
								Tx_byteData[107 + (i-k) * 104 + j] = 0;
							}
						}
						else {
							Byte_imsi = DataAlgorithm.hexStringToBytes(temp_Str);
							for (j = 0; j < Byte_imsi.length; j++) {
								Tx_byteData[107 + (i-k) * 104 + j] = Byte_imsi[j];
							}
							for (j = Byte_imsi.length; j < 16; j++) {
								Tx_byteData[107 + (i-k) * 104 + j] = 0;
							}
						}
						
						Tx_byteData[123 + (i-k) * 104] = 0;		//最后一个是告警状态字节，暂填0
					}

					i++;
					
					if (i-k >= 8) {
						break;
					}
				}
				i = 22 + 104 * (i-k);		//帧长度
			}
			db.close();
		}
				
		//CRC校验
		final int Tx_Length = CRC_And_Trans_7E(i);					//进行CRC校验和转义处理
				
		new Thread(new Runnable() {
	        public void run() {
	        	try {
					TCP_Out.write( Tx_byteData, 0, Tx_Length + 1);		//将 Tx_byteData 前面的有效数据传送出去
				} catch (IOException e) {

					e.printStackTrace();
				}
	        }
	    }).start();
		
		return int_Frame_Numb;
	}
	
	public static int[] Update_PopedomData_6212(String user_ID,	int index, String[] Get_Name, String[] Get_ID, int[] Get_Type, boolean[] Get_Selected, int[] int_Frame_Numb) {
		//上传用户辖区选择结果
		int i, j, k, Numb_Selected = 0;
		String temp_Str;
		
		long temp_Long;
		
		byte Byte_Name[] = new byte[50];
		byte Byte_ID[] = user_ID.getBytes();
		
		Send_Basic_Data();
		
		//命令码
		Tx_byteData[17] = 0x12;
		Tx_byteData[18] = 0x62;
		
		if (int_Frame_Numb[0] == 0) {
			//辖区更新头帧
			for (i = 0; i<index; i++) {
				if (Get_Selected[i]) {
					Numb_Selected++;		//统计被选中的范畴数量
				}
			}
			
			if (Numb_Selected > 255) {
				Numb_Selected = 255;		//范畴数量限制值
			}
			
			if (Numb_Selected == 0) {
				//没有已选辖区数据，则直接退出，不上传
				int_Frame_Numb[0] = 0;
				int_Frame_Numb[1] = 0;
				return int_Frame_Numb;
			}
			
			int_Frame_Numb[1] = (int) (Numb_Selected/15.0);		//计算总帧数
			
			if (Numb_Selected % 15 > 0) {
				//有余数，则总帧数多一帧
				int_Frame_Numb[1] ++;	
			}

			int_Frame_Numb[0] = 1;
			Tx_byteData[3] = 1;				//帧号
			Tx_byteData[5] = (byte) int_Frame_Numb[1];				//帧总数
						
			for (i = 0; i<Byte_ID.length; i++ ) {
				Tx_byteData[20 + i] = Byte_ID[i];		//用户ID
			}
			for (i = Byte_ID.length; i<16; i++ ) {
				Tx_byteData[20 + i] = 0;
			}
			
			Tx_byteData[36] = (byte) Numb_Selected;		//范畴数量
			
			j = 0;
			for (i = 0; i<index; i++) {
				if (Get_Selected[i]) {
					//被选中的范畴需要传送
					if (j < 15) {
						Tx_byteData[37 + j * 59] = (byte) Get_Type[i];	//范畴类型
						
						temp_Str = Get_ID[i].substring(0, 2);
						Tx_byteData[37 + 1 + j * 59] = (byte) Integer.parseInt(temp_Str, 16);
						
						temp_Str = Get_ID[i].substring(2, 4);
						Tx_byteData[37 + 2 + j * 59] = (byte) Integer.parseInt(temp_Str, 16);
						
						temp_Str = Get_ID[i].substring(4, 6);
						Tx_byteData[37 + 3 + j * 59] = (byte) Integer.parseInt(temp_Str, 16);
						
						temp_Str = Get_ID[i].substring(6, 8);
						Tx_byteData[37 + 4 + j * 59] = (byte) Integer.parseInt(temp_Str, 16);
						
						temp_Str = Get_ID[i].substring(8, 10);
						Tx_byteData[37 + 5 + j * 59] = (byte) Integer.parseInt(temp_Str, 16);
						
						temp_Str = Get_ID[i].substring(10, 12);
						Tx_byteData[37 + 6 + j * 59] = (byte) Integer.parseInt(temp_Str, 16);
						
						temp_Str = Get_ID[i].substring(12, 14);
						Tx_byteData[37 + 7 + j * 59] = (byte) Integer.parseInt(temp_Str, 16);
						
						temp_Str = Get_ID[i].substring(14);
						Tx_byteData[37 + 8 + j * 59] = (byte) Integer.parseInt(temp_Str, 16);
						
						temp_Str = Get_Name[i];		//范畴名
						Byte_Name = temp_Str.getBytes();
						for (k = 0; k < Byte_Name.length; k++) {
							Tx_byteData[46 + j * 59 + k] = Byte_Name[k];
						}
						for (k = Byte_Name.length; k < 50; k++) {
							Tx_byteData[46 + j * 59 + k] = 0;
						}
					}
					else {
						break;	//此帧结束
					}
					j++;
				}
			}
			i = 22 + 17 + 59 * j;		//TODO 帧长度好像有点问题

		}
		else {
			//回单后续帧，数据从[20]开始保存
			k = 15 * int_Frame_Numb[0];
			int_Frame_Numb[0]++;		//帧号递增
			
			Tx_byteData[3] = (byte) int_Frame_Numb[0];				//帧号
			Tx_byteData[5] = (byte) int_Frame_Numb[1];				//帧总数
			
			j = 0;
			int n = k;
			for (i = 0; i<index; i++) {
				if (Get_Selected[i]) {
					//被选中的范畴需要传送
					if (j >= n && j < n + 15) {
						Tx_byteData[20 + (j-n) * 59] = (byte) Get_Type[i];	//范畴类型
						
						temp_Str = Get_ID[i].substring(0, 2);
						Tx_byteData[20 + 1 + (j-n) * 59] = (byte) Integer.parseInt(temp_Str, 16);
						Tx_byteData[20 + 2 + (j-n) * 59] = (byte) Integer.parseInt(temp_Str, 16);
						Tx_byteData[20 + 3 + (j-n) * 59] = (byte) Integer.parseInt(temp_Str, 16);
						Tx_byteData[20 + 4 + (j-n) * 59] = (byte) Integer.parseInt(temp_Str, 16);
						Tx_byteData[20 + 5 + (j-n) * 59] = (byte) Integer.parseInt(temp_Str, 16);
						Tx_byteData[20 + 6 + (j-n) * 59] = (byte) Integer.parseInt(temp_Str, 16);
						Tx_byteData[20 + 7 + (j-n) * 59] = (byte) Integer.parseInt(temp_Str, 16);
						Tx_byteData[20 + 8 + (j-n) * 59] = (byte) Integer.parseInt(temp_Str, 16);
						
						temp_Str = Get_Name[i];		//范畴名
						Byte_Name = temp_Str.getBytes();
						for (k = 0; k < Byte_Name.length; k++) {
							Tx_byteData[29 + (j-n) * 59 + k] = Byte_Name[k];
						}
						for (k = Byte_Name.length; k < 50; k++) {
							Tx_byteData[29 + (j-n) * 59 + k] = 0;
						}
					}
					else {
						break;	//此帧结束
					}
					j++;
				}
			}
						
			i = 22 + (j-n) * 59;
		}
				
		//CRC校验
		final int Tx_Length = CRC_And_Trans_7E(i);					//进行CRC校验和转义处理
				
		new Thread(new Runnable() {
	        public void run() {
	        	try {
					TCP_Out.write( Tx_byteData, 0, Tx_Length + 1);		//将 Tx_byteData 前面的有效数据传送出去
				} catch (IOException e) {

					e.printStackTrace();
				}
	        }
	    }).start();
		
		return int_Frame_Numb;
	}
	
	public static void Read_Help_Lock_6213(String User_ID) {
		//读取远程开锁求助信息
		int i;
		byte Byte_Name[] = User_ID.getBytes();
		
		Send_Basic_Data();
		
		//命令码
		Tx_byteData[17] = 0x013;
		Tx_byteData[18] = 0x62;
				
		for (i = 0; i<Byte_Name.length; i++) {
			Tx_byteData[20 + i] = Byte_Name[i];
		}
		
		for (i = Byte_Name.length; i<16; i++) {
			Tx_byteData[20 + i] = 0;
		}
				
		//CRC校验
		final int Tx_Length = CRC_And_Trans_7E(38);					//进行CRC校验和转义处理
		//长度参数是：19+3+消息体长度
				
		new Thread(new Runnable() {
	        public void run() {
	        	try {
					TCP_Out.write( Tx_byteData, 0, Tx_Length + 1);		//将 Tx_byteData 前面的有效数据传送出去
				} catch (IOException e) {

					e.printStackTrace();
				}
	        }
	    }).start();
	}
	
	public static void DownLoad_OrderData_6214(String Master_User_ID) {
		//发送下载群组用户列表指令
		
		int i, j;
		byte Byte_Name[] = Master_User_ID.getBytes();
		
		Send_Basic_Data();
		
		//命令码
		Tx_byteData[17] = 0x014;
		Tx_byteData[18] = 0x62;
				
		for (i = 0; i<Byte_Name.length; i++) {
			Tx_byteData[20 + i] = Byte_Name[i];
		}
		j = i;
		
		for (i = j; i<16; i++) {
			Tx_byteData[20 + i] = 0;
		}
				
		//CRC校验
		final int Tx_Length = CRC_And_Trans_7E(38);					//进行CRC校验和转义处理
		//长度参数是：19+3+消息体长度
				
		new Thread(new Runnable() {
	        public void run() {
	        	try {
					TCP_Out.write( Tx_byteData, 0, Tx_Length + 1);		//将 Tx_byteData 前面的有效数据传送出去
				} catch (IOException e) {

					e.printStackTrace();
				}
	        }
	    }).start();
				
	}
	
	
	public static void DownLoad_PopedomData_6215(String User_ID) {
		//下载辖区数据
		int i, j;
		byte Byte_Name[] = User_ID.getBytes();
		
		Send_Basic_Data();
		
		//命令码
		Tx_byteData[17] = 0x015;
		Tx_byteData[18] = 0x62;
				
		for (i = 0; i<Byte_Name.length; i++) {
			Tx_byteData[20 + i] = Byte_Name[i];
		}
		j = i;
		
		for (i = j; i<16; i++) {
			Tx_byteData[20 + i] = 0;
		}
				
		//CRC校验
		final int Tx_Length = CRC_And_Trans_7E(38);					//进行CRC校验和转义处理
		//长度参数是：19+3+消息体长度
				
		new Thread(new Runnable() {
	        public void run() {
	        	try {
					TCP_Out.write( Tx_byteData, 0, Tx_Length + 1);		//将 Tx_byteData 前面的有效数据传送出去
				} catch (IOException e) {

					e.printStackTrace();
				}
	        }
	    }).start();
	}

	
	
	public static void Update_User_Inform_6216(String Boss_ID, String User_ID, String str_NewPassword, String Str_telephone, String User_Name, int Int_Tag) {
		//刷新用户信息，Boss_ID：群组主管ID; User_ID:用户ID; str_NewPassword:用户新密码；Str_telephone:用户新电话；User_Name:用户真实姓名；Int_Tag为0时是删除用户信息，其它情况下为添加或更改信息
		int i, j;
		byte Byte_Str[] = new byte[16];
		byte Byte_tph[] = new byte[11];
		
		Send_Basic_Data();
		
		//命令码
		Tx_byteData[17] = 0x16;
		Tx_byteData[18] = 0x62;
				
		Byte_Str = Boss_ID.getBytes();
		for (i = 0; i<Byte_Str.length; i++) {
			Tx_byteData[20 + i] = Byte_Str[i];
		}
		j = i;
		for (i = j; i<16; i++) {
			Tx_byteData[20 + i] = 0;
		}
		
		Byte_Str = User_ID.getBytes();
		for (i = 0; i<Byte_Str.length; i++) {
			Tx_byteData[36 + i] = Byte_Str[i];
		}
		j = i;
		for (i = j; i<16; i++) {
			Tx_byteData[36 + i] = 0;
		}
		
		Byte_Str = str_NewPassword.getBytes();
		for (i = 0; i<Byte_Str.length; i++) {
			Tx_byteData[52 + i] = Byte_Str[i];
		}
		j = i;
		for (i = j; i<16; i++) {
			Tx_byteData[52 + i] = 0;
		}
		
		Byte_Str = Str_telephone.getBytes();
		for (i = 0; i<Byte_Str.length; i++) {
			Tx_byteData[68 + i] = Byte_Str[i];
		}
		j = i;
		for (i = j; i<11; i++) {
			Tx_byteData[68 + i] = 0;
		}
		
		Byte_Str = User_Name.getBytes();
		for (i = 0; i<Byte_Str.length; i++) {
			Tx_byteData[80 + i] = Byte_Str[i];
		}
		j = i;
		for (i = j; i<11; i++) {
			Tx_byteData[80 + i] = 0;
		}
		
		Tx_byteData[92] = (byte) Int_Tag;
		
		//CRC校验
		final int Tx_Length = CRC_And_Trans_7E(95);					//进行CRC校验和转义处理
		//长度参数是：19+3+消息体长度
				
		new Thread(new Runnable() {
	        public void run() {
	        	try {
					TCP_Out.write( Tx_byteData, 0, Tx_Length + 1);		//将 Tx_byteData 前面的有效数据传送出去
				} catch (IOException e) {

					e.printStackTrace();
				}
	        }
	    }).start();
	}
	
	
	public static void Send_NB_Information_6217(String lock_ID, String lock_imsi) {
		//上报 NB信息
		String lock_imei = lock_ID.substring(2);
		byte Byte_NB[] = new byte[16];
		int j;
		
		Send_Basic_Data();
		
		//命令码
		Tx_byteData[17] = 0x17;
		Tx_byteData[18] = 0x62;
		
		Byte_NB = DataAlgorithm.hexStringToBytes(lock_imei);
		for (j = 0; j < Byte_NB.length; j++) {
			Tx_byteData[20 + j] = Byte_NB[j];
		}
		for (j = Byte_NB.length; j < 16; j++) {
			Tx_byteData[20 + j] = 0;
		}
		
		Byte_NB = DataAlgorithm.hexStringToBytes(lock_imsi);
		for (j = 0; j < Byte_NB.length; j++) {
			Tx_byteData[36 + j] = Byte_NB[j];
		}
		for (j = Byte_NB.length; j < 16; j++) {
			Tx_byteData[36 + j] = 0;
		}
		
		//CRC校验
		final int Tx_Length = CRC_And_Trans_7E(54);					//进行CRC校验和转义处理
		//长度参数是：19+3+消息体长度
				
		new Thread(new Runnable() {
	        public void run() {
	        	try {
					TCP_Out.write( Tx_byteData, 0, Tx_Length + 1);		//将 Tx_byteData 前面的有效数据传送出去
				} catch (IOException e) {

					e.printStackTrace();
				}
	        }
	    }).start();
				
	}
	
	public static void Download_Softwear_6224() {
		// 下载设备升级用软件指令
		Send_Basic_Data();
		
		//命令码
		Tx_byteData[17] = 0x24;
		Tx_byteData[18] = 0x62;
				
		//CRC校验
		Tx_byteData[20] = 0x5F;
		Tx_byteData[21] = 0x65;
		Tx_byteData[22] = 0x7E;
		
		final int Tx_Length = 22;
		//长度参数是：19+3+消息体长度
		
		new Thread(new Runnable() {
	        public void run() {
	        	try {
					TCP_Out.write( Tx_byteData, 0, Tx_Length + 1);		//将 Tx_byteData 前面的有效数据传送出去
				} catch (IOException e) {

					e.printStackTrace();
				}
	        }
	    }).start();
		
	}
	
	public static void Read_NMS_Information_6230(String lock_ID, int int_Type) {
		//从网管读锁信息
		String lock_imei = lock_ID.substring(2);
		byte Byte_NB[] = new byte[16];
		int j;
		
		Send_Basic_Data();
		
		//命令码
		Tx_byteData[17] = 0x30;
		Tx_byteData[18] = 0x62;
		
		Tx_byteData[20] = (byte) int_Type;		//查询类型
		
		Byte_NB = DataAlgorithm.hexStringToBytes(lock_imei);
		for (j = 0; j < Byte_NB.length; j++) {
			Tx_byteData[21 + j] = Byte_NB[j];
		}
		for (j = Byte_NB.length; j < 16; j++) {
			Tx_byteData[21 + j] = 0;
		}
				
		//CRC校验
		final int Tx_Length = CRC_And_Trans_7E(39);					//进行CRC校验和转义处理
		//长度参数是：19+3+消息体长度
				
		new Thread(new Runnable() {
	        public void run() {
	        	try {
					TCP_Out.write( Tx_byteData, 0, Tx_Length + 1);		//将 Tx_byteData 前面的有效数据传送出去
				} catch (IOException e) {

					e.printStackTrace();
				}
	        }
	    }).start();
				
	}

	public static int Cala_CRC(byte[] Data_in_buff, int bye_Lenght) {
		// 不校验0存储位置的帧头，对最后数据位置到 bye_Lenght 的 二进制数据序列计算 CRC。CRC多项式为 x^16+x^12+x^5+1
		
		int CRC_Table_All[] = {0x0000, 0x1021, 0x2042, 0x3063, 0x4084, 0x50a5, 0x60c6, 0x70e7, 					//256个 CRC余数
													0x8108, 0x9129, 0xa14a, 0xb16b, 0xc18c, 0xd1ad, 0xe1ce, 0xf1ef, 
													0x1231, 0x0210, 0x3273, 0x2252, 0x52b5, 0x4294, 0x72f7, 0x62d6, 
													0x9339, 0x8318, 0xb37b, 0xa35a, 0xd3bd, 0xc39c, 0xf3ff, 0xe3de, 
													0x2462, 0x3443, 0x0420, 0x1401, 0x64e6, 0x74c7, 0x44a4, 0x5485, 
													0xa56a, 0xb54b, 0x8528, 0x9509, 0xe5ee, 0xf5cf, 0xc5ac, 0xd58d, 
													0x3653, 0x2672, 0x1611, 0x0630, 0x76d7, 0x66f6, 0x5695, 0x46b4, 
													0xb75b, 0xa77a, 0x9719, 0x8738, 0xf7df, 0xe7fe, 0xd79d, 0xc7bc,
													0x48c4, 0x58e5, 0x6886, 0x78a7, 0x0840, 0x1861, 0x2802, 0x3823,
													0xc9cc, 0xd9ed, 0xe98e, 0xf9af, 0x8948, 0x9969, 0xa90a, 0xb92b,
													0x5af5, 0x4ad4, 0x7ab7, 0x6a96, 0x1a71, 0x0a50, 0x3a33, 0x2a12,
													0xdbfd, 0xcbdc, 0xfbbf, 0xeb9e, 0x9b79, 0x8b58, 0xbb3b, 0xab1a, 
													0x6ca6, 0x7c87, 0x4ce4, 0x5cc5, 0x2c22, 0x3c03, 0x0c60, 0x1c41,
													0xedae, 0xfd8f, 0xcdec, 0xddcd, 0xad2a, 0xbd0b, 0x8d68, 0x9d49,
													0x7e97, 0x6eb6, 0x5ed5, 0x4ef4, 0x3e13, 0x2e32, 0x1e51, 0x0e70,
													0xff9f, 0xefbe, 0xdfdd, 0xcffc, 0xbf1b, 0xaf3a, 0x9f59, 0x8f78,
													0x9188, 0x81a9, 0xb1ca, 0xa1eb, 0xd10c, 0xc12d, 0xf14e, 0xe16f,
													0x1080, 0x00a1, 0x30c2, 0x20e3, 0x5004, 0x4025, 0x7046, 0x6067,
													0x83b9, 0x9398, 0xa3fb, 0xb3da, 0xc33d, 0xd31c, 0xe37f, 0xf35e,
													0x02b1, 0x1290, 0x22f3, 0x32d2, 0x4235, 0x5214, 0x6277, 0x7256,
													0xb5ea, 0xa5cb, 0x95a8, 0x8589, 0xf56e, 0xe54f, 0xd52c, 0xc50d,
													0x34e2, 0x24c3, 0x14a0, 0x0481, 0x7466, 0x6447, 0x5424, 0x4405,
													0xa7db, 0xb7fa, 0x8799, 0x97b8, 0xe75f, 0xf77e, 0xc71d, 0xd73c,
													0x26d3, 0x36f2, 0x0691, 0x16b0, 0x6657, 0x7676, 0x4615, 0x5634,
													0xd94c, 0xc96d, 0xf90e, 0xe92f, 0x99c8, 0x89e9, 0xb98a, 0xa9ab,
													0x5844, 0x4865, 0x7806, 0x6827, 0x18c0, 0x08e1, 0x3882, 0x28a3,
													0xcb7d, 0xdb5c, 0xeb3f, 0xfb1e, 0x8bf9, 0x9bd8, 0xabbb, 0xbb9a,
													0x4a75, 0x5a54, 0x6a37, 0x7a16, 0x0af1, 0x1ad0, 0x2ab3, 0x3a92,
													0xfd2e, 0xed0f, 0xdd6c, 0xcd4d, 0xbdaa, 0xad8b, 0x9de8, 0x8dc9,
													0x7c26, 0x6c07, 0x5c64, 0x4c45, 0x3ca2, 0x2c83, 0x1ce0, 0x0cc1,
													0xef1f, 0xff3e, 0xcf5d, 0xdf7c, 0xaf9b, 0xbfba, 0x8fd9, 0x9ff8,
													0x6e17, 0x7e36, 0x4e55, 0x5e74, 0x2e93, 0x3eb2, 0x0ed1, 0x1ef0 };
		
		int crc_All = 0;
		int i, j, len;
		int  da;
		
		
		len = bye_Lenght;
		for (i = 1; i <= len; i++) {			//对帧头的7E不进行CRC计算
			da = crc_All >> 8;
			crc_All = (crc_All & 0xFF) << 8;
			
			j = Data_in_buff[i];
			if (j < 0) {
				j = 256 + Data_in_buff[i];				//负数转成无符号数
			}

			crc_All = crc_All ^ CRC_Table_All[j ^ da];
		}
		
		return crc_All;
	}
	
	public static int Cala_CRC(byte[] Data_in_buff, int bye_Start, int bye_End) {
		// 由bye_Start到最后数据位置到 bye_Lenght 的 二进制数据序列计算 CRC。CRC多项式为 x^16+x^12+x^5+1
		
		int CRC_Table_All[] = {0x0000, 0x1021, 0x2042, 0x3063, 0x4084, 0x50a5, 0x60c6, 0x70e7, 					//256个 CRC余数
													0x8108, 0x9129, 0xa14a, 0xb16b, 0xc18c, 0xd1ad, 0xe1ce, 0xf1ef, 
													0x1231, 0x0210, 0x3273, 0x2252, 0x52b5, 0x4294, 0x72f7, 0x62d6, 
													0x9339, 0x8318, 0xb37b, 0xa35a, 0xd3bd, 0xc39c, 0xf3ff, 0xe3de, 
													0x2462, 0x3443, 0x0420, 0x1401, 0x64e6, 0x74c7, 0x44a4, 0x5485, 
													0xa56a, 0xb54b, 0x8528, 0x9509, 0xe5ee, 0xf5cf, 0xc5ac, 0xd58d, 
													0x3653, 0x2672, 0x1611, 0x0630, 0x76d7, 0x66f6, 0x5695, 0x46b4, 
													0xb75b, 0xa77a, 0x9719, 0x8738, 0xf7df, 0xe7fe, 0xd79d, 0xc7bc,
													0x48c4, 0x58e5, 0x6886, 0x78a7, 0x0840, 0x1861, 0x2802, 0x3823,
													0xc9cc, 0xd9ed, 0xe98e, 0xf9af, 0x8948, 0x9969, 0xa90a, 0xb92b,
													0x5af5, 0x4ad4, 0x7ab7, 0x6a96, 0x1a71, 0x0a50, 0x3a33, 0x2a12,
													0xdbfd, 0xcbdc, 0xfbbf, 0xeb9e, 0x9b79, 0x8b58, 0xbb3b, 0xab1a, 
													0x6ca6, 0x7c87, 0x4ce4, 0x5cc5, 0x2c22, 0x3c03, 0x0c60, 0x1c41,
													0xedae, 0xfd8f, 0xcdec, 0xddcd, 0xad2a, 0xbd0b, 0x8d68, 0x9d49,
													0x7e97, 0x6eb6, 0x5ed5, 0x4ef4, 0x3e13, 0x2e32, 0x1e51, 0x0e70,
													0xff9f, 0xefbe, 0xdfdd, 0xcffc, 0xbf1b, 0xaf3a, 0x9f59, 0x8f78,
													0x9188, 0x81a9, 0xb1ca, 0xa1eb, 0xd10c, 0xc12d, 0xf14e, 0xe16f,
													0x1080, 0x00a1, 0x30c2, 0x20e3, 0x5004, 0x4025, 0x7046, 0x6067,
													0x83b9, 0x9398, 0xa3fb, 0xb3da, 0xc33d, 0xd31c, 0xe37f, 0xf35e,
													0x02b1, 0x1290, 0x22f3, 0x32d2, 0x4235, 0x5214, 0x6277, 0x7256,
													0xb5ea, 0xa5cb, 0x95a8, 0x8589, 0xf56e, 0xe54f, 0xd52c, 0xc50d,
													0x34e2, 0x24c3, 0x14a0, 0x0481, 0x7466, 0x6447, 0x5424, 0x4405,
													0xa7db, 0xb7fa, 0x8799, 0x97b8, 0xe75f, 0xf77e, 0xc71d, 0xd73c,
													0x26d3, 0x36f2, 0x0691, 0x16b0, 0x6657, 0x7676, 0x4615, 0x5634,
													0xd94c, 0xc96d, 0xf90e, 0xe92f, 0x99c8, 0x89e9, 0xb98a, 0xa9ab,
													0x5844, 0x4865, 0x7806, 0x6827, 0x18c0, 0x08e1, 0x3882, 0x28a3,
													0xcb7d, 0xdb5c, 0xeb3f, 0xfb1e, 0x8bf9, 0x9bd8, 0xabbb, 0xbb9a,
													0x4a75, 0x5a54, 0x6a37, 0x7a16, 0x0af1, 0x1ad0, 0x2ab3, 0x3a92,
													0xfd2e, 0xed0f, 0xdd6c, 0xcd4d, 0xbdaa, 0xad8b, 0x9de8, 0x8dc9,
													0x7c26, 0x6c07, 0x5c64, 0x4c45, 0x3ca2, 0x2c83, 0x1ce0, 0x0cc1,
													0xef1f, 0xff3e, 0xcf5d, 0xdf7c, 0xaf9b, 0xbfba, 0x8fd9, 0x9ff8,
													0x6e17, 0x7e36, 0x4e55, 0x5e74, 0x2e93, 0x3eb2, 0x0ed1, 0x1ef0 };
		
		int crc_All = 0;
		int i, j;
		int  da;
		
		for (i = bye_Start; i <= bye_End; i++) {			//对帧头的7E不进行CRC计算
			da = crc_All >> 8;
			crc_All = (crc_All & 0xFF) << 8;
			
			j = Data_in_buff[i];
			if (j < 0) {
				j = 256 + Data_in_buff[i];				//负数转成无符号数
			}

			crc_All = crc_All ^ CRC_Table_All[j ^ da];
		}
		
		return crc_All;
	}
		
	
	public static int CRC_And_Trans_7E(int Tx_Len) {
		// 计算CRC，然后对传送数据中的7E数据进行转义处理，输入值为数据长度-1，输出为转义处理后的数据长度 -1
		int i, j, k, Tx_Length;

		//CRC校验
		i = Cala_CRC(Tx_byteData, Tx_Len - 3);
		Tx_byteData[Tx_Len - 2] = (byte) (i & 0xFF);				//低8位
		Tx_byteData[Tx_Len - 1] = (byte) (i >> 8);						//高8位
		
		//结束符
		Tx_byteData[Tx_Len] = 0x7E;
		
		//非结束符的0x7E 的转意处理
		Tx_Length = Tx_Len;
		
		//将7D转成 7D 5D
		i = 1;
		j = 0;
		while (j == 0) {
			if (Tx_byteData[i] == 0x7D ) {
				//数据序列中有 7D，则将后续数据全部后移一位，数据长度 + 1，并将 7E 转义为 7D, 5D
				for (k = Tx_Length; k >= i; k--) {
					Tx_byteData[k + 1] = Tx_byteData[k];
				}
				Tx_Length++;
				Tx_byteData[i] = 0x7D;
				Tx_byteData[i + 1] = 0x5D;
				i ++;
			}
			
			i ++;
			if (i >= Tx_Length)		j = 1;
		}
		
		//将7E转成 7D 5E
		i = 1;
		j = 0;
		while (j == 0) {
			if (Tx_byteData[i] == 0x7E ) {
				//数据序列中有 7E，则将后续数据全部后移一位，数据长度 + 1，并将 7E 转义为 7D, 5E
				for (k = Tx_Length; k >= i; k--) {
					Tx_byteData[k + 1] = Tx_byteData[k];
				}
				Tx_Length++;
				Tx_byteData[i] = 0x7D;
				Tx_byteData[i + 1] = 0x5E;
				i ++;
			}
			
			i ++;
			if (i >= Tx_Length)		j = 1;
		}
		
		return Tx_Length;
	}
	
	public static int Cala_CRC_8(byte[] Data_in_buff, int Start_Post, int bye_Lenght) {
		// 用于对电子标签的数据进行CRC计算。CRC多项式为 x^8+x^5+x^4+1
		
		int CRC_Table_All[] = {0x00, 0x5e, 0xbc, 0xe2, 0x61, 0x3f, 0xdd, 0x83,  0xc2, 0x9c, 0x7e, 0x20, 0xa3, 0xfd, 0x1f, 0x41,  
													0x9d, 0xc3, 0x21, 0x7f, 0xfc, 0xa2, 0x40, 0x1e,  0x5f, 0x01, 0xe3, 0xbd, 0x3e, 0x60, 0x82, 0xdc,  
													0x23, 0x7d, 0x9f, 0xc1, 0x42, 0x1c, 0xfe, 0xa0,  0xe1, 0xbf, 0x5d, 0x03, 0x80, 0xde, 0x3c, 0x62,  
													0xbe, 0xe0, 0x02, 0x5c, 0xdf, 0x81, 0x63, 0x3d,  0x7c, 0x22, 0xc0, 0x9e, 0x1d, 0x43, 0xa1, 0xff,  
													0x46, 0x18, 0xfa, 0xa4, 0x27, 0x79, 0x9b, 0xc5,  0x84, 0xda, 0x38, 0x66, 0xe5, 0xbb, 0x59, 0x07,  
													0xdb, 0x85, 0x67, 0x39, 0xba, 0xe4, 0x06, 0x58,  0x19, 0x47, 0xa5, 0xfb, 0x78, 0x26, 0xc4, 0x9a,  
													0x65, 0x3b, 0xd9, 0x87, 0x04, 0x5a, 0xb8, 0xe6,  0xa7, 0xf9, 0x1b, 0x45, 0xc6, 0x98, 0x7a, 0x24,  
													0xf8, 0xa6, 0x44, 0x1a, 0x99, 0xc7, 0x25, 0x7b,  0x3a, 0x64, 0x86, 0xd8, 0x5b, 0x05, 0xe7, 0xb9,  
													0x8c, 0xd2, 0x30, 0x6e, 0xed, 0xb3, 0x51, 0x0f,  0x4e, 0x10, 0xf2, 0xac, 0x2f, 0x71, 0x93, 0xcd,  
													0x11, 0x4f, 0xad, 0xf3, 0x70, 0x2e, 0xcc, 0x92,  0xd3, 0x8d, 0x6f, 0x31, 0xb2, 0xec, 0x0e, 0x50,  
													0xaf, 0xf1, 0x13, 0x4d, 0xce, 0x90, 0x72, 0x2c,  0x6d, 0x33, 0xd1, 0x8f, 0x0c, 0x52, 0xb0, 0xee,  
													0x32, 0x6c, 0x8e, 0xd0, 0x53, 0x0d, 0xef, 0xb1,  0xf0, 0xae, 0x4c, 0x12, 0x91, 0xcf, 0x2d, 0x73,  
													0xca, 0x94, 0x76, 0x28, 0xab, 0xf5, 0x17, 0x49,  0x08, 0x56, 0xb4, 0xea, 0x69, 0x37, 0xd5, 0x8b,  
													0x57, 0x09, 0xeb, 0xb5, 0x36, 0x68, 0x8a, 0xd4,  0x95, 0xcb, 0x29, 0x77, 0xf4, 0xaa, 0x48, 0x16,  
													0xe9, 0xb7, 0x55, 0x0b, 0x88, 0xd6, 0x34, 0x6a,  0x2b, 0x75, 0x97, 0xc9, 0x4a, 0x14, 0xf6, 0xa8,  
													0x74, 0x2a, 0xc8, 0x96, 0x15, 0x4b, 0xa9, 0xf7,  0xb6, 0xe8, 0x0a, 0x54, 0xd7, 0x89, 0x6b, 0x35  };
		
		int CRC_8 = 0;
		int i, j;
		
		for (i = Start_Post; i < Start_Post + bye_Lenght; i++) {			//对帧头的7E不进行CRC计算
			j = Data_in_buff[i];
			if (j < 0) {
				j = 256 + j;
			}
			CRC_8 = CRC_Table_All[CRC_8 ^ j];
		}

		return CRC_8;
	}


	
	
}
