<script setup lang="ts">
import { onMounted, reactive, ref, computed } from 'vue'
import { storeToRefs } from 'pinia'
import { useTasksStore } from '@/stores/tasks'
import { useToast } from '@/composables/useToast'
import type { Task, TaskRequest, TaskStatus } from '@/types/task'

const tasksStore = useTasksStore()
const { items, loading } = storeToRefs(tasksStore)
const { toasts, removeToast, success, error: showError } = useToast()

const statuses: TaskStatus[] = ['pendente', 'concluída']

const showCreateModal = ref(false)
const showEditModal = ref(false)
const showDeleteModal = ref(false)
const taskToEdit = ref<Task | null>(null)
const taskToDelete = ref<Task | null>(null)

const form = reactive<TaskRequest>({
  title: '',
  description: '',
  status: 'pendente'
})

const editForm = reactive<TaskRequest>({
  title: '',
  description: '',
  status: 'pendente'
})

const isValid = computed(() => form.title.trim().length > 0)
const isEditValid = computed(() => editForm.title.trim().length > 0)

function capitalizeStatus(status: string): string {
  return status.charAt(0).toUpperCase() + status.slice(1)
}

function formatDate(date: string | Date | undefined | null): string {
  if (!date) return '—'

  try {
    const dateObj = typeof date === 'string' ? new Date(date) : date
    if (isNaN(dateObj.getTime())) return '—'

    return new Intl.DateTimeFormat('pt-BR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    }).format(dateObj)
  } catch {
    return '—'
  }
}

function resetForm() {
  form.title = ''
  form.description = ''
  form.status = 'pendente'
}

function openCreateModal() {
  resetForm()
  showCreateModal.value = true
}

function closeCreateModal() {
  showCreateModal.value = false
  resetForm()
}

function openEditModal(task: Task) {
  taskToEdit.value = task
  editForm.title = task.title
  editForm.description = task.description
  editForm.status = task.status
  showEditModal.value = true
}

function closeEditModal() {
  showEditModal.value = false
  taskToEdit.value = null
  editForm.title = ''
  editForm.description = ''
  editForm.status = 'pendente'
}

function openDeleteModal(task: Task) {
  taskToDelete.value = task
  showDeleteModal.value = true
}

function closeDeleteModal() {
  showDeleteModal.value = false
  taskToDelete.value = null
}

function extractErrorMessage(error: unknown): string {
  if (!error) return 'Erro desconhecido'
  if (typeof error === 'string') return error
  if (typeof error === 'object') {
    const err = error as { message?: string; status?: number | string; details?: unknown; debugMessage?: string }
    const details = (err.details && typeof err.details === 'object'
      ? (err.details as { status?: string; message?: string; debugMessage?: string })
      : undefined)

    const isBadRequest =
      (typeof err.status === 'number' && err.status === 400) ||
      (typeof details?.status === 'string' && details.status === 'BAD_REQUEST')

    // Para BAD_REQUEST, priorize a mensagem do payload
    if (isBadRequest) {
      if (details?.message && details.message.trim()) return details.message.trim()
      if (typeof err.message === 'string' && err.message.trim()) return err.message.trim()
      if (details?.debugMessage && details.debugMessage.trim()) return details.debugMessage.trim()
      return 'Requisição inválida.'
    }

    // Outros casos: mantenha a extração padrão
    if (typeof err.message === 'string' && err.message.trim()) return err.message.trim()
    if (details?.message && details.message.trim()) return details.message.trim()
    if (details?.debugMessage && details.debugMessage.trim()) return details.debugMessage.trim()
    if (typeof err.debugMessage === 'string' && err.debugMessage.trim()) return err.debugMessage.trim()
  }
  return 'Falha ao comunicar com a API'
}

async function submitForm() {
  if (!isValid.value) return
  try {
    await tasksStore.addTask({
      title: form.title.trim(),
      description: form.description.trim(),
      status: form.status
    })
    success('Tarefa criada com sucesso!')
    closeCreateModal()
  } catch (err) {
    const message = extractErrorMessage(err)
    if (message) showError(message, 6000)
  }
}

async function submitEdit() {
  if (!isEditValid.value || !taskToEdit.value) return
  try {
    await tasksStore.updateTask(taskToEdit.value.id, {
      title: editForm.title.trim(),
      description: editForm.description.trim(),
      status: editForm.status
    })
    success('Tarefa atualizada com sucesso!')
    closeEditModal()
  } catch (err) {
    const message = extractErrorMessage(err)
    showError(message, 6000)
  }
}

async function confirmDelete() {
  if (!taskToDelete.value) return
  try {
    await tasksStore.removeTask(taskToDelete.value.id)
    success('Tarefa excluída com sucesso!')
    closeDeleteModal()
  } catch (err) {
    const message = extractErrorMessage(err)
    showError(message, 6000)
  }
}

async function toggle(task: Task) {
  try {
    await tasksStore.toggleStatus(task)
    const newStatus = task.status === 'pendente' ? 'concluída' : 'pendente'
    success(`Tarefa ${newStatus === 'concluída' ? 'concluída' : 'reaberta'} com sucesso!`)
  } catch (err) {
    const message = extractErrorMessage(err)
    showError(message, 6000)
  }
}

// Watch removido - erros são tratados diretamente nos catch blocks para evitar duplicação

onMounted(() => {
  tasksStore.fetchAll()
})
</script>

<template>
  <main class="container">
    <header class="page-header">
      <h1>Gerenciador de Tarefas</h1>
      <button type="button" class="btn-primary" @click="openCreateModal">
        <span class="btn-icon">+</span>
        Nova Tarefa
      </button>
    </header>

    <section class="card">
      <header class="list-header">
        <div class="header-info">
          <h2>Tarefas</h2>
          <div class="counts">
            <div class="count-item">
              <span class="count-label">Total</span>
              <span class="count-badge total">{{ items.length }}</span>
            </div>
            <div class="count-item">
              <span class="count-label">Pendentes</span>
              <span class="count-badge pending">{{ tasksStore.pendentesCount }}</span>
            </div>
            <div class="count-item">
              <span class="count-label">Concluídas</span>
              <span class="count-badge completed">{{ tasksStore.completedCount }}</span>
            </div>
          </div>
        </div>
      </header>

      <div v-if="loading" class="loading">Carregando...</div>

      <div v-else-if="items.length > 0" class="table-wrapper">
        <table class="tasks-table">
          <thead>
            <tr>
              <th class="toggle-header">Status</th>
              <th>Título</th>
              <th>Descrição</th>
              <th>Status</th>
              <th>Criação</th>
              <th>Edição</th>
              <th class="actions-header">Ações</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="task in items" :key="task.id" class="table-row">
              <td class="toggle-cell">
                <button
                  type="button"
                  class="btn-toggle"
                  :class="task.status === 'concluída' ? 'btn-toggle-open' : 'btn-toggle-close'"
                  @click="toggle(task)"
                  :title="task.status === 'pendente' ? 'Fechar tarefa' : 'Abrir tarefa'"
                >
                  <svg v-if="task.status === 'pendente'" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polyline points="20 6 9 17 4 12"></polyline>
                  </svg>
                  <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M3 12h18M12 3v18"></path>
                  </svg>
                  <span class="btn-toggle-text">{{ task.status === 'pendente' ? 'Fechar' : 'Abrir' }}</span>
                </button>
              </td>
              <td class="task-title">{{ task.title }}</td>
              <td class="task-desc">{{ task.description || '—' }}</td>
              <td>
                <span class="chip" :class="task.status === 'concluída' ? 'done' : 'pending'">
                  {{ capitalizeStatus(task.status) }}
                </span>
              </td>
              <td class="task-date">{{ formatDate((task as any).createdAt || (task as any).created_at) }}</td>
              <td class="task-date">{{ formatDate((task as any).updatedAt || (task as any).updated_at || (task as any).modifiedAt || (task as any).modified_at) }}</td>
              <td class="actions-cell">
                <div class="row-actions">
                  <button type="button" class="btn-icon-small" @click="openEditModal(task)" title="Editar">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                      <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                    </svg>
                  </button>
                  <button type="button" class="btn-icon-small danger" @click="openDeleteModal(task)" title="Excluir">
                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <polyline points="3 6 5 6 21 6"></polyline>
                      <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                    </svg>
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-else class="empty">
        Nenhuma tarefa cadastrada.
      </div>
    </section>
  </main>
  <!-- Small UX: keep footer space -->
  <div style="height: 24px" />

  <!-- Modal de Criação -->
  <div v-if="showCreateModal" class="modal-overlay" @click.self="closeCreateModal">
    <div class="modal-content">
      <div class="modal-header">
        <h2>Nova Tarefa</h2>
        <button type="button" class="modal-close" @click="closeCreateModal">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="18" y1="6" x2="6" y2="18"></line>
            <line x1="6" y1="6" x2="18" y2="18"></line>
          </svg>
        </button>
      </div>
      <form class="modal-form" @submit.prevent="submitForm">
        <div class="field">
          <label for="title">Título</label>
          <input id="title" v-model="form.title" type="text" placeholder="Informe o título" required />
        </div>
        <div class="field">
          <label for="description">Descrição</label>
          <textarea id="description" v-model="form.description" rows="4" placeholder="Detalhes (opcional)"></textarea>
        </div>
        <div class="field">
          <label for="status">Status</label>
          <select id="status" v-model="form.status">
            <option v-for="s in statuses" :key="s" :value="s">{{ capitalizeStatus(s) }}</option>
          </select>
        </div>
        <div class="modal-actions">
          <button type="button" class="btn-secondary" @click="closeCreateModal">Cancelar</button>
          <button type="submit" class="btn-primary" :disabled="!isValid || loading">Criar</button>
        </div>
      </form>
    </div>
  </div>

  <!-- Modal de Edição -->
  <div v-if="showEditModal" class="modal-overlay" @click.self="closeEditModal">
    <div class="modal-content">
      <div class="modal-header">
        <h2>Editar Tarefa</h2>
        <button type="button" class="modal-close" @click="closeEditModal">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="18" y1="6" x2="6" y2="18"></line>
            <line x1="6" y1="6" x2="18" y2="18"></line>
          </svg>
        </button>
      </div>
      <form class="modal-form" @submit.prevent="submitEdit">
        <div class="field">
          <label for="edit-title">Título</label>
          <input id="edit-title" v-model="editForm.title" type="text" placeholder="Informe o título" required />
        </div>
        <div class="field">
          <label for="edit-description">Descrição</label>
          <textarea id="edit-description" v-model="editForm.description" rows="4" placeholder="Detalhes (opcional)"></textarea>
        </div>
        <div class="field">
          <label for="edit-status">Status</label>
          <select id="edit-status" v-model="editForm.status">
            <option v-for="s in statuses" :key="s" :value="s">{{ capitalizeStatus(s) }}</option>
          </select>
        </div>
        <div class="modal-actions">
          <button type="button" class="btn-secondary" @click="closeEditModal">Cancelar</button>
          <button type="submit" class="btn-primary" :disabled="!isEditValid || loading">Salvar</button>
        </div>
      </form>
    </div>
  </div>

  <!-- Modal de Exclusão -->
  <div v-if="showDeleteModal" class="modal-overlay" @click.self="closeDeleteModal">
    <div class="modal-content modal-small">
      <div class="modal-header">
        <h2>Excluir Tarefa</h2>
        <button type="button" class="modal-close" @click="closeDeleteModal">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="18" y1="6" x2="6" y2="18"></line>
            <line x1="6" y1="6" x2="18" y2="18"></line>
          </svg>
        </button>
      </div>
      <div class="modal-body">
        <p>Tem certeza que deseja excluir a tarefa <strong>{{ taskToDelete?.title }}</strong>?</p>
        <p class="modal-warning">Esta ação não pode ser desfeita.</p>
      </div>
      <div class="modal-actions">
        <button type="button" class="btn-secondary" @click="closeDeleteModal">Cancelar</button>
        <button type="button" class="btn-danger" @click="confirmDelete" :disabled="loading">Excluir</button>
      </div>
    </div>
  </div>

  <!-- Toast Container -->
  <div class="toast-container">
    <TransitionGroup name="toast" tag="div">
      <div
        v-for="toast in toasts"
        :key="toast.id"
        class="toast"
        :class="`toast-${toast.type}`"
        @click="removeToast(toast.id)"
      >
        <div class="toast-content">
          <span class="toast-message">{{ toast.message }}</span>
          <button type="button" class="toast-close" @click.stop="removeToast(toast.id)">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>
      </div>
    </TransitionGroup>
  </div>
</template>

<style scoped>
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem 1rem;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  gap: 1rem;
}

