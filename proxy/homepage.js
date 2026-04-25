const express = require('express');
const axios = require('axios');
const serveIndex = require('serve-index');
const { getRecommendedVersions, arrayDownloadLinkHtml, directVersionDownloadNames, getProxylessText } = require('./recommend');

const DEFAULT_PROXYLESS_TEXT =
    `<p>For better security, download a version that supports Direct connection, if available for your device. Read the <a href="/j2me/proxyless">guide</a> before installing a version with Direct connection.</p>`;

async function checkIsModern(req, res, next) {
    const uaOrig = (req.headers['user-agent'] ?? '');
    const ua = uaOrig.toLowerCase();

    res.locals.showSponsors =
        (/linux|mac|windows|android/g.test(ua) && !/windows (ce|mobile)/g.test(ua));

    res.locals.showMetaDescription = res.locals.showSponsors;
    res.locals.showGuideImages = res.locals.showSponsors;
    res.locals.showLibrecounterImage = res.locals.showSponsors || ua.includes("opera");

    if (!res.locals.showLibrecounterImage) {
        // No counter image (browser does not support TLS or SVG)
        // Instead fetch the visitor count to be shown as text
        try {
            const stats = await axios.get(`https://librecounter.org/gtrxac.fi/siteStats`);
            res.locals.visitorCount = stats.data.byDay[stats.data.byDay.length - 1].value;
        } catch (e) {
            res.locals.visitorCount = "(error)";
        }
    }

    req.format = req.accepts("html") ? "html" : "wml";

    if (req.format == "wml") {
        res.set("Content-Type", "text/vnd.wap.wml");
    }
    next();
}

function htmlOnly(req, res, next) {
    if (req.format == 'wml') {
        res.render("htmlonly");
    } else {
        next();
    }
}

const router = express.Router();

// Home page
router.get('/', checkIsModern, async (req, res) => {
    res.render("index_" + req.format);
})

// Download page
router.get('/j2me', checkIsModern, async (req, res) => {
    res.render("download_" + req.format, {
        versions: getRecommendedVersions(req),
        proxylessText: getProxylessText(req) ?? DEFAULT_PROXYLESS_TEXT
    })
});

const allVersions = (req, res) => {
    res.render("download_" + req.format, {
        versions: arrayDownloadLinkHtml(directVersionDownloadNames),
        proxylessText: DEFAULT_PROXYLESS_TEXT
    })
}

router.get('/all', checkIsModern, allVersions);
router.get('/j2me/all', checkIsModern, allVersions);

router.get('/bench', checkIsModern, async (req, res) => {
    res.render("bench_" + req.format);
});

router.get('/j2me/guide', checkIsModern, htmlOnly, async (req, res) => {
    res.render("guide");
});

router.get('/j2me/proxyless', checkIsModern, htmlOnly, async (req, res) => {
    res.render("proxyless");
});

router.get('/countvisit', checkIsModern, (req, res) => {
    const ua = req.headers['user-agent'] ?? '';
    axios.get(
        `https://librecounter.org/count?url=http://gtrxac.fi&userAgent=${encodeURIComponent(ua)}`
    );
    res.sendFile("static/blank.png", {root: "."});
})

router.get('/jarsize', checkIsModern, htmlOnly, async (req, res) => {
    res.render("jarsize");
});

router.use('/j2me/archive', express.static('static/archive'), serveIndex('static/archive', {'icons': true}));

module.exports = router;