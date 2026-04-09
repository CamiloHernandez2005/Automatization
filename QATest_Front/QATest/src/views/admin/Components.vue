<script setup>
import { ref, onMounted, computed } from "vue";
import { getContainers } from "@/services/containersService";

// Estado
const containers = ref([]);
const loading = ref(false);
const region = ref("");
const searchTerm = ref("");
const statusFilter = ref("all");

// Constantes
const REGIONS = ["dom", "int", "mx", "etp"];
const STATUS_OPTIONS = ["all", "up", "exited", "paused", "restarting"];

// 🔹 Cargar contenedores
const loadContainers = async () => {
  try {
    loading.value = true;
    const data = await getContainers();
    containers.value = data.data || [];
  } catch (error) {
    console.error("Error cargando contenedores:", error);
  } finally {
    loading.value = false;
  }
};

// 🔹 Filtros combinados (región + búsqueda + estado)
const filteredContainers = computed(() => {
  let result = containers.value;

  // Filtro por región
  if (region.value) {
    result = result.filter(c =>
      c.name?.toLowerCase().includes(region.value)
    );
  }

  // Filtro por búsqueda
  if (searchTerm.value) {
    const term = searchTerm.value.toLowerCase();
    result = result.filter(c =>
      c.name?.toLowerCase().includes(term) ||
      c.image?.toLowerCase().includes(term)
    );
  }

  // Filtro por estado
  if (statusFilter.value !== "all") {
    result = result.filter(c =>
      c.status?.toLowerCase().includes(statusFilter.value)
    );
  }

  return result;
});

// 🔹 Estadísticas
const stats = computed(() => ({
  total: containers.value.length,
  running: containers.value.filter(c =>
    c.status?.toLowerCase().includes("up")
  ).length,
  stopped: containers.value.filter(c =>
    c.status?.toLowerCase().includes("exited")
  ).length
}));

// 🔹 Color según estado
const getStatusColor = (status) => {
  const s = status?.toLowerCase() || "";
  if (s.includes("up")) return { bg: "#10b981", text: "white" };
  if (s.includes("exited")) return { bg: "#ef4444", text: "white" };
  if (s.includes("paused")) return { bg: "#f59e0b", text: "white" };
  if (s.includes("restarting")) return { bg: "#8b5cf6", text: "white" };
  return { bg: "#6b7280", text: "white" };
};

// 🔹 Formatear puertos
const formatPorts = (ports) => {
  if (!ports) return "-";
  if (Array.isArray(ports)) {
    return ports.map(p => `${p.publicPort || "?"}:${p.privatePort || "?"}`).join(", ");
  }
  return ports;
};

onMounted(() => {
  loadContainers();
});
</script>

<template>
  <div class="containers-dashboard">
    <!-- Header -->
    <div class="header">
      <h2>
        🐳 Docker Containers
        <span class="badge">{{ stats.total }}</span>
      </h2>
      <button @click="loadContainers" class="btn-refresh" :disabled="loading">
        <span class="refresh-icon" :class="{ spinning: loading }">🔄</span>
        {{ loading ? "Cargando..." : "Refrescar" }}
      </button>
    </div>

    <!-- Stats Cards -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-label">Total</div>
        <div class="stat-value">{{ stats.total }}</div>
      </div>
      <div class="stat-card running">
        <div class="stat-label">En ejecución</div>
        <div class="stat-value">{{ stats.running }}</div>
      </div>
      <div class="stat-card stopped">
        <div class="stat-label">Detenidos</div>
        <div class="stat-value">{{ stats.stopped }}</div>
      </div>
    </div>

    <!-- Filtros -->
    <div class="filters">
      <div class="filter-group">
        <label>🌍 Región</label>
        <select v-model="region" class="filter-select">
          <option value="">Todas</option>
          <option v-for="r in REGIONS" :key="r" :value="r">
            {{ r.toUpperCase() }}
          </option>
        </select>
      </div>

      <div class="filter-group">
        <label>🔍 Buscar</label>
        <input
          v-model="searchTerm"
          type="text"
          placeholder="Nombre o imagen..."
          class="filter-input"
        />
      </div>

      <div class="filter-group">
        <label>⚡ Estado</label>
        <select v-model="statusFilter" class="filter-select">
          <option v-for="opt in STATUS_OPTIONS" :key="opt" :value="opt">
            {{ opt === "all" ? "Todos" : opt.charAt(0).toUpperCase() + opt.slice(1) }}
          </option>
        </select>
      </div>

      <!-- Reset filters -->
      <button
        v-if="region || searchTerm || statusFilter !== 'all'"
        @click="region = ''; searchTerm = ''; statusFilter = 'all'"
        class="btn-reset"
      >
        🗑️ Limpiar filtros
      </button>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="loading-container">
      <div class="spinner"></div>
      <p>Cargando contenedores...</p>
    </div>

    <!-- Tabla de contenedores -->
    <div v-else-if="filteredContainers.length" class="table-container">
      <table class="containers-table">
        <thead>
          <tr>
            <th>Nombre</th>
            <th>Imagen</th>
            <th>Puertos</th>
            <th>Estado</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="c in filteredContainers" :key="c.name">
            <td class="container-name">
              <span class="name-text">{{ c.name }}</span>
            </td>
            <td class="container-image">{{ c.image }}</td>
            <td class="container-ports">{{ formatPorts(c.ports) }}</td>
            <td>
              <span
                class="status-badge"
                :style="{
                  backgroundColor: getStatusColor(c.status).bg,
                  color: getStatusColor(c.status).text
                }"
              >
                {{ c.status || "unknown" }}
              </span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Empty state -->
    <div v-else class="empty-state">
      <div class="empty-icon">📦</div>
      <p>No se encontraron contenedores</p>
      <p class="empty-hint">Prueba a cambiar los filtros o refrescar la lista</p>
    </div>
  </div>
