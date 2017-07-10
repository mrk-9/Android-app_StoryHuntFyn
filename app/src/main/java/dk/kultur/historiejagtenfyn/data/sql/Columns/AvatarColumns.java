package dk.kultur.historiejagtenfyn.data.sql.Columns;

import dk.kultur.historiejagtenfyn.data.parse.contracts.AvatarHisContract;

/**
 * Avatar table
 * Created by JustinasK on 2/21/2015.
 */
public interface AvatarColumns {
    public static final String COLUMN_AVATAR = AvatarHisContract.KEY_AVATAR;
    public static final String COLUMN_IMAGE_1 = AvatarHisContract.KEY_FILE_IMAGE_1;
    public static final String COLUMN_IMAGE_2 = AvatarHisContract.KEY_FILE_IMAGE_2;
    public static final String COLUMN_IMAGE_3 = AvatarHisContract.KEY_FILE_IMAGE_3;
}