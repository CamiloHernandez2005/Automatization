<template>
  <div class="p-6 text-sm">

    <!-- ── Header ─────────────────────────────────────── -->
    <div class="bg-gradient-to-r from-slate-800 to-slate-700 rounded-2xl px-6 py-5 mb-5 shadow-lg">
      <div class="flex items-center justify-between flex-wrap gap-3">
        <div>
          <h2 class="text-2xl font-bold text-white tracking-tight">Docker Manager</h2>
          <p class="text-slate-300 text-xs mt-1">Manage and monitor your containers</p>
        </div>

        <div class="flex items-center gap-2 flex-wrap">
          <span v-if="selected.length" class="bg-blue-500/20 text-blue-200 text-xs px-3 py-1 rounded-full font-medium border border-blue-400/30">
            {{ selected.length }} selected
          </span>

          <button class="btn-header btn-header-start" :disabled="!selected.length" @click="bulkAction('start')">▶ Start</button>
          <button class="btn-header btn-header-restart" :disabled="!selected.length" @click="bulkAction('restart')">↺ Restart</button>
          <button class="btn-header btn-header-stop" :disabled="!selected.length" @click="bulkAction('stop')">■ Stop</button>
          <button class="btn-header btn-header-kill" :disabled="!selected.length" @click="bulkAction('kill')">✕ Kill</button>

          <div class="w-px h-5 bg-slate-500 mx-1" />

          <button class="btn-header" @click="loadContainers">⟳ Refresh</button>
        </div>
      </div>
    </div>

    <!-- ── Filters ─────────────────────────────────────── -->
    <div class="flex gap-2 mb-4 flex-wrap">
      <div class="relative flex-1 min-w-[200px]">
        <span class="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 text-sm pointer-events-none">&#128269;</span>
        <input
          v-model="filterText"
          placeholder="Search by name, image or status..."
          class="w-full pl-9 pr-3 py-2.5 border border-gray-200 rounded-xl text-sm bg-white shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500/30 focus:border-blue-400 transition-all"
        />
      </div>
      <select v-model="filterStatus" class="px-3 py-2.5 border border-gray-200 rounded-xl text-sm bg-white shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500/30 focus:border-blue-400 transition-all">
        <option value="">All statuses</option>
        <option value="running">Running</option>
        <option value="exited">Exited</option>
        <option value="stopped">Stopped</option>
        <option value="paused">Paused</option>
        <option value="restarting">Restarting</option>
      </select>
      <select v-model="filterCategory" class="px-3 py-2.5 border border-gray-200 rounded-xl text-sm bg-white shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500/30 focus:border-blue-400 transition-all">
        <option value="">All categories</option>
        <option value="dom">DOM</option>
        <option value="mx">MX</option>
        <option value="int">INT</option>
        <option value="etp">ETP</option>
      </select>
    </div>

    <!-- ── Table ───────────────────────────────────────── -->
    <div class="overflow-x-auto border border-gray-200 rounded-2xl shadow-sm bg-white">
      <table class="w-full border-collapse">
        <thead>
          <tr class="bg-gradient-to-r from-gray-50 to-gray-100/80">
            <th class="th-base w-9">
              <input
                type="checkbox"
                class="accent-slate-700 w-4 h-4 cursor-pointer"
                :checked="allSelected"
                :indeterminate.prop="someSelected"
                @change="toggleAll"
              />
            </th>
            <th class="th-base" @click="sortBy('name')">
              Name <span class="sort-icon">{{ sortIcon('name') }}</span>
            </th>
            <th class="th-base" @click="sortBy('status')">
              Status <span class="sort-icon">{{ sortIcon('status') }}</span>
            </th>
            <th class="th-base" @click="sortBy('image')">
              Image <span class="sort-icon">{{ sortIcon('image') }}</span>
            </th>
            <th class="th-base" @click="sortBy('created')">
              Created <span class="sort-icon">{{ sortIcon('created') }}</span>
            </th>
            <th class="th-base" @click="sortBy('ports')">
              Ports <span class="sort-icon">{{ sortIcon('ports') }}</span>
            </th>
          </tr>
        </thead>

        <tbody v-if="!loading && filteredContainers.length">
          <tr
            v-for="(c, idx) in paginatedContainers"
            :key="c.name"
            class="group transition-colors duration-100"
            :class="[
              isSelected(c.name) ? 'bg-blue-50/70' : idx % 2 === 0 ? 'bg-white' : 'bg-gray-50/50',
              'hover:bg-blue-50/50'
            ]"
          >
            <td class="td-base">
              <input
                type="checkbox"
                class="accent-slate-700 w-4 h-4 cursor-pointer"
                :checked="isSelected(c.name)"
                @change="toggleOne(c.name)"
              />
            </td>
            <td class="td-base">
              <button
                class="font-semibold text-slate-700 hover:text-blue-600 cursor-pointer bg-transparent border-none p-0 text-sm text-left transition-colors group-hover:text-blue-600"
                @click="openLogs(c.name)"
              >
                {{ c.name }}
              </button>
            </td>
            <td class="td-base">
              <span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-semibold shadow-sm" :class="badgeClass(c.status)">
                <span class="w-1.5 h-1.5 rounded-full" :class="dotClass(c.status)" />
                {{ stateLabel(c.status) }}
              </span>
            </td>
            <td class="td-base truncate max-w-[220px] text-gray-600 font-mono text-xs" :title="c.image">{{ c.image }}</td>
            <td class="td-base text-gray-500 text-xs">{{ formatCreated(c.created) }}</td>
            <td class="td-base text-gray-500 text-xs font-mono">{{ formatPorts(c.ports) }}</td>
          </tr>
        </tbody>

        <tbody v-else-if="loading">
          <tr>
            <td colspan="6" class="text-center py-16 text-gray-400 text-sm">
              <span class="inline-block animate-spin mr-2">⟳</span> Loading containers...
            </td>
          </tr>
        </tbody>

        <tbody v-else>
          <tr>
            <td colspan="6" class="text-center py-16 text-gray-400 text-sm">No containers found</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- ── Pagination ──────────────────────────────────── -->
    <div v-if="filteredContainers.length" class="flex items-center justify-between mt-4 px-1 flex-wrap gap-3">
      <div class="flex items-center gap-3 text-gray-500 text-xs">
        <span class="bg-gray-100 text-gray-600 px-2.5 py-1 rounded-lg font-medium">{{ filteredContainers.length }} containers</span>
        <div class="flex items-center gap-1.5">
          <label class="text-gray-400">Show</label>
          <select v-model.number="pageSize" class="px-2 py-1 border border-gray-200 rounded-lg text-xs bg-white shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500/30" @change="currentPage = 1">
            <option :value="10">10</option>
            <option :value="25">25</option>
            <option :value="50">50</option>
            <option :value="100">100</option>
          </select>
          <label class="text-gray-400">per page</label>
        </div>
      </div>
      <div class="flex items-center gap-0.5">
        <button class="btn-page" :disabled="currentPage <= 1" @click="currentPage = 1">««</button>
        <button class="btn-page" :disabled="currentPage <= 1" @click="currentPage--">«</button>
        <span class="text-xs text-gray-600 px-3 py-1 bg-slate-100 rounded-lg font-medium mx-1">{{ currentPage }} / {{ totalPages }}</span>
        <button class="btn-page" :disabled="currentPage >= totalPages" @click="currentPage++">»</button>
        <button class="btn-page" :disabled="currentPage >= totalPages" @click="currentPage = totalPages">»»</button>
      </div>
    </div>

    <!-- ── Activity log ───────────────────────────────── -->
    <div class="font-mono text-xs bg-gray-50 border border-gray-200 rounded-lg px-3 py-2.5 mt-3 max-h-28 overflow-y-auto leading-7" ref="activityLog">
      <div
        v-for="(entry, i) in activityEntries"
        :key="i"
        :class="{
          'text-gray-500': entry.type !== 'ok' && entry.type !== 'err' && entry.type !== 'info',
          'text-green-700': entry.type === 'ok',
          'text-red-600': entry.type === 'err',
          'text-blue-700': entry.type === 'info',
        }"
      >
        [{{ entry.time }}] {{ entry.msg }}
      </div>
    </div>


    <!-- ── Modal: Logs ─────────────────────────────────── -->
    <div v-if="logsOpen" class="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4" @click.self="closeLogs">
      <div
        ref="logsModal"
        class="logs-modal bg-white border border-gray-200 flex flex-col shadow-2xl rounded-2xl relative"
        :style="modalStyle"
      >
        <!-- Resize handles -->
        <div class="resize-handle resize-n" @mousedown="startResize($event, 'n')"></div>
        <div class="resize-handle resize-s" @mousedown="startResize($event, 's')"></div>
        <div class="resize-handle resize-e" @mousedown="startResize($event, 'e')"></div>
        <div class="resize-handle resize-w" @mousedown="startResize($event, 'w')"></div>
        <div class="resize-handle resize-ne" @mousedown="startResize($event, 'ne')"></div>
        <div class="resize-handle resize-nw" @mousedown="startResize($event, 'nw')"></div>
        <div class="resize-handle resize-se" @mousedown="startResize($event, 'se')"></div>
        <div class="resize-handle resize-sw" @mousedown="startResize($event, 'sw')"></div>
        <!-- Header (draggable) -->
        <div
          class="flex items-center gap-3 px-5 py-3 border-b border-gray-200 bg-gray-50 rounded-t-2xl flex-wrap shrink-0 cursor-move select-none"
          @mousedown="startDrag"
        >
          <span class="live-dot" :class="{ off: !logsLive }" />
          <h3 class="text-sm font-semibold flex-1 m-0">{{ logsOpen }}</h3>
          <label class="text-gray-500 text-xs">tail</label>
          <input
            type="number"
            v-model.number="logsTail"
            min="1"
            max="5000"
            class="w-18 px-2 py-1 border border-gray-300 rounded-md text-xs"
            @change="fetchLogs"
          />
          <button class="btn-sm-tw" @click="logsLive = !logsLive; logsLive ? startPoll() : stopPoll()">
            {{ logsLive ? '⏸ Pause' : '▶ Resume' }}
          </button>
          <button class="btn-sm-tw" @click="scrollBottom">↓ Bottom</button>
          <button class="btn-sm-tw" @click="closeLogs">✕ Close</button>
        </div>
        <!-- Body -->
        <div class="flex-1 overflow-y-auto font-mono text-xs leading-relaxed p-4 bg-gray-900 text-gray-100 whitespace-pre-wrap break-all" ref="logBody">
          <div v-if="!logsLines.length" class="text-gray-500">No logs yet...</div>
          <template v-for="(line, i) in logsLines" :key="i">
            <!-- New lines separator -->
            <div v-if="i === newLineStart" class="flex items-center gap-2 my-2 select-none">
              <div class="flex-1 border-t border-emerald-500/50"></div>
              <span class="text-[10px] text-emerald-400 font-semibold uppercase tracking-wider px-2">new</span>
              <div class="flex-1 border-t border-emerald-500/50"></div>
            </div>
            <div
              :class="[
                logLineClass(line),
                i >= newLineStart && newLineStart !== -1 ? 'log-new' : '',
              ]"
            >
              <template v-if="splitLine(line)">
                <span class="text-gray-500">{{ splitLine(line).ts }}&nbsp;&nbsp;</span>
                <span>{{ splitLine(line).msg }}</span>
              </template>
              <template v-else>{{ line }}</template>
            </div>
          </template>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from "vue";
