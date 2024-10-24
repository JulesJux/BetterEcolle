package fr.jux.bettercolloscope;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
/* loaded from: classes.dex */
class PersistentCookieStore implements CookieStore {
    private static final String SP_COOKIE_STORE = "cookieStore";
    private static final String SP_KEY_DELIMITER = "|";
    private static final String SP_KEY_DELIMITER_REGEX = "\\|";
    private static final String TAG = "PersistentCookieStore";
    private Map<URI, Set<HttpCookie>> allCookies;
    private final SharedPreferences sharedPreferences;

    /* JADX INFO: Access modifiers changed from: package-private */
    public PersistentCookieStore(Context context) {
        this.sharedPreferences = context.getSharedPreferences(SP_COOKIE_STORE, 0);
    }


    @Override // java.net.CookieStore
    public synchronized void add(URI uri, HttpCookie httpCookie) {
        URI cookieUri = cookieUri(uri, httpCookie);
        Set<HttpCookie> set = this.allCookies.get(cookieUri);
        if (set == null) {
            set = new HashSet<>();
            this.allCookies.put(cookieUri, set);
        }
        set.remove(httpCookie);
        set.add(httpCookie);
    }

    private static URI cookieUri(URI uri, HttpCookie httpCookie) {
        if (httpCookie.getDomain() != null) {
            String domain = httpCookie.getDomain();
            if (domain.charAt(0) == '.') {
                domain = domain.substring(1);
            }
            try {
                return new URI(uri.getScheme() == null ? "http" : uri.getScheme(), domain, httpCookie.getPath() == null ? "/" : httpCookie.getPath(), null);
            } catch (URISyntaxException e) {
                Log.w(TAG, e);
                return uri;
            }
        }
        return uri;
    }



    @Override // java.net.CookieStore
    public synchronized List<HttpCookie> get(URI uri) {
        return getValidCookies(uri);
    }

    @Override // java.net.CookieStore
    public synchronized List<HttpCookie> getCookies() {
        ArrayList arrayList;
        arrayList = new ArrayList();
        for (URI uri : this.allCookies.keySet()) {
            arrayList.addAll(getValidCookies(uri));
        }
        return arrayList;
    }

    private List<HttpCookie> getValidCookies(URI uri) {
        ArrayList arrayList = new ArrayList();
        for (URI uri2 : this.allCookies.keySet()) {
            if (checkDomainsMatch(uri2.getHost(), uri.getHost()) && checkPathsMatch(uri2.getPath(), uri.getPath())) {
                arrayList.addAll((Collection) Objects.requireNonNull(this.allCookies.get(uri2)));
            }
        }
        if (!arrayList.isEmpty()) {
            ArrayList arrayList2 = new ArrayList();
            Iterator it = arrayList.iterator();
            while (it.hasNext()) {
                HttpCookie httpCookie = (HttpCookie) it.next();
                if (httpCookie.hasExpired()) {
                    arrayList2.add(httpCookie);
                    it.remove();
                }
            }
            if (!arrayList2.isEmpty()) {
                removeFromPersistence(uri, arrayList2);
            }
        }
        return arrayList;
    }

    private boolean checkDomainsMatch(String str, String str2) {
        if (!str2.equals(str)) {
            if (!str2.endsWith("." + str)) {
                return false;
            }
        }
        return true;
    }

    private boolean checkPathsMatch(String str, String str2) {
        if (str2.equals(str)) {
            return true;
        }
        if (str2.startsWith(str) && str.charAt(str.length() - 1) == '/') {
            return true;
        }
        return str2.startsWith(str) && str2.substring(str.length()).charAt(0) == '/';
    }

    private void removeFromPersistence(URI uri, List<HttpCookie> list) {
        SharedPreferences.Editor edit = this.sharedPreferences.edit();
        Iterator<HttpCookie> it = list.iterator();
        while (it.hasNext()) {
            edit.remove(uri.toString() + SP_KEY_DELIMITER + it.next().getName());
        }
        edit.apply();
    }

    @Override // java.net.CookieStore
    public synchronized List<URI> getURIs() {
        return new ArrayList(this.allCookies.keySet());
    }

    @Override // java.net.CookieStore
    public synchronized boolean remove(URI uri, HttpCookie httpCookie) {
        boolean z;
        Set<HttpCookie> set = this.allCookies.get(uri);
        z = set != null && set.remove(httpCookie);
        if (z) {
            removeFromPersistence(uri, httpCookie);
        }
        return z;
    }

    private void removeFromPersistence(URI uri, HttpCookie httpCookie) {
        SharedPreferences.Editor edit = this.sharedPreferences.edit();
        edit.remove(uri.toString() + SP_KEY_DELIMITER + httpCookie.getName());
        edit.apply();
    }

    @Override // java.net.CookieStore
    public synchronized boolean removeAll() {
        this.allCookies.clear();
        removeAllFromPersistence();
        return true;
    }

    private void removeAllFromPersistence() {
        this.sharedPreferences.edit().clear().apply();
    }
}
