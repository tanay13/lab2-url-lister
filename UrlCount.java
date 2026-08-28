import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import java.util.regex.*;

public class UrlCount {

  public static class TokenizerMapper
       extends Mapper<Object, Text, Text, IntWritable>{

    private final static IntWritable one = new IntWritable(1);
    private Text htmlComps = new Text();

    public void map(Object key, Text value, Context context
                    ) throws IOException, InterruptedException {
      Pattern pattern = Pattern.compile("href=\"(?!#)([^\"]*)\"");
      Matcher matcher = pattern.matcher(value.toString());
        
      while (matcher.find()) {
        context.write(new Text(matcher.group(1)), one);
      }
    }
  }

    public static class IntSumCombiner
       extends Reducer<Text,IntWritable,Text,IntWritable> { 
    private IntWritable result = new IntWritable();

    public void reduce(Text key, Iterable<IntWritable> values,
                       Context context
                       ) throws IOException, InterruptedException {
      int sum = 0;
      for (IntWritable val : values) {
        sum += val.get();
      }
      result.set(sum);
      context.write(key, result);
    }
  }

    
  public static class IntSumReducer
       extends Reducer<Text,IntWritable,Text,IntWritable> { 
    private IntWritable result = new IntWritable();

    public void reduce(Text key, Iterable<IntWritable> values,
                       Context context
                       ) throws IOException, InterruptedException {
      int sum = 0;
      for (IntWritable val : values) {
        sum += val.get();
      }
      result.set(sum);
        if ( sum > 5) {
      context.write(key, result);
        }
    }
  }

  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    Job job = Job.getInstance(conf, "Url count");
    job.setJarByClass(UrlCount.class);
    job.setMapperClass(TokenizerMapper.class);
      
    job.setCombinerClass(IntSumCombiner.class);
      
    job.setReducerClass(IntSumReducer.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(IntWritable.class);
    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}