import {
  getContainers,
  getContainerLogs,
  runContainer,
  startContainers,
  stopContainers,
  restartContainers,
  killContainers,
} from "@/services/containersService";

// ── Main state ────────────────────────────────────────
const containers   = ref([]);
const loading      = ref(false);
const selected     = ref([]);
const filterText   = ref("");
const filterStatus = ref("");
const filterCategory = ref("");
const sortKey      = ref(null);
const sortAsc      = ref(true);
const currentPage  = ref(1);
const pageSize     = ref(10);
const showRunModal = ref(false);
const activityEntries = ref([]);
const activityLog  = ref(null);

const runForm = ref({ image: "", name: "", ports: "", env: "", cmd: "" });

// ── Logs state ────────────────────────────────────────
const logsOpen  = ref(null);
const logsLines = ref([]);
const logsTail  = ref(100);
const logsLive  = ref(true);
const logBody   = ref(null);
const logsModal = ref(null);
const newLineStart = ref(-1);
let   prevLineCount = 0;

// ── Modal resize state ───────────────────────────────
const modalW = ref(0);
const modalH = ref(0);
const modalX = ref(0);
const modalY = ref(0);
const modalStyle = computed(() => {
  if (!modalW.value) return {};
  return {
    width: modalW.value + 'px',
    height: modalH.value + 'px',
    position: 'absolute',
    left: modalX.value + 'px',
    top: modalY.value + 'px',
  };
});

