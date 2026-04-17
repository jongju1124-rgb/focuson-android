package com.focuson.app.domain

import com.focuson.app.domain.model.AppCategory

/**
 * 패키지명 → AppCategory 매핑.
 * 주요 한국/글로벌 앱을 하드코딩, 나머지는 prefix 휴리스틱 → 최종 OTHER.
 *
 * 리스트가 늘어나면 나중에 원격 JSON으로 분리 가능.
 */
object AppCategorizer {

    fun categorize(packageName: String): AppCategory {
        EXACT[packageName]?.let { return it }
        for ((prefix, category) in PREFIX) {
            if (packageName.startsWith(prefix)) return category
        }
        return AppCategory.OTHER
    }

    private val EXACT: Map<String, AppCategory> = buildMap {
        // ─────── SNS ───────
        put("com.instagram.android", AppCategory.SNS)
        put("com.instagram.barcelona", AppCategory.SNS)   // Threads
        put("com.zhiliaoapp.musically", AppCategory.SNS)  // TikTok
        put("com.ss.android.ugc.trill", AppCategory.SNS)  // TikTok Lite
        put("com.twitter.android", AppCategory.SNS)
        put("com.facebook.katana", AppCategory.SNS)
        put("com.facebook.lite", AppCategory.SNS)
        put("com.snapchat.android", AppCategory.SNS)
        put("com.linkedin.android", AppCategory.SNS)
        put("com.pinterest", AppCategory.SNS)
        put("com.bereal.ft", AppCategory.SNS)
        put("com.tumblr", AppCategory.SNS)

        // ─────── VIDEO / OTT ───────
        put("com.google.android.youtube", AppCategory.VIDEO)
        put("com.google.android.apps.youtube.kids", AppCategory.VIDEO)
        put("com.netflix.mediaclient", AppCategory.VIDEO)
        put("net.cj.cjhv.gs.tving", AppCategory.VIDEO)
        put("com.coupang.coupangplay", AppCategory.VIDEO)
        put("com.disney.disneyplus", AppCategory.VIDEO)
        put("com.frograms.wplay", AppCategory.VIDEO)      // Watcha
        put("kr.co.captv.pooqV2", AppCategory.VIDEO)      // Wavve
        put("com.laftel.android", AppCategory.VIDEO)
        put("kr.co.nowcom.mobile.afreeca", AppCategory.VIDEO)
        put("tv.twitch.android.app", AppCategory.VIDEO)
        put("com.chzzk.android", AppCategory.VIDEO)        // 치지직
        put("kr.co.rainist.chzzk", AppCategory.VIDEO)
        put("com.vimeo.android.videoapp", AppCategory.VIDEO)

        // ─────── MUSIC ───────
        put("com.google.android.apps.youtube.music", AppCategory.MUSIC)
        put("com.spotify.music", AppCategory.MUSIC)
        put("com.iloen.melon", AppCategory.MUSIC)         // 멜론
        put("com.ktmusic.geniemusic", AppCategory.MUSIC)  // 지니
        put("skplanet.musicmate", AppCategory.MUSIC)      // Flo
        put("com.neowiz.android.bugs", AppCategory.MUSIC) // 벅스
        put("com.soribada.android", AppCategory.MUSIC)
        put("com.apple.android.music", AppCategory.MUSIC)
        put("com.amazon.mp3", AppCategory.MUSIC)

        // ─────── WEBTOON ───────
        put("com.nhn.android.webtoon", AppCategory.WEBTOON)
        put("com.naver.webtoon", AppCategory.WEBTOON)
        put("com.kakao.page", AppCategory.WEBTOON)
        put("com.kakao.piccoma", AppCategory.WEBTOON)
        put("net.daum.android.webtoon", AppCategory.WEBTOON)
        put("com.ridi.books", AppCategory.WEBTOON)        // 리디
        put("com.nhn.android.nbooks", AppCategory.WEBTOON) // 네이버시리즈
        put("com.wuxiaworld.app", AppCategory.WEBTOON)

        // ─────── COMMUNITY ───────
        put("com.dcinside.app.android", AppCategory.COMMUNITY)
        put("com.ppomppu.android", AppCategory.COMMUNITY)
        put("com.reddit.frontpage", AppCategory.COMMUNITY)
        put("com.discord", AppCategory.COMMUNITY)
        put("com.cake.open.korea", AppCategory.COMMUNITY)
        put("com.everyday.shopkiller", AppCategory.COMMUNITY)
        put("com.quora.android", AppCategory.COMMUNITY)
        put("com.bluesky.android", AppCategory.COMMUNITY)
        put("org.mozilla.thunderbird", AppCategory.COMMUNITY)

        // ─────── SHOPPING / DELIVERY ───────
        put("com.coupang.mobile", AppCategory.SHOPPING)
        put("com.ebay.kr.gmarket", AppCategory.SHOPPING)
        put("com.elevenst", AppCategory.SHOPPING)
        put("com.ebay.kr.auction", AppCategory.SHOPPING)
        put("com.ssg.serviceapp.android.egiftcertificate", AppCategory.SHOPPING)
        put("com.musinsa.com", AppCategory.SHOPPING)
        put("com.the29cm.app29cm", AppCategory.SHOPPING)
        put("com.ably.ShopApp", AppCategory.SHOPPING)
        put("com.kakaostyle.store.android", AppCategory.SHOPPING)
        put("com.amazon.mShop.android.shopping", AppCategory.SHOPPING)
        put("com.aliexpress.buyer", AppCategory.SHOPPING)
        put("com.einnovation.temu", AppCategory.SHOPPING)
        put("com.xunmeng.pinduoduo", AppCategory.SHOPPING)
        put("com.lotte.lotteon", AppCategory.SHOPPING)
        put("com.wadiz", AppCategory.SHOPPING)
        put("com.towneers.www", AppCategory.SHOPPING)     // 당근
        // 배달
        put("com.coupang.mobile.domestic.eats", AppCategory.SHOPPING)
        put("com.sampleapp", AppCategory.SHOPPING)        // 배민 (과거 BA)
        put("com.baedal.woowa.runners", AppCategory.SHOPPING)
        put("net.dooray.clickdeli.main", AppCategory.SHOPPING)
        put("com.fineapp.yogiyo", AppCategory.SHOPPING)

        // ─────── GAME ───────
        put("com.roblox.client", AppCategory.GAME)
        put("com.supercell.clashofclans", AppCategory.GAME)
        put("com.supercell.clashroyale", AppCategory.GAME)
        put("com.supercell.brawlstars", AppCategory.GAME)
        put("com.supercell.squad", AppCategory.GAME)
        put("com.miHoYo.GenshinImpact", AppCategory.GAME)
        put("com.miHoYo.hkrpg", AppCategory.GAME)         // 붕괴
        put("com.miHoYo.bh3.global", AppCategory.GAME)
        put("com.HoYoverse.Nap", AppCategory.GAME)        // 젠제로
        put("com.ea.game.fifamobile21_kr", AppCategory.GAME)
        put("com.mojang.minecraftpe", AppCategory.GAME)
        put("com.activision.callofduty.shooter", AppCategory.GAME)
        put("com.tencent.ig", AppCategory.GAME)           // PUBG Mobile
        put("com.kakaogames.odin", AppCategory.GAME)
        put("com.kakaogames.smilegate.epic7", AppCategory.GAME)
        put("com.ncsoft.lineagem19", AppCategory.GAME)
        put("com.ncsoft.lineagew", AppCategory.GAME)
        put("com.ncsoft.tof", AppCategory.GAME)

        // ─────── MESSAGING ───────
        put("com.kakao.talk", AppCategory.MESSAGING)
        put("jp.naver.line.android", AppCategory.MESSAGING)
        put("org.telegram.messenger", AppCategory.MESSAGING)
        put("com.whatsapp", AppCategory.MESSAGING)
        put("com.facebook.orca", AppCategory.MESSAGING)   // Messenger
        put("com.discord", AppCategory.MESSAGING)         // also COMMUNITY — overwrite
        put("com.google.android.apps.messaging", AppCategory.MESSAGING)
        put("com.samsung.android.messaging", AppCategory.MESSAGING)

        // ─────── BROWSER ───────
        put("com.android.chrome", AppCategory.BROWSER)
        put("com.sec.android.app.sbrowser", AppCategory.BROWSER)
        put("com.microsoft.emmx", AppCategory.BROWSER)
        put("org.mozilla.firefox", AppCategory.BROWSER)
        put("com.brave.browser", AppCategory.BROWSER)
        put("com.naver.whale", AppCategory.BROWSER)
        put("com.opera.browser", AppCategory.BROWSER)
        put("com.duckduckgo.mobile.android", AppCategory.BROWSER)

        // ─────── NEWS ───────
        put("com.nhn.android.search", AppCategory.NEWS)   // 네이버 앱
        put("com.google.android.apps.magazines", AppCategory.NEWS)
        put("flipboard.app", AppCategory.NEWS)
        put("com.daum.android.daum", AppCategory.NEWS)
    }

    /**
     * prefix 기반 휴리스틱.
     * EXACT에서 못 잡은 흔한 게임 퍼블리셔 prefix.
     */
    private val PREFIX: List<Pair<String, AppCategory>> = listOf(
        "com.netmarble." to AppCategory.GAME,
        "com.nexon." to AppCategory.GAME,
        "com.ncsoft." to AppCategory.GAME,
        "com.kakaogames." to AppCategory.GAME,
        "com.com2us." to AppCategory.GAME,
        "com.supercell." to AppCategory.GAME,
        "com.miHoYo." to AppCategory.GAME,
        "com.HoYoverse." to AppCategory.GAME,
        "com.king." to AppCategory.GAME,       // Candy Crush 등
        "com.ea.game." to AppCategory.GAME,
        "com.gamevil." to AppCategory.GAME,
        "jp.konami." to AppCategory.GAME,
        "com.tencent." to AppCategory.GAME,
    )
}
