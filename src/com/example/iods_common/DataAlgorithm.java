package com.example.iods_common;

//蓝牙串口通信，数据转换类
public class DataAlgorithm {
	
	public static String byteToHexString(byte[] bArray, int b_Len){
		StringBuffer sb = new StringBuffer(b_Len);
		String sTemp;
		for (int i = 0; i < b_Len; i++){
			sTemp = Integer.toHexString(0xFF & bArray[i]);
		if (sTemp.length() < 2)
			sb.append(0);
			sb.append(sTemp.toUpperCase());
		}
		return sb.toString();
	}
    
    public static byte[] hexStringToBytes(String hexString) {
    	if (hexString == null || hexString.equals("")) {
    		return null;
    	}
	    
    	if (hexString.length() < 2) {
    		hexString = "0" + hexString;
	    }
    	
    	hexString = hexString.toUpperCase();
    	int length = hexString.length() / 2;
    	char[] hexChars = hexString.toCharArray();
    	byte[] d = new byte[length];
    	for (int i = 0; i < length; i++) {
    		int pos = i * 2;
    		d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
    	}
    	return d;
    }
    
    public static byte hexStringToByte(String hexString) {
    	if (hexString == null || hexString.equals("")) {
    		return 0;
    	}
    	hexString = hexString.toUpperCase();
    	char[] hexChars = hexString.toCharArray();
    	return (byte) (charToByte(hexChars[0]) << 4 | charToByte(hexChars[1]));
    }
    private static byte charToByte(char c) {
    	return (byte) "0123456789ABCDEF".indexOf(c);
    }
	
   
    
    /** 
     * HEX字符串前补0，主要用于长度位数不足。 
     *  
     * @param str 
     *            String 需要补充长度的十六进制字符串 
     * @param maxLength 
     *            int 补充后十六进制字符串的长度 
     * @return 补充结果 
     */  
    static public String patchHexString(String str, int maxLength) {  
        String temp = "";  
        for (int i = 0; i < maxLength - str.length(); i++) {  
            temp = "0" + temp;  
        }  
        str = (temp + str).substring(0, maxLength);  
        return str;  
    }  

    
    /**
     * 十进制整数转换成两位长度的十六进制字符串
     * @param int, 十进制整数
     * @return String, 两位长度的十六进制数值形式的字符串
     */
    public static String int2Hex(int n) {
    	if(n > 255) return null;
    	String hexString = Integer.toHexString(n & 0xFF);
		if (hexString.length() == 1) {
    		hexString = '0' + hexString;
    	}
		hexString = hexString.toUpperCase();
        return hexString;
    }
    
    /** 
     * 将十进制转换为指定长度的十六进制字符串 
     *  
     * @param algorism 
     *            int 十进制数字 
     * @param maxLength 
     *            int 转换后的十六进制字符串长度 
     * @return String 转换后的十六进制字符串 
     */  
    public static String algorismToHEXString(int algorism, int maxLength) {  
        String result = "";  
        result = Integer.toHexString(algorism);  
  
        if (result.length() % 2 == 1) {  
            result = "0" + result;  
        }  
        return patchHexString(result.toUpperCase(), maxLength);  
    }  
    
    
    
    
    /**
     * 字符串转换成十六进制字符串
     * @param str, String 我们看到的要转换成十六进制的字符串
     * @return String, 十六进制数值形式的字符串
     */
    public static String str2HexStr(String str) {
        char[] digital = "0123456789ABCDEF".toCharArray();
        StringBuffer sb = new StringBuffer("");
        byte[] bs = str.getBytes();
        int bit;
        for (int i = 0; i < bs.length; i++) {
        	bit = (bs[i] & 0x0f0) >> 4;
            sb.append(digital[bit]);
            bit = bs[i] & 0x0f;
            sb.append(digital[bit]);
        }
        return sb.toString();
    }
	
	
	/**
    * 十六进制字符串转十进制数值
    * 
    * @param hex 十六进制字符串
    * @return 十进制数值
    */
   public static int hexStringToAlgorism(String hex) {
       hex = hex.toUpperCase();
       int max = hex.length();
       int result = 0;
       for (int i = max; i > 0; i--) {
           char c = hex.charAt(i - 1);
           int algorism = 0;
           if (c >= '0' && c <= '9') {
               algorism = c - '0';
           } else {
               algorism = c - 55;
           }
           result += Math.pow(16, max - i) * algorism;
       }
       return result;
   }
	
