import java.io.*;
import java.util.HashMap;
 
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import net.paoding.analysis.analyzer.PaodingAnalyzer;
 
public class paodingtest{
 
/**
     * @param args
 */
public static void main(String[] args) {
// TODOAuto-generated method stub
     Analyzer analyzer=new PaodingAnalyzer();
     //String docText=null;
     //File file=new File("text/test2");
     //docText=readText(file);
     //System.out.println(docText);
     
     String line = "钢琴曲欣赏100首	一个月的宝宝眼睫毛那么是黄色	宝宝右眼有眼屎	小儿抽搐怎么办	剖腹产后刀口上有线头	属羊和属鸡的配吗	为什么一个月婴儿眼睫毛还没长出来	玛丽艳敏感修护套装	芦花鸡疾病知询	剖腹产后一个月肚子腰疼	剖腹产后肚子腰疼	先天性心脏病是什么原因造成的	牛奶冲后那么有泡泡	模具厂取名	小儿梅毒图片大全	一个多月宝宝吃多少毫升奶粉	小儿消化不良怎么办	肠胃不好怎么调理	剖腹产后一个月肚子疼	宝宝哭时脸红	国内喜宝奶粉怎么样	剖腹产多久可以同房	一至二个月宝宝能吃米糊吗	宝宝消化不良要换奶粉	小儿湿疹的治疗方法	宝宝手心脚心发热是怎么回事	喜宝	玛丽艳修复系列产品	一至二月宝宝消化不良应吃那种奶粉	射手座	hipp什么意思中文翻译	属蛇配属猪	下雨的声音	完美玛丽艳产品价格表	脂肪一流	芦花鸡走路一走一歪怎会事	一个多月的宝宝老吐奶怎么回事	一个多月宝宝鼻梁上有横纹	乳房彩超检查多少钱	一个多月宝宝大哭时脑血管	乳房彩超	剖腹产刀口有硬块	一个多月宝宝喘气困难	儿歌大全	一个多月宝宝老吐唾液	水的声音mp3	属鸡白羊座女	剖腹产后肚子疼是怎么回事	美图	属猪和属狗相配吗	一个多月宝宝脚有裂痕	德国喜宝奶粉	鱼肝油的功效与作用	藏力康胶囊多少钱一盒	一个多月宝宝头发黄怎会事	风的声音	剖腹后腰疼是怎么回事	国内喜宝奶粉	属羊属鸡	内膜遗位症	一个多月宝宝一哭血管发紫怎会事	灵芝菌丝体胶囊	hipp中文翻译	儿歌视频大全连续播放	鸡蛋价格表	一个多月宝宝睡觉老是挣扎不	乳透和彩超哪个好	中文喜宝奶粉	剖腹产后关节痛	小儿舌苔发白	剖腹产后刀口上面的肉突出来了	柏哲病	芦花鸡多少钱一斤	德国喜宝奶粉致癌	喜旺食品有限公司	乳透怎么检查多少钱	消化不良的症状	康婷瑞倪维儿甘蓝灵芝	伤口有线头化脓怎么办	完美肽藻营养粉的功效与作用	属鸡白羊座女和属羊天竭座男	一个多月宝宝睡觉不踏实易惊醒	下雨的声音催眠	打臭嗝老放屁拉肚子怎会事	一个多月宝宝喘气	剖腹产肚子发硬	一个多月的宝宝肚子老是咕噜噜响	为什么剖腹产后肚子硬	葫芦丝	剖腹产后肚脐周围硬	先天性心脏病的表现	天蝎座配白羊座	一个多月宝宝叹长气	属猪和属猪的姻缘好吗	玛丽艳	灵芝孢子粉的功效与作用	芦花鸡	剖腹产后胃疼屁多	肠胃湿热	湿疹是怎么引起的	心脏病的早期症状	剖腹产后屁特别多";
     
     TokenStream tokenStream = analyzer.tokenStream(line, new StringReader(line));
     try{
         Token t;
         //System.out.println(docText);
         while((t=tokenStream.next())!=null){
        	 String token = t.termText();
        	 if (Train.isUseless(token) == false)
        		 System.out.println(token + " " + t.type());
        	 
         }
     }catch(IOException e){
    	 e.printStackTrace();
     }
     
}

 private static String[] readWords(String line) {
	 return line.split("\\s+", 5); // \\s+ means any space
 }
 
 private static String readText(File file) {
     // TODOAuto-generated method stub
     String text=null;
     try{
         InputStreamReader read1=new InputStreamReader(new FileInputStream(file),"UTF-8");
         BufferedReader br1=new BufferedReader(read1);
         StringBuffer buff1=new StringBuffer();
         while((text=br1.readLine())!=null){
             buff1.append(text+"\n");
         }
         br1.close();
         text=buff1.toString();         
     }catch(FileNotFoundException e){
         System.out.println(e);
     }catch(IOException e){
         System.out.println(e);
     }
     return text;
 }
 
}
