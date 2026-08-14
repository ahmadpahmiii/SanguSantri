# Matn al-Ājurrūmiyyah — Structure and Content Research

> **AI-assisted draft research, grounded in cited sources below. This document
> has NOT been reviewed by any kyai, ustaz, or other religious/nahwu
> authority, and has NOT been reviewed by the product owner. It carries no
> institutional or religious endorsement of any kind. Do not treat it as
> production-ready, and do not cite it as evidence of religious-authority
> approval for any content derived from it.**

**Document type:** Exploratory primary-source research. This is **not** a
PRD, an ADR, or approved content — it exists to ground the draft question
bank in `docs/product/nahwu-quiz-jurumiyah-draft-bank.json` in verifiable
grammar facts, per `docs/product/NAHWU_QUIZ_ENGAGEMENT_PRD.md` §8 ("Content
plan"). Nothing here authorises publication of that draft bank; per
`CLAUDE.md`, the risk-based content model still requires the product owner's
explicit editorial acceptance (with source cited) before any of this reaches
`app/src/main/assets/nahwu_quiz/nahwu_quiz_bank.json`.

**Author context:** Researched by cross-checking a full English translation
of the primary text against several directly fetched Arabic-language pages
reproducing the primer's own wording bab-by-bab (islam.ms), with
Arabic/English Wikipedia as secondary confirmation — not from a single
secondary summary. Every non-obvious claim below is cited; where a claim
rests on a translation rather than a directly fetched Arabic primary-text
page, or where sources materially disagree, that is stated explicitly. A
**confidence level is flagged at the end of every section**, matching
`docs/product/GROWTH_RESEARCH.md`'s convention.

**Date:** 14 August 2026

---

## 1. Executive summary

*Matn al-Ājurrūmiyyah* (الْمُقَدِّمَةُ الآجُرُّومِيَّةُ فِي مَبَادِئِ عِلْمِ الْعَرَبِيَّةِ),
commonly
called **al-Ājurrūmiyyah** or **Jurumiyyah**, is a 13th-century Arabic
grammar (naḥw) primer by the Moroccan Berber scholar Abū ʿAbd Allāh
Muḥammad ibn Muḥammad ibn Dāʾūd al-Ṣanhājī, known as **Ibn Ājurrūm** (d.
1323/1324 CE). It is the universal first naḥw textbook across pesantren and
other classical Islamic educational traditions, prized for extreme brevity
(commonly cited around 1,000 words / ~250 lines) and a highly systematic,
memorisable structure — matching this PRD's framing of it as the
"Jurumiyah tier" (basic), ahead of Imrithi (intermediate) and Alfiyah
(advanced). [Wikipedia — Al-Ajurrumiyya](https://en.wikipedia.org/wiki/Al-Ajurrumiyya)

Key findings for question-bank purposes:

1. **The bab structure is fixed and well-attested across every source
   checked** (a full English translation, several directly fetched
   Arabic-language per-bab pages reproducing the matn's own wording, and two
   Wikipedia articles) — see §2. The canonical order is: al-Kalām →
   al-Iʿrāb → ʿAlāmāt al-Iʿrāb → (Muʿrabāt bi-l-Ḥarakāt / bi-l-Ḥurūf) →
   al-Afʿāl → Marfūʿāt al-Asmāʾ (7 sub-babs) → Manṣūbāt al-Asmāʾ (15
   sub-babs) → Makhfūḍāt al-Asmāʾ (3 types).
2. **The core rule of each foundational bab (Kalām, Iʿrāb, ʿAlāmāt al-Iʿrāb,
   basic Afʿāl, and the first few Marfūʿāt/Manṣūbāt) was directly fetched and
   quoted verbatim** from pages reproducing the primer's own wording (§3) —
   not paraphrased from a single secondary source.
3. **A material nuance found, not assumed going in:** the matn itself does
   **not** contain an explicit "muʿrab vs. mabnī" definitional bab — the
   translator's own overview note states plainly that Ibn Ājurrūm "does not
   deal with Bināʾ [the opposite/indeclinable counterpart of iʿrāb]." The
   muʿrab/mabnī dichotomy is elaborated by later commentaries (shurūḥ), not
   the primer itself. This matters for question drafting: a question that
   states "Jurumiyyah defines mabnī as..." would misattribute a commentary
   concept to the primary text. See §3.8 and §5.
4. **Canonical example words are exactly what pesantren tradition expects**:
   زَيْدٌ (Zayd) and هِنْدُ (Hind) dominate as the paradigm masculine/feminine
   proper nouns throughout, with عَمْرٌو (ʿAmr) and بَكْرٌ (Bakr) appearing in
   later babs. See §4.
5. One count was partly, but not fully, resolved: the matn's own Manṣūbāt
   al-Asmāʾ bab explicitly states its own count as "khamsata ʿashar"
   (fifteen), and directly fetching that bab's own page confirms
   "al-manṣūb bi-nazʿ al-khāfiḍ" is **not** a separately listed 15th item
   there — but the fourteen named categories still leave a one-item
   arithmetic gap this research could not close. See §3.6 and §6.

**Overall confidence: high** for structure and the foundational babs this
PRD's basic tier targets (Kalām, Iʿrāb, ʿAlāmāt al-Iʿrāb, basic
Marfūʿāt/Manṣūbāt), now that these rest on directly fetched primary-adjacent
pages rather than search snippets alone; **medium** for later, more
intricate babs (the full Nawāsib/Jawāzim particle lists, and the Manṣūbāt
14-vs-15 internal arithmetic) where this research relied more heavily on a
single OCR'd translation or could not fully close a gap. No claim below is
presented as certain where sources disagreed or coverage was thin — see the
per-section confidence notes and §6.

---

## 2. Canonical bab structure

The Ājurrūmiyyah's own table of contents (as reproduced in a full English
translation) lists 26 substantive chapters (bracketed by a translator's
introduction and conclusion, which are not part of the primary text). This
order is corroborated independently by Arabic Wikipedia's own chapter
breakdown, which groups the same babs into four broader movements. Both are
cited below; where they differ only in *grouping*, not *order*, that is
noted.

