package pro.qsub.bd.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.io.FileUtils;

/**
 * @desc Http网络请求
 * @author 秋枫
 * 
 * 
 */
public class HttpRequest {
	// 网址
	private String url;
	// 类型[POST,GET]
	private String mode;
	// 协议头
	private HashMap<String, String> headers;
	// cookie
	private String cookies;
	// 提交数据
	private HashMap<String, String> submitdata;

	// 返回数据
	private String data;
//    Post(String url, String param, HashMap<String, String> headers, String cookie) {

	public HttpRequest(String url, String mode) throws Exception {
		/**
		 * 只有url和访问类型
		 */
		this.url = url;
		this.mode = mode;
		if (mode.equals("POST")) {
			this.Post(this.url, null, null, null);
		} else if (mode.equals("GET")) {
			this.sendGet(this.url, null, null, null);
		}

	}

	public HttpRequest(String url, String mode, HashMap<String, String> submitdata) throws Exception{
		/**
		 * url 访问类型 提交数据
		 */
		this.url = url;
		this.mode = mode;
		this.submitdata = submitdata;
		if (mode.equals("POST")) {
			this.Post(this.url, this.Processingdata(this.submitdata), null, null);
		} else if (mode.equals("GET")) {
			this.sendGet(this.url, this.Processingdata(this.submitdata), null, null);
		}
	}

	public HttpRequest(String url, String mode, HashMap<String, String> headers, String submitdata) throws Exception{

		/**
		 * url 提交类型 协议头 提交数据
		 *
		 */
		this.url = url;
		this.mode = mode;
		this.headers = headers;
		if (mode.equals("POST")) {
			this.Post(this.url, submitdata, this.headers, null);

		} else if (mode.equals("GET")) {
			this.sendGet(this.url, submitdata, this.headers, null);
		}
	}

	public HttpRequest(String url, String mode, HashMap<String, String> headers, HashMap<String, String> submitdata) throws Exception{

		/**
		 * url 提交类型 协议头 提交数据
		 *
		 */
		this.url = url;
		this.mode = mode;
		this.headers = headers;
		this.submitdata = submitdata;
		if (mode.equals("POST")) {
			this.Post(this.url, this.Processingdata(this.submitdata), this.headers, null);

		} else if (mode.equals("GET")) {
			this.sendGet(this.url, this.Processingdata(this.submitdata), this.headers, null);
		}
	}

	public HttpRequest(String url, String mode, String cookies, HashMap<String, String> submitdata) throws Exception{
		/**
		 * url 访问类型 cookie 提交数据
		 */
		this.url = url;
		this.mode = mode;
		this.cookies = cookies;
		this.submitdata = submitdata;
		if (mode.equals("POST")) {
			this.Post(this.url, this.Processingdata(this.submitdata), null, this.cookies);

		} else if (mode.equals("GET")) {
			this.sendGet(this.url, this.Processingdata(this.submitdata), null, this.cookies);
		}
	}

	public HttpRequest(String url, String mode, HashMap<String, String> headers, String cookies,
			HashMap<String, String> submitdata) throws Exception{

		/**
		 * url 访问类型 协议头 cookie 提交数据
		 */
		this.url = url;
		this.mode = mode;
		this.headers = headers;
		this.cookies = cookies;
		this.submitdata = submitdata;
		if (mode.equals("POST")) {
			this.Post(this.url, this.Processingdata(this.submitdata), this.headers, this.cookies);

		} else if (mode.equals("GET")) {
			this.sendGet(this.url, this.Processingdata(this.submitdata), this.headers, this.cookies);
		}

	}

	private String Processingdata(HashMap<String, String> submitdata) {

		String retu = "";

		for (Map.Entry<String, String> entry : submitdata.entrySet()) {
			retu = retu + "&" + entry.getKey() + "=" + entry.getValue();

		}
		return retu;
	}

