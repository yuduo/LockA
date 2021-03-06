package com.example.iods_lock_app;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;









public class User_Help_Activity extends Activity {
	//终端管理页面
	
	private Context context = User_Help_Activity.this;//定义Context对象
	TextView Help_Text;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_help);
        setTitle("亨通光电");

        initi();
        
    }
    
    public void initi(){
    	
    	Spinner spinner1 = (Spinner)findViewById(R.id.spinner1);

        Help_Text = (TextView)findViewById(R.id.textView3);

        List<String> list1 = new ArrayList<String>();
		list1.add(0, "概述");
		list1.add(1, "用户登录");
		list1.add(2, "门禁控制");
		list1.add(3, "直联开锁");
		list1.add(4, "门禁控制的远程开锁");
		list1.add(5, "求助开锁");
		list1.add(6, "锁具查询");
		list1.add(7, "终端管理");
		list1.add(8, "工程施工");
		list1.add(9, "用户管理");
		list1.add(10, "工程审查");
		list1.add(11, "群组管理的远程开锁");
		list1.add(12, "新建施工");
		list1.add(13, "维护施工");
		list1.add(14, "移机施工");
		list1.add(15, "拆除施工");
		list1.add(16, "回单与退单");
		list1.add(17, "软件版本");
		
		final ArrayAdapter<String> sAdapter1 = new ArrayAdapter<String>(context, R.layout.custom_spiner_text_item, list1);
		sAdapter1.setDropDownViewResource(R.layout.custom_spinner_dropdown_item); //定义每一个item的样式
		spinner1.setAdapter(sAdapter1);
		
		sAdapter1.notifyDataSetChanged();
		spinner1.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				
				String Str_Title = sAdapter1.getItem(position);
				String Str_Help = "";
				
				switch(Str_Title){
	  			case "概述":
	  				Str_Help = "        亨通智能门禁终端管理具备门禁钥匙以及相关管理和工程施工功能，它是光交门禁的开锁\"钥匙\"，也是工程施工的数据采集和记录器，同时给终端管理用户提供一定的" +
	  						"网络管理权限的延伸。\r\n        其\"门禁控制\"模块提供无钥匙门禁开锁能力。\r\n        \"工程施工\"模块" +
	  						"提供门禁锁设备新建、维护、移机和拆除的施工功能。\r\n        \"群组管理\"模块给终端管理用户提供用户管理、工程审查和远程开锁能力。\r\n        \"终端管理\"模块" +
	  						"则提供当前用户的密码修改、当前终端设备的软件使用日志查询、网络配置、数据清理以及使用帮助等功能。\r\n\r\n\r\n    注：        由于软件需调用百度地图功能，为便于导航和地图显示" +
	  						"，请安装百度地图软件。";
	  				break;
	  				
	  			case "用户登录":
	  				Str_Help = "        使用本软件首先需要进行用户登录，非法用户仅只能使用\"网络配置\"功能和\"使用帮助\"功能。\r\n        用户依其权限分为\"终端操作用户\"和\"终端管理用户\"两类，" +
	  						"终端管理用户必须由网管添加并授权，终端操作用户则可以由网管和终端管理用户来添加和授权。\r\n        在智能终端无法与网管系统通信的情况下，仅此前正常登录过软件的用户可以进行网络" +
	  						"脱管登录，并使用部分无需网络互连的功能，其它用户均只能进入\"网络配置\"功能和\"使用帮助\"功能模块。\r\n        软件使用前，应首先正确设置网络配置，以便智能终端能够正常与网管进行通信。";
	  				break;
				
	  			case "门禁控制":
	  				Str_Help = "       门禁控制分文字界面和地图界面两种，且有直联开锁和远程开锁两种方式。所谓直联开锁是智能终端通过蓝牙通道直接控制锁具开锁，而远程开锁则是智能终端经由网管系统控制锁具进行开锁开锁。" +
	  						"\r\n\r\n\r\n    注意：        由于远程开锁的信号传输通道关卡多，且带宽较窄，所以速度较慢，且可能会因网络原因而失败。";
	  				break;
	  				
	  			case "直联开锁":
	  				Str_Help = "        直联开锁时，需首先按压锁具上的触发按键，使其激活（锁具上的指示灯闪亮一次），再点击软件中的\"直联开锁\"开关，弹出附近的蓝牙设备列表，选取锁具的蓝牙设备后，终端软件会自动" +
	  						"会判断用户权限，若权限相符，则向锁具发出开锁指令，锁具指示灯长亮期间，按压锁柄，即可开锁。";
	  				break;
	  				
	  			case "门禁控制的远程开锁":
	  				Str_Help = "        远程开锁功能在\"门禁控制\"与\"群组管理\"中都具备，但作用不同。\"门禁控制\"中的远程开锁是用户直接对选中的锁具进行开锁，而\"群组管理\"中的远程开锁则仅用于帮助没有开锁权限的用户对" +
	  						"所提交的开锁求助需求进行远程开锁。\r\n        \"门禁控制\"的远程开锁分为文字界面和地图界面式两种，它们均将附近的锁具搜索并呈现出来，搜索范围可在\"网络配置\"中设定。\r\n        文字界面中，" +
	  						"会将附近的锁具名称呈现在锁名下拉框中。选出待开锁的锁具名，激活锁体，点击\"远程开锁\"开关即可依权限限定发送开锁指令。\r\n        地图界面仅只支持远程开锁，它会将附近的锁具以图标的方式显示在地图上，" +
	  						"同时也显示智能终端所处的地理位置。点击待开锁的锁具图标，即可弹出此锁的名称，点击此名称会弹出是否打开此锁具的对话框，确认后即可依权限限定发送开锁指令。";
	  				break;
	  				
	  			case "求助开锁":
	  				Str_Help = "        若用户没有锁具的开启权限，可以选中需要开锁的锁具名后，点击\"求助开锁\"开关，向网管或自己的管理者发出远程开锁求助信号，网管或终端管理用户下达远程开锁指令前，需激活待开锁具，以便锁具能够正常接收开锁指令。";
	  				break;
	  				
	  			case "锁具查询":
	  				Str_Help = "        锁具查询用于查询已知锁具名称或资源编码部分字段的锁具位置，以便选择要开锁的锁具，并导航到锁具的所在位置。";
	  				break;
				
	  			case "终端管理":
	  				Str_Help = "       终端管理用于管理安装软件的终端设备，除\"使用帮助\"和前面提到过的\"网络配置\"功能外，还具有\"日志查询\"功能、\"数据清理\"功能和\"密码更新\"功能。\r\n        其中，\"日志查询\"功能" +
	  						"用于查询哪个用户在什么时间使用本终端软件对什么对象进行过哪些操作。\r\n        \"网络配置\"功能用于配置网管服务器的地址和端口，以便本软件与网管进行通信；并可设定在\"门禁控制\"中搜索附近锁具的搜索范围大小。" +
	  						"\r\n        \"数据清理\"功能用于清理软件中保存的早期数据，以节省内存。\r\n        \"密码更新\"功能则用于更新当前登录用户的密码和手机号码。";
	  				break;
	  				
	  			case "工程施工":
	  				Str_Help = "       工程施工的首页为工单列表。列表中会显示出未完成工单和当日已完成工单，以及各工单的工单号、工单类型、完工时限，以及施工状态。通过点击各工单栏，可以跳转到该工单的施工导引界面。\r\n        工程施工首页有" +
	  						"两个按键，\"下载全部工单\"用于下载工单列表数据，\"设备注册查询\"按键用于查看通过蓝牙连接的设备是否已完成在移动物联网平台上的注册，以分析设备是否能够通过移动网络直接与网管通信。\r\n\r\n\r\n    注意：        工程" +
	  						"施工过程中，请注意核查清楚设备是否是待施工设备，可通过开锁过程和软件提示的锁具设备名等来检查。工单提交后，也请进行几次开关箱门和锁具的处理，以检查施工设备是否正常。\r\n\r\n    另：        锁具重新加电后，会有较长时间的" +
	  						"平台注册过程，其间锁具的LED指示灯会长亮，此时不能进行施工或开锁操作，需待指示灯熄灭后才能进行锁具信息的采集或开锁等操作。";
	  				break;
	  				
	  			case "用户管理":
	  				Str_Help = "       用户管理页面会列出终端管理用户所在群组的所有用户，以及其管理权限和真实姓名。通过点击各用户栏，可以修改该用户的密码、真实姓名，以及电话号码等信息。\r\n        用户管理页面下方有一个\"添加新用户\"按键，" +
	  						"点击它可以在当前用户群组中添加一个新的终端操作用户。\r\n\r\n\r\n    注意：        若群组组长需要授权，欲任命或撤除本群组的副组长，需要提请网络管理员在网管上进行权限更改。\r\n\r\n    另，因网络时延和服务器处理的延时，" +
	  						"用户的更新需要重新进入一次才能反映出来。";
	  				break;
	  					  				
	  			case "工程审查":
	  				Str_Help = "       新建设备工单和移机工单施工完成后会要求进行审查，在智能终端上，工程审查仅检查各工单所施工设备的安装位置是否正确，不对设备名称和资源编码进行编辑修改。可通过\"地图显示\"按键进行检查，并通过\"审查通过\"或\"审查驳回\"按键提交审查结论。";
	  				break;
	  				
	  			case "群组管理的远程开锁":
	  				Str_Help = "       \"群组管理\"中的远程开锁则用于终端管理用户在自己的权限范围内帮助没有开锁权限的下属对所提交的开锁求助需求进行远程开锁。\r\n        软件会自动把求助开锁的锁具附近的锁具设备名均搜索出来，列表在锁具下拉框中，" +
	  						"且将求助开锁的锁具名列在首位。以地图方式显示时，则会显示求助开锁位置附近的所有锁具，以便于终端管理用户选择进行远程开锁控制。\r\n        远程开锁前，须让求助开锁人员激活待开的锁具，否则锁具处于休眠状态，无法接收到开锁指令。";
	  				break;
	  					  				
	  			case "新建施工":
	  				Str_Help = "       新建设备工单的施工需在完成新的锁具或设备箱体安装后，待锁具进入正常待机状态下，激活锁具（注意，须仅只激活此一个锁具，以免出现混淆），点击\"采集锁信息\"，此时，智能终端会搜索附近的蓝牙设备。选择锁具蓝牙设备后，会自动进行设备的平台数据登录。" +
	  						"完成数据登录后，软件会转入设备地理位置定位的地图界面。确定设备定位点后，点击地图中的设备图标，会弹出图标\"锁安装位置定位\"。点击此图标，会要求输入此新锁设备名。完成设备名输入，并确认后，就完成当前锁具设备的新建操作。\r\n        此后，可以新建安装" +
	  						"下一台设备，待所有新建设备施工均完成后，即可进行回单操作，以上传施工数据。\r\n        锁具施工完成后，请进行几次门锁的开关操作，以便清除锁具残余信息，并检查锁具工作是否正常。";
	  				break;
	  				
	  			case "维护施工":
	  				Str_Help = "       若锁具出现故障，需通过维护工单进行锁具更换。\r\n        若锁具未完全死机，可通过点击\"采集锁信息\"按键，选择\"开锁\"功能，以确定是否是待施工锁具。若锁具没有响应，则请点击待施工锁具的锁具名，通过选择\"无法读取设备信息\"选项，以进入" +
	  						"新换锁具的信息采集程序。\r\n        完成新锁具的更换安装后，请激活新锁（注意，须仅只激活此一个锁具，以免出现混淆），然后点击红色\"采集新锁信息\"按键，以采集新锁信息。新锁完成设备的平台数据登录后，就完成当前锁具的维护，可进行下一个锁具的维护，直至完成" +
	  						"该工单的所有维护任务后，即可回单。\r\n        维护施工完成后，请进行几次开关操作，以便清除锁具残余信息，并检查新锁工作是否正常。\r\n\r\n\r\n    注意：        设备脱管（网管上的图标为灰色）除设备故障原因外，也有可能是网络原因，" +
	  						"可通过点击\"工程施工\"页面的\"设备注册查询\"按键，来查询设备联网情况，若显示\"设备尚未在NB平台上注册成功\"，则不是设备故障造成的设备脱管。";
	  				break;
	  				
	  			case "移机施工":
	  				Str_Help = "       移机施工前，需激活锁具，通过点击\"采集锁信息\"按键，选择\"开锁\"选项，以验证是否是待移机设备。完成移机后，请再次采集锁信息，并选择\"移机\"选项，此时软件会转入地理定位页面。定位完成后，请点击地图中设备图标，点选图标上的\"锁安装位置定位\"框，" +
	  						"此时会要求输入移机后的锁设备名，完成设备名输入，并确认后，就完成当前锁具设备的移机操作。\r\n        待所有待移机设备均施工完成后，即可回单。";
	  				break;
	  				
	  			case "拆除施工":
	  				Str_Help = "       设备拆除时，需先激活锁具，通过点击\"采集锁信息\"按键，选择\"开锁\"选项，以验证是否是待拆除设备，确定后点取\"拆除\"选项即可完成拆除。工单回单后，网管上的相关设备信息也将被删除。\r\n\r\n    另，设备拆除后，可对其实施新建施工操作。";
	  				break;
	  				
	  			case "回单与退单":
	  				Str_Help = "       回单是将终端软件采集到的施工数据回传到网络管理系统的过程。只有完成了回单操作，一个施工工单才算全部完成。否则网络管理系统中将无法了解到施工情况。当施工任务无法全部执行时，可进行强制回单。另外，由于新建施工工单难以规范新建设备的数量，所以新建施工只有" +
	  						"强制回单选项。\r\n\r\n        退单是在施工人员因各种原因无法执行施工操作时的选项，退单仅只在施工任务完全没有实施的情况下才有效。";
	  				break;
	  				
	  			case "软件版本":	//	TODO
	  				Str_Help = "BT_NB_V20180731.B01";
	  				break;
				}
					
				Help_Text.setMovementMethod(ScrollingMovementMethod.getInstance());		//设置滚动
				Help_Text.setText(Str_Help);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {				
			}
		});
	}
    
    
	protected void onDestroy() {
		super.onDestroy();

	}
}
