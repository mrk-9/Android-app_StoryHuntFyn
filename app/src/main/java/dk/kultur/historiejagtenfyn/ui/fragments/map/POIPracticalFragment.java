package dk.kultur.historiejagtenfyn.ui.fragments.map;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.flurry.android.FlurryAgent;

import java.io.InputStream;

import dk.kultur.historiejagtenfyn.R;
import dk.kultur.historiejagtenfyn.data.entities.POIEntity;
import dk.kultur.historiejagtenfyn.data.parse.contracts.POIContentHisContract;
import dk.kultur.historiejagtenfyn.data.sql.HisContract;
import dk.kultur.historiejagtenfyn.ui.activities.ImageActivity;
import dk.kultur.historiejagtenfyn.ui.util.FileUtils;
import dk.kultur.historiejagtenfyn.ui.util.UIUtils;

/**
 * Created by Lina on 2015.02.11
 */
public class POIPracticalFragment extends AbsFragmentWithSideTabView {


    private static final String TAG = POIPracticalFragment.class.getSimpleName();

    private static String EXTRA_POI_FACTS = "EXTRA_POI_FACTS";
    private static final String EXTRA_POI_OBJECTID = "EXTRA_POI_OBJECTID";

    private String mPoiFacts = "";
    private String mPoiId = null;
    private InputStream imageInputStream;

    public static POIPracticalFragment newInstance(String poiFacts, int count, boolean hasQuiz, int activeTab, String poiId) {
        POIPracticalFragment f = new POIPracticalFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_POI_FACTS, poiFacts);
        args.putString(EXTRA_POI_OBJECTID, poiId);
        initArgs(args, count, hasQuiz, activeTab);

        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mPoiFacts = getArguments().getString(EXTRA_POI_FACTS, "");
        mPoiId = getArguments().getString(EXTRA_POI_OBJECTID);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = super.onCreateView(inflater, container, savedInstanceState);

        WebView webView = (WebView) layout.findViewById(R.id.practicalWebView);
        webView.setBackgroundColor(0x00000000);


        //Next to statements remove strange webview black blinking when scrolling
        //http://stackoverflow.com/questions/17315815/strange-webview-black-blinking-when-scrolling
        webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        webView.getSettings().setJavaScriptEnabled(true);

        //http://stackoverflow.com/questions/4065312/detect-click-on-html-button-through-javascript-in-android-webview
        webView.addJavascriptInterface(new Object() {
            @JavascriptInterface           // For API 17+
            public void performClick() {
                Intent intent = new Intent(getActivity(), ImageActivity.class);
                intent.putExtra(ImageActivity.EXTRA_IMAGE_PATH, mPoiId + FileUtils.FACTS_IMAGE);
                startActivity(intent);
            }
        }, "image");


        initData(webView);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                UIUtils.hideProgressBar(getActivity());

            }

            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
            }

            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                Log.d(TAG, "should intercept url " + request.getUrl().toString());
                if (Uri.parse(request.getUrl().toString()).getScheme().equals(IMG_SCHEME)) {
                    return new WebResourceResponse("image/*", "base64", imageInputStream);
                }
                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                Log.d(TAG, "should intercept url " + url);
                if (Uri.parse(url).getScheme().equals(IMG_SCHEME)) {
                    return new WebResourceResponse("image/*", "base64", imageInputStream);
                }
                return super.shouldInterceptRequest(view, url);
            }
        });


        return layout;
    }


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_poi_practical;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        FlurryAgent.logEvent(getString(R.string.flurry_poi_practical_info_fragment), true);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        FlurryAgent.endTimedEvent(getString(R.string.flurry_poi_practical_info_fragment));
    }


    private void initData(WebView webView) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;

        String pageUrl = "html/poi_practical_600.html";
        if (dpWidth < 600.0f) {
            pageUrl = "html/poi_practical_360.html";
        }
        if (dpWidth <= 320.0f && displayMetrics.densityDpi <= DisplayMetrics.DENSITY_HIGH) {
            pageUrl = "html/poi_practical_320_hdpi.html";
        }

        Log.d(TAG, "pageUrl " + pageUrl);

        String pageContent = getContentFromAssets(pageUrl,
                getResources().getString(R.string.practical_info_header), formBody(mPoiFacts), false);


        webView.loadDataWithBaseURL("", pageContent, "text/html", "UTF-8", "");


    }

    private String formBody(String body) {


        final String relativeName = FileUtils.getRelativeName(mPoiId, FileUtils.FACTS_IMAGE);
        String imageUrl = null;
        imageInputStream = FileUtils.getWebResource(relativeName, getActivity());
        if (imageInputStream != null) {
            imageUrl = IMG_SCHEME + ":///" + relativeName;
        }

        StringBuilder total = new StringBuilder(body);
        int index = total.indexOf(POIContentFragment.HTML_PLACEHOLDER_IMAGE);
        Log.d(TAG, "form body img placeholder index " + index);

        if (index > -1) {
            String imgString = formImageString(imageUrl, "some title");
            total.insert(index + POIContentFragment.HTML_PLACEHOLDER_IMAGE.length(), "<div id=\"anotherTxt\">");

            total.append("</div>");
            total.replace(index, index + POIContentFragment.HTML_PLACEHOLDER_IMAGE.length(), imgString);

        }
        Log.d(TAG, "body " + total.toString());
        return total.toString();

    }

    private String formImageString(String url, String imageTitle) {
        Log.d(TAG, "form image string url " + url);
        if (url == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<br> <div id=\"aiContainer\">");
        sb.append("<img id=\"image1\" src='").append(url).append("'/>");
        sb.append("<img id=\"frame1\" src='file:///android_asset/pics/photo_template.png' onclick=\"image.performClick();\"/>");
        Log.d(TAG, "image Title " + imageTitle);
        if (imageTitle != null) {
            sb.append("<p id=\"textOnFrame1\">").append(imageTitle).append("</p>");
        }
        sb.append("</div> <br> ");

        Log.d(TAG, sb.toString());
        return sb.toString();

    }
}
