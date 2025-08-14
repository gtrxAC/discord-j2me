/**
 * Automatic version recommendation system for home page
 */

// the "main" links are ones that are all shown when visiting on a modern browser
// so there's one for each actual downloadable version
const mainVersionDownloadLinks = {
    midp2: `<p>5.0 for Nokia S40v3+ and Symbian S60v3+ <br/> <a href="/discord_midp2.jad">JAD</a> - <a href="/discord_midp2.jar">JAR</a></p>`,
    nokia_128px: `<p>5.0 for Nokia S40v3+ (128x160) <br/> <a href="/discord_nokia_128px.jad">JAD</a> - <a href="/discord_nokia_128px.jar">JAR</a></p>`,
    s40v2: `<p>5.0 for Nokia S40v2 <br/> <a href="/discord_s40v2.jad">JAD</a> - <a href="/discord_s40v2.jar">JAR</a></p>`,
    s60v2: `<p>5.0 for Symbian S60v2 <br/> <a href="/discord_s60v2.jad">JAD</a> - <a href="/discord_s60v2.jar">JAR</a></p>`,
    midp2_alt_other: `<p>5.0 for other MIDP2 devices <br/> <a href="/discord_midp2_alt.jad">JAD</a> - <a href="/discord_midp2_alt.jar">JAR</a></p>`,
    blackberry: `<p>5.0 for BlackBerry <br/> <a href="/discord_blackberry.jad">JAD</a> - <a href="/discord_blackberry.jar">JAR</a></p>`,
    samsung: `<p>5.0 for Samsung <br/> <a href="/discord_samsung.jad">JAD</a> - <a href="/discord_samsung.jar">JAR</a></p>`,
    samsung_100kb: `<p>5.0 for Samsung (100 kB version) <br/> <a href="/discord_samsung_100kb.jad">JAD</a> - <a href="/discord_samsung_100kb.jar">JAR</a></p>`,
    lg: `<p>5.0 for LG <br/> <a href="/discord_lg.jad">JAD</a> - <a href="/discord_lg.jar">JAR</a></p>`,
    jl: `<p>5.0 for J2ME Loader <br/> <a href="/discord_jl.jar">JAR</a></p>`,
    _6310i: `<p>3.2 for Nokia 3410/6310i (30 kB) <br/> <a href="/discord_6310i.jad">JAD</a> - <a href="/discord_6310i.jar">JAR</a></p>`,
    midp1: `<p>3.0 for MIDP1 <br/> <a href="/discord_midp1.jad">JAD</a> - <a href="/discord_midp1.jar">JAR</a></p>`
}

// the "other" links are only shown on specific devices and only differ from the "main" ones by the label shown in the HTML
const otherVersionDownloadLinks = {
    midp2_alt: `<p>5.0 for MIDP2 <br/> <a href="/discord_midp2_alt.jad">JAD</a> - <a href="/discord_midp2_alt.jar">JAR</a></p>`,
    midp2_s40: `<p>5.0 for Nokia S40v3+ <br/> <a href="/discord_midp2.jad">JAD</a> - <a href="/discord_midp2.jar">JAR</a></p>`,
    midp2_symbian: `<p>5.0 for Symbian S60v3+ <br/> <a href="/discord_midp2.jad">JAD</a> - <a href="/discord_midp2.jar">JAR</a></p>`,
    JL_INFO: `<p>You can try Discord J2ME on your Android device by downloading the JAR below. You will need the J2ME Loader app installed.</p>`,
    JL_INFO_2: `<p>If you want to load the app onto an older device, <a href="/all">choose another version here</a>.</p>`,
    SHOW_ALL: `<p><a href="/all">See all versions</a></p>`
}

// Choose which HTML snippets (from above lists) will be shown to the user
function getRecommendedVersionsArray(req) {
    const ua = (req.headers['device-stock-ua'] ?? req.headers['x-operamini-phone-ua'] ?? req.headers['user-agent'] ?? '').toLowerCase();
    if (!ua) {
        return ["midp2_alt", "_6310i", "midp1", "SHOW_ALL"];
    }
    if (ua.includes('android')) {
        return ["JL_INFO", 'jl', "JL_INFO_2"];
    }

    const midp2 = /midp\W*2/g.test(ua) || ua.includes('bada');
    const midp1 = /midp\W*1/g.test(ua);

    if (!midp2 && midp1) {
        return ["RECOMMENDED", "_6310i", "midp1", "SHOW_ALL"];
    }
    if (ua.includes('blackberry')) {
        return ["RECOMMENDED", 'blackberry', "SHOW_ALL"];
    }
    if (/samsung|gt\-|sgh|sch|sph/g.test(ua)) {
        if (/c3060|m300/g.test(ua)) return ["RECOMMENDED", "samsung_100kb", "SHOW_ALL"];
        if (midp2) return ["RECOMMENDED", "samsung", "samsung_100kb", "SHOW_ALL"];
        return ["RECOMMENDED", "samsung", "samsung_100kb", "_6310i", "midp1", "SHOW_ALL"];
    }
    if (/series60\/2/g.test(ua)) {
        return ["RECOMMENDED", "s60v2", "SHOW_ALL"];
    }
    if (/symbian|series60/g.test(ua)) {
        if (midp2) return ["RECOMMENDED", "midp2_symbian", "SHOW_ALL"];
        return ["RECOMMENDED", "midp2_symbian", "_6310i", "midp1", "SHOW_ALL"];
    }
    if (ua.includes('lg')) {
        if (midp2) return ["RECOMMENDED", 'lg', "SHOW_ALL"];
        return ["RECOMMENDED", "lg", "_6310i", "midp1", "SHOW_ALL"];
    }
    if (/^nokia(2855|315|322|507|514|602|6030|60[67]|6085|610|615[25]|6170|623[05]|6255|68|72[67]|736|880\d\/)/g.test(ua)) {
        return ["RECOMMENDED", "s40v2", "midp2_alt", "SHOW_ALL"];
    }
    if (/^nokia(168|2220|23[23]|26|27[26]|2865|310|3110|350|520|608[56]|6111|6125|6136|6151|616|707|c1|c2-00)/g.test(ua)) {
        return ["RECOMMENDED", "nokia_128px", "midp2_alt", "SHOW_ALL"];
    }
    if (midp2) {
        if (ua.includes('nokia')) return ["RECOMMENDED", "midp2_s40", "SHOW_ALL"];
        return ["RECOMMENDED", "midp2_alt", "SHOW_ALL"];
    }
    if (/linux|mac|windows/g.test(ua) && !/windows (ce|mobile)/g.test(ua)) {
        // modern device: show all downloads
        return Object.keys(mainVersionDownloadLinks);
    }
    return ["midp2_alt", "_6310i", "midp1", "SHOW_ALL"];
}

function getRecommendedVersions(req) {
    const versions = getRecommendedVersionsArray(req);
    const recommendedMultiple = versions.filter(v => v != "RECOMMENDED" && v != "SHOW_ALL").length != 1;

    return versions
        .map(ver => {
            if (ver == "RECOMMENDED") return `<p>Recommended version${recommendedMultiple ? 's' : ''} for your device:</p>`;

            return mainVersionDownloadLinks[ver] ?? otherVersionDownloadLinks[ver] ?? `<p>Unknown version: ${ver}. Please report this to developers.</p>`
        })
        .join('')
}

module.exports = {
    mainVersionDownloadLinks,
    otherVersionDownloadLinks,
    getRecommendedVersionsArray,
    getRecommendedVersions
}