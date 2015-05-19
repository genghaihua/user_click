package cn.clickwise.liqi.mapreduce.app.swa_analysis;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;


public class PageCrawlMulThread {

	public void getTitleBat(String input_file,String output_file) throws Exception
	{
		  FileReader fr=new FileReader(new File(input_file));
		  BufferedReader br=new BufferedReader(fr);
		  String line="";
		//  FileWriter fw=new FileWriter(new File(output_file));
		//  PrintWriter pw=new PrintWriter(fw);

		  String title="";
		  while((line=br.readLine())!=null)
		  {
			//  System.out.println("line:"+line);
			  line=line.trim();
              Runnable r =  new  PageCrawlThread(line);
              Thread t =  new  Thread(r);
              t.start();
              Thread.sleep(20);
            //  System.out.println(line+" "+title);
	        //  pw.println(line+"\001"+title);
		  }
		  
		  //fw.close();
		 // pw.close();
		  br.close();
		  fr.close();
	}
	
	public static void main(String[] args) throws Exception {
		PageCrawlMulThread pc=new PageCrawlMulThread();
		String input_file="input/hosts/20140225/hosts_test.txt";
		String output_file="temp/hosts/20140225/hosts_output.txt";
		pc.getTitleBat(input_file, output_file);
	}
}


 class  PageCrawlThread  implements  Runnable {
	    public String[] proxy_hosts = { 
				"122.72.111.98", "122.72.76.132",
				 "122.72.11.129", "122.72.11.130",
				"122.72.11.131", "122.72.11.132", "122.72.99.2", "122.72.99.3",
				"122.72.99.4", "122.72.99.8" };
		
    String url="";

    public  PageCrawlThread(String url) {
          this.url=url;
    }

    public   void  run() {
            try  {
             String title=getWebPageTitle(url);
             System.out.println(url+" "+title);
            }  catch  (Exception e) {
                    e.printStackTrace();
            }
    }
    
	public String getWebPageTitle(String url) {
		String title = "";

		HttpClient httpclient = new DefaultHttpClient();

		//设置代理
		double ran = Math.random();
		int rani = -1;
		rani = (int) (ran * 10);
		HttpHost proxy = new HttpHost(proxy_hosts[rani], 80, "http");
		httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
				proxy);
		httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,20000);
		//System.out.println("rani:" + rani);	
		
		url = url.trim();
		if ((url == null) || (url.length() < 5)) {
			return "";
		}
		if (url.indexOf("http") < 0) {
			url = "http://" + url;
		}
		String con = "";
		try {
			HttpGet httpget = new HttpGet(url);

			//System.out.println("executing request ==================" + httpget.getURI());
			// 执行get请求.
			HttpResponse response = httpclient.execute(httpget);

			// 获取响应状态
			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == HttpStatus.SC_OK) {
				// 获取响应实体
				HttpEntity entity = response.getEntity();

				
				Header[] allhead = response.getAllHeaders();
				Header one_head = null;
				for (int i = 0; i < allhead.length; i++) {
					one_head = allhead[i];
					//System.out.println(one_head.getName() + " "
					//		+ one_head.getValue());
				}
				if(allhead.length<1)
				{
					return "";
				}
					
				InputStream is = entity.getContent();
				String s = "";

				// InputStreamReader isr=new InputStreamReader(is);
				//BufferedInputStream bis = new BufferedInputStream(is);
				int c = 0;
				int lc = 0;
				byte[] bytes = new byte[102400];
				is.read(bytes);
				String us = new String(bytes);
				//System.out.println("us:"+us);
				
				
				
				Pattern charset_pat=Pattern.compile("(?:(?:charset)|(?:CHARSET))=([^\">]*)");
				String charset="";
				one_head=response.getFirstHeader("Content-Type");
				Matcher charset_mat=charset_pat.matcher(one_head.getValue());
				//System.out.println("head charset:"+one_head.getValue());
				if(charset_mat.find())
				{
					charset=charset_mat.group(1);
				}
				charset=charset.trim();
				
				//System.out.println("charset1:"+charset);
				String ds="";
				if(!(charset.equals("")))
				{
					ds=new String(bytes,charset);
				}
				//System.out.println("ds:"+ds);
				if(charset.equals(""))
				{
					charset_pat=Pattern.compile("<(?:(?:meta)|(?:META)|(?:Meta))[^<>]*?(?:(?:charset)|(?:CHARSET))=([^\">]*)[^<>]*?>");
					charset_mat=charset_pat.matcher(us);
					//System.out.println("us:"+us);
					if(charset_mat.find())
					{
						charset=charset_mat.group(1);
					}
				//	System.out.println("charset2:"+charset);
					ds=new String(bytes,charset);
					
				}
				
				Pattern title_pat=Pattern.compile("<title>([^<>]*?)</title>");
				Matcher title_mat=title_pat.matcher(ds);
				title=title.trim();
				if(title_mat.find())
				{
					title=title_mat.group(1);
				}
				
				
			}

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
      //  if(title.length()>0)
		//System.out.println(url+":"+title);
		return title;
	}
}