let resizeDir = null;
let resizeStartX = 0;
let resizeStartY = 0;
let resizeStartW = 0;
let resizeStartH = 0;
let resizeStartLeft = 0;
let resizeStartTop = 0;
const MIN_W = 400;
const MIN_H = 300;

function initModalRect() {
  if (!logsModal.value) return;
  const r = logsModal.value.getBoundingClientRect();
  const parent = logsModal.value.parentElement.getBoundingClientRect();
  modalW.value = r.width;
  modalH.value = r.height;
  modalX.value = r.left - parent.left;
  modalY.value = r.top - parent.top;
}

function startResize(e, dir) {
  e.preventDefault();
  if (!modalW.value) initModalRect();
  resizeDir = dir;
  resizeStartX = e.clientX;
  resizeStartY = e.clientY;
  resizeStartW = modalW.value;
  resizeStartH = modalH.value;
  resizeStartLeft = modalX.value;
  resizeStartTop = modalY.value;
  document.addEventListener('mousemove', onResize);
  document.addEventListener('mouseup', stopResize);
}

function onResize(e) {
  const dx = e.clientX - resizeStartX;
  const dy = e.clientY - resizeStartY;

  let w = resizeStartW;
  let h = resizeStartH;
  let x = resizeStartLeft;
  let y = resizeStartTop;

  if (resizeDir.includes('e')) w = Math.max(MIN_W, resizeStartW + dx);
  if (resizeDir.includes('w')) { w = Math.max(MIN_W, resizeStartW - dx); x = resizeStartLeft + resizeStartW - w; }
  if (resizeDir.includes('s')) h = Math.max(MIN_H, resizeStartH + dy);
  if (resizeDir.includes('n')) { h = Math.max(MIN_H, resizeStartH - dy); y = resizeStartTop + resizeStartH - h; }

  modalW.value = w;
  modalH.value = h;
  modalX.value = x;
  modalY.value = y;
}

