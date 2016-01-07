package org.bigbluebutton;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.bigbluebutton.MyFrameRecorder.Exception;
import org.bytedeco.javacpp.BytePointer;
import static org.bytedeco.javacpp.avcodec.*;
import static org.bytedeco.javacpp.avutil.*;

public class DesktopDemo {

  private static int framesToEncode = 620;
  private static int x = 0;
  private static int y = 0;
  
  public static void main(String[] args) throws IOException, 
                                                AWTException, InterruptedException {
       
    MyFFmpegFrameRecorder recorder = null;
    int height = 480;
    int width = 640;
    Dimension screenBounds;
    Double frameRate = 12.0;    
    //String URL = "rtmp://192.168.23.36/video/room8-recorded";
    //String URL = "rtmp://ritz-breakout.novalocal/deskshare/room2";
    //String URL = "out2.webm";
    //String URL = "out2.mp4";
    //String URL = "rtp://ritz-breakout.novalocal/deskshare/room2";
    String URL = "tcp://192.168.23.36:6060/video/room8-recorded.webm";
    
    screenBounds = Toolkit.getDefaultToolkit().getScreenSize();
    width = screenBounds.width;
    height = screenBounds.height;
    
     if(args != null && args.length == 3) {
        URL = args[0];
        width = Integer.parseInt(args[1]);
        height = Integer.parseInt(args[2]);
        System.out.println("Using passed args: [" + URL + "] width=[" + width + "] height=[" + height + "]");
     } else {
       System.out.println("Using default args: [" + URL + "] width=[" + width + "] height=[" + height + "]");
       System.out.println("args null =[" + (args == null) + "] args.length=[" + args.length + "]");
     }
    
     
      System.out.println("Java temp dir : " + System.getProperty("java.io.tmpdir"));
      System.out.println("Java name : " + System.getProperty("java.vm.name"));
      System.out.println("OS name : " + System.getProperty("os.name"));
      System.out.println("OS arch : " + System.getProperty("os.arch"));
      System.out.println("JNA Path : " + System.getProperty("jna.library.path"));
             
      System.out.println("Capturing width=[" + width + "] height=[" + height + "]");

      recorder = new MyFFmpegFrameRecorder(URL, width, height);
      
//      userSVC2(recorder);
//      userH264(recorder);
      useVP8(recorder);
      
      recorder.setFrameRate(frameRate);
      recorder.setGopSize(24);

      try {
        recorder.start();
      } catch (Exception e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }

      int i = 0;
      long startTime = System.currentTimeMillis();
 
      Robot robot = new Robot();
      
      long sleepFramerate = (long) (1000 / frameRate);
           
      while (i < framesToEncode) {
        long now = System.currentTimeMillis();
           
        // grab the screenshot
        BufferedImage image = robot.createScreenCapture(new Rectangle(x, y, width, height));
             
        BufferedImage currentScreenshot = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        currentScreenshot.getGraphics().drawImage(image, 0, 0, null);
        
 
        long endCapture = System.currentTimeMillis();
        long captureDuration = endCapture - now;
        
//        System.out.println("[ENCODER] capture time [" + endCapture + "]");
        
        DataBuffer in  = currentScreenshot.getData().getDataBuffer();        
        byte[] a = ((DataBufferByte)in).getData();

        // Pass the new data to ffmpeg.
        ByteBuffer bbuffer = ByteBuffer.wrap(a); 
        BytePointer bpointer = new BytePointer(bbuffer);
        try {
          recorder.record(bpointer, image.getWidth(), image.getHeight(), AV_PIX_FMT_BGR24);
        } catch (Exception e1) {
          // TODO Auto-generated catch block
          e1.printStackTrace();
        }

            
        long timestamp = now - startTime;
        recorder.setTimestamp(timestamp * 1000);

//        System.out.println("i=[" + i + "] timestamp=[" + timestamp + "]");
        recorder.setFrameNumber(i);
       
        long ffmpegDuration = System.currentTimeMillis() - endCapture;
        long execDuration = (System.currentTimeMillis() - now);
            
//         System.out.println("[ENCODER] encoded image " + i + " in " + execDuration);
         i++;
        
          
         long sleepDuration = Math.max(sleepFramerate - execDuration, 0);
         try {
            // sleep for framerate milliseconds
            Thread.sleep(sleepDuration);
         } catch (InterruptedException e) {
            e.printStackTrace();
         }
//         System.out.println("[ENCODER] captureTime=[" + captureDuration + "] ffmpeg=[" + ffmpegDuration + "] sleep=[" + sleepDuration + "]ms, sleepFramerate = [" + sleepFramerate + "]");
       }

       if (recorder != null) {
         
         try {
           recorder.stop();
          recorder.release();
        } catch (Exception e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
       }

  }
  
  private static void useVP8(MyFFmpegFrameRecorder recorder) {
    // https://trac.ffmpeg.org/wiki/Encode/VP8
    // http://askubuntu.com/questions/45231/how-to-make-webm-desktop-recordings
    // HOWTO: Proper Screencasting on Linux (http://ubuntuforums.org/showthread.php?t=1392026&p=8746719#post8746719)
    // http://wiki.webmproject.org/ffmpeg
    // vp8
    recorder.setFormat("vp8");
    recorder.setVideoCodec(AV_CODEC_ID_VP8);
    recorder.setPixelFormat(AV_PIX_FMT_YUV420P);

    //  recorder.setVideoQuality(26);
    //recorder.setVideoOption("f", "gdigrab");
    //recorder.setVideoOption("i", "desktop");
//    recorder.setVideoOption("crf", "10");
    //recorder.setVideoOption("preset", "veryfast");
//    recorder.setVideoOption("qmax", "50");
//    recorder.setVideoOption("qmin", "0");
    //  recorder.setVideoOption("rtmp_buffer", "0");
    //  recorder.setVideoOption("rtmp_live", "live");
    //  recorder.setVideoOption("fflags", "nobuffer");
    recorder.setVideoOption("deadline", "realtime");
    recorder.setVideoOption("b", "5000000");
    recorder.setVideoOption("minrate", "200000");
    recorder.setVideoOption("maxrate", "10000000");
    
}
  
  private static void userSVC2(MyFFmpegFrameRecorder recorder) {
      recorder.setFormat("flv");
      
      ///
      // Flash SVC2
      recorder.setVideoCodec(AV_CODEC_ID_FLASHSV2);
      recorder.setPixelFormat(AV_PIX_FMT_BGR24);
  
      //  recorder.setVideoQuality(26);
      //recorder.setVideoOption("f", "gdigrab");
      //recorder.setVideoOption("i", "desktop");
      recorder.setVideoOption("crf", "38");
      recorder.setVideoOption("preset", "veryfast");
      recorder.setVideoOption("tune", "zerolatency");
      recorder.setVideoOption("intra-refresh", "1");
      //  recorder.setVideoOption("rtmp_buffer", "0");
      //  recorder.setVideoOption("rtmp_live", "live");
      //  recorder.setVideoOption("fflags", "nobuffer");    
  }
  
  private static void userH264(MyFFmpegFrameRecorder recorder) {
    recorder.setFormat("flv");
      
    // H264
    recorder.setVideoCodec(AV_CODEC_ID_H264);
    recorder.setPixelFormat(AV_PIX_FMT_YUV420P);

    //  recorder.setVideoQuality(26);
    recorder.setVideoOption("f", "gdigrab");
    recorder.setVideoOption("i", "desktop");
    recorder.setVideoOption("crf", "38");
    recorder.setVideoOption("preset", "veryfast");
    recorder.setVideoOption("tune", "zerolatency");
    recorder.setVideoOption("intra-refresh", "1");
    //  recorder.setVideoOption("rtmp_buffer", "0");
    //  recorder.setVideoOption("rtmp_live", "live");
    //  recorder.setVideoOption("fflags", "nobuffer");    
}
}
