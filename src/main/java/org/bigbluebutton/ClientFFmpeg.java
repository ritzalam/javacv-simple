package org.bigbluebutton;

import org.bigbluebutton.MyFrameRecorder.Exception;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacv.*;

import java.io.IOException;

import static org.bytedeco.javacpp.avcodec.*;
import static org.bytedeco.javacpp.avformat.*;
import static org.bytedeco.javacpp.avutil.*;

public class ClientFFmpeg {
    public static void main(String[] args) throws Exception {
      System.out.println("Java temp dir : " + System.getProperty("java.io.tmpdir"));
      System.out.println("Java name : " + System.getProperty("java.vm.name"));
      System.out.println("OS name : " + System.getProperty("os.name"));
      System.out.println("OS arch : " + System.getProperty("os.arch"));
      System.out.println("JNA Path : " + System.getProperty("jna.library.path"));
      System.out.println("Platform : " + Loader.getPlatform());
      
      Double frameRate = 12.0;
      
      final FFmpegFrameGrabber grabber = new FFmpegFrameGrabber("desktop");
      grabber.setImageWidth(800);
      grabber.setImageHeight(600);
      grabber.setFormat("gdigrab");
      grabber.setFrameRate(frameRate);
      try {
        grabber.start();
      } catch (org.bytedeco.javacv.FrameGrabber.Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder("out3.mp4", grabber.getImageWidth(), grabber.getImageHeight());
        recorder.setFormat("flv");
        recorder.setFrameRate(frameRate);
        recorder.setGopSize(24);
        // H264
        userH264(recorder);
        
        try {
          recorder.start();
        } catch (org.bytedeco.javacv.FrameRecorder.Exception e1) {
          // TODO Auto-generated catch block
          e1.printStackTrace();
        }
        long sleepFramerate = (long) (1000 / frameRate);
        int framesToEncode = 620;
        Frame frame;
        int i = 0;
        while (i < framesToEncode) {
          long now = System.currentTimeMillis();
          try {
            frame = grabber.grabImage();
            if (frame != null) {
              try {
                recorder.record(frame);
              } catch (org.bytedeco.javacv.FrameRecorder.Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
              }
            }
          } catch (org.bytedeco.javacv.FrameGrabber.Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
          i++;
          
          long execDuration = (System.currentTimeMillis() - now);
          long sleepDuration = Math.max(sleepFramerate - execDuration, 0);
          try {
             // sleep for framerate milliseconds
             Thread.sleep(sleepDuration);
          } catch (InterruptedException e) {
             e.printStackTrace();
          }
          
          System.out.println("Captured frame " + i);

        }
        try {
          recorder.stop();
        } catch (org.bytedeco.javacv.FrameRecorder.Exception e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        try {
          grabber.stop();
        } catch (org.bytedeco.javacv.FrameGrabber.Exception e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
    }
    
    private static void userH264(FFmpegFrameRecorder recorder) {
      recorder.setFormat("flv");
        
      // H264
      recorder.setVideoCodec(AV_CODEC_ID_H264);
      recorder.setPixelFormat(AV_PIX_FMT_YUV420P);

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
}
