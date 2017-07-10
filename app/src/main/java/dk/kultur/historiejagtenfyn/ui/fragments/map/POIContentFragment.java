package dk.kultur.historiejagtenfyn.ui.fragments.map;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.*;
import android.widget.ImageView;
import com.flurry.android.FlurryAgent;
import dk.kultur.historiejagtenfyn.R;
import dk.kultur.historiejagtenfyn.data.entities.POIEntity;
import dk.kultur.historiejagtenfyn.data.entities.RoutePointEntity;
import dk.kultur.historiejagtenfyn.data.sql.Columns.RouteColumns;
import dk.kultur.historiejagtenfyn.data.sql.HisContract;
import dk.kultur.historiejagtenfyn.data.sql.ProviderMethods;
import dk.kultur.historiejagtenfyn.ui.activities.ImageActivity;
import dk.kultur.historiejagtenfyn.ui.activities.POIDetailsActivity;
import dk.kultur.historiejagtenfyn.ui.dialogs.RoutePointProgressDialogFragment;
import dk.kultur.historiejagtenfyn.ui.util.FileUtils;
import dk.kultur.historiejagtenfyn.ui.util.SoundManager;
import dk.kultur.historiejagtenfyn.ui.util.UIUtils;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Fragment for displaying main POI content page. Webview is used for dispaying text ant images. Avatar is displayes as ImageView (not in webview
 */
public class POIContentFragment extends AbsFragmentWithSideTabView {



    public static final String HTML_PLACEHOLDER_VIDEO = ":::VIDEO_HERE:::";

    private static final String TAG = POIContentFragment.class.getSimpleName();
    private static final String EXTRA_POI = "EXTRA_POI";
    private static final String EXTRA_ROUTE_POINT_TEXT_COLUMN = "EXTRA_ROUTE_POINT_TEXT_COLUMN";
    private static final String EXTRA_ROUTE_POINT_DIALOG_TEXT = "EXTRA_ROUTE_POINT_DIALOG_TEXT";
    private static final int FRAGMENT_ID = 1;

    private static final int LOADER_ID_POI_CONTENT = 4000;
    private static final int LOADER_ID_POI_ROUTE_POINT = 4001;
    private static final int LOADER_ID_ROUTE_POINT_POI_COUNT = 4002;
    private static final int LOADER_ID_ROUTE_POINT_TEXT = 4003;
    private static final int LOADER_ID_ROUTE_AVATAR = 4004;
    private static final int MESSAGE_SHOW_POPUP = 100;

    private static final String AVATAR1_SCHEME = "avatar1";
    private static final String AVATAR2_SCHEME = "avatar2";
    private static final String AVATAR3_SCHEME = "avatar3";

    private AnimationDrawable animation;
    private ImageView playButtonAvatarAnimation;
    private ImageView playAvatarBubble;
    private MediaPlayer mediaPlayer;
    private boolean animationIsStarted;

    private String mPoiId;
    private POIEntity poi;
    private boolean initialized = false;
    private RoutePointEntity routePointEntity;

    private Handler mWebViewHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (getView()!= null) {
                ((WebView) getView().findViewById(R.id.detailViewWebView)).setVisibility(View.VISIBLE);
            }
        }

    };
    private Handler dialogHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MESSAGE_SHOW_POPUP) {
                Bundle args = msg.getData();
                String text = args.getString(EXTRA_ROUTE_POINT_DIALOG_TEXT);
                showDialog(text);
            }
        }
    };
    private InputStream imageInputStream;
    private InputStream avatar1InputStream;
    private InputStream avatar2InputStream;
    private InputStream avatar3InputStream;
    private int avatarCount = 0;
    private String avatarImage1 = null;
    private String avatarImage2 = null;
    private String avatarImage3 = null;

    public static POIContentFragment newInstance(String mPoiId, int count, boolean hasQuiz, int activeTab) {
        POIContentFragment f = new POIContentFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_POI, mPoiId);
        initArgs(args, count, hasQuiz, activeTab);
        f.setArguments(args);
        return f;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        mPoiId = getArguments().getString(EXTRA_POI);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = super.onCreateView(inflater, container, savedInstanceState);

        final WebView webView = (WebView) layout.findViewById(R.id.detailViewWebView);
        webView.setBackgroundColor(0x00000000);

        webView.getSettings().setJavaScriptEnabled(true);
        //Next to statements remove strange webview black blinking when scrolling
        //http://stackoverflow.com/questions/17315815/strange-webview-black-blinking-when-scrolling
        webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

        //http://stackoverflow.com/questions/4065312/detect-click-on-html-button-through-javascript-in-android-webview
        webView.addJavascriptInterface(new Object() {
            @JavascriptInterface           // For API 17+
            public void performClick() {
                Intent intent = new Intent(getActivity(), ImageActivity.class);
                intent.putExtra(ImageActivity.EXTRA_IMAGE_PATH, poi.getObjectId() + FileUtils.IMAGE);
                startActivity(intent);
            }
        }, "image");

        webView.addJavascriptInterface(new Object() {
            @JavascriptInterface           // For API 17+
            public void performClick() {
                Log.d(TAG, "play button clicked: ");
                String videoUrl = "https://www.youtube.com/watch?v=" + poi.getVideoUrl();
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl));
                //intent.putExtra(VideoActivity.EXTRA_VIDEO_URL, videoUrl);
                startActivity(intent);

            }
        }, "play");

        webView.addJavascriptInterface(new Object() {
            @JavascriptInterface           // For API 17+
             public void audioPlay() {
                Log.d(TAG, "audio play button clicked: ");
                play();
            }

            @JavascriptInterface           // For API 17+
            public void audioStop() {
                Log.d(TAG, "audio stop button clicked: ");
                stopPlaying();
            }
            @JavascriptInterface
            public int getAvatarCount() {
                Log.d(TAG, "audio play button clicked: ");
                //todo start or stop playing
                return avatarCount;

            }
            @JavascriptInterface
            public String getAvatarImage(int index) {
                Log.d(TAG, "getAvatarImage: " + index);
                String imgUrl = avatarImage1;
                switch (avatarCount) {
                    case 1:
                        break;
                    case 2:
                        if (index % 2 == 1) {
                            imgUrl = avatarImage2;
                            Log.d(TAG, "return avatar image2");
                        }
                        break;
                    case 3:
                        if (index % 3 == 1) {
                            imgUrl = avatarImage2;
                            Log.d(TAG, "return avatar image2");
                        } else if (index % 3 == 2) {
                            imgUrl = avatarImage3;
                            Log.d(TAG, "return avatar image3");
                        }
                        break;
                }
                return imgUrl;

            }
        }, "audio");

        //this prints javascripts console.log to logcat
        webView.setWebChromeClient(new WebChromeClient() {
            public boolean onConsoleMessage(ConsoleMessage cm) {
                Log.d("MyApplication", cm.message() + " -- From line "
                        + cm.lineNumber() + " of "
                        + cm.sourceId());
                return true;
            }
        });

        UIUtils.showProgressBar(getActivity());

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                UIUtils.hideProgressBar(getActivity());


            }

            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
            }

            @TargetApi(VERSION_CODES.LOLLIPOP)
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                Log.d(TAG, "should intercept url " + request.getUrl().toString());
                if (Uri.parse(request.getUrl().toString()).getScheme().equals(IMG_SCHEME)) {
                    return new WebResourceResponse("image/*", "base64", imageInputStream);
                } else if (Uri.parse(request.getUrl().toString()).getScheme().equals(AVATAR1_SCHEME)) {
                    return new WebResourceResponse("image/*", "base64", avatar1InputStream);
                } else if (Uri.parse(request.getUrl().toString()).getScheme().equals(AVATAR2_SCHEME)) {
                    return new WebResourceResponse("image/*", "base64", avatar2InputStream);
                } else if (Uri.parse(request.getUrl().toString()).getScheme().equals(AVATAR3_SCHEME)) {
                    return new WebResourceResponse("image/*", "base64", avatar3InputStream);
                }
                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                Log.d(TAG, "should intercept url " + url);
                if (Uri.parse(url).getScheme().equals(IMG_SCHEME)) {
                    return new WebResourceResponse("image/*", "base64", imageInputStream);
                } else if (Uri.parse(url).getScheme().equals(AVATAR1_SCHEME)) {
                    return new WebResourceResponse("image/*", "base64", avatar1InputStream);
                } else if (Uri.parse(url).getScheme().equals(AVATAR2_SCHEME)) {
                    return new WebResourceResponse("image/*", "base64", avatar2InputStream);
                } else if (Uri.parse(url).getScheme().equals(AVATAR3_SCHEME)) {
                    return new WebResourceResponse("image/*", "base64", avatar3InputStream);
                }
                return super.shouldInterceptRequest(view, url);
            }
        });

        return layout;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!initialized) {
            initialized = true;
            getLoaderManager().initLoader(LOADER_ID_POI_CONTENT, getArguments(), mLoaderCallbacks);
        } else {
            initData();
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_poi_content;
    }

    private String formBody(POIEntity poi) {
        String body = poi.getInfo();

        final String relativeName = FileUtils.getRelativeName(poi.getObjectId(), FileUtils.IMAGE);
        String imageUrl = null;
        imageInputStream = FileUtils.getWebResource(relativeName, getActivity());
        if (imageInputStream != null) {
            imageUrl = IMG_SCHEME + ":///" + relativeName;
        }
        StringBuilder total = new StringBuilder(body);
        int index = total.indexOf(HTML_PLACEHOLDER_IMAGE);
        boolean imageAdded = false;
        boolean avatarAdded = false;
        Log.d(TAG, "form body img placeholder index " + index);
        if (index > -1) {

            String imgString = formImageString(imageUrl, poi.getImageTitle(), poi);
            avatarAdded = imgString.length() > 0;
            /*if (!avatarAdded) {
                total.insert(index + HTML_PLACEHOLDER_IMAGE.length(), "<div id=\"anotherTxt\" style=\"position: relative; margin-top:0px !important;\">");
            } else {*/
                total.insert(index + HTML_PLACEHOLDER_IMAGE.length(), "<div id=\"anotherTxt\">");
            //}
            total.append("</div>");
            total.replace(index, index + HTML_PLACEHOLDER_IMAGE.length(), imgString);
            imageAdded = true;

        }
        index = total.indexOf(HTML_PLACEHOLDER_VIDEO);
        //total.append(formVideoString(poi.getImageUrl(), poi.getImageTitle()));
        if (index > -1) {

            total.insert(index + HTML_PLACEHOLDER_IMAGE.length(), "<div id=\"thirdTxt\">");
            total.replace(index, index + HTML_PLACEHOLDER_VIDEO.length(), formVideoString(poi.getVideoUrl(), poi.getVideoTitle()));
            if (imageAdded) {
                total.insert(index, "</div>");
            } else {
                total.append("</div>");
            }
        }
        if (!avatarAdded) {
            total.append(formAvatarString(poi, false));
        }

        Log.d(TAG, "body " + total.toString());
        return total.toString();

    }

    private String formImageString(String url, String imageTitle, POIEntity poi) {
        Log.d(TAG, "form image string url " + url);
        if (url == null) {
            return "";
        }
        //<div id="aiContainer">
        //<img id="image1" src='http://files.parse.com/27b14e01-71d9-4ec2-aa04-3305d7bdf16b/7e3660ff-6fde-475f-9ef1-4d791e8560e0-cropped' />
        //<img id="frame1" src='pics/photo_template.png'/>
        //<p id="textOnFrame1">Broholm Slot. Litografi. Foto: Odense Bys Museer</p>
        //<div id="avatar">
        //<img id="avatar1" src='pics/hca1.png'/>
        //<img id="bubble" src='pics/speech_bubble_play.png' onclick="start()"/>
        //</div>
        //</div>
        StringBuilder sb = new StringBuilder();
        sb.append("<br> <div id=\"aiContainer\">");
        sb.append("<img id=\"image1\" src='").append(url).append("'/>");
        sb.append("<img id=\"frame1\" src='file:///android_asset/pics/photo_template.png' onclick=\"image.performClick();\"/>");
        Log.d(TAG, "image Title " + imageTitle);
        if (imageTitle != null) {
            //<p id="textOnFrame1">Broholm Slot. Litografi. Foto: Odense Bys Museer</p>
            sb.append("<p id=\"textOnFrame1\">").append(imageTitle).append("</p>");
        }
        sb.append(formAvatarString(poi, true));
        sb.append("</div> <br> ");

        Log.d(TAG, sb.toString());
        return sb.toString();

    }

    private String formVideoString(String youtubeVideoId, String imageTitle) {
        Log.d(TAG, "formVideoString");
        String imageUrl = "http://img.youtube.com/vi/" + youtubeVideoId + "/0.jpg";
        //url = "file:///android_asset/pics/video_image.jpg";
        //filePath = "/android_asset/broholm.jpg";
        //<div style="position: relative; left: 0; top: 0;">
        //<img id="image1" src='../pics/broholm.jpg' />
        //<img id="frame1" src='../pics/photo_template.png'/>
        //</div>
        StringBuilder sb = new StringBuilder("<br><div style=\"position: relative; left: 0; top: 0;\" >");
        sb.append("<img id=\"image2\" src='").append(imageUrl).append("'/>");
        sb.append("<img id=\"frame2\" src='file:///android_asset/pics/video_template_play.png' onclick=\"play.performClick();\"/>");
        if (imageTitle != null) {
            //<p id="textOnFrame1">Broholm Slot. Litografi. Foto: Odense Bys Museer</p>
            sb.append("<p id=\"textOnFrame2\">").append(imageTitle).append("</p>");
        }
        sb.append("</div><br>");

        Log.d(TAG, sb.toString());
        return sb.toString();

    }

    private String formAvatarString(POIEntity poi, boolean withImage) {
        Log.d(TAG, "form avatar string");
        String avatarId = poi.getAvatarObjectId();
        if (avatarId == null) {
            Log.d(TAG, "no avatar for " + poi.getName());
            //play();
            return "";
        }
        boolean audio = (poi.getAudioUrl()!=null);


        String relativeName = FileUtils.getRelativeName(avatarId, FileUtils.AVATAR_1);
        avatar1InputStream = FileUtils.getWebResource(relativeName, getActivity());
        if (avatar1InputStream != null) {
            avatarImage1 = AVATAR1_SCHEME + ":///" + relativeName;
            Log.d(TAG, "avatar image 1 " + avatarImage1);
        }
        relativeName = FileUtils.getRelativeName(avatarId, FileUtils.AVATAR_2);
        avatar2InputStream = FileUtils.getWebResource(relativeName, getActivity());
        if (avatar2InputStream != null) {
            avatarImage2 = AVATAR2_SCHEME + ":///" + relativeName;
            Log.d(TAG, "avatar image 2 " + avatarImage2);
        }
        relativeName = FileUtils.getRelativeName(avatarId, FileUtils.AVATAR_3);
        avatar3InputStream = FileUtils.getWebResource(relativeName, getActivity());
        if (avatar3InputStream != null) {
            avatarImage3 = AVATAR3_SCHEME + ":///" + relativeName;
            Log.d(TAG, "avatar image 3 " + avatarImage3);
        }
        if (avatarImage1 != null) {
            avatarCount++;
        }
        if (avatarImage2 != null) {
            avatarCount++;
        }
        if (avatarImage3 != null) {
            avatarCount++;
        }
        if (avatarCount == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        Log.d(TAG, "avatar on image " + withImage);
        //<div id="avatar">
        //<img id="avatar1" src='pics/hca1.png'/>
        //<img id="bubble" src='pics/speech_bubble_play.png' onclick="start()"/>
        //</div>
        if (withImage) {
            //<div id="avatar">
            //<img id="avatar1" src='pics/hca1.png'/>
            //<img id="bubble" src='pics/speech_bubble_play.png' onclick="start()"/>
            //</div>

            sb.append("<div id=\"avatar\" >");

            sb.append("<img id=\"avatar1\" src='").append(avatarImage1).append("'/>");
            if (audio) {
                sb.append("<img id=\"bubble\" src='file:///android_asset/pics/speech_bubble_play.png' onclick=\"start()\"/>");
            }
            sb.append("</div>");

        } else {
            //<div id="aiContainer">
            //<div id="avatar">
            //<img id="avatar1" src='pics/hca1.png'/>
            //<img id="bubble" src='pics/speech_bubble_play.png' onclick="start()"/>
            //</div>
            //</div>
            sb.append("<div id=\"aiContainer\" >"); //style="height:0 auto !important;"
            sb.append("<div id=\"avatar\" >");
            sb.append("<img id=\"avatar1noimg\" src='").append(avatarImage1).append("'/>");
            if (audio) {
                sb.append("<img id=\"bubble\" src='file:///android_asset/pics/speech_bubble_play.png' onclick=\"start()\"/>");
            }
            sb.append("</div>");
            sb.append("</div>");
        }

        return sb.toString();
    }

    @Override
    public void onDestroy() {
        imageInputStream = null;
        super.onDestroy();
    }



    private void play() {
        if (poi.getAudioUrl() == null) {
            return;
        }
        Log.d(TAG, "audrio url " + poi.getAudioUrl());
        try {
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        Log.d(TAG, "error " + what + " extra " + extra);
                        mp.release();
                        return false;
                    }
                });
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

            }

            AssetFileDescriptor descriptor = FileUtils.getMediaFileDescriptor(FileUtils.AUDIO, poi.getObjectId(), getActivity());
            mediaPlayer.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
            descriptor.close();

            mediaPlayer.prepare();
            mediaPlayer.setVolume(1f, 1f);
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopPlaying() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public void onDetach() {
        UIUtils.hideProgressBar(getActivity());
        super.onDetach();
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
        FlurryAgent.endTimedEvent(getString(R.string.flurry_poi_content_fragment));
    }

    private void initFragments(Cursor data) {
        int count = 1;
        boolean quiz = false;
        boolean practical = false;

        if (data.getString(9) != null && data.getString(9).length() > 0) {
            count++;
            quiz = true;
        }
        //check if facts is available
        if (data.getString(10) != null && data.getString(10).length() > 0) {
            count++;
            practical = true;
        }
        POIDetailsActivity a = (POIDetailsActivity) getActivity();
        mTabCount = count;
        mHasQuiz = quiz;
        refreshSideBar();
        if (practical) {
            a.addPracticalFragment(count, quiz, data.getString(10));
        }
        if (quiz) {
            a.addQuizFragment(count);
        }

    }

    private LoaderManager.LoaderCallbacks<Cursor> mLoaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {

            switch (id) {
                case LOADER_ID_POI_CONTENT:
                    String poiId = args.getString(EXTRA_POI);
                    return ProviderMethods.getPointContentEntry(getActivity(), POIEntity.COLUMNS_FOR_POI_CONTENT, poiId, null);
                case LOADER_ID_POI_ROUTE_POINT:
                    poiId = args.getString(EXTRA_POI);
                    return ProviderMethods.getRoutePointEntry(getActivity(), new String[]{HisContract.POIEntry.TABLE_NAME + ".objectId",
                            HisContract.RoutePointEntry.TABLE_NAME + ".objectId"}, poiId, null);
                case LOADER_ID_ROUTE_POINT_POI_COUNT:
                    return ProviderMethods.getPoisOfRoute(getActivity(), poi.getRoutePointId(), new String[]{
                                    HisContract.POIEntry.TABLE_NAME + ".objectId"},
                            null, null, null);
                case LOADER_ID_ROUTE_AVATAR:
                    poiId = args.getString(EXTRA_POI);
                    return ProviderMethods.getRouteEntryByPOI(getActivity(), new String[]{HisContract.RouteEntry.TABLE_NAME + "." + RouteColumns.COLUMN_AVATAR}, poiId, null);
            }
            return null;

        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            switch (loader.getId()) {
                case LOADER_ID_POI_CONTENT:
                    data.moveToFirst();
                    if (data.getCount() > 0) {
                        initFragments(data);
                        data.moveToFirst();
                        poi = POIEntity.newInstanceForContent(data);
                        Map<String, String> props = new HashMap<>();
                        props.put(getString(R.string.flurry_property_poi), poi.getName());
                        FlurryAgent.logEvent(getString(R.string.flurry_poi_content_fragment), props, true);
                        Log.d(TAG, "poi avatar object id " + poi.getAvatarObjectId());
                        if (poi.getAvatarObjectId() == null) {
                            getLoaderManager().initLoader(LOADER_ID_ROUTE_AVATAR, getArguments(), mLoaderCallbacks);
                        } else {
                            getLoaderManager().initLoader(LOADER_ID_POI_ROUTE_POINT, getArguments(), mLoaderCallbacks);
                        }
                    }
                    break;
                case LOADER_ID_POI_ROUTE_POINT:
                    if (data.getCount() > 0) {
                        data.moveToFirst();
                        String objectId = data.getString(0);
                        String routePointId = data.getString(1);
                        Log.d(TAG, "routepoint id is " + routePointId);
                        poi.setRoutePointId(routePointId);
                        getLoaderManager().initLoader(LOADER_ID_ROUTE_POINT_POI_COUNT, null, mLoaderCallbacks);
                    } else {
                        initData();
                    }
                    break;
                case LOADER_ID_ROUTE_POINT_POI_COUNT:
                    int count = data.getCount();
                    Log.d(TAG, "count for routepoint " + poi.getRoutePointId() + " is " + count);
                    if (count > 0) {
                        routePointEntity = new RoutePointEntity();
                        routePointEntity.setPoiCount(count);
                        routePointEntity.setObjectId(poi.getRoutePointId());
                    }
                    initData();
                    break;
                case LOADER_ID_ROUTE_AVATAR:
                    if (data.getCount() > 0) {
                        data.moveToFirst();
                        String avatarObjectId = data.getString(0);
                        Log.d(TAG, "route avatar object Id " + avatarObjectId);
                        poi.setAvatarObjectId(avatarObjectId);
                    }
                        getLoaderManager().initLoader(LOADER_ID_POI_ROUTE_POINT, getArguments(), mLoaderCallbacks);
                    break;

            }

        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    };

    private void initData() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;

        Log.d(TAG, "poi is " + mPoiId);
        Log.d(TAG, "dpWidth " + dpWidth);
        Log.d(TAG, "densitydpi  is " + displayMetrics.densityDpi);
        String pageUrl = "html/poi_600.html";
        if (dpWidth < 600.0f) {
            pageUrl = "html/poi_360.html";
        }
        if (dpWidth <= 320.0f && displayMetrics.densityDpi <= DisplayMetrics.DENSITY_HIGH) {
            pageUrl = "html/poi_320_hdpi.html";
        }

        Log.d(TAG, "pageUrl " + pageUrl);
        boolean startOnload = MapPreferences.isPlaySound(getActivity());
        ((WebView) getView().findViewById(R.id.detailViewWebView)).loadDataWithBaseURL(null, getContentFromAssets(pageUrl,
                poi.getName(), formBody(poi), startOnload), "text/html", "UTF-8", null);

        mWebViewHandler.sendEmptyMessageDelayed(1, 200);
        //avatar(getView());
        //initAvatar(getView(), poi);


        Log.d(TAG, "init data routepoint id " + poi.getRoutePointId());
        if (poi.getRoutePointId() != null) {
            playRoutePointSound(poi.getRoutePointId(), poi.getObjectId());
        }

        //set that poi is visited
        MapPreferences.setPoiVisited(getActivity(), mPoiId);
    }

    private void playRoutePointSound(String routePointId, String poiId) {

        Set<String> visitedList = MapPreferences.getVisitedPoisByRoutePointId(getActivity(), routePointId);
        if (visitedList == null) {
            visitedList = new HashSet<>();
        }
        Log.d(TAG, "visited list " + visitedList.size());
        if (!visitedList.contains(poiId)) {

            if (routePointEntity.getPoiCount() == 0) {
                Log.d(TAG, "routepoint po count 0 ");
                return;
            }
            float previousRatio = (float) visitedList.size() / (float) routePointEntity.getPoiCount();
            float currentRatio = (float) (visitedList.size() + 1) / (float) routePointEntity.getPoiCount();
            Log.d(TAG, "previous ratio " + previousRatio + "currentRatio " + currentRatio);
            MapPreferences.saveVisitedPoiForRoutePoint(getActivity(), routePointId, poiId);
            if (currentRatio == 1.0f) {
                if (previousRatio < 1.0f) {
                    initRoutePointTextLoader(HisContract.RoutePointContentEntry.KEY_TEXT100);
                    return;
                }
            } else if (currentRatio >= 0.25f && currentRatio < 0.5f) {
                if (previousRatio < 0.25f) {
                    initRoutePointTextLoader(HisContract.RoutePointContentEntry.KEY_TEXT25);
                    return;
                }
            } else if (currentRatio >= 0.5f && currentRatio < 0.75f) {
                if (previousRatio < 0.5f) {
                    initRoutePointTextLoader(HisContract.RoutePointContentEntry.KEY_TEXT50);
                    return;
                }
            } else if (currentRatio >= 0.75f && currentRatio < 1.0f) {
                if (previousRatio < 0.75f) {
                    initRoutePointTextLoader(HisContract.RoutePointContentEntry.KEY_TEXT75);
                    return;
                }
            }
            SoundManager.getInstance(getActivity()).playSound(SoundManager.SOUND_GET_POINT);

        }
    }

    private void initRoutePointTextLoader(String column) {
        Bundle args = new Bundle();
        args.putString(EXTRA_ROUTE_POINT_TEXT_COLUMN, column);
        getLoaderManager().initLoader(LOADER_ID_ROUTE_POINT_TEXT, args, mRoutePointTextLoaderCallbacks);
    }

    private LoaderManager.LoaderCallbacks<Cursor> mRoutePointTextLoaderCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            String column = args.getString(EXTRA_ROUTE_POINT_TEXT_COLUMN);
            switch (id) {

                case LOADER_ID_ROUTE_POINT_TEXT:
                    return ProviderMethods.getRoutePointContentsOfRoutePoint(getActivity(), poi.getRoutePointId(), new String[]{column}, null, null, null);
            }
            return null;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            switch (loader.getId()) {
                case LOADER_ID_ROUTE_POINT_TEXT:
                    if (data.getCount() > 0) {
                        data.moveToFirst();
                        String text = data.getString(0);
                        Bundle args = new Bundle();
                        args.putString(EXTRA_ROUTE_POINT_DIALOG_TEXT, text);
                        Message msg = new Message();
                        msg.what = MESSAGE_SHOW_POPUP;
                        msg.setData(args);
                        dialogHandler.sendMessage(msg);
                    }
                    break;
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    };

    private void showDialog(String text) {
        RoutePointProgressDialogFragment newFragment = RoutePointProgressDialogFragment.newInstance(text);
        newFragment.setCancelable(false);
        newFragment.show(getFragmentManager(), "dialog");
    }
}
