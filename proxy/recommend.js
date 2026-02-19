/**
 * Automatic version recommendation system for home page
 */

const versionDownloadLinks = {
    "midp2":               { version: "5.2", betaVersion: "5.3", target: "Symbian S60v3 and up", tls: "via system-level TLS", tlsLink: "s60" },
    "s40v3":               { version: "5.2", betaVersion: "5.3", target: "Nokia S40v3 and up" },
    "midp2_alt_tls":       { version: "5.2", betaVersion: "5.3", target: "Nokia S40v3 and up", tls: "via Java-based TLS", tlsLink: "s40" },
    "phoneme_android":     { version: "5.2", betaVersion: "5.3", target: "phoneME", tls: true, showJar: false, file: "midp2_alt_tls" },
    "nokia_128px":         { version: "5.2", betaVersion: "5.3", target: "Nokia S40v3 and up (128x160)" },
    "nokia_128px_tls":     { version: "5.2", betaVersion: "5.3", target: "Nokia S40v3 and up (128x160)", tls: "via Java-based TLS", tlsLink: "s40" },
    "s60v2":               { version: "5.2", betaVersion: "5.3", target: "Symbian S60v2" },
    "s40v2":               { version: "5.2", betaVersion: "5.3", target: "Nokia S40v2" },
    "midp2_alt":           { version: "5.2", betaVersion: "5.3", target: "other MIDP2 devices" },
    "midp2_alt_recommend": { version: "5.2", betaVersion: "5.3", target: "MIDP2", file: "midp2_alt" },
    "blackberry":          { version: "5.2", betaVersion: "5.3", target: "BlackBerry" },
    "samsung":             { version: "5.2", betaVersion: "5.3", target: "Samsung" },
    "samsung_100kb":       { version: "5.2", betaVersion: null, target: "Samsung (100 kB version)" },
    "lg":                  { version: "5.2", betaVersion: "5.3", target: "LG" },
    "jl":                  { version: "5.2", betaVersion: "5.3", target: "J2ME Loader", tls: true, showJad: false },
    "6310i":               { version: "3.2", betaVersion: null, target: "Nokia 3410/6310i (30 kB)" },
    "midp1":               { version: "3.0", betaVersion: null, target: "MIDP1" },
}

// all downloads except those that point to another file (are aliases)
const directVersionDownloadLinks = Object.entries(versionDownloadLinks)
    .filter(ver => ver[1].file === undefined);

// other html snippets which can be included as part of recommendations (must be before or after actual version names, not in between)
const otherSnippets = {
    JL_INFO: `<p>You can try Discord J2ME on your Android device by downloading the JAR below. You will need the J2ME Loader app installed.</p>`,
    JL_INFO_2: `<p>If you want to load Discord J2ME onto an older device, <a href="/j2me/all">choose another version here</a>.</p>`,
    SHOW_ALL: `<p><a href="/j2me/all">See all versions</a></p>`,
    NO_PROXYLESS: `Direct connection is currently not available on your device.`,
    NEVER_PROXYLESS: `Direct connection is not supported on your device.`,
    DONT_KNOW_PROXYLESS: `We cannot confirm if your device supports Direct connection.`
}

function downloadLinkHtml(name, beta) {
    let obj = versionDownloadLinks[name];
    if (!obj) return null;

    if (beta && obj.betaVersion == null) return '';
    if (!beta && obj.version == null) return '';

    if (obj.file) name = obj.file;
    if (beta) name += "_beta";

    const showJad = (obj.showJad || obj.showJad === undefined);
    const showJar = (obj.showJar || obj.showJar === undefined);

    const jad = (showJad) ? `<a href="/discord_${name}.jad">JAD</a> ` : '';
    const jadJarSep = (showJad && showJar) ? ' - ' : '';
    const jar = (showJar) ? `<a href="/discord_${name}.jar">JAR</a>` : '';

    const tlsVia = (typeof obj.tls == 'string') ? `<a href="/j2me/proxyless#${obj.tlsLink}">${obj.tls}</a>` : '';
    const tlsInfo = (obj.tls) ? `<small>with Direct connection ${tlsVia}</small> <br/>` : '';

    return `<p>${beta ? (obj.betaVersion + ' beta') : obj.version} for ${obj.target} <br/> ${tlsInfo} ${jad}${jadJarSep}${jar}</p>`;
}

function arrayDownloadLinkHtml(versions) {
    const haveAnyBetas = versions
        .map(ver => versionDownloadLinks[ver]?.betaVersion)
        .some(ver => ver != null);

    return (haveAnyBetas ? `<h2>Stable versions</h2>` : '') +
        versions.map(ver => downloadLinkHtml(ver, false) ?? `<p>Unknown version: ${ver}. Please report this to developers.</p>`).join('') +
        (haveAnyBetas ? `<h2>Beta versions</h2>` : '') +
        versions.map(ver => downloadLinkHtml(ver, true)).join('');
}

