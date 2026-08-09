#!/usr/bin/env ruby
# frozen_string_literal: true

require "json"
require "date"
require "cgi"

OUTPUT_DIR = File.expand_path(__dir__)
ARABIC_INDIC = "٠١٢٣٤٥٦٧٨٩".chars.freeze
PASARAN = %w[Legi Pahing Pon Wage Kliwon].freeze
DAY_NAMES = %w[Ahad Senin Selasa Rabu Kamis Jumat Sabtu].freeze
MONTH_NAMES = %w[Januari Februari Maret April Mei Juni Juli Agustus September Oktober November Desember].freeze

STATES = [
  {
    id: "01-calendar-overview-light",
    name: "Kalender — Ringkasan",
    description: "Current-month light state, 8 August 2026 selected, all agenda categories.",
    theme: :light,
    selected: 8,
    filter: "Semua",
    sheet: false,
  },
  {
    id: "02-calendar-overview-dark",
    name: "Kalender — Tema gelap",
    description: "Dark state with an official religious holiday selected.",
    theme: :dark,
    selected: 25,
    filter: "Semua",
    sheet: false,
  },
  {
    id: "03-calendar-fasting-filter",
    name: "Kalender — Filter Puasa",
    description: "Non-weekly fasting filter with Ayyamul Bidh grouped as one date range.",
    theme: :light,
    selected: 26,
    filter: "Puasa",
    sheet: false,
  },
  {
    id: "04-calendar-source-sheet",
    name: "Kalender — Sumber & metode",
    description: "Modal source boundary explaining Umm al-Qura, Pancawara, and local bundle provenance.",
    theme: :light,
    selected: 8,
    filter: "Semua",
    sheet: true,
  },
].freeze

EVENTS = {
  17 => [:holiday],
  25 => [:holiday],
  26 => [:fast],
  27 => [:fast],
  28 => [:fast],
}.freeze

AGENDA = [
  {
    category: "holiday",
    date_badge: "17",
    date_label: "17 Agustus",
    title: "Hari Kemerdekaan RI",
    meta: "Senin · 4 Rabiulawal 1448",
    status: "Hari libur resmi",
  },
  {
    category: "holiday",
    date_badge: "25",
    date_label: "25 Agustus",
    title: "Maulid Nabi Muhammad saw.",
    meta: "Selasa · 12 Rabiulawal 1448",
    status: "Hari besar & libur",
  },
  {
    category: "fast",
    date_badge: "26–28",
    date_label: "26–28 Agustus",
    title: "Puasa Ayyamul Bidh",
    meta: "13–15 Rabiulawal 1448",
    status: "Puasa non-mingguan",
  },
].freeze

def arabic_indic(value)
  value.to_s.chars.map { |char| char.match?(/\d/) ? ARABIC_INDIC[char.to_i] : char }.join
end

def pasaran_for(date)
  anchor = Date.new(2026, 8, 8)
  anchor_index = PASARAN.index("Pahing")
  PASARAN[(anchor_index + (date - anchor).to_i) % PASARAN.length]
end

def hijri_for(date)
  if date.month == 7
    [date.day - 14, "Safar"]
  elsif date.month == 8 && date.day <= 13
    [date.day + 17, "Safar"]
  elsif date.month == 8
    [date.day - 13, "Rabiulawal"]
  else
    [date.day + 18, "Rabiulawal"]
  end
end

def selected_summary(day)
  summaries = {
    8 => ["Sabtu Pahing", "8 Agustus 2026 · 25 Safar 1448"],
    25 => ["Selasa Wage", "25 Agustus 2026 · 12 Rabiulawal 1448"],
    26 => ["Rabu Kliwon", "26 Agustus 2026 · 13 Rabiulawal 1448"],
  }
  summaries.fetch(day)
end

