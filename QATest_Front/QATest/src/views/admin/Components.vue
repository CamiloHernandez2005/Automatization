<template>
  <div class="containers-view">

    <!-- ── Header ─────────────────────────────────────── -->
    <div class="header">
      <h2>Docker Manager</h2>

      <div class="toolbar">
        <span v-if="selected.length" class="sel-badge">
          {{ selected.length }} seleccionado{{ selected.length !== 1 ? "s" : "" }}
        </span>

        <button
          class="btn btn-start"
          :disabled="!selected.length"
          @click="bulkAction('start')"
        >▶ Start</button>

        <button
          class="btn btn-restart"
          :disabled="!selected.length"
          @click="bulkAction('restart')"
        >↺ Restart</button>

        <button
          class="btn btn-stop"
          :disabled="!selected.length"
          @click="bulkAction('stop')"
        >■ Stop</button>

        <button
          class="btn btn-kill"
          :disabled="!selected.length"
          @click="bulkAction('kill')"
        >✕ Kill</button>

        <div class="divider" />

        <button class="btn" @click="loadContainers">⟳ Refresh</button>
      </div>
    </div>

    <!-- ── Filtro ──────────────────────────────────────── -->
    <div class="filters">
      <input
        v-model="filterText"
        placeholder="Buscar por nombre, imagen o estado..."
        class="filter-input"
      />
      <select v-model="filterStatus" class="filter-select">
        <option value="">Todos los estados</option>
        <option value="running">Running</option>
        <option value="exited">Exited</option>
        <option value="stopped">Stopped</option>
        <option value="paused">Paused</option>
        <option value="restarting">Restarting</option>
      </select>
    </div>

    <!-- ── Tabla ───────────────────────────────────────── -->
    <div class="table-wrap">
      <table>
        <thead>
          <tr>
            <th style="width: 36px">
              <input
                type="checkbox"
                :checked="allSelected"
                :indeterminate.prop="someSelected"
                @change="toggleAll"
              />
            </th>
            <th @click="sortBy('name')">
              Nombre <span class="sort-icon">{{ sortIcon('name') }}</span>
            </th>
            <th @click="sortBy('status')">
              Estado <span class="sort-icon">{{ sortIcon('status') }}</span>
            </th>
            <th @click="sortBy('image')">
              Imagen <span class="sort-icon">{{ sortIcon('image') }}</span>
            </th>
            <th>Creado</th>
            <th>Puertos</th>
            <th style="width: 80px"></th>
          </tr>
        </thead>

        <tbody v-if="!loading && filteredContainers.length">
          <template v-for="c in filteredContainers" :key="c.name">
            <tr
              :class="{ selected: isSelected(c.name) }"
              @click="toggleRow($event, c.name)"
            >
              <td>
                <input
                  type="checkbox"
                  :checked="isSelected(c.name)"
                  @change="toggleOne(c.name)"
                  @click.stop
                />
              </td>
              <td>
                <span class="cont-name">{{ c.name }}</span>
              </td>
              <td>
                <span class="badge" :class="badgeClass(c.status)">
                  {{ stateLabel(c.status) }}
                </span>
              </td>
              <td class="ellipsis" :title="c.image">{{ c.image }}</td>
              <td class="muted">{{ formatCreated(c.created) }}</td>
              <td class="muted small" :title="c.ports">{{ c.ports || '—' }}</td>
              <td>
                <button
                  class="btn btn-sm"
                  @click.stop="toggleLogs(c.name)"
                >
                  {{ logsOpen === c.name ? '▲' : '▼' }} logs
                </button>
              </td>
            </tr>

            <!-- Panel de logs inline -->
            <tr v-if="logsOpen === c.name" :key="c.name + '-logs'">
              <td colspan="7" class="logs-cell">
                <div class="log-panel">
                  <div class="log-header">
                    <span class="live-dot" :class="{ off: !logsLive }" />
                    <strong>{{ c.name }}</strong>
                    <label class="muted">tail</label>
                    <input
                      type="number"
                      v-model.number="logsTail"
                      min="1"
                      max="2000"
                      class="tail-input"
                      @change="fetchLogs"
                      @click.stop
                    />
                    <button class="btn btn-sm" @click="logsLive = !logsLive; logsLive ? startPoll() : stopPoll()">
                      {{ logsLive ? '⏸ Pausar' : '▶ Reanudar' }}
                    </button>
                    <button class="btn btn-sm" @click="scrollBottom">↓ Final</button>
                    <button class="btn btn-sm" @click="toggleLogs(null)">✕</button>
                  </div>
                  <div class="log-body" ref="logBody">
                    <div v-if="!logsLines.length" class="muted">Sin logs aún...</div>
                    <div
                      v-for="(line, i) in logsLines"
                      :key="i"
                      :class="lineClass(line)"
                    >
                      <template v-if="splitLine(line)">
                        <span class="ts">{{ splitLine(line).ts }}&nbsp;&nbsp;</span>
                        <span>{{ splitLine(line).msg }}</span>
                      </template>
                      <template v-else>{{ line }}</template>
                    </div>
                  </div>
                </div>
              </td>
            </tr>
          </template>
        </tbody>

        <tbody v-else-if="loading">
          <tr>
            <td colspan="7" class="empty">Cargando contenedores...</td>
          </tr>
        </tbody>

        <tbody v-else>
          <tr>
            <td colspan="7" class="empty">No hay contenedores</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- ── Activity log ───────────────────────────────── -->
    <div class="activity-log" ref="activityLog">
      <div
        v-for="(entry, i) in activityEntries"
        :key="i"
        :class="['log-entry', entry.type]"
      >
        [{{ entry.time }}] {{ entry.msg }}
      </div>
    </div>

    <!-- ── Modal: Run container ───────────────────────── -->
    <div v-if="showRunModal" class="modal-bg" @click.self="showRunModal = false">
      <div class="modal">
        <h3>Ejecutar nuevo contenedor</h3>
        <div class="field">
          <label>Imagen *</label>
          <input v-model="runForm.image" placeholder="nginx:latest" />
        </div>
        <div class="field">
          <label>Nombre</label>
          <input v-model="runForm.name" placeholder="mi-contenedor" />
        </div>
        <div class="field">
          <label>Puertos (host:container)</label>
          <input v-model="runForm.ports" placeholder="8080:80, 443:443" />
        </div>
        <div class="field">
          <label>Variables de entorno</label>
          <input v-model="runForm.env" placeholder="KEY=value, OTRA=val" />
        </div>
        <div class="field">
          <label>Comando</label>
          <input v-model="runForm.cmd" placeholder="/bin/bash" />
        </div>
        <div class="modal-footer">
          <button class="btn" @click="showRunModal = false">Cancelar</button>
          <button class="btn btn-run" @click="handleRun">Ejecutar</button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from "vue";