// Choose which HTML snippets (from above lists) will be shown to the user
function getRecommendedVersionsArray(req) {
    const ua = (req.headers['device-stock-ua'] ?? req.headers['x-operamini-phone-ua'] ?? req.headers['user-agent'] ?? '').toLowerCase();
    if (!ua) {
        return ["DONT_KNOW_PROXYLESS", "midp2_alt", "6310i", "midp1", "SHOW_ALL"];
    }
    if (ua.includes('bb10')) {
        return ["RECOMMENDED", "jl", "SHOW_ALL"]
    }
    if (ua.includes('android')) {
        // return ["JL_INFO", 'jl', "JL_INFO_2"];  // won't use this for now cuz j2me loader-specific info would be shown in the google search result's description
        return ["RECOMMENDED", "jl", "phoneme_android", "SHOW_ALL"]
    }

    const midp2 = /midp\W*2/g.test(ua) || ua.includes('bada');
    const midp1 = /midp\W*1/g.test(ua);

    if (!midp2 && midp1) {
        return ["NEVER_PROXYLESS", "RECOMMENDED", "6310i", "midp1", "SHOW_ALL"];
    }
    if (ua.includes('blackberry')) {
        return ["NO_PROXYLESS", "RECOMMENDED", 'blackberry', "SHOW_ALL"];
    }
    if (/samsung|gt\-|sgh|sch|sph/g.test(ua)) {
        if (/c3060|m300/g.test(ua)) return ["NEVER_PROXYLESS", "RECOMMENDED", "samsung_100kb", "SHOW_ALL"];
        if (midp2) return ["NO_PROXYLESS", "RECOMMENDED", "samsung", "samsung_100kb", "SHOW_ALL"];
        return ["NO_PROXYLESS", "RECOMMENDED", "samsung", "samsung_100kb", "6310i", "midp1", "SHOW_ALL"];
    }
    if (/series60\/2/g.test(ua)) {
        return ["NO_PROXYLESS", "RECOMMENDED", "s60v2", "SHOW_ALL"];
    }
    if (/symbian|series60/g.test(ua)) {
        if (midp2) return ["RECOMMENDED", "midp2", "SHOW_ALL"];
        return ["RECOMMENDED", "midp2", "6310i", "midp1", "SHOW_ALL"];
    }
    if (ua.includes('lg')) {
        if (midp2) return ["NO_PROXYLESS", "RECOMMENDED", 'lg', "SHOW_ALL"];
        return ["NO_PROXYLESS", "RECOMMENDED", "lg", "6310i", "midp1", "SHOW_ALL"];
    }
    if (/^nokia(2855|315|322|507|514|602|6030|60[67]|610|615[25]|6170|623[05]|6255|68|72[67]|736|880\d\/)/g.test(ua)) {
        return ["NEVER_PROXYLESS", "RECOMMENDED", "s40v2", "SHOW_ALL"];
    }
    if (/^nokia(2690|3109|3110|350|520|608[56]|6125|6136|6151|c1|c2-00)/g.test(ua)) {  // 128x with 1mb jar size
        return ["RECOMMENDED", "nokia_128px_tls", "nokia_128px", "SHOW_ALL"];
    }
    if (/^nokia(168|2220|23[23]|26|27[26]|2865|310|6111|616|707)/g.test(ua)) {  // 128x with lower jar size
        return ["NO_PROXYLESS", "RECOMMENDED", "nokia_128px", "SHOW_ALL"];
    }
    if (/^nokia(5000|7100)/g.test(ua)) {  // 240p with lower jar size
        return ["NO_PROXYLESS", "RECOMMENDED", "s40v3", "SHOW_ALL"];
    }
    if (midp2) {
        if (ua.includes('nokia')) return ["RECOMMENDED", "midp2_alt_tls", "s40v3", "SHOW_ALL"];
        return ["DONT_KNOW_PROXYLESS", "RECOMMENDED", "midp2_alt_recommend", "SHOW_ALL"];
    }
    if (/linux|mac|windows/g.test(ua) && !/windows (ce|mobile)/g.test(ua)) {
        // modern device: show all downloads except those that point to another file (are aliases)
        return directVersionDownloadLinks.map(ver => ver[0]);
    }
    return ["DONT_KNOW_PROXYLESS", "midp2_alt_recommend", "6310i", "midp1", "SHOW_ALL"];
}

function getRecommendedVersions(req) {
    let versions = getRecommendedVersionsArray(req);
    let prefix = '';
    let suffix = '';
    let showRecommendedText = false;
    let proxylessText = "";

    if (versions[0].includes("PROXYLESS")) {
        proxylessText = "<p>" + otherSnippets[versions.shift()] + ` <a href="/j2me/proxyless#unsupported">Hosting</a> your own proxy server is recommended.</p>`;
    }
    if (versions[0] == "RECOMMENDED") {
        showRecommendedText = true;
        versions.shift();
    }
    while (versions[0] in otherSnippets) {
        prefix += otherSnippets[versions.shift()];
    }
    while (versions[versions.length - 1] in otherSnippets) {
        suffix += otherSnippets[versions.pop()];
    }
    if (showRecommendedText) {
        prefix = `<b>Recommended version${(versions.length != 1) ? 's' : ''} for your device:</b>` + prefix;
    }
    prefix = proxylessText + prefix;

    return prefix + arrayDownloadLinkHtml(versions) + suffix;
}

module.exports = {
    versionDownloadLinks,
    directVersionDownloadLinks,
    downloadLinkHtml,
    arrayDownloadLinkHtml,
    getRecommendedVersionsArray,
    getRecommendedVersions
}