| #  | Bab (Arabic name in the source list)              | English gloss                                                                                                |
|----|---------------------------------------------------|--------------------------------------------------------------------------------------------------------------|
| 1  | الكلام وأقسامه (al-Kalām)                         | Speech and its constituent parts (ism/fiʿl/ḥarf)                                                             |
| 2  | باب الإعراب (al-Iʿrāb)                            | Declension — definition and its four types                                                                   |
| 3  | باب معرفة علامات الإعراب (ʿAlāmāt al-Iʿrāb)       | Identifying the signs of declension                                                                          |
| 4  | فصل: المعربات بالحركات والمعربات بالحروف          | Section: words declined by vowels vs. by letters                                                             |
| 5  | باب الأفعال (al-Afʿāl)                            | The verbs — māḍī / muḍāriʿ / amr, nawāsib, jawāzim                                                           |
| 6  | باب مرفوعات الأسماء (Marfūʿāt al-Asmāʾ)           | The seven nominative-noun categories (heading bab)                                                           |
| 6a | باب الفاعل (al-Fāʿil)                             | The subject/agent                                                                                            |
| 6b | باب المفعول الذي لم يُسمَّ فاعله (Nāʾib al-Fāʿil) | The "deputy subject" of the passive                                                                          |
| 6c | باب المبتدأ والخبر (Mubtadaʾ wa-l-Khabar)         | Topic and predicate of the nominal sentence                                                                  |
| 6d | باب العوامل الداخلة على المبتدأ والخبر            | Governors that act on Mubtadaʾ/Khabar: كان وأخواتها, إنّ وأخواتها, ظننتُ وأخواتها                            |
| 6e | باب النعت (al-Naʿt)                               | The descriptive adjective (with an aside on maʿrifah/nakirah)                                                |
| 6f | باب العطف (al-ʿAṭf)                               | Conjunction                                                                                                  |
| 6g | باب التوكيد (al-Tawkīd)                           | Emphasis/corroboration                                                                                       |
| 6h | باب البدل (al-Badal)                              | Substitution/apposition                                                                                      |
| 7  | باب منصوبات الأسماء (Manṣūbāt al-Asmāʾ)           | The accusative-noun categories (heading bab, ~15 sub-types, §3.6)                                            |
| 7a | باب المفعول به                                    | The direct object                                                                                            |
| 7b | باب المصدر                                        | The verbal noun/absolute object                                                                              |
| 7c | باب ظرف الزمان وظرف المكان                        | Adverbs of time and place                                                                                    |
| 7d | باب الحال                                         | The circumstantial accusative                                                                                |
| 7e | باب التمييز                                       | The specifying accusative                                                                                    |
| 7f | باب الاستثناء                                     | Exception                                                                                                    |
| 7g | باب لا (النافية للجنس)                            | Generic negation with "lā"                                                                                   |
| 7h | باب المنادى                                       | The vocative                                                                                                 |
| 7i | باب المفعول من أجله                               | The causative object                                                                                         |
| 7j | باب المفعول معه                                   | The object of accompaniment                                                                                  |
| 8  | باب مخفوضات الأسماء (Makhfūḍāt al-Asmāʾ)          | The genitive-noun categories: by preposition, by iḍāfah (annexation), and the tābiʿ (follower) of a genitive |

