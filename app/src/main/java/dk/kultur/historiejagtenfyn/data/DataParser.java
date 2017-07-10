package dk.kultur.historiejagtenfyn.data;

import android.content.Context;
import android.util.Log;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import dk.kultur.historiejagtenfyn.data.entities.Language;
import dk.kultur.historiejagtenfyn.data.entities.Route;
import dk.kultur.historiejagtenfyn.data.entities.RouteContent;
import dk.kultur.historiejagtenfyn.data.repositories.LanguageRepository;
import dk.kultur.historiejagtenfyn.data.repositories.RouteContentRepository;
import dk.kultur.historiejagtenfyn.data.repositories.RouteRepository;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by Lina on 2014.06.27.
 */
public class DataParser {
    public static final String PARSE_APPLICATION_KEY = "sVGrfVql8qrbteX85x6mslpLc7pa03S12tj5kcLv";
    public static final String PARSE_CLIENT_KEY = "M9ncDaTFz6dJN82J3UMCfYiX32rG3oBGqTUgh6Yu";


    /**
     * Gets all languages and infos and save them to sqplite (saves info on language)
     *
     * @param context
     * @throws com.parse.ParseException
     */
    public static void fetchInfos(final Context context) throws ParseException {
        // Delete all data from repository
        LanguageRepository repository = new LanguageRepository(context);
        repository.cleanAll();


        // get all data and save to sqlite
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Info");
        query.include("language");
        List<ParseObject> list = query.find();
        List<Language> languageList = new ArrayList<Language>(list.size());
        for (ParseObject object : list) {
            Language language = new Language(object.getParseObject("language"), object);
            languageList.add(language);
            Log.i("Language", language.toString());
        }
        try {
            repository.insert(languageList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets all updated and deleted routes, icons, avatars and routecontents.
     * Deletes old and updated, saves new to sqplite.
     * IMPORTANT! Should not be called before all pois has been created
     *
     * @param context
     * @throws ParseException
     */
    public static void fetchRoutes(final Context context) throws ParseException {

        LanguageRepository languageRep = new LanguageRepository(context);
        String languageObjectId = languageRep.getCurrentLanguageObjectId();

        RouteRepository repository = new RouteRepository(context);
        RouteContentRepository contentRepository = new RouteContentRepository(context);
        HashMap<String, Object> existingMap = getExistingRoutes(repository);
        HashMap<String, HashMap<String, Object>> existingContainer = new HashMap<String, HashMap<String, Object>>();
        existingContainer.put("existing", existingMap);

        HashMap<String, Object> parseMap = ParseCloud.callFunction("updateRoutesWithContent", existingContainer);
        HashMap<String, Object> updatedMap = (HashMap<String, Object>) parseMap.get("updated");
        ArrayList<String> deletedMap = (ArrayList<String>) parseMap.get("deleted");

        // remove updated and deleted data
        removeRoutes(deletedMap, updatedMap, repository);

        // add updated data
        addRoutes(repository, contentRepository, updatedMap, languageObjectId);
    }

    private static void removeRoutes(ArrayList<String> deletedList, HashMap<String, Object> updatedMap, RouteRepository repository) {
        if (updatedMap != null) {
            // iterate over the routes in updatedMap
            Iterator it = updatedMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pairs = ((Map.Entry) it.next());
                ParseObject route = ((HashMap<String, ParseObject>) pairs.getValue()).get("route");
                String objectId = route.getObjectId();

                // first delete route
                try {
                    repository.deleteRoute(objectId);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                /*deleteDatabaseEntry(Route.TABLE_NAME, Route.COLUMN_OBJECTID, objectId);

                // then delete routeContent
                deleteDatabaseEntry(RouteContent.TABLE_NAME, RouteContent.COLUMN_ROUTE_ID, objectId);

                // then delete avatar
                ParseObject avatar = null;
                if(((HashMap<String, ParseObject>)  pairs.getValue()).containsKey("avatar")) {
                    Object avatarObject = ((HashMap<String, Object>)  pairs.getValue()).get("avatar");	// WE do this because we sometimes get a
                    if(!avatarObject.toString().equalsIgnoreCase("null")) {								// jsonobject with null text
                        avatar = ((HashMap<String, ParseObject>) pairs.getValue()).get("avatar");
                        if(avatar != null) {
                            deleteDatabaseEntry(Avatar.TABLE_NAME, avatar.getObjectId(), objectId);
                        }
                    }
                }

                // then delete icon
                ParseObject icon = ((HashMap<String, ParseObject>) pairs.getValue()).get("icon");
                if(icon != null) {
                    deleteDatabaseEntry(Icon.TABLE_NAME, icon.getObjectId(), objectId);
                }
                */
            }
        }

        if (deletedList != null) {
            // iterate over deletedList
            for (String objectId : deletedList) {
                // first get icon and avatar ids if any
                try {
                    repository.deleteRoute(objectId);
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                //TODO delete icons and avatars
            }
        }
    }

    /**
     * Get a hashmap of existing routes and their updated at time
     *
     * @return
     */
    private static HashMap<String, Object> getExistingRoutes(RouteRepository repository) {
        HashMap<String, Object> existing = new HashMap<String, Object>();

        List<Route> routeList = repository.selectAll();

        for (Route route : routeList) {
            String objectId = route.getObjectId();
            long dateLong = route.getUpdatedAt();
            Date date = new Date();
            date.setTime(dateLong);
            existing.put(objectId, date);
        }
        return existing;
    }

    /**
     * Add all updated data to sql for: avatar, route, routecontent, icon
     *
     * @param updatedMap
     * @return
     */
    private static void addRoutes(RouteRepository repository, RouteContentRepository contentRepository, HashMap<String, Object> updatedMap, String languageObjectId) {
        Iterator it = updatedMap.entrySet().iterator();
        List<Route> routeList = new ArrayList<Route>();
        List<RouteContent> routeContentList = new ArrayList<RouteContent>();
        // iterate over the routes
        while (it.hasNext()) {
            Map.Entry pairs = ((Map.Entry) it.next());
            ParseObject avatar = null;
            if (((HashMap<String, ParseObject>) pairs.getValue()).containsKey("avatar")) {
                Object avatarObject = ((HashMap<String, Object>) pairs.getValue()).get("avatar");    // WE do this because we sometimes get a
                if (!avatarObject.toString().equalsIgnoreCase("null")) {                                // jsonobject with null text
                    avatar = ((HashMap<String, ParseObject>) pairs.getValue()).get("avatar");
                }
            }
            ParseObject routeParse = ((HashMap<String, ParseObject>) pairs.getValue()).get("route");
            ArrayList<String> pointOfInterests =
                    ((HashMap<String, ArrayList<String>>) pairs.getValue()).get("pointOfInterests");    // this is simply ids
            HashMap<String, ParseObject> routeContents =
                    ((HashMap<String, HashMap<String, ParseObject>>) pairs.getValue()).get("contents");
            ParseObject icon = ((HashMap<String, ParseObject>) pairs.getValue()).get("icon");

            if (routeParse != null) {
                Route route = new Route(routeParse);
                if (avatar != null) {
                    route.setAvatar(avatar);
                }
                if (icon != null) {
                    route.setIcon(icon);
                }

                // save routecontents
                Iterator contentIt = routeContents.entrySet().iterator();
                while (contentIt.hasNext()) {
                    Map.Entry contentPairs = (Map.Entry) contentIt.next();
                    ParseObject routeContent = (ParseObject) contentPairs.getValue();
                    ParseObject languageParse = routeContent.getParseObject("language");
                    if (languageObjectId != null && languageObjectId.equals(languageParse.getObjectId())) {
                        RouteContent content = new RouteContent(routeContent);
                        content.setLanguageId(languageParse.getObjectId());
                        content.setRouteId(route.getObjectId());
                    }

                }
                Log.i("DataParser", "adding route " + route);
                routeList.add(route);
            }

            // we're going to store route object id in pois
            /*for(String poiObjectId : pointOfInterests) {
                updatePOIRouteId(context, poiObjectId, routeObjectId);
            }*/
        }

        try {
            repository.insert(routeList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
