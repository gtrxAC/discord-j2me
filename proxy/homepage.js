const express = require('express');
const { getRecommendedVersions, versionDownloadLinks, arrayDownloadLinkHtml } = require('./recommend');

function checkIsModern(req, res, next) {
    const ua = (req.headers['user-agent'] ?? '').toLowerCase();
    res.locals.isModern =
        (/linux|mac|windows|android/g.test(ua) && !/windows (ce|mobile)/g.test(ua))
        || ua.includes("opera");

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
        versions: getRecommendedVersions(req)
    })
});

const allVersions = (req, res) => {
    res.render("download_" + req.format, {
        versions: arrayDownloadLinkHtml(Object.keys(versionDownloadLinks))
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

module.exports = router;