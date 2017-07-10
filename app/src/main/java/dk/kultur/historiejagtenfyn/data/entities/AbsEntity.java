package dk.kultur.historiejagtenfyn.data.entities;

import com.j256.ormlite.field.DatabaseField;
import com.parse.ParseObject;

/**
 * Created by Lina on 2014.06.27.
 */
public abstract class AbsEntity {

    public static final String COLUMN_OBJECTID = "object_id";
    public static final String COLUMN_UPDATED_AT = "updated_at";
    public static final String _ID = "_id";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_LATITUDE = "latitude";

    @DatabaseField(generatedId = true, columnName = _ID)
    protected long id;
    @DatabaseField(columnName = COLUMN_OBJECTID, unique = true)
    protected String objectId;
    @DatabaseField(columnName = COLUMN_UPDATED_AT)
    protected long updatedAt;

    public AbsEntity() {

    }

    public AbsEntity(ParseObject entity) {
        objectId = entity.getObjectId();
        updatedAt = entity.getUpdatedAt().getTime();
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String mObjectId) {
        this.objectId = mObjectId;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long mUpdatedAt) {
        this.updatedAt = mUpdatedAt;
    }
}
