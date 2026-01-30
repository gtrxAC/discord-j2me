const express = require('express');
const { getRecommendedVersions, mainVersionDownloadLinks, arrayDownloadLinkHtml } = require('./recommend');

function checkIsModern(req, res, next) {
    const ua = (req.headers['user-agent'] ?? '').toLowerCase();
    res.locals.isModern =
        (/linux|mac|windows|android/g.test(ua) && !/windows (ce|mobile)/g.test(ua))
        || ua.includes("opera");

    req.format = req.accepts("html") ? "html" : "wml";
    
    next();
}

function htmlOnly(req, res, next) {
    if (req.format == 'wml') {
        res.render("htmlonly");
    } else {
        res.set("Content-Type", "text/vnd.wap.wml");
        next();
    }
}

const router = express.Router();

// Home page
router.get('/', checkIsModern, async (req, res) => {
    res.render("index_" + req.format);
})

// Download page
router.get('/j2me', checkIsModern, htmlOnly, async (req, res) => {
    res.render("download", {
        versions: getRecommendedVersions(req)
    })
});

const allVersions = (req, res) => {
    res.render("download", {
        versions: arrayDownloadLinkHtml(Object.keys(mainVersionDownloadLinks))
    })
}

router.get('/all', checkIsModern, htmlOnly, allVersions);
router.get('/j2me/all', checkIsModern, htmlOnly, allVersions);

router.get('/bench', checkIsModern, htmlOnly, async (req, res) => {
    res.render("bench");
});

router.get('/j2me/guide', checkIsModern, htmlOnly, async (req, res) => {
    res.render("guide");
});

router.get('/j2me/proxyless', checkIsModern, htmlOnly, async (req, res) => {
    res.render("proxyless");
});

module.exports = router;