</template>

<style scoped>
.containers-dashboard {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
  font-family: system-ui, -apple-system, 'Segoe UI', Roboto, sans-serif;
}

/* Header */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
}

.header h2 {
  margin: 0;
  font-size: 1.8rem;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 12px;
}

.badge {
  background: #e5e7eb;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 0.9rem;
  font-weight: 500;
  color: #374151;
}

.btn-refresh {
  background: #3b82f6;
  color: white;
  border: none;
  padding: 8px 20px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.9rem;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.2s;
}

.btn-refresh:hover:not(:disabled) {
  background: #2563eb;
  transform: translateY(-1px);
}

.btn-refresh:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.refresh-icon {
  display: inline-block;
}

.spinning {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* Stats Cards */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 16px;
  text-align: center;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  border: 1px solid #e5e7eb;
  transition: all 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.stat-card.running {
  border-left: 4px solid #10b981;
}

.stat-card.stopped {
  border-left: 4px solid #ef4444;
}

.stat-label {
  font-size: 0.85rem;
  color: #6b7280;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 2rem;
  font-weight: 700;
  color: #1f2937;
}

/* Filters */
.filters {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  flex-wrap: wrap;
  align-items: flex-end;
  background: #f9fafb;
  padding: 16px;
  border-radius: 12px;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.filter-group label {
  font-size: 0.8rem;
  font-weight: 600;
  color: #4b5563;
}

.filter-select,
.filter-input {
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 0.9rem;
  background: white;
  min-width: 140px;
}

.filter-input:focus,
.filter-select:focus {
  outline: none;
  border-color: #3b82f6;
  ring: 2px solid rgba(59,130,246,0.1);
}

.btn-reset {
  background: #6b7280;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.85rem;
  transition: background 0.2s;
}

.btn-reset:hover {
  background: #4b5563;
}

/* Loading */
.loading-container {
  text-align: center;
  padding: 60px 20px;
}

.spinner {
  border: 3px solid #e5e7eb;
  border-top-color: #3b82f6;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  animation: spin 0.8s linear infinite;
  margin: 0 auto 16px;
}

/* Table */
.table-container {
  overflow-x: auto;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  background: white;
}

.containers-table {
  width: 100%;
  border-collapse: collapse;
}

.containers-table thead {
  background: #f9fafb;
  border-bottom: 2px solid #e5e7eb;
}

.containers-table th {
  text-align: left;
  padding: 14px 16px;
  font-weight: 600;
  font-size: 0.85rem;
  color: #4b5563;
}

.containers-table td {
  padding: 14px 16px;
  border-bottom: 1px solid #f3f4f6;
}

.containers-table tbody tr:hover {
  background: #fefce8;
}

.container-name {
  font-weight: 500;
  font-family: 'Monaco', 'Courier New', monospace;
  font-size: 0.9rem;
}

.container-image {
  font-family: monospace;
  font-size: 0.85rem;
  color: #6b7280;
}

.container-ports {
  font-family: monospace;
  font-size: 0.85rem;
}

.status-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 500;
  text-transform: capitalize;
}

/* Empty state */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  background: white;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-state p {
  margin: 8px 0;
  color: #6b7280;
}

.empty-hint {
  font-size: 0.85rem;
}

/* Responsive */
@media (max-width: 768px) {
  .containers-dashboard {
    padding: 16px;
  }

  .filters {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-group {
    width: 100%;
  }

  .filter-select,
  .filter-input {
    width: 100%;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .containers-table th,
  .containers-table td {
    padding: 10px 12px;
  }
}
</style>
