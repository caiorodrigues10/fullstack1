export type TaskStatus = 'pendente' | 'concluída';

export interface Task {
	id: string;
	title: string;
	description: string;
	status: TaskStatus;
	// Accept additional fields from backend without relying on them
	[key: string]: unknown;
}

export interface TaskRequest {
	title: string;
	description: string;
	status: TaskStatus;
}

export interface TaskResponse extends Task {}


