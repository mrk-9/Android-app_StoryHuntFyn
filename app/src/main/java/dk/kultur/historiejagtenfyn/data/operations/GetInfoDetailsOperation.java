package dk.kultur.historiejagtenfyn.data.operations;

import android.content.Context;

import com.parse.ParseObject;
import dk.kultur.historiejagtenfyn.data.entities.Language;
import dk.kultur.historiejagtenfyn.data.parse.models.InfoModelHis;
import dk.kultur.historiejagtenfyn.data.parse.models.LanguageModelHis;
import dk.kultur.historiejagtenfyn.data.repositories.IRepository;
import dk.kultur.historiejagtenfyn.data.repositories.LanguageRepository;

/**
 * Created by Lina on 2014.07.02.
 */
public class GetInfoDetailsOperation extends AbsAsyncOperation<ParseObject> {

    protected LanguageRepository mRepository;

    public GetInfoDetailsOperation(Context context) {
        mRepository = new LanguageRepository(context);
    }

    @Override
    protected ParseObject doInBackground(String... params) {

        return mRepository.getCurrentLanguage();
    }
}
