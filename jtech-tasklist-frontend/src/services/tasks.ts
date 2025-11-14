import { apiFetch } from './http';
import type { Task, TaskRequest } from '@/types/task';

export async function listTasks(): Promise<Task[]> {
	return await apiFetch<Task[]>('/tasks');
}

export async function getTaskById(taskId: string): Promise<Task> {
	return await apiFetch<Task>(`/tasks/${encodeURIComponent(taskId)}`);
}

export async function createTask(payload: TaskRequest): Promise<Task> {
	return await apiFetch<Task>('/tasks', {
		method: 'POST',
		body: payload
	});
}

export async function updateTask(taskId: string, payload: TaskRequest): Promise<Task> {
	return await apiFetch<Task>(`/tasks/${encodeURIComponent(taskId)}`, {
		method: 'PUT',
		body: payload
	});
}

export async function deleteTask(taskId: string): Promise<void> {
	await apiFetch<void>(`/tasks/${encodeURIComponent(taskId)}`, {
		method: 'DELETE'
	});
}


