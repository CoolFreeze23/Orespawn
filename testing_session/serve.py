"""In-game testing session checklist server (2026-08-10).

Parses ../TESTING_CHECKLIST.md into checkable items, serves an interactive
page at http://localhost:8765, and persists every good/bad click + note to
results.json next to this script (atomic write). Review tooling reads results.json
to see the session state; the user just clicks.
"""
import json
import os
import re
import html as html_mod
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

HERE = os.path.dirname(os.path.abspath(__file__))
CHECKLIST = os.path.join(HERE, "..", "TESTING_CHECKLIST.md")
RESULTS = os.path.join(HERE, "results.json")
PORT = 8765

SKIP_SECTIONS = {"Failure log", "Session setup (once)"}


def md_inline(s: str) -> str:
    s = html_mod.escape(s)
    s = re.sub(r"`([^`]+)`", r"<code>\1</code>", s)
    s = re.sub(r"\*\*([^*]+)\*\*", r"<strong>\1</strong>", s)
    s = re.sub(r"~~([^~]+)~~", r"<s>\1</s>", s)
    return s


def parse_checklist():
    text = open(CHECKLIST, encoding="utf-8").read()
    sections = []
    cur = None
    cur_sub = None
    counter = 0
    for line in text.split("\n"):
        m = re.match(r"^## (.+)$", line)
        if m:
            title = m.group(1).strip()
            cur = {"title": title, "subs": []} if title not in SKIP_SECTIONS else None
            cur_sub = None
            if cur:
                sections.append(cur)
            continue
        m = re.match(r"^### (.+)$", line)
        if m and cur:
            cur_sub = {"title": m.group(1).strip(), "items": []}
            cur["subs"].append(cur_sub)
            continue
        m = re.match(r"^- (.+)$", line)
        if m and cur is not None:
            if cur_sub is None:
                cur_sub = {"title": "", "items": []}
                cur["subs"].append(cur_sub)
            counter += 1
            body = m.group(1).strip()
            b = re.match(r"^\*\*(.+?)\*\*", body)
            label = b.group(1) if b else body[:60]
            slug = re.sub(r"[^a-z0-9]+", "-", label.lower()).strip("-")[:60]
            cur_sub["items"].append({
                "id": f"i{counter:03d}-{slug}",
                "label": label,
                "html": md_inline(body),
            })
        elif line.strip() and cur is not None and cur_sub is not None \
                and cur_sub["items"] and line.startswith("  "):
            # continuation line of a wrapped bullet
            cur_sub["items"][-1]["html"] += " " + md_inline(line.strip())
    # drop empty subs/sections
    for s in sections:
        s["subs"] = [x for x in s["subs"] if x["items"]]
    return [s for s in sections if s["subs"]]


def load_results():
    try:
        with open(RESULTS, encoding="utf-8") as fh:
            return json.load(fh)
    except (OSError, ValueError):
        return {}


def save_results(data):
    tmp = RESULTS + ".tmp"
    with open(tmp, "w", encoding="utf-8") as fh:
        json.dump(data, fh, indent=1, sort_keys=True)
    os.replace(tmp, RESULTS)