def day_cells(selected)
  start_date = Date.new(2026, 7, 26)
  (0...42).map do |offset|
    date = start_date + offset
    hijri_day, hijri_month = hijri_for(date)
    in_month = date.month == 8
    classes = ["day-cell"]
    classes << "outside" unless in_month
    classes << "sunday" if date.sunday?
    classes << "official" if in_month && [17, 25].include?(date.day)
    classes << "selected" if in_month && date.day == selected
    dots = in_month ? EVENTS.fetch(date.day, []) : []
    dot_names = dots.map { |dot| dot == :fast ? "agenda puasa" : "hari besar atau libur" }
    semantic = [
      "#{DAY_NAMES[date.wday]}, #{date.day} #{MONTH_NAMES[date.month - 1]} #{date.year}",
      "#{hijri_day} #{hijri_month} 1448",
      pasaran_for(date),
      dot_names.join(" dan "),
    ].reject(&:empty?).join(", ")

    <<~HTML
      <div class="#{classes.join(' ')}" role="gridcell" aria-label="#{CGI.escapeHTML(semantic)}">
        <span class="hijri-number" aria-hidden="true">#{arabic_indic(hijri_day)}</span>
        <strong class="gregorian-number">#{date.day}</strong>
        <span class="pasaran">#{pasaran_for(date)}</span>
        <span class="event-dots" aria-hidden="true">#{dots.map { |dot| "<i class=\"dot #{dot}\"></i>" }.join}</span>
      </div>
    HTML
  end.join
end

def agenda_rows(filter)
  visible = filter == "Puasa" ? AGENDA.select { |item| item[:category] == "fast" } : AGENDA
  visible.map do |item|
    <<~HTML
      <article class="agenda-row">
        <div class="date-badge #{item[:category]}">
          <small>Agu</small>
          <strong>#{item[:date_badge]}</strong>
        </div>
        <div class="agenda-copy">
          <h3>#{item[:title]}</h3>
          <p>#{item[:meta]}</p>
          <span class="agenda-status #{item[:category]}">#{item[:status]}</span>
        </div>
        <button class="row-info" aria-label="Lihat sumber #{item[:title]}">i</button>
      </article>
    HTML
  end.join
end

def source_sheet
  <<~HTML
    <div class="scrim"></div>
    <section class="source-sheet" role="dialog" aria-modal="true" aria-label="Sumber dan metode kalender">
      <div class="sheet-handle"></div>
      <div class="sheet-heading">
        <div class="source-icon">i</div>
        <div><small>TENTANG KALENDER</small><h2>Sumber & metode</h2></div>
        <button aria-label="Tutup">×</button>
      </div>
      <div class="source-block">
        <strong>Tanggal Hijriah</strong>
        <p>Dihitung offline memakai kalender Umm al-Qura bawaan Android. Hasil dapat berbeda dari Kalender Hijriah Indonesia Kementerian Agama dan penetapan resmi.</p>
        <span class="method-badge">Perhitungan Umm al-Qura</span>
      </div>
      <div class="source-block compact-source">
        <strong>Pasaran & agenda</strong>
        <p>Pasaran dihitung dari siklus Pancawara. Agenda puasa dan libur berasal dari bundle lokal terkurasi dengan sumber per item.</p>
      </div>
      <button class="primary-button">Mengerti</button>
    </section>
  HTML
end

