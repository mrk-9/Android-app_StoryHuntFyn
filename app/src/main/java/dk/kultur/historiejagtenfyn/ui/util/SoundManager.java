package dk.kultur.historiejagtenfyn.ui.util;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Handler;
import android.util.Log;
import android.util.SparseIntArray;

import dk.kultur.historiejagtenfyn.R;

public class SoundManager {

    private static SoundManager mThis;

    /**
     * Event sound constants
     */
    // Sound for turning page
    public static final int SOUND_BOOK_PAGE = 1;
    // All buttons
    public static final int SOUND_BUTTON = 2;

    // sound for getting point
    public static final int SOUND_GET_POINT = 3;

    // sound for completing 25/50/75/100 percent
    public static final int SOUND_GET_POINT_WITH_BONUS = 4;


    private Context mContext;
    private SoundPool soundPool;

    // Array to save loaded sounds
    private SparseIntArray sounds = new SparseIntArray();

    private int lastSoundId = 0;

    /**
     *
     * @param context
     */
    private SoundManager(Context context) {
        mContext = context;
        soundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
        sounds.put(SOUND_BOOK_PAGE, soundPool.load(mContext, R.raw.book_page_turn, 1));
        sounds.put(SOUND_BUTTON, soundPool.load(mContext, R.raw.button, 1));
        sounds.put(SOUND_GET_POINT, soundPool.load(mContext, R.raw.get_point, 1));
        sounds.put(SOUND_GET_POINT_WITH_BONUS, soundPool.load(mContext, R.raw.get_point_with_bonus, 1));


    }


    /**
     *
     * @param appContext
     */
    public static void init(Context appContext) {
        mThis = new SoundManager(appContext);
    }

    /**
     *
     * @return
     */
    public static SoundManager getInstance(Context appContext) {
        if (mThis == null) {
            init(appContext);
        }
        return mThis;
    }

    /**
     *
     * @param sound
     */
    public void playSound(int sound) {

       soundPool.pause(lastSoundId);
        AudioManager audio = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        int currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        float volume = (float)currentVolume/maxVolume;
        int id = soundPool.play(sounds.get(sound), volume, volume, 1, 0, 1);

        lastSoundId = id;

    }


}