function stopResize() {
  resizeDir = null;
  document.removeEventListener('mousemove', onResize);
  document.removeEventListener('mouseup', stopResize);
}

// ── Modal drag ───────────────────────────────────────
let dragStartX = 0;
let dragStartY = 0;
let dragStartLeft = 0;
let dragStartTop = 0;

function startDrag(e) {
  if (e.target.closest('input, button, label')) return;
  e.preventDefault();
  if (!modalW.value) initModalRect();
  dragStartX = e.clientX;
  dragStartY = e.clientY;
  dragStartLeft = modalX.value;
  dragStartTop = modalY.value;
  document.addEventListener('mousemove', onDrag);
  document.addEventListener('mouseup', stopDrag);
}

function onDrag(e) {
  modalX.value = dragStartLeft + (e.clientX - dragStartX);
  modalY.value = dragStartTop + (e.clientY - dragStartY);
}

function stopDrag() {
  document.removeEventListener('mousemove', onDrag);
  document.removeEventListener('mouseup', stopDrag);
}
let   pollTimer = null;
let   refreshTimer = null;
const now = ref(Date.now());

// ── Computed ───────────────────────────────────────────
const filteredContainers = computed(() => {
  let list = containers.value;

  if (filterText.value) {
    const q = filterText.value.toLowerCase();
    list = list.filter(
      (c) =>
        c.name.toLowerCase().includes(q) ||
        c.image.toLowerCase().includes(q) ||
        c.status.toLowerCase().includes(q)
    );
  }

  if (filterStatus.value) {
    list = list.filter((c) =>
      stateFromStatus(c.status) === filterStatus.value
    );
  }

  if (filterCategory.value) {
    const cat = filterCategory.value.toLowerCase();
    list = list.filter((c) =>
      c.name.toLowerCase().includes(cat)
    );
  }

  if (sortKey.value) {
    list = [...list].sort((a, b) => {
      const v = a[sortKey.value] < b[sortKey.value] ? -1 : a[sortKey.value] > b[sortKey.value] ? 1 : 0;
      return sortAsc.value ? v : -v;
    });
  }

  return list;
});