PAGE = """<!doctype html>
<html><head><meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>OreSpawn Test Session</title>
<style>
:root { --bg:#f6f5f2; --card:#fff; --ink:#1a1a1a; --mut:#6b6b6b; --line:#e3e1dc;
  --pass:#1a7f37; --passbg:#e6f4ea; --fail:#c62828; --failbg:#fdecea; --accent:#4a5568; }
@media (prefers-color-scheme: dark) {
  :root { --bg:#15171a; --card:#1e2126; --ink:#e8e6e3; --mut:#9a9a9a; --line:#33373d;
    --pass:#4caf70; --passbg:#173525; --fail:#ef6b5e; --failbg:#3a1f1c; --accent:#a0aec0; } }
* { box-sizing:border-box }
body { margin:0; font:15px/1.5 system-ui,Segoe UI,sans-serif; background:var(--bg); color:var(--ink); }
header { position:sticky; top:0; z-index:5; background:var(--bg); border-bottom:1px solid var(--line);
  padding:10px 18px; display:flex; gap:14px; align-items:baseline; flex-wrap:wrap; }
header h1 { font-size:17px; margin:0 }
#tally { color:var(--mut); font-size:13px }
#tally b.p { color:var(--pass) } #tally b.f { color:var(--fail) }
.filters button { border:1px solid var(--line); background:var(--card); color:var(--ink);
  border-radius:6px; padding:3px 10px; margin-left:4px; cursor:pointer; font-size:13px }
.filters button.on { background:var(--accent); color:#fff; border-color:var(--accent) }
main { max-width:980px; margin:0 auto; padding:14px 18px 80px }
h2 { font-size:15px; margin:26px 0 4px; cursor:pointer; user-select:none }
h2 .n { color:var(--mut); font-weight:normal; font-size:13px }
h3 { font-size:13px; color:var(--mut); margin:14px 0 2px; text-transform:uppercase; letter-spacing:.04em }
.item { display:flex; gap:10px; background:var(--card); border:1px solid var(--line);
  border-radius:8px; padding:8px 10px; margin:6px 0; align-items:flex-start }
.item.pass { border-left:4px solid var(--pass); background:var(--passbg) }
.item.fail { border-left:4px solid var(--fail); background:var(--failbg) }
.btns { display:flex; gap:4px; flex-shrink:0; margin-top:2px }
.btns button { width:34px; height:30px; border-radius:6px; border:1px solid var(--line);
  background:var(--card); cursor:pointer; font-size:15px; line-height:1 }
.item.pass .bp, .item.fail .bf { border-width:2px }
.item.pass .bp { border-color:var(--pass); background:var(--pass); color:#fff }
.item.fail .bf { border-color:var(--fail); background:var(--fail); color:#fff }
.body { flex:1; min-width:0 }
.body code { background:rgba(128,128,128,.15); padding:0 4px; border-radius:4px;
  font:12.5px/1.4 Consolas,monospace; word-break:break-all }
.note { display:none; width:100%; margin-top:6px; border:1px solid var(--line); border-radius:6px;
  background:var(--bg); color:var(--ink); padding:6px 8px; font:13px/1.4 inherit; resize:vertical; min-height:34px }
.note.show, .note.hasText { display:block }
.notebtn { background:none; border:none; color:var(--mut); cursor:pointer; font-size:12px; padding:2px 0 }
#saved { position:fixed; bottom:14px; right:16px; background:var(--card); border:1px solid var(--line);
  border-radius:8px; padding:6px 12px; font-size:12.5px; color:var(--mut); opacity:0; transition:opacity .3s }
#saved.show { opacity:1 }
.collapsed + div { display:none }
</style></head><body>
<header>
  <h1>OreSpawn test session</h1>
  <span id="tally"></span>
  <span class="filters">
    <button data-f="all" class="on">all</button>
    <button data-f="open">open</button>
    <button data-f="pass">good</button>
    <button data-f="fail">bad</button>
  </span>
</header>
<main id="main"></main>
<div id="saved">saved ✓</div>
<script>
const DATA = __DATA__;
let STATE = __STATE__;
const main = document.getElementById('main');
let filter = 'all';

function itemState(id){ return STATE[id] || {}; }

function render(){
  main.innerHTML = '';
  for (const sec of DATA){
    const h2 = document.createElement('h2');
    const wrap = document.createElement('div');
    let secTotal = 0, secDone = 0;
    for (const sub of sec.subs){
      if (sub.title){
        const h3 = document.createElement('h3'); h3.textContent = sub.title; wrap.appendChild(h3);
      }
      for (const it of sub.items){
        secTotal++;
        const st = itemState(it.id);
        if (st.status) secDone++;
        if (filter==='open' && st.status) continue;
        if (filter==='pass' && st.status!=='pass') continue;
        if (filter==='fail' && st.status!=='fail') continue;
        const div = document.createElement('div');
        div.className = 'item' + (st.status ? ' ' + st.status : '');
        div.innerHTML =
          '<span class="btns">' +
          '<button class="bp" title="good">✓</button>' +
          '<button class="bf" title="bad">✗</button></span>' +
          '<span class="body">' + it.html +
          '<br><button class="notebtn">+ note</button>' +
          '<textarea class="note' + (st.notes ? ' hasText' : '') +
          '" placeholder="details…">' + (st.notes ? escapeHtml(st.notes) : '') + '</textarea></span>';
        const ta = div.querySelector('.note');
        div.querySelector('.bp').onclick = () => toggle(it.id, 'pass');
        div.querySelector('.bf').onclick = () => { toggle(it.id, 'fail');
          if (itemState(it.id).status==='fail'){ ta.classList.add('show'); ta.focus(); } };
        div.querySelector('.notebtn').onclick = () => { ta.classList.add('show'); ta.focus(); };
        ta.onchange = () => setNote(it.id, ta.value);
        wrap.appendChild(div);
      }
    }
    h2.innerHTML = sec.title + ' <span class="n">' + secDone + '/' + secTotal + '</span>';
    h2.onclick = () => h2.classList.toggle('collapsed');
    main.appendChild(h2); main.appendChild(wrap);
  }
  tally();
}
function escapeHtml(s){ const d=document.createElement('div'); d.textContent=s; return d.innerHTML; }
function tally(){
  let p=0,f=0,t=0;
  for (const sec of DATA) for (const sub of sec.subs) for (const it of sub.items){
    t++; const s=itemState(it.id).status; if(s==='pass')p++; if(s==='fail')f++;
  }
  document.getElementById('tally').innerHTML =
    '<b class="p">'+p+' good</b> · <b class="f">'+f+' bad</b> · '+(t-p-f)+' open of '+t;
}
function toggle(id, val){
  const st = STATE[id] || (STATE[id]={});
  st.status = (st.status===val) ? null : val;
  st.when = new Date().toISOString();
  push(); render();
}
function setNote(id, txt){
  const st = STATE[id] || (STATE[id]={});
  st.notes = txt; st.when = new Date().toISOString();
  push();
}
let pushTimer=null;
function push(){
  clearTimeout(pushTimer);
  pushTimer = setTimeout(async ()=>{
    try {
      await fetch('/save', {method:'POST', headers:{'Content-Type':'application/json'},
        body: JSON.stringify(STATE)});
      const el=document.getElementById('saved');
      el.classList.add('show'); setTimeout(()=>el.classList.remove('show'), 1200);
    } catch(e){ document.getElementById('saved').textContent='SAVE FAILED'; }
  }, 250);
}
document.querySelectorAll('.filters button').forEach(b=>{
  b.onclick=()=>{ filter=b.dataset.f;
    document.querySelectorAll('.filters button').forEach(x=>x.classList.toggle('on', x===b));
    render(); };
});
render();
</script></body></html>"""