    /**
     * 字符串转化为十六进制字节数组    
     * @param str, String
     * @return byte[]
     */     
    public  static byte[] convert2HexArray(String str) {
   	 	int len = str.length() / 2;
        char[] chars = str.toCharArray();
        String[] hexes = new String[len];
        byte[] bytes = new byte[len];
        for (int i = 0, j = 0; j < len; i = i + 2, j++){            	 
       	 hexes[j] =""+chars[i]+chars[i+1];
            bytes[j] = (byte)Integer.parseInt(hexes[j],16);
        }
        return bytes;
     }    
    
    
     /**
     * 字节数组转换为十六进制的字符串
     * @param bytes, byte[]
     * @return String, 十六进制的字符串
     */    
    public static String byte2Hex(byte[] b) { 
        String hs = "";
        String tmp = "";
        for (int n = 0; n < b.length; n++) {
       	 tmp = (java.lang.Integer.toHexString(b[n] & 0XFF));//整数转成十六进制表示
            if (tmp.length() == 1) {
                hs = hs + "0" + tmp;
            } else {
                hs = hs + tmp;
            }
        }
        tmp = null;
        return hs.toUpperCase(); //转成大写
 
    }
    	
     /**
      * 字节数组转换为十六进制的字符串
      * @param bytes, byte[]
      * @return String, 十六进制的字符串
      */
    public static String byte2HexString(byte[] bytes) {    	
    	String result = "";
        for (byte b: bytes) {
        	// 将每个字节与0xFF进行与运算，然后转化为10进制，然后借助于Integer再转化为16进制  
        	//java.lang.Integer.toHexString(int i);//此方法返回的字符串表示的无符号整数参数，所表示的值以十六进制        	
        	String hexString = Integer.toHexString(b & 0xFF);
        	if (hexString.length() == 1) {
        		hexString = '0' + hexString;
        	}
        	result += hexString.toUpperCase();
        }
        return result;            	
    } 
	
 // 转化十六进制编码为字符串 
 	public static String toStringHex(String s) { 
 		byte[] baKeyword = new byte[s.length()/2]; 
 		for(int i = 0; i < baKeyword.length; i++) { 
 			try { 
 				baKeyword[i] = (byte)(0xff & Integer.parseInt(s.substring(i*2, i*2+2),16)); 
 			} catch(Exception e) { 
 				e.printStackTrace(); 
 			} 
 		} 
 		try { 
 			s = new String(baKeyword, "utf-8");//UTF-16le:Not 
 		} catch (Exception e1) { 
 			e1.printStackTrace(); 
 		} 
 		return s; 
 	} 
 	
	/**
	 * 字符串长度不够左补0
	 * @param str
	 * @param strLength
	 * @return
	 */
	 public static String addtoNum_Left(String str, int strLength) {
	     int strLen = str.length();
	     StringBuffer sb = null;
	     while (strLen < strLength) {
	           sb = new StringBuffer();
	           sb.append("0").append(str);// 左(前)补0
	           str = sb.toString();
	           strLen = str.length();
	     }
	     return str;
	 }
	 
	//IP地址转为相应2位16进制字符串数据
		public static String iPto16sString(String IP) {
			String IPString = "";
			int i ;
				for (int j = 1; j < 5; j++) {
				    if(j==4)
					{
				    	 int n = Integer.parseInt(IP);
						    String v = Integer.toHexString(n);
						    if(v.length()==1)
							  {
							  	v = "0"+v;
							  }
						    IPString =IPString + v;		    
					  break;
					}
				  else{
				    i=IP.indexOf(".");
				    String g = IP.substring(0, i);
				    int n = Integer.parseInt(g);
				    String v = Integer.toHexString(n);
				    if(v.length()==1)
					  {
					  	v = "0"+v;
					  }
				    IPString =IPString + v;		    
				    IP = IP.substring(i+1);	
				  }
				}
			return IPString;		
		}

 
}
