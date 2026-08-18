package lt.vaeloria.ooc;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.ToneGenerator;
import android.os.Process;

/** Procedūrinis, be papildomų failų veikiantis aplinkos ir sąsajos garso sluoksnis. */
final class VaeloriaAudio implements AutoCloseable {
    enum Cue { NAVIGATE, ACTION, SUCCESS, DANGER }
    private static final int RATE=22050;
    private final SharedPreferences preferences;
    private volatile boolean running;
    private volatile AudioTrack track;
    private Thread ambientThread;
    private ToneGenerator tones;
    private String scene="";

    VaeloriaAudio(Context context){preferences=context.getSharedPreferences("vaeloria_visual",Context.MODE_PRIVATE);}

    synchronized void startAmbient(String location){
        if(!preferences.getBoolean("ambient",false)){stopAmbient();return;}
        String next=location==null?"Vaeloria":location;if(running&&next.equals(scene))return;stopAmbient();scene=next;running=true;
        ambientThread=new Thread(()->ambientLoop(next),"VaeloriaAmbient");ambientThread.setDaemon(true);ambientThread.start();
    }

    synchronized void stopAmbient(){running=false;AudioTrack current=track;if(current!=null){try{current.pause();current.flush();current.stop();}catch(Exception ignored){}}Thread worker=ambientThread;if(worker!=null)worker.interrupt();ambientThread=null;track=null;}

    synchronized void cue(Cue cue){
        if(!preferences.getBoolean("sounds",false))return;
        if(tones==null)tones=new ToneGenerator(AudioManager.STREAM_MUSIC,Math.max(8,preferences.getInt("sfx_volume",24)));
        int tone;int duration;
        if(cue==Cue.ACTION){tone=ToneGenerator.TONE_PROP_ACK;duration=70;}
        else if(cue==Cue.SUCCESS){tone=ToneGenerator.TONE_PROP_PROMPT;duration=90;}
        else if(cue==Cue.DANGER){tone=ToneGenerator.TONE_PROP_NACK;duration=110;}
        else{tone=ToneGenerator.TONE_PROP_BEEP;duration=45;}
        tones.startTone(tone,duration);
    }

    private void ambientLoop(String location){
        Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);AudioTrack local=null;
        try{
            int minimum=AudioTrack.getMinBufferSize(RATE,AudioFormat.CHANNEL_OUT_MONO,AudioFormat.ENCODING_PCM_16BIT);int buffer=Math.max(minimum,RATE/2);
            local=new AudioTrack.Builder().setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()).setAudioFormat(new AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT).setSampleRate(RATE).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build()).setBufferSizeInBytes(buffer*2).setTransferMode(AudioTrack.MODE_STREAM).build();track=local;local.play();
            short[] samples=new short[1024];long frame=0;int hash=Math.abs(location.hashCode());double base=42+(hash%18),fifth=base*1.5,air=base*2.01;float volume=Math.max(0,Math.min(100,preferences.getInt("ambient_volume",22)))/100f;
            while(running&&!Thread.currentThread().isInterrupted()){
                for(int i=0;i<samples.length;i++,frame++){double t=frame/(double)RATE;double breathe=.58+.42*Math.sin(2*Math.PI*.055*t);double wave=Math.sin(2*Math.PI*base*t)*.52+Math.sin(2*Math.PI*fifth*t)*.24+Math.sin(2*Math.PI*air*t)*.10;double shimmer=Math.sin(2*Math.PI*(air+Math.sin(t*.12)*2.4)*t)*.07;samples[i]=(short)(wave*breathe*shimmerScale(shimmer)*1500*volume);}
                if(local.write(samples,0,samples.length,AudioTrack.WRITE_BLOCKING)<0)break;
            }
        }catch(Throwable ignored){}finally{if(local!=null){try{local.stop();}catch(Exception ignored){}try{local.release();}catch(Exception ignored){}}if(track==local)track=null;}
    }

    private double shimmerScale(double shimmer){return 1.0+shimmer;}
    @Override public synchronized void close(){stopAmbient();if(tones!=null){tones.release();tones=null;}}
}