class Handler(BaseHTTPRequestHandler):
    def log_message(self, *a):
        pass

    def do_GET(self):
        if self.path in ("/", "/index.html"):
            page = PAGE.replace("__DATA__", json.dumps(parse_checklist())) \
                       .replace("__STATE__", json.dumps(load_results()))
            body = page.encode("utf-8")
            self.send_response(200)
            self.send_header("Content-Type", "text/html; charset=utf-8")
            self.send_header("Content-Length", str(len(body)))
            self.end_headers()
            self.wfile.write(body)
        elif self.path == "/state":
            body = json.dumps(load_results()).encode("utf-8")
            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.send_header("Content-Length", str(len(body)))
            self.end_headers()
            self.wfile.write(body)
        else:
            self.send_response(404)
            self.end_headers()

    def do_POST(self):
        if self.path == "/save":
            n = int(self.headers.get("Content-Length", 0))
            try:
                incoming = json.loads(self.rfile.read(n).decode("utf-8"))
                assert isinstance(incoming, dict)
            except Exception:
                self.send_response(400)
                self.end_headers()
                return
            merged = load_results()
            merged.update(incoming)
            # drop fully-empty entries
            merged = {k: v for k, v in merged.items()
                      if v and (v.get("status") or v.get("notes"))}
            save_results(merged)
            self.send_response(200)
            self.send_header("Content-Length", "2")
            self.end_headers()
            self.wfile.write(b"ok")
        else:
            self.send_response(404)
            self.end_headers()


if __name__ == "__main__":
    print(f"parsed {sum(len(sub['items']) for s in parse_checklist() for sub in s['subs'])} items")
    print(f"serving on http://localhost:{PORT}")
    ThreadingHTTPServer(("127.0.0.1", PORT), Handler).serve_forever()