import {
  getContainers,
  getContainerLogs,
  runContainer,
  startContainers,
  stopContainers,
  restartContainers,
  killContainers,
} from "@/services/containersService";

// ── Estado principal ───────────────────────────────────
const containers   = ref([]);
const loading      = ref(false);
const selected     = ref([]);
const filterText   = ref("");
const filterStatus = ref("");
const sortKey      = ref(null);
const sortAsc      = ref(true);
const showRunModal = ref(false);
const activityEntries = ref([]);
const activityLog  = ref(null);

const runForm = ref({ image: "", name: "", ports: "", env: "", cmd: "" });

// ── Estado logs ────────────────────────────────────────
const logsOpen  = ref(null);
const logsLines = ref([]);
const logsTail  = ref(100);
const logsLive  = ref(true);
const logBody   = ref(null);
let   pollTimer = null;

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

  if (sortKey.value) {
    list = [...list].sort((a, b) => {
      const v = a[sortKey.value] < b[sortKey.value] ? -1 : a[sortKey.value] > b[sortKey.value] ? 1 : 0;
      return sortAsc.value ? v : -v;
    });
  }

  return list;
});

const allSelected  = computed(() => selected.value.length === filteredContainers.value.length && filteredContainers.value.length > 0);
const someSelected = computed(() => selected.value.length > 0 && !allSelected.value);

// ── Helpers ────────────────────────────────────────────
function stateFromStatus(s = "") {
  s = s.toLowerCase();
  if (s.startsWith("up"))          return "running";
  if (s.includes("exited"))        return "exited";
  if (s.includes("restarting"))    return "restarting";
  if (s.includes("paused"))        return "paused";
  return "stopped";
}

function stateLabel(s) { return stateFromStatus(s); }

function badgeClass(s) { return `b-${stateFromStatus(s)}`; }

function formatCreated(s) {
  if (!s) return "—";
  const d = new Date(s);
  if (isNaN(d)) return s;
  const diff = Math.floor((Date.now() - d) / 1000);
  if (diff < 60)    return diff + "s ago";
  if (diff < 3600)  return Math.floor(diff / 60) + "m ago";
  if (diff < 86400) return Math.floor(diff / 3600) + "h ago";
  return Math.floor(diff / 86400) + "d ago";
}