const totalPages = computed(() => Math.max(1, Math.ceil(filteredContainers.value.length / pageSize.value)));

const paginatedContainers = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value;
  return filteredContainers.value.slice(start, start + pageSize.value);
});

const allSelected  = computed(() => selected.value.length === paginatedContainers.value.length && paginatedContainers.value.length > 0);
const someSelected = computed(() => selected.value.length > 0 && !allSelected.value);

watch([filterText, filterStatus, filterCategory], () => { currentPage.value = 1; });

// ── Helpers ────────────────────────────────────────────
function stateFromStatus(s = "") {
  s = s.toLowerCase();
  if (s.startsWith("up"))          return "running";
  if (s.includes("exited"))        return "exited";
  if (s.includes("restarting"))    return "restarting";
  if (s.includes("paused"))        return "paused";
  return "stopped";
}

function capitalize(str) {
  return str.charAt(0).toUpperCase() + str.slice(1);
}

function stateLabel(s) {
  const state = stateFromStatus(s);
  if (state === "running") {
    const uptime = formatUptime(s);
    return uptime ? `Running (${uptime})` : "Running";
  }
  return capitalize(state);
}

function formatUptime(s = "") {
  const m = s.match(/^Up\s+(.+)/i);
  return m ? m[1].trim() : "";
}

function badgeClass(s) {
  const state = stateFromStatus(s);
  return {
    running:    "bg-emerald-100 text-emerald-800 border border-emerald-200",
    exited:     "bg-red-50 text-red-700 border border-red-200",
    stopped:    "bg-gray-100 text-gray-600 border border-gray-200",
    restarting: "bg-yellow-50 text-yellow-700 border border-yellow-200",
    paused:     "bg-amber-50 text-amber-700 border border-amber-200",
  }[state] || "bg-gray-100 text-gray-600 border border-gray-200";
}

function dotClass(s) {
  const state = stateFromStatus(s);
  return {
    running:    "bg-emerald-500",
    exited:     "bg-red-500",
    stopped:    "bg-gray-400",
    restarting: "bg-yellow-500 animate-pulse",
    paused:     "bg-amber-500",
  }[state] || "bg-gray-400";
}

function formatCreated(s) {
  if (!s) return "—";
  const d = new Date(s);
  if (isNaN(d)) return s;
  const diff = Math.floor((now.value - d) / 1000);
  if (diff < 60)    return diff + "s ago";
  if (diff < 3600)  return Math.floor(diff / 60) + "m ago";
  if (diff < 86400) return Math.floor(diff / 3600) + "h ago";
  return Math.floor(diff / 86400) + "d ago";
}

function formatPorts(ports) {
  if (!ports) return '—';
  const matches = [...ports.matchAll(/(?:\d+\.\d+\.\d+\.\d+|::):(\d+)->(\d+)/g)];
  if (!matches.length) return '—';
  const seen = new Set();
  const result = [];
  for (const m of matches) {
    const pair = `${m[1]}:${m[2]}`;
    if (seen.has(pair)) continue;
    seen.add(pair);
    result.push(pair);
  }
  return result.join(', ');
}

function splitLine(line) {
  const m = line.match(/^(\d{4}-\d{2}-\d{2}T[\d:.]+(?:[+-][\d:]+|Z)?)\s+(.*)$/);
  if (!m) return null;
  const raw = m[1];
  let msg = m[2];
  // Strip duplicate app-level timestamp (e.g. "2026-04-20 09:20:44.120")
  msg = msg.replace(/^\d{4}-\d{2}-\d{2}\s+\d{2}:\d{2}:\d{2}[.\d]*\s*/, '');
  // Format Docker timestamp to readable HH:mm:ss
  const d = new Date(raw);
  const ts = isNaN(d) ? raw : d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' });
  return { ts, msg };
}

function logLineClass(line) {
  if (/error|fatal|exception/i.test(line)) return "text-red-400";
  if (/warn/i.test(line))                  return "text-yellow-400";
  return "";
}

// ── Activity ──────────────────────────────────────────
function log(msg, type = "info") {
  activityEntries.value.push({
    time: new Date().toLocaleTimeString(),
    msg,
    type,
  });
  nextTick(() => {
    if (activityLog.value)
      activityLog.value.scrollTop = activityLog.value.scrollHeight;
  });
}

