package com.huangjie.demo.util;

import java.util.regex.Pattern;

/**
 * Created by huangjie on 2017/6/7.
 */

public class UrlUtils {

    public static final String SCHEME_HTTP = "http://";
    public static final String SCHEME_ASSET = "assets://";


    public static final String IRI_CHAR =
            "a-zA-Z0-9\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF";

    public static final String TOP_LEVEL_DOMAIN_STR_FOR_WEB_URL = "(?:" +
            "(?:aero|arpa|asia|a[cdefgilmnoqrstuwxz])|" +
            "(?:biz|b[abdefghijmnorstvwyz])|" +
            "(?:cat|com|coop|club|c[acdfghiklmnoruvxyz])|" +
            "d[ejkmoz]|(?:edu|e[cegrstu])|f[ijkmor]|" +
            "(?:gov|g[abdefghilmnpqrstuwy])|" +
            "h[kmnrtu]|(?:info|int|i[delmnoqrst])|" +
            "(?:jobs|j[emop])|" +
            "k[eghimnprwyz]|" +
            "l[abcikrstuvy]|" +
            "(?:moe|men|mil|mobi|museum|m[acdeghklmnopqrstuvwxyz])|" +
            "(?:name|net|n[acefgilopruz])|" +
            "(?:org|om|win|work)|" +
            "(?:pub|pro|p[aefghklmnrstwy])|" +
            "qa|" +
            "r[eosuw]|" +
            "s[abcdeghijklmnortuvyz]|space|von|pub|" +
            "(?:tel|travel|top|t[cdfghjklmnoprtvwz])|" +
            "u[agksyz]|v[aceginu]|" +
            "w[fs]|wang|world|lol|link|re[dn]|kim|one|range|site|yoga|bid|vip|" +
            "xyz|" +
            "(?:δοκιμή|ол╣Щ|испытание|рф|срб|טעסט|آزمایشی|إختبار|الاردن|الجزائر|السعودية|المغرب|امارات|بھارت|تونس|سورية|فلسطين|قطر|مصر|परीक्षा|भारत|ভারত|ਭਾਰਤ|ભારત|இந்தியா|இலங்கை|சிங்கப்பூர்|பரிட்சை|భారత్|ලංකා|ไทย|テスト|佛山|慈善|集团|在线|八卦|公益|公司|移动|我爱你|时尚|淡马锡|商标|商店|商城|新闻|中文网|中信|娱乐|谷歌|网店|网络|手机|政府|机构|组织机构|世界|网址|游戏|企业|广东|政务|中国|中國|台湾|台灣|新加坡|测试|測試|香港|테스트|한국" +
            "|xn\\-\\-0zwm56d|xn\\-\\-11b5bs3a9aj6g|xn\\-\\-3e0b707e|xn\\-\\-45brj9c|xn\\-\\-80akhbyknj4f|" +
            "xn\\-\\-90a3ac|xn\\-\\-9t4b11yi5a|xn\\-\\-clchc0ea0b2g2a9gcd|xn\\-\\-deba0ad|" +
            "xn\\-\\-fiqs8s|xn\\-\\-fiqz9s|xn\\-\\-fpcrj9c3d|xn\\-\\-fzc2c9e2c|xn\\-\\-g6w251d|" +
            "xn\\-\\-gecrj9c|xn\\-\\-h2brj9c|xn\\-\\-hgbk6aj7f53bba|xn\\-\\-hlcj6aya9esc7a|" +
            "xn\\-\\-j6w193g|xn\\-\\-jxalpdlp|xn\\-\\-kgbechtv|xn\\-\\-kprw13d|xn\\-\\-kpry57d|" +
            "xn\\-\\-lgbbat1ad8j|xn\\-\\-mgbaam7a8h|xn\\-\\-mgbayh7gpa|xn\\-\\-mgbbh1a71e|" +
            "xn\\-\\-mgbc0a9azcg|xn\\-\\-mgberp4a5d4ar|xn\\-\\-o3cw4h|xn\\-\\-ogbpf8fl|" +
            "xn\\-\\-p1ai|xn\\-\\-pgbs0dh|xn\\-\\-s9brj9c|xn\\-\\-wgbh1c|xn\\-\\-wgbl6a|xn\\-\\-xkc2al3hye2a|xn\\-\\-xkc2dl3a5ee0h|xn\\-\\-yfro4i67o|xn\\-\\-ygbi2ammx|xn\\-\\-zckzah|xxx)|y[et]|z[amw]))";

    public static final Pattern WEB_URL = Pattern
            .compile(
                    "((?:(http|https|Http|Https|rtsp|Rtsp):\\/\\/(?:(?:[a-zA-Z0-9\\$\\-\\_\\.\\+\\!\\*\\'\\(\\)"
                            + "\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,64}(?:\\:(?:[a-zA-Z0-9\\$\\-\\_"
                            + "\\.\\+\\!\\*\\'\\(\\)\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,25})?\\@)?)?"
                            + "((?:(?:[" + IRI_CHAR + "][" + IRI_CHAR + "\\-]{0,64}\\.)+" // named
                            // host
                            + TOP_LEVEL_DOMAIN_STR_FOR_WEB_URL
                            + "|(?:(?:25[0-5]|2[0-4]" // or ip address
                            + "[0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(?:25[0-5]|2[0-4][0-9]"
                            + "|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(?:25[0-5]|2[0-4][0-9]|[0-1]"
                            + "[0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(?:25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}"
                            + "|[1-9][0-9]|[0-9])))"
                            + "(?:\\:\\d{1,5})?)" // plus option port number
                            + "(\\/(?:(?:[" + IRI_CHAR + "\\;\\/\\?\\:\\@\\&\\=\\#\\~" // plus
                            // option
                            // query
                            // params
                            + "\\-\\.\\+\\!\\*\\'\\(\\)\\,\\_\\|\\$])|(?:\\%[a-fA-F0-9]{2}))*)?" // added '\\|' to enable | in query param
                            + "(?:\\b|$)");

    public static String reformatUrl(String url) {
        // todo(feldstein): This needs to be beefed up and
        // ensure it still supports about:crash et al.
        if (url == null || url.trim().length() == 0)
            return url;

        if (url.indexOf("://") == -1 && !url.startsWith("about:"))
            url = "http://" + url;
        return url;
    }
}
