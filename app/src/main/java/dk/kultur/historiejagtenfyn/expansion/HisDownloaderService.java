package dk.kultur.historiejagtenfyn.expansion;

import com.google.android.vending.expansion.downloader.impl.DownloaderService;

/**
 * dk.kultur.historiejagtenfyn.expansion
 * Created by juskrt on 3/3/2015.
 */
public class HisDownloaderService extends DownloaderService {
    // stuff for LVL -- MODIFY FOR YOUR APPLICATION!
    private static final String BASE64_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAin+rt6UXfRyKP9U1PJUFzY+npyY02xDtno/elc0FDn3xTeEZ3CraEzXU9zDmGMBpnoOj50NxsWM4On8iFX5sSQIrgaWlKyyhgjgvMbcA6rrXorwDDp8ReqVTRswzw2CQbCANeQXuDxlMun3o80vYabz0qlDj9RP73NH2P648A9Ey7Beee+UqHZKuxViA1HsNNsdyKvjUoy4jnkQWuphD8B+G80i9e9WxpbpDenS227xAXUF5PBxVK1OlxM9+ejpw6jzQJEk7hlsKEx7qF5c9U3bAysBfIegq+rzyjkgj+4IzJqWiCZ3XGNILP/5zlHaczyz0iUEmjOs/Ert39hfioQIDAQAB";
    // used by the preference obfuscater
    private static final byte[] SALT = new byte[]{
            1, 43, -12, -1, 54, 98, -100, -12, 43, 2, -8, -4, 9, 5, -106, -108, -33, 45, -1, 84
    };

    /**
     * This public key comes from your Android Market publisher account, and it
     * used by the LVL to validate responses from Market on your behalf.
     */
    @Override
    public String getPublicKey() {
        return BASE64_PUBLIC_KEY;
    }

    /**
     * This is used by the preference obfuscater to make sure that your
     * obfuscated preferences are different than the ones used by other
     * applications.
     */
    @Override
    public byte[] getSALT() {
        return SALT;
    }

    /**
     * Fill this in with the class name for your alarm receiver. We do this
     * because receivers must be unique across all of Android (it's a good idea
     * to make sure that your receiver is in your unique package)
     */
    @Override
    public String getAlarmReceiverClassName() {
        return HisDownloaderService.class.getName();
    }

}