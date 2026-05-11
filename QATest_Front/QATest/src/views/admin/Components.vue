<template>
  <div class="p-6 text-sm">

    <!-- ── Top loading bar ─────────────────────────────── -->
    <Transition name="top-loader-fade">
      <div v-if="isLoading" class="top-loader">
        <div class="top-loader-bar" />
      </div>
    </Transition>


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

          <button class="btn-header btn-header-new" @click="openRunModal">+ New</button>
          <button class="btn-header" @click="loadContainers">⟳ Refresh</button>
        </div>
      </div>
    </div>

    <!-- ── Server status ───────────────────────────────── -->
    <div v-if="serverError" class="mb-4 px-4 py-3 rounded-xl border border-red-200 bg-red-50 text-red-700 text-xs flex items-center gap-2">
      <span>⚠</span>
      <span class="flex-1">Server stats unavailable: <span class="font-mono">{{ serverError }}</span></span>
      <button class="btn-run-secondary" @click="loadServer">⟳ Retry</button>
    </div>
    <div v-else-if="!serverInfo" class="mb-4 px-4 py-3 rounded-xl border border-gray-200 bg-gray-50 text-gray-500 text-xs flex items-center gap-2">
      <span class="inline-block animate-spin">⟳</span>
      Loading server stats…
    </div>
    <div v-if="serverInfo" class="grid grid-cols-2 md:grid-cols-4 gap-3 mb-4">
      <div class="server-card">
        <div class="server-card-head">
          <span class="server-card-title">🖥 Memory</span>
          <span v-if="serverMemPercent !== null" class="server-card-pct">{{ serverMemPercent }}%</span>
        </div>
        <div class="server-bar">
          <div class="server-bar-fill" :class="pctClass(serverMemPercent)" :style="{ width: (serverMemPercent ?? 0) + '%' }" />
        </div>
        <div class="server-card-foot">
          {{ formatBytes(serverInfo.server.memory?.used) }} / {{ formatBytes(serverInfo.server.memory?.total) }}
        </div>
      </div>

      <div class="server-card">
        <div class="server-card-head">
          <span class="server-card-title">💾 Server storage</span>
          <span v-if="serverDisk" class="server-card-pct">{{ serverDisk.percent }}%</span>
        </div>
        <div class="server-bar">
          <div class="server-bar-fill" :class="pctClass(serverDisk?.percent)" :style="{ width: (serverDisk?.percent ?? 0) + '%' }" />
        </div>
        <div class="server-card-foot">
          {{ formatBytes(serverDisk?.used) }} / {{ formatBytes(serverDisk?.total) }}
          <span class="text-gray-400"> · free {{ formatBytes(serverDisk?.free) }}</span>
        </div>
      </div>

      <div class="server-card">
        <div class="server-card-head">
          <span class="server-card-title">📜 Logs folder</span>
          <span class="server-card-pct">
            <span v-if="logsSize != null">{{ formatBytes(logsSize) }}</span>
            <span v-else class="text-gray-400 text-xs italic" title="añade folders.logs_size al payload de /server">n/a</span>
          </span>
        </div>
        <div class="server-bar">
          <div class="server-bar-fill bg-emerald-500" :style="{ width: (logsShareOfDisk ?? 0) + '%' }" />
        </div>
        <div class="server-card-foot">
          <span v-if="logsSize != null && serverDisk?.total">
            {{ logsShareOfDisk }}% of disk · /opt/logs
          </span>
          <span v-else>/opt/logs</span>
        </div>
      </div>

      <div class="server-card">
        <div class="server-card-head">
          <span class="server-card-title">🐳 Containers</span>
          <span class="server-card-pct text-emerald-600">{{ serverInfo.containers?.memory?.running_count ?? 0 }} running</span>
        </div>
        <div class="text-xs text-gray-600 mt-2 leading-snug">
          Mem: <span class="font-mono">{{ formatBytes(serverInfo.containers?.memory?.total_bytes) }}</span>
        </div>
        <div class="text-[11px] text-gray-500 mt-1 space-y-0.5 max-h-12 overflow-hidden">
          <div v-for="d in dockerDfRows" :key="d.Type" class="flex justify-between">
            <span>{{ d.Type }}</span>
            <span class="font-mono">{{ d.Size }}</span>
          </div>
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
            <th class="th-base" @click="sortBy('cpu')">
              CPU <span class="sort-icon">{{ sortIcon('cpu') }}</span>
            </th>
            <th class="th-base" @click="sortBy('memory')">
              Memory <span class="sort-icon">{{ sortIcon('memory') }}</span>
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
            <td class="td-base text-xs">
              <span v-if="statsByName[c.name]" class="font-mono" :class="cpuClass(statsByName[c.name].cpu_perc)">{{ statsByName[c.name].cpu_perc }}</span>
              <span v-else class="text-gray-300">—</span>
            </td>
            <td class="td-base text-xs">
              <span v-if="statsByName[c.name]" class="font-mono" :title="statsByName[c.name].mem_usage">
                {{ statsByName[c.name].mem_perc }}
                <span class="text-gray-400 ml-1">{{ statsByName[c.name].mem_usage.split('/')[0].trim() }}</span>
              </span>
              <span v-else class="text-gray-300">—</span>
            </td>
            <td class="td-base truncate max-w-[220px] text-gray-600 font-mono text-xs" :title="c.image">{{ c.image }}</td>
            <td class="td-base text-gray-500 text-xs">{{ formatCreated(c.created) }}</td>
            <td class="td-base text-gray-500 text-xs font-mono">{{ formatPorts(c.ports) }}</td>
          </tr>
        </tbody>

        <tbody v-else-if="loading && !containers.length">
          <tr>
            <td colspan="8" class="text-center py-16 text-gray-400 text-sm">
              <span class="inline-block animate-spin mr-2">⟳</span> Loading containers...
            </td>
          </tr>
        </tbody>

        <tbody v-else>
          <tr>
            <td colspan="8" class="text-center py-16 text-gray-400 text-sm">No containers found</td>
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


    <!-- ── Modal: Run new container ────────────────────── -->
    <div v-if="showRunModal" class="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4" @click.self="closeRunModal">
      <div class="bg-white rounded-2xl shadow-2xl w-[720px] max-w-[95vw] max-h-[92vh] flex flex-col overflow-hidden">
        <!-- Header -->
        <div class="flex items-center justify-between px-5 py-3 border-b border-gray-200 bg-gradient-to-r from-slate-800 to-slate-700">
          <div>
            <h3 class="text-white font-semibold tracking-tight">Run new container</h3>
            <p class="text-slate-300 text-[11px]">Pick a Docker Hub image and configure runtime options</p>
          </div>
          <button class="text-slate-300 hover:text-white text-lg leading-none" @click="closeRunModal">✕</button>
        </div>

        <!-- Body -->
        <div class="flex-1 overflow-y-auto p-5 space-y-5">

          <!-- Image -->
          <section>
            <h4 class="section-title">🐳 Image</h4>
            <div class="text-xs text-gray-500 mb-2">
              Repository: <span class="font-mono text-slate-700">{{ DOCKERHUB_USER }}/{{ DOCKERHUB_REPO }}</span>
            </div>

            <div class="flex items-center justify-between mb-2">
              <label class="run-label !mb-0">
                Tag
                <span v-if="dockerHub.loadingTags" class="text-gray-400 ml-1">(loading…)</span>
                <span v-else-if="runForm.tag" class="text-emerald-600 ml-1">(1 selected)</span>
                <span v-else-if="dockerHub.tags.length" class="text-gray-400 ml-1">({{ filteredTags.length }} of {{ dockerHub.tags.length }})</span>
              </label>
              <button class="btn-run-secondary" :disabled="dockerHub.loadingTags" @click="loadTags">
                ⟳ Reload
              </button>
            </div>

            <div v-if="!runForm.tag" class="relative mb-2">
              <span class="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400 pointer-events-none flex items-center">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <circle cx="11" cy="11" r="8"/>
                  <path d="m21 21-4.3-4.3"/>
                </svg>
              </span>
              <input
                v-model="tagFilter"
                placeholder="Search tag by name…"
                class="run-input search-input"
              />
            </div>

            <div class="border border-gray-200 rounded-lg overflow-hidden bg-white">
              <div v-if="!dockerHub.tags.length && !dockerHub.loadingTags" class="text-gray-400 text-center py-6 text-xs">
                No tags
              </div>
              <div v-else class="max-h-56 overflow-y-auto">
                <table class="w-full text-sm">
                  <thead class="bg-gray-50 sticky top-0 z-10">
                    <tr>
                      <th class="tag-th" @click="cycleSort('name')">
                        Tag <span class="sort-icon">{{ sortIconFor('name') }}</span>
                      </th>
                      <th class="tag-th tag-th-date" @click="cycleSort('date')">
                        Last updated <span class="sort-icon">{{ sortIconFor('date') }}</span>
                      </th>
                      <th v-if="runForm.tag" class="tag-th text-right">
                        <button class="btn-run-secondary !py-0.5" @click="runForm.tag = ''">✕ Clear</button>
                      </th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr
                      v-for="t in displayedTags"
                      :key="t.name"
                      class="tag-row-tr"
                      :class="{ 'tag-row-active': runForm.tag === t.name }"
                      @click="selectTag(t.name)"
                    >
                      <td class="tag-td font-mono">{{ t.name }}</td>
                      <td class="tag-td text-gray-500 text-xs">{{ formatTagDate(t.last_updated) }}</td>
                      <td v-if="runForm.tag" class="tag-td text-right text-emerald-600 text-xs font-semibold">✓ selected</td>
                    </tr>
                    <tr v-if="!displayedTags.length">
                      <td :colspan="runForm.tag ? 3 : 2" class="text-center py-6 text-gray-400 text-xs">No matches</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>

            <div v-if="fullImage" class="text-xs text-gray-500 mt-2">
              Full image: <span class="font-mono text-slate-700">{{ fullImage }}</span>
            </div>
            <div v-if="dockerHub.error" class="text-xs text-red-600 mt-1">{{ dockerHub.error }}</div>
          </section>

          <!-- Container -->
          <section>
            <h4 class="section-title">⚙️ Container</h4>
            <div class="grid grid-cols-2 gap-3">
              <div>
                <label class="run-label">Name <span class="text-red-500">*</span></label>
                <input v-model="runForm.name" class="run-input" placeholder="my-app" />
              </div>
              <div>
                <label class="run-label">Port</label>
                <input v-model="runForm.port" class="run-input" placeholder="8080 or 8080:80" />
              </div>
              <div>
                <label class="run-label">CPUs</label>
                <select v-model="runForm.cpus" class="run-select">
                  <option v-for="opt in CPU_OPTIONS" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
                </select>
              </div>
              <div>
                <label class="run-label">Memory</label>
                <select v-model="runForm.memory" class="run-select">
                  <option v-for="opt in MEMORY_OPTIONS" :key="opt.value" :value="opt.value">{{ opt.label }}</option>
                </select>
              </div>
            </div>
          </section>

          <!-- Volumes & env -->
          <section>
            <h4 class="section-title">📁 Volumes &amp; environment</h4>

            <!-- env_file -->
            <div class="mb-3">
              <label class="run-label">Env file <span class="text-gray-400 font-normal">(/opt/properties/)</span></label>
              <div class="flex gap-2">
                <input v-model="runForm.env_file" class="run-input flex-1 font-mono" placeholder="myapp/prod.env" readonly />
                <button class="btn-run-secondary" @click="toggleBrowser('properties')">
                  {{ browser.properties.open ? "Close" : "Browse" }}
                </button>
                <button v-if="runForm.env_file" class="btn-run-secondary" @click="runForm.env_file = ''">Clear</button>
              </div>
              <div v-if="browser.properties.open" class="picker">
                <div class="picker-bar">
                  <button class="picker-crumb" @click="loadProperties('')">/</button>
                  <template v-for="(seg, i) in pathSegments(browser.properties.path)" :key="i">
                    <span class="text-gray-400">/</span>
                    <button class="picker-crumb" @click="loadProperties(joinSegments(pathSegments(browser.properties.path), i))">{{ seg }}</button>
                  </template>
                  <span class="ml-auto text-gray-400" v-if="browser.properties.loading">Loading…</span>
                </div>
                <input
                  v-model="browser.properties.filter"
                  placeholder="Filter…"
                  class="run-input mb-2 text-xs"
                />
                <div class="picker-list">
                  <div v-if="!filteredPropertyItems.length && !browser.properties.loading" class="text-gray-400 text-center py-4">
                    {{ browser.properties.items.length ? "No matches" : "Empty" }}
                  </div>
                  <button
                    v-for="item in filteredPropertyItems"
                    :key="item.path"
                    class="picker-item"
                    @click="onPropertiesItemClick(item)"
                  >
                    <span>{{ item.type === "directory" ? "📁" : "📄" }}</span>
                    <span class="font-mono">{{ item.name }}</span>
                  </button>
                </div>
                <div v-if="browser.properties.error" class="text-red-600 text-xs mt-1">{{ browser.properties.error }}</div>
              </div>
            </div>

            <!-- logs_folder -->
            <div class="mb-3">
              <label class="run-label">Logs folder <span class="text-gray-400 font-normal">(/opt/logs/)</span></label>
              <div class="flex gap-2">
                <input v-model="runForm.logs_folder" class="run-input flex-1 font-mono" placeholder="myapp/prod" readonly />
                <button class="btn-run-secondary" @click="toggleBrowser('logs')">
                  {{ browser.logs.open ? "Close" : "Browse" }}
                </button>
                <button v-if="runForm.logs_folder" class="btn-run-secondary" @click="runForm.logs_folder = ''">Clear</button>
              </div>
              <div v-if="browser.logs.open" class="picker">
                <div class="picker-bar">
                  <button class="picker-crumb" @click="loadLogs('')">/</button>
                  <template v-for="(seg, i) in pathSegments(browser.logs.path)" :key="i">
                    <span class="text-gray-400">/</span>
                    <button class="picker-crumb" @click="loadLogs(joinSegments(pathSegments(browser.logs.path), i))">{{ seg }}</button>
                  </template>
                  <span class="ml-auto text-gray-400" v-if="browser.logs.loading">Loading…</span>
                </div>
                <input
                  v-model="browser.logs.filter"
                  placeholder="Filter…"
                  class="run-input mb-2 text-xs"
                />
                <div class="picker-list">
                  <div v-if="!filteredLogItems.length && !browser.logs.loading" class="text-gray-400 text-center py-4">
                    {{ browser.logs.items.length ? "No matches" : "Empty" }}
                  </div>
                  <button
                    v-for="item in filteredLogItems"
                    :key="item.path"
                    class="picker-item"
                    @click="onLogsItemClick(item)"
                  >
                    <span>📁</span>
                    <span class="font-mono">{{ item.name }}</span>
                  </button>
                </div>
                <div class="flex gap-2 items-center mt-2">
                  <input
                    v-model="newFolderName"
                    placeholder="new-folder-name"
                    class="run-input flex-1 text-xs"
                    @keydown.enter.prevent="createLogsFolder"
                  />
                  <button class="btn-run-secondary" :disabled="!newFolderName.trim()" @click="createLogsFolder">+ Create</button>
                  <button class="btn-run-secondary" @click="pickCurrentLogsFolder" :disabled="!browser.logs.path">Use this folder</button>
                </div>
                <div v-if="browser.logs.error" class="text-red-600 text-xs mt-1">{{ browser.logs.error }}</div>
              </div>
            </div>

            <!-- extra volumes -->
            <div class="mb-3">
              <label class="run-label">
                Extra volumes
                <span class="text-gray-400 font-normal">(host:container[:ro])</span>
              </label>
              <div class="flex gap-2">
                <input
                  v-model="newVolume"
                  class="run-input flex-1 font-mono"
                  placeholder="/host/path:/container/path"
                  @keydown.enter.prevent="addVolume"
                />
                <button class="btn-run-secondary" :disabled="!isValidVolume(newVolume)" @click="addVolume">+ Add</button>
              </div>
              <div v-if="runForm.volumes.length" class="space-y-1 mt-2">
                <div
                  v-for="(v, i) in runForm.volumes"
                  :key="i"
                  class="flex items-center gap-2 bg-gray-50 border border-gray-200 rounded-lg px-3 py-1.5 text-xs font-mono"
                >
                  <span class="flex-1 truncate" :title="v">{{ v }}</span>
                  <button type="button" class="text-red-500 hover:text-red-700 font-bold" @click="removeVolume(i)">✕</button>
                </div>
              </div>
            </div>

            <!-- container log path -->
            <div>
              <label class="run-label">Container log path</label>
              <input v-model="runForm.container_log_path" class="run-input font-mono" placeholder="/app/logs/" />
            </div>
          </section>

          <div v-if="runError" class="bg-red-50 border border-red-200 text-red-700 text-xs rounded-lg px-3 py-2 font-mono whitespace-pre-wrap">{{ runError }}</div>
        </div>

        <!-- Footer -->
        <div class="flex justify-end gap-2 px-5 py-3 border-t border-gray-200 bg-gray-50">
          <button class="btn-sm-tw" :disabled="runSubmitting" @click="closeRunModal">Cancel</button>
          <button class="btn-run" :disabled="!canSubmitRun || runSubmitting" @click="handleRun">
            {{ runSubmitting ? "Running…" : "Run container" }}
          </button>
        </div>
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
import { ref, reactive, computed, watch, onMounted, onUnmounted, nextTick } from "vue";
import {
  getContainers,
  getContainerLogs,
  runContainer,
  startContainers,
  stopContainers,
  restartContainers,
  killContainers,
  getDockerHubTags,
  listProperties,
  listLogs,
  createLogFolder,
  getAllContainerStats,
  getServerInfo,
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

// ── Run wizard state ──────────────────────────────────
const DOCKERHUB_USER = "qualityassurancebog";
const DOCKERHUB_REPO = "qarepository";

const DEFAULT_RUN_FORM = () => ({
  tag: "",
  name: "",
  port: "",
  cpus: "",
  memory: "",
  env_file: "",
  logs_folder: "",
  container_log_path: "/app/logs/",
  volumes: [],
});

const runForm     = ref(DEFAULT_RUN_FORM());
const runError    = ref("");
const runSubmitting = ref(false);
const tagSort     = ref("date-desc");
const tagFilter   = ref("");

const CPU_OPTIONS = [
  { value: "",    label: "No limit" },
  { value: "0.25", label: "0.25 vCPU" },
  { value: "0.5",  label: "0.5 vCPU" },
  { value: "1",    label: "1 vCPU" },
  { value: "1.5",  label: "1.5 vCPU" },
  { value: "2",    label: "2 vCPU" },
  { value: "4",    label: "4 vCPU" },
];

const MEMORY_OPTIONS = [
  { value: "",     label: "No limit" },
  { value: "256m", label: "256 MB" },
  { value: "512m", label: "512 MB" },
  { value: "1g",   label: "1 GB" },
  { value: "2g",   label: "2 GB" },
  { value: "4g",   label: "4 GB" },
  { value: "8g",   label: "8 GB" },
];

const dockerHub = reactive({
  tags: [],
  loadingTags: false,
  error: "",
});

const browser = reactive({
  properties: { open: false, path: "", items: [], loading: false, error: "", filter: "" },
  logs:       { open: false, path: "", items: [], loading: false, error: "", filter: "" },
});

const newFolderName = ref("");
const newVolume     = ref("");

// ── Stats & server state ──────────────────────────────
const statsByName = ref({});
const serverInfo  = ref(null);
const serverError = ref("");

const serverMemPercent = computed(() => serverInfo.value?.server?.memory?.percent ?? null);
const serverDisk = computed(() =>
  serverInfo.value?.server?.disks?.logs
    ?? serverInfo.value?.server?.disks?.properties
    ?? null
);
const logsSize   = computed(() => serverInfo.value?.server?.folders?.logs_size ?? null);

const logsShareOfDisk = computed(() => {
  const size = logsSize.value;
  const total = serverDisk.value?.total;
  if (size == null || !total) return null;
  return Math.round((size / total) * 1000) / 10;
});
const dockerDfRows     = computed(() => (serverInfo.value?.containers?.disk || []).slice(0, 3));

const isLoading = computed(() => loading.value || runSubmitting.value);

function pctClass(p) {
  if (p == null) return "bg-gray-300";
  if (p >= 90) return "bg-red-500";
  if (p >= 75) return "bg-amber-500";
  return "bg-emerald-500";
}

function cpuClass(s = "") {
  const v = parseFloat(s);
  if (Number.isNaN(v)) return "text-gray-500";
  if (v >= 80) return "text-red-600 font-semibold";
  if (v >= 50) return "text-amber-600 font-semibold";
  return "text-gray-700";
}

function formatBytes(b) {
  if (b == null || Number.isNaN(b)) return "—";
  const u = ["B", "KB", "MB", "GB", "TB", "PB"];
  let i = 0;
  let v = Number(b);
  while (v >= 1024 && i < u.length - 1) { v /= 1024; i++; }
  return `${v.toFixed(v >= 10 || i === 0 ? 0 : 1)} ${u[i]}`;
}

function isValidVolume(v) {
  if (!v || typeof v !== "string") return false;
  const t = v.trim();
  if (!t.includes(":") || t.length < 3) return false;
  const parts = t.split(":");
  return parts.length >= 2 && parts.every((p) => p.length > 0);
}

function addVolume() {
  const v = newVolume.value.trim();
  if (!isValidVolume(v) || runForm.value.volumes.includes(v)) return;
  runForm.value.volumes.push(v);
  newVolume.value = "";
}

function removeVolume(i) {
  runForm.value.volumes.splice(i, 1);
}

const fullImage = computed(() => {
  const tag = runForm.value.tag?.trim();
  if (!tag) return "";
  return `${DOCKERHUB_USER}/${DOCKERHUB_REPO}:${tag}`;
});

const filteredTags = computed(() => {
  const q = tagFilter.value.trim().toLowerCase();
  let list = q
    ? dockerHub.tags.filter((t) => t.name.toLowerCase().includes(q))
    : [...dockerHub.tags];
  list.sort((a, b) => {
    switch (tagSort.value) {
      case "date-asc":  return tagDateValue(a) - tagDateValue(b);
      case "date-desc": return tagDateValue(b) - tagDateValue(a);
      case "name-asc":  return a.name.localeCompare(b.name);
      case "name-desc": return b.name.localeCompare(a.name);
      default:          return 0;
    }
  });
  return list;
});

const displayedTags = computed(() => {
  if (runForm.value.tag) {
    const found = dockerHub.tags.find((t) => t.name === runForm.value.tag);
    return found ? [found] : [];
  }
  return filteredTags.value;
});

function selectTag(name) {
  runForm.value.tag = runForm.value.tag === name ? "" : name;
}

function cycleSort(col) {
  if (col === "date") {
    tagSort.value = tagSort.value === "date-desc" ? "date-asc" : "date-desc";
  } else if (col === "name") {
    tagSort.value = tagSort.value === "name-asc" ? "name-desc" : "name-asc";
  }
}

function sortIconFor(col) {
  if (col === "date") {
    if (tagSort.value === "date-desc") return "↓";
    if (tagSort.value === "date-asc") return "↑";
  }
  if (col === "name") {
    if (tagSort.value === "name-asc") return "↑";
    if (tagSort.value === "name-desc") return "↓";
  }
  return "↕";
}

function tagDateValue(t) {
  const d = t?.last_updated ? new Date(t.last_updated).getTime() : NaN;
  return Number.isNaN(d) ? 0 : d;
}

const filteredPropertyItems = computed(() => {
  const q = browser.properties.filter.trim().toLowerCase();
  if (!q) return browser.properties.items;
  return browser.properties.items.filter((i) => i.name.toLowerCase().includes(q));
});

const filteredLogItems = computed(() => {
  const q = browser.logs.filter.trim().toLowerCase();
  if (!q) return browser.logs.items;
  return browser.logs.items.filter((i) => i.name.toLowerCase().includes(q));
});

const canSubmitRun = computed(() =>
  !!runForm.value.tag?.trim() && !!runForm.value.name?.trim()
);

function formatTagDate(s) {
  if (!s) return "";
  const d = new Date(s);
  if (isNaN(d)) return "";
  return d.toLocaleDateString();
}

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
      const va = sortValue(a, sortKey.value);
      const vb = sortValue(b, sortKey.value);
      const v = va < vb ? -1 : va > vb ? 1 : 0;
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

function sortValue(c, key) {
  if (key === "cpu") {
    const s = statsByName.value[c.name];
    return s ? (parseFloat(s.cpu_perc) || 0) : -1;
  }
  if (key === "memory") {
    const s = statsByName.value[c.name];
    return s ? (parseFloat(s.mem_perc) || 0) : -1;
  }
  return c[key] ?? "";
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
  loadStats();
}

async function loadStats() {
  try {
    const data = await getAllContainerStats();
    if (data.success) {
      const map = {};
      for (const s of data.data) map[s.name] = s;
      statsByName.value = map;
    }
  } catch (e) {
    console.warn("loadStats failed:", e?.response?.data || e.message);
  }
}

async function loadServer() {
  try {
    const data = await getServerInfo();
    if (data.success) {
      serverInfo.value = data;
      serverError.value = "";
    } else {
      serverError.value = data.error || "Unknown error";
      log("Server info: " + serverError.value, "err");
    }
  } catch (e) {
    const status = e?.response?.status;
    const msg = e?.response?.data?.error || e?.message || "unknown";
    serverError.value = `${msg}${status ? ` (HTTP ${status})` : ""}`;
    log("Server info error: " + serverError.value, "err");
    console.error("loadServer failed:", e?.response || e);
  }
}

async function bulkAction(action) {
  if (!selected.value.length) return;
  const names = [...selected.value];
  const verb = action === "kill" ? "kill + remove" : action;
  log(`docker ${verb} → [${names.join(", ")}]`, "info");

  const fn = { start: startContainers, stop: stopContainers, restart: restartContainers, kill: killContainers }[action];
  try {
    const data = await fn(names);
    if (data.success) {
      log(`${verb} OK on ${names.length} container(s)`, "ok");
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

// ── Run wizard ────────────────────────────────────────
function openRunModal() {
  runForm.value = DEFAULT_RUN_FORM();
  runError.value = "";
  newFolderName.value = "";
  newVolume.value = "";
  tagFilter.value = "";
  browser.properties = { open: false, path: "", items: [], loading: false, error: "", filter: "" };
  browser.logs       = { open: false, path: "", items: [], loading: false, error: "", filter: "" };
  showRunModal.value = true;
  loadTags();
}

function closeRunModal() {
  showRunModal.value = false;
}

async function loadTags() {
  dockerHub.loadingTags = true;
  dockerHub.error = "";
  try {
    const data = await getDockerHubTags(DOCKERHUB_USER, DOCKERHUB_REPO);
    if (data.success) {
      dockerHub.tags = data.data;
      log(`${data.count} tags loaded from ${DOCKERHUB_USER}/${DOCKERHUB_REPO}`, "ok");
    } else {
      dockerHub.error = data.error || "Could not load tags";
      log("Tags error: " + dockerHub.error, "err");
    }
  } catch (e) {
    const status = e?.response?.status;
    const apiMsg = e?.response?.data?.error;
    dockerHub.error = apiMsg
      ? `${apiMsg}${status ? ` (HTTP ${status})` : ""}`
      : `${e.message}${status ? ` (HTTP ${status})` : ""}`;
    console.error("loadTags failed:", e?.response || e);
    log("Tags error: " + dockerHub.error, "err");
  } finally {
    dockerHub.loadingTags = false;
  }
}

// ── File browsers ─────────────────────────────────────
function pathSegments(p) {
  return p ? p.split("/").filter(Boolean) : [];
}

function joinSegments(segs, lastIndex) {
  return segs.slice(0, lastIndex + 1).join("/");
}

async function toggleBrowser(kind) {
  const b = browser[kind];
  b.open = !b.open;
  if (b.open && !b.items.length) {
    if (kind === "properties") await loadProperties("");
    else await loadLogs("");
  }
}

async function loadProperties(path) {
  const b = browser.properties;
  b.loading = true;
  b.error = "";
  try {
    const data = await listProperties(path);
    if (data.success) {
      b.path = data.path || "";
      b.items = data.items;
    } else {
      b.error = data.error || "Could not load directory";
    }
  } catch (e) {
    b.error = e?.response?.data?.error || e.message;
  } finally {
    b.loading = false;
  }
}

async function loadLogs(path) {
  const b = browser.logs;
  b.loading = true;
  b.error = "";
  try {
    const data = await listLogs(path);
    if (data.success) {
      b.path = data.path || "";
      b.items = data.items.filter((i) => i.type === "directory");
    } else {
      b.error = data.error || "Could not load directory";
    }
  } catch (e) {
    b.error = e?.response?.data?.error || e.message;
  } finally {
    b.loading = false;
  }
}

function onPropertiesItemClick(item) {
  if (item.type === "directory") {
    loadProperties(item.path);
  } else {
    runForm.value.env_file = item.path;
    browser.properties.open = false;
  }
}

function onLogsItemClick(item) {
  loadLogs(item.path);
}

function pickCurrentLogsFolder() {
  if (!browser.logs.path) return;
  runForm.value.logs_folder = browser.logs.path;
  browser.logs.open = false;
}

async function createLogsFolder() {
  const name = newFolderName.value.trim();
  if (!name) return;
  const target = browser.logs.path ? `${browser.logs.path}/${name}` : name;
  const b = browser.logs;
  b.error = "";
  try {
    const data = await createLogFolder(target);
    if (data.success) {
      newFolderName.value = "";
      log(`Created /opt/logs/${data.path}`, "ok");
      runForm.value.logs_folder = data.path;
      await loadLogs(b.path);
      b.open = false;
    } else {
      b.error = data.error || "Could not create folder";
    }
  } catch (e) {
    b.error = e?.response?.data?.error || e.message;
  }
}

async function handleRun() {
  if (!canSubmitRun.value) {
    runError.value = "Username, repository and container name are required";
    return;
  }
  runError.value = "";
  runSubmitting.value = true;

  const f = runForm.value;
  const payload = {
    image: `${DOCKERHUB_USER}/${DOCKERHUB_REPO}`,
    tag: f.tag.trim(),
    name: f.name.trim(),
    pull: true,
    container_log_path: (f.container_log_path || "/app/logs/").trim(),
  };
  if (f.port?.toString().trim()) payload.port = f.port.toString().trim();
  if (f.cpus !== "" && f.cpus !== null && !Number.isNaN(Number(f.cpus))) payload.cpus = Number(f.cpus);
  if (f.memory?.trim()) payload.memory = f.memory.trim();
  if (f.env_file?.trim()) payload.env_file = f.env_file.trim();
  if (f.logs_folder?.trim()) payload.logs_folder = f.logs_folder.trim();
  if (f.volumes?.length) payload.volumes = [...f.volumes];

  log(`docker run ${payload.name} (${payload.image}:${payload.tag})...`, "info");
  try {
    const data = await runContainer(payload);
    if (data.success) {
      log(`Container started — ID: ${(data.container_id || "").slice(0, 12)}`, "ok");
      closeRunModal();
      await loadContainers();
    } else {
      const detail = data.step ? `[${data.step}] ${data.error}` : data.error;
      runError.value = detail || "Run failed";
      log("Error running container: " + runError.value, "err");
    }
  } catch (e) {
    runError.value = e?.response?.data?.error || e.message;
    log("Error: " + runError.value, "err");
  } finally {
    runSubmitting.value = false;
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

let serverTimer = null;

onMounted(() => {
  loadContainers();
  loadServer();
  refreshTimer = setInterval(() => { loadContainers(); loadServer(); }, 60000);
  serverTimer  = setInterval(loadStats, 5000);
  tickTimer = setInterval(() => { now.value = Date.now(); }, 60000);
});

onUnmounted(() => {
  stopPoll();
  if (refreshTimer) clearInterval(refreshTimer);
  if (serverTimer) clearInterval(serverTimer);
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
.btn-header-new     { @apply border-emerald-400/30 text-emerald-200 hover:bg-emerald-500/25 font-semibold; }

/* Top loading bar */
.top-loader {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  z-index: 9999;
  overflow: hidden;
  background: rgba(59, 130, 246, 0.12);
  pointer-events: none;
}
.top-loader-bar {
  position: absolute;
  top: 0;
  bottom: 0;
  width: 35%;
  background: linear-gradient(90deg, transparent, #3b82f6 50%, transparent);
  animation: top-loader-slide 1.3s infinite ease-in-out;
}
@keyframes top-loader-slide {
  0%   { left: -35%; }
  100% { left: 100%; }
}
.top-loader-fade-enter-active,
.top-loader-fade-leave-active {
  transition: opacity 200ms ease;
}
.top-loader-fade-enter-from,
.top-loader-fade-leave-to {
  opacity: 0;
}

/* Server status cards */
.server-card {
  @apply bg-white border border-gray-200 rounded-xl px-3 py-2 shadow-sm;
}
.server-card-head {
  @apply flex items-center justify-between gap-2;
}
.server-card-title {
  @apply text-[11px] font-semibold text-gray-600 uppercase tracking-wider;
}
.server-card-pct {
  @apply text-xs font-bold text-slate-700;
}
.server-card-foot {
  @apply text-[11px] text-gray-500 mt-1 font-mono;
}
.server-bar {
  @apply h-1.5 bg-gray-100 rounded-full overflow-hidden mt-2;
}
.server-bar-fill {
  @apply h-full transition-all duration-300;
}

/* Run wizard modal */
.section-title {
  @apply text-[11px] font-semibold uppercase tracking-wider text-slate-500 mb-2;
}
.run-label {
  @apply block text-[11px] font-medium text-gray-500 mb-1;
}
.run-input {
  @apply w-full px-3 py-2 border border-gray-200 rounded-lg text-sm bg-white shadow-sm
         focus:outline-none focus:ring-2 focus:ring-blue-500/30 focus:border-blue-400 transition-all
         disabled:bg-gray-50 disabled:text-gray-400 disabled:cursor-not-allowed;
}
.search-input {
  padding-left: 2.25rem;
}
.run-select {
  @apply appearance-none w-full pl-3 py-2 border border-gray-200 rounded-lg text-sm bg-white shadow-sm
         cursor-pointer font-medium text-gray-700
         hover:border-gray-300
         focus:outline-none focus:ring-2 focus:ring-blue-500/30 focus:border-blue-400 transition-all
         disabled:bg-gray-50 disabled:text-gray-400 disabled:cursor-not-allowed;
  padding-right: 2.25rem;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='16' height='16' viewBox='0 0 24 24' fill='none' stroke='%2364748b' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpolyline points='6 9 12 15 18 9'%3E%3C/polyline%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 0.75rem center;
  background-size: 1rem 1rem;
}
.run-select:focus {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='16' height='16' viewBox='0 0 24 24' fill='none' stroke='%233b82f6' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpolyline points='6 9 12 15 18 9'%3E%3C/polyline%3E%3C/svg%3E");
}
.btn-run-secondary {
  @apply px-3 py-2 rounded-lg border border-gray-300 bg-white text-gray-700 text-xs font-medium
         hover:bg-gray-50 transition-colors whitespace-nowrap
         disabled:opacity-40 disabled:cursor-not-allowed disabled:pointer-events-none;
}

/* Tag datatable */
.tag-th {
  @apply text-left text-[11px] font-semibold text-gray-400 uppercase tracking-wider
         px-3 py-2 border-b border-gray-200 cursor-pointer select-none
         whitespace-nowrap transition-colors hover:text-gray-700;
}
.tag-th-date { @apply w-40; }
.tag-td {
  @apply px-3 py-2 border-b border-gray-100/80 align-middle whitespace-nowrap;
}
.tag-row-tr {
  @apply cursor-pointer transition-colors hover:bg-blue-50;
}
.tag-row-tr.tag-row-active {
  @apply bg-blue-100 hover:bg-blue-100 font-semibold text-blue-800;
}

/* Inline file/folder picker */
.picker {
  @apply mt-2 border border-gray-200 rounded-lg bg-gray-50 p-2 text-xs;
}
.picker-bar {
  @apply flex items-center gap-1 mb-2 text-gray-600 flex-wrap;
}
.picker-crumb {
  @apply px-1.5 py-0.5 rounded hover:bg-blue-100 hover:text-blue-700 text-gray-700 transition-colors;
}
.picker-list {
  @apply max-h-44 overflow-y-auto bg-white border border-gray-200 rounded;
}
.picker-item {
  @apply w-full px-2 py-1.5 text-left hover:bg-blue-50 flex items-center gap-2
         border-b border-gray-100 last:border-b-0 transition-colors;
}

/* General buttons */
.btn {
  @apply px-3 py-1.5 rounded-lg border border-gray-300 bg-white text-gray-700 cursor-pointer text-[13px] transition-colors duration-150 hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed disabled:pointer-events-none;
}
.btn-run {
  @apply px-4 py-1.5 rounded-lg border border-slate-800 bg-slate-800 text-white text-xs font-semibold
         cursor-pointer transition-colors duration-150 hover:bg-slate-700 hover:border-slate-700
         disabled:opacity-40 disabled:cursor-not-allowed disabled:pointer-events-none;
}
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