	public String getCookies() {
		return this.cookies;
	}

	/**
	 * 向指定URL发送GET方式的请求
	 *
	 * @param url   发送请求的URL
	 * @param param 请求参数
	 **/
	private void sendGet(String url, String param, HashMap<String, String> headers, String cookie) {
		StringBuilder result = new StringBuilder();
		String urlName = url + "?" + param;

		try {
			URL realUrl = new URL(urlName);

			URLConnection conn = realUrl.openConnection();
			// 设置通用的请求属性
			if (headers != null) {
				for (Map.Entry<String, String> entry : headers.entrySet()) {
					conn.addRequestProperty(entry.getKey(), entry.getValue());
				}
			}
			if (cookie != null) {
				conn.setRequestProperty("Cookie", cookie);
			}

			// 建立实际的连接
			conn.connect();
			// 获取所有的响应头字段
			Map<String, List<String>> map = conn.getHeaderFields();
			// 遍历所有的响应头字段
//            for (String key : map.keySet()) {
//                System.out.println(key + "-->" + map.get(key));
//            }
			// 定义 BufferedReader输入流来读取URL的响应
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			// 取cookie
			StringBuilder sessionId = new StringBuilder();
			String key = null;
			for (int i = 1; (key = conn.getHeaderFieldKey(i)) != null; i++) {
				if (key.equalsIgnoreCase("set-cookie")) {
					sessionId.append(conn.getHeaderField(i)).append(";");
				}
			}
			this.cookies = sessionId.toString();

			String line;
			while ((line = in.readLine()) != null) {
				result.append(line);
			}

		} catch (Exception e) {
			System.out.println("发送GET请求出现异常" + e);
			e.printStackTrace();
		}
		this.data = result.toString();
	}

	/**
	 * 向指定URL发送POST方式的请求
	 *
	 * @param url   发送请求的URL
	 * @param param 请求参数
	 */
	private void Post(String url, String param, HashMap<String, String> headers, String cookie) throws Exception {

		/**
		 * 参数HashMap<String,String> headers说明： HashMap<String,String> headers =new
		 * HashMap<String, String>(); headers.put("键", "键值");
		 */

		StringBuilder result = new StringBuilder();
		String line;

			URL realUrl = new URL(url);
			// 打开和URL之间的连接
			URLConnection conn = realUrl.openConnection();
			// 设置通用的请求属性
			if (headers != null) {
				for (Map.Entry<String, String> entry : headers.entrySet()) {
					String k = entry.getKey();
					String v = entry.getValue();
					conn.addRequestProperty(k, v);
				}
			}

			if (cookie != null) {
				conn.setRequestProperty("Cookie", cookie);
			}

			// 发送POST请求必须设置如下两行
//            conn.setDoOutput(true);
			conn.setDoOutput(true);
			conn.setDoInput(true);
			// 获取URLConnection对象对应的输出流
			PrintWriter out = new PrintWriter(conn.getOutputStream());
			// 发送请求参数
			out.print(param);
			// flush输出流的缓冲
			out.flush();
			// 定义 BufferedReader输入流来读取URL的响应
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
			// 取cookie
			String sessionId = "";
			String cookieVal = "";
			String key = null;
			for (int i = 1; (key = conn.getHeaderFieldKey(i)) != null; i++) {
				if (key.equalsIgnoreCase("set-cookie")) {
					cookieVal = conn.getHeaderField(i);
					sessionId = sessionId + cookieVal + ";";
				}
			}
			this.cookies = sessionId;

			while ((line = in.readLine()) != null) {
				result.append("\n").append(line);
			}


		this.data = result.toString();
//        System.out.println(this.data);
	}

	public String getData() {
		return this.data;
	}


	public static String getRandomString(int length) {
		String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		Random random = new Random();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < length; i++) {
			int number = random.nextInt(62);
			sb.append(str.charAt(number));
		}
		return sb.toString();
	}
}
