package dk.kultur.historiejagtenfyn.data.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.parse.ParseObject;

import org.json.JSONArray;

/**
 * Created by Lina on 2014.06.27.
 */
@DatabaseTable(tableName = Language.TABLE_NAME)
public class Language extends AbsEntity {

    public static final String TABLE_NAME = "language";
    public static final String COLUMN_LANGUAGE = "language";
    public static final String COLUMN_LANGUAGE_CODE = "code";
    public static final String COLUMN_ACTIVE = "is_active";
    public static final String COLUMN_PRIORITY = "priority";

    public static final String COLUMN_PRIORITY_LIST = "priority_list";

    // info
    public static final String COLUMN_INFO_TEXT = "info_text";
    public static final String COLUMN_INFO_TITLE = "info_title";
    public static final String COLUMN_INFO_OBJECT_ID = "info_object_id";

    @DatabaseField(columnName = COLUMN_LANGUAGE)
    private String language;
    @DatabaseField(columnName = COLUMN_LANGUAGE_CODE)
    private String code;
    @DatabaseField(columnName = COLUMN_ACTIVE)
    private boolean active;
    @DatabaseField(columnName = COLUMN_PRIORITY)
    private int priority;

    @DatabaseField(columnName = COLUMN_PRIORITY_LIST)
    private String mPriorityList;

    @DatabaseField(columnName = COLUMN_INFO_TEXT)
    private String mInfoText;
    @DatabaseField(columnName = COLUMN_INFO_TITLE)
    private String mInfoTitle;
    @DatabaseField(columnName = COLUMN_INFO_OBJECT_ID)
    private String mInfoObjectId;

    public Language() {
        super();
    }


    public Language(ParseObject language, ParseObject info) {
        super(language);
        this.language = language.getString("language");
        code = language.getString("code");
        active = language.getBoolean("KEY_ACTIVE");
        priority = language.getInt("priority");
        JSONArray json = new JSONArray(language.getList("priorityList"));
        mPriorityList = json.toString();
        mInfoText = info.getString("text");
        mInfoTitle = info.getString("title");
        mInfoObjectId = info.getObjectId();

    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String mLanguage) {
        this.language = mLanguage;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String mCode) {
        this.code = mCode;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean mActive) {
        this.active = mActive;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int mPriority) {
        this.priority = mPriority;
    }

    public String getPriorityList() {
        return mPriorityList;
    }

    public void setPriorityList(String mPriorityList) {
        this.mPriorityList = mPriorityList;
    }

    public String getInfoText() {
        return mInfoText;
    }

    public void setInfoText(String mInfoText) {
        this.mInfoText = mInfoText;
    }

    public String getInfoTitle() {
        return mInfoTitle;
    }

    public void setInfoTitle(String mInfoTitle) {
        this.mInfoTitle = mInfoTitle;
    }

    public String getInfoObjectId() {
        return mInfoObjectId;
    }

    public void setInfoObjectId(String mInfoObjectId) {
        this.mInfoObjectId = mInfoObjectId;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[objectId=").append(objectId).append("],")
                .append("[language=").append(language).append("],")
                .append("[code=").append(code).append("],")
                .append("[KEY_ACTIVE=").append(active).append("],")
                .append("[infoTitle=").append(mInfoTitle).append("]");

        return builder.toString();
    }
}
