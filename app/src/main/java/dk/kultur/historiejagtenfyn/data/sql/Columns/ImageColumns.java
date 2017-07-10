package dk.kultur.historiejagtenfyn.data.sql.Columns;

import dk.kultur.historiejagtenfyn.data.parse.contracts.ImageHisContract;
import dk.kultur.historiejagtenfyn.data.parse.contracts.ParseContract;

/**
 * Created by JustinasK on 2/22/2015.
 */
public interface ImageColumns {

    String COLUMN_CROPPED_URL = ImageHisContract.CROPPED + ParseContract.URL;
    String COLUMN_IMAGE_URL = ImageHisContract.IMAGE + ParseContract.URL;
}