// ── Selection ─────────────────────────────────────────
const isSelected = (name) => selected.value.includes(name);

function toggleOne(name) {
  const i = selected.value.indexOf(name);
  i === -1 ? selected.value.push(name) : selected.value.splice(i, 1);
}

function toggleAll() {
  selected.value = allSelected.value
    ? []
    : paginatedContainers.value.map((c) => c.name);
}

// ── Sorting ───────────────────────────────────────────
function sortBy(key) {
  if (sortKey.value === key) sortAsc.value = !sortAsc.value;
  else { sortKey.value = key; sortAsc.value = true; }
}

function sortIcon(key) {
  if (sortKey.value !== key) return "↕";
  return sortAsc.value ? "↑" : "↓";
}

// ── API calls ──────────────────────────────────────────
async function loadContainers() {
  loading.value = true;
  log("Loading containers...", "info");
  try {
    const data = await getContainers();
    containers.value = data.data;
    selected.value = [];
    log(`${data.count} containers loaded`, "ok");
  } catch (e) {
    log("Error loading containers: " + e.message, "err");
  } finally {
    loading.value = false;
  }
}

async function bulkAction(action) {
  if (!selected.value.length) return;
  const names = [...selected.value];
  log(`docker ${action} → [${names.join(", ")}]`, "info");

  const fn = { start: startContainers, stop: stopContainers, restart: restartContainers, kill: killContainers }[action];
  try {
    const data = await fn(names);
    if (data.success) {
      log(`${action} OK on ${names.length} container(s)`, "ok");
    } else {
      const failed = data.results.filter((r) => !r.success).map((r) => `${r.name}: ${r.error}`).join("; ");
      log("Errors: " + failed, "err");
    }
  } catch (e) {
    log("Error: " + e.message, "err");
  } finally {
    selected.value = [];
    await loadContainers();
  }
}

async function handleRun() {
  if (!runForm.value.image) return alert("Image is required");
  log(`docker run ${runForm.value.name} (${runForm.value.image})...`, "info");
  try {
    const data = await runContainer(runForm.value);
    if (data.success) {
      log(`Container started — ID: ${data.container_id}`, "ok");
      showRunModal.value = false;
      runForm.value = { image: "", name: "", ports: "", env: "", cmd: "" };
      await loadContainers();
    } else {
      log("Error: " + data.error, "err");
    }
  } catch (e) {
    log("Error: " + e.message, "err");
  }
}

// ── Logs ───────────────────────────────────────────────
function openLogs(name) {
  logsOpen.value = name;
  logsLines.value = [];
  newLineStart.value = -1;
  prevLineCount = 0;
  modalW.value = 0;
  modalH.value = 0;
  logsLive.value = true;
  fetchLogs();
  startPoll();
}

function closeLogs() {
  logsOpen.value = null;
  logsLines.value = [];
  modalW.value = 0;
  modalH.value = 0;
  stopPoll();
}

async function fetchLogs() {
  if (!logsOpen.value) return;
  try {
    const data = await getContainerLogs(logsOpen.value, logsTail.value);
    if (data.success) {
      const body = logBody.value;
      const atBottom = !body || body.scrollHeight - body.scrollTop - body.clientHeight < 40;
      const newCount = data.lines.length;
      if (prevLineCount > 0 && newCount > prevLineCount) {
        newLineStart.value = prevLineCount;
        setTimeout(() => { newLineStart.value = -1; }, 2000);
      }
      prevLineCount = newCount;
      logsLines.value = data.lines;
      if (atBottom) nextTick(scrollBottom);
    }
  } catch (_) {}
}

function startPoll() {
  stopPoll();
  pollTimer = setInterval(fetchLogs, 2000);
}

function stopPoll() {
  if (pollTimer) { clearInterval(pollTimer); pollTimer = null; }
}

function scrollBottom() {
  nextTick(() => {
    if (logBody.value) logBody.value.scrollTop = logBody.value.scrollHeight;
  });
}

// ── Lifecycle ──────────────────────────────────────────
let tickTimer = null;

onMounted(() => {
  loadContainers();
  refreshTimer = setInterval(loadContainers, 60000);
  tickTimer = setInterval(() => { now.value = Date.now(); }, 60000);
});

