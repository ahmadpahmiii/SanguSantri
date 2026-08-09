#!/usr/bin/env ruby

require "json"

ROOT = File.expand_path(__dir__)

TOKENS = {
  quranBackground: "#050806",
  quranSurface: "#101713",
  quranSurfaceHigh: "#1A1C19",
  quranPrimary: "#7FDB9C",
  quranPrimaryContainer: "#00391C",
  quranOnPrimaryContainer: "#A1F5B9",
  quranArabicText: "#F1F1EB",
  quranTranslationText: "#C3C8C0",
  quranMutedText: "#95A099",
  quranOutline: "#2D3933",
  quranError: "#FFB4AB",
}.freeze

TAFSIR_6232 = JSON.parse(File.read(File.join(ROOT, "data/an-nas-114-tafsir-6232-response.json"))).fetch("data").first.freeze

CSS = <<~CSS.freeze
  @font-face{font-family:"LPMQ Isep Misbah";src:url("../../assets/quran-fonts/LPMQ-Isep-Misbah.ttf") format("truetype");font-display:block}
  @font-face{font-family:"Amiri Quran";src:url("../../assets/quran-fonts/AmiriQuran-Regular.ttf") format("truetype");font-display:block}
  :root{--bg:#050806;--surface:#101713;--surface-high:#1a1c19;--primary:#7fdb9c;--primary-container:#00391c;--on-primary:#a1f5b9;--text:#f1f1eb;--translation:#c3c8c0;--muted:#95a099;--outline:#2d3933;--error:#ffb4ab}
  *{box-sizing:border-box}html,body{width:360px;height:800px;margin:0;overflow:hidden;background:transparent;font-family:Inter,Roboto,Arial,sans-serif}.screen{position:relative;width:360px;height:800px;overflow:hidden;border-radius:28px;color:var(--text);background:var(--bg)}
  .status{height:30px;padding:0 20px;display:flex;align-items:center;justify-content:space-between;color:#e8ece8;font-size:12px;font-weight:650}.status-right{display:flex;gap:8px;align-items:center}.top{min-height:82px;padding:10px 16px 12px;display:flex;align-items:center;gap:12px;background:var(--surface);border-bottom:1px solid var(--outline)}.top.plain{background:var(--bg);border:0}.back,.icon{width:48px;height:48px;display:grid;place-items:center;border:0;border-radius:24px;color:var(--text);background:transparent;font-size:27px}.icon{font-size:20px}.top-copy{min-width:0;flex:1}.top h1{margin:0;font-size:23px;line-height:28px;letter-spacing:-.4px}.top p{margin:3px 0 0;color:var(--muted);font-size:13px}.actions{display:flex;margin-left:auto}.body{height:688px;padding:18px 16px 28px;overflow:hidden}.hub-body{height:688px;padding:14px 16px 24px;overflow:hidden}
  .continue{margin-bottom:16px;padding:16px;border:1px solid var(--outline);border-radius:20px;background:linear-gradient(135deg,#07351f,#101713)}.continue-head{display:flex;justify-content:space-between;align-items:center;color:var(--muted);font-size:13px}.continue-head strong{color:var(--text);font-size:15px}.continue-name{margin-top:14px;font-size:18px}.continue-meta{margin-top:5px;color:var(--muted);font-size:14px}.progress{height:4px;margin-top:14px;overflow:hidden;border-radius:4px;background:#34423b}.progress span{display:block;height:100%;border-radius:4px;background:var(--primary)}
  .tabs-viewport{height:52px;overflow:hidden;border-bottom:1px solid var(--outline)}.tabs{width:100%;height:52px;display:flex}.tab{flex:1;height:52px;display:grid;place-items:center;border-bottom:2px solid transparent;color:var(--muted);font-size:14px}.tab.active{color:var(--primary);border-color:var(--primary)}
  .search{height:48px;margin:16px 0 8px;padding:0 16px;display:flex;align-items:center;gap:11px;border:1px solid var(--outline);border-radius:18px;color:var(--muted);background:var(--surface);font-size:14px}.search-mark{font-size:19px}.list{overflow:hidden}.row{min-height:72px;padding:12px 8px;display:flex;align-items:center;gap:12px;border-bottom:1px solid var(--outline)}.number{width:42px;height:42px;flex:0 0 42px;display:grid;place-items:center;border:1px solid var(--outline);border-radius:15px;color:var(--on-primary);background:var(--surface);font-size:15px}.row-copy{min-width:0;flex:1;overflow:hidden}.row-title{font-size:16px;font-weight:650}.row-sub{margin-top:5px;overflow:hidden;color:var(--muted);font-size:13px;text-overflow:ellipsis;white-space:nowrap}.arabic-name{width:105px;flex:0 0 105px;overflow:hidden;color:var(--primary);font-family:"LPMQ Isep Misbah",serif;font-size:19px;text-align:end;direction:rtl}.chevron{color:var(--muted);font-size:22px}.empty{height:330px;display:flex;flex-direction:column;align-items:center;justify-content:center;text-align:center}.empty-mark{width:58px;height:58px;display:grid;place-items:center;border:1px solid var(--outline);border-radius:20px;color:var(--primary);background:var(--surface);font-size:25px}.empty h2{margin:18px 0 7px;font-size:18px}.empty p{max-width:270px;margin:0;color:var(--muted);font-size:14px;line-height:1.55}
  .center-state{height:688px;padding:28px;display:flex;flex-direction:column;align-items:center;justify-content:center;text-align:center}.state-mark{width:76px;height:76px;display:grid;place-items:center;border:1px solid var(--outline);border-radius:25px;color:var(--primary);background:var(--surface);font-size:32px}.state-mark.error{color:var(--error)}.center-state h2{margin:24px 0 9px;font-size:21px}.center-state p{max-width:290px;margin:0;color:var(--muted);font-size:14px;line-height:1.55}.big-progress{width:270px;height:7px;margin-top:24px;overflow:hidden;border-radius:7px;background:#26312b}.big-progress span{display:block;height:100%;border-radius:7px;background:var(--primary)}.state-count{margin-top:11px;color:var(--muted);font-size:13px}.button{min-width:132px;height:48px;margin-top:24px;padding:0 22px;border:0;border-radius:24px;color:#00391c;background:var(--primary);font-weight:750;font-size:14px}.secondary-button{color:var(--primary);background:var(--surface);border:1px solid var(--outline)}
  .notice{margin:10px 0;padding:10px 12px;border:1px solid var(--outline);border-radius:13px;color:var(--muted);background:var(--surface);font-size:12px;line-height:1.45}.notice strong{color:var(--text)}.notice.error{color:var(--error)}
  .reader-body{height:688px;padding:16px 18px 28px;overflow:hidden}.reader-row{padding:18px 4px;border-bottom:1px solid var(--outline)}.reader-arabic{margin:0;color:var(--text);font-family:"LPMQ Isep Misbah",serif;font-size:29px;line-height:1.7;text-align:right;direction:rtl}.reader-translation{margin:12px 0 0;color:var(--translation);font-size:14px;line-height:1.55}.reader-meta{margin-top:9px;color:var(--primary);font-size:12px}.footnote{margin-top:12px;padding:11px 12px;border-left:2px solid var(--primary);color:var(--muted);background:var(--surface);font-size:12px;line-height:1.5}.loading-row{height:120px;margin-bottom:10px;border-radius:16px;background:linear-gradient(90deg,var(--surface) 25%,#18221d 50%,var(--surface) 75%);background-size:220% 100%}.selected-row{background:var(--primary-container);margin:0 -18px;padding-left:22px;padding-right:22px}
  .scrim{position:absolute;inset:0;background:rgba(0,0,0,.62)}.sheet{position:absolute;left:0;right:0;bottom:0;max-height:610px;padding:9px 20px 28px;border-radius:26px 26px 0 0;background:var(--surface-high);box-shadow:0 -18px 54px rgba(0,0,0,.45)}.handle{width:38px;height:4px;margin:0 auto 18px;border-radius:4px;background:var(--muted)}.sheet-head{display:flex;align-items:flex-start;gap:12px}.sheet-head-copy{flex:1}.sheet h2{margin:0;font-size:20px}.sheet-source{margin-top:5px;color:var(--muted);font-size:12px}.close{width:48px;height:48px;margin-top:-8px;display:grid;place-items:center;border:0;border-radius:24px;color:var(--text);background:transparent;font-size:23px}.sheet-section{margin-top:20px}.sheet-label{margin-bottom:7px;color:var(--primary);font-size:13px;font-weight:750}.sheet-copy{margin:0;color:var(--translation);font-size:14px;line-height:1.55}.sheet-state{padding:35px 4px 50px;text-align:center}.sheet-state .spinner{width:36px;height:36px;margin:0 auto 17px;border:3px solid var(--outline);border-top-color:var(--primary);border-radius:50%}.sheet-state p{margin:0;color:var(--muted);font-size:14px}.cache-chip{display:inline-flex;margin-top:10px;padding:6px 10px;border-radius:12px;color:var(--on-primary);background:var(--primary-container);font-size:11px}
  .settings{height:688px;padding:18px 16px 30px;overflow:hidden}.preview{padding:16px;border:1px solid var(--outline);border-radius:18px;background:var(--surface)}.preview-arabic{font-family:"LPMQ Isep Misbah",serif;font-size:24px;line-height:2;text-align:center;direction:rtl}.section-title{margin:20px 0 10px;font-size:15px}.font-grid{display:grid;grid-template-columns:1fr 1fr;gap:9px}.font-card{min-height:76px;padding:11px;border:1px solid var(--outline);border-radius:15px;background:var(--surface)}.font-card.selected{border-color:var(--primary);background:var(--primary-container)}.font-card.disabled{opacity:.48}.font-name{font-size:12px;font-weight:700}.font-sample{margin-top:8px;color:var(--text);font-family:"Amiri Quran",serif;font-size:19px;text-align:right;direction:rtl}.control{margin-top:16px}.control-head{display:flex;justify-content:space-between;color:var(--translation);font-size:13px}.slider{height:5px;margin-top:11px;border-radius:5px;background:#34423b}.slider span{display:block;position:relative;height:100%;border-radius:5px;background:var(--primary)}.slider span:after{content:"";position:absolute;right:-7px;top:-5px;width:15px;height:15px;border-radius:50%;background:var(--primary)}.segmented{margin-top:11px;padding:3px;display:grid;grid-template-columns:1fr 1fr;border:1px solid var(--outline);border-radius:15px;background:var(--surface)}.segment{height:38px;display:grid;place-items:center;border-radius:12px;color:var(--muted);font-size:12px}.segment.active{color:var(--on-primary);background:var(--primary-container)}
  .source-body{height:688px;padding:22px 20px;overflow:hidden}.source-brand{width:58px;height:58px;display:grid;place-items:center;border:1px solid var(--outline);border-radius:20px;color:var(--primary);background:var(--surface);font-size:25px}.source-body h2{margin:20px 0 8px;font-size:20px}.source-body p{margin:0 0 17px;color:var(--translation);font-size:14px;line-height:1.58}.source-box{padding:15px;border:1px solid var(--outline);border-radius:16px;background:var(--surface)}.source-label{color:var(--muted);font-size:11px;text-transform:uppercase;letter-spacing:.7px}.source-value{margin-top:6px;color:var(--text);font-size:13px;line-height:1.5}.source-rule{margin:18px 0;border:0;border-top:1px solid var(--outline)}
  .activity-body{height:688px;padding:18px 16px 28px;overflow:hidden}.streak{padding:17px;border:1px solid var(--outline);border-radius:19px;background:var(--surface)}.streak-top{display:flex;justify-content:space-between}.streak strong{font-size:22px}.streak p{margin:4px 0 0;color:var(--muted);font-size:13px}.days{margin-top:14px;display:flex;justify-content:space-between}.day{width:34px;text-align:center;color:var(--muted);font-size:10px}.day i{width:28px;height:28px;margin:6px auto 0;display:grid;place-items:center;border-radius:50%;font-style:normal;background:#223029}.day.done i{color:#00391c;background:var(--primary)}.filter{display:flex;gap:8px;margin:18px 0 10px}.chip{padding:8px 12px;border:1px solid var(--outline);border-radius:16px;color:var(--muted);background:var(--surface);font-size:12px}.chip.active{color:var(--on-primary);background:var(--primary-container)}.activity-row{padding:15px 6px;display:flex;gap:12px;border-bottom:1px solid var(--outline)}.activity-icon{width:42px;height:42px;display:grid;place-items:center;border-radius:14px;color:var(--primary);background:var(--surface)}.activity-time{color:var(--muted);font-size:12px}
CSS

def status_bar
  '<div class="status"><span>20:26</span><span class="status-right"><span>▂▄▆█</span><span>⌁</span><span>82%</span></span></div>'
end

def top_bar(title, subtitle = nil, back: false, actions: true, plain: false)
  action_html = actions ? '<div class="actions"><button class="icon" aria-label="Tampilan">⚙</button><button class="icon" aria-label="Sumber">ⓘ</button></div>' : ""
  <<~HTML
    <header class="top#{plain ? ' plain' : ''}">
      #{back ? '<button class="back" aria-label="Kembali">‹</button>' : ''}
      <div class="top-copy"><h1>#{title}</h1>#{subtitle ? "<p>#{subtitle}</p>" : ""}</div>
      #{action_html}
    </header>
  HTML
end

def document(title, content)
  html = <<~HTML
    <!doctype html>
    <html lang="id">
      <head>
        <meta charset="utf-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <title>#{title}</title>
        <style>#{CSS}</style>
      </head>
      <body><main class="screen">#{status_bar}#{content}</main></body>
    </html>
  HTML
  html.gsub(/[ \t]+$/, "")
end

def continue_card(refresh: nil)
  refresh_note = refresh ? "<div class=\"notice #{refresh == :failed ? 'error' : ''}\">#{refresh == :failed ? 'Pembaruan gagal. Data offline tetap tersedia.' : 'Memperbarui data Kemenag di latar belakang…'}</div>" : ""
  <<~HTML
    <section class="continue">
      <div class="continue-head"><strong>▣ &nbsp;Terakhir dibaca</strong><span>Hari ini</span></div>
      <div class="continue-name">An-Nās</div>
      <div class="continue-meta">Halaman 604 • Ayat 2</div>
      <div class="progress"><span style="width:62%"></span></div>
    </section>
    #{refresh_note}
  HTML
end

def tabs(active)
  names = [["surah", "Surah"], ["juz", "Juz"], ["bookmark", "Bookmark"]]
  inner = names.map { |id, label| "<div class=\"tab #{active == id ? 'active' : ''}\">#{label}</div>" }.join
  "<div class=\"tabs-viewport\"><div class=\"tabs\">#{inner}</div></div>"
end

def hub(content, active:, search: nil, refresh: nil, show_continue: true)
  search_html = search ? "<div class=\"search\"><span class=\"search-mark\">⌕</span><span>#{search}</span></div>" : ""
  continue_html = show_continue ? continue_card(refresh: refresh) : ""
  top_bar("Al-Qur'an", "Al-Qur'an Kemenag", plain: true) +
    "<div class=\"hub-body\">#{continue_html}#{tabs(active)}#{search_html}#{content}</div>"
end

def surah_rows
  [
    [2, "Al-Baqarah", "Sapi · Madaniyyah · 286 ayat", "البقرة"],
    [3, "Āli 'Imrān", "Keluarga Imran · Madaniyyah · 200 ayat", "اٰل عمران"],
    [4, "An-Nisā'", "Perempuan · Madaniyyah · 176 ayat", "النساۤء"],
  ].map do |number, name, meta, arabic|
    "<div class=\"row\"><div class=\"number\">#{number}</div><div class=\"row-copy\"><div class=\"row-title\">#{name}</div><div class=\"row-sub\">#{meta}</div></div><div class=\"arabic-name\">#{arabic}</div></div>"
  end.join
end

def position_rows(kind)
  rows = if kind == :bookmark
           [["15", "Al-Fajr • Ayat 15", "Juz 30 · Halaman 593"], ["2", "An-Nās • Ayat 2", "Juz 30 · Halaman 604"]]
         else
           [["H", "An-Nās • Ayat 2", "Hari ini, 20.18 · Halaman 604"], ["K", "Al-Fajr • Ayat 15", "Kemarin, 05.42 · Halaman 593"]]
         end
  rows.map { |badge, title, meta| "<div class=\"row\"><div class=\"number\">#{badge}</div><div class=\"row-copy\"><div class=\"row-title\">#{title}</div><div class=\"row-sub\">#{meta}</div></div><span class=\"chevron\">›</span></div>" }.join
end

def center_state(title, copy, mark:, progress: nil, count: nil, action: nil, error: false)
  progress_html = progress ? "<div class=\"big-progress\"><span style=\"width:#{progress}%\"></span></div>" : ""
  count_html = count ? "<div class=\"state-count\">#{count}</div>" : ""
  action_html = action ? "<button class=\"button\">#{action}</button>" : ""
  "<div class=\"center-state\"><div class=\"state-mark #{error ? 'error' : ''}\">#{mark}</div><h2>#{title}</h2><p>#{copy}</p>#{progress_html}#{count_html}#{action_html}</div>"
end

def reader_top(subtitle = "Halaman 593 • Juz 30", title = "Al-Fajr")
  top_bar(title, subtitle, back: true, actions: false) .sub("</header>", '<div class="actions"><button class="icon">aA</button><button class="icon">☷</button></div></header>')
end

def translation_reader
  ayats = JSON.parse(File.read(File.join(ROOT, "data/al-fajr-89-kemenag-response.json"))).fetch("data").sort_by { |ayat| ayat.fetch("ayat") }.first(3)
  rows = ayats.map do |ayat|
    footnote = ayat.fetch("teks_foot").empty? ? "" : "<div class=\"footnote\">#{ayat.fetch("teks_foot")}</div>"
    <<~HTML
      <section class="reader-row">
        <p class="reader-arabic">#{ayat.fetch("teks_msi_usmani")}</p>
        <p class="reader-translation">#{ayat.fetch("terjemah")}</p>
        #{footnote}
        <div class="reader-meta">Ayat #{ayat.fetch("ayat")}</div>
      </section>
    HTML
  end.join
  reader_top + "<div class=\"reader-body\">#{rows}</div>"
end

def tafsir_background
  <<~HTML
    #{reader_top("Halaman 604 • Juz 30", "An-Nās")}
    <div class="reader-body">
      <section class="reader-row selected-row">
        <p class="reader-arabic">مَلِكِ النَّاسِۙ</p>
        <p class="reader-translation">raja manusia,</p>
        <div class="reader-meta">Ayat 2</div>
      </section>
      <section class="reader-row"><p class="reader-arabic">اِلٰهِ النَّاسِۙ</p><p class="reader-translation">sembahan manusia</p></section>
    </div><div class="scrim"></div>
  HTML
end

def tafsir_sheet(content, title: "An-Nās • Ayat 2")
  <<~HTML
    #{tafsir_background}
    <section class="sheet"><div class="handle"></div><div class="sheet-head"><div class="sheet-head-copy"><h2>#{title}</h2><div class="sheet-source">Tafsir Kemenag • Kementerian Agama RI</div></div><button class="close">×</button></div>#{content}</section>
  HTML
end

def tafsir_content(cache: nil)
  chip = cache ? "<div class=\"cache-chip\">#{cache}</div>" : ""
  <<~HTML
    #{chip}
    <div class="sheet-section"><div class="sheet-label">Tafsir Ringkas</div><p class="sheet-copy">#{TAFSIR_6232.fetch("teks")}</p></div>
    <div class="sheet-section"><div class="sheet-label">Tafsir Tahlili</div><p class="sheet-copy">#{TAFSIR_6232.fetch("tahlili")}</p></div>
  HTML
end

def settings_screen
  <<~HTML
    #{top_bar("Tampilan Al-Qur'an", "Perubahan langsung diterapkan", back: true, actions: false)}
    <div class="settings">
      <div class="preview"><div class="preview-arabic">وَالْفَجْرِۙ</div></div>
      <div class="section-title">Font Arab</div>
      <div class="font-grid">
        <div class="font-card selected"><div class="font-name">● LPMQ Isep Misbah</div><div class="font-sample" style="font-family:'LPMQ Isep Misbah'">وَالْفَجْرِۙ</div></div>
        <div class="font-card"><div class="font-name">○ Amiri Quran</div><div class="font-sample">وَالْفَجْرِۙ</div></div>
        <div class="font-card disabled"><div class="font-name">○ King Fahd</div><div class="font-sample">Belum tersedia</div></div>
      </div>
      <div class="control"><div class="control-head"><span>Ukuran Arab</span><strong>24 sp</strong></div><div class="slider"><span style="width:26%"></span></div></div>
      <div class="control"><div class="control-head"><span>Jarak baris Arab</span><strong>2,00×</strong></div><div class="slider"><span style="width:73%"></span></div></div>
      <div class="control"><div class="control-head"><span>Ukuran terjemahan</span><strong>16 sp</strong></div><div class="slider"><span style="width:44%"></span></div></div>
      <div class="control"><div class="control-head"><span>Tampilan bacaan</span></div><div class="segmented"><div class="segment active">Arab saja</div><div class="segment">Arab + terjemahan</div></div></div>
      <div class="control"><div class="control-head"><span>Kecerahan Quran</span><strong>72%</strong></div><div class="slider"><span style="width:72%"></span></div></div>
    </div>
  HTML
end

def source_screen
  <<~HTML
    #{top_bar("Sumber Al-Qur'an", nil, back: true, actions: false)}
    <div class="source-body">
      <div class="source-brand">▤</div><h2>Al-Qur'an Kemenag</h2>
      <p>Sumber teks Al-Qur'an, terjemahan, dan tafsir: Lajnah Pentashihan Mushaf Al-Qur'an, Kementerian Agama Republik Indonesia.</p>
      <div class="source-box"><div class="source-label">Data yang digunakan</div><div class="source-value">Nama dan metadata surah, teks Arab Mushaf Standar Indonesia, terjemahan, catatan kaki, serta tafsir Kemenag.</div></div>
      <hr class="source-rule" />
      <p>Data disimpan di perangkat agar bacaan tetap tersedia offline setelah penyiapan pertama. Tafsir dimuat saat dipilih dan kemudian dapat disimpan secara lokal.</p>
      <p>SanguSantri menggunakan data secara baca-saja dan tidak mengubah, menerjemahkan ulang, menggabungkan, atau mengoreksi isi dari Kemenag.</p>
      <div class="notice"><strong>Catatan</strong><br />SanguSantri bukan aplikasi resmi Kementerian Agama dan tidak menyiratkan dukungan institusional terhadap fitur lain di aplikasi.</div>
    </div>
  HTML
end

def activity_screen
  days = %w[S S R K J S M].each_with_index.map { |day, i| "<div class=\"day #{i < 5 ? 'done' : ''}\">#{day}<i>#{i < 5 ? '✓' : '·'}</i></div>" }.join
  <<~HTML
    #{top_bar("Aktivitas", "Riwayat amalan di perangkat", plain: true, actions: false)}
    <div class="activity-body">
      <section class="streak"><div class="streak-top"><div><strong>5 hari</strong><p>Streak amalan saat ini</p></div><div style="color:var(--primary);font-size:26px">⌁</div></div><div class="days">#{days}</div></section>
      <div class="filter"><div class="chip active">Semua</div><div class="chip">Al-Qur'an</div><div class="chip">Amaliyah</div><div class="chip">Tasbih</div></div>
      <div class="section-title">Hari ini</div>
      <div class="activity-row"><div class="activity-icon">▣</div><div class="row-copy"><div class="row-title">Membaca Al-Qur'an</div><div class="row-sub">An-Nās • Ayat 1–6</div></div><div class="activity-time">20.18</div></div>
      <div class="activity-row"><div class="activity-icon">◌</div><div class="row-copy"><div class="row-title">Tasbih</div><div class="row-sub">Istighfar • 100 hitungan</div></div><div class="activity-time">05.55</div></div>
      <div class="section-title">Kemarin</div>
      <div class="activity-row"><div class="activity-icon">▣</div><div class="row-copy"><div class="row-title">Membaca Al-Qur'an</div><div class="row-sub">Al-Fajr • Ayat 1–15</div></div><div class="activity-time">05.42</div></div>
    </div>
  HTML
end

def state_meta(screen, state, source_data: "No Quran payload rendered")
  {
    artifactType: "local-design-reference",
    screen: screen,
    release: "0.0.6",
    logicalFrame: { width: 360, height: 800, renderScale: 2 },
    state: state,
    sourceData: source_data,
    tokens: TOKENS,
    constraints: [
      "Dark-only Quran surface",
      "No audio, Latin transliteration, copy, or share controls",
      "Runtime content renders from validated Kemenag data in Room, never this reference file",
    ],
    designReference: { page: "03 Al-Qur'an Kemenag", nodeId: nil },
  }
end

frames = []
add = lambda do |slug, screen, content, state, source_data = "No Quran payload rendered"|
  File.write(File.join(ROOT, "#{slug}.html"), document("Al-Qur'an — #{screen}", content))
  meta = state_meta(screen, state, source_data: source_data).merge(sourceFiles: { editable: "#{slug}.html", preview: "#{slug}.png" })
  File.write(File.join(ROOT, "#{slug}.json"), JSON.pretty_generate(meta) + "\n")
  frames << meta.merge(slug: slug)
end

add.call("01-quran-hub-surah", "Beranda Al-Qur'an — Surah", hub("<div class=\"list\">#{surah_rows}</div>", active: "surah", search: "Cari nama atau nomor surah"), { tab: "Surah", content: "populated" }, "Exact product-owner-supplied Kemenag metadata for surah 2–4")
add.call("02-quran-hub-juz", "Beranda Al-Qur'an — Juz", hub('<div class="list"><div class="row"><div class="number">1</div><div class="row-copy"><div class="row-title">Juz 1</div><div class="row-sub">Posisi awal mengikuti data lokal Kemenag</div></div><span class="chevron">›</span></div><div class="row"><div class="number">2</div><div class="row-copy"><div class="row-title">Juz 2</div><div class="row-sub">Posisi awal mengikuti data lokal Kemenag</div></div><span class="chevron">›</span></div><div class="row"><div class="number">30</div><div class="row-copy"><div class="row-title">Juz 30</div><div class="row-sub">Al-Fajr • Ayat 1 • Halaman 593</div></div><span class="chevron">›</span></div></div>', active: "juz"), { tab: "Juz", content: "populated" }, "Juz 30 start is derived from supplied Al-Fajr response; other row subtitles intentionally avoid invented mappings")
add.call("03a-quran-hub-bookmark-populated", "Beranda Al-Qur'an — Bookmark terisi", hub("<div class=\"list\">#{position_rows(:bookmark)}</div>", active: "bookmark"), { tab: "Bookmark", content: "populated" }, "Positions use supplied Al-Fajr and An-Nas API metadata")
add.call("03b-quran-hub-bookmark-empty", "Beranda Al-Qur'an — Bookmark kosong", hub('<div class="empty"><div class="empty-mark">☆</div><h2>Belum ada bookmark</h2><p>Tekan lama ayat saat membaca, lalu pilih Tambahkan bookmark.</p></div>', active: "bookmark"), { tab: "Bookmark", content: "empty" })
add.call("04a-quran-hub-recent-populated", "Beranda Al-Qur'an — Kartu terakhir dibaca", hub("<div class=\"list\">#{surah_rows}</div>", active: "surah", search: "Cari nama atau nomor surah"), { tab: "Surah", lastReadCard: "present" }, "Card position uses supplied An-Nas API metadata; list uses supplied Kemenag metadata for surah 2–4")
add.call("04b-quran-hub-recent-empty", "Beranda Al-Qur'an — Belum ada posisi terakhir", hub("<div class=\"list\">#{surah_rows}</div>", active: "surah", search: "Cari nama atau nomor surah", show_continue: false), { tab: "Surah", lastReadCard: "absent" }, "List uses exact product-owner-supplied Kemenag metadata for surah 2–4")
add.call("05a-initial-checking", "Penyiapan awal — Memeriksa data", top_bar("Al-Qur'an", "Al-Qur'an Kemenag", plain: true, actions: false) + center_state("Memeriksa data Al-Qur'an…", "Sebentar, kami memeriksa apakah data lengkap sudah tersedia di perangkat.", mark: "◌"), { phase: "checking" })
add.call("05b-initial-preparation", "Penyiapan awal — Berlangsung", top_bar("Al-Qur'an", "Al-Qur'an Kemenag", plain: true, actions: false) + center_state("Menyiapkan Al-Qur'an Kemenag…", "Penyiapan pertama membutuhkan internet. Jangan tutup aplikasi sampai selesai.", mark: "↓", progress: 63, count: "72 dari 114 surah"), { phase: "preparing", completedSurah: 72, totalSurah: 114 })
add.call("06a-initial-offline-error", "Penyiapan awal — Offline", top_bar("Al-Qur'an", "Al-Qur'an Kemenag", plain: true, actions: false) + center_state("Al-Qur'an belum tersedia offline", "Hubungkan perangkat ke internet satu kali untuk menyiapkan data Al-Qur'an Kemenag.", mark: "!", action: "Coba lagi", error: true), { phase: "error", reason: "offline-no-local-data" })
add.call("06b-initial-preparation-error", "Penyiapan awal — Gagal", top_bar("Al-Qur'an", "Al-Qur'an Kemenag", plain: true, actions: false) + center_state("Al-Qur'an belum dapat disiapkan", "Terjadi kendala saat menyiapkan data. Pastikan koneksi tersedia lalu coba lagi.", mark: "!", action: "Coba lagi", error: true), { phase: "error", reason: "network-http-parse-or-validation" })
add.call("06c-hub-background-refresh", "Beranda Al-Qur'an — Memperbarui", hub("<div class=\"list\">#{surah_rows}</div>", active: "surah", search: "Cari nama atau nomor surah", refresh: :loading), { tab: "Surah", refresh: "background-running", contentRemainsAvailable: true }, "Exact product-owner-supplied Kemenag metadata for surah 2–4")
add.call("06d-hub-refresh-failed-cache-kept", "Beranda Al-Qur'an — Pembaruan gagal", hub("<div class=\"list\">#{surah_rows}</div>", active: "surah", search: "Cari nama atau nomor surah", refresh: :failed), { tab: "Surah", refresh: "failed-existing-cache-kept", contentRemainsAvailable: true }, "Exact product-owner-supplied Kemenag metadata for surah 2–4")
add.call("08-reader-arab-translation", "Reader — Arab + terjemahan", translation_reader, { displayChoice: "Arab + terjemahan", surah: "Al-Fajr", page: 593, juz: 30 }, "Exact sorted teks_msi_usmani and terjemah from preserved Al-Fajr Kemenag response; Latin field excluded")
add.call("11-tafsir-loading", "Tafsir Kemenag — Memuat", tafsir_sheet('<div class="sheet-state"><div class="spinner"></div><p>Memuat tafsir Kemenag…</p></div>'), { tafsir: "loading", ayatId: 6232 }, "Selected ayat uses supplied An-Nas response; no tafsir content rendered")
add.call("12-tafsir-success", "Tafsir Kemenag — Berhasil", tafsir_sheet(tafsir_content), { tafsir: "success", ayatId: 6232 }, "Exact product-owner-supplied tafsir response for ayat ID 6232")
add.call("13a-tafsir-cached-refreshing", "Tafsir Kemenag — Cache diperbarui", tafsir_sheet(tafsir_content(cache: "Tersimpan offline • memperbarui…")), { tafsir: "cached-stale-while-refreshing", ayatId: 6232 }, "Exact product-owner-supplied tafsir response for ayat ID 6232")
add.call("13b-tafsir-offline-no-cache", "Tafsir Kemenag — Tidak tersedia offline", tafsir_sheet('<div class="sheet-state"><div class="state-mark error" style="margin:0 auto 18px">!</div><h2 style="font-size:17px">Tafsir belum tersedia offline</h2><p>Hubungkan ke internet untuk memuat tafsir ayat ini.</p></div>'), { tafsir: "offline-no-cache", ayatId: 6232 }, "Selected ayat uses supplied An-Nas response; no tafsir content rendered")
add.call("13c-tafsir-error-retry", "Tafsir Kemenag — Gagal", tafsir_sheet('<div class="sheet-state"><div class="state-mark error" style="margin:0 auto 18px">!</div><h2 style="font-size:17px">Tafsir belum dapat dimuat</h2><p>Coba kembali beberapa saat lagi.</p><button class="button">Coba lagi</button></div>'), { tafsir: "retryable-error", ayatId: 6232 }, "Selected ayat uses supplied An-Nas response; no tafsir content rendered")
add.call("14-quran-display-settings", "Tampilan Al-Qur'an", settings_screen, { destination: "display-settings", selectedFont: "LPMQ Isep Misbah", kingFahd: "placeholder-unavailable" }, "Live preview is exact Al-Fajr ayat 1 from preserved Kemenag response")
add.call("15-quran-source", "Sumber Al-Qur'an", source_screen, { destination: "source", access: "read-only" })
add.call("16-activity-quran-session", "Aktivitas — Sesi Al-Qur'an", activity_screen, { destination: "activity", filter: "all", quranSessionVisible: true }, "Quran session positions use supplied An-Nas and Al-Fajr metadata; timestamps are local presentation fixtures")
add.call("17-reader-loading", "Reader — Memuat dari Room", reader_top + '<div class="reader-body"><div class="loading-row"></div><div class="loading-row"></div><div class="loading-row"></div></div>', { reader: "loading-from-room", target: "Al-Fajr page 593" })
add.call("18-reader-invalid-target", "Reader — Target tidak ditemukan", reader_top("Posisi tidak tersedia", "Al-Qur'an") + center_state("Posisi bacaan tidak ditemukan", "Data mungkin telah diperbarui. Kembali ke daftar surah untuk memilih posisi lain.", mark: "!", action: "Kembali ke daftar", error: true), { reader: "invalid-or-deleted-target" })

catalog = {
  artifactType: "quran-visual-catalog",
  release: "0.0.6",
  generatedBy: "generate-quran-catalog.rb",
  frameCount: frames.length + 3,
  generatedFrames: frames.map { |frame| { slug: frame.fetch(:slug), screen: frame.fetch(:screen), state: frame.fetch(:state) } },
  retainedApprovedFrames: [
    { slug: "09-flowing-reader-arab-only-page", screen: "Flowing reader — Arab saja — full page" },
    { slug: "09b-flowing-reader-arab-only-selected", screen: "Arab-only long-press selected range" },
    { slug: "10-ayat-action-sheet", screen: "Ayat action sheet" },
  ],
  runtimeRule: "HTML/JSON files are design references only. Production Quran content flows from validated Kemenag responses into Room.",
}
File.write(File.join(ROOT, "00-quran-state-catalog.json"), JSON.pretty_generate(catalog) + "\n")

options = (catalog[:generatedFrames] + catalog[:retainedApprovedFrames]).map do |frame|
  "<option value=\"#{frame[:slug]}.html\">#{frame[:screen]}</option>"
end.join
catalog_html = <<~HTML
  <!doctype html><html lang="id"><head><meta charset="utf-8" /><meta name="viewport" content="width=device-width,initial-scale=1" /><title>Katalog state Al-Qur'an</title><style>*{box-sizing:border-box}body{margin:0;padding:24px;display:grid;place-items:center;gap:18px;color:#f1f1eb;background:#151716;font-family:Inter,Roboto,Arial,sans-serif}label{width:360px;color:#c3c8c0;font-size:14px}select{width:100%;height:48px;margin-top:8px;padding:0 14px;border:1px solid #4b6658;border-radius:14px;color:#f1f1eb;background:#101713;font:inherit}iframe{width:360px;height:800px;border:0;border-radius:28px;box-shadow:0 22px 70px rgba(0,0,0,.45)}</style></head><body><label>Layar atau state<select id="screen">#{options}</select></label><iframe id="preview" src="#{catalog[:generatedFrames].first[:slug]}.html"></iframe><script>const picker=document.getElementById('screen');const preview=document.getElementById('preview');picker.addEventListener('change',()=>{preview.src=picker.value});</script></body></html>
HTML
File.write(File.join(ROOT, "00-quran-state-catalog.html"), catalog_html)

puts "Generated #{frames.length} frame triplets plus catalog metadata"