function splitLine(line) {
  const m = line.match(/^(\S+)\s(.*)$/);
  return m ? { ts: m[1], msg: m[2] } : null;
}

function lineClass(line) {
  if (/error|fatal|exception/i.test(line)) return "line-err";
  if (/warn/i.test(line))                  return "line-warn";
  return "";
}

// ── Actividad ──────────────────────────────────────────
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

// ── Selección ──────────────────────────────────────────
const isSelected = (name) => selected.value.includes(name);

function toggleOne(name) {
  const i = selected.value.indexOf(name);
  i === -1 ? selected.value.push(name) : selected.value.splice(i, 1);
}

function toggleRow(e, name) {
  if (e.target.tagName === "INPUT" || e.target.tagName === "BUTTON") return;
  toggleOne(name);
}

function toggleAll() {
  selected.value = allSelected.value
    ? []
    : filteredContainers.value.map((c) => c.name);
}

// ── Ordenamiento ───────────────────────────────────────
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
  log("Cargando contenedores...", "info");
  try {
    const data = await getContainers();
    containers.value = data.data;
    selected.value = [];
    log(`${data.count} contenedores cargados`, "ok");
  } catch (e) {
    log("Error al cargar contenedores: " + e.message, "err");
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
      log(`${action} OK en ${names.length} contenedor(es)`, "ok");
    } else {
      const failed = data.results.filter((r) => !r.success).map((r) => `${r.name}: ${r.error}`).join("; ");
      log("Errores: " + failed, "err");
    }
  } catch (e) {
    log("Error: " + e.message, "err");
  } finally {
    selected.value = [];
    await loadContainers();
  }
}