onUnmounted(() => {
  stopPoll();
  if (refreshTimer) clearInterval(refreshTimer);
  if (tickTimer) clearInterval(tickTimer);
});
</script>

<style scoped>
@reference "tailwindcss";

/* Table */
.th-base {
  @apply text-left text-[11px] font-semibold text-gray-400 uppercase tracking-wider px-4 py-3 border-b border-gray-200 cursor-pointer select-none whitespace-nowrap transition-colors;
}
.th-base:hover { @apply text-gray-700; }
.sort-icon { @apply ml-1 text-[10px] opacity-40; }

.td-base {
  @apply px-4 py-3 border-b border-gray-100/80 align-middle whitespace-nowrap overflow-hidden text-ellipsis text-sm;
}

/* Header buttons */
.btn-header {
  @apply px-3 py-1.5 rounded-lg border border-white/20 bg-white/10 text-slate-200 cursor-pointer text-xs font-medium transition-all duration-150 backdrop-blur-sm hover:bg-white/20 disabled:opacity-30 disabled:cursor-not-allowed disabled:pointer-events-none;
}
.btn-header-start   { @apply border-green-400/30 text-green-300 hover:bg-green-500/20; }
.btn-header-restart { @apply border-blue-400/30 text-blue-300 hover:bg-blue-500/20; }
.btn-header-stop    { @apply border-amber-400/30 text-amber-300 hover:bg-amber-500/20; }
.btn-header-kill    { @apply border-red-400/30 text-red-300 hover:bg-red-500/20; }

/* General buttons */
.btn {
  @apply px-3 py-1.5 rounded-lg border border-gray-300 bg-white text-gray-700 cursor-pointer text-[13px] transition-colors duration-150 hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed disabled:pointer-events-none;
}
.btn-run     { @apply bg-slate-800 text-white border-slate-800 hover:bg-slate-700; }
.btn-sm-tw   { @apply px-2.5 py-1 rounded-lg border border-gray-300 bg-white text-gray-600 cursor-pointer text-xs font-medium hover:bg-gray-100 transition-colors; }

/* Pagination */
.btn-page {
  @apply px-2.5 py-1 rounded-lg border border-gray-200 bg-white text-gray-500 cursor-pointer text-xs font-medium hover:bg-gray-100 hover:text-gray-700 transition-all shadow-sm disabled:opacity-30 disabled:cursor-not-allowed disabled:pointer-events-none;
}

/* Live dot */
.live-dot {
  @apply w-2 h-2 rounded-full bg-green-500 shrink-0;
  animation: pulse 1.2s infinite;
}
.live-dot.off { @apply bg-gray-400; animation: none; }
@keyframes pulse { 0%, 100% { opacity: 1 } 50% { opacity: 0.3 } }

/* Resizable logs modal */
.logs-modal {
  width: 80vw;
  height: 70vh;
  min-width: 400px;
  min-height: 300px;
  overflow: hidden;
}

/* Resize handles */
.resize-handle { position: absolute; z-index: 10; }
.resize-n  { top: 0; left: 8px; right: 8px; height: 6px; cursor: n-resize; }
.resize-s  { bottom: 0; left: 8px; right: 8px; height: 6px; cursor: s-resize; }
.resize-e  { right: 0; top: 8px; bottom: 8px; width: 6px; cursor: e-resize; }
.resize-w  { left: 0; top: 8px; bottom: 8px; width: 6px; cursor: w-resize; }
.resize-ne { top: 0; right: 0; width: 12px; height: 12px; cursor: ne-resize; }
.resize-nw { top: 0; left: 0; width: 12px; height: 12px; cursor: nw-resize; }
.resize-se { bottom: 0; right: 0; width: 12px; height: 12px; cursor: se-resize; }
.resize-sw { bottom: 0; left: 0; width: 12px; height: 12px; cursor: sw-resize; }

/* New log lines highlight */
.log-new {
  animation: log-flash 2s ease-out forwards;
  border-left: 2px solid rgb(16 185 129 / 0.6);
  padding-left: 8px;
}
@keyframes log-flash {
  0%   { background: rgb(16 185 129 / 0.15); }
  100% { background: transparent; border-left-color: transparent; }
}
</style>
