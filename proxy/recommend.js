/**
 * Automatic version recommendation system for home page
 */

const mainVersionDownloadLinks = {
    "midp2":            { version: "5.0", betaVersion: "5.1", target: "Nokia S40v3+ and Symbian S60v3+" },
    "nokia_128px":      { version: "5.0", betaVersion: "5.1", target: "Nokia S40v3+ (128x160)" },
    "s40v2":            { version: null, betaVersion: "5.1", target: "Nokia S40v2" },
    "s60v2":            { version: null, betaVersion: "5.1", target: "Symbian S60v2" },
    "midp2_alt":        { version: "5.0", betaVersion: "5.1", target: "other MIDP2 devices" },
    "blackberry":       { version: "5.0", betaVersion: "5.1", target: "BlackBerry" },
    "samsung":          { version: "5.0", betaVersion: "5.1", target: "Samsung" },
    "samsung_100kb":    { version: "5.0", betaVersion: "5.1", target: "Samsung (100 kB version)" },
    "lg":               { version: "5.0", betaVersion: "5.1", target: "LG" },
    "jl":               { version: "5.0", betaVersion: "5.1", target: "J2ME Loader", showJad: false },
    "6310i":            { version: "3.2", betaVersion: null, target: "Nokia 3410/6310i (30 kB)" },
    "midp1":            { version: "3.0", betaVersion: null, target: "MIDP1" },
}

// below ones are shown only on specific devices and have a different 'target' field (label text shown in html)
// these must have 'name' field which is the name of the jad/jar (object key in main versions)
const otherVersionDownloadLinks = {
    "midp2_alt_recommend":  { name: "midp2_alt", target: "MIDP2" },
    "midp2_s40":            { name: "midp2", target: "Nokia S40v3+" },
    "midp2_symbian":        { name: "midp2", target: "Symbian S60v3+" },
}

// other html snippets which can be included as part of recommendations (must be before or after actual)
const otherSnippets = {
    JL_INFO: `<p>You can try Discord J2ME on your Android device by downloading the JAR below. You will need the J2ME Loader app installed.</p>`,
    JL_INFO_2: `<p>If you want to load Discord J2ME onto an older device, <a href="/j2me/all">choose another version here</a>.</p>`,
    SHOW_ALL: `<p><a href="/j2me/all">See all versions</a></p>`
}

function downloadLinkHtml(name, beta) {
    let obj = otherVersionDownloadLinks[name] ?? mainVersionDownloadLinks[name];
    if (!obj) return null;

    let target = obj.target;
    if (obj.name) obj = mainVersionDownloadLinks[obj.name];

    if (beta && obj.betaVersion == null) return '';
    if (!beta && obj.version == null) return '';
    if (beta) name += "_beta";

    const jad = (obj.showJad || obj.showJad === undefined) ? `<a href="/discord_${name}.jad">JAD</a> - ` : '';

    return `<p>${beta ? (obj.betaVersion + ' beta') : obj.version} for ${target} <br/> ${jad}<a href="/discord_${name}.jar">JAR</a></p>`;
}

function arrayDownloadLinkHtml(versions) {
    const haveAnyBetas = versions
        .map(ver => mainVersionDownloadLinks[ver]?.betaVersion || mainVersionDownloadLinks[otherVersionDownloadLinks[ver]?.name])
        .some(ver => ver != null);

    return versions.map(ver => downloadLinkHtml(ver, false) ?? `<p>Unknown version: ${ver}. Please report this to developers.</p>`).join('') +
        (haveAnyBetas ? `<h2>Beta versions</h2>` : '') +
        versions.map(ver => downloadLinkHtml(ver, true)).join('');
}

// Choose which HTML snippets (from above lists) will be shown to the user
function getRecommendedVersionsArray(req) {
    const ua = (req.headers['device-stock-ua'] ?? req.headers['x-operamini-phone-ua'] ?? req.headers['user-agent'] ?? '').toLowerCase();
    if (!ua) {
        return ["midp2_alt", "6310i", "midp1", "SHOW_ALL"];
    }
    if (ua.includes('android')) {
        return ["JL_INFO", 'jl', "JL_INFO_2"];
    }

    const midp2 = /midp\W*2/g.test(ua) || ua.includes('bada');
    const midp1 = /midp\W*1/g.test(ua);

    if (!midp2 && midp1) {
        return ["RECOMMENDED", "6310i", "midp1", "SHOW_ALL"];
    }
    if (ua.includes('blackberry')) {
        return ["RECOMMENDED", 'blackberry', "SHOW_ALL"];
    }
    if (/samsung|gt\-|sgh|sch|sph/g.test(ua)) {
        if (/c3060|m300/g.test(ua)) return ["RECOMMENDED", "samsung_100kb", "SHOW_ALL"];
        if (midp2) return ["RECOMMENDED", "samsung", "samsung_100kb", "SHOW_ALL"];
        return ["RECOMMENDED", "samsung", "samsung_100kb", "6310i", "midp1", "SHOW_ALL"];
    }
    if (/series60\/2/g.test(ua)) {
        return ["RECOMMENDED", "s60v2", "SHOW_ALL"];
    }
    if (/symbian|series60/g.test(ua)) {
        if (midp2) return ["RECOMMENDED", "midp2_symbian", "SHOW_ALL"];
        return ["RECOMMENDED", "midp2_symbian", "6310i", "midp1", "SHOW_ALL"];
    }
    if (ua.includes('lg')) {
        if (midp2) return ["RECOMMENDED", 'lg', "SHOW_ALL"];
        return ["RECOMMENDED", "lg", "6310i", "midp1", "SHOW_ALL"];
    }
    if (/^nokia(2855|315|322|507|514|602|6030|60[67]|6085|610|615[25]|6170|623[05]|6255|68|72[67]|736|880\d\/)/g.test(ua)) {
        return ["RECOMMENDED", "s40v2", "SHOW_ALL"];
    }
    if (/^nokia(168|2220|23[23]|26|27[26]|2865|310|3110|350|520|608[56]|6111|6125|6136|6151|616|707|c1|c2-00)/g.test(ua)) {
        return ["RECOMMENDED", "nokia_128px", "SHOW_ALL"];
    }
    if (midp2) {
        if (ua.includes('nokia')) return ["RECOMMENDED", "midp2_s40", "SHOW_ALL"];
        return ["RECOMMENDED", "midp2_alt", "SHOW_ALL"];
    }
    if (/linux|mac|windows/g.test(ua) && !/windows (ce|mobile)/g.test(ua)) {
        // modern device: show all downloads
        return Object.keys(mainVersionDownloadLinks);
    }
    return ["midp2_alt", "6310i", "midp1", "SHOW_ALL"];
}

function getRecommendedVersions(req) {
    let versions = getRecommendedVersionsArray(req);
    let prefix = '';
    let suffix = '';
    let showRecommendedText = false;

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

    return prefix + arrayDownloadLinkHtml(versions) + suffix;
}

module.exports = {
    mainVersionDownloadLinks,
    otherVersionDownloadLinks,
    downloadLinkHtml,
    arrayDownloadLinkHtml,
    getRecommendedVersionsArray,
    getRecommendedVersions
}