.page-header h1 {
  margin: 0;
  font-size: 2rem;
  font-weight: 600;
  color: var(--color-heading);
}

.btn-primary {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 0.75rem 1.5rem;
  background: #0ea5e9;
  color: white;
  border: 0;
  border-radius: 8px;
  cursor: pointer;
  font-family: inherit;
  font-size: 1rem;
  font-weight: 500;
  transition: all 0.2s;
  box-shadow: 0 2px 4px rgba(14, 165, 233, 0.2);
  line-height: 1.5;
}

.btn-primary:hover {
  background: #0284c7;
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(14, 165, 233, 0.3);
}

.btn-primary:active {
  transform: translateY(0);
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.btn-icon {
  font-size: 1.5rem;
  line-height: 1;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-weight: 300;
  vertical-align: middle;
  margin-top: -0.1em;
}

@media (prefers-color-scheme: dark) {
  .btn-primary {
    background: #14b8a6;
    box-shadow: 0 2px 4px rgba(20, 184, 166, 0.2);
  }
  .btn-primary:hover {
    background: #0d9488;
    box-shadow: 0 4px 8px rgba(20, 184, 166, 0.3);
  }
}

.card {
  background: var(--color-background);
  border: 1px solid var(--color-border);
  border-radius: 12px;
  padding: 0;
  margin-bottom: 1rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}


.list-header {
  padding: 1.5rem;
  border-bottom: 1px solid var(--color-border);
}

.header-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 1rem;
}