def frame_html(state)
  summary_title, summary_date = selected_summary(state[:selected])
  theme_class = state[:theme] == :dark ? "dark" : "light"
  filters = ["Semua", "Puasa", "Hari besar & libur"].map do |filter|
    selected = filter == state[:filter] ? " selected" : ""
    "<button class=\"filter#{selected}\">#{filter}</button>"
  end.join

  <<~HTML
    <!doctype html>
    <html lang="id">
    <head>
      <meta charset="utf-8">
      <meta name="viewport" content="width=360,initial-scale=1">
      <title>#{state[:name]}</title>
      <style>
        :root { color-scheme: light; --bg:#F6F6F2; --surface:#FFFFFF; --surface-soft:#EEF2ED; --text:#17201C; --muted:#68736D; --line:#DCE3DE; --teal:#176B5C; --teal-soft:#DCEFE9; --amber:#D89713; --amber-soft:#FFF1CB; --coral:#C94E4E; --coral-soft:#FBE3E1; --shadow:0 10px 30px rgba(25,45,36,.12); }
        * { box-sizing:border-box; }
        html, body { margin:0; width:360px; height:800px; overflow:hidden; font-family:Inter, "Noto Sans", system-ui, sans-serif; background:var(--bg); color:var(--text); }
        body.dark { color-scheme:dark; --bg:#101512; --surface:#18201C; --surface-soft:#222C27; --text:#F4F6F4; --muted:#A8B2AC; --line:#35413B; --teal:#62D2B8; --teal-soft:#193E35; --amber:#F3BE4E; --amber-soft:#473918; --coral:#F27E78; --coral-soft:#4A2626; --shadow:0 14px 32px rgba(0,0,0,.35); }
        button { color:inherit; font:inherit; }
        .phone { position:relative; width:360px; height:800px; overflow:hidden; background:var(--bg); }
        .status { height:24px; padding:7px 17px 0; display:flex; justify-content:space-between; font-size:10px; font-weight:700; }
        .topbar { height:52px; padding:0 14px; display:flex; align-items:center; gap:10px; background:var(--surface); border-bottom:1px solid var(--line); }
        .icon-button { width:36px; height:36px; border:0; background:transparent; border-radius:18px; display:grid; place-items:center; font-size:22px; }
        .top-title { flex:1; min-width:0; }
        .top-title strong { display:block; font-size:15px; letter-spacing:-.2px; }
        .top-title small { display:block; margin-top:1px; font-size:10px; color:var(--muted); }
        .today-button { border:1px solid var(--line); background:var(--surface-soft); border-radius:999px; padding:6px 10px; font-size:10px; font-weight:750; }
        .month-header { height:66px; padding:9px 13px; display:grid; grid-template-columns:34px 1fr 34px; align-items:center; text-align:center; background:var(--surface); }
        .month-header button { border:0; background:transparent; color:var(--teal); font-size:27px; }
        .month-title h1 { margin:0; font-size:19px; line-height:1.2; letter-spacing:-.4px; }
        .month-title p { margin:3px 0 0; color:var(--muted); font-size:11px; }
        .weekdays { height:34px; padding:0 4px; display:grid; grid-template-columns:repeat(7,1fr); align-items:center; background:var(--surface-soft); border-block:1px solid var(--line); }
        .weekdays span { text-align:center; font-size:9px; font-weight:700; color:var(--muted); }
        .weekdays span:first-child { color:var(--coral); }
        .calendar-grid { height:282px; padding:0 4px; display:grid; grid-template-columns:repeat(7,1fr); grid-template-rows:repeat(6,47px); background:var(--surface); border-bottom:1px solid var(--line); }
        .day-cell { position:relative; min-width:0; padding:6px 2px 2px; display:flex; flex-direction:column; align-items:center; border-right:1px solid var(--line); border-bottom:1px solid var(--line); }
        .day-cell:nth-child(7n) { border-right:0; }
        .gregorian-number { font-size:16px; line-height:18px; font-variant-numeric:tabular-nums; }
        .hijri-number { position:absolute; top:3px; right:5px; font-family:"Noto Naskh Arabic", "Geeza Pro", serif; font-size:9px; line-height:12px; color:var(--muted); }
        .pasaran { margin-top:1px; font-size:8px; line-height:10px; color:var(--muted); }
        .outside { opacity:.35; }
        .sunday .gregorian-number, .official .gregorian-number { color:var(--coral); }
        .selected { z-index:1; margin:2px; padding-top:4px; border:2px solid var(--teal) !important; border-radius:9px; background:var(--teal-soft); box-shadow:inset 0 0 0 1px color-mix(in srgb, var(--teal) 20%, transparent); }
        .selected:not(.sunday):not(.official) .gregorian-number { color:var(--teal); }
        .event-dots { position:absolute; bottom:2px; left:0; right:0; display:flex; justify-content:center; gap:2px; height:4px; }
        .dot { width:4px; height:4px; border-radius:50%; }
        .dot.fast { background:var(--amber); } .dot.holiday { background:var(--coral); }
        .selected-summary { height:48px; padding:7px 15px; display:flex; align-items:center; gap:9px; background:var(--surface); border-bottom:1px solid var(--line); }
        .selected-mark { width:4px; height:28px; border-radius:4px; background:var(--teal); }
        .selected-summary strong { display:block; font-size:12px; }
        .selected-summary span { display:block; margin-top:2px; font-size:10px; color:var(--muted); }
        .agenda { height:294px; padding:9px 13px 0; background:var(--bg); }
        .agenda-header { display:flex; align-items:center; justify-content:space-between; margin-bottom:7px; }
        .agenda-header h2 { margin:0; font-size:14px; letter-spacing:-.2px; }
        .legend { display:flex; align-items:center; gap:8px; font-size:8px; color:var(--muted); }
        .legend span { display:flex; align-items:center; gap:3px; }
        .filters { display:flex; gap:5px; margin-bottom:7px; overflow:hidden; }
        .filter { flex:none; height:25px; padding:0 9px; border:1px solid var(--line); border-radius:999px; background:var(--surface); color:var(--muted); font-size:9px; font-weight:700; white-space:nowrap; }
        .filter.selected { margin:0; padding:0 9px; border:1px solid var(--teal) !important; border-radius:999px; background:var(--teal-soft); color:var(--teal); box-shadow:none; }
        .agenda-list { overflow:hidden; border:1px solid var(--line); border-radius:12px; background:var(--surface); }
        .agenda-row { min-height:58px; padding:7px 8px; display:flex; gap:8px; align-items:flex-start; border-bottom:1px solid var(--line); }
        .agenda-row:last-child { border-bottom:0; }
        .date-badge { width:39px; min-width:39px; height:43px; display:flex; flex-direction:column; justify-content:center; align-items:center; border:1px solid var(--line); border-radius:9px; background:var(--surface-soft); }
        .date-badge small { font-size:8px; color:var(--muted); }
        .date-badge strong { margin-top:1px; font-size:13px; color:var(--coral); }
        .date-badge.fast strong { color:var(--amber); font-size:11px; }
        .agenda-copy { flex:1; min-width:0; }
        .agenda-copy h3 { margin:0; font-size:11px; line-height:14px; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; }
        .agenda-copy p { margin:2px 0 3px; font-size:8.5px; line-height:11px; color:var(--muted); }
        .agenda-status { display:inline-flex; padding:2px 5px; border-radius:999px; font-size:7px; font-weight:800; }
        .agenda-status.holiday { color:var(--coral); background:var(--coral-soft); }
        .agenda-status.fast { color:#8C6200; background:var(--amber-soft); }
        .dark .agenda-status.fast { color:var(--amber); }
        .row-info { width:24px; height:24px; border:0; border-radius:12px; background:var(--surface-soft); color:var(--muted); font:700 10px Georgia,serif; }
        .scrim { position:absolute; inset:0; z-index:20; background:rgba(6,12,9,.56); }
        .source-sheet { position:absolute; z-index:21; left:0; right:0; bottom:0; min-height:405px; padding:8px 18px 18px; border-radius:24px 24px 0 0; background:var(--surface); box-shadow:var(--shadow); }
        .sheet-handle { width:36px; height:4px; margin:0 auto 15px; border-radius:4px; background:var(--line); }
        .sheet-heading { display:flex; align-items:center; gap:10px; margin-bottom:15px; }
        .source-icon { width:36px; height:36px; display:grid; place-items:center; border-radius:12px; background:var(--teal-soft); color:var(--teal); font:700 17px Georgia,serif; }
        .sheet-heading div:nth-child(2) { flex:1; }
        .sheet-heading small { display:block; color:var(--teal); font-size:8px; font-weight:850; letter-spacing:.8px; }
        .sheet-heading h2 { margin:2px 0 0; font-size:19px; }
        .sheet-heading button { width:32px; height:32px; border:0; border-radius:16px; background:var(--surface-soft); color:var(--muted); font-size:20px; }
        .source-block { padding:13px; border:1px solid var(--line); border-radius:13px; background:var(--surface-soft); }
        .source-block + .source-block { margin-top:9px; }
        .source-block strong { font-size:12px; }
        .source-block p { margin:5px 0 9px; font-size:10px; line-height:15px; color:var(--muted); }
        .compact-source p { margin-bottom:0; }
        .method-badge { display:inline-flex; padding:4px 7px; border-radius:999px; background:var(--teal-soft); color:var(--teal); font-size:8px; font-weight:800; }
        .primary-button { width:100%; height:40px; margin-top:12px; border:0; border-radius:12px; background:var(--teal); color:white; font-weight:800; font-size:12px; }
      </style>
    </head>
    <body class="#{theme_class}">
      <main class="phone">
        <div class="status"><span>07.22</span><span>4G+ · 55%</span></div>
        <header class="topbar">
          <button class="icon-button" aria-label="Kembali">‹</button>
          <div class="top-title"><strong>Kalender Hijriah</strong><small>#{summary_title} · #{summary_date}</small></div>
          <button class="today-button">Hari ini</button>
          <button class="icon-button" aria-label="Sumber dan metode">ⓘ</button>
        </header>
        <section class="month-header">
          <button aria-label="Bulan sebelumnya">‹</button>
          <div class="month-title"><h1>Agustus 2026</h1><p>Safar – Rabiulawal 1448</p></div>
          <button aria-label="Bulan berikutnya">›</button>
        </section>
        <div class="weekdays" aria-hidden="true"><span>Ahad</span><span>Senin</span><span>Selasa</span><span>Rabu</span><span>Kamis</span><span>Jumat</span><span>Sabtu</span></div>
        <section class="calendar-grid" role="grid" aria-label="Kalender Agustus 2026">#{day_cells(state[:selected])}</section>
        <section class="selected-summary"><div class="selected-mark"></div><div><strong>#{summary_title}</strong><span>#{summary_date}</span></div></section>
        <section class="agenda">
          <div class="agenda-header"><h2>Agenda bulan ini</h2><div class="legend"><span><i class="dot fast"></i> Puasa</span><span><i class="dot holiday"></i> Hari besar/libur</span></div></div>
          <div class="filters">#{filters}</div>
          <div class="agenda-list">#{agenda_rows(state[:filter])}</div>
        </section>
        #{state[:sheet] ? source_sheet : ""}
      </main>
    </body>
    </html>
  HTML
end

def sidecar(state)
  {
    artifactType: "local-design-reference",
    feature: "Kalender Hijriah",
    targetRelease: "0.0.7",
    screen: state[:name],
    state: {
      theme: state[:theme],
      visibleMonth: "2026-08",
      selectedGregorianDay: state[:selected],
      agendaFilter: state[:filter],
      sourceSheetOpen: state[:sheet],
    },
    logicalFrame: { width: 360, height: 800, unit: "px" },
    preview: { width: 720, height: 1600, deviceScaleFactor: 2 },
    designDecisions: {
      weekdayLabels: DAY_NAMES,
      gregorianNumerals: "Latin",
      smallHijriCellNumerals: "Arabic-Indic",
      pasaranOnly: PASARAN,
      weeklyFastAgenda: "excluded",
      multiDayAgenda: "grouped into one row",
      redGregorianDate: "Sunday or sourced official holiday only",
      dots: { amber: "fasting", coral: "religious observance or official holiday" },
    },
    sourceBoundary: {
      hijri: "Android java.time.chrono.HijrahDate / Umm al-Qura calculation",
      pasaran: "Local Pancawara cycle; Friday Legi, 8 July 1633 anchor",
      agenda: "Versioned curated local bundle; fixture is not runtime data",
      runtimeNetwork: false,
    },
    accessibility: {
      visualArabicDigitsOnly: true,
      semanticDateLanguage: "Indonesian full date",
      colourIsNotSoleIndicator: true,
      minimumTouchTargetDp: 48,
    },
    figma: {
      intendedPage: "Kalender Hijriah",
      nodeId: nil,
      note: "Record node ID in docs/design/FIGMA_HANDOFF.md after recreation/import.",
    },
    sourceFiles: ["#{state[:id]}.html", "#{state[:id]}.png"],
  }
end

STATES.each do |state|
  File.write(File.join(OUTPUT_DIR, "#{state[:id]}.html"), frame_html(state))
  File.write(File.join(OUTPUT_DIR, "#{state[:id]}.json"), JSON.pretty_generate(sidecar(state)) + "\n")
end

catalog = {
  artifactType: "local-design-reference-catalog",
  feature: "Kalender Hijriah",
  targetRelease: "0.0.7",
  generatedBy: File.basename(__FILE__),
  states: STATES.map do |state|
    {
      id: state[:id],
      name: state[:name],
      description: state[:description],
      html: "#{state[:id]}.html",
      json: "#{state[:id]}.json",
      png: "#{state[:id]}.png",
    }
  end,
}
File.write(File.join(OUTPUT_DIR, "00-hijri-calendar-state-catalog.json"), JSON.pretty_generate(catalog) + "\n")

cards = STATES.map do |state|
  <<~HTML
    <article>
      <div class="preview"><iframe src="#{state[:id]}.html" title="#{state[:name]}"></iframe></div>
      <div class="copy"><span>#{state[:id].split('-').first}</span><h2>#{state[:name]}</h2><p>#{state[:description]}</p><a href="#{state[:id]}.html">Buka 360 × 800</a></div>
    </article>
  HTML
end.join

catalog_html = <<~HTML
  <!doctype html>
  <html lang="id">
  <head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width,initial-scale=1">
    <title>Kalender Hijriah — State catalog</title>
    <style>
      *{box-sizing:border-box} body{margin:0;background:#EEF1ED;color:#17201C;font-family:Inter,system-ui,sans-serif} header{padding:40px max(24px,calc((100vw - 1120px)/2));background:#173F37;color:#fff} header small{font-weight:800;letter-spacing:1px;color:#8FE0CC} h1{margin:8px 0;font-size:34px} header p{max-width:700px;margin:0;color:#CCE1DA;line-height:1.6} main{max-width:1120px;margin:auto;padding:32px 24px 60px;display:grid;grid-template-columns:repeat(auto-fit,minmax(440px,1fr));gap:24px} article{min-height:420px;display:flex;gap:20px;padding:18px;border:1px solid #D8E0DB;border-radius:20px;background:#fff;box-shadow:0 14px 36px rgba(23,63,55,.08)} .preview{width:180px;height:400px;overflow:hidden;border:1px solid #CBD5CF;border-radius:18px;background:#fff}.preview iframe{width:360px;height:800px;border:0;transform:scale(.5);transform-origin:top left;pointer-events:none}.copy{padding-top:14px}.copy span{display:inline-grid;place-items:center;width:30px;height:30px;border-radius:10px;background:#DCEFE9;color:#176B5C;font-weight:850}.copy h2{margin:16px 0 8px;font-size:21px}.copy p{margin:0 0 20px;color:#68736D;line-height:1.55}.copy a{color:#176B5C;font-weight:800;text-decoration:none}@media(max-width:560px){main{grid-template-columns:1fr;padding:18px}article{gap:12px;padding:12px}.preview{width:144px;height:320px}.preview iframe{transform:scale(.4)}h1{font-size:27px}}
    </style>
  </head>
  <body>
    <header><small>SANGUSANTRI · FIGMA-EXPORT</small><h1>Kalender Hijriah</h1><p>Approved local visual baseline for the compact Gregorian–Hijri calendar. Open a frame to inspect it at the 360 × 800 logical size.</p></header>
    <main>#{cards}</main>
  </body>
  </html>
HTML
File.write(File.join(OUTPUT_DIR, "00-hijri-calendar-state-catalog.html"), catalog_html)

puts "Generated #{STATES.length} Kalender Hijriah states and catalog in #{OUTPUT_DIR}"
