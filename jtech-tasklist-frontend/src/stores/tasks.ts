import { defineStore } from 'pinia';
import type { Task, TaskRequest, TaskStatus } from '@/types/task';
import { listTasks, createTask, updateTask, deleteTask } from '@/services/tasks';

interface TasksState {
	items: Task[];
	loading: boolean;
	errorMessage: string | null;
}

export const useTasksStore = defineStore('tasks', {
	state: (): TasksState => ({
		items: [],
		loading: false,
		errorMessage: null
	}),
	getters: {
		totalTasks(state): number {
			return state.items.length;
		},
		completedCount(state): number {
			return state.items.filter((t) => t.status === 'concluída').length;
		},
		pendentesCount(state): number {
			return state.items.filter((t) => t.status === 'pendente').length;
		}
	},
	actions: {
		setError(message: string | null) {
			this.errorMessage = message;
		},
		async fetchAll(): Promise<void> {
			this.loading = true;
			this.setError(null);
			try {
				this.items = await listTasks();
			} catch (error) {
				this.setError(formatErrorMessage(error));
			} finally {
				this.loading = false;
			}
		},
		async addTask(input: TaskRequest): Promise<void> {
			this.setError(null);
			try {
				const created = await createTask(input);
				this.items.unshift(created);
			} catch (error) {
				this.setError(formatErrorMessage(error));
				throw error;
			}
		},
		async updateTask(taskId: string, input: TaskRequest): Promise<void> {
			this.setError(null);
			try {
				const updated = await updateTask(taskId, input);
				const index = this.items.findIndex((t) => t.id === taskId);
				if (index >= 0) {
					this.items.splice(index, 1, updated);
				} else {
					// If it wasn't in the list, prepend it
					this.items.unshift(updated);
				}
			} catch (error) {
				this.setError(formatErrorMessage(error));
				throw error;
			}
		},
		async removeTask(taskId: string): Promise<void> {
			this.setError(null);
			try {
				await deleteTask(taskId);
				this.items = this.items.filter((t) => t.id !== taskId);
			} catch (error) {
				this.setError(formatErrorMessage(error));
				throw error;
			}
		},
		async toggleStatus(task: Task): Promise<void> {
			const nextStatus: TaskStatus = task.status === 'pendente' ? 'concluída' : 'pendente';
			await this.updateTask(task.id, {
				title: task.title,
				description: task.description,
				status: nextStatus
			});
		}
	}
});

function formatErrorMessage(error: unknown): string {
	if (!error) return 'Erro desconhecido';
	if (typeof error === 'string') return error;
	if (typeof error === 'object') {
		const maybe = error as {
			message?: string;
			status?: number | string;
			details?: unknown;
			debugMessage?: string;
		};

		// Primeiro tenta pegar a mensagem diretamente (já extraída pelo http.ts)
		if (maybe.message) return maybe.message;

		// Tenta extrair a mensagem do formato da API (com status, message, debugMessage)
		if (maybe.details && typeof maybe.details === 'object') {
			const details = maybe.details as { message?: string; debugMessage?: string; status?: string };
			if (details.message) return details.message;
			if (details.debugMessage) return details.debugMessage;
		}

		// Fallback para debugMessage
		if (maybe.debugMessage) return maybe.debugMessage;
	}
	return 'Falha ao comunicar com a API';
}