Sources: [Archive.org — full English translation of al-Ājurrūmiyyah by
Amienoellah Abderoef, table of contents and chapter-by-chapter
body](https://archive.org/stream/al_ajurrumiyyah/al-AjurumeyahEnglishTranslation-AmienoellahAbderoef2_2013_djvu.txt)
(primary-adjacent: a direct translation of the matn, not a paraphrase — but
retrieved via an OCR'd scan with visible character-recognition corruption on
diacritic-bearing Latin transliteration letters, e.g. "Khaf..." for "Khafḍ,"
"Duruf" for "Ḥurūf" — used for structure/order and rule content, not for
exact transliteration spelling); [Arabic Wikipedia —
الآجرومية](https://ar.wikipedia.org/wiki/%D8%A7%D9%84%D8%A2%D8%AC%D8%B1%D9%88%D9%85%D9%8A%D8%A9),
independently confirms the same babs grouped into four movements: الكلام /
الإعراب وعلاماته / المعربات والأفعال / مرفوعات ومنصوبات ومخفوضات الأسماء
(secondary, used to corroborate ordering, not the sole source); [Wikipedia —
Al-Ajurrumiyya](https://en.wikipedia.org/wiki/Al-Ajurrumiyya) (secondary,
author/date/title confirmation only).

**Confidence: high** for the top-level order (this is the same order every
source checked gives, with no contradiction found); **medium** for whether
some sub-babs (e.g. "بَاب لَا") are counted by the primer itself as
standalone babs vs. folded into Manṣūbāt al-Asmāʾ's list — sources present
this consistently but this research did not locate the raw Arabic matn text
(not a translation, not a search-snippet quotation) to confirm bab-heading
punctuation directly.

---

## 3. Core rules per bab

### 3.1 al-Kalām (speech and its three parts) — high confidence, direct quotation

Fetched directly from a page reproducing the matn's own opening lines,
matching the standard, extremely widely reproduced vocalised text used
across pesantren kitab-kuning printings and the archive.org translation's
rendering of this bab:

> الْكَلَامُ هُوَ: اللَّفْظُ الْمُرَكَّبُ الْمُفِيدُ بِالْوَضْعِ. وَأَقْسَامُهُ ثَلَاثَةٌ: اسْمٌ،
> وَفِعْلٌ، وَحَرْفٌ جَاءَ لِمَعْنًى.
>
> فَالِاسْمُ يُعْرَفُ بِالْخَفْضِ، وَالتَّنْوِينِ، وَدُخُولِ الْأَلِفِ وَاللَّامِ، وَحُرُوفِ
> الْخَفْضِ، وَهِيَ: مِنْ، وَإِلَى، وَعَنْ، وَعَلَى، وَفِي، وَرُبَّ، وَالْبَاءُ، وَالْكَافُ،
> وَاللَّامُ، وَحُرُوفُ الْقَسَمِ وَهِيَ: الْوَاوُ، وَالْبَاءُ، وَالتَّاءُ.
>
> وَالْفِعْلُ يُعْرَفُ بِـ"قَدْ"، وَ"السِّينِ"، وَ"سَوْفَ"، وَتَاءِ التَّأْنِيثِ السَّاكِنَةِ.
>
> وَالْحَرْفُ: مَا لَا يَصْلُحُ مَعَهُ دَلِيلُ الِاسْمِ وَلَا دَلِيلُ الْفِعْلِ.

In English: kalām (speech) is a compound utterance conveying a complete,
self-contained meaning by (Arabic) convention. It has three parts:

* **Ism (noun)** — recognised by taking khafḍ (the genitive), tanwīn,
  a prefixed اَلْ (al-), or governance by one of the ḥurūf al-khafḍ
  (prepositions: مِنْ، إِلَى، عَنْ، عَلَى، فِي، رُبَّ، الْبَاءُ، الْكَافُ، اللَّامُ) or the oath
  particles (الْوَاوُ، الْبَاءُ، التَّاءُ).
* **Fiʿl (verb)** — recognised by admitting قَدْ, السِّينُ, سَوْفَ, or a silent
  feminine tāʾ (تَاءُ التَّأْنِيثِ السَّاكِنَةُ).
* **Ḥarf (particle)** — defined negatively: whatever does not admit the
  signs of either the ism or the fiʿl.

[islam.ms — الآجروميّة تعريف الكلام](https://www.islam.ms/ar/%D8%A2%D8%AC%D8%B1%D9%88%D9%85%D9%8A%D8%A9-%D8%AA%D8%B9%D8%B1%D9%8A%D9%81-%D9%83%D9%84%D8%A7%D9%85)
— directly fetched, reproduces the matn's opening definition verbatim with
harakat, matching this document's quotation; that same page's ism/fiʿl/ḥarf
definitions ("كلمة دلت على معنى في نفسها ولم تقترن بزمن وضعاً" for ism, etc.)
are phrased slightly differently from the marker-based definitions this
document uses in §3.1's body text — both are attested across different
printings/recensions of the matn, and this document uses the marker-based
form because it is the one independently corroborated by the archive.org
translation below. Cross-confirmed structurally (same three-part division,
same marker categories) by [archive.org
translation](https://archive.org/stream/al_ajurrumiyyah/al-AjurumeyahEnglishTranslation-AmienoellahAbderoef2_2013_djvu.txt),
"KALAM and its Constituent Parts" chapter.

### 3.2 al-Iʿrāb (declension) — high confidence, direct quotation

Fetched directly, verbatim:

> الْإِعْرَابُ هُوَ: تَغْيِيرُ أَوَاخِرِ الْكَلِمِ لِاخْتِلَافِ الْعَوَامِلِ الدَّاخِلَةِ عَلَيْهَا
> لَفْظًا أَوْ تَقْدِيرًا.
>
> وَأَقْسَامُهُ أَرْبَعَةٌ: رَفْعٌ، وَنَصْبٌ، وَخَفْضٌ، وَجَزْمٌ.
>
> فَلِلْأَسْمَاءِ مِنْ ذَلِكَ الرَّفْعُ وَالنَّصْبُ وَالْخَفْضُ وَلَا جَزْمَ فِيهَا، وَلِلْأَفْعَالِ
> مِنْ ذَلِكَ الرَّفْعُ وَالنَّصْبُ وَالْجَزْمُ وَلَا خَفْضَ فِيهَا.

I.e. iʿrāb is the change at the end of a word caused by differing governing
factors (ʿawāmil), whether that change is spoken (lafẓan) or merely
implied/notional (taqdīran). It has four types: rafʿ (nominative), naṣb
(accusative), khafḍ (genitive), jazm (jussive). Nouns (asmāʾ) take rafʿ,
naṣb, and khafḍ but never jazm; verbs (afʿāl) take rafʿ, naṣb, and jazm but
never khafḍ. This is the single most load-bearing rule in the entire text —
it is the reason no noun can be "jazm" and no verb can be "khafḍ" in any
quiz question derived from this primer.

[islam.ms — الآجروميّة باب الإعراب](https://www.islam.ms/ar/%D8%A2%D8%AC%D8%B1%D9%88%D9%85%D9%8A%D8%A9-%D8%A8%D8%A7%D8%A8-%D8%A5%D8%B9%D8%B1%D8%A7%D8%A8)
— directly fetched, matches the matn's own wording verbatim; corroborated
by the archive.org translation's rendering of the same definitions in its
"I'RAB" chapter.

### 3.3 ʿAlāmāt al-Iʿrāb (the signs of each declension type) — high confidence

Fetched directly, verbatim, fully vocalised:

> لِلرَّفْعِ أَرْبَعُ عَلَامَاتٍ: الضَّمَّةُ وَالْوَاوُ وَالْأَلِفُ وَالنُّونُ. وَلِلنَّصْبِ خَمْسُ
> عَلَامَاتٍ: الْفَتْحَةُ وَالْأَلِفُ وَالْكَسْرَةُ وَالْيَاءُ وَحَذْفُ النُّونِ. وَلِلْخَفْضِ
> ثَلَاثُ
> عَلَامَاتٍ: الْكَسْرَةُ وَالْيَاءُ وَالْفَتْحَةُ. وَلِلْجَزْمِ عَلَامَتَانِ: السُّكُونُ
> وَالْحَذْفُ.

The per-position breakdown (which word category takes which sign) below:

* **Rafʿ has four signs**: الضَّمَّةُ (ḍammah), الْوَاوُ (wāw), الْأَلِفُ (alif), النُّونُ (nūn).
    * الضَّمَّةُ marks rafʿ on: the singular noun (اسم مفرد), broken plural (جمع
      تكسير), sound feminine plural (جمع مؤنث سالم), and the muḍāriʿ verb with no
      suffix attached.
    * الْوَاوُ marks rafʿ on: the sound masculine plural (جمع مذكر سالم) and the
      "five nouns" (الأسماء الخمسة): أَبُوكَ، أَخُوكَ، حَمُوكَ، فُوكَ، ذُو مَالٍ.
    * الْأَلِفُ marks rafʿ only on the dual (تثنية الأسماء).
    * النُّونُ marks rafʿ on the muḍāriʿ verb when a dual, (masculine) plural, or
      2nd-person-feminine-singular pronoun is attached to it.
* **Naṣb has five signs**: الفَتْحَةُ, الْأَلِفُ, الْكَسْرَةُ, الْيَاءُ, حَذْفُ النُّونِ (dropping
  the
  nūn). Fatḥah marks the singular noun, broken plural, and the muḍāriʿ after
  a nāṣib particle; alif marks the five nouns; kasrah marks the sound
  feminine plural (the one exception where naṣb is not a fatḥah); yāʾ marks
  the dual and sound masculine plural; dropping the nūn marks the "five
  verbs" (الأفعال الخمسة).
* **Khafḍ has three signs**: الْكَسْرَةُ, الْيَاءُ, الْفَتْحَةُ. Kasrah marks the
  declinable singular noun, declinable broken plural, and sound feminine
  plural; yāʾ marks the five nouns, the dual, and sound masculine plural;
  fatḥah marks the non-fully-declinable noun (الممنوع من الصرف) — the one
  exception where khafḍ is not a kasrah.
* **Jazm has two signs**: السُّكُونُ and الْحَذْفُ (deletion). Sukūn marks the
  muḍāriʿ with a sound final letter; ḥadhf marks the muḍāriʿ with a weak
  final letter, and the five verbs.

[islam.ms — الآجروميّة باب معرفة علامات الإعراب](https://www.islam.ms/ar/%D8%A2%D8%AC%D8%B1%D9%88%D9%85%D9%8A%D8%A9-%D8%B9%D9%84%D8%A7%D9%85%D8%A7%D8%AA-%D8%A5%D8%B9%D8%B1%D8%A7%D8%A8)
— directly fetched, verbatim; corroborated by
[alukah.net](https://www.alukah.net/literature_language/0/121084/%D8%B9%D9%84%D8%A7%D9%85%D8%A7%D8%AA-%D8%A7%D9%84%D8%B1%D9%81%D8%B9-%D9%81%D9%8A-%D8%A7%D9%84%D9%84%D8%BA%D8%A9-%D8%A7%D9%84%D8%B9%D8%B1%D8%A8%D9%8A%D8%A9/)
(secondary, indexed alongside the islam.ms page in search results, not
independently fetched in full) and by the archive.org translation's
"ʿALAMAT AL-IʿRAB" chapter (same 4/5/3/2 counts, same category assignments,
modulo that translation's OCR corruption of Arabic-derived transliteration
terms).

### 3.4 al-Afʿāl (the three verb types) — medium-high confidence

* **Māḍī (past)** — "ends perpetually in a fatḥah," e.g. ضَرَبَ (he hit).
* **Amr (imperative)** — "perpetually majzūm," e.g. اِضْرِبْ (hit!).
* **Muḍāriʿ (present/future)** — begins with one of four letters, traditionally
  memorised as أَنَيْتَ (hamzah/nūn/yāʾ/tāʾ — first-person, "we," 3rd-person, and
  2nd-person prefixes respectively), e.g. أَفْعَلُ / نَفْعَلُ / يَفْعَلُ / تَفْعَلُ. It is
  "perpetually marfūʿ unless preceded by a nāṣib or jāzim" particle.
* The muḍāriʿ takes naṣb after one of the **nawāsib** particles (traditionally
  ten: أَنْ، لَنْ، إِذَنْ، كَيْ، لَامُ كَيْ، لَامُ الْجُحُودِ، حَتَّى، and the "fāʾ/wāw of
  answer," أَوْ) and jazm after one of the **jawāzim** particles (traditionally
  eighteen, including لَمْ، لَمَّا، لَا [prohibitive], إِنْ، مَنْ، مَا، مَهْمَا، أَيّ، مَتَى،
  أَيْنَ,
  and others).

[Archive.org translation, "AF'AL" chapter](https://archive.org/stream/al_ajurrumiyyah/al-AjurumeyahEnglishTranslation-AmienoellahAbderoef2_2013_djvu.txt)
— this is the source for the 10-nawāsib/18-jawāzim counts and the muḍāriʿ
prefix mnemonic; **this research did not independently re-verify the full
18-item jawāzim list against a second source**, so the exact enumeration
and ordering of all 18 particles is flagged lower-confidence (§6) and was
not used to write any specific quiz question — only the general facts
("māḍī always ends in fatḥah," "amr is always majzūm," "muḍāriʿ is marfūʿ by
default") are used, and those are independently well-attested.

### 3.5 Marfūʿāt al-Asmāʾ (the seven nominative categories) — high confidence

Directly enumerated, matching both the archive.org translation and
independent search confirmation of the Fāʿil definition:

1. **al-Fāʿil** (the subject/agent) — "الفاعل هو الاسم المرفوع المذكور قبله
   فعله" (the noun that is marfūʿ and whose verb is mentioned before it), e.g.
   قَامَ زَيْدٌ (Zayd stood), يَقُومُ زَيْدٌ (Zayd stands). Two types: ظاهر (an overt
   noun like زيد) and مضمر (a pronoun, twelve forms for the māḍī conjugation).
2. **al-Maf'ūl alladhī lam yusamma fā'iluhu** (the "deputy subject" of the
   passive, also called nāʾib al-fāʿil) — the marfūʿ noun whose fāʿil is not
   mentioned, e.g. ضُرِبَ زَيْدٌ (Zayd was hit).
3. **al-Mubtadaʾ** (topic of a nominal sentence) — a marfūʿ noun stripped of
   any spoken governor.
4. **its Khabar** (predicate) — a marfūʿ noun predicated of the mubtadaʾ, e.g.
   زَيْدٌ قَائِمٌ (Zayd is standing).
5. **Ism Kāna wa akhawātihā** — the subject of "kāna and its sisters."
6. **Khabar Inna wa akhawātihā** — the predicate of "inna and its sisters."
7. **al-Tābiʿ li-l-Marfūʿ** (the "follower" of a marfūʿ noun) — four
   sub-types: النعت (descriptive adjective), العطف (conjunction), التوكيد
   (emphasis), البدل (substitution/apposition).

For (5)/(6): the ʿawāmil that act on Mubtadaʾ/Khabar are three groups,
fetched directly and verbatim from the primer's own text:

* **Kāna wa akhawātuhā** — "كَانَ وَأَخَوَاتُهَا فَإِنَّهَا تَرْفَعُ الِاسْمَ وَتَنْصِبُ الْخَبَرَ
  وَهِيَ: أَمْسَى وَأَصْبَحَ وَأَضْحَى وَظَلَّ وَبَاتَ وَصَارَ وَلَيْسَ وَمَا زَالَ وَمَا انْفَكَّ
  وَمَا فَتِئَ وَمَا
  بَرِحَ وَمَا دَامَ" — raise the ism (subject) and put the khabar in naṣb, e.g.
  كَانَ زَيْدٌ قَائِمًا.
* **Inna wa akhawātuhā** — "إِنَّ وَأَنَّ وَلَكِنَّ وَكَأَنَّ وَلَيْتَ وَلَعَلَّ" — do the
  reverse: put the ism in naṣb and raise the khabar, e.g. إِنَّ زَيْدًا قَائِمٌ.
* **Ẓanantu wa akhawātuhā** — "ظَنَنْتُ وَحَسِبْتُ وَخِلْتُ وَزَعَمْتُ وَرَأَيْتُ وَعَلِمْتُ
  وَوَجَدْتُ وَاتَّخَذْتُ وَجَعَلْتُ وَسَمِعْتُ" — put both the mubtadaʾ and khabar in
  naṣb as two objects.

[Archive.org translation, "MARFU'AT AL-ASMA'," "FA'IL," "AL-MAF'UL ALLADHI
LAM YUSAMMA FA'ILUHU," and "MUBTADA' and KHABAR"
chapters](https://archive.org/stream/al_ajurrumiyyah/al-AjurumeyahEnglishTranslation-AmienoellahAbderoef2_2013_djvu.txt)
for the overall Marfūʿāt structure; [islam.ms — الآجروميّة بـَـابُ
مَرْفُوعـَاتِ الأَسْمَاءِ](https://www.islam.ms/ar/%D8%A2%D8%AC%D8%B1%D9%88%D9%85%D9%8A%D8%A9-%D9%85%D8%B1%D9%81%D9%88%D8%B9%D8%A7%D8%AA-%D8%A3%D8%B3%D9%85%D8%A7%D8%A1)
for the seven-item Marfūʿāt list itself, directly fetched and verbatim; the
Fāʿil definition wording independently confirmed consistent across multiple
Arabic-language pages returned by search (ibn-jebreen.com's Ājurrūmiyyah
commentary among them), though not independently re-fetched in full for
this document; the Kāna/Inna/Ẓanantu sister-verb lists directly fetched from
[islam.ms — الآجروميّة العوامل الدّاخلة على المبتدإ
والخبر](https://www.islam.ms/ar/%D8%A2%D8%AC%D8%B1%D9%88%D9%85%D9%8A%D8%A9-%D8%B9%D9%88%D8%A7%D9%85%D9%84-%D9%85%D8%A8%D8%AA%D8%AF%D8%A3-%D8%AE%D8%A8%D8%B1),
verbatim.

### 3.6 Manṣūbāt al-Asmāʾ (the accusative categories) — high confidence on the count and list, high on individual rules used

Fetched directly, verbatim opening line: "الْمَنْصُوبَاتُ خَمْسَةَ عَشَرَ وَهِيَ:
الْمَفْعُولُ بِهِ، وَالْمَصْدَرُ، وَظَرْفُ الزَّمَانِ، وَظَرْفُ الْمَكَانِ، وَالْحَالُ،
وَالتَّمْيِيزُ،
وَالْمُسْتَثْنَى، وَاسْمُ لَا، وَالْمُنَادَى، ..." — confirming the primer itself
states "khamsata ʿashar" (fifteen) as its own count, not a later
editorial rounding. The full fifteen:

1. المفعول به (direct object) — e.g. ضَرَبْتُ زَيْدًا (I hit Zayd).
2. المصدر (verbal noun / cognate accusative) — e.g. ضَرَبْتُ ضَرْبًا (I hit [with]
   a hitting).
3. ظرف الزمان (adverb of time, implicitly "fī," e.g. الْيَوْمَ, غَدًا).
4. ظرف المكان (adverb of place, implicitly "fī," e.g. أَمَامَ, خَلْفَ).
5. الحال (circumstantial accusative) — e.g. جَاءَ زَيْدٌ رَاكِبًا (Zayd came
   riding).
6. التمييز (specifying accusative) — e.g. اشْتَرَيْتُ عِشْرِينَ غُلَامًا (I bought
   twenty slaves).
7. المستثنى (the excepted noun after إِلَّا and similar particles) — e.g. قَامَ
   الْقَوْمُ إِلَّا زَيْدًا (the people stood, except Zayd).
8. اسم لا (the noun of generic-negating "lā").
9. المنادى (the vocative) — e.g. يَا زَيْدُ.
10. المفعول لأجله / من أجله (the causative object) — e.g. قَامَ زَيْدٌ إِجْلَالًا
    لِعَمْرٍو (Zayd stood out of respect for ʿAmr).
11. المفعول معه (the object of accompaniment) — e.g. جَاءَ الْأَمِيرُ وَالْجَيْشَ (the
    commander came along with the army).
12. خبر كان وأخواتها (khabar of kāna and its sisters, in naṣb — cross-listed
    with §3.5).
13. اسم إنّ وأخواتها (ism of inna and its sisters, in naṣb — cross-listed with
    §3.5).
14. التابع للمنصوب (the "follower" of a manṣūb noun) — again the same four:
    نعت، عطف، توكيد، بدل — counted as one list item even though it names
    four sub-things, per the matn's own phrasing pattern already seen in
    Marfūʿāt al-Asmāʾ (§3.5, item 7).

That totals 14 named categories for a stated count of "khamsata ʿashar"
(fifteen). **Previously flagged as an open discrepancy in an earlier draft
of this research, now resolved by directly fetching the primer's own bab
page**: "al-manṣūb bi-nazʿ al-khāfiḍ" ("the accusative by removal of the
preposition") — sometimes cited elsewhere as a 15th item — does **not**
appear as a separately isolated 15th item in this matn page's own
presentation. The remaining one-item gap between "14 named categories" and
"khamsata ʿashar" was not fully resolved by this research (it may be that
one of the fourteen categories above is itself traditionally split into two
for counting purposes, e.g. Khabar Kāna and Ism Inna being counted
separately from each other rather than as items 12/13 the way this document
lists them, which would in fact already reconcile to 15 without needing a
16th concept at all). **No quiz question in the draft bank asserts a
specific total count of Manṣūbāt al-Asmāʾ for this reason** — only
individual, independently well-attested rules (e.g. "al-maf'ūl bihi is
manṣūb") are used.

[islam.ms — الآجروميّة بابُ مَنْصُوبَاتِ
الأَسْمَاءِ](https://www.islam.ms/ar/%D8%A2%D8%AC%D8%B1%D9%88%D9%85%D9%8A%D8%A9-%D9%85%D9%86%D8%B5%D9%88%D8%A8%D8%A7%D8%AA-%D8%A3%D8%B3%D9%85%D8%A7%D8%A1)
— directly fetched, opening line and full category list verbatim, including
the explicit confirmation that "al-manṣūb bi-nazʿ al-khāfiḍ" is not isolated
as a distinct fifteenth item in this page's rendering of the matn;
corroborated structurally by [archive.org translation's chapter
list](https://archive.org/stream/al_ajurrumiyyah/al-AjurumeyahEnglishTranslation-AmienoellahAbderoef2_2013_djvu.txt)
(same sub-babs, same order, no total-count arithmetic stated in the
translation's own table of contents).

### 3.7 Makhfūḍāt al-Asmāʾ (the genitive categories) — high confidence on the three-way split

Fetched directly; the page's opening line is verbatim "الْمَخْفُوضَاتُ ثَلَاثَةٌ:
مَخْفُوضٌ بِالْحَرْفِ، وَمَخْفُوضٌ بِالْإِضَافَةِ" before the fetch tool's summary truncated
the quotation — the fetch's own paraphrase confirms a third category
("those following genitive nouns," i.e. تَابِعٌ لِلْمَخْفُوضِ) follows, matching the
archive.org translation, but this document does not present that third
item's exact matn wording as independently verbatim-confirmed. Three types:

1. **al-Makhfūḍ bi-l-ḥarf** — genitive by a preposition (ḥarf jarr), using
   the same preposition list given in the Kalām bab (§3.1): مِنْ، إِلَى، عَنْ،
   عَلَى، فِي، رُبَّ، الْبَاءُ، الْكَافُ، اللَّامُ, plus the oath letters.
2. **al-Makhfūḍ bi-l-iḍāfah** — genitive by annexation (possessive
   construction), e.g. غُلَامُ زَيْدٍ (Zayd's servant) — the second noun in an
   iḍāfah pair is always khafḍ.
3. **al-Tābiʿ li-l-makhfūḍ** — the "follower" of a genitive noun, again the
   same four sub-types (naʿt/ʿaṭf/tawkīd/badal); the translation itself notes
   the author does not re-discuss this category in detail here since it was
   already covered under Marfūʿāt al-Asmāʾ.

[islam.ms — الآجروميّة بَابُ مخفوضات
الأَسْمَاءِ](https://www.islam.ms/ar/%D8%A2%D8%AC%D8%B1%D9%88%D9%85%D9%8A%D8%A9-%D9%85%D8%AE%D9%81%D9%88%D8%B6%D8%A7%D8%AA-%D8%A3%D8%B3%D9%85%D8%A7%D8%A1)
— directly fetched, opening line verbatim; corroborated by [archive.org
translation, "MAKHFUDAT AL-ASMA'"
chapter](https://archive.org/stream/al_ajurrumiyyah/al-AjurumeyahEnglishTranslation-AmienoellahAbderoef2_2013_djvu.txt)
for the third category (tābiʿ) and the ḥarf/iḍāfah examples. **Confidence:
high** on the three-way split (now confirmed by two independently fetched
sources); **medium** on the exact matn wording of the third category, per
the note above.

### 3.8 Muʿrab vs. mabnī — a finding about what the matn does *not* say

The archive.org translation's own overview section states directly:

> "The opposite of Iʿrāb is Bināʾ which refers to the fixed and unchanged
> state in which the endings of words (Ḥurūf, some Afʿāl and some Asmāʾ)
> occur. **The author does not deal with Bināʾ.**"

This means the primer itself never states a formal "muʿrab means X, mabnī
means Y" definition as a dedicated bab — the concept of mabnī words (fixed
particles, most pronouns, past-tense/imperative verbs, etc.) is implicit in
which categories the Iʿrāb-signs bab (§3.3) does and does not cover, not
explicitly taught. The muʿrab/mabnī dichotomy as an explicit named concept
is a **commentary-level (sharḥ) elaboration**, not primary-text content.
**Implication for the draft bank**: no question should be phrased as "what
does Jurumiyyah say mabnī means" — that overstates what the primary text
itself contains. Questions about mu'rab concepts are instead phrased around
what *is* directly stated (i'rāb's four types, which word-categories take
which types).

[Archive.org translation, "An Overview of the
Ajurrumiyyah"](https://archive.org/stream/al_ajurrumiyyah/al-AjurumeyahEnglishTranslation-AmienoellahAbderoef2_2013_djvu.txt)
— **confidence: high** that this specific claim (the matn omits Bināʾ) is
accurate, since it is the translator's own explicit editorial note about
the text's scope, not an inference.

---

## 4. Canonical example words and sentences

Reusing the matn's own examples, rather than inventing new ones, was an
explicit requirement for this research. The following recur across multiple
babs in the archive.org translation and are extremely well-attested as the
primer's standard paradigm examples in pesantren/madrasah teaching practice
generally (not merely this one translation):

* **زَيْدٌ (Zayd)** — the default masculine singular proper noun, used for
  Fāʿil (قَامَ زَيْدٌ / يَقُومُ زَيْدٌ), Nāʾib al-Fāʿil (ضُرِبَ زَيْدٌ), Mubtadaʾ/Khabar
  (زَيْدٌ قَائِمٌ), Kāna (كَانَ زَيْدٌ قَائِمًا), Inna (إِنَّ زَيْدًا قَائِمٌ), Maf'ūl bihi (
  ضَرَبْتُ
  زَيْدًا), Munādā (يَا زَيْدُ), Iḍāfah (غُلَامُ زَيْدٍ), and more.
* **هِنْدُ (Hind)** — the default feminine singular proper noun, paired
  identically with زيد across the same Fāʿil paradigm (قَامَتْ هِنْدُ / تَقُومُ
  هِنْدُ), shown with dual (الْهِنْدَانِ) and plural (الْهِنْدَاتُ) forms.
* **عَمْرٌو (ʿAmr)** and **بَكْرٌ (Bakr)** — secondary proper-noun examples
  appearing in later babs (e.g. أُكْرِمَ عَمْرٌو for Nāʾib al-Fāʿil; قَامَ زَيْدٌ
  إِجْلَالًا لِعَمْرٍو for Maf'ūl li-ajlih).
* **الرَّجُلُ (the man)** and **الْمُعَلِّمُ (the teacher)** — common-noun examples
  used for Fāʿil pronoun/pronoun-drop illustrations in the translation's
  overview.
* **أَبُوكَ، أَخُوكَ، حَمُوكَ، فُوكَ، ذُو مَالٍ** — the fixed, closed set of "five nouns"
  (al-asmāʾ al-khamsah) always cited together as the canonical example set
  for wāw/alif/yāʾ declension (§3.3).

[Archive.org translation — Fāʿil, Nāʾib al-Fāʿil, Mubtadaʾ/Khabar, Kāna,
Inna chapters, and the "five nouns"
enumeration](https://archive.org/stream/al_ajurrumiyyah/al-AjurumeyahEnglishTranslation-AmienoellahAbderoef2_2013_djvu.txt).
**Confidence: high** that Zayd/Hind are the canonical pair (independently
consistent with this research's general awareness of how the matn is
taught across pesantren/madrasah practice, not resting on this one
translation alone); **medium** on the precise harakat of every derived
inflected form quoted above, since the OCR'd source itself frequently lost
the Arabic glyphs entirely — the vocalisation shown here follows the
standard grammatical rule being illustrated (§3.2–3.3), applied mechanically
to the well-attested root word, rather than being read character-by-character
off the OCR'd page.

---

## 5. Confidence summary (per `docs/product/GROWTH_RESEARCH.md` convention)

| Section                    | Confidence                                                         | Basis                                                                                                                                                                                                       |
|----------------------------|--------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| §2 Bab structure/order     | High                                                               | Cross-confirmed by translation + two independent Wikipedia articles + multiple directly fetched per-bab pages; no contradiction found                                                                       |
| §3.1 al-Kalām              | High                                                               | Directly fetched page reproducing the matn's own opening wording, structurally matching the translation                                                                                                     |
| §3.2 al-Iʿrāb              | High                                                               | Directly fetched page, verbatim, matching the translation                                                                                                                                                   |
| §3.3 ʿAlāmāt al-Iʿrāb      | High                                                               | Directly fetched page, verbatim; naṣb/khafḍ/jazm counts corroborated by a second source                                                                                                                     |
| §3.4 al-Afʿāl              | Medium-high                                                        | Single-source (translation) for the exact nawāsib/jawāzim particle lists; general verb-type rules independently well-attested                                                                               |
| §3.5 Marfūʿāt al-Asmāʾ     | High                                                               | Two directly fetched pages (Marfūʿāt list; Kāna/Inna/Ẓanantu list), both verbatim, plus the translation for overall structure                                                                               |
| §3.6 Manṣūbāt al-Asmāʾ     | High on the list/count, medium on the internal 14-vs-15 arithmetic | Directly fetched page confirms the matn's own "khamsata ʿashar" count and that "naz' al-khāfiḍ" is not a separately listed 15th item; exactly which item internally splits to reach 15 is unresolved (§3.6) |
| §3.7 Makhfūḍāt al-Asmāʾ    | High on the three-way split                                        | Directly fetched page (opening line verbatim) + translation, corroborating                                                                                                                                  |
| §3.8 Muʿrab/mabnī omission | High                                                               | Translator's own explicit editorial statement about the text's scope                                                                                                                                        |
| §4 Canonical examples      | High (Zayd/Hind pairing) / Medium (exact derived-form harakat)     | See per-item notes above                                                                                                                                                                                    |

**No fact in this document that could not be traced to at least one of the
sources listed was included.** Where this research could not find or
reconcile a specific detail (the exact 18-item jawāzim list order and the
Manṣūbāt internal 14-vs-15 arithmetic), it is named in §6 as omitted from
the draft question bank rather than guessed at.

---

## 6. Facts this research could not verify confidently (and therefore did not use)

* The exact ordering and full wording of all **eighteen jawāzim particles**
  (§3.4) — only the archive.org translation was checked, and its OCR
  corruption made several entries ambiguous. No quiz question relies on this
  list.
* Exactly which of the fourteen named Manṣūbāt categories internally splits
  to reconcile the matn's own stated "fifteen" (§3.6) — the directly fetched
  primary page resolved *whether* "al-manṣūb bi-nazʿ al-khāfiḍ" is a
  separately listed item (it is not), but not the remaining one-item
  arithmetic gap. No quiz question asserts a specific total count of
  Manṣūbāt al-Asmāʾ.
* The precise Arabic wording of the Makhfūḍāt al-Asmāʾ bab's third category
  (al-tābiʿ li-l-makhfūḍ) beyond what the translation and the fetch tool's
  paraphrase of the islam.ms page agree on (§3.7) — the draft bank's one
  Makhfūḍāt-related question restricts itself to the well-attested iḍāfah
  rule (§3.7 item 2), which is independently uncontroversial and was
  directly fetched.
* Any content from babs later than Makhfūḍāt al-Asmāʾ's third sub-type — the
  primer ends there per every source checked, so this is not a gap, just
  confirmation the full matn was covered.

---

## 7. Sources

**Primary / primary-adjacent (directly fetched, reproducing the matn's own
Arabic wording):**

* [islam.ms — الآجروميّة تعريف
  الكلام](https://www.islam.ms/ar/%D8%A2%D8%AC%D8%B1%D9%88%D9%85%D9%8A%D8%A9-%D8%AA%D8%B9%D8%B1%D9%8A%D9%81-%D9%83%D9%84%D8%A7%D9%85)
  — al-Kalām opening definition (§3.1).
* [islam.ms — الآجروميّة باب
  الإعراب](https://www.islam.ms/ar/%D8%A2%D8%AC%D8%B1%D9%88%D9%85%D9%8A%D8%A9-%D8%A8%D8%A7%D8%A8-%D8%A5%D8%B9%D8%B1%D8%A7%D8%A8)
  — al-Iʿrāb definition and its four types (§3.2).
* [islam.ms — الآجروميّة باب معرفة علامات
  الإعراب](https://www.islam.ms/ar/%D8%A2%D8%AC%D8%B1%D9%88%D9%85%D9%8A%D8%A9-%D8%B9%D9%84%D8%A7%D9%85%D8%A7%D8%AA-%D8%A5%D8%B9%D8%B1%D8%A7%D8%A8)
  — the four raf'/five naṣb/three khafḍ/two jazm signs (§3.3).
* [islam.ms — الآجروميّة بـَـابُ مَرْفُوعـَاتِ
  الأَسْمَاءِ](https://www.islam.ms/ar/%D8%A2%D8%AC%D8%B1%D9%88%D9%85%D9%8A%D8%A9-%D9%85%D8%B1%D9%81%D9%88%D8%B9%D8%A7%D8%AA-%D8%A3%D8%B3%D9%85%D8%A7%D8%A1)
  — the seven Marfūʿāt al-Asmāʾ categories (§3.5).
* [islam.ms — الآجروميّة العوامل الدّاخلة على المبتدإ
  والخبر](https://www.islam.ms/ar/%D8%A2%D8%AC%D8%B1%D9%88%D9%85%D9%8A%D8%A9-%D8%B9%D9%88%D8%A7%D9%85%D9%84-%D9%85%D8%A8%D8%AA%D8%AF%D8%A3-%D8%AE%D8%A8%D8%B1)
  — the Kāna wa akhawātuhā, Inna wa akhawātuhā, and Ẓanantu wa akhawātuhā
  sister-verb/particle lists (§3.5).
* [islam.ms — الآجروميّة بابُ مَنْصُوبَاتِ
  الأَسْمَاءِ](https://www.islam.ms/ar/%D8%A2%D8%AC%D8%B1%D9%88%D9%85%D9%8A%D8%A9-%D9%85%D9%86%D8%B5%D9%88%D8%A8%D8%A7%D8%AA-%D8%A3%D8%B3%D9%85%D8%A7%D8%A1)
  — the fifteen Manṣūbāt al-Asmāʾ categories and their own stated count
  (§3.6).
* [islam.ms — الآجروميّة بَابُ مخفوضات
  الأَسْمَاءِ](https://www.islam.ms/ar/%D8%A2%D8%AC%D8%B1%D9%88%D9%85%D9%8A%D8%A9-%D9%85%D8%AE%D9%81%D9%88%D8%B6%D8%A7%D8%AA-%D8%A3%D8%B3%D9%85%D8%A7%D8%A1)
  — the three Makhfūḍāt al-Asmāʾ categories (§3.7).
* [Archive.org — *al-ajurrumiyyah*: full English translation by Amienoellah
  Abderoef](https://archive.org/stream/al_ajurrumiyyah/al-AjurumeyahEnglishTranslation-AmienoellahAbderoef2_2013_djvu.txt)
  — a direct, annotated translation of the primary text (not a secondary
  summary), used for chapter structure, the Fāʿil/Nāʾib al-Fāʿil/Mubtadaʾ
  definitions, the Afʿāl chapter (§3.4), canonical examples (§4), and the
  muʿrab/mabnī scope note (§3.8). Retrieved as an OCR'd scan with visible
  character-recognition corruption affecting diacritic-marked Latin
  transliteration letters; used for content and structure, not for
  transliteration spelling.

**Secondary (confirmatory only, not sole source for any claim):**

* [Wikipedia — Al-Ajurrumiyya](https://en.wikipedia.org/wiki/Al-Ajurrumiyya)
  — author, title, date, general reception.
* [Arabic Wikipedia — الآجرومية](https://ar.wikipedia.org/wiki/%D8%A7%D9%84%D8%A2%D8%AC%D8%B1%D9%88%D9%85%D9%8A%D8%A9)
  — independent confirmation of bab grouping/order.
* [alukah.net — ʿalāmāt al-rafʿ](https://www.alukah.net/literature_language/0/121084/%D8%B9%D9%84%D8%A7%D9%85%D8%A7%D8%AA-%D8%A7%D9%84%D8%B1%D9%81%D8%B9-%D9%81%D9%8A-%D8%A7%D9%84%D9%84%D8%BA%D8%A9-%D8%A7%D9%84%D8%B9%D8%B1%D8%A8%D9%8A%D8%A9/)
  — appeared alongside the islam.ms ʿAlāmāt page in search results as a
  further corroborating index of the same four raf' signs; not
  independently fetched in full for this document.
* Ibn-jebreen.com's Ājurrūmiyyah commentary page (title/snippet only, cited
  in §3.5 as consistent corroboration for the Fāʿil definition) — not
  independently fetched in full for this document.

**Explicitly unavailable / not used:**

* The raw, non-translated Arabic matn as a single, cleanly citable
  Wikisource transcription — Arabic Wikisource search results returned
  commentary/supplement PDFs (`ملحة الإعراب`, `متممة الآجرومية`, various
  shurūḥ) rather than a directly fetchable plain-text primary transcription,
  and Shamela.ws (المكتبة الشاملة), which does appear to host the raw matn
  text, returned HTTP 403 on direct fetch (consistent with
  `docs/product/GROWTH_RESEARCH.md`'s own experience of some Indonesian
  government data portals blocking direct fetches). islam.ms's per-bab pages
  were used instead once found, and functioned as directly fetchable,
  primary-adjacent reproductions of the matn's own wording — see the
  primary-source list above.
* A second independent source for the full 18-item jawāzim list and the
  Manṣūbāt al-Asmāʾ internal 14-vs-15 arithmetic (§6).