async function handleRun() {
  if (!runForm.value.image) return alert("La imagen es requerida");
  log(`docker run ${runForm.value.name} (${runForm.value.image})...`, "info");
  try {
    const data = await runContainer(runForm.value);
    if (data.success) {
      log(`Contenedor iniciado — ID: ${data.container_id}`, "ok");
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
function toggleLogs(name) {
  if (logsOpen.value === name) {
    logsOpen.value = null;
    logsLines.value = [];
    stopPoll();
  } else {
    logsOpen.value = name;
    logsLive.value = true;
    fetchLogs();
    startPoll();
  }
}

async function fetchLogs() {
  if (!logsOpen.value) return;
  try {
    const data = await getContainerLogs(logsOpen.value, logsTail.value);
    if (data.success) {
      const body = logBody.value;
      const atBottom = !body || body.scrollHeight - body.scrollTop - body.clientHeight < 40;
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
onMounted(loadContainers);
onUnmounted(stopPoll);
</script>

<style scoped>
.containers-view { padding: 1.5rem; font-size: 14px; }

/* Header */
.header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 1rem; flex-wrap: wrap; gap: 8px; }
.header h2 { font-size: 20px; font-weight: 600; }
.toolbar { display: flex; align-items: center; gap: 6px; flex-wrap: wrap; }
.divider { width: 1px; height: 20px; background: #e5e7eb; margin: 0 2px; }
.sel-badge { background: #dbeafe; color: #1e40af; font-size: 11px; padding: 2px 8px; border-radius: 9999px; font-weight: 500; }

/* Botones */
.btn { padding: 6px 13px; border-radius: 6px; border: 1px solid #d1d5db; background: #fff; color: #374151; cursor: pointer; font-size: 13px; transition: background 0.15s; }
.btn:hover { background: #f9fafb; }
.btn:disabled { opacity: 0.38; cursor: not-allowed; pointer-events: none; }
.btn-sm { padding: 3px 8px; font-size: 11px; }
.btn-run     { background: #1e3a5f; color: #fff; border-color: #1e3a5f; }
.btn-run:hover { background: #1e4d8c; }
.btn-stop    { border-color: #b45309; color: #92400e; }
.btn-stop:hover { background: #fef3c7; }
.btn-restart { border-color: #1d4ed8; color: #1e3a8a; }
.btn-restart:hover { background: #dbeafe; }
.btn-kill    { border-color: #dc2626; color: #991b1b; }
.btn-kill:hover { background: #fee2e2; }
.btn-start   { border-color: #15803d; color: #14532d; }
.btn-start:hover { background: #dcfce7; }

/* Filtros */
.filters { display: flex; gap: 8px; margin-bottom: 10px; flex-wrap: wrap; }
.filter-input { flex: 1; min-width: 200px; padding: 7px 10px; border: 1px solid #d1d5db; border-radius: 6px; font-size: 13px; }
.filter-input:focus { outline: none; border-color: #3b82f6; }
.filter-select { padding: 7px 10px; border: 1px solid #d1d5db; border-radius: 6px; font-size: 13px; background: #fff; }

/* Tabla */
.table-wrap { overflow-x: auto; border: 1px solid #e5e7eb; border-radius: 10px; }
table { width: 100%; border-collapse: collapse; table-layout: fixed; }
th { text-align: left; font-size: 12px; font-weight: 500; color: #6b7280; padding: 9px 12px; background: #f9fafb; border-bottom: 1px solid #e5e7eb; cursor: pointer; user-select: none; white-space: nowrap; }
th:hover { color: #111827; }
.sort-icon { margin-left: 4px; font-size: 10px; opacity: 0.5; }
td { padding: 10px 12px; border-bottom: 1px solid #f3f4f6; vertical-align: middle; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
tr:last-child td { border-bottom: none; }
tr:hover td { background: #f9fafb; }
tr.selected td { background: #f0fdf4; }
.ellipsis { overflow: hidden; text-overflow: ellipsis; max-width: 180px; }
.cont-name { font-weight: 500; }
.muted { color: #6b7280; font-size: 12px; }
.small { font-size: 11px; }
.empty { text-align: center; padding: 2.5rem; color: #9ca3af; font-size: 13px; }

/* Badges */
.badge { display: inline-block; padding: 2px 8px; border-radius: 9999px; font-size: 11px; font-weight: 500; }
.b-running    { background: #dcfce7; color: #14532d; }
.b-exited     { background: #fee2e2; color: #7f1d1d; }
.b-stopped    { background: #f3f4f6; color: #374151; }
.b-restarting { background: #fef9c3; color: #713f12; }
.b-paused     { background: #fef3c7; color: #92400e; }

/* Log panel */
.logs-cell { padding: 0 !important; }
.log-panel { border-top: 1px solid #e5e7eb; }
.log-header { display: flex; align-items: center; gap: 8px; padding: 7px 12px; background: #f9fafb; border-bottom: 1px solid #e5e7eb; flex-wrap: wrap; }
.log-header strong { font-size: 13px; flex: 1; }
.live-dot { width: 7px; height: 7px; border-radius: 50%; background: #16a34a; animation: pulse 1.2s infinite; flex-shrink: 0; }
.live-dot.off { background: #9ca3af; animation: none; }
@keyframes pulse { 0%, 100% { opacity: 1 } 50% { opacity: 0.3 } }
.tail-input { width: 68px; padding: 3px 7px; border: 1px solid #d1d5db; border-radius: 6px; font-size: 12px; }
.log-body { font-family: monospace; font-size: 12px; line-height: 1.6; padding: 10px 14px; max-height: 280px; overflow-y: auto; white-space: pre-wrap; word-break: break-all; background: #fff; }
.ts { color: #9ca3af; }
.line-err { color: #991b1b; }
.line-warn { color: #92400e; }

/* Activity log */
.activity-log { font-family: monospace; font-size: 12px; background: #f9fafb; border: 1px solid #e5e7eb; border-radius: 8px; padding: 10px 12px; margin-top: 12px; max-height: 110px; overflow-y: auto; line-height: 1.7; }
.log-entry { color: #6b7280; }
.log-entry.ok   { color: #15803d; }
.log-entry.err  { color: #dc2626; }
.log-entry.info { color: #1d4ed8; }

/* Modal */
.modal-bg { position: fixed; inset: 0; background: rgba(0,0,0,0.45); z-index: 100; display: flex; align-items: center; justify-content: center; }
.modal { background: #fff; border-radius: 12px; border: 1px solid #e5e7eb; padding: 1.5rem; width: 460px; max-width: 95vw; }
.modal h3 { font-size: 16px; font-weight: 600; margin-bottom: 1rem; }
.field { margin-bottom: 12px; }
.field label { display: block; font-size: 12px; color: #6b7280; margin-bottom: 4px; }
.field input { width: 100%; padding: 7px 10px; border: 1px solid #d1d5db; border-radius: 6px; font-size: 13px; }
.field input:focus { outline: none; border-color: #3b82f6; }
.modal-footer { display: flex; justify-content: flex-end; gap: 8px; margin-top: 1rem; }
</style>
