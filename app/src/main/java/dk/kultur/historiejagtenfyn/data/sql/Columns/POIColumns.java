package dk.kultur.historiejagtenfyn.data.sql.Columns;

import dk.kultur.historiejagtenfyn.data.parse.contracts.POIContentHisContract;
import dk.kultur.historiejagtenfyn.data.parse.contracts.POIHisContract;
import dk.kultur.historiejagtenfyn.data.parse.contracts.ParseContract;

/**
 * POI column data
 * Created by JustinasK on 2/20/2015.
 */
public interface POIColumns {

    public static final String COLUMN_AUTO_RANGE = POIHisContract.AUTO_RANGE;
    public static final String COLUMN_AUTO_PLAY = POIHisContract.AUTO_PLAY;
    public static final String COLUMN_AR_RANGE = POIHisContract.AR_RANGE;
    public static final String COLUMN_IS_AR = POIHisContract.IS_AUTO_RANGE;
    public static final String COLUMN_COORDINATES_LAT = POIHisContract.COORDINATES + POIHisContract.COORDINATES_LAT;
    public static final String COLUMN_COORDINATES_LON = POIHisContract.COORDINATES + POIHisContract.COORDINATES_LON;
    public static final String COLUMN_MAP_RANGE = POIHisContract.MAP_RANGE;
    public static final String COLUMN_CLICK_RANGE = POIHisContract.CLICK_RANGE;
    public static final String COLUMN_NAME = POIHisContract.NAME;
    public static final String COLUMN_NO_AVATAR = POIHisContract.NO_AVATAR;
    public static final String COLUMN_NO_AVARAR = POIHisContract.NO_AVARAR;
    public static final String COLUMN_VIDEO_URL = POIHisContract.VIDEO_URL;
    public static final String COLUMN_IMAGE = POIHisContract.IMAGE + POIHisContract.POINTER_ID;
    public static final String COLUMN_AVATAR = POIHisContract.AVATAR + POIHisContract.POINTER_ID;
    public static final String COLUMN_AUDIO = POIHisContract.AUDIO + ParseContract.URL;
    public static final String COLUMN_QUIZ = POIHisContract.QUIZ + POIHisContract.POINTER_ID;
    public static final String COLUMN_UNLOCK_POI = POIHisContract.UNLOCK_POI + POIHisContract.POINTER_ID;
    public static final String COLUMN_PARENT_POI = POIHisContract.PARENT_POI + POIHisContract.POINTER_ID;
    public static final String COLUMN_POINT_AWARDING = POIHisContract.POINT_AWARDING;
    public static final String COLUMN_FACTS_IMAGE = POIHisContract.FACTS_IMAGE + POIHisContract.POINTER_ID;
    public static final String COLUMN_PARENT_POINT = POIHisContract.PARENT_POINT;


    public interface POIContentColumns {

        public static final String COLUMN_LANGUAGE_ID = POIContentHisContract.KEY_POINTER_LANGUAGE + ParseContract.POINTER_ID;
        public static final String COLUMN_FACTS = POIContentHisContract.KEY_FACTS;
        public static final String COLUMN_IMAGE_TITLE = POIContentHisContract.KEY_IMAGE_TITLE;
        public static final String COLUMN_INFO = POIContentHisContract.KEY_INFO;
        public static final String COLUMN_NAME = POIContentHisContract.KEY_NAME;
        public static final String COLUMN_VIDEO_TITLE = POIContentHisContract.KEY_VIDEO_TITLE;
        public static final String COLUMN_FACTS_IMAGE_TITLE = POIContentHisContract.KEY_FACTS_IMAGE_TITLE;

    }

}