.header-info h2 {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--color-heading);
}

.counts {
  display: flex;
  gap: 1.25rem;
  flex-wrap: wrap;
}

.count-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.375rem;
}

.count-label {
  font-size: 0.75rem;
  font-weight: 500;
  color: var(--color-text);
  opacity: 0.7;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.count-badge {
  padding: 0.5rem 0.875rem;
  border-radius: 6px;
  font-size: 1.125rem;
  font-weight: 600;
  background: var(--color-background-soft);
  border: 1px solid var(--color-border);
  color: var(--color-text);
  min-width: 2.5rem;
  text-align: center;
}

.count-badge.total {
  background: color-mix(in oklab, #6366f1 10%, transparent);
  border-color: color-mix(in oklab, #6366f1 30%, transparent);
  color: #6366f1;
}

.count-badge.pending {
  background: color-mix(in oklab, #f59e0b 10%, transparent);
  border-color: color-mix(in oklab, #f59e0b 30%, transparent);
  color: #f59e0b;
}

.count-badge.completed {
  background: color-mix(in oklab, #22c55e 10%, transparent);
  border-color: color-mix(in oklab, #22c55e 30%, transparent);
  color: #22c55e;
}

.loading, .empty {
  padding: 3rem 1.5rem;
  text-align: center;
  color: var(--color-text);
  font-size: 1rem;
}

.alert.error {
  margin: 1.5rem;
  padding: 0.875rem 1rem;
  border-radius: 8px;
  color: #ef4444;
  background: color-mix(in oklab, #ef4444 10%, transparent);
  border: 1px solid color-mix(in oklab, #ef4444 30%, transparent);
  font-size: 0.9rem;
}

.table-wrapper {
  overflow-x: auto;
}

.tasks-table {
  width: 100%;
  border-collapse: collapse;
}

.tasks-table thead {
  background: var(--color-background-soft);
}

.tasks-table th {
  padding: 1rem 1.5rem;
  text-align: left;
  font-weight: 600;
  font-size: 0.875rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  color: var(--color-text);
  border-bottom: 2px solid var(--color-border);
}

.tasks-table td {
  padding: 1.25rem 1.5rem;
  border-bottom: 1px solid var(--color-border);
  vertical-align: middle;
}

.table-row {
  transition: background 0.15s;
}

.table-row:hover {
  background: var(--color-background-soft);
}

.task-title {
  font-weight: 500;
  color: var(--color-heading);
}

.task-desc {
  color: var(--color-text);
  font-size: 0.9rem;
  max-width: 400px;
  white-space: pre-wrap;
  word-break: break-word;
  opacity: 0.8;
}

.task-date {
  font-size: 0.875rem;
  color: var(--color-text);
  opacity: 0.7;
  white-space: nowrap;
}

.toggle-header {
  text-align: center;
  width: 120px;
}

.toggle-cell {
  text-align: center;
  padding: 1rem 0.75rem;
}

.btn-toggle {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 0.75rem;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  background: var(--color-background);
  color: var(--color-text);
  cursor: pointer;
  font-family: inherit;
  font-size: 0.875rem;
  font-weight: 500;
  transition: all 0.2s;
  white-space: nowrap;
}

.btn-toggle:hover {
  background: var(--color-background-soft);
  border-color: var(--color-border-hover);
  transform: translateY(-1px);
}

.btn-toggle-close {
  border-color: color-mix(in oklab, #22c55e 30%, transparent);
  color: #16a34a;
}

.btn-toggle-close:hover {
  background: color-mix(in oklab, #22c55e 10%, transparent);
  border-color: #22c55e;
}

.btn-toggle-open {
  border-color: color-mix(in oklab, #f59e0b 30%, transparent);
  color: #d97706;
}

.btn-toggle-open:hover {
  background: color-mix(in oklab, #f59e0b 10%, transparent);
  border-color: #f59e0b;
}

.btn-toggle svg {
  flex-shrink: 0;
}

.btn-toggle-text {
  font-size: 0.875rem;
}

.actions-header {
  text-align: center;
}

.actions-cell {
  text-align: center;
}
.chip {
  display: inline-block;
  padding: 0.375rem 0.75rem;
  border-radius: 6px;
  font-size: 0.875rem;
  font-weight: 500;
  border: 1px solid;
}

.chip.done {
  background: color-mix(in oklab, #22c55e 12%, transparent);
  border-color: color-mix(in oklab, #22c55e 30%, transparent);
  color: #16a34a;
}

.chip.pending {
  background: color-mix(in oklab, #f59e0b 12%, transparent);
  border-color: color-mix(in oklab, #f59e0b 30%, transparent);
  color: #d97706;
}

.row-actions {
  display: flex;
  gap: 0.5rem;
  justify-content: center;
  align-items: center;
}

.btn-icon-small {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  padding: 0;
  background: transparent;
  border: 1px solid var(--color-border);
  border-radius: 6px;
  cursor: pointer;
  color: var(--color-text);
  transition: all 0.2s;
}

.btn-icon-small:hover {
  background: var(--color-background-soft);
  border-color: var(--color-border-hover);
  transform: translateY(-1px);
}

.btn-icon-small.danger {
  color: #ef4444;
  border-color: color-mix(in oklab, #ef4444 30%, transparent);
}

.btn-icon-small.danger:hover {
  background: color-mix(in oklab, #ef4444 10%, transparent);
  border-color: #ef4444;
}

.btn-icon-small svg {
  flex-shrink: 0;
}

/* Modal Styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 1rem;
  animation: fadeIn 0.2s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.modal-content {
  background: var(--color-background);
  border-radius: 16px;
  width: 100%;
  max-width: 520px;
  max-height: 90vh;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  animation: slideUp 0.3s ease-out;
  display: flex;
  flex-direction: column;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.modal-small {
  max-width: 420px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid var(--color-border);
}

.modal-header h2 {
  margin: 0;
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--color-heading);
}

.modal-close {
  background: transparent;
  border: 0;
  color: var(--color-text);
  cursor: pointer;
  padding: 0.5rem;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  transition: all 0.2s;
  opacity: 0.7;
}

.modal-close:hover {
  background: var(--color-background-soft);
  opacity: 1;
}

.modal-close svg {
  width: 20px;
  height: 20px;
}

.modal-form {
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
  overflow-y: auto;
}

.modal-body {
  padding: 1.5rem;
}

.modal-body p {
  margin: 0 0 1rem;
  color: var(--color-text);
  line-height: 1.6;
}

.modal-body p:last-child {
  margin-bottom: 0;
}

.modal-warning {
  color: #ef4444;
  font-size: 0.9rem;
  margin-top: 0.5rem;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.field label {
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--color-heading);
}

input, textarea, select {
  padding: 0.75rem;
  background: var(--color-background);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  color: var(--color-text);
  font-family: inherit;
  font-size: 1rem;
  line-height: 1.5;
  box-sizing: border-box;
  transition: all 0.2s;
  width: 100%;
}

input:focus, textarea:focus, select:focus {
  outline: none;
  border-color: #0ea5e9;
  box-shadow: 0 0 0 3px rgba(14, 165, 233, 0.1);
}

textarea {
  resize: vertical;
  min-height: 100px;
}

.modal-actions {
  display: flex;
  gap: 0.75rem;
  justify-content: flex-end;
  padding: 1.5rem;
  border-top: 1px solid var(--color-border);
  margin-top: auto;
}

.btn-secondary {
  padding: 0.75rem 1.5rem;
  background: transparent;
  color: var(--color-text);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  cursor: pointer;
  font-family: inherit;
  font-size: 1rem;
  font-weight: 500;
  transition: all 0.2s;
}

.btn-secondary:hover {
  background: var(--color-background-soft);
  border-color: var(--color-border-hover);
}

.btn-danger {
  padding: 0.75rem 1.5rem;
  background: #ef4444;
  color: white;
  border: 0;
  border-radius: 8px;
  cursor: pointer;
  font-family: inherit;
  font-size: 1rem;
  font-weight: 500;
  transition: all 0.2s;
}

.btn-danger:hover {
  background: #dc2626;
}

.btn-danger:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: stretch;
  }

  .page-header h1 {
    font-size: 1.75rem;
  }

  .btn-primary {
    width: 100%;
    justify-content: center;
  }

  .header-info {
    flex-direction: column;
    align-items: flex-start;
  }

  .tasks-table {
    font-size: 0.875rem;
  }

  .tasks-table th,
  .tasks-table td {
    padding: 0.875rem 1rem;
  }

  .task-desc {
    max-width: 200px;
  }

  .task-date {
    font-size: 0.75rem;
  }

  .tasks-table th:nth-child(5),
  .tasks-table th:nth-child(6) {
    display: none;
  }

  .tasks-table td:nth-child(5),
  .tasks-table td:nth-child(6) {
    display: none;
  }

  .btn-toggle-text {
    display: none;
  }

  .btn-toggle {
    padding: 0.5rem;
    min-width: 36px;
  }

  .toggle-header {
    width: auto;
  }

  .modal-content {
    max-width: 100%;
    border-radius: 16px 16px 0 0;
    margin-top: auto;
  }

  .modal-overlay {
    align-items: flex-end;
    padding: 0;
  }
}

/* Toast Styles */
.toast-container {
  position: fixed;
  top: 1rem;
  right: 1rem;
  z-index: 2000;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  max-width: 400px;
  pointer-events: none;
}

.toast {
  pointer-events: auto;
  background: var(--color-background);
  border: 1px solid var(--color-border);
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  cursor: pointer;
  transition: all 0.3s ease;
  animation: slideInRight 0.3s ease-out;
}

.toast:hover {
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.2);
  transform: translateX(-4px);
}

.toast-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  padding: 1rem 1.25rem;
}

.toast-message {
  flex: 1;
  font-size: 0.9rem;
  line-height: 1.5;
  color: var(--color-text);
}

.toast-close {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  padding: 0;
  background: transparent;
  border: 0;
  color: var(--color-text);
  opacity: 0.6;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.2s;
  flex-shrink: 0;
}

.toast-close:hover {
  opacity: 1;
  background: var(--color-background-soft);
}

.toast-error {
  border-left: 4px solid #ef4444;
  background: color-mix(in oklab, #ef4444 5%, var(--color-background));
}

.toast-success {
  border-left: 4px solid #22c55e;
  background: color-mix(in oklab, #22c55e 5%, var(--color-background));
}

.toast-info {
  border-left: 4px solid #0ea5e9;
  background: color-mix(in oklab, #0ea5e9 5%, var(--color-background));
}

.toast-warning {
  border-left: 4px solid #f59e0b;
  background: color-mix(in oklab, #f59e0b 5%, var(--color-background));
}

@keyframes slideInRight {
  from {
    opacity: 0;
    transform: translateX(100%);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s ease;
}

.toast-enter-from {
  opacity: 0;
  transform: translateX(100%);
}

.toast-leave-to {
  opacity: 0;
  transform: translateX(100%);
}

.toast-move {
  transition: transform 0.3s ease;
}

@media (max-width: 768px) {
  .toast-container {
    top: 0.5rem;
    right: 0.5rem;
    left: 0.5rem;
    max-width: none;
  }
}
</style>


