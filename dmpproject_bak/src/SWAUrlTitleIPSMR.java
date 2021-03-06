import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;



public class SWAUrlTitleIPSMR {

	private static class PrepareMapper extends Mapper<Object, Text, Text, Text> {

		private Text word = new Text();
		private Text word1 = new Text();

		public void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {

			String[] seg_arr = (value.toString()).split("\t");

			String hourseg="";
            String input_ip="";
            String input_area="";
            String input_cookie="";
            String host="";
            String input_url="";
            String input_title="";
            String title_fenci="";
            String input_ci_ip="";
            String input_ci_ip_area="";
            String input_refer="";
            
			
			
	            
			if (seg_arr != null && seg_arr.length == 11) {
	
                hourseg=seg_arr[0].trim();
                input_ip=seg_arr[1].trim();
                input_area=seg_arr[2].trim();
                input_cookie=seg_arr[3].trim();
                host=seg_arr[4].trim();
                input_url=seg_arr[5].trim();
                input_title=seg_arr[6].trim();
                title_fenci=seg_arr[7].trim();
                input_ci_ip=seg_arr[8].trim();
                input_ci_ip_area=seg_arr[9].trim();
                input_refer=seg_arr[10].trim();
                
				if ((input_url != null) && (!input_url.equals(""))) {
						word.set(input_url);
						word1.set(input_title+"\001"+input_ci_ip);
						context.write(word, word1);										
				}
			}

		}

		public boolean isValidTitle(String title) {
			title = title.trim();
			if(title==null||title.equals(""))
			{
				return false;
			}
			boolean isVal = true;
			String eng_mat = "[a-zA-Z0-9\\.:\\?#=_/&\\-%]*";
			if (Pattern.matches(eng_mat, title)) {
				isVal = false;
			}
			if ((title.indexOf("<") != -1) || (title.indexOf(">") != -1)) {
				isVal = false;
			}

			char first_char = title.charAt(0);
			if (Pattern.matches(eng_mat, first_char+"")) {
				isVal = false;
			}
			if(!isChinese(first_char))
			{
				isVal = false;
			}

			return isVal;

		}

		public  boolean isChinese(char a) {
			int v = (int) a;
			return (v >= 19968 && v <= 171941);
		}

	}

	private static class PrepareReducer extends Reducer<Text, Text, Text, Text> {
		private Text result = new Text();

		protected void reduce(Text key, Iterable<Text> values, Context context)
				throws IOException, InterruptedException {

			Iterator<Text> it = values.iterator();
            String val="";
            String[] seg_arr=null;
            String title="";
            String ci_ip="";
            int title_max_len=0;
            String title_max="";
            Hashtable ci_ip_hash=new Hashtable();
            
			while(it.hasNext()) {
				val=it.next().toString();
				val=val.trim();
				if((val==null)||(val.equals("")))
				{
					continue;
				}
				seg_arr=val.split("\001");
				if(seg_arr.length!=2)
				{
					continue;
				}
				title=seg_arr[0].trim();
				ci_ip=seg_arr[1].trim();
				if(isValidTitle(title))
				{
					if(title.length()>title_max_len)
					{
						title_max_len=title.length();
						title_max=title;
					}
				}
				
				
				context.write(key, it.next());
			}

		}
		
		public boolean isValidTitle(String title) {
			title = title.trim();
			if(title==null||title.equals(""))
			{
				return false;
			}
			boolean isVal = true;
			String eng_mat = "[a-zA-Z0-9\\.:\\?#=_/&\\-%]*";
			if (Pattern.matches(eng_mat, title)) {
				isVal = false;
			}
			if ((title.indexOf("<") != -1) || (title.indexOf(">") != -1)) {
				isVal = false;
			}

			char first_char = title.charAt(0);
			if (Pattern.matches(eng_mat, first_char+"")) {
				isVal = false;
			}
			if(!isChinese(first_char))
			{
				isVal = false;
			}

			return isVal;

		}

		public  boolean isChinese(char a) {
			int v = (int) a;
			return (v >= 19968 && v <= 171941);
		}
		
		
		
	}

	public static void main(String[] args) throws Exception {

		Configuration conf = new Configuration();
		String[] otherArgs = new GenericOptionsParser(conf, args)
				.getRemainingArgs();
		if (otherArgs.length != 3) {
			System.err.println("Usage: SWAUrlTitleIPSMR <day> <input> <output>");
			System.exit(2);
		}
		
		String day = otherArgs[0];
		Job job = new Job(conf, "SWAUrlTitleIPSMR_" + day);
		job.setJarByClass(SWAUrlTitleIPSMR.class);
		job.setMapperClass(PrepareMapper.class);
		job.setReducerClass(PrepareReducer.class);
		job.setNumReduceTasks(1);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		FileInputFormat.addInputPath(job, new Path(otherArgs[1]));
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[2]));
		System.exit(job.waitForCompletion(true) ? 0 : 1);

	}
	

	
}
